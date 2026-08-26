package com.example.readinglibrary.service;

import com.example.readinglibrary.entity.ArticleDetails;
import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.repository.ArticleDetailsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class ArticleDetailsCreator implements MaterialDetailsCreator {

    private final ArticleDetailsRepository articleDetailsRepository;

    public ArticleDetailsCreator(
            ArticleDetailsRepository articleDetailsRepository
    ) {
        this.articleDetailsRepository = articleDetailsRepository;
    }

    @Override
    public MaterialKind supportedKind() {
        return MaterialKind.ARTICLE;
    }

    @Override
    public void createDetails(
            MaterialVersion version,
            JsonNode details
    ) {
        ArticleDetails articleDetails =
                new ArticleDetails(
                        details.get("wordCount").asInt()
                );

        articleDetails.setVersion(version);
        version.setArticleDetails(articleDetails);

        articleDetailsRepository.save(articleDetails);
    }
}