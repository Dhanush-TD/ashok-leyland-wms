package com.ashokleyland.wms.repository;

import com.ashokleyland.wms.model.TemporaryRelocationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemporaryRelocationRecordRepository extends JpaRepository<TemporaryRelocationRecord, String> {
}
