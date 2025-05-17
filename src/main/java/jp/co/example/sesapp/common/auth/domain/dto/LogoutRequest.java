package jp.co.example.sesapp.common.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * ログアウトリクエストのDTO
 */
public class LogoutRequest {

    @NotBlank(message = "リフレッシュトークンは必須です")
    private String refreshToken;

    public LogoutRequest() {
    }

    public LogoutRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}