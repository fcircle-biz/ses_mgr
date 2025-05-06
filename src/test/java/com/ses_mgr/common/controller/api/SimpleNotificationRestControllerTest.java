package com.ses_mgr.common.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.common.dto.NotificationDto;
import com.ses_mgr.common.dto.NotificationListResponseDto;
import com.ses_mgr.common.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 通知コントローラの簡易テストクラス
 * セキュリティやフィルターを省略した単純なテスト実装
 */
public class SimpleNotificationRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationRestController notificationRestController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private UUID notificationId;
    private NotificationDto notificationDto;
    private NotificationListResponseDto listResponseDto;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        // MockMvcの設定（セキュリティフィルターを省略）
        mockMvc = MockMvcBuilders.standaloneSetup(notificationRestController).build();
        
        // テスト用データの準備
        notificationId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // 通知DTO
        notificationDto = new NotificationDto();
        notificationDto.setId(notificationId);
        notificationDto.setType("task");
        notificationDto.setTitle("テスト通知");
        notificationDto.setBody("これはテスト通知です。");
        notificationDto.setRead(false);
        notificationDto.setCreatedAt(now);
        notificationDto.setUpdatedAt(now);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("task_id", "123");
        metadata.put("importance", "high");
        notificationDto.setMetadata(metadata);

        // 通知一覧レスポンスDTO
        List<NotificationDto> notifications = new ArrayList<>();
        notifications.add(notificationDto);
        
        NotificationListResponseDto.PaginationDto pagination = 
                new NotificationListResponseDto.PaginationDto(0, 20, 1, 1);
        
        NotificationListResponseDto.SummaryDto summary = 
                new NotificationListResponseDto.SummaryDto(1, 1);
        
        listResponseDto = new NotificationListResponseDto(notifications, pagination, summary);
    }

    @Test
    public void testGetNotifications() throws Exception {
        // モックの設定
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "created_at"));
        when(notificationService.getNotifications(eq(userId), isNull(), isNull(), eq(pageable)))
                .thenReturn(listResponseDto);

        // リクエスト実行と検証
        mockMvc.perform(get("/api/v1/common/notifications")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].id").value(notificationId.toString()))
                .andExpect(jsonPath("$.data.data[0].type").value("task"))
                .andExpect(jsonPath("$.data.data[0].title").value("テスト通知"))
                .andExpect(jsonPath("$.data.pagination.current_page").value(0))
                .andExpect(jsonPath("$.data.summary.unread_count").value(1));
    }

    @Test
    public void testGetNotificationById() throws Exception {
        // モックの設定
        Long userId = 1L;
        when(notificationService.getNotificationById(eq(notificationId), eq(userId)))
                .thenReturn(notificationDto);

        // リクエスト実行と検証
        mockMvc.perform(get("/api/v1/common/notifications/" + notificationId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(notificationId.toString()))
                .andExpect(jsonPath("$.data.type").value("task"))
                .andExpect(jsonPath("$.data.title").value("テスト通知"));
    }

    @Test
    public void testMarkAsRead() throws Exception {
        // 既読に設定
        NotificationDto readNotification = new NotificationDto();
        readNotification.setId(notificationId);
        readNotification.setRead(true);
        readNotification.setUpdatedAt(LocalDateTime.now());

        // モックの設定
        Long userId = 1L;
        when(notificationService.markAsRead(eq(notificationId), eq(userId)))
                .thenReturn(readNotification);

        // リクエスト実行と検証
        mockMvc.perform(put("/api/v1/common/notifications/" + notificationId + "/read")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(notificationId.toString()))
                .andExpect(jsonPath("$.data.read").value(true));
    }

    @Test
    public void testMarkAllAsRead() throws Exception {
        // モックの設定
        Long userId = 1L;
        when(notificationService.markAllAsRead(eq(userId), isNull()))
                .thenReturn(5);

        // リクエスト実行と検証
        mockMvc.perform(put("/api/v1/common/notifications/read-all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.marked_as_read").value(5));
    }

    @Test
    public void testDeleteNotification() throws Exception {
        // モックの設定
        Long userId = 1L;
        doNothing().when(notificationService).deleteNotification(eq(notificationId), eq(userId));

        // リクエスト実行と検証
        mockMvc.perform(delete("/api/v1/common/notifications/" + notificationId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}