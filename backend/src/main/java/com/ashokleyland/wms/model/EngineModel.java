package com.ashokleyland.wms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "engine_models")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EngineModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_id")
    private Integer modelId;

    @Column(name = "model_code", nullable = false, unique = true, length = 30)
    private String modelCode;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "engine_type", nullable = false, length = 50)
    private String engineType;

    @Column(name = "displacement_cc", nullable = false)
    private Integer displacementCc;

    @Column(name = "power_hp", nullable = false)
    private Integer powerHp;

    @Column(name = "weight_kg", nullable = false, precision = 8, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "dimensions_mm", nullable = false, length = 50)
    private String dimensionsMm;
}
