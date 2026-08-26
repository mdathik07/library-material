package com.example.readinglibrary.controller;

import com.example.readinglibrary.dto.*;
import com.example.readinglibrary.service.MaterialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/materials")
public class AdminMaterialController {

    private final MaterialService materialService;

    public AdminMaterialController(
            MaterialService materialService
    ) {
        this.materialService = materialService;
    }

    @PostMapping
    public ResponseEntity<CreateMaterialResponse> createMaterial(
            @Valid @RequestBody CreateMaterialRequest request
    ) {
        CreateMaterialResponse response =
                materialService.createMaterial(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{materialId}/versions")
    public ResponseEntity<CreateVersionResponse> createDraftVersion(
            @PathVariable Long materialId
    ) {
        CreateVersionResponse response =
                materialService.createDraftVersion(materialId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @PutMapping("/{materialId}/versions/{versionId}")
    public ResponseEntity<Void> updateDraftVersion(
            @PathVariable Long materialId,
            @PathVariable Long versionId,
            @Valid @RequestBody UpdateMaterialVersionRequest request
    ) {
        materialService.updateDraftVersion(
                materialId,
                versionId,
                request
        );

        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{materialId}/versions/{versionId}/publish")
    public ResponseEntity<PublishVersionResponse> publishVersion(
            @PathVariable Long materialId,
            @PathVariable Long versionId
    ) {
        PublishVersionResponse response =
                materialService.publishVersion(
                        materialId,
                        versionId
                );

        return ResponseEntity.ok(response);
    }
}