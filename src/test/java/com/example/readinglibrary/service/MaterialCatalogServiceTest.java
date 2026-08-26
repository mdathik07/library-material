package com.example.readinglibrary.service;

import com.example.readinglibrary.dto.CreateMaterialRequest;
import com.example.readinglibrary.dto.MaterialPageResponse;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.repository.MaterialRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Transactional
class MaterialCatalogServiceTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MaterialService materialService;

    @Autowired
    private MaterialCatalogService materialCatalogService;

    private final ObjectMapper objectMapper =
            new ObjectMapper();
    @Test
    void shouldReturnOnlyLiveMaterial() throws Exception {

        var created =
                materialService.createMaterial(
                        new CreateMaterialRequest(
                                "Draft PDF",
                                MaterialKind.PDF,
                                objectMapper.readTree("""
                                    {
                                      "pageCount": 50,
                                      "fileUrl": "https://example.com/a.pdf"
                                    }
                                    """)
                        )
                );

        MaterialPageResponse beforePublish =
                materialCatalogService.searchMaterials(
                        null,
                        null,
                        PageRequest.of(0, 20)
                );

        assertThat(
                beforePublish.content()
        ).isEmpty();

        materialService.publishVersion(
                created.materialId(),
                created.versionId()
        );

        MaterialPageResponse afterPublish =
                materialCatalogService.searchMaterials(
                        null,
                        null,
                        PageRequest.of(0, 20)
                );

        assertThat(afterPublish.totalElements())
                .isEqualTo(1);

        assertThat(afterPublish.content().get(0).versionId())
                .isEqualTo(created.versionId());
    }
}