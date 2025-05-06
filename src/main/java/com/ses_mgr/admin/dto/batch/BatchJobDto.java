package com.ses_mgr.admin.dto.batch;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchJobDto {
    
    private String id;
    private String name;
    private String description;
    private String category;
    private String jobType;
    private String status;
    private Map<String, Object> parameters;
    private LastRunInfo lastRun;
    private String nextRun;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LastRunInfo {
        private String status;
        private String startTime;
        private String endTime;
        private Integer executionTime;  // 秒
        private String errorMessage;
    }
}