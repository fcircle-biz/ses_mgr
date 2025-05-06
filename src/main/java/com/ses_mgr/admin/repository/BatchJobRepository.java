package com.ses_mgr.admin.repository;

import com.ses_mgr.admin.entity.BatchJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchJobRepository extends JpaRepository<BatchJob, String> {

    Optional<BatchJob> findByJobId(String jobId);

    Page<BatchJob> findByStatus(String status, Pageable pageable);

    Page<BatchJob> findByCategory(String category, Pageable pageable);

    @Query("SELECT bj FROM BatchJob bj WHERE " +
            "(:status IS NULL OR bj.status = :status) AND " +
            "(:category IS NULL OR bj.category = :category) AND " +
            "(:search IS NULL OR LOWER(bj.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(bj.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<BatchJob> searchBatchJobs(
            @Param("status") String status,
            @Param("category") String category,
            @Param("search") String search,
            Pageable pageable);

    List<BatchJob> findByJobTypeAndStatus(String jobType, String status);
}