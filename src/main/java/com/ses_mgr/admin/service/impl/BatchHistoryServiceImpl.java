package com.ses_mgr.admin.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.admin.dto.batch.BatchHistoryItemDto;
import com.ses_mgr.admin.entity.BatchExecutionHistory;
import com.ses_mgr.admin.entity.BatchJob;
import com.ses_mgr.admin.repository.BatchExecutionHistoryRepository;
import com.ses_mgr.admin.repository.BatchJobRepository;
import com.ses_mgr.admin.service.BatchHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class BatchHistoryServiceImpl implements BatchHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(BatchHistoryServiceImpl.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    @Autowired
    private BatchExecutionHistoryRepository batchExecutionHistoryRepository;

    @Autowired
    private BatchJobRepository batchJobRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Page<BatchHistoryItemDto> getBatchHistory(String jobId, String status, 
                                                  LocalDateTime from, LocalDateTime to, 
                                                  String executedBy, Integer page, Integer limit, String sort) {
        
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
                pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "startTime"));
            }
        } else {
            pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "startTime"));
        }
        
        // 日付範囲のバリデーション
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "fromはtoより前の日時を指定してください");
        }
        
        // バッチ実行履歴を取得
        Page<BatchExecutionHistory> histories = 
                batchExecutionHistoryRepository.searchBatchHistory(jobId, status, from, to, executedBy, pageable);
        
        // DTOに変換
        return histories.map(this::convertToBatchHistoryItemDto);
    }

    @Override
    public BatchHistoryItemDto getBatchHistoryById(String executionId) {
        // バッチ実行履歴を取得
        BatchExecutionHistory history = batchExecutionHistoryRepository.findByExecutionId(executionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "指定された実行履歴が見つかりません"));
        
        // DTOに変換
        return convertToBatchHistoryItemDto(history);
    }

    // DTOへの変換メソッド
    private BatchHistoryItemDto convertToBatchHistoryItemDto(BatchExecutionHistory history) {
        BatchHistoryItemDto dto = new BatchHistoryItemDto();
        dto.setExecutionId(history.getExecutionId());
        dto.setJobId(history.getJobId());
        
        // ジョブ名を取得
        batchJobRepository.findByJobId(history.getJobId())
                .ifPresent(job -> dto.setJobName(job.getName()));
        
        dto.setStatus(history.getStatus());
        dto.setStartTime(history.getStartTime().format(DATE_TIME_FORMATTER));
        
        if (history.getEndTime() != null) {
            dto.setEndTime(history.getEndTime().format(DATE_TIME_FORMATTER));
        }
        
        dto.setExecutionTime(history.getExecutionTime());
        dto.setExecutedBy(history.getExecutedBy());
        dto.setTriggerType(history.getTriggerType());
        dto.setRecordsProcessed(history.getRecordsProcessed());
        dto.setErrorMessage(history.getErrorMessage());
        dto.setDescription(history.getDescription());
        
        // パラメータをJSONからMapに変換
        if (history.getParameters() != null && !history.getParameters().isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> parametersMap = objectMapper.readValue(history.getParameters(), Map.class);
                dto.setParameters(parametersMap);
            } catch (JsonProcessingException e) {
                logger.warn("パラメータのJSON解析に失敗しました: {}", e.getMessage());
            }
        }
        
        return dto;
    }

    // ソートフィールド名の変換
    private String convertSortField(String field) {
        // DTOのフィールド名からエンティティのフィールド名に変換
        switch (field) {
            case "id":
                return "executionId";
            default:
                return field;
        }
    }
}