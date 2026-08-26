package com.example.readinglibrary.repository;

import com.example.readinglibrary.entity.ArticleDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleDetailsRepository
        extends JpaRepository<ArticleDetails, Long> {

    Optional<ArticleDetails> findByVersionId(Long versionId);
}