package com.example.readinglibrary.repository;

import com.example.readinglibrary.entity.PdfDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PdfDetailsRepository
        extends JpaRepository<PdfDetails, Long> {

    Optional<PdfDetails> findByVersionId(Long versionId);
}