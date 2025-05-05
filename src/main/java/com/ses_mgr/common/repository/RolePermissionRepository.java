package com.ses_mgr.common.repository;

import com.ses_mgr.common.entity.RolePermission;
import com.ses_mgr.common.entity.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

    // 特定のロールに関連付けられた権限関連を取得
    List<RolePermission> findByRoleRoleId(UUID roleId);
    
    // 特定の権限に関連付けられたロール関連を取得
    List<RolePermission> findByPermissionPermissionId(UUID permissionId);
    
    // 特定のロールと権限の関連を確認
    boolean existsByRoleRoleIdAndPermissionPermissionId(UUID roleId, UUID permissionId);
    
    // 特定のアクセスレベルを持つロール権限関連を取得
    List<RolePermission> findByAccessLevel(String accessLevel);
    
    // 特定のロールと権限のアクセスレベルを取得
    @Query("SELECT rp.accessLevel FROM RolePermission rp " +
           "WHERE rp.role.roleId = :roleId AND rp.permission.permissionId = :permissionId")
    String findAccessLevel(@Param("roleId") UUID roleId, @Param("permissionId") UUID permissionId);
    
    // 特定のロールに関連付けられた権限関連を削除
    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.role.roleId = :roleId")
    int deleteAllByRoleId(@Param("roleId") UUID roleId);
    
    // 特定の権限に関連付けられたロール関連を削除
    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.permission.permissionId = :permissionId")
    int deleteAllByPermissionId(@Param("permissionId") UUID permissionId);
    
    // アクセスレベルを更新
    @Modifying
    @Query("UPDATE RolePermission rp SET rp.accessLevel = :accessLevel, rp.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE rp.role.roleId = :roleId AND rp.permission.permissionId = :permissionId")
    int updateAccessLevel(@Param("roleId") UUID roleId, @Param("permissionId") UUID permissionId, @Param("accessLevel") String accessLevel);
    
    // 特定のロールコードと権限コードの関連を取得
    @Query("SELECT rp FROM RolePermission rp " +
           "JOIN rp.role r JOIN rp.permission p " +
           "WHERE r.roleCode = :roleCode AND p.permissionCode = :permissionCode")
    List<RolePermission> findByRoleCodeAndPermissionCode(@Param("roleCode") String roleCode, @Param("permissionCode") String permissionCode);
}