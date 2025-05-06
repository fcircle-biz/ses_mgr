package com.ses_mgr.admin.entity;

import com.ses_mgr.admin.util.JsonbConverter;
import jakarta.persistence.*;
import java.util.Map;
import java.util.UUID;

/**
 * 監査ログエンティティクラス
 * Audit log entity representing user activity logs
 */
@Entity
@Table(name = "audit_log")
public class AuditLog extends BaseLog {

    /**
     * 詳細情報（JSON形式）
     * Detailed information in JSON format
     */
    @Column(name = "details")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> details;

    /**
     * ユーザーID
     * User ID
     */
    @Column(name = "user_id")
    private UUID userId;

    /**
     * ユーザー名
     * Username
     */
    @Column(name = "username", length = 255)
    private String username;

    /**
     * IPアドレス
     * IP address
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * ユーザーエージェント
     * User agent
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * リソースタイプ（user, role, project, engineer など）
     * Resource type (user, role, project, engineer, etc.)
     */
    @Column(name = "resource_type", length = 50)
    private String resourceType;

    /**
     * リソースID
     * Resource ID
     */
    @Column(name = "resource_id", length = 50)
    private String resourceId;

    /**
     * アクション（create, read, update, delete）
     * Action (create, read, update, delete)
     */
    @Column(name = "action", length = 20)
    private String action;

    // Constructors

    public AuditLog() {
        this.setLogType("audit");
    }

    // Getters and Setters

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}