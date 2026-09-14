package com.ashokleyland.wms.repository;

import com.ashokleyland.wms.model.MovementLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovementLogRepository extends JpaRepository<MovementLog, String> {
    List<MovementLog> findByEngine_EngineIdOrderByInitiatedAtDesc(String engineId);
}
