package com.ses_mgr.common.repository;

import com.ses_mgr.common.entity.UserRole;
import com.ses_mgr.common.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    // 特定のユーザーのロール割り当てを取得
    List<UserRole> findByUserUserId(UUID userId);
    
    // 特定のロールに割り当てられたユーザーの関連を取得
    List<UserRole> findByRoleRoleId(UUID roleId);
    
    // 特定のユーザーとロールの関連を確認
    boolean existsByUserUserIdAndRoleRoleId(UUID userId, UUID roleId);
    
    // 有効期限切れのロール割り当てを取得
    @Query("SELECT ur FROM UserRole ur WHERE ur.expiresAt IS NOT NULL AND ur.expiresAt < CURRENT_TIMESTAMP")
    List<UserRole> findExpiredUserRoles();
    
    // 有効期限切れのロール割り当てを削除
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.expiresAt IS NOT NULL AND ur.expiresAt < CURRENT_TIMESTAMP")
    int deleteExpiredUserRoles();
    
    // 特定のユーザーのロール割り当てを削除
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user.userId = :userId")
    int deleteAllByUserId(@Param("userId") UUID userId);
    
    // 特定のロールの割り当てをすべて削除
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.role.roleId = :roleId")
    int deleteAllByRoleId(@Param("roleId") UUID roleId);
    
    // 特定の割り当て者によるロール割り当てを検索
    List<UserRole> findByAssignedByUserId(UUID assignedByUserId);
    
    // 有効期限を更新
    @Modifying
    @Query("UPDATE UserRole ur SET ur.expiresAt = :expiresAt, ur.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE ur.user.userId = :userId AND ur.role.roleId = :roleId")
    int updateExpiresAt(@Param("userId") UUID userId, @Param("roleId") UUID roleId, @Param("expiresAt") LocalDateTime expiresAt);
}