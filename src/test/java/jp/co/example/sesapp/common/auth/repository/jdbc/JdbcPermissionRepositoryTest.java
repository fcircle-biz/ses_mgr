package jp.co.example.sesapp.common.auth.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import jp.co.example.sesapp.common.auth.domain.ActionType;
import jp.co.example.sesapp.common.auth.domain.Permission;
import jp.co.example.sesapp.common.auth.domain.ResourceType;
import jp.co.example.sesapp.common.auth.domain.Role;
import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.exception.ResourceNotFoundException;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JdbcPermissionRepository.class, JdbcRoleRepository.class, JdbcUserRepository.class})
@ActiveProfiles("test")
public class JdbcPermissionRepositoryTest {

    @Autowired
    private JdbcPermissionRepository permissionRepository;
    
    @Autowired
    private JdbcRoleRepository roleRepository;
    
    @Autowired
    private JdbcUserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Permission testPermission;
    private UUID permissionId;

    @BeforeEach
    public void setUp() {
        // テスト用のスキーマとテーブルを作成
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS auth");
        
        jdbcTemplate.execute("DROP TABLE IF EXISTS auth.user_roles");
        jdbcTemplate.execute("DROP TABLE IF EXISTS auth.role_permissions");
        jdbcTemplate.execute("DROP TABLE IF EXISTS auth.permissions");
        jdbcTemplate.execute("DROP TABLE IF EXISTS auth.roles");
        jdbcTemplate.execute("DROP TABLE IF EXISTS auth.users");
        
        jdbcTemplate.execute("CREATE TABLE auth.permissions (" +
                "id UUID PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL UNIQUE, " +
                "description VARCHAR(255), " +
                "resource_type VARCHAR(50) NOT NULL, " +
                "action_type VARCHAR(50) NOT NULL, " +
                "created_at TIMESTAMP NOT NULL, " +
                "updated_at TIMESTAMP" +
                ")");
                
        jdbcTemplate.execute("CREATE TABLE auth.roles (" +
                "id UUID PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL UNIQUE, " +
                "description VARCHAR(255), " +
                "is_system_role BOOLEAN DEFAULT FALSE, " +
                "created_at TIMESTAMP NOT NULL, " +
                "updated_at TIMESTAMP" +
                ")");
                
        jdbcTemplate.execute("CREATE TABLE auth.users (" +
                "id UUID PRIMARY KEY, " +
                "username VARCHAR(255) NOT NULL UNIQUE, " +
                "email VARCHAR(255) NOT NULL UNIQUE, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "first_name VARCHAR(255), " +
                "last_name VARCHAR(255), " +
                "department_id UUID, " +
                "is_enabled BOOLEAN DEFAULT TRUE, " +
                "is_account_locked BOOLEAN DEFAULT FALSE, " +
                "account_expire_date TIMESTAMP, " +
                "credentials_expire_date TIMESTAMP, " +
                "last_login_date TIMESTAMP, " +
                "login_fail_count INT DEFAULT 0, " +
                "authentication_method VARCHAR(50), " +
                "is_mfa_enabled BOOLEAN DEFAULT FALSE, " +
                "mfa_secret VARCHAR(255), " +
                "created_at TIMESTAMP NOT NULL, " +
                "updated_at TIMESTAMP" +
                ")");
                
        jdbcTemplate.execute("CREATE TABLE auth.user_roles (" +
                "user_id UUID NOT NULL, " +
                "role_id UUID NOT NULL, " +
                "created_at TIMESTAMP NOT NULL, " +
                "PRIMARY KEY (user_id, role_id)" +
                ")");
                
        jdbcTemplate.execute("CREATE TABLE auth.role_permissions (" +
                "role_id UUID NOT NULL, " +
                "permission_id UUID NOT NULL, " +
                "created_at TIMESTAMP NOT NULL, " +
                "PRIMARY KEY (role_id, permission_id)" +
                ")");

        // テスト用の権限データを作成
        permissionId = UUID.randomUUID();
        testPermission = new Permission(permissionId, "user:read", "Read user information", ResourceType.USER, ActionType.READ.name());
    }

