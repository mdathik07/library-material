package com.example.readinglibrary.service;

import com.example.readinglibrary.dto.*;
import com.example.readinglibrary.entity.Material;
import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.entity.ReaderProgress;
import com.example.readinglibrary.enums.MaterialVersionStatus;
import com.example.readinglibrary.enums.ReaderMaterialStatus;
import com.example.readinglibrary.exception.ResourceNotFoundException;
import com.example.readinglibrary.repository.MaterialRepository;
import com.example.readinglibrary.repository.MaterialVersionRepository;
import com.example.readinglibrary.repository.ReaderProgressRepository;
import com.example.readinglibrary.reader.ReaderContext;
import com.example.readinglibrary.validation.ReaderPositionValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReaderMaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialVersionRepository materialVersionRepository;
    private final ReaderProgressRepository readerProgressRepository;
    private final ReaderMaterialResponseMapper responseMapper;
    private final ReaderPositionValidator readerPositionValidator;

    public ReaderMaterialService(
            MaterialRepository materialRepository,
            MaterialVersionRepository materialVersionRepository,
            ReaderProgressRepository readerProgressRepository,
            ReaderMaterialResponseMapper responseMapper,
            ReaderPositionValidator readerPositionValidator
    ) {
        this.materialRepository = materialRepository;
        this.materialVersionRepository = materialVersionRepository;
        this.readerProgressRepository = readerProgressRepository;
        this.responseMapper = responseMapper;
        this.readerPositionValidator = readerPositionValidator;
    }

    private ReaderMaterialListItem toReaderMaterialListItem(
            ReaderProgress progress
    ) {
        MaterialVersion version =
                progress.getMaterialVersion();

        Material material =
                version.getMaterial();

        return new ReaderMaterialListItem(
                material.getId(),
                material.getTitle(),
                material.getKind(),
                version.getId(),
                version.getVersionNumber(),
                progress.getPosition(),
                progress.isCompleted()
        );
    }

    @Transactional(readOnly = true)
    public ReaderMaterialResponse openMaterial(
            Long materialId,
            ReaderContext readerContext
    ) {
        Material material =
                materialRepository.findById(materialId)
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
                                new ResourceNotFoundException(
                                        "MATERIAL_NO_LIVE_VERSION",
                                        "Material has no LIVE version: "
                                                + materialId
                                )
                        );

        ReaderProgress progress =
                readerProgressRepository
                        .findByReaderIdAndMaterialVersionId(
                                readerContext.readerId(),
                                liveVersion.getId()
                        )
                        .orElse(null);

        return responseMapper.toResponse(
                material,
                liveVersion,
                progress
        );
    }

    @Transactional
    public SaveProgressResponse saveProgress(
            Long materialId,
            Long versionId,
            ReaderContext readerContext,
            SaveProgressRequest request
    ) {
        MaterialVersion version =
                materialVersionRepository.findById(versionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "VERSION_NOT_FOUND",
                                        "Version not found: " + versionId
                                )
                        );

        /*
         * The version must belong to the material from the URL.
         */
        if (!version.getMaterial().getId().equals(materialId)) {
            throw new ResourceNotFoundException(
                    "VERSION_WRONG_MATERIAL",
                    "Version " + versionId
                            + " does not belong to material "
                            + materialId
            );
        }

        /*
         * Validate position against THIS VERSION'S details.
         *
         * We intentionally do not look up the current LIVE version.
         */
        boolean completed =
                readerPositionValidator.validateAndGetCompleted(
                        version,
                        request.position()
                );

        ReaderProgress progress =
                readerProgressRepository
                        .findByReaderIdAndMaterialVersionId(
                                readerContext.readerId(),
                                versionId
                        )
                        .orElse(null);

        if (progress == null) {
            progress = new ReaderProgress(
                    readerContext.readerId(),
                    version,
                    request.position(),
                    completed
            );
        } else {
            progress.updateProgress(
                    request.position(),
                    completed
            );
        }

        readerProgressRepository.save(progress);

        return new SaveProgressResponse(
                materialId,
                version.getId(),
                version.getVersionNumber(),
                progress.getPosition(),
                progress.isCompleted()
        );
    }

    @Transactional(readOnly = true)
    public ReaderMaterialPageResponse getReaderMaterials(
            ReaderContext readerContext,
            ReaderMaterialStatus status,
            Pageable pageable
    ) {
        Page<ReaderProgress> progressPage;

        if (status == null) {
            progressPage =
                    readerProgressRepository.findByReaderId(
                            readerContext.readerId(),
                            pageable
                    );
        } else {
            boolean completed =
                    status == ReaderMaterialStatus.COMPLETED;

            progressPage =
                    readerProgressRepository
                            .findByReaderIdAndCompleted(
                                    readerContext.readerId(),
                                    completed,
                                    pageable
                            );
        }

        List<ReaderMaterialListItem> content =
                progressPage.getContent()
                        .stream()
                        .map(this::toReaderMaterialListItem)
                        .toList();

        return new ReaderMaterialPageResponse(
                content,
                progressPage.getNumber(),
                progressPage.getSize(),
                progressPage.getTotalElements(),
                progressPage.getTotalPages()
        );
    }
    @Transactional(readOnly = true)
    public ReaderContinuationResponse continueReading(
            Long materialId,
            ReaderContext readerContext
    ) {
        Material material =
                materialRepository.findById(materialId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "MATERIAL_NOT_FOUND",
                                        "Material not found: " + materialId
                                )
                        );

        List<ReaderProgress> progressList =
                readerProgressRepository
                        .findReaderProgressForMaterial(
                                readerContext.readerId(),
                                materialId
                        );

        ReaderProgress progress =
                progressList.stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "NO_READING_PROGRESS",
                                        "Reader has no saved progress for material: "
                                                + materialId
                                )
                        );

        MaterialVersion version =
                progress.getMaterialVersion();

        ReaderMaterialResponse materialResponse =
                responseMapper.toResponse(
                        material,
                        version,
                        progress
                );

        return new ReaderContinuationResponse(
                materialResponse.materialId(),
                materialResponse.title(),
                materialResponse.kind(),
                materialResponse.versionId(),
                materialResponse.versionNumber(),
                materialResponse.details(),
                progress.getPosition(),
                progress.isCompleted()
        );
    }
}