package com.ses_mgr.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 基本ログデータ転送オブジェクト
 * Base log data transfer object
 */
public class BaseLogDto {

    private Long id;

    @NotBlank(message = "ログタイプは必須です")
    @Size(max = 20, message = "ログタイプは20文字以内で入力してください")
    private String type;

    @NotBlank(message = "ログレベルは必須です")
    @Size(max = 10, message = "ログレベルは10文字以内で入力してください")
    private String level;

    @NotBlank(message = "メッセージは必須です")
    @Size(max = 1000, message = "メッセージは1000文字以内で入力してください")
    private String message;

    private Map<String, Object> details;

    @NotNull(message = "タイムスタンプは必須です")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}