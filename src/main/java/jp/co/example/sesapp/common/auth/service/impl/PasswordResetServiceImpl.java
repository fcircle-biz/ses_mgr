package jp.co.example.sesapp.common.auth.service.impl;

import jp.co.example.sesapp.common.auth.domain.AuthEventType;
import jp.co.example.sesapp.common.auth.domain.PasswordResetToken;
import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.auth.repository.PasswordResetTokenRepository;
import jp.co.example.sesapp.common.auth.repository.UserRepository;
import jp.co.example.sesapp.common.auth.service.PasswordResetService;
import jp.co.example.sesapp.common.exception.AuthenticationException;
import jp.co.example.sesapp.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * パスワードリセットサービスの実装クラス
 */
@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    // メール送信サービスも本来は必要

    @Value("${application.security.password-reset.token-expiry-minutes:30}")
    private int tokenExpiryMinutes;

    public PasswordResetServiceImpl(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public PasswordResetToken createPasswordResetToken(String email) {
        // メールアドレスからユーザーを取得
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // まだ有効なトークンがある場合は取り消す
        tokenRepository.findValidTokenByUserId(user.getId()).ifPresent(token -> {
            token.markAsUsed();
            tokenRepository.save(token);
        });

        // 新しいトークンを生成
        PasswordResetToken token = PasswordResetToken.createToken(user.getId(), tokenExpiryMinutes);
        
        // トークンを保存
        PasswordResetToken savedToken = tokenRepository.save(token);
        
        logger.debug("Password reset token created for user: {}", email);
        
        return savedToken;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validatePasswordResetToken(String tokenString, String email) {
        // メールアドレスからユーザーを取得
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // トークンを検証
        return tokenRepository.findByTokenAndUserId(tokenString, user.getId())
                .map(PasswordResetToken::isValid)
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean resetPassword(String tokenString, String email, String newPassword) {
        // メールアドレスからユーザーを取得
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // トークンを検証
        PasswordResetToken token = tokenRepository.findByTokenAndUserId(tokenString, user.getId())
                .orElseThrow(() -> new AuthenticationException("Invalid reset token", AuthEventType.PASSWORD_RESET_FAILED));

        if (!token.isValid()) {
            throw new AuthenticationException("Token is expired or has been used", AuthEventType.PASSWORD_RESET_FAILED);
        }

        // パスワードを変更
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        
        // パスワード有効期限を更新（例：90日間）
        user.setPasswordExpiresAt(null); // パスワードリセット後は無期限とする例
        
        userRepository.save(user);

        // トークンを使用済みにする
        token.markAsUsed();
        tokenRepository.save(token);
        
        logger.info("Password has been reset for user: {}", email);
        
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByResetToken(String tokenString) {
        PasswordResetToken token = tokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid reset token"));
        
        return userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public void sendPasswordResetEmail(PasswordResetToken token, String email) {
        // 実際の実装ではメールサービスを使用してメールを送信
        // 例: emailService.sendPasswordResetEmail(email, token.getToken());
        
        logger.info("Password reset email would be sent to: {} with token: {}", email, token.getToken());
        
        // TODO: 実際のプロジェクトではメール送信サービスを実装する
    }
}