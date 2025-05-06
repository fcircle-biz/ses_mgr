package com.ses_mgr.common.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.common.dto.NotificationDto;
import com.ses_mgr.common.dto.NotificationListResponseDto;
import com.ses_mgr.common.entity.Notification;
import com.ses_mgr.common.entity.NotificationType;
import com.ses_mgr.common.repository.NotificationRepository;
import com.ses_mgr.common.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 通知サービス実装クラス
 * Notification service implementation
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository, ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationListResponseDto getNotifications(Long userId, String typeStr, String readStatus, Pageable pageable) {
        Page<Notification> notificationsPage;
        
        // 通知タイプの変換
        NotificationType type = typeStr != null ? NotificationType.fromString(typeStr) : null;
        
        // 既読状態のフィルタリング
        Boolean readFilter = null;
        if (readStatus != null) {
            if ("read".equalsIgnoreCase(readStatus)) {
                readFilter = true;
            } else if ("unread".equalsIgnoreCase(readStatus)) {
                readFilter = false;
            }
        }
        
        // クエリの実行
        if (type != null && readFilter != null) {
            notificationsPage = notificationRepository.findByRecipientIdAndTypeAndRead(userId, type, readFilter, pageable);
        } else if (type != null) {
            notificationsPage = notificationRepository.findByRecipientIdAndType(userId, type, pageable);
        } else if (readFilter != null) {
            notificationsPage = notificationRepository.findByRecipientIdAndRead(userId, readFilter, pageable);
        } else {
            notificationsPage = notificationRepository.findByRecipientId(userId, pageable);
        }
        
        // 結果をDTOに変換
        List<NotificationDto> notificationDtos = notificationsPage.getContent().stream()
                .map(NotificationDto::new)
                .collect(Collectors.toList());
        
        // 未読通知数の取得
        long unreadCount = notificationRepository.countUnreadByRecipientId(userId);
        
        // 全通知数の取得
        long totalCount = notificationRepository.countByRecipientId(userId);
        
        // レスポンスDTOの構築
        return NotificationListResponseDto.builder()
                .notifications(notificationDtos)
                .currentPage(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalPages(notificationsPage.getTotalPages())
                .totalItems(notificationsPage.getTotalElements())
                .unreadCount(unreadCount)
                .totalCount(totalCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationDto getNotificationById(UUID notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new EntityNotFoundException("通知が見つかりません: " + notificationId));
        
        return new NotificationDto(notification);
    }

    @Override
    @Transactional
    public NotificationDto markAsRead(UUID notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new EntityNotFoundException("通知が見つかりません: " + notificationId));
        
        // 既に既読の場合は何もしない
        if (notification.isRead()) {
            return new NotificationDto(notification);
        }
        
        // 既読に設定
        notification.setRead(true);
        notification.setUpdatedAt(LocalDateTime.now());
        
        Notification updatedNotification = notificationRepository.save(notification);
        return new NotificationDto(updatedNotification);
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId, String typeStr) {
        LocalDateTime now = LocalDateTime.now();
        int updatedCount;
        
        if (typeStr != null) {
            NotificationType type = NotificationType.fromString(typeStr);
            if (type == null) {
                throw new IllegalArgumentException("無効な通知タイプです: " + typeStr);
            }
            updatedCount = notificationRepository.markAllAsReadByRecipientIdAndType(userId, type, now);
        } else {
            updatedCount = notificationRepository.markAllAsReadByRecipientId(userId, now);
        }
        
        return updatedCount;
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, Long userId) {
        // 通知の存在確認
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new EntityNotFoundException("通知が見つかりません: " + notificationId));
        
        // ソフトデリート
        LocalDateTime now = LocalDateTime.now();
        int deleted = notificationRepository.softDelete(notificationId, userId, now);
        
        if (deleted != 1) {
            throw new RuntimeException("通知の削除に失敗しました: " + notificationId);
        }
    }

    @Override
    @Transactional
    public NotificationDto createNotification(NotificationType type, String title, String body,
                                              Object metadata, Long senderId, String senderName,
                                              Long recipientId, String recipientName) {
        if (type == null) {
            throw new IllegalArgumentException("通知タイプは必須です");
        }
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("タイトルは必須です");
        }
        if (recipientId == null) {
            throw new IllegalArgumentException("受信者IDは必須です");
        }
        
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(body != null ? body : "");
        notification.setRead(false);
        
        // メタデータの設定
        if (metadata != null) {
            try {
                if (metadata instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> metadataMap = (Map<String, Object>) metadata;
                    notification.setMetadata(metadataMap);
                } else {
                    // オブジェクトをMapに変換
                    String json = objectMapper.writeValueAsString(metadata);
                    Map<String, Object> metadataMap = objectMapper.readValue(json, HashMap.class);
                    notification.setMetadata(metadataMap);
                }
            } catch (Exception e) {
                logger.warn("メタデータの変換に失敗しました", e);
                notification.setMetadata(new HashMap<>());
            }
        }
        
        // 送信者情報
        notification.setSenderId(senderId);
        notification.setSenderName(senderName);
        
        // 受信者情報
        notification.setRecipientId(recipientId);
        notification.setRecipientName(recipientName);
        
        // 重要度とアクションURLの取得
        if (notification.getMetadata() != null) {
            notification.setImportance((String) notification.getMetadataValue("importance"));
            notification.setActionUrl((String) notification.getMetadataValue("action_url"));
        }
        
        // 日時の設定
        LocalDateTime now = LocalDateTime.now();
        notification.setCreatedAt(now);
        notification.setUpdatedAt(now);
        
        Notification savedNotification = notificationRepository.save(notification);
        return new NotificationDto(savedNotification);
    }
}