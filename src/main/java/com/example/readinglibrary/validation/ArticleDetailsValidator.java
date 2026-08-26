package com.example.readinglibrary.validation;

import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.exception.InvalidMaterialDetailsException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class ArticleDetailsValidator implements MaterialDetailsValidator {

    @Override
    public MaterialKind supportedKind() {
        return MaterialKind.ARTICLE;
    }

    @Override
    public void validate(JsonNode details) {
        if (details == null || !details.isObject()) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_ARTICLE_DETAILS",
                    "Article details must be an object."
            );
        }

        JsonNode wordCount = details.get("wordCount");

        if (wordCount == null || wordCount.isNull()) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_ARTICLE_DETAILS",
                    "wordCount is required for ARTICLE material."
            );
        }

        if (!wordCount.isIntegralNumber()) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_ARTICLE_DETAILS",
                    "wordCount must be an integer for ARTICLE material."
            );
        }

        if (wordCount.asInt() < 0) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_ARTICLE_DETAILS",
                    "wordCount cannot be negative for ARTICLE material."
            );
        }
    }
}