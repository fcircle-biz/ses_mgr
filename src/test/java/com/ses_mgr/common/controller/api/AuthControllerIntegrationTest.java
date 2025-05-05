package com.ses_mgr.common.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.common.dto.LoginRequestDto;
import com.ses_mgr.common.dto.UserCreateRequestDto;
import com.ses_mgr.common.dto.UserResponseDto;
import com.ses_mgr.common.entity.Department;
import com.ses_mgr.common.entity.Role;
import com.ses_mgr.common.repository.DepartmentRepository;
import com.ses_mgr.common.repository.RoleRepository;
import com.ses_mgr.common.service.UserManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RoleRepository roleRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // 部署の作成
        Department testDepartment = new Department();
        testDepartment.setDepartmentName("Test Department");
        testDepartment.setDepartmentCode("TST");
        testDepartment.setIsActive(true);
        departmentRepository.save(testDepartment);

        // ロールの作成
        Role adminRole = new Role();
        adminRole.setRoleCode("ADMIN");
        adminRole.setName("システム管理者");
        adminRole.setRoleType("system");
        roleRepository.save(adminRole);

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
        userManagementService.createUser(createUserDto);

        // 管理者ユーザーを作成
        UserCreateRequestDto adminUserDto = new UserCreateRequestDto();
        adminUserDto.setLoginId("admin.user");
        adminUserDto.setEmail("admin@example.com");
        adminUserDto.setName("Admin User");
        adminUserDto.setPassword("AdminPass123");
        adminUserDto.setDepartment("Test Department");
        adminUserDto.setRole("ADMIN");
        userManagementService.createUser(adminUserDto);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        // Given
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setLoginId("test.user");
        loginRequest.setPassword("Password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.name").value("Test User"))
                .andExpect(jsonPath("$.data.roles[0]").value("USER"));
    }

    @Test
    void login_WithAdminCredentials_ShouldReturnTokenWithAdminRole() throws Exception {
        // Given
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setLoginId("admin.user");
        loginRequest.setPassword("AdminPass123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.name").value("Admin User"))
                .andExpect(jsonPath("$.data.roles[0]").value("ADMIN"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Given
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setLoginId("test.user");
        loginRequest.setPassword("WrongPassword");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithNonExistentUser_ShouldReturnUnauthorized() throws Exception {
        // Given
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setLoginId("nonexistent.user");
        loginRequest.setPassword("Password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithInvalidInput_ShouldReturnBadRequest() throws Exception {
        // Given
        LoginRequestDto loginRequest = new LoginRequestDto();
        // ログインIDが空
        loginRequest.setLoginId("");
        loginRequest.setPassword("Password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Given
        loginRequest = new LoginRequestDto();
        loginRequest.setLoginId("test.user");
        // パスワードが空
        loginRequest.setPassword("");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
}