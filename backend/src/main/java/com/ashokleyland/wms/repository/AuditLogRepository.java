package com.ashokleyland.wms.repository;

import com.ashokleyland.wms.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
