package com.ses_mgr.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.common.dto.NotificationDto;
import com.ses_mgr.common.dto.NotificationListResponseDto;
import com.ses_mgr.common.entity.Notification;
import com.ses_mgr.common.entity.NotificationType;
import com.ses_mgr.common.repository.NotificationRepository;
import com.ses_mgr.common.service.impl.NotificationServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private UUID notificationId;
    private Notification notification;
    private Long userId;
    private Pageable pageable;

    @BeforeEach
    public void setup() {
        notificationId = UUID.randomUUID();
        userId = 1L;
        pageable = PageRequest.of(0, 20);

        // テスト用通知の作成
        notification = new Notification();
        notification.setId(notificationId);
        notification.setType(NotificationType.TASK);
        notification.setTitle("テスト通知");
        notification.setBody("これはテスト通知です。");
        notification.setRead(false);
        notification.setRecipientId(userId);
        notification.setRecipientName("テストユーザー");
        notification.setSenderId(2L);
        notification.setSenderName("送信者");
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("task_id", "123");
        metadata.put("importance", "high");
        notification.setMetadata(metadata);
    }

    @Test
    public void testGetNotifications() {
        // モックの設定
        List<Notification> notifications = Collections.singletonList(notification);
        Page<Notification> page = new PageImpl<>(notifications, pageable, 1);
        
        when(notificationRepository.findByRecipientId(eq(userId), any(Pageable.class)))
                .thenReturn(page);
        when(notificationRepository.countUnreadByRecipientId(userId))
                .thenReturn(1L);
        when(notificationRepository.countByRecipientId(userId))
                .thenReturn(1L);

        // サービス呼び出し
        NotificationListResponseDto result = notificationService.getNotifications(userId, null, null, pageable);

        // 検証
        assertNotNull(result);
        assertEquals(1, result.getNotifications().size());
        assertEquals(notificationId, result.getNotifications().get(0).getId());
        assertEquals("task", result.getNotifications().get(0).getType());
        assertEquals(1, result.getSummary().getUnreadCount());
        assertEquals(1, result.getSummary().getTotalCount());
        
        // リポジトリメソッドの呼び出し回数検証
        verify(notificationRepository, times(1)).findByRecipientId(eq(userId), any(Pageable.class));
        verify(notificationRepository, times(1)).countUnreadByRecipientId(userId);
        verify(notificationRepository, times(1)).countByRecipientId(userId);
    }

    @Test
    public void testGetNotificationsByTypeAndReadStatus() {
        // モックの設定
        List<Notification> notifications = Collections.singletonList(notification);
        Page<Notification> page = new PageImpl<>(notifications, pageable, 1);
        
        when(notificationRepository.findByRecipientIdAndTypeAndRead(eq(userId), eq(NotificationType.TASK), eq(false), any(Pageable.class)))
                .thenReturn(page);
        when(notificationRepository.countUnreadByRecipientId(userId))
                .thenReturn(1L);
        when(notificationRepository.countByRecipientId(userId))
                .thenReturn(1L);

        // サービス呼び出し
        NotificationListResponseDto result = notificationService.getNotifications(userId, "task", "unread", pageable);

        // 検証
        assertNotNull(result);
        assertEquals(1, result.getNotifications().size());
        assertEquals("task", result.getNotifications().get(0).getType());
        
        // リポジトリメソッドの呼び出し検証
        verify(notificationRepository, times(1)).findByRecipientIdAndTypeAndRead(eq(userId), eq(NotificationType.TASK), eq(false), any(Pageable.class));
    }

    @Test
    public void testGetNotificationById() {
        // モックの設定
        when(notificationRepository.findByIdAndRecipientId(notificationId, userId))
                .thenReturn(Optional.of(notification));

        // サービス呼び出し
        NotificationDto result = notificationService.getNotificationById(notificationId, userId);

        // 検証
        assertNotNull(result);
        assertEquals(notificationId, result.getId());
        assertEquals("task", result.getType());
        assertEquals("テスト通知", result.getTitle());
        
        // リポジトリメソッドの呼び出し検証
        verify(notificationRepository, times(1)).findByIdAndRecipientId(notificationId, userId);
    }

    @Test
    public void testGetNotificationByIdNotFound() {
        // モックの設定
        when(notificationRepository.findByIdAndRecipientId(notificationId, userId))
                .thenReturn(Optional.empty());

        // サービス呼び出し & 例外検証
        assertThrows(EntityNotFoundException.class, () -> {
            notificationService.getNotificationById(notificationId, userId);
        });
        
        // リポジトリメソッドの呼び出し検証
        verify(notificationRepository, times(1)).findByIdAndRecipientId(notificationId, userId);
    }

    @Test
    public void testMarkAsRead() {
        // モックの設定
        when(notificationRepository.findByIdAndRecipientId(notificationId, userId))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(notification);

        // サービス呼び出し
        NotificationDto result = notificationService.markAsRead(notificationId, userId);

        // 検証
        assertNotNull(result);
        assertTrue(notification.isRead());
        
        // リポジトリメソッドの呼び出し検証
        verify(notificationRepository, times(1)).findByIdAndRecipientId(notificationId, userId);
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    public void testMarkAllAsRead() {
        // モックの設定
        LocalDateTime now = LocalDateTime.now();
        when(notificationRepository.markAllAsReadByRecipientId(eq(userId), any(LocalDateTime.class)))
                .thenReturn(5);

        // サービス呼び出し
        int result = notificationService.markAllAsRead(userId, null);

        // 検証
        assertEquals(5, result);
        
        // リポジトリメソッドの呼び出し検証
        verify(notificationRepository, times(1)).markAllAsReadByRecipientId(eq(userId), any(LocalDateTime.class));
    }

    @Test
    public void testMarkAllAsReadByType() {
        // モックの設定
        when(notificationRepository.markAllAsReadByRecipientIdAndType(eq(userId), eq(NotificationType.TASK), any(LocalDateTime.class)))
                .thenReturn(3);

        // サービス呼び出し
        int result = notificationService.markAllAsRead(userId, "task");

        // 検証
        assertEquals(3, result);
        
        // リポジトリメソッドの呼び出し検証
        verify(notificationRepository, times(1)).markAllAsReadByRecipientIdAndType(eq(userId), eq(NotificationType.TASK), any(LocalDateTime.class));
    }

    @Test
    public void testDeleteNotification() {
        // モックの設定
        when(notificationRepository.findByIdAndRecipientId(notificationId, userId))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.softDelete(eq(notificationId), eq(userId), any(LocalDateTime.class)))
                .thenReturn(1);

        // サービス呼び出し
        notificationService.deleteNotification(notificationId, userId);
        
        // リポジトリメソッドの呼び出し検証
        verify(notificationRepository, times(1)).findByIdAndRecipientId(notificationId, userId);
        verify(notificationRepository, times(1)).softDelete(eq(notificationId), eq(userId), any(LocalDateTime.class));
    }

    @Test
    public void testCreateNotification() {
        // モックの設定
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> {
                    Notification savedNotification = invocation.getArgument(0);
                    savedNotification.setId(notificationId);
                    return savedNotification;
                });

        // メタデータ
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("task_id", "123");
        metadata.put("importance", "high");
        metadata.put("action_url", "/tasks/123");

        // サービス呼び出し
        NotificationDto result = notificationService.createNotification(
                NotificationType.TASK,
                "新しいタスク",
                "新しいタスクが作成されました。",
                metadata,
                2L,
                "システム",
                userId,
                "テストユーザー"
        );

        // 検証
        assertNotNull(result);
        assertEquals(notificationId, result.getId());
        assertEquals("task", result.getType());
        assertEquals("新しいタスク", result.getTitle());
        assertEquals("high", result.getImportance());
        assertEquals("/tasks/123", result.getActionUrl());
        
        // リポジトリメソッドの呼び出し検証
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}