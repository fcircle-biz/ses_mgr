package com.ses_mgr.admin.dto.batch;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchScheduleDto {
    
    private String id;
    private String jobId;
    private String jobName;
    private String description;
    private String cronExpression;
    private String type;
    private Boolean isActive;
    private String nextRun;
    private Map<String, Object> parameters;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}