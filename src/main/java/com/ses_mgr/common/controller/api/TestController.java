package com.ses_mgr.common.controller.api;

import com.ses_mgr.common.dto.ApiResponseDto;
import com.ses_mgr.common.dto.NotificationDto;
import com.ses_mgr.common.entity.NotificationType;
import com.ses_mgr.common.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simple test controller for debugging API access
 */
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private final NotificationService notificationService;

    @Autowired
    public TestController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Public test endpoint successful");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/private")
    public ResponseEntity<Map<String, Object>> privateTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Private test endpoint successful");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-notification")
    public ResponseEntity<ApiResponseDto<NotificationDto>> createTestNotification() {
        try {
            // テスト用固定ユーザーID
            Long userId = 1L;
            
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
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "テスト通知の作成中にエラーが発生しました。" + e.getMessage()));
        }
    }
}