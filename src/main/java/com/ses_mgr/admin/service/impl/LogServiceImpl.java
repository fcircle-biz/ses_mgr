package com.ses_mgr.admin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.admin.dto.*;
import com.ses_mgr.admin.entity.AccessLog;
import com.ses_mgr.admin.entity.AuditLog;
import com.ses_mgr.admin.entity.ErrorLog;
import com.ses_mgr.admin.entity.SystemLog;
import com.ses_mgr.admin.repository.AccessLogRepository;
import com.ses_mgr.admin.repository.AuditLogRepository;
import com.ses_mgr.admin.repository.ErrorLogRepository;
import com.ses_mgr.admin.repository.SystemLogRepository;
import com.ses_mgr.admin.service.LogService;
import com.ses_mgr.common.exception.ResourceAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ログ管理サービス実装クラス
 * Log service implementation
 */
@Service
public class LogServiceImpl implements LogService {

    private static final Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final Map<String, LogExportResponseDto> exportCache = new ConcurrentHashMap<>();

    private final SystemLogRepository systemLogRepository;
    private final AuditLogRepository auditLogRepository;
    private final ErrorLogRepository errorLogRepository;
    private final AccessLogRepository accessLogRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.logs.export.path:/tmp/logs/exports}")
    private String exportPath;

    @Value("${app.logs.export.max-records:100000}")
    private int maxExportRecords;

    @Value("${app.logs.export.token-validity-minutes:30}")
    private int tokenValidityMinutes;

    @Autowired
    public LogServiceImpl(
            SystemLogRepository systemLogRepository,
            AuditLogRepository auditLogRepository,
            ErrorLogRepository errorLogRepository,
            AccessLogRepository accessLogRepository,
            ObjectMapper objectMapper) {
        this.systemLogRepository = systemLogRepository;
        this.auditLogRepository = auditLogRepository;
        this.errorLogRepository = errorLogRepository;
        this.accessLogRepository = accessLogRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SystemLogDto> getSystemLogs(
            LocalDateTime from,
            LocalDateTime to,
            String level,
            String module,
            String search,
            Pageable pageable) {

        // デフォルト値の設定
        LocalDateTime actualFrom = from != null ? from : LocalDateTime.now().minusDays(7);
        LocalDateTime actualTo = to != null ? to : LocalDateTime.now();

        Page<SystemLog> systemLogsPage = systemLogRepository.searchByMultipleCriteria(
                actualFrom, actualTo, level, module, search, pageable);

        List<SystemLogDto> systemLogDtos = systemLogsPage.getContent().stream()
                .map(this::convertToSystemLogDto)
                .collect(Collectors.toList());

        return new PageImpl<>(systemLogDtos, pageable, systemLogsPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAuditLogs(
            LocalDateTime from,
            LocalDateTime to,
            UUID userId,
            String action,
            String resourceType,
            String resourceId,
            String search,
            Pageable pageable) {

        // デフォルト値の設定
        LocalDateTime actualFrom = from != null ? from : LocalDateTime.now().minusDays(7);
        LocalDateTime actualTo = to != null ? to : LocalDateTime.now();

        Page<AuditLog> auditLogsPage = auditLogRepository.searchByMultipleCriteria(
                actualFrom, actualTo, userId, action, resourceType, resourceId, search, pageable);

        List<AuditLogDto> auditLogDtos = auditLogsPage.getContent().stream()
                .map(this::convertToAuditLogDto)
                .collect(Collectors.toList());

        return new PageImpl<>(auditLogDtos, pageable, auditLogsPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ErrorLogDto> getErrorLogs(
            LocalDateTime from,
            LocalDateTime to,
            String level,
            String module,
            String errorCode,
            String search,
            Pageable pageable) {

        // デフォルト値の設定
        LocalDateTime actualFrom = from != null ? from : LocalDateTime.now().minusDays(7);
        LocalDateTime actualTo = to != null ? to : LocalDateTime.now();

        Page<ErrorLog> errorLogsPage = errorLogRepository.searchByMultipleCriteria(
                actualFrom, actualTo, level, module, errorCode, search, pageable);

        List<ErrorLogDto> errorLogDtos = errorLogsPage.getContent().stream()
                .map(this::convertToErrorLogDto)
                .collect(Collectors.toList());

        return new PageImpl<>(errorLogDtos, pageable, errorLogsPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccessLogDto> getAccessLogs(
            LocalDateTime from,
            LocalDateTime to,
            UUID userId,
            String action,
            String status,
            String ipAddress,
            String search,
            Pageable pageable) {

        // デフォルト値の設定
        LocalDateTime actualFrom = from != null ? from : LocalDateTime.now().minusDays(7);
        LocalDateTime actualTo = to != null ? to : LocalDateTime.now();

        Page<AccessLog> accessLogsPage = accessLogRepository.searchByMultipleCriteria(
                actualFrom, actualTo, userId, action, status, ipAddress, search, pageable);

        List<AccessLogDto> accessLogDtos = accessLogsPage.getContent().stream()
                .map(this::convertToAccessLogDto)
                .collect(Collectors.toList());

        return new PageImpl<>(accessLogDtos, pageable, accessLogsPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BaseLogDto> searchLogs(LogSearchRequestDto searchRequest) {
        // クエリパラメータの処理
        LocalDateTime from = searchRequest.getFrom() != null 
                ? searchRequest.getFrom() 
                : LocalDateTime.now().minusDays(7);
                
        LocalDateTime to = searchRequest.getTo() != null 
                ? searchRequest.getTo() 
                : LocalDateTime.now();
                
        List<String> types = searchRequest.getTypes();
        String keyword = searchRequest.getSearch();
        
        // ページング情報の構築
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getSize(),
                Sort.by(Sort.Direction.DESC, "timestamp"));
                
        if (searchRequest.getSort() != null && !searchRequest.getSort().isEmpty()) {
            String[] sortParams = searchRequest.getSort().split(",");
            if (sortParams.length >= 2) {
                String property = sortParams[0];
                Sort.Direction direction = "desc".equalsIgnoreCase(sortParams[1]) 
                        ? Sort.Direction.DESC 
                        : Sort.Direction.ASC;
                pageable = PageRequest.of(
                        searchRequest.getPage(),
                        searchRequest.getSize(),
                        Sort.by(direction, property));
            }
        }

        List<CompletableFuture<List<? extends BaseLogDto>>> futures = new ArrayList<>();
        
        // 変数を効果的にfinalにするためのローカル変数
        final LocalDateTime fromFinal = from;
        final LocalDateTime toFinal = to;
        final String keywordFinal = keyword;
        final Pageable pageableFinal = pageable;
        final LogSearchRequestDto requestFinal = searchRequest;
        
        // タイプに基づいて並行してクエリを実行
        if (types == null || types.isEmpty() || types.contains("system")) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                Page<SystemLog> systemLogs = systemLogRepository.searchByMultipleCriteria(
                        fromFinal, toFinal, null, requestFinal.getModule(), keywordFinal, pageableFinal);
                return systemLogs.getContent().stream()
                        .map(this::convertToSystemLogDto)
                        .collect(Collectors.toList());
            }));
        }
        
        if (types == null || types.isEmpty() || types.contains("audit")) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                Page<AuditLog> auditLogs = auditLogRepository.searchByMultipleCriteria(
                        fromFinal, toFinal, requestFinal.getUserId(), requestFinal.getAction(), 
                        requestFinal.getResourceType(), requestFinal.getResourceId(), keywordFinal, pageableFinal);
                return auditLogs.getContent().stream()
                        .map(this::convertToAuditLogDto)
                        .collect(Collectors.toList());
            }));
        }
        
        if (types == null || types.isEmpty() || types.contains("error")) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                Page<ErrorLog> errorLogs = errorLogRepository.searchByMultipleCriteria(
                        fromFinal, toFinal, null, requestFinal.getModule(), requestFinal.getErrorCode(), keywordFinal, pageableFinal);
                return errorLogs.getContent().stream()
                        .map(this::convertToErrorLogDto)
                        .collect(Collectors.toList());
            }));
        }
        
        if (types == null || types.isEmpty() || types.contains("access")) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                Page<AccessLog> accessLogs = accessLogRepository.searchByMultipleCriteria(
                        fromFinal, toFinal, requestFinal.getUserId(), requestFinal.getAction(), 
                        requestFinal.getStatus(), requestFinal.getIpAddress(), keywordFinal, pageableFinal);
                return accessLogs.getContent().stream()
                        .map(this::convertToAccessLogDto)
                        .collect(Collectors.toList());
            }));
        }

        // すべての結果を収集して結合
        List<BaseLogDto> combinedResults = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(BaseLogDto::getTimestamp).reversed())
                .collect(Collectors.toList());
                
        // ページサイズに合わせて結果をトリミング
        int start = Math.min((int) pageable.getOffset(), combinedResults.size());
        int end = Math.min((start + pageable.getPageSize()), combinedResults.size());
        
        List<BaseLogDto> pageContent = combinedResults.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, combinedResults.size());
    }

    @Override
    @Transactional(readOnly = true)
    public LogStatisticsResponseDto getLogStatistics(LogStatisticsRequestDto statisticsRequest) {
        LogStatisticsResponseDto response = new LogStatisticsResponseDto();
        
        // リクエストパラメータの取得
        LocalDateTime from = statisticsRequest.getFrom();
        LocalDateTime to = statisticsRequest.getTo();
        List<String> types = statisticsRequest.getTypes();
        
        // 集計に使用する間隔
        String interval = statisticsRequest.getInterval() != null 
                ? statisticsRequest.getInterval() 
                : "day";

        Map<String, Object> summary = new HashMap<>();
        summary.put("period", Map.of("from", from, "to", to));
        
        // 各ログタイプごとの集計を非同期で実行
        CompletableFuture<Map<String, Integer>> totalCountByType = CompletableFuture.supplyAsync(() -> {
            Map<String, Integer> counts = new HashMap<>();
            
            // タイプごとの集計
            if (types == null || types.isEmpty() || types.contains("system")) {
                counts.put("system", (int) systemLogRepository.countByTimeRange(from, to));
            }
            
            if (types == null || types.isEmpty() || types.contains("audit")) {
                counts.put("audit", (int) auditLogRepository.countByTimeRange(from, to));
            }
            
            if (types == null || types.isEmpty() || types.contains("error")) {
                counts.put("error", (int) errorLogRepository.countByTimeRange(from, to));
            }
            
            if (types == null || types.isEmpty() || types.contains("access")) {
                counts.put("access", (int) accessLogRepository.countByTimeRange(from, to));
            }
            
            int total = counts.values().stream().mapToInt(Integer::intValue).sum();
            counts.put("total", total);
            
            return counts;
        });

        // レベル別の集計
        CompletableFuture<Map<String, Integer>> levelCounts = CompletableFuture.supplyAsync(() -> {
            Map<String, Integer> counts = new HashMap<>();
            
            // システムログのレベル別集計
            if (types == null || types.isEmpty() || types.contains("system")) {
                List<Object[]> systemLogLevelCounts = systemLogRepository.countByLevel(from, to);
                for (Object[] count : systemLogLevelCounts) {
                    String level = (String) count[0];
                    Long countValue = (Long) count[1];
                    counts.merge(level, countValue.intValue(), Integer::sum);
                }
            }
            
            // 監査ログのレベル別集計
            if (types == null || types.isEmpty() || types.contains("audit")) {
                List<Object[]> auditLogLevelCounts = auditLogRepository.countByLevel(from, to);
                for (Object[] count : auditLogLevelCounts) {
                    String level = (String) count[0];
                    Long countValue = (Long) count[1];
                    counts.merge(level, countValue.intValue(), Integer::sum);
                }
            }
            
            // エラーログのレベル別集計
            if (types == null || types.isEmpty() || types.contains("error")) {
                List<Object[]> errorLogLevelCounts = errorLogRepository.countByLevel(from, to);
                for (Object[] count : errorLogLevelCounts) {
                    String level = (String) count[0];
                    Long countValue = (Long) count[1];
                    counts.merge(level, countValue.intValue(), Integer::sum);
                }
            }
            
            // アクセスログのレベル別集計
            if (types == null || types.isEmpty() || types.contains("access")) {
                List<Object[]> accessLogLevelCounts = accessLogRepository.countByLevel(from, to);
                for (Object[] count : accessLogLevelCounts) {
                    String level = (String) count[0];
                    Long countValue = (Long) count[1];
                    counts.merge(level, countValue.intValue(), Integer::sum);
                }
            }
            
            return counts;
        });
        
        // トップユーザーの集計
        CompletableFuture<List<Map<String, Object>>> topUsers = CompletableFuture.supplyAsync(() -> {
            List<Map<String, Object>> users = new ArrayList<>();
            
            if (types == null || types.isEmpty() || 
                    (types.contains("audit") || types.contains("access"))) {
                // アクセスログからユーザー活動の集計
                List<Object[]> userAccessCounts = accessLogRepository.countByUser(
                        from, to, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "count")));
                
                for (Object[] count : userAccessCounts) {
                    if (count[0] != null) {  // nullでないユーザーIDのみを処理
                        Map<String, Object> userCount = new HashMap<>();
                        userCount.put("userId", count[0]);
                        userCount.put("username", count[1]);
                        userCount.put("count", count[2]);
                        userCount.put("source", "access_logs");
                        users.add(userCount);
                    }
                }
                
                // 監査ログからユーザー活動の集計（アクセスログの情報と結合）
                List<Object[]> userAuditCounts = auditLogRepository.countByUser(
                        from, to, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "count")));
                
                for (Object[] count : userAuditCounts) {
                    if (count[0] != null) {  // nullでないユーザーIDのみを処理
                        UUID userId = (UUID) count[0];
                        boolean found = false;
                        
                        // 既存リストで更新
                        for (Map<String, Object> user : users) {
                            if (userId.equals(user.get("userId"))) {
                                int accessCount = (int) user.get("count");
                                long auditCount = (long) count[2];
                                user.put("count", accessCount + (int) auditCount);
                                user.put("source", "access_and_audit_logs");
                                found = true;
                                break;
                            }
                        }
                        
                        // 見つからない場合は新規追加
                        if (!found && users.size() < 10) {
                            Map<String, Object> userCount = new HashMap<>();
                            userCount.put("userId", count[0]);
                            userCount.put("username", count[1]);
                            userCount.put("count", count[2]);
                            userCount.put("source", "audit_logs");
                            users.add(userCount);
                        }
                    }
                }
                
                // カウント降順でソート
                users.sort((a, b) -> {
                    int countA = a.get("count") instanceof Integer ? (int) a.get("count") : ((Long) a.get("count")).intValue();
                    int countB = b.get("count") instanceof Integer ? (int) b.get("count") : ((Long) b.get("count")).intValue();
                    return Integer.compare(countB, countA);
                });
                
                // 上位10件に制限
                if (users.size() > 10) {
                    users = users.subList(0, 10);
                }
            }
            
            return users;
        });

        // トップエラーの集計
        CompletableFuture<List<Map<String, Object>>> topErrors = CompletableFuture.supplyAsync(() -> {
            List<Map<String, Object>> errors = new ArrayList<>();
            
            if (types == null || types.isEmpty() || types.contains("error")) {
                // エラーコード別の集計
                List<Object[]> errorCodeCounts = errorLogRepository.countByErrorCode(from, to);
                
                for (Object[] count : errorCodeCounts) {
                    if (count[0] != null) {  // nullでないエラーコードのみを処理
                        Map<String, Object> errorCount = new HashMap<>();
                        errorCount.put("errorCode", count[0]);
                        errorCount.put("count", count[1]);
                        errors.add(errorCount);
                    }
                }
                
                // エラーコードの最終発生時間を追加
                List<Object[]> latestErrorOccurrences = errorLogRepository.getLatestOccurrenceByErrorCode(from, to);
                
                for (Map<String, Object> error : errors) {
                    String errorCode = (String) error.get("errorCode");
                    
                    for (Object[] occurrence : latestErrorOccurrences) {
                        if (errorCode.equals(occurrence[0])) {
                            error.put("lastOccurred", occurrence[1]);
                            break;
                        }
                    }
                }
                
                // 上位10件に制限
                if (errors.size() > 10) {
                    errors = errors.subList(0, 10);
                }
            }
            
            return errors;
        });

        try {
            // サマリーセクションの構築
            Map<String, Integer> typeCountsMap = totalCountByType.get();
            Map<String, Integer> levelCountsMap = levelCounts.get();
            
            summary.put("totalCount", typeCountsMap.get("total"));
            summary.put("byType", typeCountsMap);
            summary.put("byLevel", levelCountsMap);
            
            response.setSummary(summary);
            response.setTopUsers(topUsers.get());
            response.setTopErrors(topErrors.get());

            // タイムラインの生成は実際の実装では必要に応じて複雑になる
            // 簡単な実装例として日別の集計を返す
            List<LogStatisticsResponseDto.TimelineEntry> timeline = new ArrayList<>();
            
            // TODO: 実際の実装ではinterval（hour, day, week, month）に基づいて
            // タイムラインを生成する。この例では簡略化のため省略。
            
            response.setTimeline(timeline);
        } catch (Exception e) {
            logger.error("Error generating log statistics", e);
            throw new RuntimeException("Failed to generate log statistics: " + e.getMessage(), e);
        }
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public LogExportResponseDto exportLogs(LogExportRequestDto exportRequest) {
        // リクエストパラメータの取得と検証
        if (exportRequest.getFrom() == null || exportRequest.getTo() == null) {
            throw new IllegalArgumentException("From and to dates are required for log export");
        }
        
        LocalDateTime from = exportRequest.getFrom();
        LocalDateTime to = exportRequest.getTo();
        
        // エクスポート対象のレコード数を見積もる
        long estimatedCount = countLogsForExport(exportRequest);
        
        if (estimatedCount > maxExportRecords) {
            throw new IllegalArgumentException("Export record limit exceeded. Maximum allowed: " + 
                    maxExportRecords + ", requested: " + estimatedCount);
        }
        
        try {
            // エクスポートディレクトリの作成
            Path exportDir = Paths.get(exportPath);
            if (!Files.exists(exportDir)) {
                Files.createDirectories(exportDir);
            }
            
            String exportId = UUID.randomUUID().toString();
            String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
            String fileName = "logs_export_" + timestamp + "." + exportRequest.getFormat();
            Path filePath = exportDir.resolve(fileName);
            
            // 非同期エクスポート処理の開始
            CompletableFuture.runAsync(() -> {
                try {
                    exportLogsToFile(exportRequest, filePath.toString());
                } catch (Exception e) {
                    logger.error("Error during log export", e);
                    // エラー処理：現実の実装ではこのエラーを適切に通知する必要がある
                }
            });
            
            // レスポンスの作成
            LogExportResponseDto response = new LogExportResponseDto();
            response.setFileFormat(exportRequest.getFormat());
            response.setFileName(fileName);
            response.setFileUrl("/api/v1/admin/logs/download/" + exportId);
            response.setDownloadToken(exportId);
            response.setTotalRecords((int) estimatedCount);
            
            // キャッシュに保存して後でダウンロードできるようにする
            exportCache.put(exportId, response);
            
            // 一定時間後にキャッシュからエントリを削除するタスクをスケジュール
            scheduleTokenCleanup(exportId);
            
            return response;
        } catch (IOException e) {
            logger.error("Error preparing log export", e);
            throw new RuntimeException("Failed to prepare log export: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<Resource> downloadExportedLogs(String token) {
        LogExportResponseDto exportInfo = exportCache.get(token);
        
        if (exportInfo == null) {
            throw new IllegalArgumentException("Invalid or expired download token");
        }
        
        Path exportFile = Paths.get(exportPath, exportInfo.getFileName());
        if (!Files.exists(exportFile)) {
            throw new IllegalArgumentException("Export file not found or still being generated");
        }
        
        Resource resource = new FileSystemResource(exportFile.toFile());
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + exportInfo.getFileName());
        
        if ("csv".equals(exportInfo.getFileFormat())) {
            headers.setContentType(MediaType.parseMediaType("text/csv"));
        } else if ("excel".equals(exportInfo.getFileFormat())) {
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        }
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
    
    // ヘルパーメソッド
    
    private SystemLogDto convertToSystemLogDto(SystemLog entity) {
        SystemLogDto dto = new SystemLogDto();
        dto.setId(entity.getId());
        dto.setLevel(entity.getLevel());
        dto.setMessage(entity.getMessage());
        dto.setDetails(entity.getDetails());
        dto.setTimestamp(entity.getTimestamp());
        dto.setModule(entity.getModule());
        dto.setFunction(entity.getFunction());
        dto.setServer(entity.getServer());
        dto.setTraceId(entity.getTraceId());
        return dto;
    }
    
    private AuditLogDto convertToAuditLogDto(AuditLog entity) {
        AuditLogDto dto = new AuditLogDto();
        dto.setId(entity.getId());
        dto.setLevel(entity.getLevel());
        dto.setMessage(entity.getMessage());
        dto.setDetails(entity.getDetails());
        dto.setTimestamp(entity.getTimestamp());
        dto.setUserId(entity.getUserId());
        dto.setUsername(entity.getUsername());
        dto.setIpAddress(entity.getIpAddress());
        dto.setUserAgent(entity.getUserAgent());
        dto.setResourceType(entity.getResourceType());
        dto.setResourceId(entity.getResourceId());
        dto.setAction(entity.getAction());
        return dto;
    }
    
    private ErrorLogDto convertToErrorLogDto(ErrorLog entity) {
        ErrorLogDto dto = new ErrorLogDto();
        dto.setId(entity.getId());
        dto.setLevel(entity.getLevel());
        dto.setMessage(entity.getMessage());
        dto.setDetails(entity.getDetails());
        dto.setTimestamp(entity.getTimestamp());
        dto.setModule(entity.getModule());
        dto.setFunction(entity.getFunction());
        dto.setErrorCode(entity.getErrorCode());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setStackTrace(entity.getStackTrace());
        return dto;
    }
    
    private AccessLogDto convertToAccessLogDto(AccessLog entity) {
        AccessLogDto dto = new AccessLogDto();
        dto.setId(entity.getId());
        dto.setLevel(entity.getLevel());
        dto.setMessage(entity.getMessage());
        dto.setDetails(entity.getDetails());
        dto.setTimestamp(entity.getTimestamp());
        dto.setUserId(entity.getUserId());
        dto.setUsername(entity.getUsername());
        dto.setAction(entity.getAction());
        dto.setStatus(entity.getStatus());
        dto.setIpAddress(entity.getIpAddress());
        dto.setUserAgent(entity.getUserAgent());
        dto.setRequestUri(entity.getRequestUri());
        dto.setRequestMethod(entity.getRequestMethod());
        dto.setResponseStatus(entity.getResponseStatus());
        dto.setResponseTime(entity.getResponseTime());
        return dto;
    }
    
    private long countLogsForExport(LogExportRequestDto exportRequest) {
        LocalDateTime from = exportRequest.getFrom();
        LocalDateTime to = exportRequest.getTo();
        List<String> types = exportRequest.getTypes();
        
        long count = 0;
        
        if (types == null || types.isEmpty()) {
            // デフォルトですべてのタイプを含める
            count += systemLogRepository.countByTimeRange(from, to);
            count += auditLogRepository.countByTimeRange(from, to);
            count += errorLogRepository.countByTimeRange(from, to);
            count += accessLogRepository.countByTimeRange(from, to);
        } else {
            if (types.contains("system")) {
                count += systemLogRepository.countByTimeRange(from, to);
            }
            if (types.contains("audit")) {
                count += auditLogRepository.countByTimeRange(from, to);
            }
            if (types.contains("error")) {
                count += errorLogRepository.countByTimeRange(from, to);
            }
            if (types.contains("access")) {
                count += accessLogRepository.countByTimeRange(from, to);
            }
        }
        
        return count;
    }

    private void exportLogsToFile(LogExportRequestDto exportRequest, String filePath) throws IOException {
        // エクスポート処理の実装
        // この実装は簡略化しています。実際のプロダクションコードでは
        // バッチ処理やページングを考慮した実装が必要です。
        
        // CSVフォーマットの場合の簡易実装例
        if ("csv".equals(exportRequest.getFormat())) {
            Charset charset = "UTF-8".equals(exportRequest.getEncoding()) 
                    ? StandardCharsets.UTF_8 
                    : Charset.forName(exportRequest.getEncoding());
                    
            try (OutputStreamWriter writer = new OutputStreamWriter(
                     new FileOutputStream(filePath), charset)) {
                
                // ヘッダー行の出力
                Set<String> fields = exportRequest.getFields();
                if (fields == null || fields.isEmpty()) {
                    // デフォルトフィールド
                    fields = Set.of("id", "type", "level", "message", "timestamp");
                }
                
                writer.write(String.join(",", fields) + "\n");
                
                // データ行の出力
                // 実際の実装では、大量データに対応するためにバッチ処理やストリーミング処理が必要
                // ...
            }
        } else if ("excel".equals(exportRequest.getFormat())) {
            // Excel形式のエクスポート処理
            // ここでは省略...
        }
    }

    private void scheduleTokenCleanup(String token) {
        // トークンの有効期限を設定し、期限後に自動的に削除するタスクをスケジュール
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                exportCache.remove(token);
                
                // 関連するファイルも削除（オプション）
                try {
                    LogExportResponseDto exportInfo = exportCache.get(token);
                    if (exportInfo != null) {
                        Path exportFile = Paths.get(exportPath, exportInfo.getFileName());
                        Files.deleteIfExists(exportFile);
                    }
                } catch (IOException e) {
                    logger.warn("Failed to delete export file for token: " + token, e);
                }
            }
        }, tokenValidityMinutes * 60 * 1000);
    }
}