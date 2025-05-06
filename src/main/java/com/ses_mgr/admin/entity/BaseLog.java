package com.ses_mgr.admin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 全ログタイプの基底エンティティクラス
 * Base entity class for all log types
 */
@MappedSuperclass
public abstract class BaseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ログタイプ（system, audit, error, access）
     * Log type (system, audit, error, access)
     */
    @Column(name = "log_type", nullable = false, length = 20)
    private String logType;

    /**
     * ログレベル（info, warning, error, critical）
     * Log level (info, warning, error, critical)
     */
    @Column(name = "level", nullable = false, length = 10)
    private String level;

    /**
     * ログメッセージ
     * Log message
     */
    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    /**
     * イベント発生時間
     * Event timestamp
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * ログ記録時間
     * Log creation timestamp
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 作成前にタイムスタンプを設定
     * Set timestamps before creation
     */
    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        createdAt = LocalDateTime.now();
    }
}