package com.example.readinglibrary.service;

import com.example.readinglibrary.entity.BookDetails;
import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.repository.BookDetailsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class BookDetailsUpdater implements MaterialDetailsUpdater {

    private final BookDetailsRepository bookDetailsRepository;

    public BookDetailsUpdater(
            BookDetailsRepository bookDetailsRepository
    ) {
        this.bookDetailsRepository = bookDetailsRepository;
    }

    @Override
    public MaterialKind supportedKind() {
        return MaterialKind.BOOK;
    }

    @Override
    public void updateDetails(
            MaterialVersion version,
            JsonNode details
    ) {
        BookDetails bookDetails =
                bookDetailsRepository
                        .findByVersionId(version.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Book details missing for version "
                                                + version.getId()
                                )
                        );

        bookDetails.setAuthor(
                details.get("author").asText()
        );

        bookDetails.setChapters(
                details.get("chapters").deepCopy()
        );

        bookDetailsRepository.save(bookDetails);
    }
}