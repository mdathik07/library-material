package com.example.readinglibrary.service;

import com.example.readinglibrary.entity.BookDetails;
import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.repository.BookDetailsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class BookDetailsCopier implements MaterialDetailsCopier {

    private final BookDetailsRepository bookDetailsRepository;

    public BookDetailsCopier(
            BookDetailsRepository bookDetailsRepository
    ) {
        this.bookDetailsRepository = bookDetailsRepository;
    }

    @Override
    public MaterialKind supportedKind() {
        return MaterialKind.BOOK;
    }

    @Override
    public void copyDetails(
            MaterialVersion sourceVersion,
            MaterialVersion targetVersion
    ) {
        BookDetails source =
                bookDetailsRepository
                        .findByVersionId(sourceVersion.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Book details missing for version "
                                                + sourceVersion.getId()
                                )
                        );

        JsonNode chaptersCopy =
                source.getChapters().deepCopy();

        BookDetails copy =
                new BookDetails(
                        source.getAuthor(),
                        chaptersCopy
                );

        copy.setVersion(targetVersion);
        targetVersion.setBookDetails(copy);

        bookDetailsRepository.save(copy);
    }
}