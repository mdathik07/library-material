package com.example.readinglibrary.service;

import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.enums.MaterialKind;
import com.fasterxml.jackson.databind.JsonNode;

public interface MaterialDetailsUpdater {

    MaterialKind supportedKind();

    void updateDetails(
            MaterialVersion version,
            JsonNode details
    );
}