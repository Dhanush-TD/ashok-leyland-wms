package com.ashokleyland.wms.service;

import com.ashokleyland.wms.dto.EngineDto;
import com.ashokleyland.wms.dto.LocationDto;
import com.ashokleyland.wms.dto.WarehouseMapResponse;
import com.ashokleyland.wms.exception.ResourceNotFoundException;
import com.ashokleyland.wms.model.Engine;
import com.ashokleyland.wms.model.WarehouseLocation;
import com.ashokleyland.wms.repository.EngineRepository;
import com.ashokleyland.wms.repository.WarehouseLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Backs FR-01 (2D cinema grid) and FR-02 (cell drawer). */
@Service
@RequiredArgsConstructor
public class WarehouseService {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WarehouseLocationRepository locationRepository;
    private final EngineRepository engineRepository;

    public WarehouseMapResponse getMap() {
        List<WarehouseLocation> locations = locationRepository.findAllByOrderByRowCodeAscColNumberAsc();

        long occupied = 0, available = 0, pending = 0, reservedTemp = 0;
        List<LocationDto> grid = new ArrayList<>();

        for (WarehouseLocation loc : locations) {
            switch (loc.getStatus()) {
                case OCCUPIED -> occupied++;
                case AVAILABLE -> available++;
                case PENDING_CONFIRMATION -> pending++;
                case RESERVED_TEMP -> reservedTemp++;
                default -> {}
            }
            grid.add(toLocationDto(loc));
        }

        return WarehouseMapResponse.builder()
                .totalLocations(locations.size())
                .occupied(occupied)
                .available(available)
                .pending(pending)
                .reservedTemp(reservedTemp)
                .grid(grid)
                .build();
    }

    public LocationDto getLocationDetail(String locationCode) {
        WarehouseLocation loc = locationRepository.findByLocationCode(locationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + locationCode));
        return toLocationDto(loc);
    }

    private LocationDto toLocationDto(WarehouseLocation loc) {
        EngineDto engineDto = engineRepository.findByCurrentLocation_LocationId(loc.getLocationId())
                .map(this::toEngineDto)
                .orElse(null);

        return LocationDto.builder()
                .locationCode(loc.getLocationCode())
                .rowCode(loc.getRowCode())
                .colNumber(loc.getColNumber())
                .status(loc.getStatus().name())
                .engine(engineDto)
                .build();
    }

    private EngineDto toEngineDto(Engine engine) {
        return EngineDto.builder()
                .engineNumber(engine.getEngineNumber())
                .barcode(engine.getBarcode())
                .model(engine.getModel() != null ? engine.getModel().getModelName() : null)
                .batchNumber(engine.getBatchNumber())
                .mfgDate(engine.getMfgDate() != null ? engine.getMfgDate().toString() : null)
                .status(engine.getCurrentStatus().name())
                .lastMovement(engine.getUpdatedAt() != null ? engine.getUpdatedAt().format(TS_FMT) : null)
                .build();
    }
}
