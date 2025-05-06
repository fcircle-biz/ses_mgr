package com.ses_mgr.admin.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.admin.dto.batch.BatchScheduleCreateRequestDto;
import com.ses_mgr.admin.dto.batch.BatchScheduleDto;
import com.ses_mgr.admin.dto.batch.BatchScheduleUpdateRequestDto;
import com.ses_mgr.admin.entity.BatchJob;
import com.ses_mgr.admin.entity.BatchSchedule;
import com.ses_mgr.admin.repository.BatchJobRepository;
import com.ses_mgr.admin.repository.BatchScheduleRepository;
import com.ses_mgr.admin.service.BatchScheduleService;
import org.quartz.CronExpression;
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

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class BatchScheduleServiceImpl implements BatchScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(BatchScheduleServiceImpl.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    @Autowired
    private BatchJobRepository batchJobRepository;

    @Autowired
    private BatchScheduleRepository batchScheduleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Page<BatchScheduleDto> getBatchSchedules(String jobId, Boolean isActive, String type, 
                                                 Integer page, Integer limit) {
        
        // ページネーションパラメータの処理
        int pageNumber = (page != null && page > 0) ? page - 1 : DEFAULT_PAGE - 1;
        int pageSize = (limit != null && limit > 0) ? Math.min(limit, MAX_LIMIT) : DEFAULT_LIMIT;
        
        // バッチスケジュール一覧を取得
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "nextRun"));
        Page<BatchSchedule> schedules = batchScheduleRepository.searchBatchSchedules(jobId, isActive, type, pageable);
        
        // DTOに変換
        return schedules.map(this::convertToBatchScheduleDto);
    }

    @Override
    @Transactional
    public BatchScheduleDto createBatchSchedule(BatchScheduleCreateRequestDto createRequest, UUID createdBy) {
        // バッチジョブを取得
        BatchJob batchJob = batchJobRepository.findByJobId(createRequest.getJobId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "指定されたバッチジョブが見つかりません"));
        
        // Cron式の検証
        validateCronExpression(createRequest.getCronExpression());
        
        try {
            // パラメータをJSONに変換
            String parametersJson = null;
            if (createRequest.getParameters() != null) {
                parametersJson = objectMapper.writeValueAsString(createRequest.getParameters());
            }
            
            // スケジュールIDを生成
            String scheduleId = "schedule" + System.currentTimeMillis();
            
            // 次回実行日時を計算
            LocalDateTime nextRun = calculateNextRunTime(createRequest.getCronExpression());
            
            // バッチスケジュールを作成
            BatchSchedule batchSchedule = BatchSchedule.builder()
                    .scheduleId(scheduleId)
                    .jobId(createRequest.getJobId())
                    .description(createRequest.getDescription())
                    .cronExpression(createRequest.getCronExpression())
                    .scheduleType(createRequest.getType())
                    .isActive(createRequest.getIsActive())
                    .parameters(parametersJson)
                    .nextRun(nextRun)
                    .createdBy(createdBy)
                    .updatedBy(createdBy)
                    .build();
            
            // 保存
            batchSchedule = batchScheduleRepository.save(batchSchedule);
            
            // DTOに変換して返す
            return convertToBatchScheduleDto(batchSchedule);
        } catch (JsonProcessingException e) {
            logger.error("パラメータのJSON変換に失敗しました", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "パラメータのJSON変換に失敗しました", e);
        }
    }

    @Override
    @Transactional
    public BatchScheduleDto updateBatchSchedule(String scheduleId, BatchScheduleUpdateRequestDto updateRequest, UUID updatedBy) {
        // バッチスケジュールを取得
        BatchSchedule batchSchedule = batchScheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "指定されたスケジュールが見つかりません"));
        
        // 更新処理
        if (updateRequest.getDescription() != null) {
            batchSchedule.setDescription(updateRequest.getDescription());
        }
        
        if (updateRequest.getCronExpression() != null) {
            // Cron式の検証
            validateCronExpression(updateRequest.getCronExpression());
            batchSchedule.setCronExpression(updateRequest.getCronExpression());
            
            // 次回実行日時を再計算
            LocalDateTime nextRun = calculateNextRunTime(updateRequest.getCronExpression());
            batchSchedule.setNextRun(nextRun);
        }
        
        if (updateRequest.getType() != null) {
            batchSchedule.setScheduleType(updateRequest.getType());
        }
        
        if (updateRequest.getIsActive() != null) {
            batchSchedule.setIsActive(updateRequest.getIsActive());
        }
        
        if (updateRequest.getParameters() != null) {
            try {
                String parametersJson = objectMapper.writeValueAsString(updateRequest.getParameters());
                batchSchedule.setParameters(parametersJson);
            } catch (JsonProcessingException e) {
                logger.error("パラメータのJSON変換に失敗しました", e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                        "パラメータのJSON変換に失敗しました", e);
            }
        }
        
        batchSchedule.setUpdatedBy(updatedBy);
        
        // 保存
        batchSchedule = batchScheduleRepository.save(batchSchedule);
        
        // DTOに変換して返す
        return convertToBatchScheduleDto(batchSchedule);
    }

    @Override
    @Transactional
    public boolean deleteBatchSchedule(String scheduleId) {
        // バッチスケジュールを取得
        BatchSchedule batchSchedule = batchScheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "指定されたスケジュールが見つかりません"));
        
        // 削除
        batchScheduleRepository.delete(batchSchedule);
        
        return true;
    }

    @Override
    @Transactional
    public BatchScheduleDto updateNextRunTime(String scheduleId) {
        // バッチスケジュールを取得
        BatchSchedule batchSchedule = batchScheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "指定されたスケジュールが見つかりません"));
        
        // 次回実行日時を計算
        LocalDateTime nextRun = calculateNextRunTime(batchSchedule.getCronExpression());
        batchSchedule.setNextRun(nextRun);
        
        // 保存
        batchSchedule = batchScheduleRepository.save(batchSchedule);
        
        // DTOに変換して返す
        return convertToBatchScheduleDto(batchSchedule);
    }

    // DTOへの変換メソッド
    private BatchScheduleDto convertToBatchScheduleDto(BatchSchedule batchSchedule) {
        BatchScheduleDto dto = new BatchScheduleDto();
        dto.setId(batchSchedule.getScheduleId());
        dto.setJobId(batchSchedule.getJobId());
        
        // ジョブ名を取得
        batchJobRepository.findByJobId(batchSchedule.getJobId())
                .ifPresent(job -> dto.setJobName(job.getName()));
        
        dto.setDescription(batchSchedule.getDescription());
        dto.setCronExpression(batchSchedule.getCronExpression());
        dto.setType(batchSchedule.getScheduleType());
        dto.setIsActive(batchSchedule.getIsActive());
        
        // 次回実行日時をフォーマット
        if (batchSchedule.getNextRun() != null) {
            dto.setNextRun(batchSchedule.getNextRun().format(DATE_TIME_FORMATTER));
        }
        
        // パラメータをJSONからMapに変換
        if (batchSchedule.getParameters() != null && !batchSchedule.getParameters().isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> parametersMap = objectMapper.readValue(batchSchedule.getParameters(), Map.class);
                dto.setParameters(parametersMap);
            } catch (JsonProcessingException e) {
                logger.warn("パラメータのJSON解析に失敗しました: {}", e.getMessage());
            }
        }
        
        dto.setCreatedAt(batchSchedule.getCreatedAt());
        dto.setUpdatedAt(batchSchedule.getUpdatedAt());
        
        return dto;
    }

    // Cron式の検証
    private void validateCronExpression(String cronExpression) {
        try {
            new CronExpression(cronExpression);
        } catch (ParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "無効なcron式が指定されました: " + e.getMessage(), e);
        }
    }

    // 次回実行日時の計算
    private LocalDateTime calculateNextRunTime(String cronExpression) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            Date nextRunDate = cron.getNextValidTimeAfter(new Date());
            return LocalDateTime.ofInstant(nextRunDate.toInstant(), ZoneId.systemDefault());
        } catch (ParseException e) {
            logger.error("Cron式の解析に失敗しました", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "無効なcron式が指定されました: " + e.getMessage(), e);
        }
    }
}