package com.example.readinglibrary.validation;

import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.exception.InvalidMaterialDetailsException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class PdfDetailsValidator implements MaterialDetailsValidator {

    @Override
    public MaterialKind supportedKind() {
        return MaterialKind.PDF;
    }

    @Override
    public void validate(JsonNode details) {
        if (details == null || !details.isObject()) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_PDF_DETAILS",
                    "PDF details must be an object."
            );
        }

        JsonNode pageCount = details.get("pageCount");

        if (pageCount == null || pageCount.isNull()) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_PDF_DETAILS",
                    "pageCount is required for PDF material."
            );
        }

        if (!pageCount.isIntegralNumber()) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_PDF_DETAILS",
                    "pageCount must be an integer for PDF material."
            );
        }

        if (pageCount.asInt() <= 0) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_PDF_DETAILS",
                    "pageCount must be greater than 0 for PDF material."
            );
        }

        JsonNode fileUrl = details.get("fileUrl");

        if (fileUrl == null
                || fileUrl.isNull()
                || !fileUrl.isTextual()
                || fileUrl.asText().isBlank()) {

            throw new InvalidMaterialDetailsException(
                    "INVALID_PDF_DETAILS",
                    "fileUrl is required for PDF material."
            );
        }
    }
}