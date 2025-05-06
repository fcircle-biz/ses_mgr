package com.ses_mgr.admin.dto;

import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * アクセスログデータ転送オブジェクト
 * Access log data transfer object
 */
public class AccessLogDto extends BaseLogDto {

    private UUID userId;

    @Size(max = 255, message = "ユーザー名は255文字以内で入力してください")
    private String username;

    @Size(max = 20, message = "アクションは20文字以内で入力してください")
    private String action;

    @Size(max = 20, message = "ステータスは20文字以内で入力してください")
    private String status;

    @Size(max = 50, message = "IPアドレスは50文字以内で入力してください")
    private String ipAddress;

    @Size(max = 500, message = "ユーザーエージェントは500文字以内で入力してください")
    private String userAgent;

    @Size(max = 1000, message = "リクエストURIは1000文字以内で入力してください")
    private String requestUri;

    @Size(max = 10, message = "リクエストメソッドは10文字以内で入力してください")
    private String requestMethod;

    private Integer responseStatus;

    private Integer responseTime;

    public AccessLogDto() {
        setType("access");
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Integer getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Integer responseTime) {
        this.responseTime = responseTime;
    }
}