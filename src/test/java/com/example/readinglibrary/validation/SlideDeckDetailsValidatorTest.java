package com.example.readinglibrary.validation;

import com.example.readinglibrary.exception.InvalidMaterialDetailsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlideDeckDetailsValidatorTest {

    private final SlideDeckDetailsValidator validator =
            new SlideDeckDetailsValidator();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Test
    void shouldAcceptValidSlideDeckDetails() throws Exception {
        var details = objectMapper.readTree("""
                {
                  "slideCount": 30,
                  "fileUrl": "https://example.com/slides.pdf"
                }
                """);

        assertThatCode(() -> validator.validate(details))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectMissingSlideCount() throws Exception {
        var details = objectMapper.readTree("""
                {
                  "fileUrl": "https://example.com/slides.pdf"
                }
                """);

        assertThatThrownBy(() -> validator.validate(details))
                .isInstanceOf(InvalidMaterialDetailsException.class)
                .hasMessage(
                        "slideCount is required for SLIDE_DECK material."
                );
    }

    @Test
    void shouldRejectNonPositiveSlideCount() throws Exception {
        var details = objectMapper.readTree("""
                {
                  "slideCount": 0,
                  "fileUrl": "https://example.com/slides.pdf"
                }
                """);

        assertThatThrownBy(() -> validator.validate(details))
                .isInstanceOf(InvalidMaterialDetailsException.class)
                .hasMessage(
                        "slideCount must be greater than 0 for SLIDE_DECK material."
                );
    }
}