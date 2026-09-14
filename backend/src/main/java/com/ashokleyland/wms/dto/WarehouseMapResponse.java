package com.ashokleyland.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseMapResponse {
    private long totalLocations;
    private long occupied;
    private long available;
    private long pending;
    private long reservedTemp;
    private List<LocationDto> grid;
}
