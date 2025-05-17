package jp.co.example.sesapp.common.auth.service.impl;

import jp.co.example.sesapp.common.auth.domain.PasswordResetToken;
import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.auth.repository.PasswordResetTokenRepository;
import jp.co.example.sesapp.common.auth.repository.UserRepository;
import jp.co.example.sesapp.common.exception.AuthenticationException;
import jp.co.example.sesapp.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    private User testUser;
    private PasswordResetToken validToken;
    private PasswordResetToken expiredToken;
    private PasswordResetToken usedToken;
    private String testEmail;
    private String resetTokenString;
    private final int tokenExpiryMinutes = 30;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        resetTokenString = "resettoken123456";
        
        // テスト用ユーザーを設定
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email(testEmail)
                .name("Test User")
                .passwordHash("hashed_password")
                .build();
        
        // 有効なトークンを設定
        validToken = new PasswordResetToken(
                UUID.randomUUID(),
                testUser.getId(),
                resetTokenString,
                LocalDateTime.now().plusMinutes(15)
        );
        
        // 期限切れトークンを設定
        expiredToken = new PasswordResetToken(
                UUID.randomUUID(),
                testUser.getId(),
                "expired-token",
                LocalDateTime.now().minusMinutes(15)
        );
        
        // 使用済みトークンを設定
        usedToken = new PasswordResetToken(
                UUID.randomUUID(),
                testUser.getId(),
                "used-token",
                LocalDateTime.now().plusMinutes(15)
        );
        usedToken.markAsUsed();

        // トークン有効期限の設定
        ReflectionTestUtils.setField(passwordResetService, "tokenExpiryMinutes", tokenExpiryMinutes);
    }

    @Test
    void createPasswordResetToken_ShouldCreateNewToken() {
        // 準備
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(tokenRepository.findValidTokenByUserId(testUser.getId())).thenReturn(Optional.empty());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));
        
        // 実行
        PasswordResetToken result = passwordResetService.createPasswordResetToken(testEmail);
        
        // 検証
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(testUser.getId());
        assertThat(result.isUsed()).isFalse();
        assertThat(result.getExpiryDate()).isAfter(LocalDateTime.now());
        
        // 有効期限が正しく設定されているか検証
        LocalDateTime expectedExpiryDate = LocalDateTime.now().plusMinutes(tokenExpiryMinutes);
        assertThat(result.getExpiryDate().getYear()).isEqualTo(expectedExpiryDate.getYear());
        assertThat(result.getExpiryDate().getMonth()).isEqualTo(expectedExpiryDate.getMonth());
        assertThat(result.getExpiryDate().getDayOfMonth()).isEqualTo(expectedExpiryDate.getDayOfMonth());
        assertThat(result.getExpiryDate().getHour()).isEqualTo(expectedExpiryDate.getHour());
        assertThat(result.getExpiryDate().getMinute()).isEqualTo(expectedExpiryDate.getMinute());
        
        // リポジトリの呼び出し検証
        verify(userRepository).findByEmail(testEmail);
        verify(tokenRepository).findValidTokenByUserId(testUser.getId());
        verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void createPasswordResetToken_WithExistingValidToken_ShouldInvalidateOldToken() {
        // 準備
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(tokenRepository.findValidTokenByUserId(testUser.getId())).thenReturn(Optional.of(validToken));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));
        
        // 実行
        PasswordResetToken result = passwordResetService.createPasswordResetToken(testEmail);
        
        // 検証
        assertThat(result).isNotNull();
        
        // 古いトークンが使用済みになっているか検証
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository, times(2)).save(tokenCaptor.capture());
        
        // 1回目の保存は古いトークンの無効化
        assertThat(tokenCaptor.getAllValues().get(0).isUsed()).isTrue();
        // 2回目の保存は新しいトークンの作成
        assertThat(tokenCaptor.getAllValues().get(1).isUsed()).isFalse();
        
        // リポジトリの呼び出し検証
        verify(userRepository).findByEmail(testEmail);
        verify(tokenRepository).findValidTokenByUserId(testUser.getId());
    }

    @Test
    void createPasswordResetToken_UserNotFound_ShouldThrowException() {
        // 準備
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        
        // 実行 & 検証
        Throwable thrown = catchThrowable(() -> passwordResetService.createPasswordResetToken(testEmail));
        
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                          .hasMessageContaining("User not found");
        
        // リポジトリの呼び出し検証
        verify(userRepository).findByEmail(testEmail);
        verify(tokenRepository, never()).findValidTokenByUserId(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void validatePasswordResetToken_WithValidToken_ShouldReturnTrue() {
        // 準備
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByTokenAndUserId(resetTokenString, testUser.getId()))
                .thenReturn(Optional.of(validToken));
        
        // 実行
        boolean result = passwordResetService.validatePasswordResetToken(resetTokenString, testEmail);
        
        // 検証
        assertThat(result).isTrue();
        
        // リポジトリの呼び出し検証
        verify(userRepository).findByEmail(testEmail);
        verify(tokenRepository).findByTokenAndUserId(resetTokenString, testUser.getId());
    }

    @Test
    void validatePasswordResetToken_WithExpiredToken_ShouldReturnFalse() {
        // 準備
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByTokenAndUserId("expired-token", testUser.getId()))
                .thenReturn(Optional.of(expiredToken));
        
        // 実行
        boolean result = passwordResetService.validatePasswordResetToken("expired-token", testEmail);
        
        // 検証
        assertThat(result).isFalse();
        
        // リポジトリの呼び出し検証
        verify(userRepository).findByEmail(testEmail);
        verify(tokenRepository).findByTokenAndUserId("expired-token", testUser.getId());
    }

    @Test
    void validatePasswordResetToken_WithUsedToken_ShouldReturnFalse() {
        // 準備
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByTokenAndUserId("used-token", testUser.getId()))
                .thenReturn(Optional.of(usedToken));
        
        // 実行
        boolean result = passwordResetService.validatePasswordResetToken("used-token", testEmail);
        
        // 検証
        assertThat(result).isFalse();
        
        // リポジトリの呼び出し検証
        verify(userRepository).findByEmail(testEmail);
        verify(tokenRepository).findByTokenAndUserId("used-token", testUser.getId());
    }

    @Test
    void validatePasswordResetToken_TokenNotFound_ShouldReturnFalse() {
        // 準備
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByTokenAndUserId(resetTokenString, testUser.getId()))
                .thenReturn(Optional.empty());
        
        // 実行
        boolean result = passwordResetService.validatePasswordResetToken(resetTokenString, testEmail);
        
        // 検証
        assertThat(result).isFalse();
        
        // リポジトリの呼び出し検証
        verify(userRepository).findByEmail(testEmail);
        verify(tokenRepository).findByTokenAndUserId(resetTokenString, testUser.getId());
    }

    @Test
    void validatePasswordResetToken_UserNotFound_ShouldThrowException() {
        // 準備
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        
        // 実行 & 検証
        Throwable thrown = catchThrowable(() -> 
                passwordResetService.validatePasswordResetToken(resetTokenString, testEmail));
        
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                          .hasMessageContaining("User not found");
        
        // リポジトリの呼び出し検証
        verify(userRepository).findByEmail(testEmail);
        verify(tokenRepository, never()).findByTokenAndUserId(any(), any());
    }

    @Test
    void resetPassword_WithValidToken_ShouldChangePasswordAndInvalidateToken() {
        // 準備
        String newPassword = "newPassword123";
        String encodedPassword = "encoded_password";
        
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByTokenAndUserId(resetTokenString, testUser.getId()))
                .thenReturn(Optional.of(validToken));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(validToken);
        
        // 実行
        boolean result = passwordResetService.resetPassword(resetTokenString, testEmail, newPassword);
        
        // 検証
        assertThat(result).isTrue();
        
        // ユーザーのパスワードが更新されているか検証
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo(encodedPassword);
        
        // トークンが使用済みになっているか検証
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().isUsed()).isTrue();
        
        // リポジトリの呼び出し検証
        verify(userRepository).findByEmail(testEmail);
        verify(tokenRepository).findByTokenAndUserId(resetTokenString, testUser.getId());
        verify(passwordEncoder).encode(newPassword);
    }

    @Test
    void resetPassword_WithExpiredToken_ShouldThrowException() {
        // 準備
        String newPassword = "newPassword123";
        
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByTokenAndUserId("expired-token", testUser.getId()))
                .thenReturn(Optional.of(expiredToken));
        
        // 実行 & 検証
        Throwable thrown = catchThrowable(() -> 
                passwordResetService.resetPassword("expired-token", testEmail, newPassword));
        
        assertThat(thrown).isInstanceOf(AuthenticationException.class)
                          .hasMessageContaining("Token is expired");
        
        // リポジトリの呼び出し検証
        verify(userRepository).findByEmail(testEmail);
        verify(tokenRepository).findByTokenAndUserId("expired-token", testUser.getId());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_WithUsedToken_ShouldThrowException() {
        // 準備
        String newPassword = "newPassword123";
        
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByTokenAndUserId("used-token", testUser.getId()))
                .thenReturn(Optional.of(usedToken));
        
        // 実行 & 検証
        Throwable thrown = catchThrowable(() -> 
                passwordResetService.resetPassword("used-token", testEmail, newPassword));
        
        assertThat(thrown).isInstanceOf(AuthenticationException.class)
                          .hasMessageContaining("Token is expired or has been used");
        
        // リポジトリの呼び出し検証
        verify(userRepository).findByEmail(testEmail);
        verify(tokenRepository).findByTokenAndUserId("used-token", testUser.getId());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_TokenNotFound_ShouldThrowException() {
        // 準備
        String newPassword = "newPassword123";
        
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByTokenAndUserId(resetTokenString, testUser.getId()))
                .thenReturn(Optional.empty());
        
        // 実行 & 検証
        Throwable thrown = catchThrowable(() -> 
                passwordResetService.resetPassword(resetTokenString, testEmail, newPassword));
        
        assertThat(thrown).isInstanceOf(AuthenticationException.class)
                          .hasMessageContaining("Invalid reset token");
        
        // リポジトリの呼び出し検証
        verify(userRepository).findByEmail(testEmail);
        verify(tokenRepository).findByTokenAndUserId(resetTokenString, testUser.getId());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_UserNotFound_ShouldThrowException() {
        // 準備
        String newPassword = "newPassword123";
        
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        
        // 実行 & 検証
        Throwable thrown = catchThrowable(() -> 
                passwordResetService.resetPassword(resetTokenString, testEmail, newPassword));
        
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                          .hasMessageContaining("User not found");
        
        // リポジトリの呼び出し検証
        verify(userRepository).findByEmail(testEmail);
        verify(tokenRepository, never()).findByTokenAndUserId(any(), any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserByResetToken_WithValidToken_ShouldReturnUser() {
        // 準備
        when(tokenRepository.findByToken(resetTokenString)).thenReturn(Optional.of(validToken));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        
        // 実行
        User result = passwordResetService.getUserByResetToken(resetTokenString);
        
        // 検証
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUser.getId());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        
        // リポジトリの呼び出し検証
        verify(tokenRepository).findByToken(resetTokenString);
        verify(userRepository).findById(testUser.getId());
    }

    @Test
    void getUserByResetToken_TokenNotFound_ShouldThrowException() {
        // 準備
        when(tokenRepository.findByToken(resetTokenString)).thenReturn(Optional.empty());
        
        // 実行 & 検証
        Throwable thrown = catchThrowable(() -> passwordResetService.getUserByResetToken(resetTokenString));
        
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                          .hasMessageContaining("Invalid reset token");
        
        // リポジトリの呼び出し検証
        verify(tokenRepository).findByToken(resetTokenString);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getUserByResetToken_UserNotFound_ShouldThrowException() {
        // 準備
        when(tokenRepository.findByToken(resetTokenString)).thenReturn(Optional.of(validToken));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        
        // 実行 & 検証
        Throwable thrown = catchThrowable(() -> passwordResetService.getUserByResetToken(resetTokenString));
        
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                          .hasMessageContaining("User not found");
        
        // リポジトリの呼び出し検証
        verify(tokenRepository).findByToken(resetTokenString);
        verify(userRepository).findById(testUser.getId());
    }

    @Test
    void sendPasswordResetEmail_ShouldLogMessage() {
        // テストは実装のみを確認（実際のメール送信はモック）
        passwordResetService.sendPasswordResetEmail(validToken, testEmail);
        
        // 実際の実装ではここでメール送信サービスの呼び出しを検証する
        // 現状の実装はログ出力のみなので、特に検証は行わない
    }
}