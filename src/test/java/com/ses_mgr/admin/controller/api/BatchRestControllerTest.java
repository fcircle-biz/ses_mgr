package com.ses_mgr.admin.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.admin.dto.batch.*;
import com.ses_mgr.admin.service.BatchHistoryService;
import com.ses_mgr.admin.service.BatchJobService;
import com.ses_mgr.admin.service.BatchScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BatchRestController.class)
@WithMockUser(username = "admin", roles = "ADMIN")
@Import(BatchTestConfig.class)
class BatchRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BatchJobService batchJobService;

    @MockBean
    private BatchScheduleService batchScheduleService;

    @MockBean
    private BatchHistoryService batchHistoryService;
    
    // Security related mock beans
    @MockBean
    private com.ses_mgr.config.JwtTokenProvider jwtTokenProvider;
    
    @MockBean
    private com.ses_mgr.config.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<BatchJobDto> batchJobs;
    private BatchJobDetailDto batchJobDetail;
    private List<BatchScheduleDto> batchSchedules;
    private List<BatchHistoryItemDto> batchHistoryItems;
    private BatchJobStatusUpdateRequestDto statusUpdateRequest;
    private BatchJobExecuteRequestDto executeRequest;
    private BatchJobExecuteResponseDto executeResponse;
    private BatchScheduleCreateRequestDto scheduleCreateRequest;
    private BatchScheduleDto scheduleDto;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
        testUserId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        
        // テスト用データの作成
        batchJobs = Arrays.asList(
            BatchJobDto.builder()
                .id("batch001")
                .name("日次データ集計")
                .description("前日のデータを集計し、サマリーテーブルを更新するバッチ")
                .category("report")
                .jobType("daily_summary")
                .status("active")
                .parameters(Map.of("targetTables", Arrays.asList("project_stats", "engineer_stats")))
                .lastRun(BatchJobDto.LastRunInfo.builder()
                        .status("success")
                        .startTime("2025-05-04T01:00:00Z")
                        .endTime("2025-05-04T01:10:25Z")
                        .executionTime(625)
                        .build())
                .nextRun("2025-05-05T01:00:00Z")
                .build(),
            BatchJobDto.builder()
                .id("batch002")
                .name("月次請求データ作成")
                .description("月次の請求データを作成するバッチ")
                .category("billing")
                .jobType("monthly_billing")
                .status("active")
                .build()
        );
        
        // バッチジョブ詳細
        batchJobDetail = BatchJobDetailDto.builder()
                .id("batch001")
                .name("日次データ集計")
                .description("前日のデータを集計し、サマリーテーブルを更新するバッチ")
                .category("report")
                .jobType("daily_summary")
                .status("active")
                .cronExpression("0 0 1 * * ?")
                .parameters(Map.of("targetTables", Arrays.asList("project_stats", "engineer_stats")))
                .lastRun(BatchJobDetailDto.ExecutionDetail.builder()
                        .status("success")
                        .startTime("2025-05-04T01:00:00Z")
                        .endTime("2025-05-04T01:10:25Z")
                        .executionTime(625)
                        .recordsProcessed(15420)
                        .message("処理が正常に完了しました")
                        .build())
                .executionHistory(Arrays.asList(
                        BatchJobDetailDto.ExecutionHistoryItem.builder()
                                .executionId("exec12345")
                                .status("success")
                                .startTime("2025-05-04T01:00:00Z")
                                .endTime("2025-05-04T01:10:25Z")
                                .executionTime(625)
                                .recordsProcessed(15420)
                                .build()
                ))
                .nextRun("2025-05-05T01:00:00Z")
                .schedules(Arrays.asList(
                        BatchJobDetailDto.ScheduleInfo.builder()
                                .id("schedule001")
                                .cronExpression("0 0 1 * * ?")
                                .description("毎日午前1時に実行")
                                .isActive(true)
                                .build()
                ))
                .build();
        
        // バッチスケジュール
        batchSchedules = Arrays.asList(
            BatchScheduleDto.builder()
                .id("schedule001")
                .jobId("batch001")
                .jobName("日次データ集計")
                .description("毎日午前1時に実行")
                .cronExpression("0 0 1 * * ?")
                .type("daily")
                .isActive(true)
                .nextRun("2025-05-05T01:00:00Z")
                .build(),
            BatchScheduleDto.builder()
                .id("schedule002")
                .jobId("batch002")
                .jobName("月次請求データ作成")
                .description("毎月1日午前2時に実行")
                .cronExpression("0 0 2 1 * ?")
                .type("monthly")
                .isActive(true)
                .nextRun("2025-06-01T02:00:00Z")
                .build()
        );
        
        // バッチ実行履歴
        batchHistoryItems = Arrays.asList(
            BatchHistoryItemDto.builder()
                .executionId("exec12345")
                .jobId("batch001")
                .jobName("日次データ集計")
                .status("success")
                .startTime("2025-05-04T01:00:00Z")
                .endTime("2025-05-04T01:10:25Z")
                .executionTime(625)
                .executedBy("scheduler")
                .triggerType("scheduled")
                .recordsProcessed(15420)
                .build(),
            BatchHistoryItemDto.builder()
                .executionId("exec12346")
                .jobId("batch001")
                .jobName("日次データ集計")
                .status("error")
                .startTime("2025-05-03T01:00:00Z")
                .endTime("2025-05-03T01:05:15Z")
                .executionTime(315)
                .executedBy("scheduler")
                .triggerType("scheduled")
                .errorMessage("データベース接続エラー")
                .build()
        );
        
        // ステータス更新リクエスト
        statusUpdateRequest = BatchJobStatusUpdateRequestDto.builder()
                .status("inactive")
                .reason("システムメンテナンスのため一時的に無効化")
                .build();
        
        // バッチ実行リクエスト
        executeRequest = BatchJobExecuteRequestDto.builder()
                .async(true)
                .description("テスト実行")
                .parameters(Map.of("targetDate", "2025-05-01"))
                .build();
        
        // バッチ実行レスポンス
        executeResponse = BatchJobExecuteResponseDto.builder()
                .message("バッチジョブの実行を開始しました")
                .executionId("exec12347")
                .jobId("batch001")
                .jobName("日次データ集計")
                .startTime("2025-05-04T15:30:00Z")
                .status("running")
                .async(true)
                .statusUrl("/api/v1/admin/batch/history/exec12347")
                .build();
        
        // スケジュール作成リクエスト
        scheduleCreateRequest = BatchScheduleCreateRequestDto.builder()
                .jobId("batch001")
                .description("毎週月曜日午前5時に実行")
                .cronExpression("0 0 5 ? * MON")
                .type("weekly")
                .isActive(true)
                .parameters(Map.of("mode", "full"))
                .build();
        
        // スケジュールDTO
        scheduleDto = BatchScheduleDto.builder()
                .id("schedule003")
                .jobId("batch001")
                .jobName("日次データ集計")
                .description("毎週月曜日午前5時に実行")
                .cronExpression("0 0 5 ? * MON")
                .type("weekly")
                .isActive(true)
                .nextRun("2025-05-06T05:00:00Z")
                .parameters(Map.of("mode", "full"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getBatchJobs_ReturnsAllJobs() throws Exception {
        // モックの設定
        Page<BatchJobDto> jobsPage = new PageImpl<>(batchJobs);
        when(batchJobService.getBatchJobs(nullable(String.class), nullable(String.class), nullable(String.class), 
                anyInt(), anyInt(), nullable(String.class)))
                .thenReturn(jobsPage);
        
        // APIリクエスト実行
        mockMvc.perform(get("/api/v1/admin/batch/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[0].id").value("batch001"))
                .andExpect(jsonPath("$.data.items[0].name").value("日次データ集計"))
                .andExpect(jsonPath("$.data.items[0].category").value("report"))
                .andExpect(jsonPath("$.data.items[0].status").value("active"))
                .andExpect(jsonPath("$.data.items[0].lastRun.status").value("success"))
                .andExpect(jsonPath("$.data.items[0].nextRun").value("2025-05-05T01:00:00Z"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.limit").value(20));
        
        // モックの検証
        verify(batchJobService, times(1)).getBatchJobs(nullable(String.class), nullable(String.class), nullable(String.class), 
                anyInt(), anyInt(), nullable(String.class));
    }

    @Test
    void getBatchJobs_WithParameters_ReturnsFilteredJobs() throws Exception {
        // モックの設定
        Page<BatchJobDto> jobsPage = new PageImpl<>(Collections.singletonList(batchJobs.get(0)));
        when(batchJobService.getBatchJobs(eq("active"), eq("report"), eq("集計"), eq(1), eq(10), eq("name:asc")))
                .thenReturn(jobsPage);
        
        // APIリクエスト実行
        mockMvc.perform(get("/api/v1/admin/batch/jobs")
                .param("status", "active")
                .param("category", "report")
                .param("search", "集計")
                .param("page", "1")
                .param("limit", "10")
                .param("sort", "name:asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].id").value("batch001"))
                .andExpect(jsonPath("$.data.items[0].category").value("report"))
                .andExpect(jsonPath("$.data.total").value(1));
        
        // モックの検証
        verify(batchJobService, times(1)).getBatchJobs("active", "report", "集計", 1, 10, "name:asc");
    }

    @Test
    void getBatchJobById_WithValidId_ReturnsJobDetail() throws Exception {
        // モックの設定
        when(batchJobService.getBatchJobById("batch001")).thenReturn(batchJobDetail);
        
        // APIリクエスト実行
        mockMvc.perform(get("/api/v1/admin/batch/jobs/batch001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value("batch001"))
                .andExpect(jsonPath("$.data.name").value("日次データ集計"))
                .andExpect(jsonPath("$.data.category").value("report"))
                .andExpect(jsonPath("$.data.status").value("active"))
                .andExpect(jsonPath("$.data.cronExpression").value("0 0 1 * * ?"))
                .andExpect(jsonPath("$.data.lastRun.status").value("success"))
                .andExpect(jsonPath("$.data.executionHistory", hasSize(1)))
                .andExpect(jsonPath("$.data.schedules", hasSize(1)));
        
        // モックの検証
        verify(batchJobService, times(1)).getBatchJobById("batch001");
    }

    @Test
    void getBatchJobById_WithInvalidId_ReturnsNotFound() throws Exception {
        // モックの設定
        when(batchJobService.getBatchJobById("invalid-id"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "指定されたバッチジョブが見つかりません"));
        
        // APIリクエスト実行
        mockMvc.perform(get("/api/v1/admin/batch/jobs/invalid-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error.code").value("BATCH_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("指定されたバッチジョブが見つかりません"));
        
        // モックの検証
        verify(batchJobService, times(1)).getBatchJobById("invalid-id");
    }

    @Test
    void executeBatchJob_WithValidIdAndParameters_ReturnsAcceptedResponse() throws Exception {
        // モックの設定
        when(batchJobService.executeBatchJob(eq("batch001"), any(BatchJobExecuteRequestDto.class), anyString()))
                .thenReturn(executeResponse);
        
        // APIリクエスト実行
        mockMvc.perform(post("/api/v1/admin/batch/jobs/batch001/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(executeRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.message").value("バッチジョブの実行を開始しました"))
                .andExpect(jsonPath("$.data.executionId").value("exec12347"))
                .andExpect(jsonPath("$.data.jobId").value("batch001"))
                .andExpect(jsonPath("$.data.jobName").value("日次データ集計"))
                .andExpect(jsonPath("$.data.status").value("running"))
                .andExpect(jsonPath("$.data.async").value(true))
                .andExpect(jsonPath("$.data.statusUrl").value("/api/v1/admin/batch/history/exec12347"));
        
        // モックの検証
        verify(batchJobService, times(1)).executeBatchJob(eq("batch001"), any(BatchJobExecuteRequestDto.class), anyString());
    }

    @Test
    void executeBatchJob_WithNoParameters_UsesDefaultParameters() throws Exception {
        // モックの設定
        when(batchJobService.executeBatchJob(eq("batch001"), any(BatchJobExecuteRequestDto.class), anyString()))
                .thenReturn(executeResponse);
        
        // APIリクエスト実行（リクエストボディなし）
        mockMvc.perform(post("/api/v1/admin/batch/jobs/batch001/execute")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());
        
        // モックの検証
        verify(batchJobService, times(1)).executeBatchJob(eq("batch001"), any(BatchJobExecuteRequestDto.class), anyString());
    }

    @Test
    void updateBatchJobStatus_WithValidRequest_ReturnsUpdatedJob() throws Exception {
        // モックの設定
        BatchJobDto updatedJob = batchJobs.get(0).toBuilder().status("inactive").build();
        when(batchJobService.updateBatchJobStatus(eq("batch001"), any(BatchJobStatusUpdateRequestDto.class), any(UUID.class)))
                .thenReturn(updatedJob);
        
        // APIリクエスト実行
        mockMvc.perform(put("/api/v1/admin/batch/jobs/batch001/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value("batch001"))
                .andExpect(jsonPath("$.data.name").value("日次データ集計"))
                .andExpect(jsonPath("$.data.status").value("inactive"));
        
        // モックの検証
        verify(batchJobService, times(1)).updateBatchJobStatus(eq("batch001"), any(BatchJobStatusUpdateRequestDto.class), any(UUID.class));
    }

    @Test
    void getBatchSchedules_ReturnsAllSchedules() throws Exception {
        // モックの設定
        Page<BatchScheduleDto> schedulesPage = new PageImpl<>(batchSchedules);
        when(batchScheduleService.getBatchSchedules(nullable(String.class), nullable(Boolean.class), nullable(String.class), 
                anyInt(), anyInt()))
                .thenReturn(schedulesPage);
        
        // APIリクエスト実行
        mockMvc.perform(get("/api/v1/admin/batch/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[0].id").value("schedule001"))
                .andExpect(jsonPath("$.data.items[0].jobId").value("batch001"))
                .andExpect(jsonPath("$.data.items[0].jobName").value("日次データ集計"))
                .andExpect(jsonPath("$.data.items[0].cronExpression").value("0 0 1 * * ?"))
                .andExpect(jsonPath("$.data.items[0].type").value("daily"))
                .andExpect(jsonPath("$.data.items[0].isActive").value(true))
                .andExpect(jsonPath("$.data.items[0].nextRun").value("2025-05-05T01:00:00Z"))
                .andExpect(jsonPath("$.data.total").value(2));
        
        // モックの検証
        verify(batchScheduleService, times(1)).getBatchSchedules(nullable(String.class), nullable(Boolean.class), nullable(String.class), 
                anyInt(), anyInt());
    }

    @Test
    void createBatchSchedule_WithValidRequest_ReturnsCreatedSchedule() throws Exception {
        // モックの設定
        when(batchScheduleService.createBatchSchedule(any(BatchScheduleCreateRequestDto.class), any(UUID.class)))
                .thenReturn(scheduleDto);
        
        // APIリクエスト実行
        mockMvc.perform(post("/api/v1/admin/batch/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(scheduleCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value("schedule003"))
                .andExpect(jsonPath("$.data.jobId").value("batch001"))
                .andExpect(jsonPath("$.data.jobName").value("日次データ集計"))
                .andExpect(jsonPath("$.data.description").value("毎週月曜日午前5時に実行"))
                .andExpect(jsonPath("$.data.cronExpression").value("0 0 5 ? * MON"))
                .andExpect(jsonPath("$.data.type").value("weekly"))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.data.nextRun").value("2025-05-06T05:00:00Z"));
        
        // モックの検証
        verify(batchScheduleService, times(1)).createBatchSchedule(any(BatchScheduleCreateRequestDto.class), any(UUID.class));
    }

    @Test
    void updateBatchSchedule_WithValidRequest_ReturnsUpdatedSchedule() throws Exception {
        // テストデータ
        BatchScheduleUpdateRequestDto updateRequest = new BatchScheduleUpdateRequestDto();
        updateRequest.setCronExpression("0 0 6 ? * MON");
        updateRequest.setDescription("毎週月曜日午前6時に実行（更新）");
        updateRequest.setIsActive(true);
        
        // 更新後のスケジュール
        BatchScheduleDto updatedSchedule = scheduleDto.toBuilder()
                .cronExpression("0 0 6 ? * MON")
                .description("毎週月曜日午前6時に実行（更新）")
                .nextRun("2025-05-06T06:00:00Z")
                .build();
        
        // モックの設定
        when(batchScheduleService.updateBatchSchedule(eq("schedule003"), any(BatchScheduleUpdateRequestDto.class), any(UUID.class)))
                .thenReturn(updatedSchedule);
        
        // APIリクエスト実行
        mockMvc.perform(put("/api/v1/admin/batch/schedules/schedule003")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value("schedule003"))
                .andExpect(jsonPath("$.data.description").value("毎週月曜日午前6時に実行（更新）"))
                .andExpect(jsonPath("$.data.cronExpression").value("0 0 6 ? * MON"))
                .andExpect(jsonPath("$.data.nextRun").value("2025-05-06T06:00:00Z"));
        
        // モックの検証
        verify(batchScheduleService, times(1)).updateBatchSchedule(eq("schedule003"), any(BatchScheduleUpdateRequestDto.class), any(UUID.class));
    }

    @Test
    void deleteBatchSchedule_WithValidId_ReturnsSuccess() throws Exception {
        // モックの設定
        when(batchScheduleService.deleteBatchSchedule("schedule001")).thenReturn(true);
        
        // APIリクエスト実行
        mockMvc.perform(delete("/api/v1/admin/batch/schedules/schedule001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.message").value("スケジュールが正常に削除されました"))
                .andExpect(jsonPath("$.data.id").value("schedule001"));
        
        // モックの検証
        verify(batchScheduleService, times(1)).deleteBatchSchedule("schedule001");
    }

    @Test
    void getBatchHistory_ReturnsAllHistory() throws Exception {
        // モックの設定
        Page<BatchHistoryItemDto> historyPage = new PageImpl<>(batchHistoryItems);
        when(batchHistoryService.getBatchHistory(nullable(String.class), nullable(String.class), 
                nullable(LocalDateTime.class), nullable(LocalDateTime.class), nullable(String.class), 
                anyInt(), anyInt(), nullable(String.class)))
                .thenReturn(historyPage);
        
        // APIリクエスト実行
        mockMvc.perform(get("/api/v1/admin/batch/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[0].executionId").value("exec12345"))
                .andExpect(jsonPath("$.data.items[0].jobId").value("batch001"))
                .andExpect(jsonPath("$.data.items[0].jobName").value("日次データ集計"))
                .andExpect(jsonPath("$.data.items[0].status").value("success"))
                .andExpect(jsonPath("$.data.items[0].startTime").value("2025-05-04T01:00:00Z"))
                .andExpect(jsonPath("$.data.items[0].executedBy").value("scheduler"))
                .andExpect(jsonPath("$.data.items[0].triggerType").value("scheduled"))
                .andExpect(jsonPath("$.data.total").value(2));
        
        // モックの検証
        verify(batchHistoryService, times(1)).getBatchHistory(nullable(String.class), nullable(String.class), 
                nullable(LocalDateTime.class), nullable(LocalDateTime.class), nullable(String.class), 
                anyInt(), anyInt(), nullable(String.class));
    }

    @Test
    void getBatchHistoryById_WithValidId_ReturnsHistoryItem() throws Exception {
        // モックの設定
        when(batchHistoryService.getBatchHistoryById("exec12345")).thenReturn(batchHistoryItems.get(0));
        
        // APIリクエスト実行
        mockMvc.perform(get("/api/v1/admin/batch/history/exec12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.executionId").value("exec12345"))
                .andExpect(jsonPath("$.data.jobId").value("batch001"))
                .andExpect(jsonPath("$.data.jobName").value("日次データ集計"))
                .andExpect(jsonPath("$.data.status").value("success"))
                .andExpect(jsonPath("$.data.startTime").value("2025-05-04T01:00:00Z"))
                .andExpect(jsonPath("$.data.endTime").value("2025-05-04T01:10:25Z"))
                .andExpect(jsonPath("$.data.executionTime").value(625))
                .andExpect(jsonPath("$.data.executedBy").value("scheduler"))
                .andExpect(jsonPath("$.data.triggerType").value("scheduled"))
                .andExpect(jsonPath("$.data.recordsProcessed").value(15420));
        
        // モックの検証
        verify(batchHistoryService, times(1)).getBatchHistoryById("exec12345");
    }
}