package com.example.readinglibrary.repository;

import com.example.readinglibrary.entity.ArticleDetails;
import com.example.readinglibrary.entity.Material;
import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.enums.MaterialVersionStatus;
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
class ArticleDetailsRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private MaterialVersionRepository materialVersionRepository;

    @Autowired
    private ArticleDetailsRepository articleDetailsRepository;

    @Test
    void shouldSaveAndLoadArticleDetails() {
        Material material =
                materialRepository.saveAndFlush(
                        new Material("Spring Article", MaterialKind.ARTICLE)
                );

        MaterialVersion version =
                new MaterialVersion(1, MaterialVersionStatus.DRAFT);

        material.addVersion(version);
        materialVersionRepository.saveAndFlush(version);

        ArticleDetails details =
                new ArticleDetails(5000);

        details.setVersion(version);

        articleDetailsRepository.saveAndFlush(details);

        ArticleDetails saved =
                articleDetailsRepository
                        .findByVersionId(version.getId())
                        .orElseThrow();

        assertThat(saved.getWordCount())
                .isEqualTo(5000);
    }
}