package com.ashokleyland.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private String userId;
    private String username;
    private String fullName;
    private String role;
    private String badgeId;
}
