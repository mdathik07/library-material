package com.example.readinglibrary.service;

import com.example.readinglibrary.dto.CreateMaterialRequest;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.entity.*;
import com.example.readinglibrary.enums.MaterialVersionStatus;
import com.example.readinglibrary.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.example.readinglibrary.exception.InvalidMaterialDetailsException;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
class MaterialServiceTest {

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


    @Test
    void shouldCreatePdfMaterialAsDraft() throws Exception {

        var request =
                new CreateMaterialRequest(
                        "Intro to SQL",
                        MaterialKind.PDF,
                        objectMapper.readTree("""
                                {
                                  "pageCount": 50,
                                  "fileUrl": "https://example.com/sql.pdf"
                                }
                                """)
                );

        var response =
                materialService.createMaterial(request);

        assertThat(response.materialId())
                .isNotNull();

        assertThat(response.versionId())
                .isNotNull();

        assertThat(response.versionNumber())
                .isEqualTo(1);

        assertThat(response.kind())
                .isEqualTo(MaterialKind.PDF);

        assertThat(response.status().name())
                .isEqualTo("DRAFT");

        PdfDetails details =
                pdfDetailsRepository
                        .findByVersionId(response.versionId())
                        .orElseThrow();

        assertThat(details.getPageCount())
                .isEqualTo(50);

        assertThat(details.getFileUrl())
                .isEqualTo("https://example.com/sql.pdf");
    }
    @Test
    void shouldCreateArticleMaterialAsDraft() throws Exception {
        var request =
                new CreateMaterialRequest(
                        "Spring Transactions",
                        MaterialKind.ARTICLE,
                        objectMapper.readTree("""
                            {
                              "wordCount": 5000
                            }
                            """)
                );

        var response = materialService.createMaterial(request);

        assertThat(response.versionNumber()).isEqualTo(1);
        assertThat(response.kind()).isEqualTo(MaterialKind.ARTICLE);
        assertThat(response.status())
                .isEqualTo(MaterialVersionStatus.DRAFT);

        ArticleDetails details =
                articleDetailsRepository
                        .findByVersionId(response.versionId())
                        .orElseThrow();

        assertThat(details.getWordCount()).isEqualTo(5000);
    }
    @Test
    void shouldCreateBookMaterialAsDraft() throws Exception {
        var request =
                new CreateMaterialRequest(
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

        var response = materialService.createMaterial(request);

        assertThat(response.versionNumber()).isEqualTo(1);
        assertThat(response.kind()).isEqualTo(MaterialKind.BOOK);
        assertThat(response.status())
                .isEqualTo(MaterialVersionStatus.DRAFT);

        BookDetails details =
                bookDetailsRepository
                        .findByVersionId(response.versionId())
                        .orElseThrow();

        assertThat(details.getAuthor())
                .isEqualTo("Robert Martin");

        assertThat(details.getChapters().isArray())
                .isTrue();

        assertThat(details.getChapters().size())
                .isEqualTo(4);
    }
    @Test
    void shouldCreateSlideDeckMaterialAsDraft() throws Exception {
        var request =
                new CreateMaterialRequest(
                        "Spring Boot Slides",
                        MaterialKind.SLIDE_DECK,
                        objectMapper.readTree("""
                            {
                              "slideCount": 30,
                              "fileUrl": "https://example.com/slides.pdf"
                            }
                            """)
                );

        var response = materialService.createMaterial(request);

        assertThat(response.versionNumber()).isEqualTo(1);
        assertThat(response.kind()).isEqualTo(MaterialKind.SLIDE_DECK);
        assertThat(response.status())
                .isEqualTo(MaterialVersionStatus.DRAFT);

        SlideDeckDetails details =
                slideDeckDetailsRepository
                        .findByVersionId(response.versionId())
                        .orElseThrow();

        assertThat(details.getSlideCount()).isEqualTo(30);

        assertThat(details.getFileUrl())
                .isEqualTo("https://example.com/slides.pdf");
    }
    @Test
    void shouldRejectArticleWithoutWordCount() throws Exception {
        var request =
                new CreateMaterialRequest(
                        "Spring Article",
                        MaterialKind.ARTICLE,
                        objectMapper.readTree("""
                            {}
                            """)
                );

        assertThatThrownBy(() ->
                materialService.createMaterial(request)
        )
                .isInstanceOf(InvalidMaterialDetailsException.class)
                .hasMessage(
                        "wordCount is required for ARTICLE material."
                );
    }
    @Test
    void shouldRejectBookWithoutChapters() throws Exception {
        var request =
                new CreateMaterialRequest(
                        "Clean Code",
                        MaterialKind.BOOK,
                        objectMapper.readTree("""
                            {
                              "author": "Robert Martin",
                              "chapters": []
                            }
                            """)
                );

        assertThatThrownBy(() ->
                materialService.createMaterial(request)
        )
                .isInstanceOf(InvalidMaterialDetailsException.class)
                .hasMessage(
                        "chapters must contain at least one chapter."
                );
    }
    @Test
    void shouldRejectPdfWithoutPageCount() throws Exception {
        var request =
                new CreateMaterialRequest(
                        "Intro to SQL",
                        MaterialKind.PDF,
                        objectMapper.readTree("""
                            {
                              "fileUrl": "https://example.com/sql.pdf"
                            }
                            """)
                );

        assertThatThrownBy(() ->
                materialService.createMaterial(request)
        )
                .isInstanceOf(InvalidMaterialDetailsException.class)
                .hasMessage(
                        "pageCount is required for PDF material."
                );
    }
    @Test
    void shouldRejectSlideDeckWithoutSlideCount() throws Exception {
        var request =
                new CreateMaterialRequest(
                        "Spring Boot",
                        MaterialKind.SLIDE_DECK,
                        objectMapper.readTree("""
                            {
                              "fileUrl": "https://example.com/slides.pdf"
                            }
                            """)
                );

        assertThatThrownBy(() ->
                materialService.createMaterial(request)
        )
                .isInstanceOf(InvalidMaterialDetailsException.class)
                .hasMessage(
                        "slideCount is required for SLIDE_DECK material."
                );
    }
    @Test
    void shouldNotCreateMaterialWhenValidationFails() throws Exception {

        long materialsBefore =
                materialRepository.count();

        long versionsBefore =
                materialVersionRepository.count();

        long articleDetailsBefore =
                articleDetailsRepository.count();

        var request =
                new CreateMaterialRequest(
                        "Invalid Article",
                        MaterialKind.ARTICLE,
                        objectMapper.readTree("""
                            {}
                            """)
                );

        assertThatThrownBy(() ->
                materialService.createMaterial(request)
        )
                .isInstanceOf(InvalidMaterialDetailsException.class);

        assertThat(materialRepository.count())
                .isEqualTo(materialsBefore);

        assertThat(materialVersionRepository.count())
                .isEqualTo(versionsBefore);

        assertThat(articleDetailsRepository.count())
                .isEqualTo(articleDetailsBefore);
    }

}