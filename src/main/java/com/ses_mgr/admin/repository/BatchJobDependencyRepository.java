package com.ses_mgr.admin.repository;

import com.ses_mgr.admin.entity.BatchJobDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchJobDependencyRepository extends JpaRepository<BatchJobDependency, Long> {

    List<BatchJobDependency> findByJobId(String jobId);

    List<BatchJobDependency> findByDependentJobId(String dependentJobId);

    void deleteByJobIdAndDependentJobId(String jobId, String dependentJobId);
}