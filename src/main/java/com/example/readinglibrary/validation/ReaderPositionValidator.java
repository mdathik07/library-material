package com.example.readinglibrary.validation;

import com.example.readinglibrary.entity.BookDetails;
import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.exception.InvalidMaterialDetailsException;
import org.springframework.stereotype.Component;

@Component
public class ReaderPositionValidator {

    public boolean validateAndGetCompleted(
            MaterialVersion version,
            int position
    ) {
        MaterialKind kind = version.getMaterial().getKind();

        return switch (kind) {
            case PDF -> validatePdf(version, position);
            case ARTICLE -> validateArticle(position);
            case BOOK -> validateBook(version, position);
            case SLIDE_DECK -> validateSlideDeck(version, position);
        };
    }

    private boolean validatePdf(
            MaterialVersion version,
            int position
    ) {
        if (version.getPdfDetails() == null) {
            throw new IllegalStateException(
                    "PDF details are missing for version "
                            + version.getId()
            );
        }

        int pageCount =
                version.getPdfDetails().getPageCount();

        if (position < 1 || position > pageCount) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_READER_POSITION",
                    "PDF position must be between 1 and "
                            + pageCount + "."
            );
        }

        return position == pageCount;
    }

    private boolean validateArticle(int position) {
        if (position < 0 || position > 100) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_READER_POSITION",
                    "Article percentage must be between 0 and 100."
            );
        }

        return position == 100;
    }

    private boolean validateBook(
            MaterialVersion version,
            int position
    ) {
        if (version.getBookDetails() == null) {
            throw new IllegalStateException(
                    "Book details are missing for version "
                            + version.getId()
            );
        }

        BookDetails details =
                version.getBookDetails();

        int chapterCount =
                details.getChapters().size();

        if (position < 1 || position > chapterCount) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_READER_POSITION",
                    "Book chapter must be between 1 and "
                            + chapterCount + "."
            );
        }

        return position == chapterCount;
    }

    private boolean validateSlideDeck(
            MaterialVersion version,
            int position
    ) {
        if (version.getSlideDeckDetails() == null) {
            throw new IllegalStateException(
                    "Slide deck details are missing for version "
                            + version.getId()
            );
        }

        int slideCount =
                version.getSlideDeckDetails().getSlideCount();

        if (position < 1 || position > slideCount) {
            throw new InvalidMaterialDetailsException(
                    "INVALID_READER_POSITION",
                    "Slide position must be between 1 and "
                            + slideCount + "."
            );
        }

        return position == slideCount;
    }
}