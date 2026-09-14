package com.ashokleyland.wms.controller;

import com.ashokleyland.wms.dto.RelocationRequest;
import com.ashokleyland.wms.dto.RelocationResponse;
import com.ashokleyland.wms.service.BulkOrderService;
import com.ashokleyland.wms.service.RelocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/retrieval")
@RequiredArgsConstructor
public class RetrievalController {

    private final RelocationService relocationService;
    private final BulkOrderService bulkOrderService;

    @PostMapping("/solve-relocation")
    public ResponseEntity<RelocationResponse> solveRelocation(@Valid @RequestBody RelocationRequest request) {
        return ResponseEntity.ok(relocationService.solveRelocation(request));
    }

    @PostMapping(value = "/upload-excel", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> uploadExcel(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(bulkOrderService.processUpload(file));
    }
}
