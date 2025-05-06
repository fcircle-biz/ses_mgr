package com.ses_mgr.admin.entity;

import com.ses_mgr.admin.util.JsonbConverter;
import jakarta.persistence.*;
import java.util.Map;

/**
 * システムログエンティティクラス
 * System log entity representing system operation logs
 */
@Entity
@Table(name = "system_log")
public class SystemLog extends BaseLog {

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
     * サーバー名
     * Server name
     */
    @Column(name = "server", length = 100)
    private String server;

    /**
     * トレースID（分散トレーシング用）
     * Trace ID for distributed tracing
     */
    @Column(name = "trace_id", length = 100)
    private String traceId;

    // Constructors

    public SystemLog() {
        this.setLogType("system");
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