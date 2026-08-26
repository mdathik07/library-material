package com.example.readinglibrary.dto;

import com.example.readinglibrary.enums.MaterialVersionStatus;

import java.time.OffsetDateTime;

public record PublishVersionResponse(
        Long materialId,
        Long versionId,
        Integer versionNumber,
        MaterialVersionStatus status,
        OffsetDateTime publishedAt
) {
}