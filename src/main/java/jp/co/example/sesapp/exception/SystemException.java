package jp.co.example.sesapp.exception;

/**
 * システムエラーを表す例外クラス.
 * システム内部で発生した予期しないエラーを表します。
 */
public class SystemException extends BaseException {

    /**
     * コンストラクタ.
     *
     * @param message エラーメッセージ
     * @param errorCode エラーコード
     */
    public SystemException(String message, String errorCode) {
        super(message, errorCode);
    }

    /**
     * コンストラクタ.
     *
     * @param message エラーメッセージ
     * @param cause 原因となる例外
     * @param errorCode エラーコード
     */
    public SystemException(String message, Throwable cause, String errorCode) {
        super(message, cause, errorCode);
    }
}