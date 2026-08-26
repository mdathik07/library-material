package com.example.readinglibrary.service;

import com.example.readinglibrary.dto.ReaderMaterialResponse;
import com.example.readinglibrary.dto.ReaderProgressResponse;
import com.example.readinglibrary.entity.*;
import com.example.readinglibrary.enums.MaterialKind;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

@Component
public class ReaderMaterialResponseMapper {

    private final ObjectMapper objectMapper;

    public ReaderMaterialResponseMapper(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    public ReaderMaterialResponse toResponse(
            Material material,
            MaterialVersion version,
            ReaderProgress progress
    ) {
        JsonNode details =
                buildDetails(version, material.getKind());

        ReaderProgressResponse progressResponse =
                progress == null
                        ? null
                        : new ReaderProgressResponse(
                        progress.getMaterialVersion().getId(),
                        progress.getPosition(),
                        progress.isCompleted()
                );

        return new ReaderMaterialResponse(
                material.getId(),
                material.getTitle(),
                material.getKind(),
                version.getId(),
                version.getVersionNumber(),
                details,
                progressResponse
        );
    }

    private JsonNode buildDetails(
            MaterialVersion version,
            MaterialKind kind
    ) {
        ObjectNode details =
                objectMapper.createObjectNode();

        switch (kind) {
            case PDF -> {
                PdfDetails pdf =
                        version.getPdfDetails();

                details.put(
                        "pageCount",
                        pdf.getPageCount()
                );

                details.put(
                        "fileUrl",
                        pdf.getFileUrl()
                );
            }

            case ARTICLE -> {
                ArticleDetails article =
                        version.getArticleDetails();

                details.put(
                        "wordCount",
                        article.getWordCount()
                );
            }

            case BOOK -> {
                BookDetails book =
                        version.getBookDetails();

                details.put(
                        "author",
                        book.getAuthor()
                );

                details.set(
                        "chapters",
                        book.getChapters()
                );
            }

            case SLIDE_DECK -> {
                SlideDeckDetails slideDeck =
                        version.getSlideDeckDetails();

                details.put(
                        "slideCount",
                        slideDeck.getSlideCount()
                );

                details.put(
                        "fileUrl",
                        slideDeck.getFileUrl()
                );
            }
        }

        return details;
    }
}