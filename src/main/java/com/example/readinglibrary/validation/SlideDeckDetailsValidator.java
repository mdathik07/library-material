package com.example.readinglibrary.validation;

import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.exception.InvalidMaterialDetailsException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class SlideDeckDetailsValidator implements MaterialDetailsValidator {

    @Override
    public MaterialKind supportedKind() {
        return MaterialKind.SLIDE_DECK;
    }

    @Override
    public void validate(JsonNode details) {
        if (details == null || !details.isObject()) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_SLIDE_DECK_DETAILS",
                    "Slide deck details must be an object."
            );
        }

        JsonNode slideCount = details.get("slideCount");

        if (slideCount == null || slideCount.isNull()) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_SLIDE_DECK_DETAILS",
                    "slideCount is required for SLIDE_DECK material."
            );
        }

        if (!slideCount.isIntegralNumber()) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_SLIDE_DECK_DETAILS",
                    "slideCount must be an integer for SLIDE_DECK material."
            );
        }

        if (slideCount.asInt() <= 0) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_SLIDE_DECK_DETAILS",
                    "slideCount must be greater than 0 for SLIDE_DECK material."
            );
        }

        JsonNode fileUrl = details.get("fileUrl");

        if (fileUrl == null
                || fileUrl.isNull()
                || !fileUrl.isTextual()
                || fileUrl.asText().isBlank()) {

            throw new InvalidMaterialDetailsException(
                    "INVALID_SLIDE_DECK_DETAILS",
                    "fileUrl is required for SLIDE_DECK material."
            );
        }
    }
}