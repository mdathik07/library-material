package com.example.readinglibrary.service;

import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.entity.PdfDetails;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.repository.PdfDetailsRepository;
import org.springframework.stereotype.Component;

@Component
public class PdfDetailsCopier implements MaterialDetailsCopier {

    private final PdfDetailsRepository pdfDetailsRepository;

    public PdfDetailsCopier(
            PdfDetailsRepository pdfDetailsRepository
    ) {
        this.pdfDetailsRepository = pdfDetailsRepository;
    }

    @Override
    public MaterialKind supportedKind() {
        return MaterialKind.PDF;
    }

    @Override
    public void copyDetails(
            MaterialVersion sourceVersion,
            MaterialVersion targetVersion
    ) {
        PdfDetails source =
                pdfDetailsRepository
                        .findByVersionId(sourceVersion.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "PDF details missing for version "
                                                + sourceVersion.getId()
                                )
                        );

        PdfDetails copy =
                new PdfDetails(
                        source.getPageCount(),
                        source.getFileUrl()
                );

        copy.setVersion(targetVersion);
        targetVersion.setPdfDetails(copy);
        pdfDetailsRepository.save(copy);

    }
}