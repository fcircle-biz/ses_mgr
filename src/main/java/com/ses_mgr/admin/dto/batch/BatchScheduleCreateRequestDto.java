package com.ses_mgr.admin.dto.batch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchScheduleCreateRequestDto {
    
    @NotBlank(message = "ジョブIDは必須です")
    private String jobId;
    
    private String description;
    
    @NotBlank(message = "cron式は必須です")
    private String cronExpression;
    
    @NotBlank(message = "スケジュールタイプは必須です")
    @Pattern(regexp = "daily|weekly|monthly", message = "スケジュールタイプはdaily, weekly, monthlyのいずれかを指定してください")
    private String type;
    
    @NotNull(message = "有効フラグは必須です")
    private Boolean isActive;
    
    private Map<String, Object> parameters;
}