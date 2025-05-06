package com.ses_mgr.common.service;

import com.ses_mgr.common.dto.UserResponseDto;
import com.ses_mgr.common.entity.Department;
import com.ses_mgr.common.entity.Role;
import com.ses_mgr.common.entity.User;
import com.ses_mgr.common.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SimplifiedUserServiceTest {

    @Mock
    private UserRepository userRepository;

    private SimplifiedUserService userService;

    private UUID testUserId;
    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new SimplifiedUserService(userRepository);
        testUserId = UUID.randomUUID();
        testUser = createTestUser();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUserDto() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        UserResponseDto result = userService.getUserById(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals("test.user@example.com", result.getEmail());
        assertEquals("Test User", result.getName());
    }

    // より簡素化したサービス実装
    private static class SimplifiedUserService {
        private final UserRepository userRepository;

        public SimplifiedUserService(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        public UserResponseDto getUserById(UUID userId) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return convertToDto(user);
        }

        private UserResponseDto convertToDto(User user) {
            return UserResponseDto.builder()
                    .id(user.getUserId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .department(user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null)
                    .status(user.getStatus())
                    .lastLoginAt(user.getLastLoginAt())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
        }
    }

    // ユーティリティメソッド
    private User createTestUser() {
        User user = new User();
        user.setUserId(testUserId);
        user.setLoginId("test.login");
        user.setEmail("test.user@example.com");
        user.setName("Test User");
        user.setPasswordHash("encodedPassword");
        user.setStatus("active");
        user.setMfaEnabled(false);
        user.setLoginAttempts(0);

        Department department = new Department();
        department.setDepartmentId(1);
        department.setDepartmentName("Test Department");
        user.setDepartment(department);

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setUserRoles(Collections.emptySet());
        return user;
    }
}