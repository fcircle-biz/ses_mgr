package jp.co.example.sesapp.common.auth.service;

import jp.co.example.sesapp.common.auth.domain.PasswordResetToken;
import jp.co.example.sesapp.common.auth.domain.User;

/**
 * パスワードリセット機能を提供するサービスインターフェース
 */
public interface PasswordResetService {

    /**
     * パスワードリセットプロセスを開始し、リセットトークンを生成します
     * 
     * @param email ユーザーのメールアドレス
     * @return 生成されたパスワードリセットトークン（メール送信用）
     */
    PasswordResetToken createPasswordResetToken(String email);
    
    /**
     * リセットトークンの有効性を検証します
     * 
     * @param tokenString リセットトークン文字列
     * @param email ユーザーのメールアドレス
     * @return 有効な場合はtrue、そうでない場合はfalse
     */
    boolean validatePasswordResetToken(String tokenString, String email);
    
    /**
     * パスワードをリセットします
     * 
     * @param tokenString リセットトークン文字列
     * @param email ユーザーのメールアドレス
     * @param newPassword 新しいパスワード
     * @return パスワードリセットが成功した場合はtrue
     */
    boolean resetPassword(String tokenString, String email, String newPassword);
    
    /**
     * リセットトークンに対応するユーザーを取得します
     * 
     * @param tokenString リセットトークン文字列
     * @return ユーザーエンティティ
     */
    User getUserByResetToken(String tokenString);
    
    /**
     * トークンをメールで送信します
     * 
     * @param token パスワードリセットトークン
     * @param email ユーザーのメールアドレス
     */
    void sendPasswordResetEmail(PasswordResetToken token, String email);
}