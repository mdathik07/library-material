package com.example.readinglibrary.dto;

import com.example.readinglibrary.enums.MaterialKind;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMaterialRequest(

        @NotBlank(message = "title is required")
        String title,

        @NotNull(message = "kind is required")
        MaterialKind kind,

        @NotNull(message = "details are required")
        JsonNode details
) {
}