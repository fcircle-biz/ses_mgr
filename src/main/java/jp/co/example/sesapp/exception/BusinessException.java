package jp.co.example.sesapp.exception;

/**
 * ビジネスルール違反を表す例外クラス.
 * ビジネスルールに違反する操作が行われた場合にスローされます。
 */
public class BusinessException extends BaseException {

    /**
     * コンストラクタ.
     *
     * @param message エラーメッセージ
     * @param errorCode エラーコード
     */
    public BusinessException(String message, String errorCode) {
        super(message, errorCode);
    }

    /**
     * コンストラクタ.
     *
     * @param message エラーメッセージ
     * @param cause 原因となる例外
     * @param errorCode エラーコード
     */
    public BusinessException(String message, Throwable cause, String errorCode) {
        super(message, cause, errorCode);
    }
}