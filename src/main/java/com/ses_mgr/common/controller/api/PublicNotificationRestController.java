package com.ses_mgr.common.controller.api;

import com.ses_mgr.common.dto.ApiResponseDto;
import com.ses_mgr.common.dto.NotificationDto;
import com.ses_mgr.common.dto.NotificationListResponseDto;
import com.ses_mgr.common.entity.NotificationType;
import com.ses_mgr.common.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 公開通知REST APIコントローラ（認証なし、テスト用）
 * Public notification REST API controller (no authentication, for testing)
 */
@RestController
@RequestMapping("/api/v1/public/notifications")
public class PublicNotificationRestController {

    private static final Logger logger = LoggerFactory.getLogger(PublicNotificationRestController.class);

    private final NotificationService notificationService;

    @Autowired
    public PublicNotificationRestController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // テスト用の固定ユーザーID
    private static final Long TEST_USER_ID = 1L;

    /**
     * 通知一覧を取得（テスト用）
     * Get notifications list (for testing)
     *
     * @param page      ページ番号（0始まり）
     * @param pageSize  1ページあたりの通知数
     * @param type      通知タイプ
     * @param readStatus 既読状態
     * @param sort      ソート順
     * @return 通知一覧
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<NotificationListResponseDto>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(name = "read_status", required = false) String readStatus,
            @RequestParam(defaultValue = "created_at:desc") String sort) {

        try {
            // テスト用固定ユーザーIDを使用
            Long userId = TEST_USER_ID;

            // ソート情報の解析
            String[] sortParts = sort.split(":");
            String sortField = sortParts[0];
            Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") 
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, Math.min(pageSize, 100), Sort.by(direction, sortField));

            // 通知一覧の取得
            NotificationListResponseDto notifications = notificationService.getNotifications(userId, type, readStatus, pageable);

            return ResponseEntity.ok(ApiResponseDto.success(notifications));
        } catch (Exception e) {
            logger.error("通知一覧の取得中にエラーが発生しました", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "通知一覧の取得中にエラーが発生しました。"));
        }
    }

    /**
     * 通知詳細を取得（テスト用）
     * Get notification details (for testing)
     *
     * @param id 通知ID
     * @return 通知詳細
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<NotificationDto>> getNotificationById(@PathVariable String id) {
        try {
            Long userId = TEST_USER_ID;
            UUID notificationId = UUID.fromString(id);

            NotificationDto notification = notificationService.getNotificationById(notificationId, userId);
            return ResponseEntity.ok(ApiResponseDto.success(notification));
        } catch (IllegalArgumentException e) {
            logger.error("無効な通知IDが指定されました: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error("INVALID_ID", "無効な通知IDが指定されました。"));
        } catch (Exception e) {
            logger.error("通知詳細の取得中にエラーが発生しました: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "通知詳細の取得中にエラーが発生しました。"));
        }
    }

    /**
     * 特定の通知を既読にする（テスト用）
     * Mark notification as read (for testing)
     *
     * @param id 通知ID
     * @return 更新結果
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> markAsRead(@PathVariable String id) {
        try {
            Long userId = TEST_USER_ID;
            UUID notificationId = UUID.fromString(id);

            NotificationDto updatedNotification = notificationService.markAsRead(notificationId, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("id", updatedNotification.getId().toString());
            result.put("read", updatedNotification.isRead());
            result.put("updated_at", updatedNotification.getUpdatedAt());

            return ResponseEntity.ok(ApiResponseDto.success(result));
        } catch (IllegalArgumentException e) {
            logger.error("無効な通知IDが指定されました: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error("INVALID_ID", "無効な通知IDが指定されました。"));
        } catch (Exception e) {
            logger.error("通知の既読設定中にエラーが発生しました: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "通知の既読設定中にエラーが発生しました。"));
        }
    }

    /**
     * テスト通知の作成（テスト用）
     * Create test notification (for testing)
     *
     * @return 作成された通知
     */
    @PostMapping("/test")
    public ResponseEntity<ApiResponseDto<NotificationDto>> createTestNotification() {
        try {
            Long userId = TEST_USER_ID;
            
            // テスト用メタデータ
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("test_id", UUID.randomUUID().toString());
            metadata.put("importance", "medium");
            metadata.put("action_url", "/test");
            
            // テスト通知の作成
            NotificationDto notification = notificationService.createNotification(
                    NotificationType.SYSTEM, 
                    "テスト通知", 
                    "これはテスト通知です。" + LocalDateTime.now(), 
                    metadata, 
                    null, 
                    "システム", 
                    userId, 
                    "テストユーザー");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(notification));
        } catch (Exception e) {
            logger.error("テスト通知の作成中にエラーが発生しました", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "テスト通知の作成中にエラーが発生しました。"));
        }
    }
}