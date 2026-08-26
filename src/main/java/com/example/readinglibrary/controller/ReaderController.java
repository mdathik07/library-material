package com.example.readinglibrary.controller;

import com.example.readinglibrary.dto.ReaderContinuationResponse;
import com.example.readinglibrary.dto.ReaderMaterialPageResponse;
import com.example.readinglibrary.enums.ReaderMaterialStatus;
import com.example.readinglibrary.reader.ReaderContext;
import com.example.readinglibrary.reader.ReaderContextResolver;
import com.example.readinglibrary.service.ReaderMaterialService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
public class ReaderController {

    private final ReaderMaterialService readerMaterialService;
    private final ReaderContextResolver readerContextResolver;

    public ReaderController(
            ReaderMaterialService readerMaterialService,
            ReaderContextResolver readerContextResolver
    ) {
        this.readerMaterialService = readerMaterialService;
        this.readerContextResolver = readerContextResolver;
    }

    @GetMapping("/materials")
    public ResponseEntity<ReaderMaterialPageResponse> getReaderMaterials(
            @RequestHeader("X-Reader-Id") Long readerId,
            @RequestParam(required = false) ReaderMaterialStatus status,
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

        ReaderContext readerContext =
                readerContextResolver.resolve(readerId);

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "updatedAt"
                        )
                );

        ReaderMaterialPageResponse response =
                readerMaterialService.getReaderMaterials(
                        readerContext,
                        status,
                        pageable
                );

        return ResponseEntity.ok(response);
    }
    @GetMapping("/materials/{materialId}/reading")
    public ResponseEntity<ReaderContinuationResponse> continueReading(
            @PathVariable Long materialId,
            @RequestHeader("X-Reader-Id") Long readerId
    ) {
        ReaderContext readerContext =
                readerContextResolver.resolve(readerId);

        ReaderContinuationResponse response =
                readerMaterialService.continueReading(
                        materialId,
                        readerContext
                );

        return ResponseEntity.ok(response);
    }
}