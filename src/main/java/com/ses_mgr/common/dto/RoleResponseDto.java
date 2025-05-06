package com.ses_mgr.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleResponseDto {

    private UUID id;
    private String roleCode;
    private String name;
    private String description;
    private String roleType;
    private boolean isSystem;
    private Integer permissionCount;
    private Integer userCount;
    private List<PermissionResponseDto> permissions;
    private List<SimpleUserDto> users;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;
    
    private UUID createdBy;
    private UUID updatedBy;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleUserDto {
        private UUID id;
        private String username;
        private String fullName;
    }
}