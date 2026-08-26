package com.example.readinglibrary.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SaveProgressRequest(

        @NotNull(message = "position is required")
        @PositiveOrZero(message = "position must be zero or greater")
        Integer position
) {
}