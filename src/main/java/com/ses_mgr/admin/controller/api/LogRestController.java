package com.ses_mgr.admin.controller.api;

import com.ses_mgr.admin.dto.*;
import com.ses_mgr.admin.service.LogService;
import com.ses_mgr.common.dto.ApiResponseDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ログ管理REST APIコントローラ
 * Log REST API controller
 */
@RestController
@RequestMapping("/api/v1/admin/logs")
public class LogRestController {

    private static final Logger logger = LoggerFactory.getLogger(LogRestController.class);

    private final LogService logService;

    @Autowired
    public LogRestController(LogService logService) {
        this.logService = logService;
    }

    /**
     * システムログの取得
     * Get system logs
     *
     * @param from     開始日時（オプション）
     * @param to       終了日時（オプション）
     * @param level    ログレベル（オプション）
     * @param module   モジュール名（オプション）
     * @param search   検索キーワード（オプション）
     * @param page     ページ番号（デフォルト: 0）
     * @param size     ページサイズ（デフォルト: 20）
     * @param sort     ソート条件（デフォルト: timestamp,desc）
     * @return ApiResponseDto containing page of SystemLogDto
     */
    @GetMapping("/system")
    @PreAuthorize("hasAuthority('system.logs.read')")
    public ResponseEntity<ApiResponseDto<Page<SystemLogDto>>> getSystemLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp,desc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<SystemLogDto> logs = logService.getSystemLogs(from, to, level, module, search, pageable);
        
