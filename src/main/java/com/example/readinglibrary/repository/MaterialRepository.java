package com.example.readinglibrary.repository;

import com.example.readinglibrary.entity.Material;
import com.example.readinglibrary.enums.MaterialKind;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MaterialRepository
        extends JpaRepository<Material, Long> {

    Optional<Material> findByIdAndKind(
            Long id,
            MaterialKind kind
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT m
        FROM Material m
        WHERE m.id = :materialId
        """)
    Optional<Material> findByIdForUpdate(
            @Param("materialId") Long materialId
    );
    @Query(
            value = """
                SELECT
                    m.id AS materialId,
                    m.title AS title,
                    m.kind AS kind,
                    mv.id AS versionId,
                    mv.version_number AS versionNumber,
                    pd.page_count AS pdfPageCount
                FROM materials m
                JOIN material_versions mv
                    ON mv.material_id = m.id
                   AND mv.status = 'LIVE'
                LEFT JOIN pdf_details pd
                    ON pd.version_id = mv.id
                WHERE (:kind IS NULL OR m.kind = :kind)
                  AND (
                        :minPages IS NULL
                        OR (
                            m.kind = 'PDF'
                            AND pd.page_count >= :minPages
                        )
                  )
                ORDER BY m.id ASC
                """,
            countQuery = """
                SELECT COUNT(*)
                FROM materials m
                JOIN material_versions mv
                    ON mv.material_id = m.id
                   AND mv.status = 'LIVE'
                LEFT JOIN pdf_details pd
                    ON pd.version_id = mv.id
                WHERE (:kind IS NULL OR m.kind = :kind)
                  AND (
                        :minPages IS NULL
                        OR (
                            m.kind = 'PDF'
                            AND pd.page_count >= :minPages
                        )
                  )
                """,
            nativeQuery = true
    )
    Page<MaterialCatalogProjection> findLiveCatalog(
            @Param("kind") String kind,
            @Param("minPages") Integer minPages,
            Pageable pageable
    );
}