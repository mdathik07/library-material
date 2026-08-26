package com.example.readinglibrary.service;

import com.example.readinglibrary.entity.BookDetails;
import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.repository.BookDetailsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class BookDetailsCreator implements MaterialDetailsCreator {

    private final BookDetailsRepository bookDetailsRepository;

    public BookDetailsCreator(
            BookDetailsRepository bookDetailsRepository
    ) {
        this.bookDetailsRepository = bookDetailsRepository;
    }

    @Override
    public MaterialKind supportedKind() {
        return MaterialKind.BOOK;
    }

    @Override
    public void createDetails(
            MaterialVersion version,
            JsonNode details
    ) {
        BookDetails bookDetails =
                new BookDetails(
                        details.get("author").asText(),
                        details.get("chapters").deepCopy()
                );

        bookDetails.setVersion(version);
        version.setBookDetails(bookDetails);

        bookDetailsRepository.save(bookDetails);
    }
}