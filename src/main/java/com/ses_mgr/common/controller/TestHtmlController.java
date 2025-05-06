package com.ses_mgr.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * テスト用のシンプルなHTMLを返すコントローラー
 */
@Controller
@RequestMapping("/html-test")
public class TestHtmlController {

    /**
     * デバッグ用テストページHTMLを直接返すエンドポイント
     * @return HTMLコンテンツ
     */
    @GetMapping
    @ResponseBody
    public String testPage() {
        return "<!DOCTYPE html>" +
                "<html lang=\"ja\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>テストページ | SES管理システム</title>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; }" +
                "        .container { max-width: 800px; margin: 0 auto; background-color: #f8f9fa; padding: 20px; border-radius: 5px; }" +
                "        h1 { color: #0d6efd; }" +
                "        .success { color: #198754; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class=\"container\">" +
                "        <h1>テストページ</h1>" +
                "        <p class=\"success\">アプリケーションは正常に動作しています。</p>" +
                "        <p>このページはテスト用のダミーページです。</p>" +
                "        <p>現在の時刻: " + java.time.LocalDateTime.now() + "</p>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}