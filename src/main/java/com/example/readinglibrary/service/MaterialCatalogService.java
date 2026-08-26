package com.example.readinglibrary.service;

import com.example.readinglibrary.dto.MaterialListItem;
import com.example.readinglibrary.dto.MaterialPageResponse;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.repository.MaterialCatalogProjection;
import com.example.readinglibrary.repository.MaterialRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MaterialCatalogService {

    private final MaterialRepository materialRepository;

    public MaterialCatalogService(
            MaterialRepository materialRepository
    ) {
        this.materialRepository = materialRepository;
    }

    @Transactional(readOnly = true)
    public MaterialPageResponse searchMaterials(
            MaterialKind kind,
            Integer minPages,
            Pageable pageable
    ) {
        validateSearchParameters(kind, minPages);

        String kindValue =
                kind == null
                        ? null
                        : kind.name();

        Page<MaterialCatalogProjection> page =
                materialRepository.findLiveCatalog(
                        kindValue,
                        minPages,
                        pageable
                );

        List<MaterialListItem> content =
                page.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return new MaterialPageResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private void validateSearchParameters(
            MaterialKind kind,
            Integer minPages
    ) {
        if (minPages != null && minPages <= 0) {
            throw new IllegalArgumentException(
                    "minPages must be greater than 0"
            );
        }

        if (minPages != null
                && kind != null
                && kind != MaterialKind.PDF) {

            throw new IllegalArgumentException(
                    "minPages can only be used with PDF material."
            );
        }

        if (minPages != null && kind == null) {
            throw new IllegalArgumentException(
                    "kind=PDF is required when minPages is provided."
            );
        }
    }

    private MaterialListItem toResponse(
            MaterialCatalogProjection projection
    ) {
        MaterialKind kind =
                MaterialKind.valueOf(
                        projection.getKind()
                );

        return new MaterialListItem(
                projection.getMaterialId(),
                projection.getTitle(),
                kind,
                projection.getVersionId(),
                projection.getVersionNumber(),
                projection.getPdfPageCount()
        );
    }
}