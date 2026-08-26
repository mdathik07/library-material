package com.example.readinglibrary.controller;

import com.example.readinglibrary.dto.ReaderMaterialResponse;
import com.example.readinglibrary.reader.ReaderContext;
import com.example.readinglibrary.reader.ReaderContextResolver;
import com.example.readinglibrary.service.MaterialCatalogService;
import com.example.readinglibrary.service.ReaderMaterialService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.readinglibrary.dto.SaveProgressRequest;
import com.example.readinglibrary.dto.SaveProgressResponse;
import jakarta.validation.Valid;
import com.example.readinglibrary.dto.MaterialPageResponse;
import com.example.readinglibrary.enums.MaterialKind;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/materials")
public class MaterialController {

    private final ReaderMaterialService readerMaterialService;
    private final ReaderContextResolver readerContextResolver;
    private final MaterialCatalogService materialCatalogService;

    public MaterialController(
            ReaderMaterialService readerMaterialService,
            ReaderContextResolver readerContextResolver,
            MaterialCatalogService materialCatalogService
    ) {
        this.readerMaterialService = readerMaterialService;
        this.readerContextResolver = readerContextResolver;
        this.materialCatalogService = materialCatalogService;
    }

    @GetMapping("/{materialId}")
    public ResponseEntity<ReaderMaterialResponse> openMaterial(
            @PathVariable Long materialId,
            @RequestHeader("X-Reader-Id") Long readerId
    ) {
        ReaderContext readerContext =
                readerContextResolver.resolve(readerId);

        ReaderMaterialResponse response =
                readerMaterialService.openMaterial(
                        materialId,
                        readerContext
                );

        return ResponseEntity.ok(response);
    }
    @PutMapping("/{materialId}/versions/{versionId}/progress")
    public ResponseEntity<SaveProgressResponse> saveProgress(
            @PathVariable Long materialId,
            @PathVariable Long versionId,
            @RequestHeader("X-Reader-Id") Long readerId,
            @Valid @RequestBody SaveProgressRequest request
    ) {
        ReaderContext readerContext =
                readerContextResolver.resolve(readerId);

        SaveProgressResponse response =
                readerMaterialService.saveProgress(
                        materialId,
                        versionId,
                        readerContext,
                        request
                );

        return ResponseEntity.ok(response);
    }
    @GetMapping
    public ResponseEntity<MaterialPageResponse> listMaterials(
            @RequestParam(required = false) MaterialKind kind,
            @RequestParam(required = false) Integer minPages,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "page must be greater than or equal to 0"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "size must be between 1 and 100"
            );
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        size
                );

        MaterialPageResponse response =
                materialCatalogService.searchMaterials(
                        kind,
                        minPages,
                        pageable
                );

        return ResponseEntity.ok(response);
    }
}