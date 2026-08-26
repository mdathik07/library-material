package com.example.readinglibrary.repository;

import com.example.readinglibrary.entity.BookDetails;
import com.example.readinglibrary.entity.Material;
import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.enums.MaterialVersionStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
class BookDetailsRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private MaterialVersionRepository materialVersionRepository;

    @Autowired
    private BookDetailsRepository bookDetailsRepository;

//    @Autowired
//    private ObjectMapper objectMapper;
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSaveAndLoadBookChaptersAsJsonb() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        Material material =
                materialRepository.saveAndFlush(
                        new Material("Clean Code", MaterialKind.BOOK)
                );

        MaterialVersion version =
                new MaterialVersion(1, MaterialVersionStatus.DRAFT);

        material.addVersion(version);
        materialVersionRepository.saveAndFlush(version);

        JsonNode chapters = objectMapper.readTree("""
            [
              "Introduction",
              "Meaningful Names",
              "Functions",
              "Comments"
            ]
            """);

        BookDetails details =
                new BookDetails(
                        "Robert Martin",
                        chapters
                );

        details.setVersion(version);

        bookDetailsRepository.saveAndFlush(details);

        BookDetails saved =
                bookDetailsRepository
                        .findByVersionId(version.getId())
                        .orElseThrow();

        assertThat(saved.getAuthor())
                .isEqualTo("Robert Martin");

        assertThat(saved.getChapters().isArray())
                .isTrue();

        assertThat(saved.getChapters().size())
                .isEqualTo(4);

        assertThat(saved.getChapters().get(0).asText())
                .isEqualTo("Introduction");

        assertThat(saved.getChapters().get(3).asText())
                .isEqualTo("Comments");
    }

}