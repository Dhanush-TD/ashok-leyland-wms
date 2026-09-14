package com.ashokleyland.wms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RelocationRequest {
    @NotBlank
    private String targetEngineNumber;
}
