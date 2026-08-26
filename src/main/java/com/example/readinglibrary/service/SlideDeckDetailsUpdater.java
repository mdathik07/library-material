package com.example.readinglibrary.service;

import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.entity.SlideDeckDetails;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.repository.SlideDeckDetailsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class SlideDeckDetailsUpdater implements MaterialDetailsUpdater {

    private final SlideDeckDetailsRepository slideDeckDetailsRepository;

    public SlideDeckDetailsUpdater(
            SlideDeckDetailsRepository slideDeckDetailsRepository
    ) {
        this.slideDeckDetailsRepository = slideDeckDetailsRepository;
    }

    @Override
    public MaterialKind supportedKind() {
        return MaterialKind.SLIDE_DECK;
    }

    @Override
    public void updateDetails(
            MaterialVersion version,
            JsonNode details
    ) {
        SlideDeckDetails slideDeckDetails =
                slideDeckDetailsRepository
                        .findByVersionId(version.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Slide deck details missing for version "
                                                + version.getId()
                                )
                        );

        slideDeckDetails.setSlideCount(
                details.get("slideCount").asInt()
        );

        slideDeckDetails.setFileUrl(
                details.get("fileUrl").asText()
        );

        slideDeckDetailsRepository.save(slideDeckDetails);
    }
}