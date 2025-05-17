package jp.co.example.sesapp.common.auth.service.impl;

import jp.co.example.sesapp.common.auth.domain.AuthEventType;
import jp.co.example.sesapp.common.auth.domain.AuthenticationMethod;
import jp.co.example.sesapp.common.auth.domain.RefreshToken;
import jp.co.example.sesapp.common.auth.domain.Role;
import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.auth.domain.dto.*;
import jp.co.example.sesapp.common.auth.repository.RefreshTokenRepository;
import jp.co.example.sesapp.common.auth.repository.RoleRepository;
import jp.co.example.sesapp.common.auth.repository.UserRepository;
import jp.co.example.sesapp.common.auth.service.AuthenticationService;
import jp.co.example.sesapp.common.auth.service.JwtProvider;
import jp.co.example.sesapp.common.exception.AuthenticationException;
import jp.co.example.sesapp.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 認証サービスの実装クラス
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final MfaService mfaService;

    @Value("${application.security.jwt.refresh-token-expiration}")
    private long refreshTokenDurationMs;

    @Value("${application.security.account.max-failed-attempts}")
    private int maxFailedAttempts;

    public AuthenticationServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider,
            MfaService mfaService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.mfaService = mfaService;
    }

    @Override
    @Transactional
    public AuthenticationResponse authenticate(Credentials credentials) {
        return authenticate(credentials, null, null, null);
    }
    
    /**
     * 拡張認証メソッド - デバイス情報とIPアドレス付き
     */
    @Transactional
    public AuthenticationResponse authenticate(Credentials credentials, String deviceId, String deviceInfo, String ipAddress) {
        // ユーザー名またはメールでユーザーを検索
        User user = findUserByUsernameOrEmail(credentials.getUsernameOrEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid username/email or password", AuthEventType.LOGIN_FAILED));

        // アカウントの状態チェック
        validateUserAccountStatus(user);

        // パスワードの検証
        if (!passwordEncoder.matches(credentials.getPassword(), user.getPasswordHash())) {
            // ログイン失敗をカウント
            user.incrementLoginFailCount();
            
            // 最大失敗回数を超えた場合はアカウントをロック
            if (user.getLoginFailCount() >= maxFailedAttempts) {
                user.setAccountLocked(true);
                userRepository.save(user);
                throw new AuthenticationException("Account locked due to too many failed attempts", AuthEventType.ACCOUNT_LOCKED);
            }
            
            userRepository.save(user);
            throw new AuthenticationException("Invalid username/email or password", AuthEventType.LOGIN_FAILED);
        }

        // ログイン成功処理
        user.resetLoginFailCount();
        user.setLastLoginDate(LocalDateTime.now());
        userRepository.save(user);

        // MFA確認が必要かどうかを判断
        if (user.isMfaEnabled()) {
            return createMfaChallenge(user, deviceId, deviceInfo);
        }

        // MFAが不要な場合はトークンを生成
        return generateAuthenticationTokens(user, deviceId, deviceInfo, ipAddress);
    }

    @Override
    @Transactional
    public AuthenticationResponse verifyMfaCode(MfaVerificationRequest mfaRequest) {
        // ユーザーを検索
        User user = userRepository.findById(mfaRequest.getUserId())
                .orElseThrow(() -> new AuthenticationException("Invalid MFA verification request", AuthEventType.MFA_FAILED));

        // MFAのチャレンジIDを検証（実際の実装ではチャレンジの有効期限なども検証）
        // 実装例では簡易的な検証のみ

        // MFAコードを検証（実装例ではTOTPの検証を省略）
        boolean isValidMfaCode = verifyMfaCode(user, mfaRequest.getMfaCode());
        if (!isValidMfaCode) {
            throw new AuthenticationException("Invalid MFA code", AuthEventType.MFA_FAILED);
        }

        // トークンを生成
        return generateAuthenticationTokens(user, mfaRequest.getDeviceId(), mfaRequest.getDeviceInfo(), mfaRequest.getIpAddress());
    }

    @Override
    @Transactional
    public AuthenticationResponse refreshToken(String refreshToken, String deviceId, String deviceInfo, String ipAddress) {
        // リフレッシュトークンの検証
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token", AuthEventType.TOKEN_REFRESH_FAILED));

        // トークンが無効化されていないか確認
        if (token.isRevoked()) {
            throw new AuthenticationException("Refresh token is revoked", AuthEventType.TOKEN_REFRESH_FAILED);
        }

        // トークンの有効期限を確認
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            token.setRevoked(true);
            token.setRevokedReason("Expired");
            refreshTokenRepository.save(token);
            throw new AuthenticationException("Refresh token expired", AuthEventType.TOKEN_EXPIRED);
        }

        // ユーザーIDの検証
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new AuthenticationException("User not found for token", AuthEventType.TOKEN_REFRESH_FAILED));

        // アカウントの状態チェック
        validateUserAccountStatus(user);

        // リフレッシュトークンの利用を記録
        token.setLastUsedAt(LocalDateTime.now());
        refreshTokenRepository.save(token);

        // 新しいトークンを生成
        return generateAuthenticationTokens(user, deviceId, deviceInfo, ipAddress);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.setRevoked(true);
            token.setRevokedReason("User logout");
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void logoutAllDevices(UUID userId) {
        List<RefreshToken> userTokens = refreshTokenRepository.findByUserId(userId);
        for (RefreshToken token : userTokens) {
            token.setRevoked(true);
            token.setRevokedReason("Logout from all devices");
            refreshTokenRepository.save(token);
        }
    }

    @Override
    @Transactional
    public MfaSetupResponse setupMfa(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // MFAのセットアップ情報を生成
        MfaSetupResponse response = generateMfaSetup(user);
        
        // ユーザーにシークレットを保存（検証が完了するまではまだ有効にしない）
        user.setMfaSecret(response.getSecret());
        user.setMfaEnabled(false);
        userRepository.save(user);

        return response;
    }

    @Override
    @Transactional
    public void verifyAndEnableMfa(UUID userId, String mfaCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getMfaSecret() == null) {
            throw new AuthenticationException("MFA not set up for this user", AuthEventType.MFA_SETUP_FAILED);
        }

        // MFAコードを検証（実装例ではTOTPの検証を省略）
        boolean isValidMfaCode = verifyMfaCode(user, mfaCode);
        if (!isValidMfaCode) {
            throw new AuthenticationException("Invalid MFA code", AuthEventType.MFA_SETUP_FAILED);
        }

        // MFAを有効化
        user.setMfaEnabled(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void disableMfa(UUID userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // パスワード確認
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid password", AuthEventType.MFA_DISABLE_FAILED);
        }

        // MFAを無効化
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userRepository.save(user);
    }

    // ユーザー名またはメールでユーザーを検索するヘルパーメソッド
    private Optional<User> findUserByUsernameOrEmail(String usernameOrEmail) {
        Optional<User> user = userRepository.findByUsername(usernameOrEmail);
        if (user.isPresent()) {
            return user;
        }
        return userRepository.findByEmail(usernameOrEmail);
    }

    // ユーザーアカウントの状態を検証するヘルパーメソッド
    private void validateUserAccountStatus(User user) {
        if (!user.isEnabled()) {
            throw new AuthenticationException("Account is disabled", AuthEventType.ACCOUNT_DISABLED);
        }

        if (user.isAccountLocked()) {
            throw new AuthenticationException("Account is locked", AuthEventType.ACCOUNT_LOCKED);
        }

        LocalDateTime now = LocalDateTime.now();

        if (user.getAccountExpireDate() != null && user.getAccountExpireDate().isBefore(now)) {
            throw new AuthenticationException("Account has expired", AuthEventType.ACCOUNT_EXPIRED);
        }

        if (user.getCredentialsExpireDate() != null && user.getCredentialsExpireDate().isBefore(now)) {
            throw new AuthenticationException("Credentials have expired", AuthEventType.CREDENTIALS_EXPIRED);
        }
    }

    // MFAチャレンジを作成するヘルパーメソッド
    private AuthenticationResponse createMfaChallenge(User user, String deviceId, String deviceInfo) {
        // 本来はチャレンジIDを生成して保存する処理が必要
        UUID challengeId = UUID.randomUUID();

        MfaChallenge mfaChallenge = new MfaChallenge();
        mfaChallenge.setChallengeId(challengeId);
        mfaChallenge.setUserId(user.getId());
        mfaChallenge.setDeviceId(deviceId);
        mfaChallenge.setDeviceInfo(deviceInfo);

        AuthenticationResponse response = new AuthenticationResponse();
        response.setRequiresMfa(true);
        response.setMfaChallenge(mfaChallenge);
        return response;
    }

    // 認証トークンを生成するヘルパーメソッド
    private AuthenticationResponse generateAuthenticationTokens(User user, String deviceId, String deviceInfo, String ipAddress) {
        // ユーザーの役割と権限を取得
        List<Role> roles = roleRepository.findByUserId(user.getId());
        List<String> roleNames = roles.stream().map(Role::getName).collect(Collectors.toList());

        // JWTアクセストークンを生成
        String accessToken = jwtProvider.generateAccessToken(user, roleNames);
        
        // 既存のリフレッシュトークンを確認
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserIdAndDeviceId(user.getId(), deviceId);
        
        RefreshToken refreshToken;
        if (existingToken.isPresent() && !existingToken.get().isRevoked() && 
                existingToken.get().getExpiryDate().isAfter(LocalDateTime.now())) {
            // 既存のトークンが有効なら再利用
            refreshToken = existingToken.get();
            refreshToken.setLastUsedAt(LocalDateTime.now());
        } else {
            // 新しいリフレッシュトークンを生成
            refreshToken = new RefreshToken();
            refreshToken.setUserId(user.getId());
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000));
            refreshToken.setDeviceInfo(deviceInfo);
            refreshToken.setDeviceId(deviceId);
            refreshToken.setIpAddress(ipAddress);
            refreshToken.setIssuedAt(LocalDateTime.now());
            refreshToken.setLastUsedAt(LocalDateTime.now());
            refreshToken.setRevoked(false);
        }
        
        refreshTokenRepository.save(refreshToken);
        
        // レスポンスを作成
        AuthenticationResponse response = new AuthenticationResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken.getToken());
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtProvider.getAccessTokenExpirationMs() / 1000);
        response.setRequiresMfa(false);
        
        return response;
    }

    // MFAコードを検証するヘルパーメソッド
    private boolean verifyMfaCode(User user, String mfaCode) {
        if (!user.isMfaEnabled() || user.getMfaSecret() == null) {
            return false;
        }
        // TOTPアルゴリズムを使用してコードを検証する
        return mfaService.verifyCode(user.getMfaSecret(), mfaCode);
    }

    // MFAセットアップ情報を生成するヘルパーメソッド
    private MfaSetupResponse generateMfaSetup(User user) {
        return mfaService.generateMfaSetup(user);
    }

    // MFAシークレットを生成するヘルパーメソッド
    private String generateMfaSecret() {
        return mfaService.generateSecretKey();
    }

    // MFA QRコードURIを生成するヘルパーメソッド
    private String generateQrCodeUri(String username, String secret) {
        return mfaService.generateQrCodeUri(username, secret);
    }
    
    @Override
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        UUID userId = getCurrentUserId();
        if (userId == null) {
            throw new AuthenticationException("User not authenticated", AuthEventType.LOGIN_FAILED);
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Current password is incorrect", AuthEventType.PASSWORD_CHANGE_FAILED);
        }
        
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setCredentialsExpireDate(LocalDateTime.now().plusDays(90)); // 90日間有効
        userRepository.save(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AuthenticationResponse refreshToken(String refreshToken) {
        return refreshToken(refreshToken, null, null, null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UUID getCurrentUserId() {
        // 実際の実装ではSecurityContextからユーザーIDを取得
        // この例では、実装を簡略化
        return null; // 実際にはセキュリティコンテキストから取得
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUserProfile() {
        UUID userId = getCurrentUserId();
        if (userId == null) {
            throw new AuthenticationException("User not authenticated", AuthEventType.UNAUTHORIZED);
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        // ユーザーDTOを作成
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setDepartmentId(user.getDepartmentId());
        dto.setMfaEnabled(user.isMfaEnabled());
        
        // ロールを設定
        List<String> roleNames = roleRepository.findByUserId(userId).stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        dto.setRoles(roleNames);
        
        return dto;
    }
    
    @Override
    @Transactional
    public UserDto updateUserProfile(UserUpdateDto updateDto) {
        UUID userId = getCurrentUserId();
        if (userId == null) {
            throw new AuthenticationException("User not authenticated", AuthEventType.UNAUTHORIZED);
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // ユーザープロファイル情報の更新
        if (updateDto.getFirstName() != null) {
            user.setFirstName(updateDto.getFirstName());
        }
        
        if (updateDto.getLastName() != null) {
            user.setLastName(updateDto.getLastName());
        }
        
        // 他のフィールドも同様に更新
        
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        
        // 更新されたユーザー情報をDTOに変換して返す
        return getCurrentUserProfile();
    }
    
    @Override
    @Transactional
    public MfaChallenge initiateMfaAuthentication(UUID userId, AuthenticationMethod method) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        UUID challengeId = UUID.randomUUID();
        
        MfaChallenge mfaChallenge = new MfaChallenge();
        mfaChallenge.setChallengeId(challengeId);
        mfaChallenge.setMethod(method);
        mfaChallenge.setExpiresIn(300); // 5分間有効
        mfaChallenge.setUserId(userId);
        
        // 実際の実装では、チャレンジをデータベースに保存する
        
        return mfaChallenge;
    }
    
    @Override
    @Transactional
    public boolean verifyMfaCode(UUID userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return verifyMfaCode(user, code);
    }
    
    @Override
    @Transactional
    public String initiatePasswordReset(String email) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        // Generate password reset token
        String token = UUID.randomUUID().toString();
        
        // In a real implementation, save the token and expiry to a repository
        
        return token;
    }
    
    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        // In a real implementation, verify the token and update the password
        // For this example, we'll assume the token is valid
        
        // Find user by token (mock implementation)
        User user = null; // In a real implementation, find user by token
        
        if (user != null) {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            user.setCredentialsExpireDate(LocalDateTime.now().plusDays(90));
            userRepository.save(user);
            return true;
        }
        
        return false;
    }
}