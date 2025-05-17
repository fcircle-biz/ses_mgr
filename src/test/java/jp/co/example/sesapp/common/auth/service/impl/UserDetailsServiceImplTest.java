package jp.co.example.sesapp.common.auth.service.impl;

import jp.co.example.sesapp.common.auth.domain.Permission;
import jp.co.example.sesapp.common.auth.domain.ResourceType;
import jp.co.example.sesapp.common.auth.domain.Role;
import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.auth.repository.RoleRepository;
import jp.co.example.sesapp.common.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserDetailsServiceImplTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserDetailsServiceImpl userDetailsService;
    
    private User normalUser;
    private User lockedUser;
    private User disabledUser;
    private User expiredUser;
    private User expiredCredentialsUser;
    private Role adminRole;
    private Role userRole;
    private Set<Permission> adminPermissions;
    private Set<Permission> userPermissions;
    
    @BeforeEach
    void setUp() {
        // モックを手動で作成
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        userDetailsService = new UserDetailsServiceImpl(userRepository, roleRepository);
        
        // 権限を作成
        adminPermissions = new HashSet<>();
        Permission userReadPerm = Permission.createNewPermission(ResourceType.USER, "READ", "User read permission");
        Permission userWritePerm = Permission.createNewPermission(ResourceType.USER, "UPDATE", "User write permission");
        Permission userDeletePerm = Permission.createNewPermission(ResourceType.USER, "DELETE", "User delete permission");
        Permission adminReadPerm = Permission.createNewPermission(ResourceType.ROLE, "READ", "Admin read permission");
        Permission adminWritePerm = Permission.createNewPermission(ResourceType.ROLE, "UPDATE", "Admin write permission");
        
        adminPermissions.add(userReadPerm);
        adminPermissions.add(userWritePerm);
        adminPermissions.add(userDeletePerm);
        adminPermissions.add(adminReadPerm);
        adminPermissions.add(adminWritePerm);
        
        userPermissions = new HashSet<>();
        userPermissions.add(userReadPerm);
        
        // ロールを作成
        UUID adminRoleId = UUID.randomUUID();
        adminRole = new Role(adminRoleId, "ADMIN", "Administrator Role");
        adminRole.setPermissions(adminPermissions);
        
        UUID userRoleId = UUID.randomUUID();
        userRole = new Role(userRoleId, "USER", "User Role");
        userRole.setPermissions(userPermissions);
        
        // 通常ユーザーを作成
        normalUser = User.builder()
                .id(UUID.randomUUID())
                .email("normal@example.com")
                .passwordHash("hashedPassword")
                .username("normaluser")
                .firstName("Normal")
                .lastName("User")
                .roleId(adminRoleId)
                .build();
        
        // ロックされたユーザーを作成
        lockedUser = User.builder()
                .id(UUID.randomUUID())
                .email("locked@example.com")
                .passwordHash("hashedPassword")
                .username("lockeduser")
                .firstName("Locked")
                .lastName("User")
                .roleId(userRoleId)
                .accountLocked(true)
                .build();
        
        // 無効化されたユーザーを作成
        disabledUser = User.builder()
                .id(UUID.randomUUID())
                .email("disabled@example.com")
                .passwordHash("hashedPassword")
                .username("disableduser")
                .firstName("Disabled")
                .lastName("User")
                .roleId(userRoleId)
                .enabled(false)
                .build();
        
        // アカウント期限切れユーザーを作成
        expiredUser = User.builder()
                .id(UUID.randomUUID())
                .email("expired@example.com")
                .passwordHash("hashedPassword")
                .username("expireduser")
                .firstName("Expired")
                .lastName("User")
                .roleId(userRoleId)
                .accountExpireDate(LocalDateTime.now().minusDays(1))
                .build();
        
        // 認証情報期限切れユーザーを作成
        expiredCredentialsUser = User.builder()
                .id(UUID.randomUUID())
                .email("expiredcreds@example.com")
                .passwordHash("hashedPassword")
                .username("expiredcredsuser")
                .firstName("ExpiredCreds")
                .lastName("User")
                .roleId(userRoleId)
                .credentialsExpireDate(LocalDateTime.now().minusDays(1))
                .build();
        
        // リポジトリモックのセットアップ
        when(userRepository.findByEmail(normalUser.getEmail())).thenReturn(Optional.of(normalUser));
        when(userRepository.findByEmail(lockedUser.getEmail())).thenReturn(Optional.of(lockedUser));
        when(userRepository.findByEmail(disabledUser.getEmail())).thenReturn(Optional.of(disabledUser));
        when(userRepository.findByEmail(expiredUser.getEmail())).thenReturn(Optional.of(expiredUser));
        when(userRepository.findByEmail(expiredCredentialsUser.getEmail())).thenReturn(Optional.of(expiredCredentialsUser));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        when(roleRepository.findById(adminRole.getId())).thenReturn(Optional.of(adminRole));
        when(roleRepository.findById(userRole.getId())).thenReturn(Optional.of(userRole));
    }
    
    @Test
    void loadUserByUsername_WithValidEmail_ShouldReturnUserDetails() {
        // メールアドレスでユーザー詳細を読み込み
        UserDetails userDetails = userDetailsService.loadUserByUsername(normalUser.getEmail());
        
        // ユーザー詳細を検証
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(normalUser.getEmail());
        assertThat(userDetails.getPassword()).isEqualTo(normalUser.getPasswordHash());
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        
        // 権限を検証
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).isNotEmpty();
        
        // ROLE_ADMIN権限を確認
        assertThat(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))).isTrue();
        
        // 個別権限を確認
        for (Permission permission : adminPermissions) {
            assertThat(authorities.contains(new SimpleGrantedAuthority(permission.getName()))).isTrue();
        }
    }
    
    @Test
    void loadUserByUsername_WithNonExistentEmail_ShouldThrowException() {
        // 存在しないユーザーを読み込み試行
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("nonexistent@example.com");
    }
    
    @Test
    void loadUserByUsername_WithLockedAccount_ShouldReturnLockedUserDetails() {
        // ロックされたユーザー詳細を読み込み
        UserDetails userDetails = userDetailsService.loadUserByUsername(lockedUser.getEmail());
        
        // アカウントがロックされていることを確認
        assertThat(userDetails.isAccountNonLocked()).isFalse();
    }
    
    @Test
    void loadUserByUsername_WithDisabledAccount_ShouldReturnDisabledUserDetails() {
        // 無効化されたユーザー詳細を読み込み
        UserDetails userDetails = userDetailsService.loadUserByUsername(disabledUser.getEmail());
        
        // アカウントが無効化されていることを確認
        assertThat(userDetails.isEnabled()).isFalse();
    }
    
    @Test
    void loadUserByUsername_WithExpiredAccount_ShouldReturnExpiredUserDetails() {
        // 期限切れユーザー詳細を読み込み
        UserDetails userDetails = userDetailsService.loadUserByUsername(expiredUser.getEmail());
        
        // アカウントが期限切れであることを確認
        assertThat(userDetails.isAccountNonExpired()).isFalse();
    }
    
    @Test
    void loadUserByUsername_WithExpiredCredentials_ShouldReturnExpiredCredentialsUserDetails() {
        // 認証情報期限切れユーザー詳細を読み込み
        UserDetails userDetails = userDetailsService.loadUserByUsername(expiredCredentialsUser.getEmail());
        
        // 認証情報が期限切れであることを確認
        assertThat(userDetails.isCredentialsNonExpired()).isFalse();
    }
    
    @Test
    void getAuthorities_ForAdminRole_ShouldReturnAllPermissions() {
        // 管理者ユーザー詳細を読み込み
        UserDetails userDetails = userDetailsService.loadUserByUsername(normalUser.getEmail());
        
        // すべての管理者権限が存在することを確認
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        
        // ROLE_ADMIN権限を確認
        assertThat(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))).isTrue();
        
        // すべての管理者権限を確認
        for (Permission permission : adminPermissions) {
            assertThat(authorities.contains(new SimpleGrantedAuthority(permission.getName()))).isTrue();
        }
        
        // 権限の数が正しいことを確認（ROLE_ADMIN + すべての管理者権限）
        assertThat(authorities).hasSize(adminPermissions.size() + 1);
    }
    
    @Test
    void getAuthorities_ForUserRole_ShouldReturnLimitedPermissions() {
        // 一般ユーザー詳細を読み込み
        UserDetails userDetails = userDetailsService.loadUserByUsername(lockedUser.getEmail());
        
        // 限定的なユーザー権限を確認
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        
        // ROLE_USER権限を確認
        assertThat(authorities.contains(new SimpleGrantedAuthority("ROLE_USER"))).isTrue();
        
        // ユーザー権限を確認
        for (Permission permission : userPermissions) {
            assertThat(authorities.contains(new SimpleGrantedAuthority(permission.getName()))).isTrue();
        }
        
        // 管理者専用権限が存在しないことを確認
        assertThat(authorities.contains(new SimpleGrantedAuthority("role:read"))).isFalse();
        assertThat(authorities.contains(new SimpleGrantedAuthority("role:update"))).isFalse();
        
        // 権限の数が正しいことを確認（ROLE_USER + すべてのユーザー権限）
        assertThat(authorities).hasSize(userPermissions.size() + 1);
    }
}