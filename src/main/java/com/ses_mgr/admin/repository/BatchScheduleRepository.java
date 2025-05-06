package com.ses_mgr.admin.repository;

import com.ses_mgr.admin.entity.BatchSchedule;
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
public interface BatchScheduleRepository extends JpaRepository<BatchSchedule, String> {

    Optional<BatchSchedule> findByScheduleId(String scheduleId);

    List<BatchSchedule> findByJobId(String jobId);

    Page<BatchSchedule> findByJobId(String jobId, Pageable pageable);

    List<BatchSchedule> findByIsActive(Boolean isActive);

    Page<BatchSchedule> findByIsActive(Boolean isActive, Pageable pageable);

    List<BatchSchedule> findByScheduleType(String scheduleType);

    Page<BatchSchedule> findByScheduleType(String scheduleType, Pageable pageable);

    @Query("SELECT bs FROM BatchSchedule bs WHERE " +
            "(:jobId IS NULL OR bs.jobId = :jobId) AND " +
            "(:isActive IS NULL OR bs.isActive = :isActive) AND " +
            "(:scheduleType IS NULL OR bs.scheduleType = :scheduleType)")
    Page<BatchSchedule> searchBatchSchedules(
            @Param("jobId") String jobId,
            @Param("isActive") Boolean isActive,
            @Param("scheduleType") String scheduleType,
            Pageable pageable);

    List<BatchSchedule> findByIsActiveTrueAndNextRunLessThanEqual(LocalDateTime dateTime);
}