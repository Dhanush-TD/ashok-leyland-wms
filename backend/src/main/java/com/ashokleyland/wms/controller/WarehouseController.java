package com.ashokleyland.wms.controller;

import com.ashokleyland.wms.dto.LocationDto;
import com.ashokleyland.wms.dto.WarehouseMapResponse;
import com.ashokleyland.wms.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping("/map")
    public ResponseEntity<WarehouseMapResponse> getMap() {
        return ResponseEntity.ok(warehouseService.getMap());
    }

    @GetMapping("/location/{locationCode}")
    public ResponseEntity<LocationDto> getLocation(@PathVariable String locationCode) {
        return ResponseEntity.ok(warehouseService.getLocationDetail(locationCode));
    }
}
