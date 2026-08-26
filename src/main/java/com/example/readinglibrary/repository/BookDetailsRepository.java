package com.example.readinglibrary.repository;

import com.example.readinglibrary.entity.BookDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookDetailsRepository
        extends JpaRepository<BookDetails, Long> {

    Optional<BookDetails> findByVersionId(Long versionId);
}