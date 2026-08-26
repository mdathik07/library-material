package com.example.readinglibrary.validation;

import com.example.readinglibrary.exception.InvalidMaterialDetailsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookDetailsValidatorTest {

    private final BookDetailsValidator validator =
            new BookDetailsValidator();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Test
    void shouldAcceptValidBookDetails() throws Exception {
        var details = objectMapper.readTree("""
                {
                  "author": "Robert Martin",
                  "chapters": [
                    "Introduction",
                    "Meaningful Names",
                    "Functions"
                  ]
                }
                """);

        assertThatCode(() -> validator.validate(details))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectMissingAuthor() throws Exception {
        var details = objectMapper.readTree("""
                {
                  "chapters": [
                    "Introduction"
                  ]
                }
                """);

        assertThatThrownBy(() -> validator.validate(details))
                .isInstanceOf(InvalidMaterialDetailsException.class)
                .hasMessage(
                        "author is required for BOOK material."
                );
    }

    @Test
    void shouldRejectEmptyChapterList() throws Exception {
        var details = objectMapper.readTree("""
                {
                  "author": "Robert Martin",
                  "chapters": []
                }
                """);

        assertThatThrownBy(() -> validator.validate(details))
                .isInstanceOf(InvalidMaterialDetailsException.class)
                .hasMessage(
                        "chapters must contain at least one chapter."
                );
    }

    @Test
    void shouldRejectBlankChapterName() throws Exception {
        var details = objectMapper.readTree("""
                {
                  "author": "Robert Martin",
                  "chapters": [
                    "Introduction",
                    ""
                  ]
                }
                """);

        assertThatThrownBy(() -> validator.validate(details))
                .isInstanceOf(InvalidMaterialDetailsException.class)
                .hasMessage(
                        "chapter at index 1 must contain a non-empty name."
                );
    }
}