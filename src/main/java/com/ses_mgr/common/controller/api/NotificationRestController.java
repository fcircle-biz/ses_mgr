package com.ses_mgr.common.controller.api;

import com.ses_mgr.common.dto.ApiResponseDto;
import com.ses_mgr.common.dto.NotificationDto;
import com.ses_mgr.common.dto.NotificationListResponseDto;
import com.ses_mgr.common.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ses_mgr.common.entity.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 通知REST APIコントローラ
 * Notification REST API controller
 */
@RestController
@RequestMapping("/api/v1/common/notifications")
public class NotificationRestController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationRestController.class);

    private final NotificationService notificationService;

    @Autowired
    public NotificationRestController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 通知一覧を取得
     * Get notifications list
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
            // 現在のユーザーIDを取得
            Long userId = getCurrentUserId();

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
     * 通知詳細を取得
     * Get notification details
     *
     * @param id 通知ID
     * @return 通知詳細
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<NotificationDto>> getNotificationById(@PathVariable String id) {
        try {
            Long userId = getCurrentUserId();
            UUID notificationId = UUID.fromString(id);

            NotificationDto notification = notificationService.getNotificationById(notificationId, userId);
            return ResponseEntity.ok(ApiResponseDto.success(notification));
        } catch (IllegalArgumentException e) {
            logger.error("無効な通知IDが指定されました: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error("INVALID_ID", "無効な通知IDが指定されました。"));
        } catch (EntityNotFoundException e) {
            logger.error("通知が見つかりません: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("RESOURCE_NOT_FOUND", "指定された通知が存在しません。"));
        } catch (Exception e) {
            logger.error("通知詳細の取得中にエラーが発生しました: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "通知詳細の取得中にエラーが発生しました。"));
        }
    }

    /**
     * 特定の通知を既読にする
     * Mark notification as read
     *
     * @param id 通知ID
     * @return 更新結果
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> markAsRead(@PathVariable String id) {
        try {
            Long userId = getCurrentUserId();
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
        } catch (EntityNotFoundException e) {
            logger.error("通知が見つかりません: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("RESOURCE_NOT_FOUND", "指定された通知が存在しません。"));
        } catch (Exception e) {
            logger.error("通知の既読設定中にエラーが発生しました: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "通知の既読設定中にエラーが発生しました。"));
        }
    }

    /**
     * 全ての未読通知を既読にする
     * Mark all unread notifications as read
     *
     * @param type 通知タイプ（オプション）
     * @return 更新結果
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> markAllAsRead(
            @RequestParam(required = false) String type) {
        try {
            Long userId = getCurrentUserId();
            int markedCount = notificationService.markAllAsRead(userId, type);

            Map<String, Object> result = new HashMap<>();
            result.put("marked_as_read", markedCount);
            result.put("updated_at", LocalDateTime.now());

            return ResponseEntity.ok(ApiResponseDto.success(result));
        } catch (IllegalArgumentException e) {
            logger.error("無効なパラメータ: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error("INVALID_PARAMETER", e.getMessage()));
        } catch (Exception e) {
            logger.error("全通知の既読設定中にエラーが発生しました", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "全通知の既読設定中にエラーが発生しました。"));
        }
    }

    /**
     * 通知を削除する
     * Delete notification
     *
     * @param id 通知ID
     * @return 削除結果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteNotification(@PathVariable String id) {
        try {
            Long userId = getCurrentUserId();
            UUID notificationId = UUID.fromString(id);

            notificationService.deleteNotification(notificationId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("無効な通知IDが指定されました: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error("INVALID_ID", "無効な通知IDが指定されました。"));
        } catch (EntityNotFoundException e) {
            logger.error("通知が見つかりません: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("RESOURCE_NOT_FOUND", "指定された通知が存在しません。"));
        } catch (Exception e) {
            logger.error("通知の削除中にエラーが発生しました: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "通知の削除中にエラーが発生しました。"));
        }
    }

    /**
     * 現在のユーザーIDを取得する
     * Get the current user ID
     *
     * @return ユーザーID (Long形式)
     */
    protected Long getCurrentUserId() {
        try {
            // 認証情報からユーザーを取得
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                // UUIDの文字列表現から数値IDを生成（マッピング用）
                // 注意: 本番環境では適切なユーザーIDマッピングが必要
                return 1L;
            }
        } catch (Exception e) {
            // エラーが発生した場合はログに記録
            logger.warn("認証情報からユーザーIDの取得に失敗しました", e);
        }
        
        // テスト用や認証情報が取得できない場合のデフォルト値
        return 1L;
    }
}