        return ResponseEntity.ok(new ApiResponseDto<>(logs));
    }

    /**
     * 監査ログの取得
     * Get audit logs
     *
     * @param from         開始日時（オプション）
     * @param to           終了日時（オプション）
     * @param userId       ユーザーID（オプション）
     * @param action       アクション（オプション）
     * @param resourceType リソースタイプ（オプション）
     * @param resourceId   リソースID（オプション）
     * @param search       検索キーワード（オプション）
     * @param page         ページ番号（デフォルト: 0）
     * @param size         ページサイズ（デフォルト: 20）
     * @param sort         ソート条件（デフォルト: timestamp,desc）
     * @return ApiResponseDto containing page of AuditLogDto
     */
    @GetMapping("/audit")
    @PreAuthorize("hasAuthority('system.logs.read')")
    public ResponseEntity<ApiResponseDto<Page<AuditLogDto>>> getAuditLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp,desc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<AuditLogDto> logs = logService.getAuditLogs(
                from, to, userId, action, resourceType, resourceId, search, pageable);
        
        return ResponseEntity.ok(new ApiResponseDto<>(logs));
    }

    /**
     * エラーログの取得
     * Get error logs
     *
     * @param from      開始日時（オプション）
     * @param to        終了日時（オプション）
     * @param level     ログレベル（オプション）
     * @param module    モジュール名（オプション）
     * @param errorCode エラーコード（オプション）
     * @param search    検索キーワード（オプション）
     * @param page      ページ番号（デフォルト: 0）
     * @param size      ページサイズ（デフォルト: 20）
     * @param sort      ソート条件（デフォルト: timestamp,desc）
     * @return ApiResponseDto containing page of ErrorLogDto
     */
    @GetMapping("/error")
    @PreAuthorize("hasAuthority('system.logs.read')")
    public ResponseEntity<ApiResponseDto<Page<ErrorLogDto>>> getErrorLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String errorCode,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp,desc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<ErrorLogDto> logs = logService.getErrorLogs(
                from, to, level, module, errorCode, search, pageable);
        
        return ResponseEntity.ok(new ApiResponseDto<>(logs));
    }

    /**
     * アクセスログの取得
     * Get access logs
     *
     * @param from      開始日時（オプション）
     * @param to        終了日時（オプション）
     * @param userId    ユーザーID（オプション）
     * @param action    アクション（オプション）
     * @param status    ステータス（オプション）
     * @param ipAddress IPアドレス（オプション）
     * @param search    検索キーワード（オプション）
     * @param page      ページ番号（デフォルト: 0）
     * @param size      ページサイズ（デフォルト: 20）
     * @param sort      ソート条件（デフォルト: timestamp,desc）
     * @return ApiResponseDto containing page of AccessLogDto
     */
    @GetMapping("/access")
    @PreAuthorize("hasAuthority('system.logs.read')")
    public ResponseEntity<ApiResponseDto<Page<AccessLogDto>>> getAccessLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp,desc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<AccessLogDto> logs = logService.getAccessLogs(
                from, to, userId, action, status, ipAddress, search, pageable);
        
        return ResponseEntity.ok(new ApiResponseDto<>(logs));
    }

    /**
     * ログの横断検索
     * Search across all log types
     *
     * @param from         開始日時（オプション）
     * @param to           終了日時（オプション）
     * @param types        ログタイプ（オプション、複数指定可）
     * @param userId       ユーザーID（オプション）
     * @param action       アクション（オプション）
     * @param level        ログレベル（オプション）
     * @param module       モジュール名（オプション）
     * @param errorCode    エラーコード（オプション）
     * @param resourceType リソースタイプ（オプション）
     * @param resourceId   リソースID（オプション）
     * @param status       ステータス（オプション）
     * @param ipAddress    IPアドレス（オプション）
     * @param search       検索キーワード（オプション）
     * @param page         ページ番号（デフォルト: 0）
     * @param size         ページサイズ（デフォルト: 20）
     * @param sort         ソート条件（デフォルト: timestamp,desc）
     * @return ApiResponseDto containing page of BaseLogDto
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('system.logs.read')")
    public ResponseEntity<ApiResponseDto<Page<BaseLogDto>>> searchLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) List<String> types,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String errorCode,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp,desc") String sort) {

        // 検索リクエストの構築
        LogSearchRequestDto searchRequest = new LogSearchRequestDto();
        searchRequest.setFrom(from);
        searchRequest.setTo(to);
        searchRequest.setTypes(types);
        searchRequest.setUserId(userId);
        searchRequest.setAction(action);
        searchRequest.setLevel(level);
        searchRequest.setModule(module);
        searchRequest.setErrorCode(errorCode);
        searchRequest.setResourceType(resourceType);
        searchRequest.setResourceId(resourceId);
        searchRequest.setStatus(status);
        searchRequest.setIpAddress(ipAddress);
        searchRequest.setSearch(search);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSort(sort);
        
        Page<BaseLogDto> logs = logService.searchLogs(searchRequest);
        
        return ResponseEntity.ok(new ApiResponseDto<>(logs));
    }

    /**
     * ログ統計情報の取得
     * Get log statistics
     *
     * @param from     開始日時（必須）
     * @param to       終了日時（必須）
     * @param types    ログタイプ（オプション、複数指定可）
     * @param interval 集計間隔（hour, day, week, month）（オプション、デフォルト: day）
     * @return ApiResponseDto containing LogStatisticsResponseDto
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('system.logs.read')")
    public ResponseEntity<ApiResponseDto<LogStatisticsResponseDto>> getLogStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) List<String> types,
            @RequestParam(required = false, defaultValue = "day") String interval) {

        LogStatisticsRequestDto request = new LogStatisticsRequestDto();
        request.setFrom(from);
        request.setTo(to);
        request.setTypes(types);
        request.setInterval(interval);
        
        LogStatisticsResponseDto statistics = logService.getLogStatistics(request);
        
        return ResponseEntity.ok(new ApiResponseDto<>(statistics));
    }

    /**
     * ログのエクスポート
     * Export logs
     *
     * @param exportRequest エクスポートリクエスト
     * @return ApiResponseDto containing LogExportResponseDto
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('system.logs.export')")
    public ResponseEntity<ApiResponseDto<LogExportResponseDto>> exportLogs(
            @RequestBody @Valid LogExportRequestDto exportRequest) {
            
        LogExportResponseDto exportResponse = logService.exportLogs(exportRequest);
        
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(new ApiResponseDto<>(exportResponse));
    }

    /**
     * エクスポートしたログのダウンロード
     * Download exported logs
     *
     * @param token ダウンロードトークン
     * @return ダウンロードファイル
     */
    @GetMapping("/download/{token}")
    @PreAuthorize("hasAuthority('system.logs.export')")
    public ResponseEntity<Resource> downloadExportedLogs(@PathVariable String token) {
        return logService.downloadExportedLogs(token);
    }

    /**
     * ページングとソート条件の作成
     * Creates a pageable object from page, size and sort parameters
     *
     * @param page ページ番号
     * @param size ページサイズ
     * @param sort ソート条件（カンマ区切り: フィールド名,方向）
     * @return Pageable オブジェクト
     */
    private Pageable createPageable(int page, int size, String sort) {
        String[] sortParams = sort.split(",");
        String sortField = "timestamp";
        Sort.Direction direction = Sort.Direction.DESC;
        
        if (sortParams.length >= 1) {
            sortField = sortParams[0];
        }
        
        if (sortParams.length >= 2) {
            direction = "asc".equalsIgnoreCase(sortParams[1]) 
                    ? Sort.Direction.ASC 
                    : Sort.Direction.DESC;
        }
        
        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }
}