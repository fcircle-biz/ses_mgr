package jp.co.example.sesapp.common.auth.repository.jdbc;

import jp.co.example.sesapp.common.auth.domain.Role;
import jp.co.example.sesapp.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JdbcRoleRepositoryのテストクラス
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test-docker")
@Import(JdbcRoleRepositoryTest.TestConfig.class)
@Sql(scripts = {
        "classpath:scripts/cleanup_test_db.sql",
        "classpath:db/testdata/init_auth_schema.sql",
        "classpath:db/testdata/test_users.sql"
})
class JdbcRoleRepositoryTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JdbcRoleRepository jdbcRoleRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
            return new JdbcRoleRepository(jdbcTemplate, namedParameterJdbcTemplate);
        }
    }

    @Autowired
    private JdbcRoleRepository roleRepository;

    private Role testRole;

    @BeforeEach
    void setUp() {
        // テスト用ロールの作成
        testRole = new Role();
        testRole.setName("TEST_ROLE");
        testRole.setDescription("テスト用ロール");
        testRole.setSystemRole(false);
    }

    @Test
    void findById_WhenRoleExists_ShouldReturnRole() {
        // テストデータベースから既存のロールIDを取得
        UUID existingRoleId = UUID.fromString("c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33");
        
        // When
        Optional<Role> found = roleRepository.findById(existingRoleId);
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ADMIN");
        assertThat(found.get().isSystemRole()).isTrue();
    }
    
    @Test
    void findById_WhenRoleDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<Role> found = roleRepository.findById(UUID.randomUUID());
        
        // Then
        assertThat(found).isEmpty();
    }
    
    @Test
    void findByName_WhenRoleExists_ShouldReturnRole() {
        // When
        Optional<Role> found = roleRepository.findByName("ADMIN");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("管理者");
    }
    
    @Test
    void findByName_WhenRoleDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<Role> found = roleRepository.findByName("NONEXISTENT");
        
        // Then
        assertThat(found).isEmpty();
    }
    
    @Test
    void findAll_ShouldReturnAllRoles() {
        // When
        List<Role> roles = roleRepository.findAll();
        
        // Then
        assertThat(roles).isNotEmpty();
        assertThat(roles).hasSize(3); // テストデータに基づく
    }
    
    @Test
    void save_ShouldInsertNewRole() {
        // When
        Role savedRole = roleRepository.save(testRole);
        
        // Then
        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getCreatedAt()).isNotNull();
        
        // 保存したロールを確認
        Optional<Role> found = roleRepository.findByName(testRole.getName());
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo(testRole.getDescription());
    }
    
    @Test
    void save_ShouldUpdateExistingRole() {
        // Given
        UUID existingRoleId = UUID.fromString("c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a35"); // READONLY role (not system role)
        Role existingRole = roleRepository.findById(existingRoleId).get();
        String updatedDescription = "更新された説明";
        existingRole.setDescription(updatedDescription);
        
        // When
        Role updatedRole = roleRepository.save(existingRole);
        
        // Then
        assertThat(updatedRole.getDescription()).isEqualTo(updatedDescription);
        assertThat(updatedRole.getUpdatedAt()).isNotNull();
        
        // 更新を確認
        Role reloadedRole = roleRepository.findById(existingRoleId).get();
        assertThat(reloadedRole.getDescription()).isEqualTo(updatedDescription);
    }
    
    @Test
    void deleteById_WhenRoleExists_AndNotSystemRole_ShouldDeleteRole() {
        // Given
        Role savedRole = roleRepository.save(testRole);
        UUID roleId = savedRole.getId();
        
        // When
        roleRepository.deleteById(roleId);
        
        // Then
        Optional<Role> deleted = roleRepository.findById(roleId);
        assertThat(deleted).isEmpty();
    }
    
    @Test
    void deleteById_WhenRoleIsSystemRole_ShouldThrowException() {
        // Given - テストデータから取得したシステムロール
        UUID systemRoleId = UUID.fromString("c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33"); // ADMIN role (system role)
        
        // When/Then - システムロールは削除できない
        assertThatThrownBy(() -> roleRepository.deleteById(systemRoleId))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Cannot delete system role");
    }
    
    @Test
    void deleteById_WhenRoleDoesNotExist_ShouldThrowException() {
        // When/Then
        UUID nonExistentId = UUID.randomUUID();
        assertThatThrownBy(() -> roleRepository.deleteById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(nonExistentId.toString());
    }
    
    @Test
    void findByUserId_WhenUserExists_ShouldReturnRoles() {
        // Given - テストデータベースから既存のユーザーIDを取得
        UUID existingUserId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"); // admin user
        
        // When
        List<Role> userRoles = roleRepository.findByUserId(existingUserId);
        
        // Then
        assertThat(userRoles).isNotEmpty();
        assertThat(userRoles).hasSize(1); // テストデータに基づく
        assertThat(userRoles.get(0).getName()).isEqualTo("ADMIN");
    }
    
    @Test
    void findByPermissionId_WhenPermissionExists_ShouldReturnRoles() {
        // Given - テストデータベースから既存の権限IDを取得
        UUID existingPermissionId = UUID.fromString("d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44"); // USER_READ
        
        // When
        List<Role> rolesWithPermission = roleRepository.findByPermissionId(existingPermissionId);
        
        // Then
        assertThat(rolesWithPermission).isNotEmpty();
        assertThat(rolesWithPermission).hasSize(3); // テストデータに基づく
    }
    
    @Test
    void assignRoleToUser_ShouldAssignRoleToUser() {
        // Given
        UUID userId = UUID.fromString("a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a12"); // user1
        UUID roleId = UUID.fromString("c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33"); // ADMIN role
        
        // When
        roleRepository.assignRoleToUser(userId, roleId);
        
        // Then
        List<Role> userRoles = roleRepository.findByUserId(userId);
        assertThat(userRoles).extracting("name").contains("ADMIN");
    }
    
    @Test
    void removeRoleFromUser_ShouldRemoveRoleFromUser() {
        // Given
        UUID userId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"); // admin
        UUID roleId = UUID.fromString("c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33"); // ADMIN role
        
        // When
        roleRepository.removeRoleFromUser(userId, roleId);
        
        // Then
        List<Role> userRoles = roleRepository.findByUserId(userId);
        assertThat(userRoles).isEmpty();
    }
}