    @Test
    public void savePermission_ShouldInsertNewPermission() {
        // テスト実行
        Permission savedPermission = permissionRepository.save(testPermission);

        // 検証
        assertThat(savedPermission).isNotNull();
        assertThat(savedPermission.getId()).isEqualTo(permissionId);
        assertThat(savedPermission.getName()).isEqualTo("user:read");
        assertThat(savedPermission.getResourceType()).isEqualTo(ResourceType.USER);
        assertThat(savedPermission.getActionType()).isEqualTo(ActionType.READ);
        assertThat(savedPermission.getCreatedAt()).isNotNull();
    }

    @Test
    public void findById_ShouldReturnPermission_WhenPermissionExists() {
        // 事前準備
        permissionRepository.save(testPermission);

        // テスト実行
        Optional<Permission> foundPermission = permissionRepository.findById(permissionId);

        // 検証
        assertThat(foundPermission).isPresent();
        assertThat(foundPermission.get().getId()).isEqualTo(permissionId);
        assertThat(foundPermission.get().getName()).isEqualTo("user:read");
        assertThat(foundPermission.get().getResourceType()).isEqualTo(ResourceType.USER);
    }

    @Test
    public void findById_ShouldReturnEmpty_WhenPermissionDoesNotExist() {
        // テスト実行
        Optional<Permission> foundPermission = permissionRepository.findById(UUID.randomUUID());

        // 検証
        assertThat(foundPermission).isEmpty();
    }

    @Test
    public void findByName_ShouldReturnPermission_WhenPermissionExists() {
        // 事前準備
        permissionRepository.save(testPermission);

        // テスト実行
        Optional<Permission> foundPermission = permissionRepository.findByName("user:read");

        // 検証
        assertThat(foundPermission).isPresent();
        assertThat(foundPermission.get().getId()).isEqualTo(permissionId);
        assertThat(foundPermission.get().getName()).isEqualTo("user:read");
    }

    @Test
    public void findByResourceAndAction_ShouldReturnPermission_WhenPermissionExists() {
        // 事前準備
        permissionRepository.save(testPermission);

        // テスト実行
        Optional<Permission> foundPermission = permissionRepository.findByResourceAndAction(ResourceType.USER, ActionType.READ);

        // 検証
        assertThat(foundPermission).isPresent();
        assertThat(foundPermission.get().getId()).isEqualTo(permissionId);
        assertThat(foundPermission.get().getResourceType()).isEqualTo(ResourceType.USER);
        assertThat(foundPermission.get().getActionType()).isEqualTo(ActionType.READ);
    }

    @Test
    public void findAll_ShouldReturnAllPermissions() {
        // 事前準備
        permissionRepository.save(testPermission);
        
        Permission secondPermission = new Permission(
                UUID.randomUUID(), 
                "project:create", 
                "Create project", 
                ResourceType.PROJECT, 
                ActionType.CREATE.name()
        );
        permissionRepository.save(secondPermission);

        // テスト実行
        List<Permission> permissions = permissionRepository.findAll();

        // 検証
        assertThat(permissions).hasSize(2);
        assertThat(permissions).extracting("name").contains("user:read", "project:create");
    }

    @Test
    public void findByResourceType_ShouldReturnPermissions_ForResourceType() {
        // 事前準備
        permissionRepository.save(testPermission);
        
        Permission userCreatePermission = new Permission(
                UUID.randomUUID(), 
                "user:create", 
                "Create user", 
                ResourceType.USER, 
                ActionType.CREATE.name()
        );
        permissionRepository.save(userCreatePermission);
        
        Permission projectPermission = new Permission(
                UUID.randomUUID(), 
                "project:read", 
                "Read project", 
                ResourceType.PROJECT, 
                ActionType.READ.name()
        );
        permissionRepository.save(projectPermission);

        // テスト実行
        List<Permission> userPermissions = permissionRepository.findByResourceType(ResourceType.USER);

        // 検証
        assertThat(userPermissions).hasSize(2);
        assertThat(userPermissions).extracting("name").containsExactlyInAnyOrder("user:read", "user:create");
    }

