package com.ses_mgr.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * トークンリフレッシュリクエスト用DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshRequestDto {

    @NotBlank(message = "リフレッシュトークンは必須です")
    private String refreshToken;
}