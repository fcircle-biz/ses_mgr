package jp.co.example.sesapp.common.auth.domain;

/**
 * リソース種別の列挙型。
 */
public enum ResourceType {
    USER,       // ユーザー
    ROLE,       // ロール
    PERMISSION, // 権限
    ENGINEER,   // 技術者
    PROJECT,    // 案件
    CONTRACT,   // 契約
    INVOICE,    // 請求
    TIMESHEET,  // 勤怠
    REPORT      // レポート
}