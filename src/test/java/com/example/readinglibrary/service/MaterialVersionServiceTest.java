package com.example.readinglibrary.service;

import com.example.readinglibrary.dto.CreateMaterialRequest;
import com.example.readinglibrary.dto.CreateVersionResponse;
import com.example.readinglibrary.dto.UpdateMaterialVersionRequest;
import com.example.readinglibrary.entity.*;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.enums.MaterialVersionStatus;
import com.example.readinglibrary.exception.ConflictException;
import com.example.readinglibrary.exception.InvalidMaterialDetailsException;
import com.example.readinglibrary.exception.ResourceNotFoundException;
import com.example.readinglibrary.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
@Transactional
class MaterialVersionServiceTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MaterialService materialService;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private MaterialVersionRepository materialVersionRepository;

    @Autowired
    private PdfDetailsRepository pdfDetailsRepository;

    @Autowired
    private ArticleDetailsRepository articleDetailsRepository;

    @Autowired
    private BookDetailsRepository bookDetailsRepository;

    @Autowired
    private SlideDeckDetailsRepository slideDeckDetailsRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CreateMaterialRequest pdfRequest() throws Exception {
        return new CreateMaterialRequest(
                "Intro to SQL",
                MaterialKind.PDF,
                objectMapper.readTree("""
                        {
                          "pageCount": 50,
                          "fileUrl": "https://example.com/sql-v1.pdf"
                        }
                        """)
        );
    }

    private CreateMaterialRequest articleRequest() throws Exception {
        return new CreateMaterialRequest(
                "Spring Transactions",
                MaterialKind.ARTICLE,
                objectMapper.readTree("""
                        {
                          "wordCount": 5000
                        }
                        """)
        );
    }

    private CreateMaterialRequest bookRequest() throws Exception {
        return new CreateMaterialRequest(
                "Clean Code",
                MaterialKind.BOOK,
                objectMapper.readTree("""
                        {
                          "author": "Robert Martin",
                          "chapters": [
                            "Introduction",
                            "Meaningful Names",
                            "Functions",
                            "Comments"
                          ]
                        }
                        """)
        );
    }

    private CreateMaterialRequest slideDeckRequest() throws Exception {
        return new CreateMaterialRequest(
                "Spring Boot Slides",
                MaterialKind.SLIDE_DECK,
                objectMapper.readTree("""
                        {
                          "slideCount": 30,
                          "fileUrl": "https://example.com/slides-v1.pdf"
                        }
                        """)
        );
    }

    private void makeVersionLive(Long versionId) {
        MaterialVersion version =
                materialVersionRepository.findById(versionId)
                        .orElseThrow();

        version.setStatus(MaterialVersionStatus.LIVE);
        version.setPublishedAt(OffsetDateTime.now());

        materialVersionRepository.saveAndFlush(version);
    }

    private MaterialVersion findVersion(
            Long materialId,
            int versionNumber
    ) {
        return materialVersionRepository
                .findByMaterialIdAndVersionNumber(
                        materialId,
                        versionNumber
                )
                .orElseThrow();
    }
    @Test
    void shouldCreateVersionTwoAsDraftFromLivePdf()
            throws Exception {

        var created =
                materialService.createMaterial(pdfRequest());

        makeVersionLive(created.versionId());

        CreateVersionResponse response =
                materialService.createDraftVersion(
                        created.materialId()
                );

        assertThat(response.materialId())
                .isEqualTo(created.materialId());

        assertThat(response.versionNumber())
                .isEqualTo(2);

        assertThat(response.status())
                .isEqualTo(MaterialVersionStatus.DRAFT);

        MaterialVersion v2 =
                findVersion(created.materialId(), 2);

        assertThat(v2.getStatus())
                .isEqualTo(MaterialVersionStatus.DRAFT);

        PdfDetails details =
                pdfDetailsRepository
                        .findByVersionId(v2.getId())
                        .orElseThrow();

        assertThat(details.getPageCount())
                .isEqualTo(50);

        assertThat(details.getFileUrl())
                .isEqualTo(
                        "https://example.com/sql-v1.pdf"
                );
    }
    @Test
    void shouldCreateMultipleDraftVersionsWithIncreasingNumbers()
            throws Exception {

        var created =
                materialService.createMaterial(pdfRequest());

        makeVersionLive(created.versionId());

        var v2 =
                materialService.createDraftVersion(
                        created.materialId()
                );

        var v3 =
                materialService.createDraftVersion(
                        created.materialId()
                );

        assertThat(v2.versionNumber())
                .isEqualTo(2);

        assertThat(v3.versionNumber())
                .isEqualTo(3);

        assertThat(
                findVersion(
                        created.materialId(),
                        2
                ).getStatus()
        ).isEqualTo(MaterialVersionStatus.DRAFT);

        assertThat(
                findVersion(
                        created.materialId(),
                        3
                ).getStatus()
        ).isEqualTo(MaterialVersionStatus.DRAFT);
    }
    @Test
    void shouldCopyArticleDetails() throws Exception {

        var created =
                materialService.createMaterial(
                        articleRequest()
                );

        makeVersionLive(created.versionId());

        var v2 =
                materialService.createDraftVersion(
                        created.materialId()
                );

        ArticleDetails details =
                articleDetailsRepository
                        .findByVersionId(v2.versionId())
                        .orElseThrow();

        assertThat(details.getWordCount())
                .isEqualTo(5000);
    }
    @Test
    void shouldCopyBookDetailsAndChaptersIndependently()
            throws Exception {

        var created =
                materialService.createMaterial(
                        bookRequest()
                );

        makeVersionLive(created.versionId());

        var v2 =
                materialService.createDraftVersion(
                        created.materialId()
                );

        BookDetails v1Details =
                bookDetailsRepository
                        .findByVersionId(created.versionId())
                        .orElseThrow();

        BookDetails v2Details =
                bookDetailsRepository
                        .findByVersionId(v2.versionId())
                        .orElseThrow();

        assertThat(v2Details.getAuthor())
                .isEqualTo("Robert Martin");

        assertThat(v2Details.getChapters().isArray())
                .isTrue();

        assertThat(v2Details.getChapters().size())
                .isEqualTo(4);

        assertThat(v1Details.getChapters())
                .isNotSameAs(v2Details.getChapters());
    }
    @Test
    void shouldCopySlideDeckDetails() throws Exception {

        var created =
                materialService.createMaterial(
                        slideDeckRequest()
                );

        makeVersionLive(created.versionId());

        var v2 =
                materialService.createDraftVersion(
                        created.materialId()
                );

        SlideDeckDetails details =
                slideDeckDetailsRepository
                        .findByVersionId(v2.versionId())
                        .orElseThrow();

        assertThat(details.getSlideCount())
                .isEqualTo(30);

        assertThat(details.getFileUrl())
                .isEqualTo(
                        "https://example.com/slides-v1.pdf"
                );
    }
    @Test
    void shouldEditDraftWithoutChangingLiveVersion()
            throws Exception {

        var created =
                materialService.createMaterial(
                        pdfRequest()
                );

        makeVersionLive(created.versionId());

        var v2 =
                materialService.createDraftVersion(
                        created.materialId()
                );

        var updateRequest =
                new UpdateMaterialVersionRequest(
                        objectMapper.readTree("""
                            {
                              "pageCount": 52,
                              "fileUrl": "https://example.com/sql-v2.pdf"
                            }
                            """)
                );

        materialService.updateDraftVersion(
                created.materialId(),
                v2.versionId(),
                updateRequest
        );

        PdfDetails v1Details =
                pdfDetailsRepository
                        .findByVersionId(created.versionId())
                        .orElseThrow();

        PdfDetails v2Details =
                pdfDetailsRepository
                        .findByVersionId(v2.versionId())
                        .orElseThrow();

        assertThat(v1Details.getPageCount())
                .isEqualTo(50);

        assertThat(v1Details.getFileUrl())
                .isEqualTo(
                        "https://example.com/sql-v1.pdf"
                );

        assertThat(v2Details.getPageCount())
                .isEqualTo(52);

        assertThat(v2Details.getFileUrl())
                .isEqualTo(
                        "https://example.com/sql-v2.pdf"
                );
    }
    @Test
    void shouldRejectEditingLiveVersion()
            throws Exception {

        var created =
                materialService.createMaterial(
                        pdfRequest()
                );

        makeVersionLive(created.versionId());

        var updateRequest =
                new UpdateMaterialVersionRequest(
                        objectMapper.readTree("""
                            {
                              "pageCount": 60,
                              "fileUrl": "https://example.com/changed.pdf"
                            }
                            """)
                );

        assertThatThrownBy(() ->
                materialService.updateDraftVersion(
                        created.materialId(),
                        created.versionId(),
                        updateRequest
                )
        )
                .isInstanceOf(ConflictException.class)
                .hasMessage(
                        "Only DRAFT versions can be edited."
                );
    }
    @Test
    void shouldRejectInvalidDraftUpdate()
            throws Exception {

        var created =
                materialService.createMaterial(
                        pdfRequest()
                );

        makeVersionLive(created.versionId());

        var v2 =
                materialService.createDraftVersion(
                        created.materialId()
                );

        var updateRequest =
                new UpdateMaterialVersionRequest(
                        objectMapper.readTree("""
                            {
                              "pageCount": 0,
                              "fileUrl": "https://example.com/invalid.pdf"
                            }
                            """)
                );

        assertThatThrownBy(() ->
                materialService.updateDraftVersion(
                        created.materialId(),
                        v2.versionId(),
                        updateRequest
                )
        )
                .isInstanceOf(
                        InvalidMaterialDetailsException.class
                )
                .hasMessage(
                        "pageCount must be greater than 0 for PDF material."
                );

        PdfDetails details =
                pdfDetailsRepository
                        .findByVersionId(v2.versionId())
                        .orElseThrow();

        assertThat(details.getPageCount())
                .isEqualTo(50);
    }
    @Test
    void shouldRejectEditingArchivedVersion()
            throws Exception {

        var created =
                materialService.createMaterial(
                        pdfRequest()
                );

        MaterialVersion version =
                findVersion(
                        created.materialId(),
                        1
                );

        version.setStatus(
                MaterialVersionStatus.ARCHIVED
        );

        version.setPublishedAt(
                OffsetDateTime.now()
        );

        materialVersionRepository.saveAndFlush(version);

        var updateRequest =
                new UpdateMaterialVersionRequest(
                        objectMapper.readTree("""
                            {
                              "pageCount": 60,
                              "fileUrl": "https://example.com/changed.pdf"
                            }
                            """)
                );

        assertThatThrownBy(() ->
                materialService.updateDraftVersion(
                        created.materialId(),
                        version.getId(),
                        updateRequest
                )
        )
                .isInstanceOf(ConflictException.class)
                .hasMessage(
                        "Only DRAFT versions can be edited."
                );
    }
    @Test
    void shouldRejectCreatingVersionWhenNoLiveVersionExists()
            throws Exception {

        var created =
                materialService.createMaterial(
                        pdfRequest()
                );

        assertThatThrownBy(() ->
                materialService.createDraftVersion(
                        created.materialId()
                )
        )
                .isInstanceOf(ConflictException.class)
                .hasMessage(
                        "Material has no live version: "
                                + created.materialId()
                );
    }
    @Test
    void shouldRejectVersionThatDoesNotBelongToMaterial()
            throws Exception {

        var first =
                materialService.createMaterial(
                        pdfRequest()
                );

        var second =
                materialService.createMaterial(
                        articleRequest()
                );

        var updateRequest =
                new UpdateMaterialVersionRequest(
                        objectMapper.readTree("""
                            {
                              "wordCount": 6000
                            }
                            """)
                );
        assertThatThrownBy(() ->
                materialService.updateDraftVersion(
                        first.materialId(),
                        second.versionId(),
                        updateRequest
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        "Version " + second.versionId()
                                + " does not belong to material "
                                + first.materialId()
                );
    }

}