package com.ses_mgr.common.repository;

import com.ses_mgr.common.entity.RefreshToken;
import com.ses_mgr.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * リフレッシュトークンリポジトリ
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * トークン文字列によるリフレッシュトークン検索
     * @param token トークン文字列
     * @return リフレッシュトークンエンティティ（存在する場合）
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * ユーザーに関連付けられた有効なリフレッシュトークンリストの取得
     * @param user ユーザーエンティティ
     * @param now 現在時刻
     * @return 有効なリフレッシュトークンリスト
     */
    @Query("SELECT r FROM RefreshToken r WHERE r.user = :user AND r.expiresAt > :now AND r.revoked = false")
    List<RefreshToken> findValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * ユーザーIDに関連付けられたリフレッシュトークンを全て無効化
     * @param userId ユーザーID
     * @return 更新されたトークン数
     */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user.userId = :userId")
    int revokeAllUserTokens(@Param("userId") UUID userId);

    /**
     * 期限切れのリフレッシュトークンを削除
     * @param now 現在時刻
     * @return 削除されたトークン数
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now OR r.revoked = true")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
}