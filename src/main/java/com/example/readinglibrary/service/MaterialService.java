package com.example.readinglibrary.service;

import com.example.readinglibrary.dto.*;
import com.example.readinglibrary.entity.Material;
import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.enums.MaterialVersionStatus;
import com.example.readinglibrary.exception.ConflictException;
import com.example.readinglibrary.exception.ResourceNotFoundException;
import com.example.readinglibrary.repository.MaterialRepository;
import com.example.readinglibrary.repository.MaterialVersionRepository;
import com.example.readinglibrary.validation.MaterialDetailsValidatorFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialVersionRepository materialVersionRepository;
    private final MaterialDetailsValidatorFactory validatorFactory;
    private final MaterialDetailsCreatorFactory creatorFactory;
    private final MaterialDetailsCopierFactory copierFactory;
    private final MaterialDetailsUpdaterFactory updaterFactory;

    public MaterialService(
            MaterialRepository materialRepository,
            MaterialVersionRepository materialVersionRepository,
            MaterialDetailsValidatorFactory validatorFactory,
            MaterialDetailsCreatorFactory creatorFactory,
            MaterialDetailsCopierFactory copierFactory,
            MaterialDetailsUpdaterFactory updaterFactory
    ) {
        this.materialRepository = materialRepository;
        this.materialVersionRepository = materialVersionRepository;
        this.validatorFactory = validatorFactory;
        this.creatorFactory = creatorFactory;
        this.copierFactory = copierFactory;
        this.updaterFactory = updaterFactory;
    }

    @Transactional
    public CreateMaterialResponse createMaterial(
            CreateMaterialRequest request
    ) {
        // 1. Validate kind-specific details.
        validatorFactory
                .getValidator(request.kind())
                .validate(request.details());

        // 2. Create the logical material.
        Material material =
                new Material(
                        request.title().trim(),
                        request.kind()
                );

        materialRepository.save(material);

        // 3. Create version 1 as DRAFT.
        MaterialVersion version =
                new MaterialVersion(
                        1,
                        MaterialVersionStatus.DRAFT
                );

        material.addVersion(version);

        materialVersionRepository.save(version);

        // 4. Create kind-specific details.
        creatorFactory
                .getCreator(request.kind())
                .createDetails(
                        version,
                        request.details()
                );

        // 5. Return API response.
        return new CreateMaterialResponse(
                material.getId(),
                version.getId(),
                version.getVersionNumber(),
                material.getTitle(),
                material.getKind(),
                version.getStatus()
        );
    }

    @Transactional
    public CreateVersionResponse createDraftVersion(
            Long materialId
    ) {
        /*
         * Lock the material row.
         *
         * This prevents concurrent requests for the same material
         * from calculating the same next version number.
         */
        Material material =
                materialRepository.findByIdForUpdate(materialId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "MATERIAL_NOT_FOUND",
                                        "Material not found: " + materialId
                                )
                        );

        MaterialVersion liveVersion =
                materialVersionRepository
                        .findByMaterialIdAndStatus(
                                materialId,
                                MaterialVersionStatus.LIVE
                        )
                        .orElseThrow(() ->
                                new ConflictException(
                                        "MATERIAL_NO_LIVE_VERSION",
                                        "Material has no live version: "
                                                + materialId
                                )
                        );

        Integer maxVersionNumber =
                materialVersionRepository
                        .findMaxVersionNumber(materialId);

        int nextVersionNumber =
                maxVersionNumber + 1;

        MaterialVersion draftVersion =
                new MaterialVersion(
                        nextVersionNumber,
                        MaterialVersionStatus.DRAFT
                );

        material.addVersion(draftVersion);

        materialVersionRepository.save(draftVersion);

        copierFactory
                .getCopier(material.getKind())
                .copyDetails(
                        liveVersion,
                        draftVersion
                );

        return new CreateVersionResponse(
                material.getId(),
                draftVersion.getId(),
                draftVersion.getVersionNumber(),
                material.getTitle(),
                material.getKind(),
                draftVersion.getStatus()
        );
    }

    @Transactional
    public void updateDraftVersion(
            Long materialId,
            Long versionId,
            UpdateMaterialVersionRequest request
    ) {
        MaterialVersion version =
                materialVersionRepository
                        .findById(versionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "VERSION_NOT_FOUND",
                                        "Version not found: " + versionId
                                )
                        );

        if (!version.getMaterial().getId().equals(materialId)) {
            throw new ResourceNotFoundException(
                    "VERSION_WRONG_MATERIAL",
                    "Version " + versionId
                            + " does not belong to material "
                            + materialId
            );
        }

        if (version.getStatus() != MaterialVersionStatus.DRAFT) {
            throw new ConflictException(
                    "VERSION_NOT_EDITABLE",
                    "Only DRAFT versions can be edited."
            );
        }

        validatorFactory
                .getValidator(version.getMaterial().getKind())
                .validate(request.details());

        updaterFactory
                .getUpdater(version.getMaterial().getKind())
                .updateDetails(
                        version,
                        request.details()
                );
    }

    @Transactional
    public PublishVersionResponse publishVersion(
            Long materialId,
            Long versionId
    ) {
        /*
         * Serialize all publication operations for this material.
         */
        Material material =
                materialRepository.findByIdForUpdate(materialId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "MATERIAL_NOT_FOUND",
                                        "Material not found: " + materialId
                                )
                        );

        MaterialVersion targetVersion =
                materialVersionRepository.findById(versionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "VERSION_NOT_FOUND",
                                        "Version not found: " + versionId
                                )
                        );

        /*
         * A version ID by itself is not enough.
         * Make sure it belongs to the material in the URL.
         */
        if (!targetVersion.getMaterial().getId().equals(materialId)) {
            throw new ResourceNotFoundException(
                    "VERSION_WRONG_MATERIAL",
                    "Version " + versionId
                            + " does not belong to material "
                            + materialId
            );
        }

        /*
         * A LIVE version is already published.
         */
        if (targetVersion.getStatus()
                == MaterialVersionStatus.LIVE) {

            throw new ConflictException(
                    "VERSION_ALREADY_LIVE",
                    "Version " + versionId + " is already LIVE."
            );
        }

        /*
         * Only DRAFT and ARCHIVED versions can become LIVE.
         *
         * ARCHIVED is allowed because this is how rollback works.
         */
        if (targetVersion.getStatus()
                != MaterialVersionStatus.DRAFT
                && targetVersion.getStatus()
                != MaterialVersionStatus.ARCHIVED) {

            throw new ConflictException(
                    "VERSION_NOT_PUBLISHABLE",
                    "Only DRAFT or ARCHIVED versions can be published."
            );
        }

        MaterialVersion currentLiveVersion =
                materialVersionRepository
                        .findByMaterialIdAndStatus(
                                materialId,
                                MaterialVersionStatus.LIVE
                        )
                        .orElse(null);

        /*
         * Remove the current LIVE version from the unique-indexed set
         * before making another version LIVE.
         *
         * The explicit flush is important because PostgreSQL has:
         *
         * UNIQUE INDEX ... WHERE status = 'LIVE'
         *
         * We want the old LIVE row to be ARCHIVED in the database
         * before the target row becomes LIVE.
         */
        if (currentLiveVersion != null) {
            currentLiveVersion.setStatus(
                    MaterialVersionStatus.ARCHIVED
            );

            materialVersionRepository.saveAndFlush(
                    currentLiveVersion
            );
        }

        /*
         * Publish target version.
         */
        targetVersion.setStatus(
                MaterialVersionStatus.LIVE
        );

        targetVersion.setPublishedAt(
                OffsetDateTime.now()
        );

        materialVersionRepository.saveAndFlush(
                targetVersion
        );

        return new PublishVersionResponse(
                material.getId(),
                targetVersion.getId(),
                targetVersion.getVersionNumber(),
                targetVersion.getStatus(),
                targetVersion.getPublishedAt()
        );
    }
}