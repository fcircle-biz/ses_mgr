package com.ses_mgr.common.entity;

/**
 * 通知タイプを表す列挙型
 * Enum representing notification types
 */
public enum NotificationType {
    /**
     * システム通知
     * System notification
     */
    SYSTEM,
    
    /**
     * タスク通知
     * Task notification
     */
    TASK,
    
    /**
     * アラート通知
     * Alert notification
     */
    ALERT,
    
    /**
     * イベント通知
     * Event notification
     */
    EVENT;
    
    /**
     * 文字列表現を小文字で取得
     * Get lowercase string representation
     */
    public String toLowerCase() {
        return this.name().toLowerCase();
    }
    
    /**
     * 文字列からNotificationTypeを取得
     * Get NotificationType from string
     */
    public static NotificationType fromString(String typeString) {
        if (typeString == null) {
            return null;
        }
        
        try {
            return NotificationType.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}