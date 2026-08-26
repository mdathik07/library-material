package com.example.readinglibrary.dto;

import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.enums.MaterialVersionStatus;

public record CreateMaterialResponse(
        Long materialId,
        Long versionId,
        Integer versionNumber,
        String title,
        MaterialKind kind,
        MaterialVersionStatus status
) {
}