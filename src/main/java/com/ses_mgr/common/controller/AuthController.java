package com.ses_mgr.common.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 認証関連のコントローラークラス
 * ログイン・ログアウト・パスワードリセット・多要素認証などの認証関連機能を提供
 */
@Controller
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * ログイン画面を表示
     * @return ログイン画面のビュー名
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    /**
     * パスワードリセット画面を表示
     * @return パスワードリセット画面のビュー名
     */
    @GetMapping("/reset-password")
    public String resetPassword() {
        logger.info("パスワードリセット画面にアクセスされました");
        // 仮の実装: 現在のtemplate名は /forgot-password になっているのでそれを使用
        return "forgot-password";
    }
    
    /**
     * 従来のforgot-passwordエンドポイントをリダイレクト
     */
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "redirect:/reset-password";
    }
    
    /**
     * 多要素認証コードの検証
     * @param code 入力された6桁の認証コード
     * @param session 多要素認証セッションID
     * @return 認証結果に応じたリダイレクト先
     */
    @PostMapping("/mfa-verify")
    public String verifyMfaCode(
            @RequestParam("code") String code,
            @RequestParam("session") String session) {
        
        logger.info("多要素認証コードが入力されました: {}", code.replaceAll("\\d", "*"));
        
        // TODO: 多要素認証の検証ロジックを実装
        boolean isValidCode = "123456".equals(code); // 開発用の仮実装
        
        if (isValidCode) {
            return "redirect:/dashboard";
        } else {
            return "redirect:/login?mfa=true&error&session=" + session;
        }
    }
}