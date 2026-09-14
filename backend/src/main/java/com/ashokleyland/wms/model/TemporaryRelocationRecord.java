package com.ashokleyland.wms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "temporary_relocation_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemporaryRelocationRecord {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "relocation_id", columnDefinition = "char(36)", updatable = false, nullable = false)    private String relocationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", columnDefinition = "char(36)", nullable = false)    private RetrievalOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_engine_id", columnDefinition = "char(36)", nullable = false)    private Engine targetEngine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocking_engine_id", columnDefinition = "char(36)", nullable = false)    private Engine blockingEngine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_location_id", nullable = false)
    private WarehouseLocation originalLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "temporary_location_id", nullable = false)
    private WarehouseLocation temporaryLocation;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RelocationState state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", columnDefinition = "char(36)")    private User operator;

    @Column(name = "moved_to_temp_at")
    private LocalDateTime movedToTempAt;

    @Column(name = "restored_at")
    private LocalDateTime restoredAt;

    @PrePersist
    public void prePersist() {
        if (state == null) state = RelocationState.SCHEDULED;
    }
}
