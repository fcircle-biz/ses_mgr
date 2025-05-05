package com.ses_mgr.common.service;

import com.ses_mgr.common.dto.*;
import com.ses_mgr.common.entity.Department;
import com.ses_mgr.common.entity.Role;
import com.ses_mgr.common.entity.User;
import com.ses_mgr.common.entity.UserRole;
import com.ses_mgr.common.entity.UserRoleId;
import com.ses_mgr.common.repository.DepartmentRepository;
import com.ses_mgr.common.repository.RoleRepository;
import com.ses_mgr.common.repository.UserRepository;
import com.ses_mgr.common.repository.UserRoleRepository;
import com.ses_mgr.common.service.impl.UserManagementServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
public class UserManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    private PasswordEncoder passwordEncoder;
    private UserManagementServiceImpl userManagementService;

    private UUID testUserId;
    private User testUser;
    private Role testRole;
    private Department testDepartment;
    private UserRole testUserRole;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userManagementService = new UserManagementServiceImpl(
                userRepository, departmentRepository, roleRepository, userRoleRepository, passwordEncoder);

        testUserId = UUID.randomUUID();
        testRole = createTestRole();
        testDepartment = createTestDepartment();
        testUser = createTestUser();
        testUserRole = createTestUserRole(testUser, testRole);
        
        // Setup standard user roles
        Set<UserRole> userRoles = new HashSet<>();
        userRoles.add(testUserRole);
        testUser.setUserRoles(userRoles);
    }

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        // Given
        when(userRepository.findByLoginId("test.login")).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = userManagementService.loadUserByUsername("test.login");

        // Then
        assertNotNull(result);
        assertEquals("test.login", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
    }

    @Test
    void loadUserByUsername_WhenUserDoesNotExist_ShouldThrowException() {
        // Given
        when(userRepository.findByLoginId("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            userManagementService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUserDto() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        UserResponseDto result = userManagementService.getUserById(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals("test.user@example.com", result.getEmail());
        assertEquals("Test User", result.getName());
        assertEquals("Test Department", result.getDepartment());
        assertEquals("USER", result.getRole());
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            userManagementService.getUserById(nonExistentId);
        });
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        // Given
        List<User> userList = Collections.singletonList(testUser);
        when(userRepository.findAll()).thenReturn(userList);

        // When
        List<UserResponseDto> result = userManagementService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserId, result.get(0).getId());
        assertEquals("test.user@example.com", result.get(0).getEmail());
    }

    @Test
    void getAllUsers_WithPagination_ShouldReturnPageOfUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<User> userList = Collections.singletonList(testUser);
        Page<User> userPage = new PageImpl<>(userList, pageable, 1);
        
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // When
        Page<UserResponseDto> result = userManagementService.getAllUsers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUserId, result.getContent().get(0).getId());
        assertEquals("test.user@example.com", result.getContent().get(0).getEmail());
    }

    @Test
    void searchUsers_ShouldReturnMatchingUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        UserSearchRequestDto searchRequestDto = new UserSearchRequestDto();
        searchRequestDto.setKeyword("test");
        searchRequestDto.setStatus("active");
        searchRequestDto.setDepartment("Test Department");
        
        when(departmentRepository.findByDepartmentName("Test Department")).thenReturn(Optional.of(testDepartment));
        
        List<User> userList = Collections.singletonList(testUser);
        Page<User> userPage = new PageImpl<>(userList, pageable, 1);
        
        when(userRepository.searchUsers(
                eq("test"), eq("active"), eq(testDepartment.getDepartmentId()), 
                isNull(), isNull(), isNull(), eq(pageable)
        )).thenReturn(userPage);

        // When
        Page<UserResponseDto> result = userManagementService.searchUsers(searchRequestDto, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUserId, result.getContent().get(0).getId());
        assertEquals("test.user@example.com", result.getContent().get(0).getEmail());
    }

    @Test
    void createUser_ShouldCreateUserWithEncodedPassword() {
        // Given
        UserCreateRequestDto createRequestDto = new UserCreateRequestDto();
        createRequestDto.setLoginId("test.login");
        createRequestDto.setEmail("new.user@example.com");
        createRequestDto.setName("New User");
        createRequestDto.setPassword("password123");
        // Setting no role to skip role assignment
        createRequestDto.setRole(null);
        createRequestDto.setDepartment("Test Department");

        when(userRepository.existsByLoginId(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(departmentRepository.findByDepartmentName(anyString())).thenReturn(Optional.of(testDepartment));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserResponseDto result = userManagementService.createUser(createRequestDto);

        // Then
        assertNotNull(result);
        assertEquals("new.user@example.com", result.getEmail());
        assertEquals("New User", result.getName());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        // パスワードがエンコードされていることを確認（実際のハッシュ値の検証は困難なので、値が存在することを確認）
        assertNotNull(savedUser.getPasswordHash());
        assertNotEquals("password123", savedUser.getPasswordHash());
        
        // Verify that role assignment was not called
        verify(roleRepository, never()).findByRoleCode(anyString());
    }

    @Test
    void createUser_WithExistingLoginId_ShouldThrowException() {
        // Given
        UserCreateRequestDto createRequestDto = new UserCreateRequestDto();
        createRequestDto.setLoginId("existing.login");
        createRequestDto.setEmail("new.user@example.com");
        
        when(userRepository.existsByLoginId("existing.login")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userManagementService.createUser(createRequestDto);
        });
        
        // Verify that save was never called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WithExistingEmail_ShouldThrowException() {
        // Given
        UserCreateRequestDto createRequestDto = new UserCreateRequestDto();
        createRequestDto.setLoginId("new.login");
        createRequestDto.setEmail("existing.email@example.com");
        
        when(userRepository.existsByLoginId("new.login")).thenReturn(false);
        when(userRepository.existsByEmail("existing.email@example.com")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userManagementService.createUser(createRequestDto);
        });
        
        // Verify that save was never called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_ShouldUpdateUserInfo() {
        // Given
        UserUpdateRequestDto updateRequestDto = new UserUpdateRequestDto();
        updateRequestDto.setEmail("updated.email@example.com");
        updateRequestDto.setName("Updated Name");
        updateRequestDto.setPosition("Senior Developer");
        updateRequestDto.setDepartment("Test Department");
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(departmentRepository.findByDepartmentName(anyString())).thenReturn(Optional.of(testDepartment));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserResponseDto result = userManagementService.updateUser(testUserId, updateRequestDto);

        // Then
        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals("updated.email@example.com", result.getEmail());
        assertEquals("Updated Name", result.getName());
        assertEquals("Senior Developer", result.getPosition());
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("updated.email@example.com", savedUser.getEmail());
        assertEquals("Updated Name", savedUser.getName());
        assertEquals("Senior Developer", savedUser.getPosition());
    }

    @Test
    void updateUser_WithNonExistentUser_ShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        UserUpdateRequestDto updateRequestDto = new UserUpdateRequestDto();
        
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            userManagementService.updateUser(nonExistentId, updateRequestDto);
        });
        
        // Verify that save was never called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserStatus_ShouldUpdateStatus() {
        // Given
        UserStatusRequestDto statusRequestDto = new UserStatusRequestDto();
        statusRequestDto.setStatus("inactive");
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserResponseDto result = userManagementService.updateUserStatus(testUserId, statusRequestDto);

        // Then
        assertNotNull(result);
        assertEquals("inactive", result.getStatus());
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("inactive", savedUser.getStatus());
    }

    @Test
    void resetUserPassword_ShouldUpdatePasswordAndExpiryDate() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userManagementService.resetUserPassword(testUserId);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        
        // Password should be changed
        assertNotEquals("encodedPassword", savedUser.getPasswordHash());
        // Password expiry should be set
        assertNotNull(savedUser.getPasswordExpiresAt());
        // Expiry should be in the future (1 day by default)
        assertTrue(savedUser.getPasswordExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void unlockUserAccount_ShouldUnlockAndResetLoginAttempts() {
        // Given
        User lockedUser = createTestUser();
        lockedUser.setStatus("locked");
        lockedUser.setLoginAttempts(5);
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(lockedUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserResponseDto result = userManagementService.unlockUserAccount(testUserId);

        // Then
        assertNotNull(result);
        assertEquals("active", result.getStatus());
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("active", savedUser.getStatus());
        assertEquals(0, savedUser.getLoginAttempts());
    }

    @Test
    void unlockUserAccount_WhenUserNotLocked_ShouldThrowException() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser)); // Default status is "active"

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            userManagementService.unlockUserAccount(testUserId);
        });
        
        // Verify that save was never called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateBulkUserStatus_ShouldReturnUpdatedCount() {
        // Given
        List<UUID> userIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        UserBulkStatusRequestDto bulkStatusRequestDto = new UserBulkStatusRequestDto();
        bulkStatusRequestDto.setUserIds(userIds);
        bulkStatusRequestDto.setStatus("inactive");
        
        when(userRepository.updateStatusForUsers(userIds, "inactive")).thenReturn(2);

        // When
        int updatedCount = userManagementService.updateBulkUserStatus(bulkStatusRequestDto);

        // Then
        assertEquals(2, updatedCount);
        verify(userRepository).updateStatusForUsers(userIds, "inactive");
    }

    @Test
    void unlockBulkUserAccounts_ShouldReturnUnlockedCount() {
        // Given
        List<UUID> userIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        UserBulkUnlockRequestDto bulkUnlockRequestDto = new UserBulkUnlockRequestDto();
        bulkUnlockRequestDto.setUserIds(userIds);
        
        when(userRepository.unlockUsers(userIds)).thenReturn(2);

        // When
        int unlockedCount = userManagementService.unlockBulkUserAccounts(bulkUnlockRequestDto);

        // Then
        assertEquals(2, unlockedCount);
        verify(userRepository).unlockUsers(userIds);
    }

    @Test
    void assignRoleToUser_ShouldAssignRole() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByRoleCode("ADMIN")).thenReturn(Optional.of(testRole));
        when(userRoleRepository.existsByUserUserIdAndRoleRoleId(testUserId, testRole.getRoleId())).thenReturn(false);

        // When
        userManagementService.assignRoleToUser(testUserId, "ADMIN");

        // Then
        ArgumentCaptor<UserRole> userRoleCaptor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(userRoleCaptor.capture());
        UserRole savedUserRole = userRoleCaptor.getValue();
        assertEquals(testUser, savedUserRole.getUser());
        assertEquals(testRole, savedUserRole.getRole());
        assertNotNull(savedUserRole.getAssignedAt());
    }

    @Test
    void removeRoleFromUser_ShouldRemoveRole() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByRoleCode("USER")).thenReturn(Optional.of(testRole));
        
        UserRoleId userRoleId = new UserRoleId(testUserId, testRole.getRoleId());
        when(userRoleRepository.findById(userRoleId)).thenReturn(Optional.of(testUserRole));

        // When
        userManagementService.removeRoleFromUser(testUserId, "USER");

        // Then
        verify(userRoleRepository).delete(testUserRole);
    }

    @Test
    void getUserRoles_ShouldReturnRolesList() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        List<RoleResponseDto> roles = userManagementService.getUserRoles(testUserId);

        // Then
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("USER", roles.get(0).getRoleCode());
    }

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        // Given
        when(userRepository.findByEmail("test.user@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserResponseDto result = userManagementService.findByEmail("test.user@example.com");

        // Then
        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals("test.user@example.com", result.getEmail());
    }

    @Test
    void findByLoginId_WhenUserExists_ShouldReturnUser() {
        // Given
        when(userRepository.findByLoginId("test.login")).thenReturn(Optional.of(testUser));

        // When
        UserResponseDto result = userManagementService.findByLoginId("test.login");

        // Then
        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals("test.login", testUser.getLoginId());
    }

    @Test
    void existsById_ShouldReturnTrueForExistingUser() {
        // Given
        when(userRepository.existsById(testUserId)).thenReturn(true);

        // When
        boolean result = userManagementService.existsById(testUserId);

        // Then
        assertTrue(result);
    }

    @Test
    void existsByEmail_ShouldReturnTrueForExistingEmail() {
        // Given
        when(userRepository.existsByEmail("test.user@example.com")).thenReturn(true);

        // When
        boolean result = userManagementService.existsByEmail("test.user@example.com");

        // Then
        assertTrue(result);
    }

    @Test
    void existsByLoginId_ShouldReturnTrueForExistingLoginId() {
        // Given
        when(userRepository.existsByLoginId("test.login")).thenReturn(true);

        // When
        boolean result = userManagementService.existsByLoginId("test.login");

        // Then
        assertTrue(result);
    }

    @Test
    void updateLastLoginTime_ShouldCallRepository() {
        // When
        userManagementService.updateLastLoginTime(testUserId);

        // Then
        verify(userRepository).updateLastLoginTime(testUserId);
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
        user.setDepartment(testDepartment);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setUserRoles(Collections.emptySet());
        return user;
    }

    private Role createTestRole() {
        Role role = new Role();
        role.setRoleId(UUID.randomUUID());
        role.setRoleCode("USER");
        role.setName("一般ユーザー");
        role.setDescription("標準的なユーザー権限を持つロール");
        role.setRoleType("business");
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        role.setUserRoles(Collections.emptySet());
        role.setRolePermissions(Collections.emptySet());
        return role;
    }

    private Department createTestDepartment() {
        Department department = new Department();
        department.setDepartmentId(1);
        department.setDepartmentName("Test Department");
        department.setDepartmentCode("TST");
        department.setIsActive(true);
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());
        department.setUsers(Collections.emptySet());
        department.setChildDepartments(Collections.emptySet());
        return department;
    }
    
    private UserRole createTestUserRole(User user, Role role) {
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedAt(LocalDateTime.now());
        return userRole;
    }
}