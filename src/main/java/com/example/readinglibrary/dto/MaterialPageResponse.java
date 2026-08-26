package com.example.readinglibrary.dto;

import java.util.List;

public record MaterialPageResponse(
        List<MaterialListItem> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}