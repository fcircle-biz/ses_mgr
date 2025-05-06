package com.ses_mgr.admin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "batch_execution_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchExecutionHistory {

    @Id
    @Column(name = "execution_id")
    private String executionId;

    @Column(name = "job_id", nullable = false)
    private String jobId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", insertable = false, updatable = false)
    private BatchJob batchJob;

    @Column(name = "schedule_id")
    private String scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", insertable = false, updatable = false)
    private BatchSchedule batchSchedule;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "execution_time")
    private Integer executionTime;  // 実行時間（秒）

    @Column(name = "executed_by", nullable = false)
    private String executedBy;

    @Column(name = "trigger_type", nullable = false)
    private String triggerType;

    @Column(name = "records_processed")
    private Integer recordsProcessed;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "parameters", columnDefinition = "JSON")
    private String parameters;  // JSON形式のパラメータ

    @Column(name = "description")
    private String description;

    @Column(name = "logs", columnDefinition = "TEXT")
    private String logs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}