package com.ses_mgr.common.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ses_mgr.common.dto.*;
import com.ses_mgr.common.entity.Department;
import com.ses_mgr.common.entity.Role;
import com.ses_mgr.common.entity.User;
import com.ses_mgr.common.repository.DepartmentRepository;
import com.ses_mgr.common.repository.RoleRepository;
import com.ses_mgr.common.repository.UserRepository;
import com.ses_mgr.common.service.UserManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserManagementRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RoleRepository roleRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private UUID testUserId;
    private Department testDepartment;
    private Role adminRole;

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

        Role userRole = new Role();
        userRole.setRoleCode("USER");
        userRole.setName("一般ユーザー");
        userRole.setRoleType("business");
        roleRepository.save(userRole);

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
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnUsersList() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].email").exists())
                .andExpect(jsonPath("$.data[0].name").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchUsers_ShouldReturnMatchingUsers() throws Exception {
        // Given
        // 検索用にさらにユーザーを作成
        UserCreateRequestDto adminUserDto = new UserCreateRequestDto();
        adminUserDto.setLoginId("admin.user");
        adminUserDto.setEmail("admin@example.com");
        adminUserDto.setName("Admin User");
        adminUserDto.setPassword("Password123");
        adminUserDto.setDepartment("Test Department");
        adminUserDto.setRole("ADMIN");
        userManagementService.createUser(adminUserDto);

        UserSearchRequestDto searchRequestDto = new UserSearchRequestDto();
        searchRequestDto.setKeyword("admin");

        // When & Then
        mockMvc.perform(post("/api/v1/admin/users/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].email").value("admin@example.com"))
                .andExpect(jsonPath("$.data.content[0].name").value("Admin User"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/users/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.data.email").value("test.user@example.com"))
                .andExpect(jsonPath("$.data.name").value("Test User"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(get("/api/v1/admin/users/{userId}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_WithValidData_ShouldCreateAndReturnUser() throws Exception {
        // Given
        UserCreateRequestDto createRequestDto = new UserCreateRequestDto();
        createRequestDto.setLoginId("new.user");
        createRequestDto.setEmail("new.user@example.com");
        createRequestDto.setName("New User");
        createRequestDto.setPassword("Password123");
        createRequestDto.setDepartment("Test Department");
        createRequestDto.setRole("ADMIN");

        // When & Then
        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.email").value("new.user@example.com"))
                .andExpect(jsonPath("$.data.name").value("New User"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WithValidData_ShouldUpdateAndReturnUser() throws Exception {
        // Given
        UserUpdateRequestDto updateRequestDto = new UserUpdateRequestDto();
        updateRequestDto.setEmail("updated.email@example.com");
        updateRequestDto.setName("Updated Name");
        updateRequestDto.setPosition("Senior Developer");

        // When & Then
        mockMvc.perform(put("/api/v1/admin/users/{userId}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.data.email").value("updated.email@example.com"))
                .andExpect(jsonPath("$.data.name").value("Updated Name"))
                .andExpect(jsonPath("$.data.position").value("Senior Developer"));

        // データベースに反映されていることを確認
        User updatedUser = userRepository.findById(testUserId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("updated.email@example.com", updatedUser.getEmail());
        org.junit.jupiter.api.Assertions.assertEquals("Updated Name", updatedUser.getName());
        org.junit.jupiter.api.Assertions.assertEquals("Senior Developer", updatedUser.getPosition());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserStatus_ShouldUpdateAndReturnStatus() throws Exception {
        // Given
        UserStatusRequestDto statusRequestDto = new UserStatusRequestDto();
        statusRequestDto.setStatus("inactive");

        // When & Then
        mockMvc.perform(put("/api/v1/admin/users/{userId}/status", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.data.status").value("inactive"));

        // データベースに反映されていることを確認
        User updatedUser = userRepository.findById(testUserId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("inactive", updatedUser.getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetUserPassword_ShouldResetPasswordAndReturnConfirmation() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/admin/users/{userId}/reset-password", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("パスワードが正常にリセットされました"))
                .andExpect(jsonPath("$.data.id").value(testUserId.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unlockUserAccount_WhenUserLocked_ShouldUnlockAndReturnConfirmation() throws Exception {
        // Given
        User user = userRepository.findById(testUserId).orElseThrow();
        user.setStatus("locked");
        user.setLoginAttempts(5);
        userRepository.save(user);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/users/{userId}/unlock", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("アカウントロックが解除されました"))
                .andExpect(jsonPath("$.data.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.data.status").value("active"));

        // データベースに反映されていることを確認
        User updatedUser = userRepository.findById(testUserId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("active", updatedUser.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(0, updatedUser.getLoginAttempts());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void bulkUpdateUserStatus_ShouldUpdateMultipleUsersAndReturnConfirmation() throws Exception {
        // Given
        // さらに2人のユーザーを作成
        UserCreateRequestDto user1Dto = new UserCreateRequestDto();
        user1Dto.setLoginId("user1");
        user1Dto.setEmail("user1@example.com");
        user1Dto.setName("User One");
        user1Dto.setPassword("Password123");
        user1Dto.setDepartment("Test Department");
        user1Dto.setRole("USER");
        UserResponseDto createdUser1 = userManagementService.createUser(user1Dto);

        UserCreateRequestDto user2Dto = new UserCreateRequestDto();
        user2Dto.setLoginId("user2");
        user2Dto.setEmail("user2@example.com");
        user2Dto.setName("User Two");
        user2Dto.setPassword("Password123");
        user2Dto.setDepartment("Test Department");
        user2Dto.setRole("USER");
        UserResponseDto createdUser2 = userManagementService.createUser(user2Dto);

        UserBulkStatusRequestDto bulkStatusRequestDto = new UserBulkStatusRequestDto();
        bulkStatusRequestDto.setUserIds(Arrays.asList(testUserId, createdUser1.getId(), createdUser2.getId()));
        bulkStatusRequestDto.setStatus("inactive");

        // When & Then
        mockMvc.perform(put("/api/v1/admin/users/bulk-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkStatusRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value(containsString("3 件のユーザーステータスが更新されました")))
                .andExpect(jsonPath("$.data.count").value(3));

        // データベースに反映されていることを確認
        for (UUID userId : Arrays.asList(testUserId, createdUser1.getId(), createdUser2.getId())) {
            User user = userRepository.findById(userId).orElseThrow();
            org.junit.jupiter.api.Assertions.assertEquals("inactive", user.getStatus());
        }
    }

    @Test
    @WithMockUser(roles = "USER")
    void accessWithInsufficientPermissions_ShouldReturnForbidden() throws Exception {
        // When & Then - USER ロールではADMIN専用APIにアクセスできない
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessWithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // When & Then - 認証なしではAPIにアクセスできない
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
    }
}