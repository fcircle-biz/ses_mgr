package com.ses_mgr.admin.dto.batch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobExecuteRequestDto {
    
    private Map<String, Object> parameters;  // 実行時パラメータの上書き（オプション）
    
    @Builder.Default
    private Boolean async = true;  // 非同期実行するかどうか（デフォルト: true）
    
    private String description;  // 実行理由などの説明（オプション）
    
    private Boolean notifyOnCompletion;  // 完了時に通知するかどうか（オプション）
    
    private String notificationEmail;  // 通知先メールアドレス（オプション）
}