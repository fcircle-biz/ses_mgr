package jp.co.example.sesapp.common.auth.repository;

import jp.co.example.sesapp.common.auth.domain.PasswordResetToken;

import java.util.Optional;
import java.util.UUID;

/**
 * パスワードリセットトークンのリポジトリインターフェース
 */
public interface PasswordResetTokenRepository {
    
    /**
     * トークンを保存する
     * 
     * @param token 保存するトークン
     * @return 保存されたトークン
     */
    PasswordResetToken save(PasswordResetToken token);
    
    /**
     * トークンIDによってトークンを検索する
     * 
     * @param id トークンID
     * @return 検索結果
     */
    Optional<PasswordResetToken> findById(UUID id);
    
    /**
     * トークン文字列によってトークンを検索する
     * 
     * @param token トークン文字列
     * @return 検索結果
     */
    Optional<PasswordResetToken> findByToken(String token);
    
    /**
     * ユーザーIDによって有効なトークンを検索する
     * 
     * @param userId ユーザーID
     * @return 検索結果
     */
    Optional<PasswordResetToken> findValidTokenByUserId(UUID userId);
    
    /**
     * トークン文字列とユーザーIDによってトークンを検索する
     * 
     * @param token トークン文字列
     * @param userId ユーザーID
     * @return 検索結果
     */
    Optional<PasswordResetToken> findByTokenAndUserId(String token, UUID userId);
    
    /**
     * 期限切れのトークンを削除する
     * 
     * @return 削除された数
     */
    int deleteExpiredTokens();
}