package com.example.readinglibrary.dto;

public record SaveProgressResponse(
        Long materialId,
        Long materialVersionId,
        Integer versionNumber,
        Integer position,
        boolean completed
) {
}