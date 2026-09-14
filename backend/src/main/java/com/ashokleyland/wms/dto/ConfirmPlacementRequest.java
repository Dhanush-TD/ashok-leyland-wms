package com.ashokleyland.wms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfirmPlacementRequest {

    @NotBlank
    private String engineBarcode;

    @NotBlank
    private String locationCode;
}