package com.ashokleyland.wms.service;

import com.ashokleyland.wms.dto.*;
import com.ashokleyland.wms.exception.LocationMismatchException;
import com.ashokleyland.wms.exception.ResourceNotFoundException;
import com.ashokleyland.wms.model.*;
import com.ashokleyland.wms.repository.*;
import com.ashokleyland.wms.websocket.WarehouseUpdateHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Implements FR-03 (Barcode Workflow) and FR-04 (Auto Allocation) plus the
 * Nearest Storage Location Allocation Algorithm from Algorithms_and_Optimization.md #2.
 */
@Service
@RequiredArgsConstructor
public class PlacementService {

    // A notional "Inbound Receiving Bay" anchor used for nearest-location scoring.
    // Row 'A' / Col 0 sits just outside the grid, next to the dock doors.
    private static final char INBOUND_ROW = 'A';
    private static final int INBOUND_COL = 0;
    private static final double METERS_PER_CELL = 2.5;

    private final EngineRepository engineRepository;
    private final WarehouseLocationRepository locationRepository;
    private final MovementLogRepository movementLogRepository;
    private final WarehouseUpdateHandler wsHandler;

    /**
     * Step 1 of inbound placement:
     *
     * 1. Validate the engine barcode.
     * 2. Make sure the engine is actually available for placement.
     * 3. Find the nearest available warehouse location.
     * 4. Reserve that location for THIS engine.
     * 5. Store the pending location on the engine.
     *
     * The engine is not marked STORED until the operator physically scans
     * and confirms the assigned location.
     */
    @Transactional
    public ScanEngineResponse scanEngine(ScanEngineRequest request) {

        Engine engine = engineRepository
                .findByBarcode(request.getEngineBarcode())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Unknown engine barcode: "
                                        + request.getEngineBarcode()
                        )
                );

        /*
         * Prevent an already stored/placed engine from being scanned again
         * as a new inbound engine.
         */
        if (engine.getCurrentStatus() == EngineStatus.STORED
                || engine.getCurrentLocation() != null) {

            throw new IllegalArgumentException(
                    "Engine "
                            + engine.getEngineNumber()
                            + " is already placed in the warehouse."
            );
        }

        /*
         * Only engines waiting for placement can enter this workflow.
         */
        if (engine.getCurrentStatus()
                != EngineStatus.PENDING_PLACEMENT) {

            throw new IllegalArgumentException(
                    "Engine "
                            + engine.getEngineNumber()
                            + " is not available for placement."
            );
        }

        /*
         * Find currently available warehouse locations.
         */
        List<WarehouseLocation> available =
                locationRepository.findByStatus(
                        LocationStatus.AVAILABLE
                );

        if (available.isEmpty()) {

            throw new IllegalArgumentException(
                    "No available storage locations in the warehouse"
            );
        }

        /*
         * Select the nearest available location using
         * Manhattan distance.
         */
        WarehouseLocation recommended = available.stream()
                .min(
                        Comparator.comparingInt(
                                this::distanceFromInboundBay
                        )
                )
                .orElseThrow();

        /*
         * Reserve the location for this specific engine.
         *
         * PENDING_CONFIRMATION means:
         *
         * "This location has been assigned to an engine,
         * but the operator has not physically confirmed it yet."
         */
        recommended.setStatus(
                LocationStatus.PENDING_CONFIRMATION
        );

        recommended.setCurrentEngineId(
                engine.getEngineId()
        );

        locationRepository.save(recommended);

        /*
         * Remember the assigned location on the engine.
         *
         * This allows confirmPlacement() to verify that the
         * physically scanned location is the location assigned
         * to this exact engine.
         */
        engine.setCurrentLocation(recommended);

        engine.setCurrentStatus(
                EngineStatus.PENDING_PLACEMENT
        );

        engineRepository.save(engine);

        /*
         * Calculate approximate physical distance.
         */
        double distanceMeters =
                distanceFromInboundBay(recommended)
                        * METERS_PER_CELL;

        /*
         * Notify connected warehouse-map clients.
         */
        wsHandler.broadcast(
                "LOCATION_UPDATE",
                LocationDto.builder()
                        .locationCode(
                                recommended.getLocationCode()
                        )
                        .rowCode(
                                recommended.getRowCode()
                        )
                        .colNumber(
                                recommended.getColNumber()
                        )
                        .status(
                                recommended.getStatus().name()
                        )
                        .build()
        );

        return ScanEngineResponse.builder()
                .status("VALIDATED")
                .engineNumber(
                        engine.getEngineNumber()
                )
                .recommendedLocation(
                        recommended.getLocationCode()
                )
                .distanceMeters(
                        Math.round(
                                distanceMeters * 10.0
                        ) / 10.0
                )
                .build();
    }

    /**
     * Step 2 of inbound placement.
     *
     * The operator physically scans the engine and warehouse location.
     *
     * The backend verifies:
     *
     * 1. Engine exists.
     * 2. Location exists.
     * 3. User is authenticated.
     * 4. Engine is awaiting placement confirmation.
     * 5. Engine has an assigned location.
     * 6. Scanned location matches assigned location.
     * 7. Location is still pending confirmation.
     * 8. Location is reserved for this engine.
     *
     * The operator is obtained from the authenticated JWT/SecurityContext,
     * NOT from a client-supplied operatorId.
     */
    @Transactional
    public ConfirmPlacementResponse confirmPlacement(
            ConfirmPlacementRequest request) {

        Engine engine = engineRepository
                .findByBarcode(request.getEngineBarcode())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Unknown engine barcode: "
                                        + request.getEngineBarcode()
                        )
                );

        WarehouseLocation location =
                locationRepository
                        .findByLocationCode(
                                request.getLocationCode()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Unknown location: "
                                                + request.getLocationCode()
                                )
                        );

        /*
         * Get the authenticated user from Spring Security.
         *
         * JwtAuthFilter already places the User entity into
         * Authentication.getPrincipal().
         */
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Authenticated user not found."
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal instanceof User)) {

            throw new IllegalStateException(
                    "Authenticated principal is not a valid user."
            );
        }

        User operator = (User) principal;

        /*
         * Verify that the authenticated user is active.
         *
         * This provides an additional protection in case a user's
         * account was disabled after authentication.
         */
        if (!Boolean.TRUE.equals(operator.getIsActive())) {

            throw new IllegalStateException(
                    "User account is inactive."
            );
        }

        /*
         * Engine must still be waiting for placement confirmation.
         */
        if (engine.getCurrentStatus()
                != EngineStatus.PENDING_PLACEMENT
                || engine.getCurrentLocation() == null) {

            wsHandler.broadcast(
                    "ALERT_MISMATCH",
                    Map.of(
                            "engineBarcode",
                            request.getEngineBarcode(),

                            "scannedLocation",
                            request.getLocationCode(),

                            "reason",
                            "Engine is not awaiting placement confirmation"
                    )
            );

            throw new LocationMismatchException(
                    "Engine "
                            + engine.getEngineNumber()
                            + " is not awaiting placement confirmation."
            );
        }

        /*
         * Get the location specifically assigned to this engine.
         */
        WarehouseLocation assignedLocation =
                engine.getCurrentLocation();

        /*
         * CRITICAL VALIDATION:
         *
         * The physically scanned location MUST be the same location
         * that was assigned to this engine during scanEngine().
         */
        if (!assignedLocation.getLocationId()
                .equals(location.getLocationId())) {

            wsHandler.broadcast(
                    "ALERT_MISMATCH",
                    Map.of(
                            "engineBarcode",
                            request.getEngineBarcode(),

                            "scannedLocation",
                            request.getLocationCode(),

                            "expectedLocation",
                            assignedLocation.getLocationCode(),

                            "reason",
                            "Scanned location does not match assigned location"
                    )
            );

            throw new LocationMismatchException(
                    "Location mismatch! Engine "
                            + engine.getEngineNumber()
                            + " was assigned to "
                            + assignedLocation.getLocationCode()
                            + " but "
                            + request.getLocationCode()
                            + " was scanned."
            );
        }

        /*
         * Location must still be waiting for confirmation.
         */
        if (location.getStatus()
                != LocationStatus.PENDING_CONFIRMATION) {

            wsHandler.broadcast(
                    "ALERT_MISMATCH",
                    Map.of(
                            "engineBarcode",
                            request.getEngineBarcode(),

                            "scannedLocation",
                            request.getLocationCode(),

                            "reason",
                            "Location is no longer awaiting confirmation"
                    )
            );

            throw new LocationMismatchException(
                    "Location "
                            + location.getLocationCode()
                            + " is no longer awaiting confirmation."
            );
        }

        /*
         * Verify that this location is actually reserved
         * for this exact engine.
         */
        if (!engine.getEngineId()
                .equals(location.getCurrentEngineId())) {

            wsHandler.broadcast(
                    "ALERT_MISMATCH",
                    Map.of(
                            "engineBarcode",
                            request.getEngineBarcode(),

                            "scannedLocation",
                            request.getLocationCode(),

                            "reason",
                            "Location is not reserved for this engine"
                    )
            );

            throw new LocationMismatchException(
                    "Location "
                            + location.getLocationCode()
                            + " is not reserved for this engine."
            );
        }

        /*
         * For inbound placement there is no previous warehouse
         * location.
         *
         * The engine's currentLocation is temporarily being used
         * to remember the assigned/pending location.
         */
        WarehouseLocation previousLocation = null;

        /*
         * Finalize the warehouse location.
         */
        location.setStatus(
                LocationStatus.OCCUPIED
        );

        location.setCurrentEngineId(
                engine.getEngineId()
        );

        locationRepository.save(location);

        /*
         * Finalize the engine.
         */
        engine.setCurrentLocation(location);

        engine.setCurrentStatus(
                EngineStatus.STORED
        );

        engineRepository.save(engine);

        /*
         * Create movement history.
         *
         * The operator comes from the authenticated JWT user.
         */
        MovementLog log = MovementLog.builder()
                .engine(engine)
                .operator(operator)
                .fromLocation(previousLocation)
                .toLocation(location)
                .movementType("INBOUND_PLACEMENT")
                .scannedEngineBarcode(
                        request.getEngineBarcode()
                )
                .scannedLocationBarcode(
                        request.getLocationCode()
                )
                .confirmationStatus(
                        "CONFIRMED_PLACED"
                )
                .confirmedAt(
                        LocalDateTime.now()
                )
                .build();

        movementLogRepository.save(log);

        /*
         * Notify connected warehouse-map clients that the
         * location is now occupied.
         */
        wsHandler.broadcast(
                "LOCATION_UPDATE",
                LocationDto.builder()
                        .locationCode(
                                location.getLocationCode()
                        )
                        .rowCode(
                                location.getRowCode()
                        )
                        .colNumber(
                                location.getColNumber()
                        )
                        .status(
                                location.getStatus().name()
                        )
                        .build()
        );

        return ConfirmPlacementResponse.builder()
                .success(true)
                .message(
                        "Engine "
                                + engine.getEngineNumber()
                                + " stored at location "
                                + location.getLocationCode()
                                + " successfully."
                )
                .locationStatus(
                        location.getStatus().name()
                )
                .build();
    }

    /**
     * Calculates Manhattan distance from the inbound receiving bay.
     *
     * distance =
     * |row difference| + |column difference|
     */
    private int distanceFromInboundBay(
            WarehouseLocation loc) {

        int rowDelta =
                Math.abs(
                        loc.getRowCode().charAt(0)
                                - INBOUND_ROW
                );

        int colDelta =
                Math.abs(
                        loc.getColNumber()
                                - INBOUND_COL
                );

        return rowDelta + colDelta;
    }
}