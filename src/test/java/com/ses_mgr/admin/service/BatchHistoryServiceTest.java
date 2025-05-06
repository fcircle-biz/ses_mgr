package com.ses_mgr.admin.service;

import com.ses_mgr.admin.dto.batch.BatchHistoryItemDto;
import com.ses_mgr.admin.entity.BatchExecutionHistory;
import com.ses_mgr.admin.entity.BatchJob;
import com.ses_mgr.admin.repository.BatchExecutionHistoryRepository;
import com.ses_mgr.admin.service.impl.BatchHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchHistoryServiceTest {

    @Mock
    private BatchExecutionHistoryRepository batchExecutionHistoryRepository;

    @InjectMocks
    private BatchHistoryServiceImpl batchHistoryService;

    private BatchJob testBatchJob;
    private BatchExecutionHistory testHistory;
    private List<BatchExecutionHistory> testHistoryList;

    @BeforeEach
    void setUp() {
        // バッチジョブの準備
        testBatchJob = BatchJob.builder()
                .jobId("batch001")
                .name("日次レポート生成")
                .description("毎日の売上レポートを生成します")
                .category("report")
                .jobType("java")
                .status("active")
                .parameters("{\"format\":\"pdf\",\"includeGraphs\":true}")
                .createdAt(LocalDateTime.now().minusDays(10))
                .updatedAt(LocalDateTime.now().minusDays(5))
                .build();

        // 実行履歴の準備
        LocalDateTime now = LocalDateTime.now();
        testHistory = BatchExecutionHistory.builder()
                .executionId("history001")
                .jobId("batch001")
                .batchJob(testBatchJob)
                .status("completed")
                .startTime(now.minusHours(2))
                .endTime(now.minusHours(1))
                .executionTime(3600)
                .executedBy("scheduler")
                .triggerType("scheduled")
                .parameters("{\"format\":\"pdf\",\"includeGraphs\":true}")
                .createdAt(now.minusHours(2))
                .build();

        // 実行履歴リストの準備
        testHistoryList = Arrays.asList(
                testHistory,
                BatchExecutionHistory.builder()
                        .executionId("history002")
                        .jobId("batch001")
                        .batchJob(testBatchJob)
                        .status("failed")
                        .startTime(now.minusDays(1))
                        .endTime(now.minusDays(1).plusMinutes(10))
                        .executionTime(600)
                        .executedBy("user:admin")
                        .triggerType("manual")
                        .errorMessage("ネットワークエラーが発生しました")
                        .parameters("{\"format\":\"pdf\",\"includeGraphs\":true}")
                        .createdAt(now.minusDays(1))
                        .build()
        );
    }

    @Test
    void getBatchHistory_ReturnsPageOfHistory() {
        // 準備
        Pageable pageable = Pageable.unpaged();
        Page<BatchExecutionHistory> historyPage = new PageImpl<>(testHistoryList);
        
        when(batchExecutionHistoryRepository.searchBatchHistory(
            eq(null), eq(null), eq(null), eq(null), eq(null), eq(pageable))
        ).thenReturn(historyPage);
        
        // 実行
        Page<BatchHistoryItemDto> resultPage = batchHistoryService.getBatchHistory(
            null, null, null, null, null, null, null, null
        );
        
        // 検証
        assertNotNull(resultPage);
        assertEquals(2, resultPage.getContent().size());
        assertEquals("history001", resultPage.getContent().get(0).getExecutionId());
        assertEquals("history002", resultPage.getContent().get(1).getExecutionId());
        assertEquals("batch001", resultPage.getContent().get(0).getJobId());
        assertEquals("日次レポート生成", resultPage.getContent().get(0).getJobName());
        assertEquals("completed", resultPage.getContent().get(0).getStatus());
        assertEquals("failed", resultPage.getContent().get(1).getStatus());
        assertEquals("ネットワークエラーが発生しました", resultPage.getContent().get(1).getErrorMessage());
    }

    @Test
    void getBatchHistoryById_WithValidId_ReturnsHistory() {
        // 準備
        when(batchExecutionHistoryRepository.findByExecutionId("history001")).thenReturn(Optional.of(testHistory));
        
        // 実行
        BatchHistoryItemDto result = batchHistoryService.getBatchHistoryById("history001");
        
        // 検証
        assertNotNull(result);
        assertEquals("history001", result.getExecutionId());
        assertEquals("batch001", result.getJobId());
        assertEquals("日次レポート生成", result.getJobName());
        assertEquals("completed", result.getStatus());
        assertEquals(3600, result.getExecutionTime());
        assertNull(result.getErrorMessage());
    }

    @Test
    void getBatchHistoryById_WithInvalidId_ThrowsException() {
        // 準備
        when(batchExecutionHistoryRepository.findByExecutionId("nonexistent")).thenReturn(Optional.empty());
        
        // 実行 & 検証
        assertThrows(ResponseStatusException.class, () -> 
            batchHistoryService.getBatchHistoryById("nonexistent")
        );
    }

    @Test
    void getBatchHistoryByJobId_ReturnsFilteredHistory() {
        // 準備
        Pageable pageable = Pageable.unpaged();
        Page<BatchExecutionHistory> historyPage = new PageImpl<>(testHistoryList);
        
        when(batchExecutionHistoryRepository.searchBatchHistory(
            eq("batch001"), eq(null), eq(null), eq(null), eq(null), eq(pageable))
        ).thenReturn(historyPage);
        
        // 実行
        Page<BatchHistoryItemDto> resultPage = batchHistoryService.getBatchHistory(
            "batch001", null, null, null, null, null, null, null
        );
        
        // 検証
        assertNotNull(resultPage);
        assertEquals(2, resultPage.getContent().size());
        assertEquals("history001", resultPage.getContent().get(0).getExecutionId());
        assertEquals("history002", resultPage.getContent().get(1).getExecutionId());
        assertEquals("batch001", resultPage.getContent().get(0).getJobId());
        assertEquals("日次レポート生成", resultPage.getContent().get(0).getJobName());
    }
}