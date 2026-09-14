package com.ashokleyland.wms.repository;

import com.ashokleyland.wms.model.LocationStatus;
import com.ashokleyland.wms.model.WarehouseLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseLocationRepository extends JpaRepository<WarehouseLocation, Integer> {
    Optional<WarehouseLocation> findByLocationCode(String locationCode);
    List<WarehouseLocation> findByRowCodeOrderByColNumberAsc(String rowCode);
    List<WarehouseLocation> findByStatus(LocationStatus status);
    List<WarehouseLocation> findAllByOrderByRowCodeAscColNumberAsc();
}
