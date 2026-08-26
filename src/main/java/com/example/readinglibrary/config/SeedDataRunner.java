package com.example.readinglibrary.config;

import com.example.readinglibrary.dto.CreateMaterialRequest;
import com.example.readinglibrary.dto.CreateVersionResponse;
import com.example.readinglibrary.enums.MaterialKind;
import com.example.readinglibrary.service.MaterialService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("seed")
public class SeedDataRunner implements CommandLineRunner {

    private final MaterialService materialService;
    private final ObjectMapper objectMapper;

    public SeedDataRunner(
            MaterialService materialService,
            ObjectMapper objectMapper
    ) {
        this.materialService = materialService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {

        createAndPublish(
                new CreateMaterialRequest(
                        "Intro to SQL",
                        MaterialKind.PDF,
                        objectMapper.readTree("""
                                {
                                  "pageCount": 50,
                                  "fileUrl": "https://example.com/sql.pdf"
                                }
                                """)
                )
        );

        createAndPublish(
                new CreateMaterialRequest(
                        "Spring Transactions",
                        MaterialKind.ARTICLE,
                        objectMapper.readTree("""
                                {
                                  "wordCount": 5000
                                }
                                """)
                )
        );

        createAndPublish(
                new CreateMaterialRequest(
                        "Clean Code",
                        MaterialKind.BOOK,
                        objectMapper.readTree("""
                                {
                                  "author": "Robert Martin",
                                  "chapters": [
                                    "Introduction",
                                    "Meaningful Names",
                                    "Functions",
                                    "Comments"
                                  ]
                                }
                                """)
                )
        );

        createAndPublish(
                new CreateMaterialRequest(
                        "Spring Boot Slides",
                        MaterialKind.SLIDE_DECK,
                        objectMapper.readTree("""
                                {
                                  "slideCount": 30,
                                  "fileUrl": "https://example.com/slides.pdf"
                                }
                                """)
                )
        );
    }

    private void createAndPublish(
            CreateMaterialRequest request
    ) {
        var created =
                materialService.createMaterial(request);

        materialService.publishVersion(
                created.materialId(),
                created.versionId()
        );
    }
}