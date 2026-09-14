package com.ashokleyland.wms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Integer locationId;

    @Column(name = "location_code", nullable = false, unique = true, length = 10)
    private String locationCode;

    @Column(name = "row_code", nullable = false, length = 5)
    private String rowCode;

    @Column(name = "col_number", nullable = false)
    private Integer colNumber;

    @Column(name = "zone_name", length = 30)
    private String zoneName;

    @Column(name = "is_temp_allowed")
    private Boolean isTempAllowed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private LocationStatus status;

    @Column(name = "current_engine_id", columnDefinition = "char(36)")
    private String currentEngineId;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    public void touch() {
        lastUpdated = LocalDateTime.now();
        if (status == null) status = LocationStatus.AVAILABLE;
        if (zoneName == null) zoneName = "MAIN_BAY";
        if (isTempAllowed == null) isTempAllowed = true;
    }

    /** Manhattan-distance helper used by the placement & relocation algorithms. */
    public int manhattanDistanceTo(WarehouseLocation other) {
        int rowDelta = Math.abs(this.rowCode.charAt(0) - other.rowCode.charAt(0));
        int colDelta = Math.abs(this.colNumber - other.colNumber);
        return rowDelta + colDelta;
    }
}
