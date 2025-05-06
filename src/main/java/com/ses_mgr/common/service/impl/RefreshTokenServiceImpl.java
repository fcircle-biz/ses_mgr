package com.ses_mgr.common.service.impl;

import com.ses_mgr.common.entity.RefreshToken;
import com.ses_mgr.common.entity.User;
import com.ses_mgr.common.repository.RefreshTokenRepository;
import com.ses_mgr.common.repository.UserRepository;
import com.ses_mgr.common.service.RefreshTokenService;
import com.ses_mgr.config.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * リフレッシュトークンサービス実装
 */
@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpirationInMs;

    @Value("${app.jwt.extended-refresh-expiration:2592000000}") // 30日（デフォルト値）
    private long extendedRefreshExpirationInMs;
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            JwtTokenProvider tokenProvider) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public RefreshToken createRefreshToken(UUID userId, boolean rememberMe) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません: " + userId));

        String tokenValue = tokenProvider.generateRefreshToken();
        LocalDateTime expiryDate = createExpiryDate(rememberMe);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenId(UUID.randomUUID())
                .token(tokenValue)
                .user(user)
                .expiresAt(expiryDate)
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> validateAndGetRefreshToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);
        
        if (refreshTokenOpt.isEmpty()) {
            return Optional.empty();
        }
        
        RefreshToken refreshToken = refreshTokenOpt.get();
        
        // 無効化または期限切れのトークンはエラー
        if (refreshToken.isRevoked() || refreshToken.isExpired()) {
            return Optional.empty();
        }
        
        return Optional.of(refreshToken);
    }

    @Override
    public int revokeAllUserTokens(UUID userId) {
        return refreshTokenRepository.revokeAllUserTokens(userId);
    }

    @Override
    public RefreshToken rotateRefreshToken(User user, boolean rememberMe) {
        // 既存のトークンを無効化
        revokeAllUserTokens(user.getUserId());
        
        // 新しいトークンを作成
        return createRefreshToken(user.getUserId(), rememberMe);
    }

    @Override
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }

    /**
     * 期限切れのリフレッシュトークンを定期的に削除（毎日深夜3時）
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void purgeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteExpiredTokens(now);
    }

    /**
     * リフレッシュトークンの有効期限日時を作成
     * @param rememberMe 長期保持フラグ
     * @return 有効期限日時
     */
    private LocalDateTime createExpiryDate(boolean rememberMe) {
        long expiration = rememberMe ? extendedRefreshExpirationInMs : refreshExpirationInMs;
        Date expiryDate = new Date(System.currentTimeMillis() + expiration);
        return expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}