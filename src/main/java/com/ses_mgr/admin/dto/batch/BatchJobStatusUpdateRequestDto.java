package com.ses_mgr.admin.dto.batch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobStatusUpdateRequestDto {
    
    @NotBlank(message = "ステータスは必須です")
    @Pattern(regexp = "active|inactive", message = "ステータスはactive または inactiveを指定してください")
    private String status;
    
    private String reason;  // ステータス変更理由（オプション）
}