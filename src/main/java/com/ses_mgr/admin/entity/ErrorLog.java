package com.ses_mgr.admin.entity;

import com.ses_mgr.admin.util.JsonbConverter;
import jakarta.persistence.*;
import java.util.Map;

/**
 * エラーログエンティティクラス
 * Error log entity representing system errors and exceptions
 */
@Entity
@Table(name = "error_log")
public class ErrorLog extends BaseLog {

    /**
     * 詳細情報（JSON形式）
     * Detailed information in JSON format
     */
    @Column(name = "details")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> details;

    /**
     * モジュール名
     * Module name
     */
    @Column(name = "module", length = 100)
    private String module;

    /**
     * 関数名
     * Function name
     */
    @Column(name = "function", length = 100)
    private String function;

    /**
     * エラーコード
     * Error code
     */
    @Column(name = "error_code", length = 50)
    private String errorCode;

    /**
     * エラーメッセージ
     * Error message
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * スタックトレース
     * Stack trace
     */
    @Column(name = "stack_trace", columnDefinition = "text")
    private String stackTrace;

    // Constructors

    public ErrorLog() {
        this.setLogType("error");
    }

    // Getters and Setters

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
}