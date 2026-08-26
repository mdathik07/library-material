package com.example.readinglibrary.validation;

import com.example.readinglibrary.entity.*;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.enums.MaterialVersionStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReaderPositionValidatorTest {

    private final ReaderPositionValidator validator =
            new ReaderPositionValidator();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Test
    void shouldValidatePdfPosition() {
        Material material =
                new Material("SQL", MaterialKind.PDF);

        MaterialVersion version =
                new MaterialVersion(1, MaterialVersionStatus.LIVE);

        version.setPublishedAt(OffsetDateTime.now());

        PdfDetails pdfDetails =
                new PdfDetails(
                        50,
                        "https://example.com/sql.pdf"
                );

        material.addVersion(version);

        pdfDetails.setVersion(version);
        version.setPdfDetails(pdfDetails);

        assertThat(
                validator.validateAndGetCompleted(version, 20)
        ).isFalse();

        assertThat(
                validator.validateAndGetCompleted(version, 50)
        ).isTrue();
    }

    @Test
    void shouldRejectPdfPositionAbovePageCount() {
        Material material =
                new Material("SQL", MaterialKind.PDF);

        MaterialVersion version =
                new MaterialVersion(1, MaterialVersionStatus.LIVE);

        version.setPublishedAt(OffsetDateTime.now());

        PdfDetails pdfDetails =
                new PdfDetails(
                        50,
                        "https://example.com/sql.pdf"
                );

        material.addVersion(version);

        pdfDetails.setVersion(version);
        version.setPdfDetails(pdfDetails);

        assertThatThrownBy(() ->
                validator.validateAndGetCompleted(version, 51)
        ).isInstanceOf(
                com.example.readinglibrary.exception
                        .InvalidMaterialDetailsException.class
        );
    }

    @Test
    void shouldValidateArticlePercentage() {
        Material material =
                new Material(
                        "Spring Transactions",
                        MaterialKind.ARTICLE
                );

        MaterialVersion version =
                new MaterialVersion(
                        1,
                        MaterialVersionStatus.LIVE
                );

        version.setPublishedAt(OffsetDateTime.now());

        material.addVersion(version);

        assertThat(
                validator.validateAndGetCompleted(version, 65)
        ).isFalse();

        assertThat(
                validator.validateAndGetCompleted(version, 100)
        ).isTrue();
    }

    @Test
    void shouldRejectArticlePercentageAbove100() {
        Material material =
                new Material(
                        "Spring Transactions",
                        MaterialKind.ARTICLE
                );

        MaterialVersion version =
                new MaterialVersion(
                        1,
                        MaterialVersionStatus.LIVE
                );

        version.setPublishedAt(OffsetDateTime.now());

        material.addVersion(version);

        assertThatThrownBy(() ->
                validator.validateAndGetCompleted(version, 101)
        ).isInstanceOf(
                com.example.readinglibrary.exception
                        .InvalidMaterialDetailsException.class
        );
    }

    @Test
    void shouldValidateBookPosition() throws Exception {
        Material material =
                new Material(
                        "Clean Code",
                        MaterialKind.BOOK
                );

        MaterialVersion version =
                new MaterialVersion(
                        1,
                        MaterialVersionStatus.LIVE
                );

        version.setPublishedAt(OffsetDateTime.now());

        JsonNode chapters = objectMapper.readTree("""
                [
                  "Introduction",
                  "Functions",
                  "Comments",
                  "Classes"
                ]
                """);

        BookDetails bookDetails =
                new BookDetails(
                        "Robert Martin",
                        chapters
                );

        material.addVersion(version);

        bookDetails.setVersion(version);
        version.setBookDetails(bookDetails);

        assertThat(
                validator.validateAndGetCompleted(version, 3)
        ).isFalse();

        assertThat(
                validator.validateAndGetCompleted(version, 4)
        ).isTrue();
    }

    @Test
    void shouldValidateSlideDeckPosition() {
        Material material =
                new Material(
                        "Spring Boot Slides",
                        MaterialKind.SLIDE_DECK
                );

        MaterialVersion version =
                new MaterialVersion(
                        1,
                        MaterialVersionStatus.LIVE
                );

        version.setPublishedAt(OffsetDateTime.now());

        SlideDeckDetails details =
                new SlideDeckDetails(
                        30,
                        "https://example.com/slides.pdf"
                );

        material.addVersion(version);

        details.setVersion(version);
        version.setSlideDeckDetails(details);

        assertThat(
                validator.validateAndGetCompleted(version, 12)
        ).isFalse();

        assertThat(
                validator.validateAndGetCompleted(version, 30)
        ).isTrue();
    }
}