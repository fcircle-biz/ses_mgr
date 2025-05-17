package jp.co.example.sesapp.common.auth.repository.jdbc;

import jp.co.example.sesapp.common.auth.domain.Role;
import jp.co.example.sesapp.common.auth.domain.User;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 認証リポジトリの統合テストクラス
 * このテストクラスは複数のリポジトリ間の連携を検証します
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test-docker")
@Import(RepositoriesIntegrationTest.TestConfig.class)
@Sql(scripts = {
        "classpath:scripts/cleanup_test_db.sql",
        "classpath:db/testdata/init_auth_schema.sql",
        "classpath:db/testdata/test_users.sql"
})
class RepositoriesIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JdbcUserRepository jdbcUserRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
            return new JdbcUserRepository(jdbcTemplate, namedParameterJdbcTemplate);
        }

        @Bean
        public JdbcRoleRepository jdbcRoleRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
            return new JdbcRoleRepository(jdbcTemplate, namedParameterJdbcTemplate);
        }

        @Bean
        public JdbcPermissionRepository jdbcPermissionRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
            return new JdbcPermissionRepository(jdbcTemplate, namedParameterJdbcTemplate);
        }

        @Bean
        public JdbcRefreshTokenRepository jdbcRefreshTokenRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
            return new JdbcRefreshTokenRepository(jdbcTemplate, namedParameterJdbcTemplate);
        }

        @Bean
        public JdbcPasswordResetTokenRepository jdbcPasswordResetTokenRepository(DataSource dataSource) {
            return new JdbcPasswordResetTokenRepository(dataSource);
        }
    }

    @Autowired
    private JdbcUserRepository userRepository;

    @Autowired
    private JdbcRoleRepository roleRepository;

    /**
     * ユーザーとロールの関連付けテスト
     */
    @Test
    void userRoleAssociation_ShouldWorkCorrectly() {
        // Given
        User newUser = User.builder()
                .username("integrationtest")
                .email("integration@example.com")
                .passwordHash("password_hash")
                .firstName("Integration")
                .lastName("Test")
                .enabled(true)
                .build();

        Role newRole = new Role();
        newRole.setName("INTEGRATION_ROLE");
        newRole.setDescription("統合テスト用ロール");
        newRole.setSystemRole(false);

        // When
        User savedUser = userRepository.save(newUser);
        Role savedRole = roleRepository.save(newRole);

        // ユーザーにロールを割り当て
        roleRepository.assignRoleToUser(savedUser.getId(), savedRole.getId());

        // Then
        List<Role> userRoles = roleRepository.findByUserId(savedUser.getId());
        assertThat(userRoles).isNotEmpty();
        assertThat(userRoles).extracting("name").contains("INTEGRATION_ROLE");

        // クリーンアップ
        roleRepository.removeRoleFromUser(savedUser.getId(), savedRole.getId());
        roleRepository.deleteById(savedRole.getId());
        userRepository.deleteById(savedUser.getId());
    }

    /**
     * 既存のユーザーとロールの関連を検証するテスト
     */
    @Test
    void existingUserRoleAssociation_ShouldBeCorrect() {
        // Given - テストデータから既存のユーザーとロールを取得
        UUID adminUserId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
        UUID adminRoleId = UUID.fromString("c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33");

        // When
        Optional<User> adminUser = userRepository.findById(adminUserId);
        Optional<Role> adminRole = roleRepository.findById(adminRoleId);
        List<Role> adminUserRoles = roleRepository.findByUserId(adminUserId);

        // Then
        assertThat(adminUser).isPresent();
        assertThat(adminRole).isPresent();
        assertThat(adminUserRoles).isNotEmpty();
        assertThat(adminUserRoles.get(0).getId()).isEqualTo(adminRole.get().getId());
    }

    /**
     * ロックされたユーザーの状態を検証するテスト
     */
    @Test
    void lockedUser_ShouldBeIdentifiable() {
        // Given - テストデータからロックされたユーザーを取得
        UUID lockedUserId = UUID.fromString("a2eebc99-9c0b-4ef8-bb6d-6bb9bd380a13");

        // When
        Optional<User> lockedUser = userRepository.findById(lockedUserId);
        List<User> lockedUsers = userRepository.findByAccountLocked();

        // Then
        assertThat(lockedUser).isPresent();
        assertThat(lockedUser.get().isAccountLocked()).isTrue();
        assertThat(lockedUsers).isNotEmpty();
        assertThat(lockedUsers).extracting("id").contains(lockedUserId);
    }
}