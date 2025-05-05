package com.ses_mgr.common.repository;

import com.ses_mgr.common.entity.UserRoleHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleHistoryRepository extends JpaRepository<UserRoleHistory, UUID> {
    
    List<UserRoleHistory> findByUserUserId(UUID userId);
    
    List<UserRoleHistory> findByRoleRoleId(UUID roleId);
    
    List<UserRoleHistory> findByPerformedByUserId(UUID performedById);
    
    List<UserRoleHistory> findByOperationType(String operationType);
    
    @Query("SELECT urh FROM UserRoleHistory urh WHERE urh.performedAt BETWEEN :startDate AND :endDate")
    List<UserRoleHistory> findByDateRange(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT urh FROM UserRoleHistory urh WHERE urh.user.userId = :userId ORDER BY urh.performedAt DESC")
    Page<UserRoleHistory> findUserHistory(@Param("userId") UUID userId, Pageable pageable);
    
    @Query("SELECT urh FROM UserRoleHistory urh WHERE urh.role.roleId = :roleId ORDER BY urh.performedAt DESC")
    Page<UserRoleHistory> findRoleHistory(@Param("roleId") UUID roleId, Pageable pageable);
}