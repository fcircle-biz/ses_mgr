package com.ses_mgr.common.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.common.dto.*;
import com.ses_mgr.common.service.UserManagementService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserManagementRestController.class)
public class UserManagementRestControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockBean
    private UserManagementService userManagementService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private UUID testUserId;
    private UserResponseDto testUserDto;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testUserId = UUID.randomUUID();
        testUserDto = createTestUserResponseDto();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getAllUsers_ShouldReturnUsersList() throws Exception {
        // Given
        List<UserResponseDto> usersList = Arrays.asList(testUserDto, createAnotherTestUserResponseDto());
        when(userManagementService.getAllUsers()).thenReturn(usersList);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").value(testUserId.toString()))
                .andExpect(jsonPath("$.data[0].email").value("test.user@example.com"))
                .andExpect(jsonPath("$.data[0].name").value("Test User"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void searchUsers_ShouldReturnMatchingUsers() throws Exception {
        // Given
        UserSearchRequestDto searchRequestDto = new UserSearchRequestDto();
        searchRequestDto.setKeyword("test");
        searchRequestDto.setStatus("active");

        List<UserResponseDto> matchingUsers = Collections.singletonList(testUserDto);
        when(userManagementService.searchUsers(any(UserSearchRequestDto.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(matchingUsers));

        // When & Then
        mockMvc.perform(post("/api/v1/admin/users/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(testUserId.toString()))
                .andExpect(jsonPath("$.data.content[0].email").value("test.user@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {
        // Given
        when(userManagementService.getUserById(testUserId)).thenReturn(testUserDto);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/users/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.data.email").value("test.user@example.com"))
                .andExpect(jsonPath("$.data.name").value("Test User"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getUserById_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(userManagementService.getUserById(any(UUID.class)))
                .thenThrow(new EntityNotFoundException("ユーザーが見つかりません"));

        // When & Then
        mockMvc.perform(get("/api/v1/admin/users/{userId}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createUser_WithValidData_ShouldCreateUser() throws Exception {
        // Given
        UserCreateRequestDto createRequestDto = new UserCreateRequestDto();
        createRequestDto.setLoginId("new.user");
        createRequestDto.setEmail("new.user@example.com");
        createRequestDto.setName("New User");
        createRequestDto.setPassword("Password123");
        createRequestDto.setRole("USER");

        UserResponseDto createdUserDto = UserResponseDto.builder()
                .id(UUID.randomUUID())
                .email("new.user@example.com")
                .name("New User")
                .role("USER")
                .status("active")
                .build();

        when(userManagementService.createUser(any(UserCreateRequestDto.class))).thenReturn(createdUserDto);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("new.user@example.com"))
                .andExpect(jsonPath("$.data.name").value("New User"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateUser_WithValidData_ShouldUpdateUser() throws Exception {
        // Given
        UserUpdateRequestDto updateRequestDto = new UserUpdateRequestDto();
        updateRequestDto.setEmail("updated.email@example.com");
        updateRequestDto.setName("Updated Name");

        UserResponseDto updatedUserDto = UserResponseDto.builder()
                .id(testUserId)
                .email("updated.email@example.com")
                .name("Updated Name")
                .role("USER")
                .status("active")
                .build();

        when(userManagementService.updateUser(eq(testUserId), any(UserUpdateRequestDto.class)))
                .thenReturn(updatedUserDto);

        // When & Then
        mockMvc.perform(put("/api/v1/admin/users/{userId}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.data.email").value("updated.email@example.com"))
                .andExpect(jsonPath("$.data.name").value("Updated Name"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateUserStatus_ShouldUpdateStatus() throws Exception {
        // Given
        UserStatusRequestDto statusRequestDto = new UserStatusRequestDto();
        statusRequestDto.setStatus("inactive");

        UserResponseDto updatedUserDto = UserResponseDto.builder()
                .id(testUserId)
                .email("test.user@example.com")
                .name("Test User")
                .role("USER")
                .status("inactive")
                .build();

        when(userManagementService.updateUserStatus(eq(testUserId), any(UserStatusRequestDto.class)))
                .thenReturn(updatedUserDto);

        // When & Then
        mockMvc.perform(put("/api/v1/admin/users/{userId}/status", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.data.status").value("inactive"));
    }

    // ユーティリティメソッド
    private UserResponseDto createTestUserResponseDto() {
        return UserResponseDto.builder()
                .id(testUserId)
                .email("test.user@example.com")
                .name("Test User")
                .department("Test Department")
                .position("Developer")
                .role("USER")
                .status("active")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private UserResponseDto createAnotherTestUserResponseDto() {
        return UserResponseDto.builder()
                .id(UUID.randomUUID())
                .email("another.user@example.com")
                .name("Another User")
                .department("Another Department")
                .position("Manager")
                .role("MANAGER")
                .status("active")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}