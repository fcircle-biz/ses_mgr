package com.ses_mgr.common.service.impl;

import com.ses_mgr.common.entity.PasswordResetToken;
import com.ses_mgr.common.entity.User;
import com.ses_mgr.common.repository.PasswordResetTokenRepository;
import com.ses_mgr.common.repository.UserRepository;
import com.ses_mgr.common.service.PasswordResetService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * パスワードリセットサービス実装
 */
@Service
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

    @Value("${app.password-reset.expiration:3600000}") // デフォルト1時間
    private long passwordResetExpirationInMs;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordResetServiceImpl(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String createPasswordResetRequest(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません: " + email));

        // 既存のトークンを無効化
        tokenRepository.invalidateUserTokens(user.getUserId());

        // 新しいトークンを生成
        String tokenValue = generateToken();
        
        // トークンの有効期限を設定（1時間後）
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(passwordResetExpirationInMs / 1000);

        // トークンを保存
        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .tokenId(UUID.randomUUID())
                .token(tokenValue)
                .user(user)
                .expiresAt(expiryDate)
                .used(false)
                .build();

        tokenRepository.save(passwordResetToken);

        // 実際のアプリケーションではここでメールを送信
        // emailService.sendPasswordResetEmail(user.getEmail(), tokenValue);

        return tokenValue;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validatePasswordResetToken(String token, String email) {
        if (token == null || token.isEmpty() || email == null || email.isEmpty()) {
            return false;
        }

        // メールアドレスからユーザーを検索
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        // トークンを検証
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        if (tokenOptional.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOptional.get();

        // トークンがこのユーザーのものでない場合は無効
        if (!resetToken.getUser().getUserId().equals(user.getUserId())) {
            return false;
        }

        // 既に使用済みか期限切れの場合は無効
        return resetToken.isValid();
    }

    @Override
    public boolean executePasswordReset(String token, String email, String newPassword) {
        // トークンを検証
        if (!validatePasswordResetToken(token, email)) {
            return false;
        }

        // ユーザーを取得
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません: " + email));

        // トークンを取得して使用済みにマーク
        PasswordResetToken resetToken = tokenRepository.findByToken(token).get();
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        // パスワードを更新
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordExpiresAt(null); // パスワード有効期限をリセット
        userRepository.save(user);

        return true;
    }

    /**
     * 期限切れのパスワードリセットトークンを定期的に削除（毎日深夜2時）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void purgeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.deleteExpiredTokens(now);
    }

    /**
     * 安全なランダムトークンを生成
     * @return ランダムトークン文字列
     */
    private String generateToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}