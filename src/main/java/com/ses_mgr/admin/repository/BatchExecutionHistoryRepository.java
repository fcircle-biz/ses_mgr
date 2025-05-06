package com.ses_mgr.admin.repository;

import com.ses_mgr.admin.entity.BatchExecutionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatchExecutionHistoryRepository extends JpaRepository<BatchExecutionHistory, String> {

    Optional<BatchExecutionHistory> findByExecutionId(String executionId);

    List<BatchExecutionHistory> findByJobId(String jobId);

    Page<BatchExecutionHistory> findByJobId(String jobId, Pageable pageable);

    List<BatchExecutionHistory> findByScheduleId(String scheduleId);

    Page<BatchExecutionHistory> findByScheduleId(String scheduleId, Pageable pageable);

    List<BatchExecutionHistory> findByStatus(String status);

    Page<BatchExecutionHistory> findByStatus(String status, Pageable pageable);

    List<BatchExecutionHistory> findByStartTimeBetween(LocalDateTime from, LocalDateTime to);

    Page<BatchExecutionHistory> findByStartTimeBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    List<BatchExecutionHistory> findByExecutedBy(String executedBy);

    Page<BatchExecutionHistory> findByExecutedBy(String executedBy, Pageable pageable);

    @Query("SELECT beh FROM BatchExecutionHistory beh WHERE " +
            "(:jobId IS NULL OR beh.jobId = :jobId) AND " +
            "(:status IS NULL OR beh.status = :status) AND " +
            "(:from IS NULL OR beh.startTime >= :from) AND " +
            "(:to IS NULL OR beh.startTime <= :to) AND " +
            "(:executedBy IS NULL OR beh.executedBy = :executedBy)")
    Page<BatchExecutionHistory> searchBatchHistory(
            @Param("jobId") String jobId,
            @Param("status") String status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("executedBy") String executedBy,
            Pageable pageable);

    List<BatchExecutionHistory> findTop10ByJobIdOrderByStartTimeDesc(String jobId);

    @Query("SELECT beh FROM BatchExecutionHistory beh WHERE beh.jobId = :jobId AND beh.status = 'running'")
    List<BatchExecutionHistory> findRunningJobsById(@Param("jobId") String jobId);
}