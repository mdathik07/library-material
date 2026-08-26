package com.example.readinglibrary.repository;

import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.enums.MaterialVersionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface MaterialVersionRepository
        extends JpaRepository<MaterialVersion, Long> {

    Optional<MaterialVersion> findByMaterialIdAndVersionNumber(
            Long materialId,
            Integer versionNumber
    );

    Optional<MaterialVersion> findByMaterialIdAndStatus(
            Long materialId,
            MaterialVersionStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT mv
            FROM MaterialVersion mv
            WHERE mv.id = :versionId
            """)
    Optional<MaterialVersion> findByIdForUpdate(
            @Param("versionId") Long versionId
    );

    @Query("""
        SELECT COALESCE(MAX(mv.versionNumber), 0)
        FROM MaterialVersion mv
        WHERE mv.material.id = :materialId
        """)
    Integer findMaxVersionNumber(
            @Param("materialId") Long materialId
    );
}