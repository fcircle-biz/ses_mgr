package jp.co.example.sesapp.common.auth.service;

import jp.co.example.sesapp.common.auth.domain.User;
import org.springframework.security.core.Authentication;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * JWT（JSON Web Token）の生成と検証を担当するインターフェース
 */
public interface JwtProvider {

    /**
     * ユーザー情報と権限情報に基づいてJWTアクセストークンを生成する
     *
     * @param user ユーザー情報
     * @param permissions ユーザーの権限リスト
     * @return 生成されたJWTアクセストークン
     */
    String generateAccessToken(User user, List<String> permissions);

    /**
     * ユーザー情報に基づいてJWTリフレッシュトークンを生成する
     *
     * @param user ユーザー情報
     * @return 生成されたJWTリフレッシュトークン
     */
    String generateRefreshToken(User user);

    /**
     * JWTトークンを検証し、有効かどうかを判定する
     *
     * @param token 検証するJWTトークン
     * @return トークンが有効な場合はtrue、そうでない場合はfalse
     */
    boolean validateToken(String token);

    /**
     * トークンがリフレッシュトークンであるかを判定する
     *
     * @param token 検証するJWTトークン
     * @return リフレッシュトークンの場合はtrue、そうでない場合はfalse
     */
    boolean isRefreshToken(String token);

    /**
     * JWTトークンからユーザーIDを取得する
     *
     * @param token JWTトークン
     * @return トークンに含まれるユーザーID
     */
    UUID getUserIdFromToken(String token);

    /**
     * JWTトークンからユーザー名（メールアドレス）を取得する
     *
     * @param token JWTトークン
     * @return トークンに含まれるユーザー名
     */
    String getUsernameFromToken(String token);

    /**
     * JWTトークンから有効期限を取得する
     *
     * @param token JWTトークン
     * @return トークンの有効期限
     */
    Date getExpirationFromToken(String token);

    /**
     * JWTトークンからAuthenticationオブジェクトを生成する
     *
     * @param token JWTトークン
     * @return SpringのAuthenticationオブジェクト
     */
    Authentication getAuthentication(String token);

    /**
     * アクセストークンの有効期間をミリ秒単位で取得する
     *
     * @return アクセストークンの有効期間（ミリ秒）
     */
    long getAccessTokenExpirationMs();

    /**
     * リフレッシュトークンの有効期間をミリ秒単位で取得する
     *
     * @return リフレッシュトークンの有効期間（ミリ秒）
     */
    long getRefreshTokenExpirationMs();
    
    /**
     * JWTトークンからロール名を取得する
     *
     * @param token JWTトークン
     * @return トークンに含まれるロール名
     */
    String getRoleFromToken(String token);
}