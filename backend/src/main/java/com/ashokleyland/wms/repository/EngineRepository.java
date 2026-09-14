package com.ashokleyland.wms.repository;

import com.ashokleyland.wms.model.Engine;
import com.ashokleyland.wms.model.EngineStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EngineRepository extends JpaRepository<Engine, String> {
    Optional<Engine> findByBarcode(String barcode);
    Optional<Engine> findByEngineNumber(String engineNumber);
    boolean existsByBarcode(String barcode);
    boolean existsByEngineNumber(String engineNumber);
    List<Engine> findByCurrentStatus(EngineStatus status);

    List<Engine> findByEngineNumberContainingIgnoreCaseOrBarcodeContainingIgnoreCaseOrBatchNumberContainingIgnoreCase(
            String engineNumber, String barcode, String batchNumber);

    Optional<Engine> findByCurrentLocation_LocationId(Integer locationId);
}
