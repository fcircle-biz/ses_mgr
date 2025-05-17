package jp.co.example.sesapp.common.auth.repository.jdbc;

import jp.co.example.sesapp.common.auth.domain.AuthenticationMethod;
import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
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

@JdbcTest
@Import(JdbcUserRepository.class)
@ActiveProfiles("test")
@Sql(scripts = {"classpath:db/test-data/auth-schema.sql", "classpath:db/test-data/users-test-data.sql"})
class JdbcUserRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private JdbcUserRepository userRepository;

    private User testUser;
    private final UUID testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("newuser");
        testUser.setEmail("newuser@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setFirstName("New");
        testUser.setLastName("User");
        testUser.setEnabled(true);
        testUser.setAccountLocked(false);
        testUser.setLoginFailCount(0);
        testUser.setAuthenticationMethod(AuthenticationMethod.PASSWORD);
        testUser.setMfaEnabled(false);
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        // Act
        Optional<User> foundUser = userRepository.findById(testUserId);

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("testuser@example.com");
    }

    @Test
    void findById_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        UUID nonExistentId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        // Act
        Optional<User> foundUser = userRepository.findById(nonExistentId);

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByUsername_WhenUserExists_ShouldReturnUser() {
        // Act
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(testUserId);
        assertThat(foundUser.get().getEmail()).isEqualTo("testuser@example.com");
    }

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        // Act
        Optional<User> foundUser = userRepository.findByEmail("testuser@example.com");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(testUserId);
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Act
        List<User> users = userRepository.findAll();

        // Assert
        assertThat(users).hasSize(3); // testuser, admin, manager
    }

    @Test
    void save_WhenInsertingNewUser_ShouldInsertAndReturnWithId() {
        // Act
        User savedUser = userRepository.save(testUser);

        // Assert
        assertThat(savedUser.getId()).isNotNull();
        
        // Verify saved data
        Optional<User> retrievedUser = userRepository.findByUsername("newuser");
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getEmail()).isEqualTo("newuser@example.com");
    }

    @Test
    void save_WhenUpdatingExistingUser_ShouldUpdateAndReturn() {
        // Arrange
        Optional<User> existingUser = userRepository.findById(testUserId);
        assertThat(existingUser).isPresent();
        
        User userToUpdate = existingUser.get();
        userToUpdate.setEmail("updated@example.com");
        userToUpdate.setFirstName("Updated");

        // Act
        User updatedUser = userRepository.save(userToUpdate);

        // Assert
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
        
        // Verify updated data
        Optional<User> retrievedUser = userRepository.findById(testUserId);
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getEmail()).isEqualTo("updated@example.com");
        assertThat(retrievedUser.get().getFirstName()).isEqualTo("Updated");
    }

    @Test
    void deleteById_WhenUserExists_ShouldDeleteUser() {
        // Arrange
        Optional<User> existingUser = userRepository.findById(testUserId);
        assertThat(existingUser).isPresent();

        // Act
        userRepository.deleteById(testUserId);

        // Assert
        Optional<User> deletedUser = userRepository.findById(testUserId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void deleteById_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        // Act & Assert
        assertThatThrownBy(() -> userRepository.deleteById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: " + nonExistentId);
    }

    @Test
    void findByLastLoginOlderThan_ShouldReturnUsersWithOlderLogin() {
        // Act
        List<User> inactiveUsers = userRepository.findByLastLoginOlderThan(30);

        // Assert
        assertThat(inactiveUsers).hasSize(1);
        assertThat(inactiveUsers.get(0).getUsername()).isEqualTo("inactiveuser");
    }

    @Test
    void findByPasswordExpired_ShouldReturnUsersWithExpiredPasswords() {
        // Act
        List<User> usersWithExpiredPasswords = userRepository.findByPasswordExpired();

        // Assert
        assertThat(usersWithExpiredPasswords).hasSize(1);
        assertThat(usersWithExpiredPasswords.get(0).getUsername()).isEqualTo("expireduser");
    }

    @Test
    void findByAccountLocked_ShouldReturnLockedUsers() {
        // Act
        List<User> lockedUsers = userRepository.findByAccountLocked();

        // Assert
        assertThat(lockedUsers).hasSize(1);
        assertThat(lockedUsers.get(0).getUsername()).isEqualTo("lockeduser");
    }
}