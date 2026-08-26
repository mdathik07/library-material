package com.example.readinglibrary.service;

import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.entity.PdfDetails;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.repository.PdfDetailsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class PdfDetailsCreator implements MaterialDetailsCreator {

    private final PdfDetailsRepository pdfDetailsRepository;

    public PdfDetailsCreator(
            PdfDetailsRepository pdfDetailsRepository
    ) {
        this.pdfDetailsRepository = pdfDetailsRepository;
    }

    @Override
    public MaterialKind supportedKind() {
        return MaterialKind.PDF;
    }

    @Override
    public void createDetails(
            MaterialVersion version,
            JsonNode details
    ) {
        PdfDetails pdfDetails =
                new PdfDetails(
                        details.get("pageCount").asInt(),
                        details.get("fileUrl").asText()
                );

        pdfDetails.setVersion(version);
        version.setPdfDetails(pdfDetails);

        pdfDetailsRepository.save(pdfDetails);
    }
}