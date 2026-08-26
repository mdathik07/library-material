package com.example.readinglibrary.controller;

import com.example.readinglibrary.dto.CreateMaterialRequest;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.service.MaterialService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class MaterialControllerTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MaterialService materialService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldOpenLiveMaterial() throws Exception {

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

        /*
         * Publishing will be exercised through the service here.
         * The publish endpoint itself has separate tests.
         */
        materialService.publishVersion(
                created.materialId(),
                created.versionId()
        );

        mockMvc.perform(
                        get("/api/materials/{materialId}",
                                created.materialId())
                                .header("X-Reader-Id", "101")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materialId")
                        .value(created.materialId()))
                .andExpect(jsonPath("$.versionId")
                        .value(created.versionId()))
                .andExpect(jsonPath("$.versionNumber")
                        .value(1))
                .andExpect(jsonPath("$.kind")
                        .value("PDF"))
                .andExpect(jsonPath("$.details.pageCount")
                        .value(50));
    }

    @Test
    void shouldRequireReaderIdHeader() throws Exception {

        mockMvc.perform(
                        get("/api/materials/1")
                )
                .andExpect(status().isBadRequest());
    }
    @Test
    void shouldSaveReaderProgress() throws Exception {

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

        materialService.publishVersion(
                created.materialId(),
                created.versionId()
        );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders
                                .put(
                                        "/api/materials/{materialId}/versions/{versionId}/progress",
                                        created.materialId(),
                                        created.versionId()
                                )
                                .header("X-Reader-Id", "101")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "position": 20
                            }
                            """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materialId")
                        .value(created.materialId()))
                .andExpect(jsonPath("$.materialVersionId")
                        .value(created.versionId()))
                .andExpect(jsonPath("$.position")
                        .value(20))
                .andExpect(jsonPath("$.completed")
                        .value(false));
    }
    @Test
    void shouldRejectInvalidReaderPosition() throws Exception {

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

        materialService.publishVersion(
                created.materialId(),
                created.versionId()
        );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders
                                .put(
                                        "/api/materials/{materialId}/versions/{versionId}/progress",
                                        created.materialId(),
                                        created.versionId()
                                )
                                .header("X-Reader-Id", "101")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "position": 51
                            }
                            """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_READER_POSITION"));
    }
}