package com.ses_mgr.common.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.common.dto.NotificationDto;
import com.ses_mgr.common.dto.NotificationListResponseDto;
import com.ses_mgr.common.entity.NotificationType;
import com.ses_mgr.common.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Bean;
import com.ses_mgr.config.TestSecurityConfig;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import com.ses_mgr.config.WithMockCustomUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationRestController.class, 
        useDefaultFilters = false, 
        includeFilters = {
            @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = NotificationRestController.class
            )
        },
        excludeFilters = {
            @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                    com.ses_mgr.config.SecurityConfig.class,
                    com.ses_mgr.config.JwtAuthenticationFilter.class
                }
            )
        })
@Import({TestSecurityConfig.class, NotificationRestControllerTest.TestConfig.class})
public class NotificationRestControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public NotificationRestController notificationRestController(NotificationService notificationService) {
            return new NotificationRestController(notificationService) {
                @Override
                protected Long getCurrentUserId() {
                    return 1L; // Mock to match our test setup
                }
            };
        }
    }

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private com.ses_mgr.config.JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID notificationId;
    private NotificationDto notificationDto;
    private NotificationListResponseDto listResponseDto;

    @BeforeEach
    public void setup() {
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
    @WithMockCustomUser(userId = "00000000-0000-0000-0000-000000000001", loginId = "testuser")
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
    @WithMockCustomUser(userId = "00000000-0000-0000-0000-000000000001", loginId = "testuser")
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
    @WithMockCustomUser(userId = "00000000-0000-0000-0000-000000000001", loginId = "testuser")
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
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(notificationId.toString()))
                .andExpect(jsonPath("$.data.read").value(true));
    }

    @Test
    @WithMockCustomUser(userId = "00000000-0000-0000-0000-000000000001", loginId = "testuser")
    public void testMarkAllAsRead() throws Exception {
        // モックの設定
        Long userId = 1L;
        when(notificationService.markAllAsRead(eq(userId), isNull()))
                .thenReturn(5);

        // リクエスト実行と検証
        mockMvc.perform(put("/api/v1/common/notifications/read-all")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.marked_as_read").value(5));
    }

    @Test
    @WithMockCustomUser(userId = "00000000-0000-0000-0000-000000000001", loginId = "testuser")
    public void testDeleteNotification() throws Exception {
        // モックの設定
        Long userId = 1L;
        doNothing().when(notificationService).deleteNotification(eq(notificationId), eq(userId));

        // リクエスト実行と検証
        mockMvc.perform(delete("/api/v1/common/notifications/" + notificationId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}