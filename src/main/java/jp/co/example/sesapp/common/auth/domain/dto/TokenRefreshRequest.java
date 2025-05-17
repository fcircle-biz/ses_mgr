package jp.co.example.sesapp.common.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * トークンリフレッシュリクエストのDTO
 */
public class TokenRefreshRequest {

    @NotBlank(message = "リフレッシュトークンは必須です")
    private String refreshToken;

    public TokenRefreshRequest() {
    }

    public TokenRefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}