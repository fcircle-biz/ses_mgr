package com.ses_mgr.admin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "batch_schedule")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchSchedule {

    @Id
    @Column(name = "schedule_id")
    private String scheduleId;

    @Column(name = "job_id", nullable = false)
    private String jobId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", insertable = false, updatable = false)
    private BatchJob batchJob;

    @Column(name = "description")
    private String description;

    @Column(name = "cron_expression", nullable = false)
    private String cronExpression;

    @Column(name = "schedule_type", nullable = false)
    private String scheduleType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "parameters", columnDefinition = "JSONB")
    private String parameters;  // JSON形式のパラメータ

    @Column(name = "next_run")
    private LocalDateTime nextRun;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", columnDefinition = "BINARY(16)")
    private UUID createdBy;

    @Column(name = "updated_by", columnDefinition = "BINARY(16)")
    private UUID updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}