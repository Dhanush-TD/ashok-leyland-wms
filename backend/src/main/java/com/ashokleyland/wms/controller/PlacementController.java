package com.ashokleyland.wms.controller;

import com.ashokleyland.wms.dto.*;
import com.ashokleyland.wms.service.PlacementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/placement")
@RequiredArgsConstructor
public class PlacementController {

    private final PlacementService placementService;

    @PostMapping("/scan-engine")
    public ResponseEntity<ScanEngineResponse> scanEngine(
            @Valid @RequestBody ScanEngineRequest request) {

        return ResponseEntity.ok(
                placementService.scanEngine(request)
        );
    }

    @PostMapping("/confirm")
    public ResponseEntity<ConfirmPlacementResponse> confirm(
            @Valid @RequestBody ConfirmPlacementRequest request) {

        return ResponseEntity.ok(
                placementService.confirmPlacement(request)
        );
    }
}