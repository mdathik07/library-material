package com.example.readinglibrary.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public record UpdateMaterialVersionRequest(

        @NotNull(message = "details are required")
        JsonNode details
) {
}