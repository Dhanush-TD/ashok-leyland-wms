package com.ashokleyland.wms.service;

import com.ashokleyland.wms.dto.RelocationRequest;
import com.ashokleyland.wms.dto.RelocationResponse;
import com.ashokleyland.wms.dto.RelocationStepDto;
import com.ashokleyland.wms.exception.ResourceNotFoundException;
import com.ashokleyland.wms.model.Engine;
import com.ashokleyland.wms.model.LocationStatus;
import com.ashokleyland.wms.model.WarehouseLocation;
import com.ashokleyland.wms.repository.EngineRepository;
import com.ashokleyland.wms.repository.WarehouseLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Java port of Algorithm 1 (ComputeRelocationPlan) from Algorithms_and_Optimization.md #1.
 *
 * Detects engines blocking access to a target engine along its aisle (same row,
 * columns 1..targetCol-1), then builds a 3-phase greedy minimal-travel plan:
 *   Phase 1 TEMP_RELOCATION  - move each blocker to its nearest free cell (Manhattan distance)
 *   Phase 2 TARGET_EXTRACTION - retrieve the target engine to the dispatch bay
 *   Phase 3 RESTORATION       - return blockers to their original cells, LIFO order
 */
@Service
@RequiredArgsConstructor
public class RelocationService {

    private final EngineRepository engineRepository;
    private final WarehouseLocationRepository locationRepository;

    public RelocationResponse solveRelocation(RelocationRequest request) {
        Engine target = engineRepository.findByEngineNumber(request.getTargetEngineNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unknown engine number: " + request.getTargetEngineNumber()));

        WarehouseLocation targetLoc = target.getCurrentLocation();
        if (targetLoc == null) {
            throw new IllegalArgumentException(
                    "Engine " + target.getEngineNumber() + " is not currently stored in the warehouse");
        }

        List<WarehouseLocation> rowLocations = locationRepository
                .findByRowCodeOrderByColNumberAsc(targetLoc.getRowCode());

        // Step 1: detect blocking engines in columns 1..targetCol-1 of the same row/aisle.
        List<WarehouseLocation> blockingLocations = new ArrayList<>();
        for (WarehouseLocation loc : rowLocations) {
            if (loc.getColNumber() >= targetLoc.getColNumber()) continue;
            if (loc.getStatus() == LocationStatus.OCCUPIED) {
                blockingLocations.add(loc);
            }
        }

        if (blockingLocations.isEmpty()) {
            return RelocationResponse.builder()
                    .targetEngine(target.getEngineNumber())
                    .currentLocation(targetLoc.getLocationCode())
                    .requiresRelocation(false)
                    .blockersCount(0)
                    .sequence(List.of(RelocationStepDto.builder()
                            .step(1)
                            .phase("TARGET_EXTRACTION")
                            .action("Extract " + target.getEngineNumber() + " directly from "
                                    + targetLoc.getLocationCode() + " to Dispatch Bay")
                            .engineNumber(target.getEngineNumber())
                            .from(targetLoc.getLocationCode())
                            .to("DISPATCH_BAY")
                            .build()))
                    .build();
        }

        // Step 2: find nearest free temp cell for each blocker (Manhattan distance),
        // reserving cells as they're claimed so two blockers never target the same temp slot.
        List<WarehouseLocation> freeCells = locationRepository.findByStatus(LocationStatus.AVAILABLE);
        Set<Integer> reservedTempIds = new HashSet<>();

        List<RelocationStepDto> steps = new ArrayList<>();
        // (engineNumber, originalCode, tempCode) kept in blocker-processing order for the restoration phase
        List<String[]> tempAssignments = new ArrayList<>();

        for (WarehouseLocation blockerLoc : blockingLocations) {
            Engine blockerEngine = engineRepository.findByCurrentLocation_LocationId(blockerLoc.getLocationId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Location " + blockerLoc.getLocationCode() + " is OCCUPIED but has no linked engine"));

            WarehouseLocation bestTemp = freeCells.stream()
                    .filter(c -> !reservedTempIds.contains(c.getLocationId()))
                    .min(Comparator.comparingInt(c -> c.manhattanDistanceTo(blockerLoc)))
                    .orElseThrow(() -> new IllegalStateException("No free temporary storage cells available"));

            reservedTempIds.add(bestTemp.getLocationId());
            tempAssignments.add(new String[]{blockerEngine.getEngineNumber(), blockerLoc.getLocationCode(), bestTemp.getLocationCode()});

            steps.add(RelocationStepDto.builder()
                    .step(steps.size() + 1)
                    .phase("TEMP_RELOCATION")
                    .action("Move Blocker Engine " + blockerEngine.getEngineNumber() + " from "
                            + blockerLoc.getLocationCode() + " -> Temp Cell " + bestTemp.getLocationCode())
                    .engineNumber(blockerEngine.getEngineNumber())
                    .from(blockerLoc.getLocationCode())
                    .to(bestTemp.getLocationCode())
                    .build());
        }

        // Step 3: target extraction
        steps.add(RelocationStepDto.builder()
                .step(steps.size() + 1)
                .phase("TARGET_EXTRACTION")
                .action("Retrieve Target " + target.getEngineNumber() + " from " + targetLoc.getLocationCode() + " -> Dispatch Bay")
                .engineNumber(target.getEngineNumber())
                .from(targetLoc.getLocationCode())
                .to("DISPATCH_BAY")
                .build());

        // Step 4: restoration, LIFO order (reverse of temp-relocation order) to maintain zero footprint shift.
        for (int i = tempAssignments.size() - 1; i >= 0; i--) {
            String[] a = tempAssignments.get(i);
            String engineNumber = a[0], originalCode = a[1], tempCode = a[2];
            steps.add(RelocationStepDto.builder()
                    .step(steps.size() + 1)
                    .phase("RESTORATION")
                    .action("Return " + engineNumber + " from Temp Cell " + tempCode + " -> Original Cell " + originalCode)
                    .engineNumber(engineNumber)
                    .from(tempCode)
                    .to(originalCode)
                    .build());
        }

        return RelocationResponse.builder()
                .targetEngine(target.getEngineNumber())
                .currentLocation(targetLoc.getLocationCode())
                .requiresRelocation(true)
                .blockersCount(blockingLocations.size())
                .sequence(steps)
                .build();
    }
}
