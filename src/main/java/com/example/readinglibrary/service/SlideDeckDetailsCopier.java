package com.example.readinglibrary.service;

import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.entity.SlideDeckDetails;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.repository.SlideDeckDetailsRepository;
import org.springframework.stereotype.Component;

@Component
public class SlideDeckDetailsCopier implements MaterialDetailsCopier {

    private final SlideDeckDetailsRepository slideDeckDetailsRepository;

    public SlideDeckDetailsCopier(
            SlideDeckDetailsRepository slideDeckDetailsRepository
    ) {
        this.slideDeckDetailsRepository = slideDeckDetailsRepository;
    }

    @Override
    public MaterialKind supportedKind() {
        return MaterialKind.SLIDE_DECK;
    }

    @Override
    public void copyDetails(
            MaterialVersion sourceVersion,
            MaterialVersion targetVersion
    ) {
        SlideDeckDetails source =
                slideDeckDetailsRepository
                        .findByVersionId(sourceVersion.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Slide deck details missing for version "
                                                + sourceVersion.getId()
                                )
                        );

        SlideDeckDetails copy =
                new SlideDeckDetails(
                        source.getSlideCount(),
                        source.getFileUrl()
                );

        copy.setVersion(targetVersion);
        targetVersion.setSlideDeckDetails(copy);

        slideDeckDetailsRepository.save(copy);
    }
}