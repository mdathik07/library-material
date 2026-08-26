package com.example.readinglibrary.validation;

import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.exception.InvalidMaterialDetailsException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class BookDetailsValidator implements MaterialDetailsValidator {

    @Override
    public MaterialKind supportedKind() {
        return MaterialKind.BOOK;
    }

    @Override
    public void validate(JsonNode details) {
        if (details == null || !details.isObject()) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_BOOK_DETAILS",
                    "Book details must be an object."
            );
        }

        JsonNode author = details.get("author");

        if (author == null
                || author.isNull()
                || !author.isTextual()
                || author.asText().isBlank()) {

            throw new InvalidMaterialDetailsException(
                    "INVALID_BOOK_DETAILS",
                    "author is required for BOOK material."
            );
        }

        JsonNode chapters = details.get("chapters");

        if (chapters == null || chapters.isNull()) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_BOOK_DETAILS",
                    "chapters are required for BOOK material."
            );
        }

        if (!chapters.isArray()) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_BOOK_DETAILS",
                    "chapters must be an array for BOOK material."
            );
        }

        if (chapters.isEmpty()) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_BOOK_DETAILS",
                    "chapters must contain at least one chapter."
            );
        }

        for (int i = 0; i < chapters.size(); i++) {
            JsonNode chapter = chapters.get(i);

            if (chapter == null
                    || chapter.isNull()
                    || !chapter.isTextual()
                    || chapter.asText().isBlank()) {

                throw new InvalidMaterialDetailsException(
                        "INVALID_BOOK_DETAILS",
                        "chapter at index " + i
                                + " must contain a non-empty name."
                );
            }
        }
    }
}