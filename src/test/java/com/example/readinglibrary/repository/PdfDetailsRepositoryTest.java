package com.example.readinglibrary.repository;

import com.example.readinglibrary.entity.Material;
import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.entity.PdfDetails;
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
class PdfDetailsRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private MaterialVersionRepository materialVersionRepository;

    @Autowired
    private PdfDetailsRepository pdfDetailsRepository;

    @Test
    void shouldSaveAndLoadPdfDetails() {
        Material material =
                materialRepository.saveAndFlush(
                        new Material("Intro to SQL", MaterialKind.PDF)
                );

        MaterialVersion version =
                new MaterialVersion(1, MaterialVersionStatus.DRAFT);

        material.addVersion(version);
        materialVersionRepository.saveAndFlush(version);

        PdfDetails details =
                new PdfDetails(
                        50,
                        "https://example.com/sql.pdf"
                );

        details.setVersion(version);

        pdfDetailsRepository.saveAndFlush(details);

        PdfDetails saved =
                pdfDetailsRepository
                        .findByVersionId(version.getId())
                        .orElseThrow();

        assertThat(saved.getVersionId())
                .isEqualTo(version.getId());

        assertThat(saved.getPageCount())
                .isEqualTo(50);

        assertThat(saved.getFileUrl())
                .isEqualTo("https://example.com/sql.pdf");
    }
}