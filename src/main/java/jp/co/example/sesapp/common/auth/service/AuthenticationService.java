package jp.co.example.sesapp.common.auth.service;

import jp.co.example.sesapp.common.auth.domain.AuthenticationMethod;
import jp.co.example.sesapp.common.auth.domain.dto.AuthenticationResponse;
import jp.co.example.sesapp.common.auth.domain.dto.Credentials;
import jp.co.example.sesapp.common.auth.domain.dto.MfaChallenge;
import jp.co.example.sesapp.common.auth.domain.dto.MfaSetupResponse;
import jp.co.example.sesapp.common.auth.domain.dto.MfaVerificationRequest;
import jp.co.example.sesapp.common.auth.domain.dto.UserDto;
import jp.co.example.sesapp.common.auth.domain.dto.UserUpdateDto;

import java.util.UUID;

/**
 * 認証サービスのインターフェース。
 * 認証に関連する操作を提供します。
 */
public interface AuthenticationService {
    /**
     * ユーザー認証を行い認証情報を返します。
     * @param credentials ユーザー認証情報（メールアドレス、パスワード）
     * @return 認証結果（アクセストークン、リフレッシュトークン）
     * @throws jp.co.example.sesapp.common.exception.AuthenticationException 認証失敗
     */
    AuthenticationResponse authenticate(Credentials credentials);
    
    /**
     * デバイス情報付きでユーザー認証を行い認証情報を返します。
     * @param credentials ユーザー認証情報（メールアドレス、パスワード）
     * @param deviceId デバイスID
     * @param deviceInfo デバイス情報
     * @param ipAddress IPアドレス
     * @return 認証結果（アクセストークン、リフレッシュトークン）
     * @throws jp.co.example.sesapp.common.exception.AuthenticationException 認証失敗
     */
    AuthenticationResponse authenticate(Credentials credentials, String deviceId, String deviceInfo, String ipAddress);
    
    /**
     * リフレッシュトークンを使用して新しいアクセストークンを返します。
     * @param refreshToken リフレッシュトークン
     * @return 新しい認証情報
     * @throws jp.co.example.sesapp.common.exception.TokenException トークンが無効
     */
    AuthenticationResponse refreshToken(String refreshToken);
    
    /**
     * デバイス情報付きでリフレッシュトークンを使用して新しいアクセストークンを返します。
     * @param refreshToken リフレッシュトークン
     * @param deviceId デバイスID
     * @param deviceInfo デバイス情報
     * @param ipAddress IPアドレス
     * @return 新しい認証情報
     * @throws jp.co.example.sesapp.common.exception.TokenException トークンが無効
     */
    AuthenticationResponse refreshToken(String refreshToken, String deviceId, String deviceInfo, String ipAddress);
    
    /**
     * アクセストークンを無効化してログアウトします。
     * @param accessToken アクセストークン
     */
    void logout(String accessToken);
    
    /**
     * パスワードリセット処理を開始します。
     * @param email ユーザーのメールアドレス
     * @return パスワードリセットトークン
     */
    String initiatePasswordReset(String email);
    
    /**
     * パスワードをリセットします。
     * @param token パスワードリセットトークン
     * @param newPassword 新しいパスワード
     * @return 成功
     * @throws jp.co.example.sesapp.common.exception.TokenException トークンが無効
     */
    boolean resetPassword(String token, String newPassword);
    
    /**
     * 多要素認証を開始します。
     * @param userId ユーザーID
     * @param method 多要素認証方式
     * @return 多要素認証チャレンジ
     */
    MfaChallenge initiateMfaAuthentication(UUID userId, AuthenticationMethod method);
    
    /**
     * 多要素認証を検証します。
     * @param userId ユーザーID
     * @param code 検証コード
     * @return 検証成功
     */
    boolean verifyMfaCode(UUID userId, String code);
    
    /**
     * MFA検証リクエストで多要素認証を検証します。
     * @param request MFA検証リクエスト
     * @return 認証レスポンス
     */
    AuthenticationResponse verifyMfaCode(MfaVerificationRequest request);
    
    /**
     * 現在認証されているユーザーのIDを取得します。
     * @return ユーザーID
     */
    UUID getCurrentUserId();
    
    /**
     * 現在認証されているユーザーのプロファイル情報を取得します。
     * @return ユーザーDTO
     */
    UserDto getCurrentUserProfile();
    
    /**
     * ユーザープロファイルを更新します。
     * @param updateDto 更新用DTO
     * @return 更新されたユーザーDTO
     */
    UserDto updateUserProfile(UserUpdateDto updateDto);
    
    /**
     * パスワードを変更します。
     * @param currentPassword 現在のパスワード
     * @param newPassword 新しいパスワード
     */
    void changePassword(String currentPassword, String newPassword);
    
    /**
     * MFA設定情報を取得します。
     * @param userId ユーザーID
     * @return MFA設定レスポンス
     */
    MfaSetupResponse setupMfa(UUID userId);
    
    /**
     * MFAコードを検証してMFAを有効化します。
     * @param userId ユーザーID
     * @param mfaCode MFAコード
     */
    void verifyAndEnableMfa(UUID userId, String mfaCode);
    
    /**
     * MFAを無効化します。
     * @param userId ユーザーID
     * @param password パスワード
     */
    void disableMfa(UUID userId, String password);
}