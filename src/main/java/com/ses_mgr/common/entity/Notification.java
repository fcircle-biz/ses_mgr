package com.ses_mgr.common.entity;

import com.ses_mgr.admin.util.JsonbConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 通知エンティティ
 * Notification entity
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    /**
     * 通知タイプ
     * Notification type
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;

    /**
     * 通知タイトル
     * Notification title
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 通知本文
     * Notification body
     */
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    /**
     * 既読フラグ
     * Read flag
     */
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    /**
     * メタデータ（JSON形式で保存）
     * Metadata (stored as JSON)
     */
    @Column(name = "metadata", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 送信者ID
     * Sender ID
     */
    @Column(name = "sender_id")
    private Long senderId;

    /**
     * 送信者名
     * Sender name
     */
    @Column(name = "sender_name", length = 100)
    private String senderName;

    /**
     * 受信者ID
     * Recipient ID
     */
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    /**
     * 受信者名
     * Recipient name
     */
    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    /**
     * 重要度（LOW, MEDIUM, HIGH）
     * Importance level
     */
    @Column(name = "importance", length = 10)
    private String importance;

    /**
     * アクションURL
     * Action URL
     */
    @Column(name = "action_url", length = 500)
    private String actionUrl;

    /**
     * 作成日時
     * Created date and time
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     * Updated date and time
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 削除日時
     * Deleted date and time (for soft delete)
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 初期値の設定
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    /**
     * メタデータから特定のキーの値を取得
     * Get value for a specific key from metadata
     */
    public Object getMetadataValue(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    /**
     * メタデータに値を設定
     * Set value for a specific key in metadata
     */
    public void setMetadataValue(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }
}