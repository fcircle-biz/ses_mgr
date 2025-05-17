package jp.co.example.sesapp.common.auth.service.impl;

import jp.co.example.sesapp.common.auth.domain.AuthEventType;
import jp.co.example.sesapp.common.auth.domain.PasswordResetToken;
import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.auth.repository.PasswordResetTokenRepository;
import jp.co.example.sesapp.common.auth.repository.UserRepository;
import jp.co.example.sesapp.common.exception.AuthenticationException;
import jp.co.example.sesapp.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PasswordResetServiceImplTest {

    private UserRepository userRepository;
    private PasswordResetTokenRepository tokenRepository;
    private PasswordEncoder passwordEncoder;
    private PasswordResetServiceImpl passwordResetService;

    private User testUser;
    private PasswordResetToken validToken;
    private PasswordResetToken expiredToken;
    private PasswordResetToken usedToken;

    @BeforeEach
    void setUp() {
        // Create mocks manually
        userRepository = mock(UserRepository.class);
        tokenRepository = mock(PasswordResetTokenRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        
        // Instantiate service manually
        passwordResetService = new PasswordResetServiceImpl(
            userRepository, tokenRepository, passwordEncoder
        );
        
        // Set token expiry minutes
        ReflectionTestUtils.setField(passwordResetService, "tokenExpiryMinutes", 30);

        // Create test user
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .passwordHash("currentHashedPassword")
                .build();

        // Create valid token
        validToken = new PasswordResetToken(
                UUID.randomUUID(),
                testUser.getId(),
                "valid-token",
                LocalDateTime.now().plusMinutes(30)
        );

        // Create expired token
        expiredToken = new PasswordResetToken(
                UUID.randomUUID(),
                testUser.getId(),
                "expired-token",
                LocalDateTime.now().minusMinutes(1)
        );

        // Create used token
        usedToken = new PasswordResetToken(
                UUID.randomUUID(),
                testUser.getId(),
                "used-token",
                LocalDateTime.now().plusMinutes(30)
        );
        usedToken.markAsUsed();
    }

    @Test
    void createPasswordResetToken_ShouldCreateNewToken() {
        // Setup
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tokenRepository.findValidTokenByUserId(testUser.getId())).thenReturn(Optional.empty());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));
        
        // Execute
        PasswordResetToken token = passwordResetService.createPasswordResetToken(testUser.getEmail());

        // Verify token properties
        assertThat(token).isNotNull();
        assertThat(token.getUserId()).isEqualTo(testUser.getId());
        assertThat(token.getToken()).isNotNull().isNotEmpty();
        assertThat(token.getExpiryDate()).isAfter(LocalDateTime.now());
        assertThat(token.isUsed()).isFalse();

        // Verify token was saved
        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
    }

    @Test
    void createPasswordResetToken_WithExistingValidToken_ShouldInvalidateOldToken() {
        // Setup
        PasswordResetToken existingToken = new PasswordResetToken(
                UUID.randomUUID(),
                testUser.getId(),
                "existing-token",
                LocalDateTime.now().plusMinutes(15)
        );
        
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tokenRepository.findValidTokenByUserId(testUser.getId())).thenReturn(Optional.of(existingToken));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));

        // Execute
        PasswordResetToken token = passwordResetService.createPasswordResetToken(testUser.getEmail());

        // Verify existing token was invalidated
        verify(tokenRepository, times(1)).save(existingToken);
        assertThat(existingToken.isUsed()).isTrue();

        // Verify new token was created and saved
        verify(tokenRepository, times(2)).save(any(PasswordResetToken.class));
        assertThat(token).isNotNull();
        assertThat(token.getUserId()).isEqualTo(testUser.getId());
        assertThat(token.getToken()).isNotNull().isNotEmpty();
        assertThat(token.isUsed()).isFalse();
    }

    @Test
    void createPasswordResetToken_UserNotFound_ShouldThrowException() {
        // Setup
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        // Execute and verify
        assertThatThrownBy(() -> passwordResetService.createPasswordResetToken("nonexistent@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        // Verify no token was saved
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    void validatePasswordResetToken_WithValidToken_ShouldReturnTrue() {
        // Setup
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByTokenAndUserId("valid-token", testUser.getId())).thenReturn(Optional.of(validToken));
        
        // Execute
        boolean isValid = passwordResetService.validatePasswordResetToken("valid-token", testUser.getEmail());

        // Verify result
        assertThat(isValid).isTrue();
    }

    @Test
    void validatePasswordResetToken_WithExpiredToken_ShouldReturnFalse() {
        // Setup
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByTokenAndUserId("expired-token", testUser.getId())).thenReturn(Optional.of(expiredToken));
        
        // Execute
        boolean isValid = passwordResetService.validatePasswordResetToken("expired-token", testUser.getEmail());

        // Verify result
        assertThat(isValid).isFalse();
    }

    @Test
    void validatePasswordResetToken_WithUsedToken_ShouldReturnFalse() {
        // Setup
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByTokenAndUserId("used-token", testUser.getId())).thenReturn(Optional.of(usedToken));
        
        // Execute
        boolean isValid = passwordResetService.validatePasswordResetToken("used-token", testUser.getEmail());

        // Verify result
        assertThat(isValid).isFalse();
    }

    @Test
    void validatePasswordResetToken_TokenNotFound_ShouldReturnFalse() {
        // Setup
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByTokenAndUserId("nonexistent-token", testUser.getId())).thenReturn(Optional.empty());

        // Execute
        boolean isValid = passwordResetService.validatePasswordResetToken("nonexistent-token", testUser.getEmail());

        // Verify result
        assertThat(isValid).isFalse();
    }

    @Test
    void validatePasswordResetToken_UserNotFound_ShouldThrowException() {
        // Setup
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Execute and verify
        assertThatThrownBy(() -> passwordResetService.validatePasswordResetToken("valid-token", "nonexistent@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void resetPassword_WithValidToken_ShouldChangePasswordAndInvalidateToken() {
        // Setup
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByTokenAndUserId("valid-token", testUser.getId())).thenReturn(Optional.of(validToken));
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(validToken);

        // Execute
        boolean result = passwordResetService.resetPassword("valid-token", testUser.getEmail(), "newPassword");

        // Verify result
        assertThat(result).isTrue();

        // Verify user password was updated
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPasswordHash()).isEqualTo("newHashedPassword");

        // Verify token was marked as used
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository, times(1)).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.isUsed()).isTrue();
    }

    @Test
    void resetPassword_WithExpiredToken_ShouldThrowException() {
        // Setup
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByTokenAndUserId("expired-token", testUser.getId())).thenReturn(Optional.of(expiredToken));

        // Execute and verify
        assertThatThrownBy(() -> passwordResetService.resetPassword("expired-token", testUser.getEmail(), "newPassword"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Token is expired or has been used");

        // Verify no changes were made
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_WithUsedToken_ShouldThrowException() {
        // Setup
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByTokenAndUserId("used-token", testUser.getId())).thenReturn(Optional.of(usedToken));

        // Execute and verify
        assertThatThrownBy(() -> passwordResetService.resetPassword("used-token", testUser.getEmail(), "newPassword"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Token is expired or has been used");

        // Verify no changes were made
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_TokenNotFound_ShouldThrowException() {
        // Setup
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByTokenAndUserId("nonexistent-token", testUser.getId())).thenReturn(Optional.empty());

        // Execute and verify
        assertThatThrownBy(() -> passwordResetService.resetPassword("nonexistent-token", testUser.getEmail(), "newPassword"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid reset token");

        // Verify no changes were made
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_UserNotFound_ShouldThrowException() {
        // Setup
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Execute and verify
        assertThatThrownBy(() -> passwordResetService.resetPassword("valid-token", "nonexistent@example.com", "newPassword"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        // Verify no changes were made
        verify(userRepository, never()).save(any(User.class));
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    void getUserByResetToken_WithValidToken_ShouldReturnUser() {
        // Setup
        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Execute
        User user = passwordResetService.getUserByResetToken("valid-token");

        // Verify result
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(testUser.getId());
        assertThat(user.getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    void getUserByResetToken_TokenNotFound_ShouldThrowException() {
        // Setup
        when(tokenRepository.findByToken("nonexistent-token")).thenReturn(Optional.empty());

        // Execute and verify
        assertThatThrownBy(() -> passwordResetService.getUserByResetToken("nonexistent-token"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Invalid reset token");
    }

    @Test
    void getUserByResetToken_UserNotFound_ShouldThrowException() {
        // Setup
        UUID orphanedUserId = UUID.randomUUID();
        PasswordResetToken orphanedToken = new PasswordResetToken(
                UUID.randomUUID(),
                orphanedUserId,
                "orphaned-token",
                LocalDateTime.now().plusMinutes(30)
        );
        
        when(tokenRepository.findByToken("orphaned-token")).thenReturn(Optional.of(orphanedToken));
        when(userRepository.findById(orphanedUserId)).thenReturn(Optional.empty());

        // Execute and verify
        assertThatThrownBy(() -> passwordResetService.getUserByResetToken("orphaned-token"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void sendPasswordResetEmail_ShouldLogEmailInfo() {
        // This test verifies that the sendPasswordResetEmail method executes without error
        // Since actual email sending is not implemented, we just check that it runs
        passwordResetService.sendPasswordResetEmail(validToken, testUser.getEmail());
        
        // No verification needed since method doesn't do much in current implementation
    }
}