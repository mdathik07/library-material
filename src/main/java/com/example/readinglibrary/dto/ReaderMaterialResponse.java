package com.example.readinglibrary.dto;

import com.example.readinglibrary.enums.MaterialKind;

import com.fasterxml.jackson.databind.JsonNode;

public record ReaderMaterialResponse(
        Long materialId,
        String title,
        MaterialKind kind,
        Long versionId,
        Integer versionNumber,
        JsonNode details,
        ReaderProgressResponse progress
) {
}