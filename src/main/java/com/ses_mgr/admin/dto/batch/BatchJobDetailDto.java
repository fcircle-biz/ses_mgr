package com.ses_mgr.admin.dto.batch;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchJobDetailDto {
    
    private String id;
    private String name;
    private String description;
    private String category;
    private String jobType;
    private String status;
    private String cronExpression;
    private Map<String, Object> parameters;
    private ExecutionDetail lastRun;
    private List<ExecutionHistoryItem> executionHistory;
    private String nextRun;
    private List<ScheduleInfo> schedules;
    private List<DependencyInfo> dependencies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ExecutionDetail {
        private String status;
        private String startTime;
        private String endTime;
        private Integer executionTime;  // 秒
        private Integer recordsProcessed;
        private String message;
        private List<String> logs;
        private String errorMessage;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ExecutionHistoryItem {
        private String executionId;
        private String status;
        private String startTime;
        private String endTime;
        private Integer executionTime;  // 秒
        private Integer recordsProcessed;
        private String errorMessage;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ScheduleInfo {
        private String id;
        private String cronExpression;
        private String description;
        private Boolean isActive;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DependencyInfo {
        private String jobId;
        private String jobName;
        private String type;
    }
}