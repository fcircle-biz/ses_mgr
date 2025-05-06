package com.ses_mgr.admin.service;

import com.ses_mgr.admin.dto.batch.BatchScheduleCreateRequestDto;
import com.ses_mgr.admin.dto.batch.BatchScheduleDto;
import com.ses_mgr.admin.dto.batch.BatchScheduleUpdateRequestDto;
import com.ses_mgr.admin.entity.BatchJob;
import com.ses_mgr.admin.entity.BatchSchedule;
import com.ses_mgr.admin.repository.BatchJobRepository;
import com.ses_mgr.admin.repository.BatchScheduleRepository;
import com.ses_mgr.admin.service.impl.BatchScheduleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.CronExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchScheduleServiceTest {

    @Mock
    private BatchScheduleRepository batchScheduleRepository;

    @Mock
    private BatchJobRepository batchJobRepository;

    @InjectMocks
    private BatchScheduleServiceImpl batchScheduleService;

    private BatchJob testBatchJob;
    private BatchSchedule testSchedule;
    private List<BatchSchedule> testSchedules;

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

        // バッチスケジュールの準備
        testSchedule = BatchSchedule.builder()
                .scheduleId("schedule001")
                .jobId("batch001")
                .batchJob(testBatchJob)
                .description("毎日午前5時に実行")
                .cronExpression("0 0 5 * * ?")
                .scheduleType("scheduled")
                .isActive(true)
                .parameters("{\"additionalParam\":\"value1\"}")
                .createdAt(LocalDateTime.now().minusDays(8))
                .updatedAt(LocalDateTime.now().minusDays(3))
                .build();

        // スケジュールリストの準備
        testSchedules = Arrays.asList(
                testSchedule,
                BatchSchedule.builder()
                        .scheduleId("schedule002")
                        .jobId("batch001")
                        .batchJob(testBatchJob)
                        .description("毎週月曜日午前9時に実行")
                        .cronExpression("0 0 9 ? * MON")
                        .scheduleType("scheduled")
                        .isActive(true)
                        .parameters("{}")
                        .createdAt(LocalDateTime.now().minusDays(7))
                        .updatedAt(LocalDateTime.now().minusDays(2))
                        .build()
        );
    }

    @Test
    void getAllBatchSchedules_ReturnsPageOfSchedules() {
        // 準備
        Page<BatchSchedule> schedulePage = new PageImpl<>(testSchedules);
        
        when(batchScheduleRepository.searchBatchSchedules(eq(null), eq(null), eq(null), any(Pageable.class)))
            .thenReturn(schedulePage);
        
        // 実行
        Page<BatchScheduleDto> resultPage = batchScheduleService.getBatchSchedules(null, null, null, null, null);
        
        // 検証
        assertNotNull(resultPage);
        assertEquals(2, resultPage.getContent().size());
        assertEquals("schedule001", resultPage.getContent().get(0).getId());
        assertEquals("schedule002", resultPage.getContent().get(1).getId());
        assertEquals("batch001", resultPage.getContent().get(0).getJobId());
        assertEquals("毎日午前5時に実行", resultPage.getContent().get(0).getDescription());
    }

    @Test
    void getBatchScheduleById_WithValidId_ReturnsSchedule() {
        // 準備
        when(batchScheduleRepository.findByScheduleId("schedule001")).thenReturn(Optional.of(testSchedule));
        
        // 実行
        BatchScheduleDto result = batchScheduleService.updateNextRunTime("schedule001");
        
        // 検証
        assertNotNull(result);
        assertEquals("schedule001", result.getId());
        assertEquals("batch001", result.getJobId());
        assertEquals("毎日午前5時に実行", result.getDescription());
        assertEquals("0 0 5 * * ?", result.getCronExpression());
    }

    @Test
    void getBatchScheduleById_WithInvalidId_ThrowsException() {
        // 準備
        when(batchScheduleRepository.findByScheduleId("nonexistent")).thenReturn(Optional.empty());
        
        // 実行 & 検証
        assertThrows(ResponseStatusException.class, () -> 
            batchScheduleService.updateNextRunTime("nonexistent")
        );
    }

    @Test
    void createBatchSchedule_WithValidRequest_ReturnsCreatedSchedule() {
        // 準備
        BatchScheduleCreateRequestDto requestDto = new BatchScheduleCreateRequestDto();
        requestDto.setJobId("batch001");
        requestDto.setDescription("新しいスケジュール");
        requestDto.setCronExpression("0 0 12 * * ?");
        requestDto.setType("scheduled");
        requestDto.setIsActive(true);
        Map<String, Object> params = new HashMap<>();
        params.put("key", "value");
        requestDto.setParameters(params);

        BatchSchedule newSchedule = BatchSchedule.builder()
                .scheduleId("newSchedule")
                .jobId("batch001")
                .batchJob(testBatchJob)
                .description("新しいスケジュール")
                .cronExpression("0 0 12 * * ?")
                .scheduleType("scheduled")
                .isActive(true)
                .parameters("{\"key\":\"value\"}")
                .nextRun(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(batchJobRepository.findByJobId("batch001")).thenReturn(Optional.of(testBatchJob));
        when(batchScheduleRepository.save(any(BatchSchedule.class))).thenReturn(newSchedule);
        
        // 実行
        BatchScheduleDto result = batchScheduleService.createBatchSchedule(requestDto, UUID.randomUUID());
        
        // 検証
        assertNotNull(result);
        assertEquals("newSchedule", result.getId());
        assertEquals("batch001", result.getJobId());
        assertEquals("新しいスケジュール", result.getDescription());
        assertEquals("0 0 12 * * ?", result.getCronExpression());
        assertTrue(result.getIsActive());
    }

    @Test
    void updateBatchSchedule_WithValidRequest_ReturnsUpdatedSchedule() {
        // 準備
        BatchScheduleUpdateRequestDto requestDto = new BatchScheduleUpdateRequestDto();
        requestDto.setDescription("更新されたスケジュール");
        requestDto.setCronExpression("0 30 10 * * ?");
        requestDto.setIsActive(false);
        Map<String, Object> params = new HashMap<>();
        params.put("newKey", "newValue");
        requestDto.setParameters(params);

        BatchSchedule updatedSchedule = BatchSchedule.builder()
                .scheduleId("schedule001")
                .jobId("batch001")
                .batchJob(testBatchJob)
                .description("更新されたスケジュール")
                .cronExpression("0 30 10 * * ?")
                .scheduleType("scheduled")
                .isActive(false)
                .parameters("{\"newKey\":\"newValue\"}")
                .nextRun(LocalDateTime.now().plusDays(1))
                .createdAt(testSchedule.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(batchScheduleRepository.findByScheduleId("schedule001")).thenReturn(Optional.of(testSchedule));
        when(batchScheduleRepository.save(any(BatchSchedule.class))).thenReturn(updatedSchedule);
        
        // 実行
        BatchScheduleDto result = batchScheduleService.updateBatchSchedule("schedule001", requestDto, UUID.randomUUID());
        
        // 検証
        assertNotNull(result);
        assertEquals("schedule001", result.getId());
        assertEquals("更新されたスケジュール", result.getDescription());
        assertEquals("0 30 10 * * ?", result.getCronExpression());
        assertFalse(result.getIsActive());
    }

    @Test
    void deleteBatchSchedule_WithValidId_DeletesSchedule() {
        // 準備
        when(batchScheduleRepository.findByScheduleId("schedule001")).thenReturn(Optional.of(testSchedule));
        doNothing().when(batchScheduleRepository).delete(testSchedule);
        
        // 実行
        batchScheduleService.deleteBatchSchedule("schedule001");
        
        // 検証
        verify(batchScheduleRepository).delete(testSchedule);
    }

    @Test
    void deleteBatchSchedule_WithInvalidId_ThrowsException() {
        // 準備
        when(batchScheduleRepository.findByScheduleId("nonexistent")).thenReturn(Optional.empty());
        
        // 実行 & 検証
        assertThrows(ResponseStatusException.class, () -> 
            batchScheduleService.deleteBatchSchedule("nonexistent")
        );
        
        verify(batchScheduleRepository, never()).delete(any());
    }
}