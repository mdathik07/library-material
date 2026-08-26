package com.example.readinglibrary.service;

import com.example.readinglibrary.entity.ArticleDetails;
import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.repository.ArticleDetailsRepository;
import org.springframework.stereotype.Component;

@Component
public class ArticleDetailsCopier implements MaterialDetailsCopier {

    private final ArticleDetailsRepository articleDetailsRepository;

    public ArticleDetailsCopier(
            ArticleDetailsRepository articleDetailsRepository
    ) {
        this.articleDetailsRepository = articleDetailsRepository;
    }

    @Override
    public MaterialKind supportedKind() {
        return MaterialKind.ARTICLE;
    }

    @Override
    public void copyDetails(
            MaterialVersion sourceVersion,
            MaterialVersion targetVersion
    ) {
        ArticleDetails source =
                articleDetailsRepository
                        .findByVersionId(sourceVersion.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Article details missing for version "
                                                + sourceVersion.getId()
                                )
                        );

        ArticleDetails copy =
                new ArticleDetails(
                        source.getWordCount()
                );

        copy.setVersion(targetVersion);
        targetVersion.setArticleDetails(copy);

        articleDetailsRepository.save(copy);
    }
}