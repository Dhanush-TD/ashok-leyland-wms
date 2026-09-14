package com.ashokleyland.wms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "movement_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovementLog {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "movement_id", columnDefinition = "char(36)", updatable = false, nullable = false)    private String movementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "engine_id", columnDefinition = "char(36)", nullable = false)    private Engine engine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", columnDefinition = "char(36)", nullable = false)    private User operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_location_id")
    private WarehouseLocation fromLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_location_id", nullable = false)
    private WarehouseLocation toLocation;

    @Column(name = "movement_type", nullable = false, length = 50)
    private String movementType;

    @Column(name = "scanned_engine_barcode", nullable = false, length = 100)
    private String scannedEngineBarcode;

    @Column(name = "scanned_location_barcode", nullable = false, length = 100)
    private String scannedLocationBarcode;

    @Column(name = "confirmation_status", nullable = false, length = 30)
    private String confirmationStatus;

    @Column(name = "initiated_at")
    private LocalDateTime initiatedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @PrePersist
    public void prePersist() {
        if (initiatedAt == null) initiatedAt = LocalDateTime.now();
    }
}
