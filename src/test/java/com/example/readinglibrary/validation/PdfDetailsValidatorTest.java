package com.example.readinglibrary.validation;

import com.example.readinglibrary.exception.InvalidMaterialDetailsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfDetailsValidatorTest {

    private final PdfDetailsValidator validator =
            new PdfDetailsValidator();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Test
    void shouldAcceptValidPdfDetails() throws Exception {
        var details = objectMapper.readTree("""
                {
                  "pageCount": 50,
                  "fileUrl": "https://example.com/book.pdf"
                }
                """);

        assertThatCode(() -> validator.validate(details))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectMissingPageCount() throws Exception {
        var details = objectMapper.readTree("""
                {
                  "fileUrl": "https://example.com/book.pdf"
                }
                """);

        assertThatThrownBy(() -> validator.validate(details))
                .isInstanceOf(InvalidMaterialDetailsException.class)
                .hasMessage("pageCount is required for PDF material.");
    }

    @Test
    void shouldRejectNonPositivePageCount() throws Exception {
        var details = objectMapper.readTree("""
                {
                  "pageCount": 0,
                  "fileUrl": "https://example.com/book.pdf"
                }
                """);

        assertThatThrownBy(() -> validator.validate(details))
                .isInstanceOf(InvalidMaterialDetailsException.class)
                .hasMessage(
                        "pageCount must be greater than 0 for PDF material."
                );
    }

    @Test
    void shouldRejectMissingFileUrl() throws Exception {
        var details = objectMapper.readTree("""
                {
                  "pageCount": 50
                }
                """);

        assertThatThrownBy(() -> validator.validate(details))
                .isInstanceOf(InvalidMaterialDetailsException.class)
                .hasMessage("fileUrl is required for PDF material.");
    }
}