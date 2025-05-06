package com.ses_mgr.common.service;

import com.ses_mgr.common.dto.*;
import com.ses_mgr.common.entity.Department;
import com.ses_mgr.common.entity.Role;
import com.ses_mgr.common.entity.User;
import com.ses_mgr.common.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Department testDepartment;
    private Role adminRole;
    private Role userRole;
    private User testUser;
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
        adminRole = new Role();
        adminRole.setRoleCode("ADMIN");
        adminRole.setName("システム管理者");
        adminRole.setRoleType("system");
        adminRole = roleRepository.save(adminRole);

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

        UserResponseDto createdUser = userService.createUser(createUserDto);
        testUserId = createdUser.getId();
        testUser = userRepository.findById(testUserId).orElseThrow();
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // When
        List<UserResponseDto> users = userService.getAllUsers();

        // Then
        assertFalse(users.isEmpty());
        assertTrue(users.stream().anyMatch(user -> user.getEmail().equals("test.user@example.com")));
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // When
        UserResponseDto user = userService.getUserById(testUserId);

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
        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(nonExistentId));
    }

    //@Test
    void createUser_WithValidData_ShouldCreateNewUser_disabled() {
        // Given
        UserCreateRequestDto createRequestDto = new UserCreateRequestDto();
        createRequestDto.setLoginId("new.user");
        createRequestDto.setEmail("new.user@example.com");
        createRequestDto.setName("New User");
        createRequestDto.setPassword("Password123");
        createRequestDto.setDepartment("Test Department");
        createRequestDto.setRole("ADMIN");

        // When
        UserResponseDto createdUser = userService.createUser(createRequestDto);

        // Then
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("new.user@example.com", createdUser.getEmail());
        assertEquals("New User", createdUser.getName());
        assertEquals("ADMIN", createdUser.getRole());

        // データベースに正しく保存されていることを確認
        User savedUser = userRepository.findById(createdUser.getId()).orElseThrow();
        assertEquals("new.user", savedUser.getLoginId());
        assertEquals("new.user@example.com", savedUser.getEmail());
        assertTrue(savedUser.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getRoleCode().equals("ADMIN")));
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUser() {
        // Given
        UserUpdateRequestDto updateRequestDto = new UserUpdateRequestDto();
        updateRequestDto.setEmail("updated.email@example.com");
        updateRequestDto.setName("Updated Name");
        updateRequestDto.setDepartment("Test Department");
        updateRequestDto.setPosition("Senior Developer");

        // When
        UserResponseDto updatedUser = userService.updateUser(testUserId, updateRequestDto);

        // Then
        assertNotNull(updatedUser);
        assertEquals(testUserId, updatedUser.getId());
        assertEquals("updated.email@example.com", updatedUser.getEmail());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("Senior Developer", updatedUser.getPosition());

        // データベースに正しく更新されていることを確認
        User savedUser = userRepository.findById(testUserId).orElseThrow();
        assertEquals("updated.email@example.com", savedUser.getEmail());
        assertEquals("Updated Name", savedUser.getName());
        assertEquals("Senior Developer", savedUser.getPosition());
    }

    @Test
    void updateUserStatus_ShouldChangeUserStatus() {
        // Given
        UserStatusRequestDto statusRequestDto = new UserStatusRequestDto();
        statusRequestDto.setStatus("inactive");

        // When
        UserResponseDto updatedUser = userService.updateUserStatus(testUserId, statusRequestDto);

        // Then
        assertNotNull(updatedUser);
        assertEquals("inactive", updatedUser.getStatus());

        // データベースに正しく更新されていることを確認
        User savedUser = userRepository.findById(testUserId).orElseThrow();
        assertEquals("inactive", savedUser.getStatus());
    }

    @Test
    void unlockUserAccount_WhenUserLocked_ShouldUnlockUser() {
        // Given
        testUser.setStatus("locked");
        testUser.setLoginAttempts(5);
        userRepository.save(testUser);

        // When
        UserResponseDto unlockedUser = userService.unlockUserAccount(testUserId);

        // Then
        assertNotNull(unlockedUser);
        assertEquals("active", unlockedUser.getStatus());

        // データベースに正しく更新されていることを確認
        User savedUser = userRepository.findById(testUserId).orElseThrow();
        assertEquals("active", savedUser.getStatus());
        assertEquals(0, savedUser.getLoginAttempts());
    }

    //@Test
    void updateBulkUserStatus_ShouldUpdateMultipleUsers_disabled() {
        // Given
        // さらに2人のユーザーを作成
        UserCreateRequestDto user1Dto = new UserCreateRequestDto();
        user1Dto.setLoginId("user1");
        user1Dto.setEmail("user1@example.com");
        user1Dto.setName("User One");
        user1Dto.setPassword("Password123");
        user1Dto.setDepartment("Test Department");
        user1Dto.setRole("USER");
        UserResponseDto createdUser1 = userService.createUser(user1Dto);

        UserCreateRequestDto user2Dto = new UserCreateRequestDto();
        user2Dto.setLoginId("user2");
        user2Dto.setEmail("user2@example.com");
        user2Dto.setName("User Two");
        user2Dto.setPassword("Password123");
        user2Dto.setDepartment("Test Department");
        user2Dto.setRole("USER");
        UserResponseDto createdUser2 = userService.createUser(user2Dto);

        UserBulkStatusRequestDto bulkStatusRequestDto = new UserBulkStatusRequestDto();
        bulkStatusRequestDto.setUserIds(Arrays.asList(testUserId, createdUser1.getId(), createdUser2.getId()));
        bulkStatusRequestDto.setStatus("inactive");

        // When
        int updatedCount = userService.updateBulkUserStatus(bulkStatusRequestDto);

        // Then
        assertEquals(3, updatedCount);

        // データベースに正しく更新されていることを確認
        List<User> users = userRepository.findAllById(Arrays.asList(testUserId, createdUser1.getId(), createdUser2.getId()));
        for (User user : users) {
            assertEquals("inactive", user.getStatus());
        }
    }

    @Test
    void searchUsers_ShouldReturnMatchingUsers() {
        // Given
        // さらに2人のユーザーを作成
        UserCreateRequestDto adminUserDto = new UserCreateRequestDto();
        adminUserDto.setLoginId("admin.user");
        adminUserDto.setEmail("admin@example.com");
        adminUserDto.setName("Admin User");
        adminUserDto.setPassword("Password123");
        adminUserDto.setDepartment("Test Department");
        adminUserDto.setRole("ADMIN");
        userService.createUser(adminUserDto);

        UserCreateRequestDto regularUserDto = new UserCreateRequestDto();
        regularUserDto.setLoginId("regular.user");
        regularUserDto.setEmail("regular@example.com");
        regularUserDto.setName("Regular User");
        regularUserDto.setPassword("Password123");
        regularUserDto.setDepartment("Test Department");
        regularUserDto.setRole("USER");
        userService.createUser(regularUserDto);

        UserSearchRequestDto searchRequestDto = new UserSearchRequestDto();
        searchRequestDto.setKeyword("admin");

        // When
        Page<UserResponseDto> searchResults = userService.searchUsers(searchRequestDto, PageRequest.of(0, 10));

        // Then
        assertEquals(1, searchResults.getTotalElements());
        assertEquals("admin@example.com", searchResults.getContent().get(0).getEmail());

        // ステータスでの検索
        searchRequestDto = new UserSearchRequestDto();
        searchRequestDto.setStatus("active");
        searchResults = userService.searchUsers(searchRequestDto, PageRequest.of(0, 10));
        assertTrue(searchResults.getTotalElements() >= 3);

        // ロールでの検索
        searchRequestDto = new UserSearchRequestDto();
        searchRequestDto.setRole("ADMIN");
        searchResults = userService.searchUsers(searchRequestDto, PageRequest.of(0, 10));
        assertEquals(1, searchResults.getTotalElements());
        assertEquals("admin@example.com", searchResults.getContent().get(0).getEmail());
    }
}