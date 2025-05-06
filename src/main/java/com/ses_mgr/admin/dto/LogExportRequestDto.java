package com.ses_mgr.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * ログエクスポートリクエストデータ転送オブジェクト
 * Log export request data transfer object
 */
public class LogExportRequestDto {

    @NotNull(message = "開始日時は必須です")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime from;

    @NotNull(message = "終了日時は必須です")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime to;

    private List<String> types;
    
    private List<String> levels;

    @Pattern(regexp = "^(csv|excel)$", message = "ファイル形式は csv または excel を指定してください")
    private String format = "csv";

    @Pattern(regexp = "^(UTF-8|Shift_JIS)$", message = "エンコーディングは UTF-8 または Shift_JIS を指定してください")
    private String encoding = "UTF-8";

    private boolean includeDetails = true;
    
    private Set<String> fields;
    
    private LogSearchRequestDto filters;

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

    public List<String> getLevels() {
        return levels;
    }

    public void setLevels(List<String> levels) {
        this.levels = levels;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public boolean isIncludeDetails() {
        return includeDetails;
    }

    public void setIncludeDetails(boolean includeDetails) {
        this.includeDetails = includeDetails;
    }

    public Set<String> getFields() {
        return fields;
    }

    public void setFields(Set<String> fields) {
        this.fields = fields;
    }

    public LogSearchRequestDto getFilters() {
        return filters;
    }

    public void setFilters(LogSearchRequestDto filters) {
        this.filters = filters;
    }
}