package com.example.readinglibrary.repository;

import com.example.readinglibrary.entity.Material;
import com.example.readinglibrary.enums.MaterialKind;
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
class MaterialRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MaterialRepository materialRepository;

    @Test
    void shouldSaveAndLoadMaterial() {
        Material material =
                new Material("Intro to SQL", MaterialKind.PDF);

        Material saved = materialRepository.saveAndFlush(material);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Intro to SQL");
        assertThat(saved.getKind()).isEqualTo(MaterialKind.PDF);
    }
}