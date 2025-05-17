package jp.co.example.sesapp.common.auth.repository;

import jp.co.example.sesapp.common.auth.domain.RefreshToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * リフレッシュトークン情報のデータアクセスを担当するリポジトリインターフェース
 */
public interface RefreshTokenRepository {

    /**
     * トークンIDに基づいてリフレッシュトークンを検索
     *
     * @param id トークンID
     * @return 指定されたIDのリフレッシュトークン（存在する場合）
     */
    Optional<RefreshToken> findById(UUID id);

    /**
     * トークン文字列に基づいてリフレッシュトークンを検索
     *
     * @param token トークン文字列
     * @return 指定されたトークン文字列のリフレッシュトークン（存在する場合）
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * ユーザーIDに基づいてリフレッシュトークンを検索
     *
     * @param userId ユーザーID
     * @return 指定されたユーザーのリフレッシュトークンのリスト
     */
    List<RefreshToken> findByUserId(UUID userId);

    /**
     * デバイスIDに基づいてリフレッシュトークンを検索
     *
     * @param userId ユーザーID
     * @param deviceId デバイスID
     * @return 指定されたユーザーとデバイスのリフレッシュトークン（存在する場合）
     */
    Optional<RefreshToken> findByUserIdAndDeviceId(UUID userId, String deviceId);

    /**
     * リフレッシュトークンを保存または更新
     *
     * @param refreshToken 保存または更新するリフレッシュトークン
     * @return 保存されたリフレッシュトークン
     */
    RefreshToken save(RefreshToken refreshToken);

    /**
     * 指定されたIDのリフレッシュトークンを削除
     *
     * @param id トークンID
     */
    void deleteById(UUID id);

    /**
     * 指定されたトークン文字列のリフレッシュトークンを削除
     *
     * @param token トークン文字列
     */
    void deleteByToken(String token);

    /**
     * 特定ユーザーのすべてのリフレッシュトークンを削除
     *
     * @param userId ユーザーID
     */
    void deleteAllByUserId(UUID userId);

    /**
     * 期限切れのリフレッシュトークンをすべて削除
     */
    void deleteAllExpired();
}