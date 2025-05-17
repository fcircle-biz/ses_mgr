package jp.co.example.sesapp.common.auth.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * パスワードリセットトークンのドメインモデル
 */
public class PasswordResetToken {
    private UUID id;
    private UUID userId;
    private String token;
    private LocalDateTime expiryDate;
    private boolean used;
    private LocalDateTime createdAt;

    /**
     * デフォルトコンストラクタ
     */
    public PasswordResetToken() {
    }

    /**
     * コンストラクタ
     *
     * @param id トークンID
     * @param userId ユーザーID
     * @param token トークン文字列
     * @param expiryDate 有効期限
     */
    public PasswordResetToken(UUID id, UUID userId, String token, LocalDateTime expiryDate) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiryDate = expiryDate;
        this.used = false;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 新しいパスワードリセットトークンを作成する
     *
     * @param userId ユーザーID
     * @param expiryMinutes 有効期限（分）
     * @return パスワードリセットトークン
     */
    public static PasswordResetToken createToken(UUID userId, int expiryMinutes) {
        UUID id = UUID.randomUUID();
        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(expiryMinutes);
        return new PasswordResetToken(id, userId, token, expiryDate);
    }

    /**
     * トークンが有効かどうかを確認する
     *
     * @return 有効な場合はtrue
     */
    public boolean isValid() {
        return !isExpired() && !used;
    }

    /**
     * トークンが期限切れかどうかを確認する
     *
     * @return 期限切れの場合はtrue
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * トークンを使用済みとしてマークする
     */
    public void markAsUsed() {
        this.used = true;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}