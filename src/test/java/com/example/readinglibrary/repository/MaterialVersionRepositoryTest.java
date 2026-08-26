package com.example.readinglibrary.repository;

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
class MaterialVersionRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private MaterialVersionRepository materialVersionRepository;

    @Test
    void shouldSaveAndLoadMaterialVersion() {
        Material material =
                new Material("Intro to SQL", MaterialKind.PDF);

        material = materialRepository.saveAndFlush(material);

        MaterialVersion version =
                new MaterialVersion(1, MaterialVersionStatus.DRAFT);

        material.addVersion(version);

        materialVersionRepository.saveAndFlush(version);

        MaterialVersion saved =
                materialVersionRepository
                        .findByMaterialIdAndVersionNumber(
                                material.getId(),
                                1
                        )
                        .orElseThrow();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVersionNumber()).isEqualTo(1);
        assertThat(saved.getStatus())
                .isEqualTo(MaterialVersionStatus.DRAFT);
        assertThat(saved.getMaterial().getId())
                .isEqualTo(material.getId());
    }
}