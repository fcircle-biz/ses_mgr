package jp.co.example.sesapp.common.auth.domain;

/**
 * 認証方式の列挙型。
 */
public enum AuthenticationMethod {
    PASSWORD,  // パスワード認証
    MFA_EMAIL, // メール多要素認証
    MFA_APP,   // アプリ多要素認証
    MFA_SMS,   // SMS多要素認証
    TOTP       // Time-based One-Time Password認証
}