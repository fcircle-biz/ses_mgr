package jp.co.example.sesapp.common.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * MFA無効化リクエストのDTO
 */
public class MfaDisableRequest {

    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    @NotBlank(message = "パスワードは必須です")
    private String password;

    public MfaDisableRequest() {
    }

    public MfaDisableRequest(UUID userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}