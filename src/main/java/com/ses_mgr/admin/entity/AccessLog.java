package com.ses_mgr.admin.entity;

import com.ses_mgr.admin.util.JsonbConverter;
import jakarta.persistence.*;
import java.util.Map;
import java.util.UUID;

/**
 * アクセスログエンティティクラス
 * Access log entity representing system access logs
 */
@Entity
@Table(name = "access_log")
public class AccessLog extends BaseLog {

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
     * アクション（login, logout, view, api_access）
     * Action (login, logout, view, api_access)
     */
    @Column(name = "action", length = 20)
    private String action;

    /**
     * ステータス（success, failure）
     * Status (success, failure)
     */
    @Column(name = "status", length = 20)
    private String status;

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
     * リクエストURI
     * Request URI
     */
    @Column(name = "request_uri", length = 1000)
    private String requestUri;

    /**
     * リクエストメソッド
     * Request method
     */
    @Column(name = "request_method", length = 10)
    private String requestMethod;

    /**
     * レスポンスステータス
     * Response status
     */
    @Column(name = "response_status")
    private Integer responseStatus;

    /**
     * レスポンス時間（ミリ秒）
     * Response time in milliseconds
     */
    @Column(name = "response_time")
    private Integer responseTime;

    // Constructors

    public AccessLog() {
        this.setLogType("access");
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