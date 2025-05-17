package jp.co.example.sesapp.common.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * MFA有効化リクエストのDTO
 */
public class MfaEnableRequest {

    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    @NotBlank(message = "MFAコードは必須です")
    private String mfaCode;

    public MfaEnableRequest() {
    }

    public MfaEnableRequest(UUID userId, String mfaCode) {
        this.userId = userId;
        this.mfaCode = mfaCode;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getMfaCode() {
        return mfaCode;
    }

    public void setMfaCode(String mfaCode) {
        this.mfaCode = mfaCode;
    }
}