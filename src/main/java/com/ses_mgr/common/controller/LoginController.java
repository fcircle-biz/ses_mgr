package com.ses_mgr.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * ログイン画面のコントローラー
 */
@Controller
public class LoginController {

    /**
     * ログイン画面を表示
     * @return ログイン画面テンプレート名
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    /**
     * パスワードリセット画面を表示
     * @return パスワードリセット画面テンプレート名
     */
    @GetMapping("/reset-password")
    public String resetPassword() {
        return "reset-password";
    }
    
    /**
     * ルートアクセス時にログイン画面を直接表示
     * @return ログイン画面テンプレート名
     */
    @GetMapping("/")
    public String root() {
        return "login";
    }
    
    /**
     * ヘルスチェック用エンドポイント
     * @return ステータスメッセージ
     */
    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "Application is running!";
    }
    
    /**
     * デバッグ用のテストページを表示
     * @return テストページテンプレート名
     */
    @GetMapping("/test")
    public String test() {
        return "test";
    }
}