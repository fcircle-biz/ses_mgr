package com.ses_mgr.admin.dto;

import jakarta.validation.constraints.Size;

/**
 * システムログデータ転送オブジェクト
 * System log data transfer object
 */
public class SystemLogDto extends BaseLogDto {

    @Size(max = 100, message = "モジュール名は100文字以内で入力してください")
    private String module;

    @Size(max = 100, message = "関数名は100文字以内で入力してください")
    private String function;

    @Size(max = 100, message = "サーバー名は100文字以内で入力してください")
    private String server;

    @Size(max = 100, message = "トレースIDは100文字以内で入力してください")
    private String traceId;

    public SystemLogDto() {
        setType("system");
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

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}