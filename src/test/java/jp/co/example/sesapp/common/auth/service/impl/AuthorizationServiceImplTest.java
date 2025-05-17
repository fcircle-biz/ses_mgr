package jp.co.example.sesapp.common.auth.service.impl;

import jp.co.example.sesapp.common.auth.domain.ActionType;
import jp.co.example.sesapp.common.auth.domain.Permission;
import jp.co.example.sesapp.common.auth.domain.ResourceType;
import jp.co.example.sesapp.common.auth.domain.Role;
import jp.co.example.sesapp.common.auth.repository.PermissionRepository;
import jp.co.example.sesapp.common.auth.repository.RoleRepository;
import jp.co.example.sesapp.common.auth.repository.UserRepository;
import jp.co.example.sesapp.common.exception.AccessDeniedException;
import jp.co.example.sesapp.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthorizationServiceImpl authorizationService;

    private UUID testUserId;
    private List<Permission> userPermissions;
    private List<Role> userRoles;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        // テスト用のパーミッションを作成
        Permission readUserPermission = new Permission();
        readUserPermission.setId(UUID.randomUUID());
        readUserPermission.setName("USER:READ");
        readUserPermission.setResourceType(ResourceType.USER);
        readUserPermission.setActionType(ActionType.READ);

        Permission createUserPermission = new Permission();
        createUserPermission.setId(UUID.randomUUID());
        createUserPermission.setName("USER:CREATE");
        createUserPermission.setResourceType(ResourceType.USER);
        createUserPermission.setActionType(ActionType.CREATE);

        userPermissions = Arrays.asList(readUserPermission, createUserPermission);

        // テスト用のロールを作成
        Role adminRole = new Role();
        adminRole.setId(UUID.randomUUID());
        adminRole.setName("ADMIN");
        adminRole.setDescription("Administrator role");

        Role userRole = new Role();
        userRole.setId(UUID.randomUUID());
        userRole.setName("USER");
        userRole.setDescription("User role");

        userRoles = Arrays.asList(adminRole, userRole);

        // SecurityContextをモック
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void hasPermission_WithPermittedUser_ShouldReturnTrue() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(new jp.co.example.sesapp.common.auth.domain.User()));
        when(permissionRepository.findByUserId(testUserId)).thenReturn(userPermissions);

        // Act
        boolean result = authorizationService.hasPermission(testUserId, ResourceType.USER, ActionType.READ);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void hasPermission_WithoutPermission_ShouldReturnFalse() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(new jp.co.example.sesapp.common.auth.domain.User()));
        when(permissionRepository.findByUserId(testUserId)).thenReturn(userPermissions);

        // Act
        boolean result = authorizationService.hasPermission(testUserId, ResourceType.USER, ActionType.DELETE);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void hasPermission_WithNonexistentUser_ShouldReturnFalse() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act
        boolean result = authorizationService.hasPermission(testUserId, ResourceType.USER, ActionType.READ);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void hasRole_WithUserHavingRole_ShouldReturnTrue() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(new jp.co.example.sesapp.common.auth.domain.User()));
        when(roleRepository.findByUserId(testUserId)).thenReturn(userRoles);

        // Act
        boolean result = authorizationService.hasRole(testUserId, "ADMIN");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void hasRole_WithUserNotHavingRole_ShouldReturnFalse() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(new jp.co.example.sesapp.common.auth.domain.User()));
        when(roleRepository.findByUserId(testUserId)).thenReturn(userRoles);

        // Act
        boolean result = authorizationService.hasRole(testUserId, "MANAGER");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void checkPermission_WithPermittedUser_ShouldNotThrowException() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(new jp.co.example.sesapp.common.auth.domain.User()));
        when(permissionRepository.findByUserId(testUserId)).thenReturn(userPermissions);

        // Act & Assert - 例外が発生しないことを確認
        authorizationService.checkPermission(testUserId, ResourceType.USER, ActionType.READ);
    }

    @Test
    void checkPermission_WithoutPermission_ShouldThrowAccessDeniedException() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(new jp.co.example.sesapp.common.auth.domain.User()));
        when(permissionRepository.findByUserId(testUserId)).thenReturn(userPermissions);

        // Act & Assert
        assertThatThrownBy(() -> 
            authorizationService.checkPermission(testUserId, ResourceType.USER, ActionType.DELETE)
        )
        .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void checkRole_WithUserHavingRole_ShouldNotThrowException() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(new jp.co.example.sesapp.common.auth.domain.User()));
        when(roleRepository.findByUserId(testUserId)).thenReturn(userRoles);

        // Act & Assert - 例外が発生しないことを確認
        authorizationService.checkRole(testUserId, "ADMIN");
    }

    @Test
    void checkRole_WithUserNotHavingRole_ShouldThrowAccessDeniedException() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(new jp.co.example.sesapp.common.auth.domain.User()));
        when(roleRepository.findByUserId(testUserId)).thenReturn(userRoles);

        // Act & Assert
        assertThatThrownBy(() -> 
            authorizationService.checkRole(testUserId, "MANAGER")
        )
        .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void assignRoleToUser_ShouldAssignRole() {
        // Arrange
        UUID roleId = UUID.randomUUID();
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(new jp.co.example.sesapp.common.auth.domain.User()));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(new Role()));

        // Act
        authorizationService.assignRoleToUser(testUserId, roleId);

        // Assert
        verify(roleRepository).assignRoleToUser(testUserId, roleId);
    }

    @Test
    void assignRoleToUser_WithNonexistentUser_ShouldThrowException() {
        // Arrange
        UUID roleId = UUID.randomUUID();
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> 
            authorizationService.assignRoleToUser(testUserId, roleId)
        )
        .isInstanceOf(ResourceNotFoundException.class);
        
        verify(roleRepository, never()).assignRoleToUser(any(), any());
    }

    @Test
    void assignRoleToUser_WithNonexistentRole_ShouldThrowException() {
        // Arrange
        UUID roleId = UUID.randomUUID();
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(new jp.co.example.sesapp.common.auth.domain.User()));
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> 
            authorizationService.assignRoleToUser(testUserId, roleId)
        )
        .isInstanceOf(ResourceNotFoundException.class);
        
        verify(roleRepository, never()).assignRoleToUser(any(), any());
    }

    @Test
    void removeRoleFromUser_ShouldRemoveRole() {
        // Arrange
        UUID roleId = UUID.randomUUID();
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(new jp.co.example.sesapp.common.auth.domain.User()));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(new Role()));

        // Act
        authorizationService.removeRoleFromUser(testUserId, roleId);

        // Assert
        verify(roleRepository).removeRoleFromUser(testUserId, roleId);
    }

    @Test
    void assignPermissionToRole_ShouldAssignPermission() {
        // Arrange
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(new Role()));
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(new Permission()));

        // Act
        authorizationService.assignPermissionToRole(roleId, permissionId);

        // Assert
        verify(permissionRepository).assignPermissionToRole(roleId, permissionId);
    }

    @Test
    void removePermissionFromRole_ShouldRemovePermission() {
        // Arrange
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(new Role()));
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(new Permission()));

        // Act
        authorizationService.removePermissionFromRole(roleId, permissionId);

        // Assert
        verify(permissionRepository).removePermissionFromRole(roleId, permissionId);
    }

    @Test
    void getUserRoles_ShouldReturnRoleNames() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(new jp.co.example.sesapp.common.auth.domain.User()));
        when(roleRepository.findByUserId(testUserId)).thenReturn(userRoles);

        // Act
        List<String> roles = authorizationService.getUserRoles(testUserId);

        // Assert
        assertThat(roles).hasSize(2);
        assertThat(roles).contains("ADMIN", "USER");
    }

    @Test
    void getUserPermissions_ShouldReturnPermissionNames() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(new jp.co.example.sesapp.common.auth.domain.User()));
        when(permissionRepository.findByUserId(testUserId)).thenReturn(userPermissions);

        // Act
        List<String> permissions = authorizationService.getUserPermissions(testUserId);

        // Assert
        assertThat(permissions).hasSize(2);
        assertThat(permissions).contains("USER:READ", "USER:CREATE");
    }

    @Test
    void isCurrentUserAuthorized_WithAuthenticatedUserHavingPermission_ShouldReturnTrue() {
        // Arrange
        jp.co.example.sesapp.common.auth.domain.User mockUser = new jp.co.example.sesapp.common.auth.domain.User();
        mockUser.setId(testUserId);
        mockUser.setUsername("testuser");

        // 認証情報を設定
        org.springframework.security.core.userdetails.User userDetails = 
            new org.springframework.security.core.userdetails.User(
                "testuser", "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, "password", Collections.emptyList());
            
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        
        // USER:READの権限を持っていることを確認する
        Permission readPermission = userPermissions.get(0); // USER:READ権限
        when(permissionRepository.findByUserIdAndResourceTypeAndActionType(
            testUserId, ResourceType.USER, ActionType.READ))
            .thenReturn(Optional.of(readPermission));

        // Act
        boolean result = authorizationService.isCurrentUserAuthorized(ResourceType.USER, ActionType.READ);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void isCurrentUserAuthorized_WithAuthenticatedUserWithoutPermission_ShouldReturnFalse() {
        // Arrange
        jp.co.example.sesapp.common.auth.domain.User mockUser = new jp.co.example.sesapp.common.auth.domain.User();
        mockUser.setId(testUserId);
        mockUser.setUsername("testuser");

        // 認証情報を設定
        org.springframework.security.core.userdetails.User userDetails = 
            new org.springframework.security.core.userdetails.User(
                "testuser", "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, "password", Collections.emptyList());
            
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        
        // DELETE権限は持っていない
        when(permissionRepository.findByUserIdAndResourceTypeAndActionType(
            testUserId, ResourceType.USER, ActionType.DELETE))
            .thenReturn(Optional.empty());

        // Act
        boolean result = authorizationService.isCurrentUserAuthorized(ResourceType.USER, ActionType.DELETE);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void isCurrentUserAuthorized_WithUnauthenticatedUser_ShouldReturnFalse() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        boolean result = authorizationService.isCurrentUserAuthorized(ResourceType.USER, ActionType.READ);

        // Assert
        assertThat(result).isFalse();
    }
}