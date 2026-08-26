package com.example.readinglibrary.service;

import com.example.readinglibrary.dto.*;
import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.enums.MaterialVersionStatus;
import com.example.readinglibrary.enums.ReaderMaterialStatus;
import com.example.readinglibrary.exception.InvalidMaterialDetailsException;
import com.example.readinglibrary.exception.ResourceNotFoundException;
import com.example.readinglibrary.reader.ReaderContext;
import com.example.readinglibrary.repository.ReaderProgressRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;



@Testcontainers
@SpringBootTest
@Transactional
class ReaderMaterialServiceTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MaterialService materialService;

    @Autowired
    private ReaderMaterialService readerMaterialService;

    @Autowired
    private com.example.readinglibrary.repository.MaterialVersionRepository
            materialVersionRepository;
    @Autowired
    private ReaderProgressRepository readerProgressRepository;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Test
    void shouldOpenCurrentLiveVersion() throws Exception {

        var created =
                materialService.createMaterial(
                        new CreateMaterialRequest(
                                "Intro to SQL",
                                MaterialKind.PDF,
                                objectMapper.readTree("""
                                        {
                                          "pageCount": 50,
                                          "fileUrl": "https://example.com/v1.pdf"
                                        }
                                        """)
                        )
                );

        makeLive(created.versionId());

        ReaderMaterialResponse response =
                readerMaterialService.openMaterial(
                        created.materialId(),
                        new ReaderContext(101L)
                );

        assertThat(response.materialId())
                .isEqualTo(created.materialId());

        assertThat(response.versionId())
                .isEqualTo(created.versionId());

        assertThat(response.versionNumber())
                .isEqualTo(1);

        assertThat(response.kind())
                .isEqualTo(MaterialKind.PDF);

        assertThat(response.details()
                .get("pageCount")
                .asInt())
                .isEqualTo(50);

        assertThat(response.progress())
                .isNull();
    }

    @Test
    void shouldOpenNewestLiveVersion() throws Exception {

        var created =
                materialService.createMaterial(
                        new CreateMaterialRequest(
                                "Intro to SQL",
                                MaterialKind.PDF,
                                objectMapper.readTree("""
                                        {
                                          "pageCount": 50,
                                          "fileUrl": "https://example.com/v1.pdf"
                                        }
                                        """)
                        )
                );

        makeLive(created.versionId());

        var v2 =
                materialService.createDraftVersion(
                        created.materialId()
                );

        materialService.updateDraftVersion(
                created.materialId(),
                v2.versionId(),
                new com.example.readinglibrary.dto
                        .UpdateMaterialVersionRequest(
                        objectMapper.readTree("""
                                        {
                                          "pageCount": 52,
                                          "fileUrl": "https://example.com/v2.pdf"
                                        }
                                        """)
                )
        );

        materialService.publishVersion(
                created.materialId(),
                v2.versionId()
        );

        ReaderMaterialResponse response =
                readerMaterialService.openMaterial(
                        created.materialId(),
                        new ReaderContext(101L)
                );

        assertThat(response.versionId())
                .isEqualTo(v2.versionId());

        assertThat(response.versionNumber())
                .isEqualTo(2);

        assertThat(response.details()
                .get("pageCount")
                .asInt())
                .isEqualTo(52);

        assertThat(response.details()
                .get("fileUrl")
                .asText())
                .isEqualTo(
                        "https://example.com/v2.pdf"
                );
    }

    @Test
    void shouldNotOpenMaterialWithoutLiveVersion()
            throws Exception {

        var created =
                materialService.createMaterial(
                        new CreateMaterialRequest(
                                "Draft Only",
                                MaterialKind.PDF,
                                objectMapper.readTree("""
                                        {
                                          "pageCount": 20,
                                          "fileUrl": "https://example.com/draft.pdf"
                                        }
                                        """)
                        )
                );

        assertThatThrownBy(() ->
                readerMaterialService.openMaterial(
                        created.materialId(),
                        new ReaderContext(101L)
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        "Material has no LIVE version: "
                                + created.materialId()
                );
    }

    private void makeLive(Long versionId) {
        MaterialVersion version =
                materialVersionRepository
                        .findById(versionId)
                        .orElseThrow();

        version.setStatus(
                MaterialVersionStatus.LIVE
        );

        version.setPublishedAt(
                OffsetDateTime.now()
        );

        materialVersionRepository.saveAndFlush(
                version
        );
    }
    @Test
    void shouldSaveInitialPdfProgress() throws Exception {

        var created =
                materialService.createMaterial(
                        new CreateMaterialRequest(
                                "Intro to SQL",
                                MaterialKind.PDF,
                                objectMapper.readTree("""
                                    {
                                      "pageCount": 50,
                                      "fileUrl": "https://example.com/sql.pdf"
                                    }
                                    """)
                        )
                );

        makeLive(created.versionId());

        SaveProgressResponse response =
                readerMaterialService.saveProgress(
                        created.materialId(),
                        created.versionId(),
                        new ReaderContext(101L),
                        new SaveProgressRequest(20)
                );

        assertThat(response.materialId())
                .isEqualTo(created.materialId());

        assertThat(response.materialVersionId())
                .isEqualTo(created.versionId());

        assertThat(response.versionNumber())
                .isEqualTo(1);

        assertThat(response.position())
                .isEqualTo(20);

        assertThat(response.completed())
                .isFalse();
    }
    @Test
    void shouldUpdateExistingProgress() throws Exception {

        var created =
                materialService.createMaterial(
                        new CreateMaterialRequest(
                                "Intro to SQL",
                                MaterialKind.PDF,
                                objectMapper.readTree("""
                                    {
                                      "pageCount": 50,
                                      "fileUrl": "https://example.com/sql.pdf"
                                    }
                                    """)
                        )
                );

        makeLive(created.versionId());

        ReaderContext reader =
                new ReaderContext(101L);

        readerMaterialService.saveProgress(
                created.materialId(),
                created.versionId(),
                reader,
                new SaveProgressRequest(20)
        );

        readerMaterialService.saveProgress(
                created.materialId(),
                created.versionId(),
                reader,
                new SaveProgressRequest(40)
        );

        var progress =
                readerProgressRepository
                        .findByReaderIdAndMaterialVersionId(
                                101L,
                                created.versionId()
                        )
                        .orElseThrow();

        assertThat(progress.getPosition())
                .isEqualTo(40);

        assertThat(progress.isCompleted())
                .isFalse();
    }
    @Test
    void shouldMarkPdfCompleteOnLastPage() throws Exception {

        var created =
                materialService.createMaterial(
                        new CreateMaterialRequest(
                                "Intro to SQL",
                                MaterialKind.PDF,
                                objectMapper.readTree("""
                                    {
                                      "pageCount": 50,
                                      "fileUrl": "https://example.com/sql.pdf"
                                    }
                                    """)
                        )
                );

        makeLive(created.versionId());

        SaveProgressResponse response =
                readerMaterialService.saveProgress(
                        created.materialId(),
                        created.versionId(),
                        new ReaderContext(101L),
                        new SaveProgressRequest(50)
                );

        assertThat(response.completed())
                .isTrue();
    }
    @Test
    void shouldMarkArticleCompleteAt100Percent()
            throws Exception {

        var created =
                materialService.createMaterial(
                        new CreateMaterialRequest(
                                "Spring Transactions",
                                MaterialKind.ARTICLE,
                                objectMapper.readTree("""
                                    {
                                      "wordCount": 5000
                                    }
                                    """)
                        )
                );

        makeLive(created.versionId());

        SaveProgressResponse response =
                readerMaterialService.saveProgress(
                        created.materialId(),
                        created.versionId(),
                        new ReaderContext(101L),
                        new SaveProgressRequest(100)
                );

        assertThat(response.completed())
                .isTrue();
    }
    @Test
    void shouldNotMarkArticleCompleteAt98Percent()
            throws Exception {

        var created =
                materialService.createMaterial(
                        new CreateMaterialRequest(
                                "Spring Transactions",
                                MaterialKind.ARTICLE,
                                objectMapper.readTree("""
                                    {
                                      "wordCount": 5000
                                    }
                                    """)
                        )
                );

        makeLive(created.versionId());

        SaveProgressResponse response =
                readerMaterialService.saveProgress(
                        created.materialId(),
                        created.versionId(),
                        new ReaderContext(101L),
                        new SaveProgressRequest(98)
                );

        assertThat(response.completed())
                .isFalse();
    }
    @Test
    void shouldRejectPdfPositionBeyondPageCount()
            throws Exception {

        var created =
                materialService.createMaterial(
                        new CreateMaterialRequest(
                                "Intro to SQL",
                                MaterialKind.PDF,
                                objectMapper.readTree("""
                                    {
                                      "pageCount": 50,
                                      "fileUrl": "https://example.com/sql.pdf"
                                    }
                                    """)
                        )
                );

        makeLive(created.versionId());

        assertThatThrownBy(() ->
                readerMaterialService.saveProgress(
                        created.materialId(),
                        created.versionId(),
                        new ReaderContext(101L),
                        new SaveProgressRequest(51)
                )
        )
                .isInstanceOf(InvalidMaterialDetailsException.class);
    }
    @Test
    void shouldKeepProgressAttachedToOriginalVersion()
            throws Exception {

        var created =
                materialService.createMaterial(
                        new CreateMaterialRequest(
                                "Intro to SQL",
                                MaterialKind.PDF,
                                objectMapper.readTree("""
                                    {
                                      "pageCount": 50,
                                      "fileUrl": "https://example.com/v1.pdf"
                                    }
                                    """)
                        )
                );

        makeLive(created.versionId());

        ReaderContext reader =
                new ReaderContext(101L);

        /*
         * Reader is reading v1.
         */
        readerMaterialService.saveProgress(
                created.materialId(),
                created.versionId(),
                reader,
                new SaveProgressRequest(20)
        );

        /*
         * Create v2 from v1.
         */
        var v2 =
                materialService.createDraftVersion(
                        created.materialId()
                );

        /*
         * Change v2.
         */
        materialService.updateDraftVersion(
                created.materialId(),
                v2.versionId(),
                new UpdateMaterialVersionRequest(
                        objectMapper.readTree("""
                            {
                              "pageCount": 52,
                              "fileUrl": "https://example.com/v2.pdf"
                            }
                            """)
                )
        );

        /*
         * Publish v2.
         */
        materialService.publishVersion(
                created.materialId(),
                v2.versionId()
        );

        /*
         * Read the old progress directly.
         */
        var saved =
                readerProgressRepository
                        .findByReaderIdAndMaterialVersionId(
                                101L,
                                created.versionId()
                        )
                        .orElseThrow();

        assertThat(saved.getMaterialVersion().getId())
                .isEqualTo(created.versionId());

        assertThat(saved.getPosition())
                .isEqualTo(20);

        assertThat(saved.isCompleted())
                .isFalse();

        /*
         * Confirm opening the material now returns v2,
         * but the old v1 progress has NOT moved.
         */
        ReaderMaterialResponse opened =
                readerMaterialService.openMaterial(
                        created.materialId(),
                        reader
                );

        assertThat(opened.versionId())
                .isEqualTo(v2.versionId());

        assertThat(opened.versionNumber())
                .isEqualTo(2);

        assertThat(opened.progress())
                .isNull();
    }
    @Test
    void readersShouldHaveIndependentProgress()
            throws Exception {

        var created =
                materialService.createMaterial(
                        new CreateMaterialRequest(
                                "Intro to SQL",
                                MaterialKind.PDF,
                                objectMapper.readTree("""
                                    {
                                      "pageCount": 50,
                                      "fileUrl": "https://example.com/sql.pdf"
                                    }
                                    """)
                        )
                );

        makeLive(created.versionId());

        readerMaterialService.saveProgress(
                created.materialId(),
                created.versionId(),
                new ReaderContext(101L),
                new SaveProgressRequest(20)
        );

        readerMaterialService.saveProgress(
                created.materialId(),
                created.versionId(),
                new ReaderContext(202L),
                new SaveProgressRequest(40)
        );

        var reader101 =
                readerMaterialService.openMaterial(
                        created.materialId(),
                        new ReaderContext(101L)
                );

        var reader202 =
                readerMaterialService.openMaterial(
                        created.materialId(),
                        new ReaderContext(202L)
                );

        assertThat(reader101.progress().position())
                .isEqualTo(20);

        assertThat(reader202.progress().position())
                .isEqualTo(40);
    }
    @Test
    void shouldListReaderInProgressMaterials()
            throws Exception {

        var created =
                materialService.createMaterial(
                        new CreateMaterialRequest(
                                "Intro to SQL",
                                MaterialKind.PDF,
                                objectMapper.readTree("""
                                    {
                                      "pageCount": 50,
                                      "fileUrl": "https://example.com/sql.pdf"
                                    }
                                    """)
                        )
                );

        makeLive(created.versionId());

        readerMaterialService.saveProgress(
                created.materialId(),
                created.versionId(),
                new ReaderContext(101L),
                new SaveProgressRequest(20)
        );

        ReaderMaterialPageResponse response =
                readerMaterialService.getReaderMaterials(
                        new ReaderContext(101L),
                        ReaderMaterialStatus.IN_PROGRESS,
                        PageRequest.of(0, 20)
                );

        assertThat(response.totalElements())
                .isEqualTo(1);

        assertThat(response.content())
                .hasSize(1);

        var item =
                response.content().get(0);

        assertThat(item.materialId())
                .isEqualTo(created.materialId());

        assertThat(item.versionId())
                .isEqualTo(created.versionId());

        assertThat(item.position())
                .isEqualTo(20);

        assertThat(item.completed())
                .isFalse();
    }
    @Test
    void shouldListCompletedMaterials()
            throws Exception {

        var created =
                materialService.createMaterial(
                        new CreateMaterialRequest(
                                "Intro to SQL",
                                MaterialKind.PDF,
                                objectMapper.readTree("""
                                    {
                                      "pageCount": 50,
                                      "fileUrl": "https://example.com/sql.pdf"
                                    }
                                    """)
                        )
                );

        makeLive(created.versionId());

        readerMaterialService.saveProgress(
                created.materialId(),
                created.versionId(),
                new ReaderContext(101L),
                new SaveProgressRequest(50)
        );

        ReaderMaterialPageResponse response =
                readerMaterialService.getReaderMaterials(
                        new ReaderContext(101L),
                        ReaderMaterialStatus.COMPLETED,
                        PageRequest.of(0, 20)
                );

        assertThat(response.totalElements())
                .isEqualTo(1);

        assertThat(response.content().get(0).completed())
                .isTrue();

        assertThat(response.content().get(0).position())
                .isEqualTo(50);
    }
    @Test
    void shouldContinueFromExactSavedVersion()
            throws Exception {

        var created =
                materialService.createMaterial(
                        new CreateMaterialRequest(
                                "Intro to SQL",
                                MaterialKind.PDF,
                                objectMapper.readTree("""
                                    {
                                      "pageCount": 50,
                                      "fileUrl": "https://example.com/v1.pdf"
                                    }
                                    """)
                        )
                );

        materialService.publishVersion(
                created.materialId(),
                created.versionId()
        );

        ReaderContext reader =
                new ReaderContext(101L);

        readerMaterialService.saveProgress(
                created.materialId(),
                created.versionId(),
                reader,
                new SaveProgressRequest(20)
        );

        var v2 =
                materialService.createDraftVersion(
                        created.materialId()
                );

        materialService.updateDraftVersion(
                created.materialId(),
                v2.versionId(),
                new UpdateMaterialVersionRequest(
                        objectMapper.readTree("""
                            {
                              "pageCount": 52,
                              "fileUrl": "https://example.com/v2.pdf"
                            }
                            """)
                )
        );

        materialService.publishVersion(
                created.materialId(),
                v2.versionId()
        );

        ReaderContinuationResponse response =
                readerMaterialService.continueReading(
                        created.materialId(),
                        reader
                );

        assertThat(response.versionId())
                .isEqualTo(created.versionId());

        assertThat(response.versionNumber())
                .isEqualTo(1);

        assertThat(response.position())
                .isEqualTo(20);

        assertThat(response.completed())
                .isFalse();

        assertThat(
                response.details()
                        .get("pageCount")
                        .asInt()
        ).isEqualTo(50);
    }
}