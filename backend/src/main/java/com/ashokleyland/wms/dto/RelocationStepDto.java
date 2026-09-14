package com.ashokleyland.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelocationStepDto {
    private int step;
    private String phase;
    private String action;
    private String engineNumber;
    private String from;
    private String to;
}
