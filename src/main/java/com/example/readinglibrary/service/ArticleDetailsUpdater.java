package com.example.readinglibrary.service;

import com.example.readinglibrary.entity.ArticleDetails;
import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.repository.ArticleDetailsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class ArticleDetailsUpdater implements MaterialDetailsUpdater {

    private final ArticleDetailsRepository articleDetailsRepository;

    public ArticleDetailsUpdater(
            ArticleDetailsRepository articleDetailsRepository
    ) {
        this.articleDetailsRepository = articleDetailsRepository;
    }

    @Override
    public MaterialKind supportedKind() {
        return MaterialKind.ARTICLE;
    }

    @Override
    public void updateDetails(
            MaterialVersion version,
            JsonNode details
    ) {
        ArticleDetails articleDetails =
                articleDetailsRepository
                        .findByVersionId(version.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Article details missing for version "
                                                + version.getId()
                                )
                        );

        articleDetails.setWordCount(
                details.get("wordCount").asInt()
        );

        articleDetailsRepository.save(articleDetails);
    }
}