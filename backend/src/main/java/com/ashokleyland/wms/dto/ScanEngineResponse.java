package com.ashokleyland.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanEngineResponse {
    private String status;
    private String engineNumber;
    private String recommendedLocation;
    private double distanceMeters;
}
