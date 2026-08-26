package com.example.readinglibrary.service;

import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.entity.PdfDetails;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.repository.PdfDetailsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class PdfDetailsUpdater implements MaterialDetailsUpdater {

    private final PdfDetailsRepository pdfDetailsRepository;

    public PdfDetailsUpdater(
            PdfDetailsRepository pdfDetailsRepository
    ) {
        this.pdfDetailsRepository = pdfDetailsRepository;
    }

    @Override
    public MaterialKind supportedKind() {
        return MaterialKind.PDF;
    }

    @Override
    public void updateDetails(
            MaterialVersion version,
            JsonNode details
    ) {
        PdfDetails pdfDetails =
                pdfDetailsRepository
                        .findByVersionId(version.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "PDF details missing for version "
                                                + version.getId()
                                )
                        );

        pdfDetails.setPageCount(
                details.get("pageCount").asInt()
        );

        pdfDetails.setFileUrl(
                details.get("fileUrl").asText()
        );

        pdfDetailsRepository.save(pdfDetails);
    }
}