package com.ses_mgr.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.admin.dto.batch.BatchJobDetailDto;
import com.ses_mgr.admin.dto.batch.BatchJobDto;
import com.ses_mgr.admin.dto.batch.BatchJobExecuteRequestDto;
import com.ses_mgr.admin.dto.batch.BatchJobStatusUpdateRequestDto;
import com.ses_mgr.admin.entity.BatchExecutionHistory;
import com.ses_mgr.admin.entity.BatchJob;
import com.ses_mgr.admin.entity.BatchSchedule;
import com.ses_mgr.admin.repository.BatchExecutionHistoryRepository;
import com.ses_mgr.admin.repository.BatchJobDependencyRepository;
import com.ses_mgr.admin.repository.BatchJobRepository;
import com.ses_mgr.admin.repository.BatchScheduleRepository;
import com.ses_mgr.admin.service.impl.BatchJobServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchJobServiceTest {

    @Mock
    private BatchJobRepository batchJobRepository;

    @Mock
    private BatchScheduleRepository batchScheduleRepository;

    @Mock
    private BatchExecutionHistoryRepository batchExecutionHistoryRepository;

    @Mock
    private BatchJobDependencyRepository batchJobDependencyRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private BatchJobServiceImpl batchJobService;

    private BatchJob testBatchJob;
    private BatchSchedule testBatchSchedule;
    private BatchExecutionHistory testBatchExecutionHistory;
    private List<BatchJob> testBatchJobs;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
        
        testUserId = UUID.randomUUID();
        
        // テスト用データの作成
        testBatchJob = BatchJob.builder()
                .jobId("batch001")
                .name("日次データ集計")
                .description("前日のデータを集計し、サマリーテーブルを更新するバッチ")
                .category("report")
                .jobType("daily_summary")
                .status("active")
                .parameters("{\"targetTables\": [\"project_stats\", \"engineer_stats\"], \"daysToKeep\": 365}")
                .createdAt(LocalDateTime.now().minusDays(10))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();
        
        testBatchSchedule = BatchSchedule.builder()
                .scheduleId("schedule001")
                .jobId("batch001")
                .description("毎日午前1時に実行")
                .cronExpression("0 0 1 * * ?")
                .scheduleType("daily")
                .isActive(true)
                .nextRun(LocalDateTime.now().plusDays(1).withHour(1).withMinute(0).withSecond(0).withNano(0))
                .createdAt(LocalDateTime.now().minusDays(10))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();
        
        testBatchExecutionHistory = BatchExecutionHistory.builder()
                .executionId("exec12345")
                .jobId("batch001")
                .scheduleId("schedule001")
                .status("success")
                .startTime(LocalDateTime.now().minusDays(1).withHour(1).withMinute(0).withSecond(0))
                .endTime(LocalDateTime.now().minusDays(1).withHour(1).withMinute(10).withSecond(25))
                .executionTime(625)
                .executedBy("scheduler")
                .triggerType("scheduled")
                .recordsProcessed(15420)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        
        testBatchJobs = new ArrayList<>();
        testBatchJobs.add(testBatchJob);
        testBatchJobs.add(BatchJob.builder()
                .jobId("batch002")
                .name("月次請求データ作成")
                .description("月次の請求データを作成するバッチ")
                .category("billing")
                .jobType("monthly_billing")
                .status("active")
                .parameters("{\"targetMonth\": \"current\", \"formatVersion\": \"v2\"}")
                .createdAt(LocalDateTime.now().minusDays(10))
                .updatedAt(LocalDateTime.now().minusHours(2))
                .build());
    }

    @Test
    void getBatchJobs_ReturnsPageOfBatchJobs() {
        // モックの設定
        Page<BatchJob> batchJobsPage = new PageImpl<>(testBatchJobs);
        when(batchJobRepository.searchBatchJobs(anyString(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(batchJobsPage);
        
        // メソッド呼び出し
        Page<BatchJobDto> result = batchJobService.getBatchJobs("active", "report", "データ", 1, 20, "name:asc");
        
        // 検証
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("batch001", result.getContent().get(0).getId());
        assertEquals("日次データ集計", result.getContent().get(0).getName());
        
        verify(batchJobRepository, times(1)).searchBatchJobs(anyString(), anyString(), anyString(), any(Pageable.class));
    }

    @Test
    void getBatchJobById_WithValidId_ReturnsBatchJobDetailDto() {
        // モックの設定
        when(batchJobRepository.findByJobId("batch001")).thenReturn(Optional.of(testBatchJob));
        when(batchScheduleRepository.findByJobId("batch001")).thenReturn(Collections.singletonList(testBatchSchedule));
        when(batchJobDependencyRepository.findByJobId("batch001")).thenReturn(Collections.emptyList());
        when(batchExecutionHistoryRepository.findTop10ByJobIdOrderByStartTimeDesc("batch001"))
                .thenReturn(Collections.singletonList(testBatchExecutionHistory));
        
        // メソッド呼び出し
        BatchJobDetailDto result = batchJobService.getBatchJobById("batch001");
        
        // 検証
        assertNotNull(result);
        assertEquals("batch001", result.getId());
        assertEquals("日次データ集計", result.getName());
        assertEquals("report", result.getCategory());
        assertNotNull(result.getLastRun());
        assertEquals("success", result.getLastRun().getStatus());
        assertNotNull(result.getSchedules());
        assertEquals(1, result.getSchedules().size());
        
        verify(batchJobRepository, times(1)).findByJobId("batch001");
        verify(batchScheduleRepository, times(1)).findByJobId("batch001");
        verify(batchJobDependencyRepository, times(1)).findByJobId("batch001");
        verify(batchExecutionHistoryRepository, times(1)).findTop10ByJobIdOrderByStartTimeDesc("batch001");
    }

    @Test
    void getBatchJobById_WithInvalidId_ThrowsResponseStatusException() {
        // モックの設定
        when(batchJobRepository.findByJobId("invalid-id")).thenReturn(Optional.empty());
        
        // メソッド呼び出しと例外の検証
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            batchJobService.getBatchJobById("invalid-id");
        });
        
        // 例外メッセージの検証
        assertTrue(exception.getMessage().contains("指定されたバッチジョブが見つかりません"));
        
        verify(batchJobRepository, times(1)).findByJobId("invalid-id");
    }

    @Test
    void updateBatchJobStatus_WithValidId_UpdatesStatus() {
        // テストデータ
        BatchJobStatusUpdateRequestDto updateRequest = new BatchJobStatusUpdateRequestDto();
        updateRequest.setStatus("inactive");
        updateRequest.setReason("テスト用に無効化");
        
        // モックの設定
        when(batchJobRepository.findByJobId("batch001")).thenReturn(Optional.of(testBatchJob));
        when(batchJobRepository.save(any(BatchJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // メソッド呼び出し
        BatchJobDto result = batchJobService.updateBatchJobStatus("batch001", updateRequest, testUserId);
        
        // 検証
        assertNotNull(result);
        assertEquals("batch001", result.getId());
        assertEquals("inactive", result.getStatus());
        
        verify(batchJobRepository, times(1)).findByJobId("batch001");
        verify(batchJobRepository, times(1)).save(any(BatchJob.class));
    }

    @Test
    void executeBatchJob_WithValidIdAndAsyncTrue_ReturnsSuccessResponse() {
        // テストデータ
        BatchJobExecuteRequestDto executeRequest = new BatchJobExecuteRequestDto();
        executeRequest.setAsync(true);
        executeRequest.setDescription("テスト実行");
        executeRequest.setParameters(Map.of("targetDate", "2025-05-01"));
        
        // モックの設定
        when(batchJobRepository.findByJobId("batch001")).thenReturn(Optional.of(testBatchJob));
        when(batchExecutionHistoryRepository.findRunningJobsById("batch001")).thenReturn(Collections.emptyList());
        when(batchExecutionHistoryRepository.save(any(BatchExecutionHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // メソッド呼び出し
        var result = batchJobService.executeBatchJob("batch001", executeRequest, "user:testuser");
        
        // 検証
        assertNotNull(result);
        assertEquals("batch001", result.getJobId());
        assertEquals("日次データ集計", result.getJobName());
        assertEquals("running", result.getStatus());
        assertTrue(result.getAsync());
        assertNotNull(result.getExecutionId());
        assertNotNull(result.getStartTime());
        assertTrue(result.getStatusUrl().contains("/api/v1/admin/batch/history/"));
        
        verify(batchJobRepository, times(1)).findByJobId("batch001");
        verify(batchExecutionHistoryRepository, times(1)).findRunningJobsById("batch001");
        verify(batchExecutionHistoryRepository, times(1)).save(any(BatchExecutionHistory.class));
    }

    @Test
    void executeBatchJob_WithInactiveJob_ThrowsResponseStatusException() {
        // テストデータ
        testBatchJob.setStatus("inactive");
        BatchJobExecuteRequestDto executeRequest = new BatchJobExecuteRequestDto();
        executeRequest.setAsync(true);
        
        // モックの設定
        when(batchJobRepository.findByJobId("batch001")).thenReturn(Optional.of(testBatchJob));
        
        // メソッド呼び出しと例外の検証
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            batchJobService.executeBatchJob("batch001", executeRequest, "user:testuser");
        });
        
        // 例外メッセージの検証
        assertTrue(exception.getMessage().contains("無効化されているバッチジョブは実行できません"));
        
        verify(batchJobRepository, times(1)).findByJobId("batch001");
    }

    @Test
    void executeBatchJob_WithAlreadyRunningJob_ThrowsResponseStatusException() {
        // テストデータ
        BatchJobExecuteRequestDto executeRequest = new BatchJobExecuteRequestDto();
        executeRequest.setAsync(true);
        BatchExecutionHistory runningJob = BatchExecutionHistory.builder()
                .executionId("exec99999")
                .jobId("batch001")
                .status("running")
                .startTime(LocalDateTime.now().minusMinutes(5))
                .executedBy("user:otheruser")
                .triggerType("manual")
                .build();
        
        // モックの設定
        when(batchJobRepository.findByJobId("batch001")).thenReturn(Optional.of(testBatchJob));
        when(batchExecutionHistoryRepository.findRunningJobsById("batch001"))
                .thenReturn(Collections.singletonList(runningJob));
        
        // メソッド呼び出しと例外の検証
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            batchJobService.executeBatchJob("batch001", executeRequest, "user:testuser");
        });
        
        // 例外メッセージの検証
        assertTrue(exception.getMessage().contains("指定されたバッチジョブは既に実行中です"));
        
        verify(batchJobRepository, times(1)).findByJobId("batch001");
        verify(batchExecutionHistoryRepository, times(1)).findRunningJobsById("batch001");
    }

    @Test
    void updateJobExecutionStatus_WithValidId_UpdatesStatus() {
        // テストデータ
        LocalDateTime endTime = LocalDateTime.now();
        
        // モックの設定
        when(batchExecutionHistoryRepository.findByExecutionId("exec12345"))
                .thenReturn(Optional.of(testBatchExecutionHistory));
        when(batchExecutionHistoryRepository.save(any(BatchExecutionHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // メソッド呼び出し
        boolean result = batchJobService.updateJobExecutionStatus("exec12345", "error", endTime, 12345, "エラーが発生しました");
        
        // 検証
        assertTrue(result);
        
        verify(batchExecutionHistoryRepository, times(1)).findByExecutionId("exec12345");
        verify(batchExecutionHistoryRepository, times(1)).save(any(BatchExecutionHistory.class));
    }
}