package com.ashokleyland.wms.repository;

import com.ashokleyland.wms.model.RetrievalOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RetrievalOrderRepository extends JpaRepository<RetrievalOrder, String> {
    Optional<RetrievalOrder> findByOrderNumber(String orderNumber);
}