    @Test
    public void updatePermission_ShouldUpdateExistingPermission() {
        // 事前準備
        permissionRepository.save(testPermission);

        // 権限情報の更新
        testPermission.setDescription("Updated description");
        testPermission.setName("user:read:updated");

        // テスト実行
        Permission updatedPermission = permissionRepository.save(testPermission);

        // 検証
        assertThat(updatedPermission.getDescription()).isEqualTo("Updated description");
        assertThat(updatedPermission.getName()).isEqualTo("user:read:updated");

        // データベースから再取得して確認
        Optional<Permission> retrievedPermission = permissionRepository.findById(permissionId);
        assertThat(retrievedPermission).isPresent();
        assertThat(retrievedPermission.get().getDescription()).isEqualTo("Updated description");
        assertThat(retrievedPermission.get().getName()).isEqualTo("user:read:updated");
    }

    @Test
    public void updatePermission_ShouldThrowException_WhenPermissionDoesNotExist() {
        // 存在しない権限を更新しようとする
        Permission nonExistentPermission = new Permission(
                UUID.randomUUID(), 
                "nonexistent:permission", 
                "Non-existent Permission", 
                ResourceType.USER, 
                ActionType.READ.name()
        );

        // テスト実行と検証
        assertThatThrownBy(() -> permissionRepository.save(nonExistentPermission))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Permission not found with id");
    }

    @Test
    public void deleteById_ShouldRemovePermission() {
        // 事前準備
        permissionRepository.save(testPermission);

        // テスト実行
        permissionRepository.deleteById(permissionId);

        // 検証
        Optional<Permission> deletedPermission = permissionRepository.findById(permissionId);
        assertThat(deletedPermission).isEmpty();
    }

