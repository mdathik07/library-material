package com.example.readinglibrary.controller;

import com.example.readinglibrary.entity.PdfDetails;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.repository.MaterialRepository;
import com.example.readinglibrary.repository.PdfDetailsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AdminMaterialControllerTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private PdfDetailsRepository pdfDetailsRepository;

    @Test
    void shouldCreatePdfMaterial() throws Exception {

        String request = """
                {
                  "title": "Intro to SQL",
                  "kind": "PDF",
                  "details": {
                    "pageCount": 50,
                    "fileUrl": "https://example.com/sql.pdf"
                  }
                }
                """;

        mockMvc.perform(
                        post("/api/admin/materials")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.materialId").isNumber())
                .andExpect(jsonPath("$.versionId").isNumber())
                .andExpect(jsonPath("$.versionNumber").value(1))
                .andExpect(jsonPath("$.title").value("Intro to SQL"))
                .andExpect(jsonPath("$.kind").value("PDF"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }
    @Test
    void shouldRejectArticleWithoutWordCount() throws Exception {

        String request = """
        {
          "title": "Spring Article",
          "kind": "ARTICLE",
          "details": {}
        }
        """;

        mockMvc.perform(
                        post("/api/admin/materials")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("INVALID_ARTICLE_DETAILS"))
                .andExpect(jsonPath("$.message")
                        .value("wordCount is required for ARTICLE material."));
    }
}