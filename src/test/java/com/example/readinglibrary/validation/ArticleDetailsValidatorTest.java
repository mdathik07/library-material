package com.example.readinglibrary.validation;

import com.example.readinglibrary.exception.InvalidMaterialDetailsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArticleDetailsValidatorTest {

    private final ArticleDetailsValidator validator =
            new ArticleDetailsValidator();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Test
    void shouldAcceptValidArticleDetails() throws Exception {
        var details = objectMapper.readTree("""
                {
                  "wordCount": 5000
                }
                """);

        assertThatCode(() -> validator.validate(details))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectMissingWordCount() throws Exception {
        var details = objectMapper.readTree("""
                {}
                """);

        assertThatThrownBy(() -> validator.validate(details))
                .isInstanceOf(InvalidMaterialDetailsException.class)
                .hasMessage(
                        "wordCount is required for ARTICLE material."
                );
    }

    @Test
    void shouldRejectNegativeWordCount() throws Exception {
        var details = objectMapper.readTree("""
                {
                  "wordCount": -1
                }
                """);

        assertThatThrownBy(() -> validator.validate(details))
                .isInstanceOf(InvalidMaterialDetailsException.class)
                .hasMessage(
                        "wordCount cannot be negative for ARTICLE material."
                );
    }
}