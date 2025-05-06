package com.ses_mgr.admin.controller.api;

import com.ses_mgr.admin.dto.*;
import com.ses_mgr.admin.service.LogManagementService;
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
 * ログ管理RESTコントローラー
 * Log management REST controller
 */
@RestController
@RequestMapping("/api/v1/admin/logs")
public class LogManagementRestController {

    private static final Logger logger = LoggerFactory.getLogger(LogManagementRestController.class);

    private final LogManagementService logManagementService;

    @Autowired
    public LogManagementRestController(LogManagementService logManagementService) {
        this.logManagementService = logManagementService;
    }

    /**
     * システムログの取得
     * Get system logs
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
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "timestamp,desc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<SystemLogDto> logsPage = logManagementService.getSystemLogs(from, to, level, module, search, pageable);

        return ResponseEntity.ok(new ApiResponseDto<>(logsPage, "システムログを取得しました"));
    }

    /**
     * 監査ログの取得
     * Get audit logs
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
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "timestamp,desc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<AuditLogDto> logsPage = logManagementService.getAuditLogs(
                from, to, userId, action, resourceType, resourceId, search, pageable);

        return ResponseEntity.ok(new ApiResponseDto<>(logsPage, "監査ログを取得しました"));
    }

    /**
     * エラーログの取得
     * Get error logs
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
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "timestamp,desc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<ErrorLogDto> logsPage = logManagementService.getErrorLogs(
                from, to, level, module, errorCode, search, pageable);

        return ResponseEntity.ok(new ApiResponseDto<>(logsPage, "エラーログを取得しました"));
    }

    /**
     * アクセスログの取得
     * Get access logs
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
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "timestamp,desc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<AccessLogDto> logsPage = logManagementService.getAccessLogs(
                from, to, userId, action, status, ipAddress, search, pageable);

        return ResponseEntity.ok(new ApiResponseDto<>(logsPage, "アクセスログを取得しました"));
    }

    /**
     * ログの検索
     * Search logs
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('system.logs.read')")
    public ResponseEntity<ApiResponseDto<Page<BaseLogDto>>> searchLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) List<String> types,
            @RequestParam(required = false) List<String> levels,
            @RequestParam(required = true) String search,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String errorCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "timestamp,desc") String sort) {

        LogSearchRequestDto searchRequest = new LogSearchRequestDto();
        searchRequest.setFrom(from);
        searchRequest.setTo(to);
        searchRequest.setTypes(types);
        searchRequest.setLevels(levels);
        searchRequest.setSearch(search);
        searchRequest.setUserId(userId);
        searchRequest.setUsername(username);
        searchRequest.setModule(module);
        searchRequest.setAction(action);
        searchRequest.setStatus(status);
        searchRequest.setIpAddress(ipAddress);
        searchRequest.setResourceType(resourceType);
        searchRequest.setResourceId(resourceId);
        searchRequest.setErrorCode(errorCode);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSort(sort);

        Page<BaseLogDto> searchResults = logManagementService.searchLogs(searchRequest);

        return ResponseEntity.ok(new ApiResponseDto<>(searchResults, "ログの検索結果を取得しました"));
    }

    /**
     * ログの統計情報取得
     * Get log statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('system.logs.read')")
    public ResponseEntity<ApiResponseDto<LogStatisticsResponseDto>> getLogStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) List<String> types,
            @RequestParam(required = false, defaultValue = "day") String interval,
            @RequestParam(required = false) List<String> groupBy) {

        LogStatisticsRequestDto request = new LogStatisticsRequestDto();
        request.setFrom(from);
        request.setTo(to);
        request.setTypes(types);
        request.setInterval(interval);
        if (groupBy != null) {
            request.setGroupBy(groupBy);
        }

        LogStatisticsResponseDto statistics = logManagementService.getLogStatistics(request);

        return ResponseEntity.ok(new ApiResponseDto<>(statistics, "ログの統計情報を取得しました"));
    }

    /**
     * ログのエクスポート
     * Export logs
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('system.logs.export')")
    public ResponseEntity<ApiResponseDto<LogExportResponseDto>> exportLogs(
            @Valid @RequestBody LogExportRequestDto exportRequest) {

        LogExportResponseDto exportResult = logManagementService.exportLogs(exportRequest);

        return new ResponseEntity<>(
                new ApiResponseDto<>(exportResult, "ログのエクスポートを開始しました"),
                HttpStatus.ACCEPTED);
    }

    /**
     * エクスポートしたログファイルのダウンロード
     * Download exported log file
     */
    @GetMapping("/download/{token}")
    @PreAuthorize("hasAuthority('system.logs.export')")
    public ResponseEntity<Resource> downloadExportedLogs(@PathVariable String token) {
        return logManagementService.downloadExportedLogs(token);
    }

    // Helper methods

    private Pageable createPageable(int page, int size, String sort) {
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "timestamp";

        if (sort != null && !sort.isEmpty()) {
            String[] sortParams = sort.split(",");
            if (sortParams.length >= 2) {
                property = sortParams[0];
                direction = "desc".equalsIgnoreCase(sortParams[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
            } else {
                property = sortParams[0];
            }
        }

        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}