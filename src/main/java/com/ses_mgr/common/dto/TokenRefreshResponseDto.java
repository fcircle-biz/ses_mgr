package com.ses_mgr.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * トークンリフレッシュレスポンス用DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponseDto {

    private String accessToken;
    private String tokenType;
    private long expiresIn;
}