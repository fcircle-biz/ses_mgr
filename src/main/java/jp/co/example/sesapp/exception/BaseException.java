package jp.co.example.sesapp.exception;

/**
 * アプリケーション固有の例外の基底クラス.
 * すべてのカスタム例外はこのクラスを継承します。
 */
public abstract class BaseException extends RuntimeException {

    /**
     * エラーコード
     */
    private final String errorCode;

    /**
     * コンストラクタ.
     *
     * @param message エラーメッセージ
     * @param errorCode エラーコード
     */
    public BaseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * コンストラクタ.
     *
     * @param message エラーメッセージ
     * @param cause 原因となる例外
     * @param errorCode エラーコード
     */
    public BaseException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * エラーコードを取得します.
     *
     * @return エラーコード
     */
    public String getErrorCode() {
        return errorCode;
    }
}