package com.ses_mgr.admin.dto.batch;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchJobExecuteResponseDto {
    
    private String message;
    private String executionId;
    private String jobId;
    private String jobName;
    private String startTime;
    private String status;
    private Boolean async;
    private String statusUrl;  // ステータス確認用URL
}