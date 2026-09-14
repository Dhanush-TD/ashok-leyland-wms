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
public class RelocationResponse {
    private String targetEngine;
    private String currentLocation;
    private boolean requiresRelocation;
    private int blockersCount;
    private List<RelocationStepDto> sequence;
}
