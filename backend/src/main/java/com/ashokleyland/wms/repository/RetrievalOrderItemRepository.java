package com.ashokleyland.wms.repository;

import com.ashokleyland.wms.model.RetrievalOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RetrievalOrderItemRepository extends JpaRepository<RetrievalOrderItem, String> {
    List<RetrievalOrderItem> findByOrder_OrderIdOrderByRetrievalSequenceAsc(String orderId);
}
