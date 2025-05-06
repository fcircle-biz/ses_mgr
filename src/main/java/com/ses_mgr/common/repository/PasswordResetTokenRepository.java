package com.ses_mgr.common.repository;

import com.ses_mgr.common.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * パスワードリセットトークンリポジトリ
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    /**
     * トークン文字列によるパスワードリセットトークン検索
     * @param token トークン文字列
     * @return パスワードリセットトークンエンティティ（存在する場合）
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * ユーザーIDによる未使用かつ有効なトークン検索
     * @param userId ユーザーID
     * @param now 現在時刻
     * @return パスワードリセットトークン（存在する場合）
     */
    @Query("SELECT p FROM PasswordResetToken p WHERE p.user.userId = :userId AND p.expiresAt > :now AND p.used = false")
    Optional<PasswordResetToken> findValidTokenByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    /**
     * 期限切れのパスワードリセットトークンを削除
     * @param now 現在時刻
     * @return 削除されたトークン数
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiresAt < :now OR p.used = true")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * ユーザーの既存のトークンを全て無効化
     * @param userId ユーザーID
     * @return 更新されたトークン数
     */
    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.user.userId = :userId")
    int invalidateUserTokens(@Param("userId") UUID userId);
}