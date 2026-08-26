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
import org.springframework.dao.DataIntegrityViolationException;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@DataJpaTest
class MaterialVersionConstraintTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private MaterialVersionRepository materialVersionRepository;

    @Test
    void shouldRejectTwoLiveVersionsForSameMaterial() {
        Material material =
                materialRepository.saveAndFlush(
                        new Material(
                                "Intro to SQL",
                                MaterialKind.PDF
                        )
                );

        MaterialVersion version1 =
                new MaterialVersion(
                        1,
                        MaterialVersionStatus.LIVE
                );

        version1.setPublishedAt(OffsetDateTime.now());
        material.addVersion(version1);

        materialVersionRepository.saveAndFlush(version1);

        MaterialVersion version2 =
                new MaterialVersion(
                        2,
                        MaterialVersionStatus.LIVE
                );

        version2.setPublishedAt(OffsetDateTime.now());
        material.addVersion(version2);

        assertThatThrownBy(() ->
                materialVersionRepository.saveAndFlush(version2)
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}