package jp.co.example.sesapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

/**
 * ダッシュボード画面のコントローラー.
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    /**
     * ダッシュボード画面を表示します.
     *
     * @param model モデル
     * @return ダッシュボード画面のビュー名
     */
    @GetMapping
    public String dashboard(Model model) {
        // 現在日時をモデルに追加
        model.addAttribute("now", LocalDateTime.now());
        
        // TODO: ダッシュボードに表示するKPIデータを取得してモデルに追加
        
        return "dashboard/index";
    }
}