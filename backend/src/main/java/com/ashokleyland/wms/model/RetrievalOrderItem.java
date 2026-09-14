package com.ashokleyland.wms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "retrieval_order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetrievalOrderItem {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "item_id", columnDefinition = "char(36)", updatable = false, nullable = false)    private String itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", columnDefinition = "char(36)", nullable = false)    private RetrievalOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "engine_id", columnDefinition = "char(36)", nullable = false)    private Engine engine;

    @Column(name = "retrieval_sequence", nullable = false)
    private Integer retrievalSequence;

    @Column(name = "requires_relocation")
    private Boolean requiresRelocation;

    @Column(name = "relocation_count")
    private Integer relocationCount;

    @Column(length = 30)
    private String status;

    @Column(name = "retrieved_at")
    private LocalDateTime retrievedAt;

    @PrePersist
    public void prePersist() {
        if (status == null) status = "PENDING";
        if (requiresRelocation == null) requiresRelocation = false;
        if (relocationCount == null) relocationCount = 0;
    }
}
