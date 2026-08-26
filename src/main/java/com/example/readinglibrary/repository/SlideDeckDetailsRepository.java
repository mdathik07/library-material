package com.example.readinglibrary.repository;

import com.example.readinglibrary.entity.SlideDeckDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SlideDeckDetailsRepository
        extends JpaRepository<SlideDeckDetails, Long> {

    Optional<SlideDeckDetails> findByVersionId(Long versionId);
}