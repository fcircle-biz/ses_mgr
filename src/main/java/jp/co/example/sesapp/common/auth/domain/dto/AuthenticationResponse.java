package jp.co.example.sesapp.common.auth.domain.dto;

import java.util.UUID;

/**
 * 認証結果。
 * 認証成功時にクライアントに返す情報を表します。
 */
public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String tokenType = "Bearer";
    private boolean requiresMfa;
    private UUID userId;
    private UserDto user;
    private MfaChallenge mfaChallenge;

    public AuthenticationResponse() {
    }

    // 通常の認証成功レスポンス
    public static AuthenticationResponse createTokenResponse(String accessToken, String refreshToken, long expiresIn) {
        AuthenticationResponse response = new AuthenticationResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(expiresIn);
        response.setRequiresMfa(false);
        return response;
    }

    // MFA認証が必要なレスポンス
    public static AuthenticationResponse createMfaResponse(UUID userId) {
        AuthenticationResponse response = new AuthenticationResponse();
        response.setRequiresMfa(true);
        response.setUserId(userId);
        return response;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public boolean isRequiresMfa() {
        return requiresMfa;
    }

    public void setRequiresMfa(boolean requiresMfa) {
        this.requiresMfa = requiresMfa;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public UserDto getUser() {
        return user;
    }
    
    public void setUser(UserDto user) {
        this.user = user;
    }
    
    public MfaChallenge getMfaChallenge() {
        return mfaChallenge;
    }
    
    public void setMfaChallenge(MfaChallenge mfaChallenge) {
        this.mfaChallenge = mfaChallenge;
    }
}