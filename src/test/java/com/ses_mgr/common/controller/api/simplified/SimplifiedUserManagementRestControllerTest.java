package com.ses_mgr.common.controller.api.simplified;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ses_mgr.common.controller.api.UserManagementRestController;
import com.ses_mgr.common.dto.*;
import com.ses_mgr.common.service.UserManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class SimplifiedUserManagementRestControllerTest {

    @Mock
    private UserManagementService userManagementService;

    @InjectMocks
    private UserManagementRestController userManagementRestController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private UUID testUserId;
    private UserResponseDto testUserDto;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userManagementRestController).build();

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

    @Test
    @WithMockUser(roles = "ADMIN")
    public void resetUserPassword_ShouldReturnConfirmation() throws Exception {
        // Given
        // userManagementService.resetUserPassword() is a void method, so no mocking needed

        // When & Then
        mockMvc.perform(post("/api/v1/admin/users/{userId}/reset-password", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").exists())
                .andExpect(jsonPath("$.data.id").value(testUserId.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void unlockUserAccount_ShouldUnlockAndReturnConfirmation() throws Exception {
        // Given
        UserResponseDto unlockedUserDto = UserResponseDto.builder()
                .id(testUserId)
                .email("test.user@example.com")
                .name("Test User")
                .role("USER")
                .status("active")
                .build();

        when(userManagementService.unlockUserAccount(testUserId)).thenReturn(unlockedUserDto);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/users/{userId}/unlock", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").exists())
                .andExpect(jsonPath("$.data.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.data.status").value("active"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void bulkUpdateUserStatus_ShouldUpdateMultipleUsersAndReturnConfirmation() throws Exception {
        // Given
        List<UUID> userIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        UserBulkStatusRequestDto bulkStatusRequestDto = new UserBulkStatusRequestDto();
        bulkStatusRequestDto.setUserIds(userIds);
        bulkStatusRequestDto.setStatus("inactive");

        when(userManagementService.updateBulkUserStatus(any(UserBulkStatusRequestDto.class))).thenReturn(2);

        // When & Then
        mockMvc.perform(put("/api/v1/admin/users/bulk-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkStatusRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void bulkUnlockUserAccounts_ShouldUnlockMultipleUsersAndReturnConfirmation() throws Exception {
        // Given
        List<UUID> userIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        UserBulkUnlockRequestDto bulkUnlockRequestDto = new UserBulkUnlockRequestDto();
        bulkUnlockRequestDto.setUserIds(userIds);

        when(userManagementService.unlockBulkUserAccounts(any(UserBulkUnlockRequestDto.class))).thenReturn(2);

        // When & Then
        mockMvc.perform(put("/api/v1/admin/users/bulk-unlock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkUnlockRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(2));
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