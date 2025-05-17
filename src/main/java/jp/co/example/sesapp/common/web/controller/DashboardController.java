package jp.co.example.sesapp.common.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;

/**
 * ダッシュボード関連のMVC Controllerクラス
 */
@Controller
public class DashboardController {

    /**
     * ダッシュボード画面を表示する
     *
     * @param model Thymeleafのモデルオブジェクト
     * @param authentication 認証情報
     * @return ビュー名
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model, Authentication authentication) {
        // 認証情報からユーザー名を取得
        String username = authentication.getName();
        model.addAttribute("username", username);
        
        return "dashboard";
    }
}