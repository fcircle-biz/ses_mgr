package com.ses_mgr.common.service.minimaltest;

import com.ses_mgr.common.dto.UserResponseDto;
import com.ses_mgr.common.entity.Department;
import com.ses_mgr.common.entity.Role;
import com.ses_mgr.common.entity.User;
import com.ses_mgr.common.repository.DepartmentRepository;
import com.ses_mgr.common.repository.RoleRepository;
import com.ses_mgr.common.repository.UserRepository;
import com.ses_mgr.common.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * リポジトリをモック化して最小限のテストを実行するテストケース
 */
@SpringBootTest
@ActiveProfiles("test")
public class MinimalUserServiceWithMocksTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DepartmentRepository departmentRepository;

    @MockBean
    private RoleRepository roleRepository;

    private UUID testUserId;
    private User testUser;
    private Department testDepartment;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testDepartment = createTestDepartment();
        testRole = createTestRole();
        testUser = createTestUser(testDepartment);
        
        // モックの設定
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));
    }

    @Test
    void getUserById_ShouldReturnUser() {
        // When
        UserResponseDto result = userService.getUserById(testUserId);
        
        // Then
        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals("test.user@example.com", result.getEmail());
        assertEquals("Test User", result.getName());
    }
    
    @Test
    void getAllUsers_ShouldReturnUsersList() {
        // When
        List<UserResponseDto> results = userService.getAllUsers();
        
        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testUserId, results.get(0).getId());
    }
    
    // ユーティリティメソッド
    private Department createTestDepartment() {
        Department department = new Department();
        department.setDepartmentId(1);
        department.setDepartmentName("Test Department");
        department.setDepartmentCode("TST");
        department.setIsActive(true);
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());
        // 循環参照を避けるため親部署と子部署は設定しない
        return department;
    }
    
    private Role createTestRole() {
        Role role = new Role();
        role.setRoleId(UUID.randomUUID());
        role.setRoleCode("USER");
        role.setName("一般ユーザー");
        role.setDescription("テストロール");
        role.setRoleType("business");
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        return role;
    }
    
    private User createTestUser(Department department) {
        User user = new User();
        user.setUserId(testUserId);
        user.setLoginId("test.login");
        user.setEmail("test.user@example.com");
        user.setName("Test User");
        user.setPasswordHash("encodedPassword");
        user.setStatus("active");
        user.setMfaEnabled(false);
        user.setLoginAttempts(0);
        user.setDepartment(department);  // 部署を設定
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setUserRoles(Collections.emptySet());  // 空のロールセットを設定
        return user;
    }
}