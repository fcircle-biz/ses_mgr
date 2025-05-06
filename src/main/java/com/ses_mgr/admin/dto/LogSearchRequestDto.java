package com.ses_mgr.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ログ検索リクエストデータ転送オブジェクト
 * Log search request data transfer object
 */
public class LogSearchRequestDto {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime from;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime to;

    @Size(max = 200, message = "キーワードは200文字以内で入力してください")
    private String search;

    private List<String> types;

    private List<String> levels;

    private UUID userId;

    private String username;

    @Size(max = 100, message = "モジュール名は100文字以内で入力してください")
    private String module;

    @Size(max = 20, message = "アクションは20文字以内で入力してください")
    private String action;

    @Size(max = 20, message = "ステータスは20文字以内で入力してください")
    private String status;

    @Size(max = 50, message = "IPアドレスは50文字以内で入力してください")
    private String ipAddress;

    @Size(max = 50, message = "リソースタイプは50文字以内で入力してください")
    private String resourceType;

    @Size(max = 50, message = "リソースIDは50文字以内で入力してください")
    private String resourceId;

    @Size(max = 50, message = "エラーコードは50文字以内で入力してください")
    private String errorCode;

    @Min(value = 0, message = "ページ番号は0以上で入力してください")
    private Integer page = 0;

    @Min(value = 1, message = "ページサイズは1以上で入力してください")
    @Max(value = 200, message = "ページサイズは200以下で入力してください")
    private Integer size = 50;

    private String sort = "timestamp,desc";

    // Getters and Setters

    public LocalDateTime getFrom() {
        return from;
    }

    public void setFrom(LocalDateTime from) {
        this.from = from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public void setTo(LocalDateTime to) {
        this.to = to;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public List<String> getLevels() {
        return levels;
    }

    public void setLevels(List<String> levels) {
        this.levels = levels;
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

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
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

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}