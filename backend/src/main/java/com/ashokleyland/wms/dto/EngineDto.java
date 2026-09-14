package com.ashokleyland.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EngineDto {
    private String engineNumber;
    private String barcode;
    private String model;
    private String batchNumber;
    private String mfgDate;
    private String status;
    private String lastMovement;
}
