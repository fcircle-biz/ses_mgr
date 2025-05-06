package com.ses_mgr.admin.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.admin.dto.batch.*;
import com.ses_mgr.admin.entity.BatchExecutionHistory;
import com.ses_mgr.admin.entity.BatchJob;
import com.ses_mgr.admin.entity.BatchJobDependency;
import com.ses_mgr.admin.entity.BatchSchedule;
import com.ses_mgr.admin.repository.BatchExecutionHistoryRepository;
import com.ses_mgr.admin.repository.BatchJobDependencyRepository;
import com.ses_mgr.admin.repository.BatchJobRepository;
import com.ses_mgr.admin.repository.BatchScheduleRepository;
import com.ses_mgr.admin.service.BatchJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BatchJobServiceImpl implements BatchJobService {

    private static final Logger logger = LoggerFactory.getLogger(BatchJobServiceImpl.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    @Autowired
    private BatchJobRepository batchJobRepository;

    @Autowired
    private BatchScheduleRepository batchScheduleRepository;

    @Autowired
    private BatchExecutionHistoryRepository batchExecutionHistoryRepository;

    @Autowired
    private BatchJobDependencyRepository batchJobDependencyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Page<BatchJobDto> getBatchJobs(String status, String category, String search, 
                                        Integer page, Integer limit, String sort) {
        
        // ページネーションパラメータの処理
        int pageNumber = (page != null && page > 0) ? page - 1 : DEFAULT_PAGE - 1;
        int pageSize = (limit != null && limit > 0) ? Math.min(limit, MAX_LIMIT) : DEFAULT_LIMIT;
        
        // ソート条件の処理
        Pageable pageable;
        if (sort != null && !sort.isEmpty()) {
            String[] sortParams = sort.split(":");
            if (sortParams.length == 2) {
                String sortField = convertSortField(sortParams[0]);
                String sortDirection = sortParams[1].toLowerCase();
                if ("asc".equals(sortDirection)) {
                    pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, sortField));
                } else {
                    pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, sortField));
                }
            } else {
                pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
            }
        } else {
            pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
        }
        
        // バッチジョブ一覧を取得
        Page<BatchJob> batchJobs = batchJobRepository.searchBatchJobs(status, category, search, pageable);
        
        // DTOに変換
        return batchJobs.map(this::convertToBatchJobDto);
    }

    @Override
    public BatchJobDetailDto getBatchJobById(String jobId) {
        // バッチジョブを取得
        BatchJob batchJob = batchJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "指定されたバッチジョブが見つかりません"));
        
        // スケジュール情報を取得
        List<BatchSchedule> schedules = batchScheduleRepository.findByJobId(jobId);
        
        // 依存関係を取得
        List<BatchJobDependency> dependencies = batchJobDependencyRepository.findByJobId(jobId);
        
        // 実行履歴を取得（最新10件）
        List<BatchExecutionHistory> executionHistories = 
                batchExecutionHistoryRepository.findTop10ByJobIdOrderByStartTimeDesc(jobId);
        
        // 最新の実行履歴を取得
        Optional<BatchExecutionHistory> lastExecution = executionHistories.stream()
                .findFirst();
        
        // DTOに変換
        return convertToBatchJobDetailDto(batchJob, schedules, dependencies, executionHistories, lastExecution.orElse(null));
    }

    @Override
    @Transactional
    public BatchJobDto updateBatchJobStatus(String jobId, BatchJobStatusUpdateRequestDto statusUpdateRequest, UUID updatedBy) {
        // バッチジョブを取得
        BatchJob batchJob = batchJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "指定されたバッチジョブが見つかりません"));
        
        // ステータスを更新
        batchJob.setStatus(statusUpdateRequest.getStatus());
        batchJob.setUpdatedBy(updatedBy);
        
        // 保存
        batchJob = batchJobRepository.save(batchJob);
        
        // DTOに変換して返す
        BatchJobDto dto = convertToBatchJobDto(batchJob);
        dto.setStatus(statusUpdateRequest.getStatus());
        
        return dto;
    }

    @Override
    @Transactional
    public BatchJobExecuteResponseDto executeBatchJob(String jobId, BatchJobExecuteRequestDto executeRequest, String executedBy) {
        // バッチジョブを取得
        BatchJob batchJob = batchJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "指定されたバッチジョブが見つかりません"));
        
        // ステータスをチェック
        if (!"active".equals(batchJob.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "無効化されているバッチジョブは実行できません");
        }
        
        // 既に実行中かチェック
        List<BatchExecutionHistory> runningJobs = 
                batchExecutionHistoryRepository.findRunningJobsById(jobId);
        if (!runningJobs.isEmpty()) {
            BatchExecutionHistory runningJob = runningJobs.get(0);
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "指定されたバッチジョブは既に実行中です", 
                    new RuntimeException("ExecutionId: " + runningJob.getExecutionId()));
        }
        
        // 実行履歴を作成
        String executionId = generateExecutionId();
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // パラメータをJSONに変換
            String parametersJson = null;
            if (executeRequest.getParameters() != null) {
                parametersJson = objectMapper.writeValueAsString(executeRequest.getParameters());
            }
            
            BatchExecutionHistory executionHistory = BatchExecutionHistory.builder()
                    .executionId(executionId)
                    .jobId(jobId)
                    .status("running")
                    .startTime(startTime)
                    .executedBy(executedBy)
                    .triggerType("manual")
                    .parameters(parametersJson)
                    .description(executeRequest.getDescription())
                    .build();
            
            batchExecutionHistoryRepository.save(executionHistory);
            
            // 非同期実行の場合はここでレスポンスを返す
            if (executeRequest.getAsync()) {
                // 実際のバッチ実行はここで別スレッドで行う（実装省略）
                // TODO: 実際のバッチ処理実行ロジックを実装
                
                return BatchJobExecuteResponseDto.builder()
                        .message("バッチジョブの実行を開始しました")
                        .executionId(executionId)
                        .jobId(jobId)
                        .jobName(batchJob.getName())
                        .startTime(startTime.format(DATE_TIME_FORMATTER))
                        .status("running")
                        .async(true)
                        .statusUrl("/api/v1/admin/batch/history/" + executionId)
                        .build();
            } else {
                // 同期実行の場合は完了まで待機（実装省略）
                // TODO: 同期実行の場合の実装
                
                return BatchJobExecuteResponseDto.builder()
                        .message("バッチジョブの実行が完了しました")
                        .executionId(executionId)
                        .jobId(jobId)
                        .jobName(batchJob.getName())
                        .startTime(startTime.format(DATE_TIME_FORMATTER))
                        .status("success")
                        .async(false)
                        .statusUrl("/api/v1/admin/batch/history/" + executionId)
                        .build();
            }
        } catch (JsonProcessingException e) {
            logger.error("パラメータのJSON変換に失敗しました", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "パラメータのJSON変換に失敗しました", e);
        } catch (Exception e) {
            logger.error("バッチジョブの実行中にエラーが発生しました", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "バッチジョブの実行中にエラーが発生しました", e);
        }
    }

    @Override
    @Transactional
    public boolean updateJobExecutionStatus(String executionId, String status, LocalDateTime endTime, 
                                          Integer recordsProcessed, String errorMessage) {
        // 実行履歴を取得
        BatchExecutionHistory executionHistory = batchExecutionHistoryRepository.findByExecutionId(executionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "指定された実行履歴が見つかりません"));
        
        // ステータスを更新
        executionHistory.setStatus(status);
        executionHistory.setEndTime(endTime);
        executionHistory.setRecordsProcessed(recordsProcessed);
        executionHistory.setErrorMessage(errorMessage);
        
        // 実行時間を計算
        if (endTime != null && executionHistory.getStartTime() != null) {
            Duration duration = Duration.between(executionHistory.getStartTime(), endTime);
            executionHistory.setExecutionTime((int) duration.getSeconds());
        }
        
        // 保存
        batchExecutionHistoryRepository.save(executionHistory);
        
        return true;
    }

    // DTOへの変換メソッド
    private BatchJobDto convertToBatchJobDto(BatchJob batchJob) {
        BatchJobDto dto = new BatchJobDto();
        dto.setId(batchJob.getJobId());
        dto.setName(batchJob.getName());
        dto.setDescription(batchJob.getDescription());
        dto.setCategory(batchJob.getCategory());
        dto.setJobType(batchJob.getJobType());
        dto.setStatus(batchJob.getStatus());
        
        // パラメータをJSONからMapに変換
        if (batchJob.getParameters() != null && !batchJob.getParameters().isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> parametersMap = objectMapper.readValue(batchJob.getParameters(), Map.class);
                dto.setParameters(parametersMap);
            } catch (JsonProcessingException e) {
                logger.warn("パラメータのJSON解析に失敗しました: {}", e.getMessage());
            }
        }
        
        // 最新の実行情報を取得
        batchExecutionHistoryRepository.findTop10ByJobIdOrderByStartTimeDesc(batchJob.getJobId())
                .stream()
                .findFirst()
                .ifPresent(lastRun -> {
                    BatchJobDto.LastRunInfo lastRunInfo = new BatchJobDto.LastRunInfo();
                    lastRunInfo.setStatus(lastRun.getStatus());
                    lastRunInfo.setStartTime(lastRun.getStartTime().format(DATE_TIME_FORMATTER));
                    if (lastRun.getEndTime() != null) {
                        lastRunInfo.setEndTime(lastRun.getEndTime().format(DATE_TIME_FORMATTER));
                    }
                    lastRunInfo.setExecutionTime(lastRun.getExecutionTime());
                    lastRunInfo.setErrorMessage(lastRun.getErrorMessage());
                    
                    dto.setLastRun(lastRunInfo);
                });
        
        // 次回実行日時を取得
        batchScheduleRepository.findByJobId(batchJob.getJobId())
                .stream()
                .filter(BatchSchedule::getIsActive)
                .map(BatchSchedule::getNextRun)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .ifPresent(nextRun -> dto.setNextRun(nextRun.format(DATE_TIME_FORMATTER)));
        
        dto.setCreatedAt(batchJob.getCreatedAt());
        dto.setUpdatedAt(batchJob.getUpdatedAt());
        
        return dto;
    }

    private BatchJobDetailDto convertToBatchJobDetailDto(BatchJob batchJob, List<BatchSchedule> schedules,
                                                       List<BatchJobDependency> dependencies,
                                                       List<BatchExecutionHistory> executionHistories,
                                                       BatchExecutionHistory lastExecution) {
        BatchJobDetailDto dto = new BatchJobDetailDto();
        dto.setId(batchJob.getJobId());
        dto.setName(batchJob.getName());
        dto.setDescription(batchJob.getDescription());
        dto.setCategory(batchJob.getCategory());
        dto.setJobType(batchJob.getJobType());
        dto.setStatus(batchJob.getStatus());
        
        // スケジュールからcron式を取得
        schedules.stream()
                .filter(BatchSchedule::getIsActive)
                .findFirst()
                .ifPresent(schedule -> dto.setCronExpression(schedule.getCronExpression()));
        
        // パラメータをJSONからMapに変換
        if (batchJob.getParameters() != null && !batchJob.getParameters().isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> parametersMap = objectMapper.readValue(batchJob.getParameters(), Map.class);
                dto.setParameters(parametersMap);
            } catch (JsonProcessingException e) {
                logger.warn("パラメータのJSON解析に失敗しました: {}", e.getMessage());
            }
        }
        
        // 最新の実行情報を設定
        if (lastExecution != null) {
            BatchJobDetailDto.ExecutionDetail lastRunDetail = new BatchJobDetailDto.ExecutionDetail();
            lastRunDetail.setStatus(lastExecution.getStatus());
            lastRunDetail.setStartTime(lastExecution.getStartTime().format(DATE_TIME_FORMATTER));
            if (lastExecution.getEndTime() != null) {
                lastRunDetail.setEndTime(lastExecution.getEndTime().format(DATE_TIME_FORMATTER));
            }
            lastRunDetail.setExecutionTime(lastExecution.getExecutionTime());
            lastRunDetail.setRecordsProcessed(lastExecution.getRecordsProcessed());
            
            if ("success".equals(lastExecution.getStatus())) {
                lastRunDetail.setMessage("処理が正常に完了しました");
            } else if ("error".equals(lastExecution.getStatus())) {
                lastRunDetail.setErrorMessage(lastExecution.getErrorMessage());
            }
            
            // ログを解析（簡易実装）
            if (lastExecution.getLogs() != null && !lastExecution.getLogs().isEmpty()) {
                lastRunDetail.setLogs(Arrays.asList(lastExecution.getLogs().split("\n")));
            }
            
            dto.setLastRun(lastRunDetail);
        }
        
        // 実行履歴を変換
        List<BatchJobDetailDto.ExecutionHistoryItem> historyItems = executionHistories.stream()
                .map(history -> {
                    BatchJobDetailDto.ExecutionHistoryItem item = new BatchJobDetailDto.ExecutionHistoryItem();
                    item.setExecutionId(history.getExecutionId());
                    item.setStatus(history.getStatus());
                    item.setStartTime(history.getStartTime().format(DATE_TIME_FORMATTER));
                    if (history.getEndTime() != null) {
                        item.setEndTime(history.getEndTime().format(DATE_TIME_FORMATTER));
                    }
                    item.setExecutionTime(history.getExecutionTime());
                    item.setRecordsProcessed(history.getRecordsProcessed());
                    item.setErrorMessage(history.getErrorMessage());
                    return item;
                })
                .collect(Collectors.toList());
        
        dto.setExecutionHistory(historyItems);
        
        // 次回実行日時を取得
        schedules.stream()
                .filter(BatchSchedule::getIsActive)
                .map(BatchSchedule::getNextRun)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .ifPresent(nextRun -> dto.setNextRun(nextRun.format(DATE_TIME_FORMATTER)));
        
        // スケジュール情報を変換
        List<BatchJobDetailDto.ScheduleInfo> scheduleInfos = schedules.stream()
                .map(schedule -> {
                    BatchJobDetailDto.ScheduleInfo info = new BatchJobDetailDto.ScheduleInfo();
                    info.setId(schedule.getScheduleId());
                    info.setCronExpression(schedule.getCronExpression());
                    info.setDescription(schedule.getDescription());
                    info.setIsActive(schedule.getIsActive());
                    return info;
                })
                .collect(Collectors.toList());
        
        dto.setSchedules(scheduleInfos);
        
        // 依存関係を変換
        List<BatchJobDetailDto.DependencyInfo> dependencyInfos = dependencies.stream()
                .map(dependency -> {
                    BatchJobDetailDto.DependencyInfo info = new BatchJobDetailDto.DependencyInfo();
                    info.setJobId(dependency.getDependentJobId());
                    info.setType(dependency.getDependencyType());
                    
                    // 依存先ジョブ名を取得
                    batchJobRepository.findByJobId(dependency.getDependentJobId())
                            .ifPresent(dependentJob -> info.setJobName(dependentJob.getName()));
                    
                    return info;
                })
                .collect(Collectors.toList());
        
        dto.setDependencies(dependencyInfos);
        
        dto.setCreatedAt(batchJob.getCreatedAt());
        dto.setUpdatedAt(batchJob.getUpdatedAt());
        
        // 作成者・更新者のIDを文字列化
        if (batchJob.getCreatedBy() != null) {
            dto.setCreatedBy(batchJob.getCreatedBy().toString());
        }
        if (batchJob.getUpdatedBy() != null) {
            dto.setUpdatedBy(batchJob.getUpdatedBy().toString());
        }
        
        return dto;
    }

    // 実行IDを生成
    private String generateExecutionId() {
        return "exec" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }

    // ソートフィールド名の変換
    private String convertSortField(String field) {
        // DTOのフィールド名からエンティティのフィールド名に変換
        switch (field) {
            case "id":
                return "jobId";
            default:
                return field;
        }
    }
}