package com.ses_mgr.common.service;

import com.ses_mgr.common.dto.NotificationDto;
import com.ses_mgr.common.dto.NotificationListResponseDto;
import com.ses_mgr.common.entity.NotificationType;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * 通知サービスインターフェース
 * Notification service interface
 */
public interface NotificationService {

    /**
     * ユーザーの通知一覧を取得する
     * Get notifications for a user
     *
     * @param userId    ユーザーID
     * @param type      通知タイプ（オプション）
     * @param readStatus 既読状態（read, unread, all）
     * @param pageable  ページネーション情報
     * @return 通知一覧レスポンスDTO
     */
    NotificationListResponseDto getNotifications(Long userId, String type, String readStatus, Pageable pageable);

    /**
     * 特定の通知の詳細を取得する
     * Get notification details
     *
     * @param notificationId 通知ID
     * @param userId        ユーザーID
     * @return 通知DTO
     */
    NotificationDto getNotificationById(UUID notificationId, Long userId);

    /**
     * 通知を既読にする
     * Mark notification as read
     *
     * @param notificationId 通知ID
     * @param userId        ユーザーID
     * @return 更新された通知DTO
     */
    NotificationDto markAsRead(UUID notificationId, Long userId);

    /**
     * 全ての通知を既読にする
     * Mark all notifications as read
     *
     * @param userId    ユーザーID
     * @param type      通知タイプ（オプション）
     * @return 既読にされた通知の数
     */
    int markAllAsRead(Long userId, String type);

    /**
     * 通知を削除する
     * Delete notification
     *
     * @param notificationId 通知ID
     * @param userId        ユーザーID
     */
    void deleteNotification(UUID notificationId, Long userId);

    /**
     * 通知を作成する（システム内部用）
     * Create notification (for internal system use)
     *
     * @param type        通知タイプ
     * @param title       タイトル
     * @param body        本文
     * @param metadata    メタデータ
     * @param senderId    送信者ID（オプション）
     * @param senderName  送信者名（オプション）
     * @param recipientId 受信者ID
     * @param recipientName 受信者名
     * @return 作成された通知DTO
     */
    NotificationDto createNotification(NotificationType type, String title, String body, 
                                      Object metadata, Long senderId, String senderName, 
                                      Long recipientId, String recipientName);
}