    @Test
    public void deleteById_ShouldThrowException_WhenPermissionDoesNotExist() {
        // テスト実行と検証
        UUID nonExistentId = UUID.randomUUID();
        assertThatThrownBy(() -> permissionRepository.deleteById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Permission not found with id");
    }

    @Test
    public void assignPermissionToRole_ShouldAssignPermissionToRole() {
        // 事前準備
        permissionRepository.save(testPermission);
        
        Role role = new Role(UUID.randomUUID(), "ROLE_TEST", "Test Role");
        roleRepository.save(role);

        // テスト実行
        permissionRepository.assignPermissionToRole(role.getId(), permissionId);

        // 検証
        List<Permission> rolePermissions = permissionRepository.findByRoleId(role.getId());
        assertThat(rolePermissions).hasSize(1);
        assertThat(rolePermissions.get(0).getId()).isEqualTo(permissionId);
    }

    @Test
    public void removePermissionFromRole_ShouldRemovePermissionFromRole() {
        // 事前準備
        permissionRepository.save(testPermission);
        
        Role role = new Role(UUID.randomUUID(), "ROLE_TEST", "Test Role");
        roleRepository.save(role);
        
        // ロールに権限を割り当て
        permissionRepository.assignPermissionToRole(role.getId(), permissionId);
        
        // 割り当てが成功したことを確認
        List<Permission> rolePermissions = permissionRepository.findByRoleId(role.getId());
        assertThat(rolePermissions).hasSize(1);

        // テスト実行
        permissionRepository.removePermissionFromRole(role.getId(), permissionId);

        // 検証
        List<Permission> updatedRolePermissions = permissionRepository.findByRoleId(role.getId());
        assertThat(updatedRolePermissions).isEmpty();
    }

    @Test
    public void findByRoleId_ShouldReturnPermissions_AssignedToRole() {
        // 事前準備
        permissionRepository.save(testPermission);
        
        Permission secondPermission = new Permission(
                UUID.randomUUID(), 
                "user:create", 
                "Create user", 
                ResourceType.USER, 
                ActionType.CREATE.name()
        );
        permissionRepository.save(secondPermission);
        
        Role role = new Role(UUID.randomUUID(), "ROLE_TEST", "Test Role");
        roleRepository.save(role);
        
        // ロールに両方の権限を割り当て
        permissionRepository.assignPermissionToRole(role.getId(), permissionId);
        permissionRepository.assignPermissionToRole(role.getId(), secondPermission.getId());

        // テスト実行
        List<Permission> rolePermissions = permissionRepository.findByRoleId(role.getId());

        // 検証
        assertThat(rolePermissions).hasSize(2);
        assertThat(rolePermissions).extracting("name").containsExactlyInAnyOrder("user:read", "user:create");
    }

    @Test
    public void findByUserId_ShouldReturnPermissions_AssignedToUser() {
        // 事前準備
        permissionRepository.save(testPermission);
        
        Permission secondPermission = new Permission(
                UUID.randomUUID(), 
                "user:create", 
                "Create user", 
                ResourceType.USER, 
                ActionType.CREATE.name()
        );
        permissionRepository.save(secondPermission);
        
        Role role = new Role(UUID.randomUUID(), "ROLE_TEST", "Test Role");
        roleRepository.save(role);
        
        // ロールに両方の権限を割り当て
        permissionRepository.assignPermissionToRole(role.getId(), permissionId);
        permissionRepository.assignPermissionToRole(role.getId(), secondPermission.getId());
        
        // ユーザーを作成
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .firstName("Test")
                .lastName("User")
                .build();
        User savedUser = userRepository.save(user);
        
        // ユーザーにロールを割り当て
        roleRepository.assignRoleToUser(savedUser.getId(), role.getId());

        // テスト実行
        List<Permission> userPermissions = permissionRepository.findByUserId(savedUser.getId());

        // 検証
        assertThat(userPermissions).hasSize(2);
        assertThat(userPermissions).extracting("name").containsExactlyInAnyOrder("user:read", "user:create");
    }

    @Test
    public void findByUserIdAndResourceTypeAndActionType_ShouldReturnPermission_WhenUserHasAccess() {
        // 事前準備
        permissionRepository.save(testPermission);
        
        Role role = new Role(UUID.randomUUID(), "ROLE_TEST", "Test Role");
        roleRepository.save(role);
        
        // ロールに権限を割り当て
        permissionRepository.assignPermissionToRole(role.getId(), permissionId);
        
        // ユーザーを作成
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .firstName("Test")
                .lastName("User")
                .build();
        User savedUser = userRepository.save(user);
        
        // ユーザーにロールを割り当て
        roleRepository.assignRoleToUser(savedUser.getId(), role.getId());

        // テスト実行
        Optional<Permission> foundPermission = permissionRepository.findByUserIdAndResourceTypeAndActionType(
                savedUser.getId(), ResourceType.USER, ActionType.READ);

        // 検証
        assertThat(foundPermission).isPresent();
        assertThat(foundPermission.get().getId()).isEqualTo(permissionId);
        assertThat(foundPermission.get().getResourceType()).isEqualTo(ResourceType.USER);
        assertThat(foundPermission.get().getActionType()).isEqualTo(ActionType.READ);
    }

    @Test
    public void findByUserIdAndResourceTypeAndActionType_ShouldReturnEmpty_WhenUserDoesNotHaveAccess() {
        // 事前準備
        permissionRepository.save(testPermission);
        
        Role role = new Role(UUID.randomUUID(), "ROLE_TEST", "Test Role");
        roleRepository.save(role);
        
        // ロールに権限を割り当て
        permissionRepository.assignPermissionToRole(role.getId(), permissionId);
        
        // ユーザーを作成
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .firstName("Test")
                .lastName("User")
                .build();
        User savedUser = userRepository.save(user);
        
        // ユーザーにロールを割り当てない

        // テスト実行
        Optional<Permission> foundPermission = permissionRepository.findByUserIdAndResourceTypeAndActionType(
                savedUser.getId(), ResourceType.USER, ActionType.READ);

        // 検証
        assertThat(foundPermission).isEmpty();
    }
}