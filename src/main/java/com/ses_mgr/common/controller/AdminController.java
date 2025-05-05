package com.ses_mgr.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * システム管理画面のコントローラー
 * ADM-001〜ADM-005の画面を処理する
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    /**
     * システム管理のメインページを表示
     *
     * @param model Viewに渡すモデル
     * @return システム管理画面のテンプレート名
     */
    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("pageTitle", "システム管理");
        return "admin/index";
    }

    /**
     * ユーザー管理画面を表示（ADM-001）
     *
     * @param model Viewに渡すモデル
     * @return ユーザー管理画面のテンプレート名
     */
    @GetMapping("/users")
    public String userManagement(Model model) {
        model.addAttribute("pageTitle", "ユーザー管理");
        return "admin/users";
    }

    /**
     * ロール管理画面を表示（ADM-002）
     *
     * @param model Viewに渡すモデル
     * @return ロール管理画面のテンプレート名
     */
    @GetMapping("/roles")
    public String roleManagement(Model model) {
        model.addAttribute("pageTitle", "ロール管理");
        return "admin/roles";
    }

    /**
     * マスタ管理画面を表示（ADM-003）
     *
     * @param model Viewに渡すモデル
     * @return マスタ管理画面のテンプレート名
     */
    @GetMapping("/masters")
    public String masterManagement(Model model) {
        model.addAttribute("pageTitle", "マスタ管理");
        return "admin/masters";
    }

    /**
     * ログ管理画面を表示（ADM-004）
     *
     * @param model Viewに渡すモデル
     * @return ログ管理画面のテンプレート名
     */
    @GetMapping("/logs")
    public String logManagement(Model model) {
        model.addAttribute("pageTitle", "ログ管理");
        return "admin/logs";
    }

    /**
     * バッチ管理画面を表示（ADM-005）
     *
     * @param model Viewに渡すモデル
     * @return バッチ管理画面のテンプレート名
     */
    @GetMapping("/batch")
    public String batchManagement(Model model) {
        model.addAttribute("pageTitle", "バッチ管理");
        return "admin/batch";
    }
}