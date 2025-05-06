package com.ses_mgr.common.service;

import com.ses_mgr.common.entity.RefreshToken;
import com.ses_mgr.common.entity.User;

import java.util.Optional;
import java.util.UUID;

/**
 * リフレッシュトークンサービスインターフェース
 */
public interface RefreshTokenService {

    /**
     * 新しいリフレッシュトークンを作成
     * @param userId ユーザーID
     * @param rememberMe 長期間の保持フラグ
     * @return 作成されたリフレッシュトークン
     */
    RefreshToken createRefreshToken(UUID userId, boolean rememberMe);
    
    /**
     * リフレッシュトークン文字列からトークンを検証して取得
     * @param token リフレッシュトークン文字列
     * @return 有効なリフレッシュトークンエンティティ（あれば）
     */
    Optional<RefreshToken> validateAndGetRefreshToken(String token);
    
    /**
     * 特定のユーザーのすべてのリフレッシュトークンを無効化
     * @param userId ユーザーID
     * @return 無効化されたトークン数
     */
    int revokeAllUserTokens(UUID userId);
    
    /**
     * ユーザーの非アクティブなトークンを全て削除し、新しいトークンを発行
     * @param user ユーザーエンティティ
     * @param rememberMe 長期間の保持フラグ
     * @return 新しいリフレッシュトークン
     */
    RefreshToken rotateRefreshToken(User user, boolean rememberMe);
    
    /**
     * リフレッシュトークンを無効化
     * @param token リフレッシュトークン文字列
     */
    void revokeRefreshToken(String token);
}