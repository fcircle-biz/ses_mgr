package jp.co.example.sesapp.common.auth.service.impl;

import jp.co.example.sesapp.common.auth.domain.AuthEventType;
import jp.co.example.sesapp.common.auth.domain.RefreshToken;
import jp.co.example.sesapp.common.auth.domain.Role;
import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.auth.domain.dto.*;
import jp.co.example.sesapp.common.auth.repository.RefreshTokenRepository;
import jp.co.example.sesapp.common.auth.repository.RoleRepository;
import jp.co.example.sesapp.common.auth.repository.UserRepository;
import jp.co.example.sesapp.common.auth.service.JwtProvider;
import jp.co.example.sesapp.common.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private MfaService mfaService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User testUser;
    private Role testRole;
    private Credentials validCredentials;
    private RefreshToken validRefreshToken;

    @BeforeEach
    void setUp() {
        // テストユーザーの設定
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("testuser@example.com");
        testUser.setName("Test User");
        testUser.setPasswordHash("hashedpassword");
        testUser.setDepartment("Test Department");
        testUser.setPosition("Test Position");
        testUser.setPhone("123-456-7890");
        testUser.setRoleId(UUID.randomUUID());
        testUser.setAccountLocked(false);
        testUser.setLoginFailCount(0);
        testUser.setMfaEnabled(false);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        // テストユーザーのロール
        testRole = new Role();
        testRole.setId(testUser.getRoleId());
        testRole.setName("USER");
        testRole.setDescription("User role");

        // 有効な認証情報
        validCredentials = new Credentials();
        validCredentials.setUsernameOrEmail("testuser@example.com");
        validCredentials.setPassword("password");
        validCredentials.setRememberMe(false);

        // 有効なリフレッシュトークン
        validRefreshToken = new RefreshToken();
        validRefreshToken.setId(UUID.randomUUID());
        validRefreshToken.setUserId(testUser.getId());
        validRefreshToken.setToken("valid-refresh-token");
        validRefreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        validRefreshToken.setDeviceInfo("test-device");
        validRefreshToken.setDeviceId("device-id");
        validRefreshToken.setIpAddress("127.0.0.1");
        validRefreshToken.setIssuedAt(LocalDateTime.now());
        validRefreshToken.setLastUsedAt(LocalDateTime.now());
        validRefreshToken.setRevoked(false);

        // 設定値をセット
        ReflectionTestUtils.setField(authenticationService, "refreshTokenDurationMs", 604800000L); // 7日間
        ReflectionTestUtils.setField(authenticationService, "maxFailedAttempts", 5);
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnTokens() {
        // モックの設定
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "hashedpassword")).thenReturn(true);
        when(roleRepository.findById(testUser.getRoleId())).thenReturn(Optional.of(testRole));
        when(jwtProvider.generateAccessToken(eq(testUser), anyList())).thenReturn("access-token");
        when(jwtProvider.generateRefreshToken(testUser)).thenReturn("refresh-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        // テスト対象メソッドの実行
        AuthenticationResponse response = authenticationService.authenticate(validCredentials);

        // 検証
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.isRequiresMfa()).isFalse();
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo(testUser.getEmail());

        verify(userRepository).save(testUser);
        verify(jwtProvider).generateAccessToken(eq(testUser), anyList());
        verify(jwtProvider).generateRefreshToken(testUser);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void authenticate_WithInvalidPassword_ShouldThrowException() {
        // モックの設定
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "hashedpassword")).thenReturn(false);

        // テスト対象メソッドの実行と検証
        assertThatThrownBy(() -> authenticationService.authenticate(validCredentials))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid username/email or password");

        verify(userRepository).save(testUser); // ログイン失敗カウントが増加したことを確認
    }

    @Test
    void authenticate_WhenAccountLocked_ShouldThrowException() {
        // テストデータの設定
        testUser.setAccountLocked(true);

        // モックの設定
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(testUser));

        // テスト対象メソッドの実行と検証
        assertThatThrownBy(() -> authenticationService.authenticate(validCredentials))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Account is locked");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_WhenAccountExpired_ShouldThrowException() {
        // テストデータの設定
        testUser.setAccountExpiresAt(LocalDateTime.now().minusDays(1));

        // モックの設定
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(testUser));

        // テスト対象メソッドの実行と検証
        assertThatThrownBy(() -> authenticationService.authenticate(validCredentials))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Account has expired");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_WhenCredentialsExpired_ShouldThrowException() {
        // テストデータの設定
        testUser.setPasswordExpiresAt(LocalDateTime.now().minusDays(1));

        // モックの設定
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(testUser));

        // テスト対象メソッドの実行と検証
        assertThatThrownBy(() -> authenticationService.authenticate(validCredentials))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Credentials have expired");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_WhenMfaEnabled_ShouldReturnMfaChallenge() {
        // テストデータの設定
        testUser.setMfaEnabled(true);
        testUser.setMfaSecret("mfa-secret");

        // モックの設定
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "hashedpassword")).thenReturn(true);

        // テスト対象メソッドの実行
        AuthenticationResponse response = authenticationService.authenticate(validCredentials);

        // 検証
        assertThat(response).isNotNull();
        assertThat(response.isRequiresMfa()).isTrue();
        assertThat(response.getMfaChallenge()).isNotNull();
        assertThat(response.getMfaChallenge().getChallengeId()).isNotNull();
        assertThat(response.getAccessToken()).isNull(); // MFA中はアクセストークンがない

        verify(userRepository).save(testUser); // ログイン成功カウントをリセット
    }

    @Test
    void authenticate_WhenMaxFailedAttemptsReached_ShouldLockAccount() {
        // テストデータの設定
        testUser.setLoginFailCount(4); // あと1回で上限に達する

        // モックの設定
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "hashedpassword")).thenReturn(false);

        // テスト対象メソッドの実行と検証
        assertThatThrownBy(() -> authenticationService.authenticate(validCredentials))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Account locked due to too many failed attempts");

        verify(userRepository, times(2)).save(testUser); // 失敗カウント増加とアカウントロックで2回
        assertThat(testUser.isAccountLocked()).isTrue(); // アカウントがロックされたことを確認
    }

    @Test
    void verifyMfaCode_WhenValidCode_ShouldReturnTokens() {
        // テストデータの設定
        testUser.setMfaEnabled(true);
        testUser.setMfaSecret("mfa-secret");
        MfaVerificationRequest request = new MfaVerificationRequest();
        request.setCode("123456");

        // モックの設定
        when(mfaService.verifyCode("mfa-secret", "123456")).thenReturn(true);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(testUser.getRoleId())).thenReturn(Optional.of(testRole));
        when(jwtProvider.generateAccessToken(eq(testUser), anyList())).thenReturn("access-token");
        when(jwtProvider.generateRefreshToken(testUser)).thenReturn("refresh-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        // テスト対象メソッドの実行
        AuthenticationResponse response = authenticationService.verifyMfaCode(request);

        // 検証
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.isRequiresMfa()).isFalse();
        
        verify(mfaService).verifyCode("mfa-secret", "123456");
        verify(jwtProvider).generateAccessToken(eq(testUser), anyList());
        verify(jwtProvider).generateRefreshToken(testUser);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void verifyMfaCode_WhenInvalidCode_ShouldThrowException() {
        // テストデータの設定
        testUser.setMfaEnabled(true);
        testUser.setMfaSecret("mfa-secret");
        MfaVerificationRequest request = new MfaVerificationRequest();
        request.setCode("invalid");

        // モックの設定
        when(mfaService.verifyCode("mfa-secret", "invalid")).thenReturn(false);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));

        // テスト対象メソッドの実行と検証
        assertThatThrownBy(() -> authenticationService.verifyMfaCode(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid MFA code");
        
        verify(mfaService).verifyCode("mfa-secret", "invalid");
        verify(jwtProvider, never()).generateAccessToken(any(), anyList());
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewTokens() {
        // モックの設定
        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(validRefreshToken));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(testUser.getRoleId())).thenReturn(Optional.of(testRole));
        when(jwtProvider.generateAccessToken(eq(testUser), anyList())).thenReturn("new-access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        // テスト対象メソッドの実行
        AuthenticationResponse response = authenticationService.refreshToken("valid-refresh-token");

        // 検証
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        
        verify(refreshTokenRepository).save(validRefreshToken); // 最終使用日時が更新されることを確認
        verify(jwtProvider).generateAccessToken(eq(testUser), anyList());
    }

    @Test
    void refreshToken_WithExpiredToken_ShouldThrowException() {
        // テストデータの設定
        validRefreshToken.setExpiryDate(LocalDateTime.now().minusDays(1));

        // モックの設定
        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(validRefreshToken));

        // テスト対象メソッドの実行と検証
        assertThatThrownBy(() -> authenticationService.refreshToken("valid-refresh-token"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Refresh token expired");

        verify(refreshTokenRepository).save(validRefreshToken); // トークンが無効化されたことを確認
        assertThat(validRefreshToken.isRevoked()).isTrue();
        assertThat(validRefreshToken.getRevokedReason()).isEqualTo("Expired");
    }

    @Test
    void refreshToken_WithRevokedToken_ShouldThrowException() {
        // テストデータの設定
        validRefreshToken.setRevoked(true);
        validRefreshToken.setRevokedReason("Already used");

        // モックの設定
        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(validRefreshToken));

        // テスト対象メソッドの実行と検証
        assertThatThrownBy(() -> authenticationService.refreshToken("valid-refresh-token"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Refresh token is revoked");
        
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void logout_ShouldRevokeRefreshToken() {
        // モックの設定
        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(validRefreshToken));

        // テスト対象メソッドの実行
        authenticationService.logout("valid-refresh-token");

        // 検証
        verify(refreshTokenRepository).save(validRefreshToken);
        assertThat(validRefreshToken.isRevoked()).isTrue();
        assertThat(validRefreshToken.getRevokedReason()).isEqualTo("User logout");
    }

    @Test
    void logoutAllDevices_ShouldRevokeAllUserTokens() {
        // テストデータの設定
        RefreshToken token1 = new RefreshToken();
        token1.setUserId(testUser.getId());
        token1.setRevoked(false);

        RefreshToken token2 = new RefreshToken();
        token2.setUserId(testUser.getId());
        token2.setRevoked(false);

        List<RefreshToken> userTokens = Arrays.asList(token1, token2);

        // モックの設定
        when(refreshTokenRepository.findByUserId(testUser.getId())).thenReturn(userTokens);

        // テスト対象メソッドの実行
        authenticationService.logoutAllDevices(testUser.getId());

        // 検証
        verify(refreshTokenRepository).save(token1);
        verify(refreshTokenRepository).save(token2);
        assertThat(token1.isRevoked()).isTrue();
        assertThat(token2.isRevoked()).isTrue();
        assertThat(token1.getRevokedReason()).isEqualTo("Logout from all devices");
        assertThat(token2.getRevokedReason()).isEqualTo("Logout from all devices");
    }

    @Test
    void setupMfa_ShouldGenerateSecretAndQrCode() {
        // テストデータの設定
        MfaSetupResponse mfaSetupResponse = new MfaSetupResponse();
        mfaSetupResponse.setSecret("generated-secret");
        mfaSetupResponse.setQrCodeUri("otpauth://totp/SESManager:testuser@example.com?secret=generated-secret&issuer=SESManager");

        // モックの設定
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(mfaService.generateMfaSetup(testUser)).thenReturn(mfaSetupResponse);
        when(userRepository.save(testUser)).thenReturn(testUser);

        // テスト対象メソッドの実行
        MfaSetupResponse response = authenticationService.setupMfa(testUser.getId());

        // 検証
        assertThat(response).isNotNull();
        assertThat(response.getSecret()).isEqualTo("generated-secret");
        assertThat(response.getQrCodeUri()).contains("otpauth://totp");
        
        verify(mfaService).generateMfaSetup(testUser);
        verify(userRepository).save(testUser);
        assertThat(testUser.getMfaSecret()).isEqualTo("generated-secret");
        assertThat(testUser.isMfaEnabled()).isFalse(); // まだ検証完了していないので有効化されていない
    }

    @Test
    void verifyAndEnableMfa_WithValidCode_ShouldEnableMfa() {
        // テストデータの設定
        testUser.setMfaSecret("mfa-secret");
        testUser.setMfaEnabled(false);

        // モックの設定
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(mfaService.verifyCode("mfa-secret", "123456")).thenReturn(true);
        when(userRepository.save(testUser)).thenReturn(testUser);

        // テスト対象メソッドの実行
        authenticationService.verifyAndEnableMfa(testUser.getId(), "123456");

        // 検証
        verify(mfaService).verifyCode("mfa-secret", "123456");
        verify(userRepository).save(testUser);
        assertThat(testUser.isMfaEnabled()).isTrue(); // MFAが有効化されたことを確認
    }

    @Test
    void verifyAndEnableMfa_WithInvalidCode_ShouldThrowException() {
        // テストデータの設定
        testUser.setMfaSecret("mfa-secret");
        testUser.setMfaEnabled(false);

        // モックの設定
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(mfaService.verifyCode("mfa-secret", "invalid")).thenReturn(false);

        // テスト対象メソッドの実行と検証
        assertThatThrownBy(() -> authenticationService.verifyAndEnableMfa(testUser.getId(), "invalid"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid MFA code");
        
        verify(mfaService).verifyCode("mfa-secret", "invalid");
        verify(userRepository, never()).save(any(User.class));
        assertThat(testUser.isMfaEnabled()).isFalse(); // MFAが有効化されていないことを確認
    }

    @Test
    void disableMfa_WithValidPassword_ShouldDisableMfa() {
        // テストデータの設定
        testUser.setMfaSecret("mfa-secret");
        testUser.setMfaEnabled(true);

        // モックの設定
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "hashedpassword")).thenReturn(true);
        when(userRepository.save(testUser)).thenReturn(testUser);

        // テスト対象メソッドの実行
        authenticationService.disableMfa(testUser.getId(), "password");

        // 検証
        verify(passwordEncoder).matches("password", "hashedpassword");
        verify(userRepository).save(testUser);
        assertThat(testUser.isMfaEnabled()).isFalse(); // MFAが無効化されたことを確認
        assertThat(testUser.getMfaSecret()).isNull(); // シークレットがクリアされたことを確認
    }

    @Test
    void disableMfa_WithInvalidPassword_ShouldThrowException() {
        // テストデータの設定
        testUser.setMfaSecret("mfa-secret");
        testUser.setMfaEnabled(true);

        // モックの設定
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong-password", "hashedpassword")).thenReturn(false);

        // テスト対象メソッドの実行と検証
        assertThatThrownBy(() -> authenticationService.disableMfa(testUser.getId(), "wrong-password"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid password");
        
        verify(passwordEncoder).matches("wrong-password", "hashedpassword");
        verify(userRepository, never()).save(any(User.class));
        assertThat(testUser.isMfaEnabled()).isTrue(); // MFAが有効なままであることを確認
        assertThat(testUser.getMfaSecret()).isNotNull(); // シークレットが残っていることを確認
    }
}