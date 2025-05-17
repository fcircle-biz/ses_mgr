package jp.co.example.sesapp.common.auth.repository.jdbc;

import jp.co.example.sesapp.common.auth.domain.AuthenticationMethod;
import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JdbcUserRepositoryのテストクラス
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test-docker")
@Import(JdbcUserRepositoryTest.TestConfig.class)
@Sql(scripts = {
        "classpath:scripts/cleanup_test_db.sql",
        "classpath:db/testdata/init_auth_schema.sql",
        "classpath:db/testdata/test_users.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JdbcUserRepositoryTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JdbcUserRepository jdbcUserRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
            return new JdbcUserRepository(jdbcTemplate, namedParameterJdbcTemplate);
        }
    }

    @Autowired
    private JdbcUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // テスト用ユーザーの作成
        testUser = User.builder()
                .username("testuser")
                .email("testuser@example.com")
                .passwordHash("password_hash")
                .firstName("Test")
                .lastName("User")
                .enabled(true)
                .authenticationMethod(AuthenticationMethod.PASSWORD)
                .build();
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        // テストデータベースから既存のユーザーIDを取得
        UUID existingUserId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
        
        // When
        Optional<User> found = userRepository.findById(existingUserId);
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("admin");
        assertThat(found.get().getEmail()).isEqualTo("admin@example.com");
    }
    
    @Test
    void findById_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository.findById(UUID.randomUUID());
        
        // Then
        assertThat(found).isEmpty();
    }
    
    @Test
    void findByUsername_WhenUserExists_ShouldReturnUser() {
        // When
        Optional<User> found = userRepository.findByUsername("admin");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("admin@example.com");
    }
    
    @Test
    void findByUsername_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository.findByUsername("nonexistent");
        
        // Then
        assertThat(found).isEmpty();
    }
    
    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        // When
        Optional<User> found = userRepository.findByEmail("admin@example.com");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("admin");
    }
    
    @Test
    void findByEmail_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");
        
        // Then
        assertThat(found).isEmpty();
    }
    
    @Test
    void findAll_ShouldReturnAllUsers() {
        // When
        List<User> users = userRepository.findAll();
        
        // Then
        assertThat(users).isNotEmpty();
        assertThat(users).hasSize(3); // テストデータに基づく
    }
    
    @Test
    void save_ShouldInsertNewUser() {
        // When
        User savedUser = userRepository.save(testUser);
        
        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        
        // 保存したユーザーを確認
        Optional<User> found = userRepository.findByEmail(testUser.getEmail());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(testUser.getUsername());
    }
    
    @Test
    void save_ShouldUpdateExistingUser() {
        // Given
        UUID existingUserId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
        User existingUser = userRepository.findById(existingUserId).get();
        String updatedFirstName = "UpdatedFirstName";
        existingUser.setFirstName(updatedFirstName);
        
        // When
        User updatedUser = userRepository.save(existingUser);
        
        // Then
        assertThat(updatedUser.getFirstName()).isEqualTo(updatedFirstName);
        assertThat(updatedUser.getUpdatedAt()).isNotNull();
        
        // 更新を確認
        User reloadedUser = userRepository.findById(existingUserId).get();
        assertThat(reloadedUser.getFirstName()).isEqualTo(updatedFirstName);
    }
    
    @Test
    void deleteById_WhenUserExists_ShouldDeleteUser() {
        // Given
        User savedUser = userRepository.save(testUser);
        UUID userId = savedUser.getId();
        
        // When
        userRepository.deleteById(userId);
        
        // Then
        Optional<User> deleted = userRepository.findById(userId);
        assertThat(deleted).isEmpty();
    }
    
    @Test
    void deleteById_WhenUserDoesNotExist_ShouldThrowException() {
        // When/Then
        UUID nonExistentId = UUID.randomUUID();
        assertThatThrownBy(() -> userRepository.deleteById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(nonExistentId.toString());
    }
    
    @Test
    void findByLastLoginOlderThan_ShouldReturnMatchingUsers() {
        // 現在の日付から7日前のユーザーを検索
        List<User> inactiveUsers = userRepository.findByLastLoginOlderThan(7);
        
        // テストデータに基づいて検証
        assertThat(inactiveUsers).isNotEmpty();
    }
    
    @Test
    void findByPasswordExpired_ShouldReturnUsersWithExpiredPasswords() {
        // パスワードが期限切れのユーザーを検索
        List<User> usersWithExpiredPasswords = userRepository.findByPasswordExpired();
        
        // テストデータに基づいて検証
        assertThat(usersWithExpiredPasswords).isNotEmpty();
    }
    
    @Test
    void findByAccountLocked_ShouldReturnLockedUsers() {
        // アカウントがロックされているユーザーを検索
        List<User> lockedUsers = userRepository.findByAccountLocked();
        
        // テストデータに基づいて検証
        assertThat(lockedUsers).isNotEmpty();
    }
    
    @Test
    void findByDepartmentId_WhenDepartmentExists_ShouldReturnUsers() {
        // Given
        UUID departmentId = UUID.fromString("b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22");
        
        // When
        List<User> usersByDepartment = userRepository.findByDepartmentId(departmentId);
        
        // Then
        // テストデータに基づいて検証
        assertThat(usersByDepartment).isNotEmpty();
    }
    
    @Test
    void findByRoleId_WhenRoleExists_ShouldReturnUsers() {
        // Given - テストデータベースからロールIDを取得
        UUID roleId = UUID.fromString("c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33");
        
        // When
        List<User> usersByRole = userRepository.findByRoleId(roleId);
        
        // Then
        // テストデータに基づいて検証
        assertThat(usersByRole).isNotEmpty();
    }
}