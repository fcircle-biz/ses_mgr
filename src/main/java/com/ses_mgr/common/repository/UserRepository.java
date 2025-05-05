package com.ses_mgr.common.repository;

import com.ses_mgr.common.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    // 基本的なクエリメソッド
    Optional<User> findByLoginId(String loginId);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByLoginId(String loginId);
    
    boolean existsByEmail(String email);
    
    // ステータスによる検索
    List<User> findByStatus(String status);
    
    Page<User> findByStatus(String status, Pageable pageable);
    
    // 部署によるフィルタリング
    @Query("SELECT u FROM User u WHERE u.department.departmentId = :departmentId")
    Page<User> findByDepartmentId(@Param("departmentId") Integer departmentId, Pageable pageable);
    
    // キーワード検索（ユーザー名、メールアドレス、ログインIDで検索）
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.loginId) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // 複合条件検索
    @Query("SELECT u FROM User u JOIN u.department d JOIN u.userRoles ur JOIN ur.role r " +
           "WHERE (:keyword IS NULL OR " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.loginId) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND (:departmentId IS NULL OR d.departmentId = :departmentId) " +
           "AND (:roleCode IS NULL OR r.roleCode = :roleCode) " + 
           "AND (:fromDate IS NULL OR u.createdAt >= :fromDate) " +
           "AND (:toDate IS NULL OR u.createdAt <= :toDate)")
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("departmentId") Integer departmentId,
            @Param("roleCode") String roleCode,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);
    
    // ロールによるフィルタリング
    @Query("SELECT u FROM User u JOIN u.userRoles ur JOIN ur.role r WHERE r.roleCode = :roleCode")
    Page<User> findByRoleCode(@Param("roleCode") String roleCode, Pageable pageable);
    
    // ステータス一括更新
    @Modifying
    @Query("UPDATE User u SET u.status = :status, u.updatedAt = CURRENT_TIMESTAMP WHERE u.userId IN :userIds")
    int updateStatusForUsers(@Param("userIds") List<UUID> userIds, @Param("status") String status);
    
    // ログイン試行回数のリセット
    @Modifying
    @Query("UPDATE User u SET u.loginAttempts = 0, u.status = 'active', u.updatedAt = CURRENT_TIMESTAMP WHERE u.userId IN :userIds AND u.status = 'locked'")
    int unlockUsers(@Param("userIds") List<UUID> userIds);
    
    // ログイン時間の更新
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = CURRENT_TIMESTAMP, u.updatedAt = CURRENT_TIMESTAMP WHERE u.userId = :userId")
    void updateLastLoginTime(@Param("userId") UUID userId);
    
    // ログイン試行回数の増加
    @Modifying
    @Query("UPDATE User u SET u.loginAttempts = u.loginAttempts + 1, u.updatedAt = CURRENT_TIMESTAMP WHERE u.loginId = :loginId")
    void incrementLoginAttempts(@Param("loginId") String loginId);
    
    // ログイン試行回数が指定数以上のユーザーをロック
    @Modifying
    @Query("UPDATE User u SET u.status = 'locked', u.updatedAt = CURRENT_TIMESTAMP WHERE u.loginAttempts >= :maxAttempts AND u.status = 'active'")
    int lockUsersWithExcessiveAttempts(@Param("maxAttempts") int maxAttempts);
}