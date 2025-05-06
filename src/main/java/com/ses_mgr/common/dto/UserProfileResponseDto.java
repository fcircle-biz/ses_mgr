package com.ses_mgr.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ユーザープロフィールレスポンス用DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDto {

    private UUID id;
    private String email;
    private String name;
    private String department;
    private String position;
    private String phone;
    private String role;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}