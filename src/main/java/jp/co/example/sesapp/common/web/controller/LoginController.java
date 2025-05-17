package jp.co.example.sesapp.common.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

/**
 * ログイン関連のMVC Controllerクラス
 */
@Controller
public class LoginController {

    /**
     * ログイン画面を表示する
     *
     * @param model Thymeleafのモデルオブジェクト
     * @return ビュー名
     */
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        return "login";
    }

    /**
     * ログイン失敗時のハンドリング
     *
     * @param model Thymeleafのモデルオブジェクト
     * @return ビュー名
     */
    @GetMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "login";
    }

    /**
     * ログアウト成功時のハンドリング
     *
     * @param model Thymeleafのモデルオブジェクト
     * @return ビュー名
     */
    @GetMapping("/logout-success")
    public String logoutSuccess(Model model) {
        model.addAttribute("logoutSuccess", true);
        return "login";
    }
}