package jp.co.example.sesapp.common.exception;

import java.util.Map;

/**
 * 業務例外。
 * 業務ルールに違反した場合に発生する例外を表します。
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private final String errorCode;
    private final Map<String, Object> details;

    /**
     * コンストラクタ。
     * 
     * @param message エラーメッセージ
     * @param errorCode エラーコード
     */
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    /**
     * コンストラクタ。
     * 
     * @param message エラーメッセージ
     * @param errorCode エラーコード
     * @param details エラー詳細情報
     */
    public BusinessException(String message, String errorCode, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * コンストラクタ。
     * 
     * @param message エラーメッセージ
     * @param errorCode エラーコード
     * @param cause 原因となった例外
     */
    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = null;
    }

    /**
     * コンストラクタ。
     * 
     * @param message エラーメッセージ
     * @param errorCode エラーコード
     * @param details エラー詳細情報
     * @param cause 原因となった例外
     */
    public BusinessException(String message, String errorCode, Map<String, Object> details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * エラーコードを取得します。
     * 
     * @return エラーコード
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * エラー詳細情報を取得します。
     * 
     * @return エラー詳細情報
     */
    public Map<String, Object> getDetails() {
        return details;
    }
}