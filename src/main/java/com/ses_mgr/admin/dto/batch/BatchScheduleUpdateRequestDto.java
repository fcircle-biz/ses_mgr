package com.ses_mgr.admin.dto.batch;

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
public class BatchScheduleUpdateRequestDto {
    
    private String description;
    
    private String cronExpression;
    
    @Pattern(regexp = "daily|weekly|monthly", message = "スケジュールタイプはdaily, weekly, monthlyのいずれかを指定してください")
    private String type;
    
    private Boolean isActive;
    
    private Map<String, Object> parameters;
}