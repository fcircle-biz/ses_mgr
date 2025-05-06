package com.ses_mgr.admin.service;

import com.ses_mgr.admin.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * ログ管理サービスインターフェース
 * Log management service interface
 */
public interface LogManagementService {

    /**
     * システムログを取得
     * Get system logs
     *
     * @param from     開始日時
     * @param to       終了日時
     * @param level    ログレベル（オプション）
     * @param module   モジュール名（オプション）
     * @param search   検索キーワード（オプション）
     * @param pageable ページング情報
     * @return システムログのページ
     */
    Page<SystemLogDto> getSystemLogs(
            java.time.LocalDateTime from,
            java.time.LocalDateTime to,
            String level,
            String module,
            String search,
            Pageable pageable);

    /**
     * 監査ログを取得
     * Get audit logs
     *
     * @param from         開始日時
     * @param to           終了日時
     * @param userId       ユーザーID（オプション）
     * @param action       アクション（オプション）
     * @param resourceType リソースタイプ（オプション）
     * @param resourceId   リソースID（オプション）
     * @param search       検索キーワード（オプション）
     * @param pageable     ページング情報
     * @return 監査ログのページ
     */
    Page<AuditLogDto> getAuditLogs(
            java.time.LocalDateTime from,
            java.time.LocalDateTime to,
            java.util.UUID userId,
            String action,
            String resourceType,
            String resourceId,
            String search,
            Pageable pageable);

    /**
     * エラーログを取得
     * Get error logs
     *
     * @param from       開始日時
     * @param to         終了日時
     * @param level      ログレベル（オプション）
     * @param module     モジュール名（オプション）
     * @param errorCode  エラーコード（オプション）
     * @param search     検索キーワード（オプション）
     * @param pageable   ページング情報
     * @return エラーログのページ
     */
    Page<ErrorLogDto> getErrorLogs(
            java.time.LocalDateTime from,
            java.time.LocalDateTime to,
            String level,
            String module,
            String errorCode,
            String search,
            Pageable pageable);

    /**
     * アクセスログを取得
     * Get access logs
     *
     * @param from       開始日時
     * @param to         終了日時
     * @param userId     ユーザーID（オプション）
     * @param action     アクション（オプション）
     * @param status     ステータス（オプション）
     * @param ipAddress  IPアドレス（オプション）
     * @param search     検索キーワード（オプション）
     * @param pageable   ページング情報
     * @return アクセスログのページ
     */
    Page<AccessLogDto> getAccessLogs(
            java.time.LocalDateTime from,
            java.time.LocalDateTime to,
            java.util.UUID userId,
            String action,
            String status,
            String ipAddress,
            String search,
            Pageable pageable);

    /**
     * 複数のログタイプを横断的に検索
     * Search across multiple log types
     *
     * @param searchRequest 検索リクエスト
     * @return 検索結果のログリスト（複数タイプ）
     */
    Page<BaseLogDto> searchLogs(LogSearchRequestDto searchRequest);

    /**
     * ログ統計情報を取得
     * Get log statistics
     *
     * @param statisticsRequest 統計情報リクエスト
     * @return ログ統計情報レスポンス
     */
    LogStatisticsResponseDto getLogStatistics(LogStatisticsRequestDto statisticsRequest);

    /**
     * ログをエクスポート
     * Export logs
     *
     * @param exportRequest エクスポートリクエスト
     * @return エクスポート結果情報
     */
    LogExportResponseDto exportLogs(LogExportRequestDto exportRequest);

    /**
     * エクスポートしたログファイルをダウンロード
     * Download exported log file
     *
     * @param token ダウンロードトークン
     * @return ファイルリソースとHTTPレスポンス情報
     */
    ResponseEntity<Resource> downloadExportedLogs(String token);
}