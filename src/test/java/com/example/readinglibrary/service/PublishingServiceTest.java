package com.example.readinglibrary.service;

import com.example.readinglibrary.dto.CreateMaterialRequest;
import com.example.readinglibrary.dto.CreateVersionResponse;
import com.example.readinglibrary.dto.PublishVersionResponse;
import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.enums.MaterialVersionStatus;
import com.example.readinglibrary.exception.ConflictException;
import com.example.readinglibrary.repository.MaterialVersionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
class PublishingServiceTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MaterialService materialService;

    @Autowired
    private MaterialVersionRepository materialVersionRepository;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private CreateMaterialRequest pdfRequest()
            throws Exception {

        return new CreateMaterialRequest(
                "Intro to SQL",
                MaterialKind.PDF,
                objectMapper.readTree("""
                        {
                          "pageCount": 50,
                          "fileUrl": "https://example.com/sql.pdf"
                        }
                        """)
        );
    }
    @Test
    void shouldPublishFirstVersion()
            throws Exception {

        var created =
                materialService.createMaterial(
                        pdfRequest()
                );

        PublishVersionResponse response =
                materialService.publishVersion(
                        created.materialId(),
                        created.versionId()
                );

        assertThat(response.materialId())
                .isEqualTo(created.materialId());

        assertThat(response.versionId())
                .isEqualTo(created.versionId());

        assertThat(response.versionNumber())
                .isEqualTo(1);

        assertThat(response.status())
                .isEqualTo(MaterialVersionStatus.LIVE);

        assertThat(response.publishedAt())
                .isNotNull();

        MaterialVersion version =
                materialVersionRepository
                        .findById(created.versionId())
                        .orElseThrow();

        assertThat(version.getStatus())
                .isEqualTo(MaterialVersionStatus.LIVE);

        assertThat(version.getPublishedAt())
                .isNotNull();
    }
    @Test
    void shouldArchiveOldLiveVersionWhenPublishingNewVersion()
            throws Exception {

        var created =
                materialService.createMaterial(
                        pdfRequest()
                );

        materialService.publishVersion(
                created.materialId(),
                created.versionId()
        );

        CreateVersionResponse v2 =
                materialService.createDraftVersion(
                        created.materialId()
                );

        materialService.publishVersion(
                created.materialId(),
                v2.versionId()
        );

        MaterialVersion v1 =
                materialVersionRepository
                        .findById(created.versionId())
                        .orElseThrow();

        MaterialVersion version2 =
                materialVersionRepository
                        .findById(v2.versionId())
                        .orElseThrow();

        assertThat(v1.getStatus())
                .isEqualTo(MaterialVersionStatus.ARCHIVED);

        assertThat(version2.getStatus())
                .isEqualTo(MaterialVersionStatus.LIVE);
    }
    @Test
    void shouldRollbackToOlderVersion()
            throws Exception {

        var created =
                materialService.createMaterial(
                        pdfRequest()
                );

        materialService.publishVersion(
                created.materialId(),
                created.versionId()
        );

        CreateVersionResponse v2 =
                materialService.createDraftVersion(
                        created.materialId()
                );

        materialService.publishVersion(
                created.materialId(),
                v2.versionId()
        );

        /*
         * Roll back to v1.
         */
        materialService.publishVersion(
                created.materialId(),
                created.versionId()
        );

        MaterialVersion v1 =
                materialVersionRepository
                        .findById(created.versionId())
                        .orElseThrow();

        MaterialVersion version2 =
                materialVersionRepository
                        .findById(v2.versionId())
                        .orElseThrow();

        assertThat(v1.getStatus())
                .isEqualTo(MaterialVersionStatus.LIVE);

        assertThat(version2.getStatus())
                .isEqualTo(MaterialVersionStatus.ARCHIVED);
    }
    @Test
    void shouldRejectPublishingAlreadyLiveVersion()
            throws Exception {

        var created =
                materialService.createMaterial(
                        pdfRequest()
                );

        materialService.publishVersion(
                created.materialId(),
                created.versionId()
        );

        assertThatThrownBy(() ->
                materialService.publishVersion(
                        created.materialId(),
                        created.versionId()
                )
        )
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(
                        "is already LIVE"
                );
    }
    @Test
    void shouldRejectVersionFromDifferentMaterial()
            throws Exception {

        var first =
                materialService.createMaterial(
                        pdfRequest()
                );

        var second =
                materialService.createMaterial(
                        pdfRequest()
                );

        assertThatThrownBy(() ->
                materialService.publishVersion(
                        first.materialId(),
                        second.versionId()
                )
        )
                .isInstanceOf(
                        com.example.readinglibrary.exception
                                .ResourceNotFoundException.class
                );
    }
    @Test
    void shouldHaveOnlyOneLiveVersion()
            throws Exception {

        var created =
                materialService.createMaterial(
                        pdfRequest()
                );

        materialService.publishVersion(
                created.materialId(),
                created.versionId()
        );

        var v2 =
                materialService.createDraftVersion(
                        created.materialId()
                );

        materialService.publishVersion(
                created.materialId(),
                v2.versionId()
        );

        List<MaterialVersion> versions =
                materialVersionRepository.findAll()
                        .stream()
                        .filter(v ->
                                v.getMaterial().getId()
                                        .equals(created.materialId()))
                        .toList();

        long liveCount =
                versions.stream()
                        .filter(v ->
                                v.getStatus()
                                        == MaterialVersionStatus.LIVE)
                        .count();

        assertThat(liveCount)
                .isEqualTo(1);
    }
    @Test
    void databaseShouldRejectTwoLiveVersions()
            throws Exception {

        var created =
                materialService.createMaterial(
                        pdfRequest()
                );

        materialService.publishVersion(
                created.materialId(),
                created.versionId()
        );

        var v2 =
                materialService.createDraftVersion(
                        created.materialId()
                );

        MaterialVersion second =
                materialVersionRepository
                        .findById(v2.versionId())
                        .orElseThrow();

        /*
         * Deliberately bypass the publishing service.
         *
         * This proves the database itself prevents two LIVE rows.
         */
        second.setStatus(
                MaterialVersionStatus.LIVE
        );

        second.setPublishedAt(
                java.time.OffsetDateTime.now()
        );

        assertThatThrownBy(() ->
                materialVersionRepository.saveAndFlush(second)
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                );
    }
    @Test
    void shouldAllowOnlyOneLiveVersionWhenPublishedConcurrently()
            throws Exception {

        var created =
                materialService.createMaterial(
                        pdfRequest()
                );

        materialService.publishVersion(
                created.materialId(),
                created.versionId()
        );

        var v2 =
                materialService.createDraftVersion(
                        created.materialId()
                );

        var v3 =
                materialService.createDraftVersion(
                        created.materialId()
                );

        java.util.concurrent.ExecutorService executor =
                java.util.concurrent.Executors.newFixedThreadPool(2);

        java.util.concurrent.CountDownLatch startLatch =
                new java.util.concurrent.CountDownLatch(1);

        try {
            var task1 = executor.submit(() -> {
                startLatch.await();

                materialService.publishVersion(
                        created.materialId(),
                        v2.versionId()
                );

                return null;
            });

            var task2 = executor.submit(() -> {
                startLatch.await();

                materialService.publishVersion(
                        created.materialId(),
                        v3.versionId()
                );

                return null;
            });

            /*
             * Release both threads at approximately the same time.
             */
            startLatch.countDown();

            task1.get();
            task2.get();

        } finally {
            executor.shutdown();
        }

        List<MaterialVersion> versions =
                materialVersionRepository.findAll()
                        .stream()
                        .filter(version ->
                                version.getMaterial()
                                        .getId()
                                        .equals(created.materialId()))
                        .toList();

        long liveCount =
                versions.stream()
                        .filter(version ->
                                version.getStatus()
                                        == MaterialVersionStatus.LIVE)
                        .count();

        assertThat(liveCount)
                .isEqualTo(1);
    }
}