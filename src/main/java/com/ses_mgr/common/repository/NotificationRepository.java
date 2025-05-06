package com.ses_mgr.common.repository;

import com.ses_mgr.common.entity.Notification;
import com.ses_mgr.common.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 通知リポジトリ
 * Notification repository
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * 受信者IDで通知を検索（ソフトデリート考慮）
     * Find notifications by recipient ID (considering soft delete)
     *
     * @param recipientId  受信者ID
     * @param pageable     ページング情報
     * @return 通知のページ
     */
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientId(@Param("recipientId") Long recipientId, Pageable pageable);

    /**
     * 受信者IDとタイプで通知を検索（ソフトデリート考慮）
     * Find notifications by recipient ID and type (considering soft delete)
     *
     * @param recipientId  受信者ID
     * @param type         通知タイプ
     * @param pageable     ページング情報
     * @return 通知のページ
     */
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.type = :type AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientIdAndType(@Param("recipientId") Long recipientId, @Param("type") NotificationType type, Pageable pageable);

    /**
     * 受信者IDと既読状態で通知を検索（ソフトデリート考慮）
     * Find notifications by recipient ID and read status (considering soft delete)
     *
     * @param recipientId  受信者ID
     * @param read         既読状態
     * @param pageable     ページング情報
     * @return 通知のページ
     */
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.read = :read AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientIdAndRead(@Param("recipientId") Long recipientId, @Param("read") boolean read, Pageable pageable);

    /**
     * 受信者ID、タイプ、既読状態で通知を検索（ソフトデリート考慮）
     * Find notifications by recipient ID, type and read status (considering soft delete)
     *
     * @param recipientId  受信者ID
     * @param type         通知タイプ
     * @param read         既読状態
     * @param pageable     ページング情報
     * @return 通知のページ
     */
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.type = :type AND n.read = :read AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientIdAndTypeAndRead(@Param("recipientId") Long recipientId, @Param("type") NotificationType type, @Param("read") boolean read, Pageable pageable);

    /**
     * IDと受信者IDで通知を検索（ソフトデリート考慮）
     * Find notification by ID and recipient ID (considering soft delete)
     *
     * @param id           通知ID
     * @param recipientId  受信者ID
     * @return 通知（オプショナル）
     */
    @Query("SELECT n FROM Notification n WHERE n.id = :id AND n.recipientId = :recipientId AND n.deletedAt IS NULL")
    Optional<Notification> findByIdAndRecipientId(@Param("id") UUID id, @Param("recipientId") Long recipientId);

    /**
     * 受信者IDで未読通知の数を取得（ソフトデリート考慮）
     * Count unread notifications by recipient ID (considering soft delete)
     *
     * @param recipientId  受信者ID
     * @return 未読通知数
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientId = :recipientId AND n.read = false AND n.deletedAt IS NULL")
    long countUnreadByRecipientId(@Param("recipientId") Long recipientId);

    /**
     * 受信者IDとタイプで未読通知の数を取得（ソフトデリート考慮）
     * Count unread notifications by recipient ID and type (considering soft delete)
     *
     * @param recipientId  受信者ID
     * @param type         通知タイプ
     * @return 未読通知数
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientId = :recipientId AND n.type = :type AND n.read = false AND n.deletedAt IS NULL")
    long countUnreadByRecipientIdAndType(@Param("recipientId") Long recipientId, @Param("type") NotificationType type);

    /**
     * 受信者IDで全通知の数を取得（ソフトデリート考慮）
     * Count all notifications by recipient ID (considering soft delete)
     *
     * @param recipientId  受信者ID
     * @return 全通知数
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientId = :recipientId AND n.deletedAt IS NULL")
    long countByRecipientId(@Param("recipientId") Long recipientId);

    /**
     * 受信者IDの未読通知を全て既読に更新
     * Update all unread notifications to read for recipient ID
     *
     * @param recipientId  受信者ID
     * @param updatedAt    更新日時
     * @return 更新された通知数
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.updatedAt = :updatedAt WHERE n.recipientId = :recipientId AND n.read = false AND n.deletedAt IS NULL")
    int markAllAsReadByRecipientId(@Param("recipientId") Long recipientId, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 受信者IDとタイプの未読通知を全て既読に更新
     * Update all unread notifications to read for recipient ID and type
     *
     * @param recipientId  受信者ID
     * @param type         通知タイプ
     * @param updatedAt    更新日時
     * @return 更新された通知数
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.updatedAt = :updatedAt WHERE n.recipientId = :recipientId AND n.type = :type AND n.read = false AND n.deletedAt IS NULL")
    int markAllAsReadByRecipientIdAndType(@Param("recipientId") Long recipientId, @Param("type") NotificationType type, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 論理削除（ソフトデリート）
     * Soft delete
     *
     * @param id          通知ID
     * @param recipientId 受信者ID
     * @param deletedAt   削除日時
     * @return 更新された行数
     */
    @Modifying
    @Query("UPDATE Notification n SET n.deletedAt = :deletedAt WHERE n.id = :id AND n.recipientId = :recipientId AND n.deletedAt IS NULL")
    int softDelete(@Param("id") UUID id, @Param("recipientId") Long recipientId, @Param("deletedAt") LocalDateTime deletedAt);
}