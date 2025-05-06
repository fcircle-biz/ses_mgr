package com.ses_mgr.admin.controller.api;

import com.ses_mgr.admin.dto.batch.*;
import com.ses_mgr.admin.service.BatchHistoryService;
import com.ses_mgr.admin.service.BatchJobService;
import com.ses_mgr.admin.service.BatchScheduleService;
import com.ses_mgr.common.dto.ApiResponseDto;
import com.ses_mgr.common.dto.PaginationResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/batch")
@Validated
public class BatchRestController {

    private static final Logger logger = LoggerFactory.getLogger(BatchRestController.class);

    @Autowired
    private BatchJobService batchJobService;

    @Autowired
    private BatchScheduleService batchScheduleService;

    @Autowired
    private BatchHistoryService batchHistoryService;

    /**
     * バッチジョブ一覧を取得する
     */
    @GetMapping("/jobs")
    public ResponseEntity<ApiResponseDto<Object>> getBatchJobs(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "1") @Min(1) Integer page,
            @RequestParam(required = false, defaultValue = "20") @Min(1) Integer limit,
            @RequestParam(required = false) String sort) {
        
        try {
            Page<BatchJobDto> jobsPage = batchJobService.getBatchJobs(status, category, search, page, limit, sort);
            
            PaginationResponseDto<BatchJobDto> response = new PaginationResponseDto<>();
            response.setItems(jobsPage.getContent());
            response.setTotal(jobsPage.getTotalElements());
            response.setPage(page);
            response.setLimit(limit);
            response.setPages(jobsPage.getTotalPages());
            
            return ResponseEntity.ok(ApiResponseDto.success(response));
        } catch (Exception e) {
            logger.error("バッチジョブ一覧の取得中にエラーが発生しました: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("BATCH_ERROR", "バッチジョブ一覧の取得中にエラーが発生しました。"));
        }
    }

    /**
     * 特定のバッチジョブの詳細情報を取得する
     */
    @GetMapping("/jobs/{id}")
    public ResponseEntity<ApiResponseDto<Object>> getBatchJobById(@PathVariable("id") String jobId) {
        try {
            BatchJobDetailDto jobDetail = batchJobService.getBatchJobById(jobId);
            return ResponseEntity.ok(ApiResponseDto.success(jobDetail));
        } catch (ResponseStatusException e) {
            logger.warn("バッチジョブの取得中にエラーが発生しました: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponseDto.error("BATCH_NOT_FOUND", e.getReason()));
        } catch (Exception e) {
            logger.error("バッチジョブの取得中にエラーが発生しました: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("BATCH_ERROR", "バッチジョブの取得中にエラーが発生しました。"));
        }
    }

    /**
     * バッチジョブを実行する
     */
    @PostMapping("/jobs/{id}/execute")
    public ResponseEntity<ApiResponseDto<Object>> executeBatchJob(
            @PathVariable("id") String jobId,
            @Valid @RequestBody(required = false) BatchJobExecuteRequestDto executeRequest,
            Authentication authentication) {
        
        try {
            // リクエストがnullの場合は空のリクエストを作成
            if (executeRequest == null) {
                executeRequest = new BatchJobExecuteRequestDto();
            }
            
            // 認証情報からユーザーIDを取得
            String executedBy = "user:" + authentication.getName();
            
            BatchJobExecuteResponseDto result = batchJobService.executeBatchJob(jobId, executeRequest, executedBy);
            
            // 非同期実行の場合は202 Accepted、同期実行の場合は200 OKを返す
            return result.getAsync() 
                    ? ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponseDto.success(result))
                    : ResponseEntity.ok(ApiResponseDto.success(result));
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                // 既に実行中の場合は409 Conflictを返す
                logger.warn("バッチジョブは既に実行中です: {}", e.getReason());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponseDto.error("BATCH_ALREADY_RUNNING", "指定されたバッチジョブは既に実行中です"));
            } else {
                logger.warn("バッチジョブの実行中にエラーが発生しました: {}", e.getReason());
                return ResponseEntity.status(e.getStatusCode())
                        .body(ApiResponseDto.error("BATCH_EXECUTION_ERROR", e.getReason()));
            }
        } catch (Exception e) {
            logger.error("バッチジョブの実行中にエラーが発生しました: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("BATCH_ERROR", "バッチジョブの実行中にエラーが発生しました。"));
        }
    }

    /**
     * バッチジョブのステータスを変更する
     */
    @PutMapping("/jobs/{id}/status")
    public ResponseEntity<ApiResponseDto<Object>> updateBatchJobStatus(
            @PathVariable("id") String jobId,
            @Valid @RequestBody BatchJobStatusUpdateRequestDto statusUpdateRequest,
            Authentication authentication) {
        
        try {
            // 認証情報からユーザーIDを取得
            UUID updatedBy = UUID.fromString(authentication.getName());
            
            BatchJobDto updatedJob = batchJobService.updateBatchJobStatus(jobId, statusUpdateRequest, updatedBy);
            
            return ResponseEntity.ok(ApiResponseDto.success(updatedJob));
        } catch (ResponseStatusException e) {
            logger.warn("バッチジョブのステータス変更中にエラーが発生しました: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponseDto.error("BATCH_STATUS_ERROR", e.getReason()));
        } catch (Exception e) {
            logger.error("バッチジョブのステータス変更中にエラーが発生しました: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("BATCH_ERROR", "バッチジョブのステータス変更中にエラーが発生しました。"));
        }
    }

    /**
     * バッチスケジュール一覧を取得する
     */
    @GetMapping("/schedules")
    public ResponseEntity<ApiResponseDto<Object>> getBatchSchedules(
            @RequestParam(required = false) String jobId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "1") @Min(1) Integer page,
            @RequestParam(required = false, defaultValue = "20") @Min(1) Integer limit) {
        
        try {
            Page<BatchScheduleDto> schedulesPage = 
                    batchScheduleService.getBatchSchedules(jobId, isActive, type, page, limit);
            
            PaginationResponseDto<BatchScheduleDto> response = new PaginationResponseDto<>();
            response.setItems(schedulesPage.getContent());
            response.setTotal(schedulesPage.getTotalElements());
            response.setPage(page);
            response.setLimit(limit);
            response.setPages(schedulesPage.getTotalPages());
            
            return ResponseEntity.ok(ApiResponseDto.success(response));
        } catch (Exception e) {
            logger.error("バッチスケジュール一覧の取得中にエラーが発生しました: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("BATCH_ERROR", "バッチスケジュール一覧の取得中にエラーが発生しました。"));
        }
    }

    /**
     * バッチスケジュールを登録する
     */
    @PostMapping("/schedules")
    public ResponseEntity<ApiResponseDto<Object>> createBatchSchedule(
            @Valid @RequestBody BatchScheduleCreateRequestDto createRequest,
            Authentication authentication) {
        
        try {
            // 認証情報からユーザーIDを取得
            UUID createdBy = UUID.fromString(authentication.getName());
            
            BatchScheduleDto createdSchedule = batchScheduleService.createBatchSchedule(createRequest, createdBy);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(createdSchedule));
        } catch (ResponseStatusException e) {
            logger.warn("バッチスケジュールの登録中にエラーが発生しました: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponseDto.error("BATCH_SCHEDULE_ERROR", e.getReason()));
        } catch (Exception e) {
            logger.error("バッチスケジュールの登録中にエラーが発生しました: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("BATCH_ERROR", "バッチスケジュールの登録中にエラーが発生しました。"));
        }
    }

    /**
     * バッチスケジュールを更新する
     */
    @PutMapping("/schedules/{id}")
    public ResponseEntity<ApiResponseDto<Object>> updateBatchSchedule(
            @PathVariable("id") String scheduleId,
            @Valid @RequestBody BatchScheduleUpdateRequestDto updateRequest,
            Authentication authentication) {
        
        try {
            // 認証情報からユーザーIDを取得
            UUID updatedBy = UUID.fromString(authentication.getName());
            
            BatchScheduleDto updatedSchedule = batchScheduleService.updateBatchSchedule(scheduleId, updateRequest, updatedBy);
            
            return ResponseEntity.ok(ApiResponseDto.success(updatedSchedule));
        } catch (ResponseStatusException e) {
            logger.warn("バッチスケジュールの更新中にエラーが発生しました: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponseDto.error("BATCH_SCHEDULE_ERROR", e.getReason()));
        } catch (Exception e) {
            logger.error("バッチスケジュールの更新中にエラーが発生しました: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("BATCH_ERROR", "バッチスケジュールの更新中にエラーが発生しました。"));
        }
    }

    /**
     * バッチスケジュールを削除する
     */
    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<ApiResponseDto<Object>> deleteBatchSchedule(
            @PathVariable("id") String scheduleId) {
        
        try {
            boolean result = batchScheduleService.deleteBatchSchedule(scheduleId);
            
            if (result) {
                return ResponseEntity.ok(ApiResponseDto.success(Map.of(
                        "message", "スケジュールが正常に削除されました",
                        "id", scheduleId
                )));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponseDto.error("BATCH_SCHEDULE_ERROR", "スケジュールの削除に失敗しました。"));
            }
        } catch (ResponseStatusException e) {
            logger.warn("バッチスケジュールの削除中にエラーが発生しました: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponseDto.error("BATCH_SCHEDULE_ERROR", e.getReason()));
        } catch (Exception e) {
            logger.error("バッチスケジュールの削除中にエラーが発生しました: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("BATCH_ERROR", "バッチスケジュールの削除中にエラーが発生しました。"));
        }
    }

    /**
     * バッチ実行履歴を取得する
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponseDto<Object>> getBatchHistory(
            @RequestParam(required = false) String jobId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String executedBy,
            @RequestParam(required = false, defaultValue = "1") @Min(1) Integer page,
            @RequestParam(required = false, defaultValue = "50") @Min(1) Integer limit,
            @RequestParam(required = false, defaultValue = "startTime:desc") String sort) {
        
        try {
            Page<BatchHistoryItemDto> historyPage = 
                    batchHistoryService.getBatchHistory(jobId, status, from, to, executedBy, page, limit, sort);
            
            PaginationResponseDto<BatchHistoryItemDto> response = new PaginationResponseDto<>();
            response.setItems(historyPage.getContent());
            response.setTotal(historyPage.getTotalElements());
            response.setPage(page);
            response.setLimit(limit);
            response.setPages(historyPage.getTotalPages());
            
            return ResponseEntity.ok(ApiResponseDto.success(response));
        } catch (ResponseStatusException e) {
            logger.warn("バッチ実行履歴の取得中にエラーが発生しました: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponseDto.error("BATCH_HISTORY_ERROR", e.getReason()));
        } catch (Exception e) {
            logger.error("バッチ実行履歴の取得中にエラーが発生しました: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("BATCH_ERROR", "バッチ実行履歴の取得中にエラーが発生しました。"));
        }
    }

    /**
     * 特定の実行IDのバッチ実行履歴を取得する
     */
    @GetMapping("/history/{id}")
    public ResponseEntity<ApiResponseDto<Object>> getBatchHistoryById(@PathVariable("id") String executionId) {
        try {
            BatchHistoryItemDto historyItem = batchHistoryService.getBatchHistoryById(executionId);
            return ResponseEntity.ok(ApiResponseDto.success(historyItem));
        } catch (ResponseStatusException e) {
            logger.warn("バッチ実行履歴の取得中にエラーが発生しました: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponseDto.error("BATCH_HISTORY_ERROR", e.getReason()));
        } catch (Exception e) {
            logger.error("バッチ実行履歴の取得中にエラーが発生しました: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("BATCH_ERROR", "バッチ実行履歴の取得中にエラーが発生しました。"));
        }
    }
}