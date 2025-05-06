package com.ses_mgr.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ユーザープロフィール更新リクエスト用DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequestDto {

    @NotNull(message = "名前は必須です")
    private String name;
    
    private String department;
    private String position;
    private String phone;
}