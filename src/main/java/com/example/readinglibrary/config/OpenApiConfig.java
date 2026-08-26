package com.example.readinglibrary.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Reading Material Library API",
                version = "1.0",
                description = """
                        Backend API for the Reading Material Library.

                        Supports PDF, Article, Book and Slide Deck materials,
                        immutable material versions, publishing/rollback,
                        reader-specific progress and library search.
                        """
        )
)
public class OpenApiConfig {
}