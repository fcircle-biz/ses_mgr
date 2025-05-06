package com.ses_mgr.admin.dto.batch;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchHistoryItemDto {
    
    private String executionId;
    private String jobId;
    private String jobName;
    private String status;
    private String startTime;
    private String endTime;
    private Integer executionTime;  // 秒
    private String executedBy;  // user:ユーザーID または scheduler
    private String triggerType;  // scheduled または manual
    private Integer recordsProcessed;
    private String errorMessage;
    private Map<String, Object> parameters;
    private String description;
}