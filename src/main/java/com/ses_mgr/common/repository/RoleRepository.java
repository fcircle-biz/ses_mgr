package com.ses_mgr.common.repository;

import com.ses_mgr.common.entity.Role;
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
public interface RoleRepository extends JpaRepository<Role, UUID> {

    // 基本的なクエリメソッド
    Optional<Role> findByRoleCode(String roleCode);
    
    boolean existsByRoleCode(String roleCode);
    
    List<Role> findByRoleType(String roleType);
    
    // 名前またはコードによる検索
    @Query("SELECT r FROM Role r WHERE " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.roleCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Role> searchByNameOrCode(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // 特定の権限を持つロールを取得
    @Query("SELECT r FROM Role r JOIN r.rolePermissions rp JOIN rp.permission p " +
           "WHERE p.permissionCode = :permissionCode")
    List<Role> findByPermissionCode(@Param("permissionCode") String permissionCode);
    
    // 特定のユーザーが持つロールを取得
    @Query("SELECT r FROM Role r JOIN r.userRoles ur JOIN ur.user u " +
           "WHERE u.userId = :userId")
    List<Role> findByUserId(@Param("userId") UUID userId);
    
    // 特定の権限レベルを持つロールを取得
    @Query("SELECT r FROM Role r JOIN r.rolePermissions rp JOIN rp.permission p " +
           "WHERE p.permissionCode = :permissionCode AND rp.accessLevel = :accessLevel")
    List<Role> findByPermissionCodeAndAccessLevel(
            @Param("permissionCode") String permissionCode,
            @Param("accessLevel") String accessLevel);
}