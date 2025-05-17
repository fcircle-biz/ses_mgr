package jp.co.example.sesapp.common.exception;

import jp.co.example.sesapp.common.auth.domain.AuthEventType;

/**
 * 認証失敗時に発生する例外クラス.
 */
public class AuthenticationException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private final String errorCode;

    /**
     * コンストラクタ.
     *
     * @param message エラーメッセージ
     */
    public AuthenticationException(String message) {
        super(message);
        this.errorCode = "AUTHENTICATION_ERROR";
    }

    /**
     * コンストラクタ.
     *
     * @param message エラーメッセージ
     * @param errorCode エラーコード
     */
    public AuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * コンストラクタ.
     *
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AUTHENTICATION_ERROR";
    }

    /**
     * コンストラクタ.
     *
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     * @param errorCode エラーコード
     */
    public AuthenticationException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * エラーコードを取得する.
     *
     * @return エラーコード
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * コンストラクタ.
     *
     * @param message エラーメッセージ
     * @param eventType 認証イベントタイプ
     */
    public AuthenticationException(String message, AuthEventType eventType) {
        super(message);
        this.errorCode = eventType != null ? eventType.name() : "AUTHENTICATION_ERROR";
    }
}