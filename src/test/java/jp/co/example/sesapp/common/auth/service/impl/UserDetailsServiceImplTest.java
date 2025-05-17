package jp.co.example.sesapp.common.auth.service.impl;

import jp.co.example.sesapp.common.auth.domain.Permission;
import jp.co.example.sesapp.common.auth.domain.Role;
import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.auth.repository.RoleRepository;
import jp.co.example.sesapp.common.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;
    private Role testRole;
    private List<Permission> testPermissions;

    @BeforeEach
    void setUp() {
        // テスト用のユーザーを作成
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setPasswordHash("hashedPassword");
        testUser.setRoleId(UUID.randomUUID());
        testUser.setAccountLocked(false);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        // テスト用のロールを作成
        testRole = new Role();
        testRole.setId(testUser.getRoleId());
        testRole.setName("ADMIN");
        testRole.setDescription("Administrator Role");

        // テスト用の権限を作成
        Permission permission1 = new Permission();
        permission1.setId(UUID.randomUUID());
        permission1.setName("user:read");
        permission1.setDescription("User read permission");

        Permission permission2 = new Permission();
        permission2.setId(UUID.randomUUID());
        permission2.setName("user:write");
        permission2.setDescription("User write permission");

        testPermissions = Arrays.asList(permission1, permission2);
        testRole.setPermissions(new HashSet<>(testPermissions));
    }

    @Test
    void loadUserByUsername_WithValidEmail_ShouldReturnUserDetails() {
        // モックの設定
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(testUser.getRoleId())).thenReturn(Optional.of(testRole));

        // テスト対象メソッドの実行
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getEmail());

        // 検証
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(testUser.getEmail());
        assertThat(userDetails.getPassword()).isEqualTo(testUser.getPasswordHash());
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();

        // GrantedAuthorityの検証
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        
        // ROLEプレフィックス付きのロールが含まれていることを確認
        assertThat(authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()))
                .contains("ROLE_" + testRole.getName().toUpperCase());
        
        // 各権限が含まれていることを確認
        for (Permission permission : testPermissions) {
            assertThat(authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()))
                    .contains(permission.getName());
        }
    }

    @Test
    void loadUserByUsername_WithNonExistentEmail_ShouldThrowUsernameNotFoundException() {
        // モックの設定
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // テスト対象メソッドの実行と検証
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("メールアドレス nonexistent@example.com のユーザーが見つかりませんでした");
    }

    @Test
    void loadUserByUsername_WithLockedAccount_ShouldReturnDisabledUserDetails() {
        // ユーザーアカウントをロック状態に設定
        testUser.setAccountLocked(true);

        // モックの設定
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(testUser.getRoleId())).thenReturn(Optional.of(testRole));

        // テスト対象メソッドの実行
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getEmail());

        // 検証
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.isEnabled()).isTrue(); // isEnabledは別の条件でチェック
        assertThat(userDetails.isAccountNonLocked()).isFalse(); // ロック状態が反映されているか確認
    }

    @Test
    void loadUserByUsername_WithExpiredAccount_ShouldReturnExpiredUserDetails() {
        // ユーザーアカウントの有効期限切れを設定
        testUser.setAccountExpiresAt(LocalDateTime.now().minusDays(1));

        // モックの設定
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(testUser.getRoleId())).thenReturn(Optional.of(testRole));

        // テスト対象メソッドの実行
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getEmail());

        // 検証
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.isAccountNonExpired()).isFalse(); // アカウント有効期限切れが反映されているか確認
    }

    @Test
    void loadUserByUsername_WithExpiredCredentials_ShouldReturnExpiredCredentialsUserDetails() {
        // ユーザー認証情報の有効期限切れを設定
        testUser.setPasswordExpiresAt(LocalDateTime.now().minusDays(1));

        // モックの設定
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(testUser.getRoleId())).thenReturn(Optional.of(testRole));

        // テスト対象メソッドの実行
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getEmail());

        // 検証
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.isCredentialsNonExpired()).isFalse(); // 認証情報有効期限切れが反映されているか確認
    }

    @Test
    void loadUserByUsername_WithNoRole_ShouldReturnUserDetailsWithoutRoleAuthorities() {
        // モックの設定 - ロールが見つからない場合
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(testUser.getRoleId())).thenReturn(Optional.empty());

        // テスト対象メソッドの実行
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getEmail());

        // 検証
        assertThat(userDetails).isNotNull();
        
        // ロールのAuthoritiesが含まれていないことを確認
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).isEmpty();
    }
}