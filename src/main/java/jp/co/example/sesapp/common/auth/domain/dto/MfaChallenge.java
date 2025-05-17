package jp.co.example.sesapp.common.auth.domain.dto;

import jp.co.example.sesapp.common.auth.domain.AuthenticationMethod;
import java.util.UUID;

/**
 * 多要素認証チャレンジ。
 * 多要素認証の過程で必要な情報を表します。
 */
public class MfaChallenge {
    private UUID challengeId;
    private AuthenticationMethod method;
    private int expiresIn;
    private UUID userId;
    private String deviceId;
    private String deviceInfo;

    public MfaChallenge() {
    }

    public MfaChallenge(UUID challengeId, AuthenticationMethod method, int expiresIn) {
        this.challengeId = challengeId;
        this.method = method;
        this.expiresIn = expiresIn;
    }

    public UUID getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(UUID challengeId) {
        this.challengeId = challengeId;
    }

    public AuthenticationMethod getMethod() {
        return method;
    }

    public void setMethod(AuthenticationMethod method) {
        this.method = method;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}