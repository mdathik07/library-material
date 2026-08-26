package com.example.readinglibrary.dto;

import com.example.readinglibrary.enums.MaterialKind;
import com.fasterxml.jackson.databind.JsonNode;

public record ReaderContinuationResponse(
        Long materialId,
        String title,
        MaterialKind kind,
        Long versionId,
        Integer versionNumber,
        JsonNode details,
        Integer position,
        boolean completed
) {
}