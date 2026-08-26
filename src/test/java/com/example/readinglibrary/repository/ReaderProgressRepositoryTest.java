package com.example.readinglibrary.repository;

import com.example.readinglibrary.entity.Material;
import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.entity.ReaderProgress;
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
class ReaderProgressRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private MaterialVersionRepository materialVersionRepository;

    @Autowired
    private ReaderProgressRepository readerProgressRepository;

    @Test
    void shouldSaveAndLoadReaderProgress() {
        Material material =
                materialRepository.saveAndFlush(
                        new Material("Intro to SQL", MaterialKind.PDF)
                );

        MaterialVersion version =
                new MaterialVersion(1, MaterialVersionStatus.LIVE);

        version.setPublishedAt(java.time.OffsetDateTime.now());

        material.addVersion(version);

        materialVersionRepository.saveAndFlush(version);

        ReaderProgress progress =
                new ReaderProgress(
                        101L,
                        version,
                        20,
                        false
                );

        ReaderProgress saved =
                readerProgressRepository.saveAndFlush(progress);

        ReaderProgress loaded =
                readerProgressRepository
                        .findByReaderIdAndMaterialVersionId(
                                101L,
                                version.getId()
                        )
                        .orElseThrow();

        assertThat(loaded.getId())
                .isEqualTo(saved.getId());

        assertThat(loaded.getReaderId())
                .isEqualTo(101L);

        assertThat(loaded.getMaterialVersion().getId())
                .isEqualTo(version.getId());

        assertThat(loaded.getPosition())
                .isEqualTo(20);

        assertThat(loaded.isCompleted())
                .isFalse();
    }
    @Test
    void shouldNotAllowDuplicateProgressForSameReaderAndVersion() {
        Material material =
                materialRepository.saveAndFlush(
                        new Material("Intro to SQL", MaterialKind.PDF)
                );

        MaterialVersion version =
                new MaterialVersion(1, MaterialVersionStatus.LIVE);

        version.setPublishedAt(java.time.OffsetDateTime.now());
        material.addVersion(version);

        materialVersionRepository.saveAndFlush(version);

        ReaderProgress first =
                new ReaderProgress(
                        101L,
                        version,
                        20,
                        false
                );

        readerProgressRepository.saveAndFlush(first);

        ReaderProgress duplicate =
                new ReaderProgress(
                        101L,
                        version,
                        30,
                        false
                );

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                readerProgressRepository.saveAndFlush(duplicate)
        ).isInstanceOf(
                org.springframework.dao.DataIntegrityViolationException.class
        );
    }
}