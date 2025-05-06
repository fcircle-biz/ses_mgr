package com.ses_mgr.admin.dto;

import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * 監査ログデータ転送オブジェクト
 * Audit log data transfer object
 */
public class AuditLogDto extends BaseLogDto {

    private UUID userId;

    @Size(max = 255, message = "ユーザー名は255文字以内で入力してください")
    private String username;

    @Size(max = 50, message = "IPアドレスは50文字以内で入力してください")
    private String ipAddress;

    @Size(max = 500, message = "ユーザーエージェントは500文字以内で入力してください")
    private String userAgent;

    @Size(max = 50, message = "リソースタイプは50文字以内で入力してください")
    private String resourceType;

    @Size(max = 50, message = "リソースIDは50文字以内で入力してください")
    private String resourceId;

    @Size(max = 20, message = "アクションは20文字以内で入力してください")
    private String action;

    public AuditLogDto() {
        setType("audit");
    }

    // Getters and Setters

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