package com.ses_mgr.common.repository;

import com.ses_mgr.common.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    // 基本的なクエリメソッド
    Optional<Permission> findByPermissionCode(String permissionCode);
    
    boolean existsByPermissionCode(String permissionCode);
    
    List<Permission> findByResourceType(String resourceType);
    
    List<Permission> findByResourceName(String resourceName);
    
    List<Permission> findByAction(String action);
    
    List<Permission> findByResourceTypeAndResourceName(String resourceType, String resourceName);
    
    // 名前またはコードによる検索
    @Query("SELECT p FROM Permission p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.permissionCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Permission> searchByNameOrCode(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // 特定のロールに関連付けられた権限を取得
    @Query("SELECT p FROM Permission p JOIN p.rolePermissions rp JOIN rp.role r " +
           "WHERE r.roleId = :roleId")
    List<Permission> findByRoleId(@Param("roleId") UUID roleId);
    
    // 特定のユーザーが持つ全ての権限を取得
    @Query("SELECT DISTINCT p FROM Permission p " +
           "JOIN p.rolePermissions rp JOIN rp.role r " +
           "JOIN r.userRoles ur JOIN ur.user u " +
           "WHERE u.userId = :userId AND rp.accessLevel <> 'none'")
    List<Permission> findAllByUserId(@Param("userId") UUID userId);
    
    // 特定のユーザーが持つ書き込み権限を取得
    @Query("SELECT DISTINCT p FROM Permission p " +
           "JOIN p.rolePermissions rp JOIN rp.role r " +
           "JOIN r.userRoles ur JOIN ur.user u " +
           "WHERE u.userId = :userId AND rp.accessLevel = 'write'")
    List<Permission> findWritePermissionsByUserId(@Param("userId") UUID userId);
}