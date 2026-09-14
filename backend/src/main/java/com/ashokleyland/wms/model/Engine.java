package com.ashokleyland.wms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "engines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Engine {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "engine_id", columnDefinition = "char(36)", updatable = false, nullable = false)    private String engineId;

    @Column(name = "engine_number", nullable = false, unique = true, length = 50)
    private String engineNumber;

    @Column(nullable = false, unique = true, length = 100)
    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private EngineModel model;

    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;

    @Column(name = "mfg_date", nullable = false)
    private LocalDate mfgDate;

    @Column(name = "arrival_date")
    private LocalDateTime arrivalDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 25)
    private EngineStatus currentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_location_id")
    private WarehouseLocation currentLocation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (arrivalDate == null) arrivalDate = now;
        if (currentStatus == null) currentStatus = EngineStatus.PENDING_PLACEMENT;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
