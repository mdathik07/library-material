package com.example.readinglibrary.dto;

import com.example.readinglibrary.enums.MaterialKind;

public record MaterialListItem(
        Long materialId,
        String title,
        MaterialKind kind,
        Long versionId,
        Integer versionNumber,
        Integer pdfPageCount
) {
}