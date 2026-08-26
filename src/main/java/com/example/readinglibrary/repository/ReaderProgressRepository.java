package com.example.readinglibrary.repository;

import com.example.readinglibrary.entity.ReaderProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReaderProgressRepository
        extends JpaRepository<ReaderProgress, Long> {

    Optional<ReaderProgress> findByReaderIdAndMaterialVersionId(
            Long readerId,
            Long materialVersionId
    );

    List<ReaderProgress> findByReaderId(Long readerId);

    List<ReaderProgress> findByReaderIdAndCompleted(
            Long readerId,
            boolean completed
    );
    Page<ReaderProgress> findByReaderId(
            Long readerId,
            Pageable pageable
    );

    Page<ReaderProgress> findByReaderIdAndCompleted(
            Long readerId,
            boolean completed,
            Pageable pageable
    );
    @Query("""
        SELECT rp
        FROM ReaderProgress rp
        JOIN FETCH rp.materialVersion mv
        JOIN FETCH mv.material m
        WHERE rp.readerId = :readerId
          AND m.id = :materialId
        ORDER BY rp.updatedAt DESC
        """)
    List<ReaderProgress> findReaderProgressForMaterial(
            @Param("readerId") Long readerId,
            @Param("materialId") Long materialId
    );
}