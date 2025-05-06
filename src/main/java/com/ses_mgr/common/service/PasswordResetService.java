package com.ses_mgr.common.service;

import java.util.Optional;

/**
 * パスワードリセットサービスインターフェース
 */
public interface PasswordResetService {

    /**
     * パスワードリセットリクエストを作成
     * @param email ユーザーのメールアドレス
     * @return 生成されたリセットトークン (通常はメールで送信されるだけなので返値はなし)
     */
    String createPasswordResetRequest(String email);
    
    /**
     * パスワードリセットトークンを検証
     * @param token リセットトークン
     * @param email ユーザーのメールアドレス
     * @return 検証結果（トークンが有効かどうか）
     */
    boolean validatePasswordResetToken(String token, String email);
    
    /**
     * 新しいパスワードでリセットを実行
     * @param token リセットトークン
     * @param email ユーザーのメールアドレス
     * @param newPassword 新しいパスワード
     * @return 処理結果（成功/失敗）
     */
    boolean executePasswordReset(String token, String email, String newPassword);
}