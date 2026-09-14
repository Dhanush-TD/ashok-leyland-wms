package com.ashokleyland.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDto {
    private String locationCode;
    private String rowCode;
    private Integer colNumber;
    private String status;
    private EngineDto engine;
}
