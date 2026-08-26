package com.example.readinglibrary.dto;

public record ReaderProgressResponse(
        Long materialVersionId,
        Integer position,
        boolean completed
) {
}