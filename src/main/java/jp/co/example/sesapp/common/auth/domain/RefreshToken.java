package jp.co.example.sesapp.common.auth.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * リフレッシュトークンエンティティ。
 * アクセストークンの再取得に使用されるトークンを表します。
 */
public class RefreshToken {
    private UUID id;
    private UUID userId;
    private String tokenHash;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime expiryDate;
    private String deviceInfo;
    private String deviceId;
    private String ipAddress;
    private LocalDateTime issuedAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean revoked;
    private LocalDateTime revokedAt;
    private String revokedReason;

    public RefreshToken() {
    }

    public RefreshToken(UUID id, UUID userId, String tokenHash, LocalDateTime expiresAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
        this.revoked = false;
    }

    // ファクトリーメソッド
    public static RefreshToken createNewToken(UUID userId, String tokenHash, long validityInSeconds) {
        UUID id = UUID.randomUUID();
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(validityInSeconds);
        return new RefreshToken(id, userId, tokenHash, expiryDate);
    }

    // ビジネスロジックメソッド
    public boolean isValid() {
        return !isExpired() && !isRevoked();
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public void revoke() {
        this.revoked = true;
        this.revokedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public LocalDateTime getExpiryDate() {
        return expiryDate != null ? expiryDate : expiresAt;
    }
    
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
        this.expiresAt = expiryDate; // 互換性のため
    }
    
    public String getDeviceInfo() {
        return deviceInfo;
    }
    
    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public LocalDateTime getIssuedAt() {
        return issuedAt != null ? issuedAt : createdAt;
    }
    
    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
    
    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }
    
    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getRevokedReason() {
        return revokedReason;
    }
    
    public void setRevokedReason(String revokedReason) {
        this.revokedReason = revokedReason;
    }
}