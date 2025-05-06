package com.ses_mgr.admin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "batch_job_dependency")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dependency_id")
    private Long dependencyId;

    @Column(name = "job_id", nullable = false)
    private String jobId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", insertable = false, updatable = false)
    private BatchJob batchJob;

    @Column(name = "dependent_job_id", nullable = false)
    private String dependentJobId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependent_job_id", insertable = false, updatable = false)
    private BatchJob dependentBatchJob;

    @Column(name = "dependency_type", nullable = false)
    private String dependencyType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}