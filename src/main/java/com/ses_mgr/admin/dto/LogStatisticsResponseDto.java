package com.ses_mgr.admin.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ログ統計情報レスポンスデータ転送オブジェクト
 * Log statistics response data transfer object
 */
public class LogStatisticsResponseDto {

    private Map<String, Object> summary;
    private List<TimelineEntry> timeline;
    private List<Map<String, Object>> topUsers;
    private List<Map<String, Object>> topErrors;

    // Getters and Setters

    public Map<String, Object> getSummary() {
        return summary;
    }

    public void setSummary(Map<String, Object> summary) {
        this.summary = summary;
    }

    public List<TimelineEntry> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<TimelineEntry> timeline) {
        this.timeline = timeline;
    }

    public List<Map<String, Object>> getTopUsers() {
        return topUsers;
    }

    public void setTopUsers(List<Map<String, Object>> topUsers) {
        this.topUsers = topUsers;
    }

    public List<Map<String, Object>> getTopErrors() {
        return topErrors;
    }

    public void setTopErrors(List<Map<String, Object>> topErrors) {
        this.topErrors = topErrors;
    }

    /**
     * タイムラインエントリー内部クラス
     * Timeline entry inner class
     */
    public static class TimelineEntry {
        private String interval;
        private Map<String, Object> counts;

        public String getInterval() {
            return interval;
        }

        public void setInterval(String interval) {
            this.interval = interval;
        }

        public Map<String, Object> getCounts() {
            return counts;
        }

        public void setCounts(Map<String, Object> counts) {
            this.counts = counts;
        }
    }
}