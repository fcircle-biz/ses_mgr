package com.ses_mgr.common.service.simplified;

import com.ses_mgr.common.dto.UserCreateRequestDto;
import com.ses_mgr.common.dto.UserResponseDto;
import com.ses_mgr.common.entity.Department;
import com.ses_mgr.common.entity.Role;
import com.ses_mgr.common.repository.DepartmentRepository;
import com.ses_mgr.common.repository.RoleRepository;
import com.ses_mgr.common.repository.UserRepository;
import com.ses_mgr.common.service.UserManagementService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SimplifiedUserManagementServiceIntegrationTest {

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Department testDepartment;
    private Role userRole;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        // 部署の作成
        testDepartment = new Department();
        testDepartment.setDepartmentName("Test Department");
        testDepartment.setDepartmentCode("TST");
        testDepartment.setIsActive(true);
        testDepartment = departmentRepository.save(testDepartment);

        // ロールの作成
        userRole = new Role();
        userRole.setRoleCode("USER");
        userRole.setName("一般ユーザー");
        userRole.setRoleType("business");
        userRole = roleRepository.save(userRole);

        // テスト用ユーザーを作成
        UserCreateRequestDto createUserDto = new UserCreateRequestDto();
        createUserDto.setLoginId("test.user");
        createUserDto.setEmail("test.user@example.com");
        createUserDto.setName("Test User");
        createUserDto.setPassword("Password123");
        createUserDto.setDepartment("Test Department");
        createUserDto.setRole("USER");

        UserResponseDto createdUser = userManagementService.createUser(createUserDto);
        testUserId = createdUser.getId();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // When
        UserResponseDto user = userManagementService.getUserById(testUserId);

        // Then
        assertNotNull(user);
        assertEquals(testUserId, user.getId());
        assertEquals("test.user@example.com", user.getEmail());
        assertEquals("Test User", user.getName());
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> userManagementService.getUserById(nonExistentId));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // When
        List<UserResponseDto> users = userManagementService.getAllUsers();

        // Then
        assertFalse(users.isEmpty());
        assertTrue(users.stream().anyMatch(user -> user.getEmail().equals("test.user@example.com")));
    }

    @Test
    void createUser_WithValidData_ShouldCreateNewUser() {
        // Given
        UserCreateRequestDto createRequestDto = new UserCreateRequestDto();
        createRequestDto.setLoginId("new.user");
        createRequestDto.setEmail("new.user@example.com");
        createRequestDto.setName("New User");
        createRequestDto.setPassword("Password123");
        createRequestDto.setDepartment("Test Department");
        createRequestDto.setRole("USER");

        // When
        UserResponseDto createdUser = userManagementService.createUser(createRequestDto);

        // Then
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("new.user@example.com", createdUser.getEmail());
        assertEquals("New User", createdUser.getName());
        assertEquals("USER", createdUser.getRole());

        // データベースに正しく保存されていることを確認
        assertNotNull(userRepository.findById(createdUser.getId()).orElse(null));
    }
}