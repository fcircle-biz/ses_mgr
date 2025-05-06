package com.ses_mgr.common.entity.file;

/**
 * ファイルタイプの列挙型
 */
public enum FileType {
    
    /**
     * 契約書
     */
    CONTRACT("contract"),
    
    /**
     * 請求書
     */
    INVOICE("invoice"),
    
    /**
     * スキルシート
     */
    SKILL_SHEET("skill_sheet"),
    
    /**
     * 案件資料
     */
    PROJECT_DOCUMENT("project_document"),
    
    /**
     * 共通資料
     */
    COMMON("common"),
    
    /**
     * その他
     */
    OTHER("other");
    
    private final String value;
    
    FileType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * 文字列からFileTypeを取得
     *
     * @param value 文字列
     * @return FileType
     */
    public static FileType fromString(String value) {
        if (value == null) {
            return OTHER;
        }
        
        for (FileType type : FileType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        
        return OTHER;
    }
}