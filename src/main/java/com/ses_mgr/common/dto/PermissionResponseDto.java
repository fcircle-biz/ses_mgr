package com.ses_mgr.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionResponseDto {

    private UUID id;
    private String permissionCode;
    private String name;
    private String description;
    private String resourceType;
    private String resourceName;
    private String action;
    private String accessLevel;
    private Boolean isAssigned;
}