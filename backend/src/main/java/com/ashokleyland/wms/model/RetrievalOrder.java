package com.ashokleyland.wms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "retrieval_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetrievalOrder {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "order_id", columnDefinition = "char(36)", updatable = false, nullable = false)    private String orderId;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id", columnDefinition = "char(36)", nullable = false)    private User supervisor;

    @Column(name = "excel_filename")
    private String excelFilename;

    @Column(name = "total_engines", nullable = false)
    private Integer totalEngines;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = OrderStatus.DRAFT;
        if (totalEngines == null) totalEngines = 0;
    }
}
