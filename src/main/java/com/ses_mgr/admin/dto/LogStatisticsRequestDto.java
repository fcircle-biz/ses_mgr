package com.ses_mgr.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ログ統計情報リクエストデータ転送オブジェクト
 * Log statistics request data transfer object
 */
public class LogStatisticsRequestDto {

    @NotNull(message = "開始日時は必須です")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime from;

    @NotNull(message = "終了日時は必須です")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime to;

    private List<String> types;

    @Pattern(regexp = "^(hour|day|week|month)$", message = "集計間隔は hour, day, week, month のいずれかを指定してください")
    private String interval = "day";

    private List<String> groupBy = List.of("type", "level");

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

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public List<String> getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(List<String> groupBy) {
        this.groupBy = groupBy;
    }
}