package com.example.readinglibrary.service;

import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.entity.SlideDeckDetails;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.repository.SlideDeckDetailsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class SlideDeckDetailsCreator implements MaterialDetailsCreator {

    private final SlideDeckDetailsRepository slideDeckDetailsRepository;

    public SlideDeckDetailsCreator(
            SlideDeckDetailsRepository slideDeckDetailsRepository
    ) {
        this.slideDeckDetailsRepository = slideDeckDetailsRepository;
    }

    @Override
    public MaterialKind supportedKind() {
        return MaterialKind.SLIDE_DECK;
    }

    @Override
    public void createDetails(
            MaterialVersion version,
            JsonNode details
    ) {
        SlideDeckDetails slideDeckDetails =
                new SlideDeckDetails(
                        details.get("slideCount").asInt(),
                        details.get("fileUrl").asText()
                );

        slideDeckDetails.setVersion(version);
        version.setSlideDeckDetails(slideDeckDetails);

        slideDeckDetailsRepository.save(slideDeckDetails);
    }
}