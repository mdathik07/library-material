package com.example.readinglibrary.dto;

import java.util.List;

public record ReaderMaterialPageResponse(
        List<ReaderMaterialListItem> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}