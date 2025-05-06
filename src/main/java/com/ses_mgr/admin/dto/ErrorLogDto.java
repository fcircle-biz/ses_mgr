package com.ses_mgr.admin.dto;

import jakarta.validation.constraints.Size;

/**
 * エラーログデータ転送オブジェクト
 * Error log data transfer object
 */
public class ErrorLogDto extends BaseLogDto {

    @Size(max = 100, message = "モジュール名は100文字以内で入力してください")
    private String module;

    @Size(max = 100, message = "関数名は100文字以内で入力してください")
    private String function;

    @Size(max = 50, message = "エラーコードは50文字以内で入力してください")
    private String errorCode;

    @Size(max = 1000, message = "エラーメッセージは1000文字以内で入力してください")
    private String errorMessage;

    private String stackTrace;

    public ErrorLogDto() {
        setType("error");
    }

    // Getters and Setters

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