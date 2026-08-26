package com.example.readinglibrary.validation;

import com.example.readinglibrary.enums.MaterialKind;
import com.fasterxml.jackson.databind.JsonNode;

public interface MaterialDetailsValidator {

    MaterialKind supportedKind();

    void validate(JsonNode details);
}