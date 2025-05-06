package com.ses_mgr.common.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ses_mgr.common.entity.User;

/**
 * メニュー画面コントローラー
 */
@Controller
@RequestMapping("/menu")
public class MenuController {

    /**
     * メニュー画面表示
     * 
     * @param model モデル
     * @return メニュー画面テンプレート名
     */
    @GetMapping
    public String showMenu(Model model) {
        // 認証情報から現在のユーザーを取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        
        if (auth != null && auth.getPrincipal() instanceof User) {
            currentUser = (User) auth.getPrincipal();
            model.addAttribute("currentUser", currentUser);
        }
        
        // アクティブメニューの設定
        model.addAttribute("activeMenu", "menu");
        
        return "menu";
    }
}