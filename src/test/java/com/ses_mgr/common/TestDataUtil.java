package com.ses_mgr.common;

import com.ses_mgr.common.entity.Department;
import com.ses_mgr.common.entity.Role;
import com.ses_mgr.common.entity.User;
import com.ses_mgr.common.entity.UserRole;
import com.ses_mgr.common.entity.UserRoleId;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * テストデータを提供するユーティリティクラス
 */
public class TestDataUtil {

    /**
     * テスト用の標準UUIDを提供
     */
    public static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID TEST_ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final UUID TEST_ROLE_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    public static final UUID TEST_ROLE_ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000102");
    public static final Integer TEST_DEPARTMENT_ID = 1;

    /**
     * テスト用の通常ユーザーを作成
     */
    public static User createTestUser() {
        Department department = createTestDepartment();
        
        User user = User.builder()
                .userId(TEST_USER_ID)
                .loginId("testuser")
                .email("testuser@example.com")
                .name("Test User")
                .department(department)
                .position("Developer")
                .phone("123-456-7890")
                .passwordHash("$2a$10$testHashedPasswordForTesting")
                .status("active")
                .loginAttempts(0)
                .mfaEnabled(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // ユーザーロールを設定
        Set<UserRole> userRoles = new HashSet<>();
        UserRole userRole = new UserRole();
        userRole.setId(new UserRoleId(TEST_USER_ID, TEST_ROLE_USER_ID));
        userRole.setUser(user);
        userRole.setRole(createUserRole());
        userRole.setAssignedAt(LocalDateTime.now());
        userRoles.add(userRole);
        
        user.setUserRoles(userRoles);
        
        return user;
    }
    
    /**
     * テスト用の管理者ユーザーを作成
     */
    public static User createTestAdmin() {
        Department department = createTestDepartment();
        
        User admin = User.builder()
                .userId(TEST_ADMIN_ID)
                .loginId("admin")
                .email("admin@example.com")
                .name("Admin User")
                .department(department)
                .position("System Administrator")
                .phone("123-456-7890")
                .passwordHash("$2a$10$testHashedPasswordForTesting")
                .status("active")
                .loginAttempts(0)
                .mfaEnabled(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // 管理者ロールを設定
        Set<UserRole> userRoles = new HashSet<>();
        UserRole adminRole = new UserRole();
        adminRole.setId(new UserRoleId(TEST_ADMIN_ID, TEST_ROLE_ADMIN_ID));
        adminRole.setUser(admin);
        adminRole.setRole(createAdminRole());
        adminRole.setAssignedAt(LocalDateTime.now());
        userRoles.add(adminRole);
        
        admin.setUserRoles(userRoles);
        
        return admin;
    }
    
    /**
     * テスト用の部署を作成
     */
    public static Department createTestDepartment() {
        Department department = new Department();
        department.setDepartmentId(TEST_DEPARTMENT_ID);
        department.setDepartmentName("Test Department");
        department.setDepartmentCode("TEST");
        department.setDescription("Test department for testing");
        department.setIsActive(true);
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());
        return department;
    }
    
    /**
     * テスト用のユーザーロールを作成
     */
    public static Role createUserRole() {
        Role role = new Role();
        role.setRoleId(TEST_ROLE_USER_ID);
        role.setRoleCode("USER");
        role.setName("一般ユーザー");
        role.setDescription("標準的なユーザー権限を持つロール");
        role.setRoleType("system");
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        return role;
    }
    
    /**
     * テスト用の管理者ロールを作成
     */
    public static Role createAdminRole() {
        Role role = new Role();
        role.setRoleId(TEST_ROLE_ADMIN_ID);
        role.setRoleCode("ADMIN");
        role.setName("管理者");
        role.setDescription("システム管理者権限を持つロール");
        role.setRoleType("system");
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        return role;
    }
}