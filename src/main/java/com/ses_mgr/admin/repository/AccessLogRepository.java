package com.ses_mgr.admin.repository;

import com.ses_mgr.admin.entity.AccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * アクセスログリポジトリインターフェース
 * Repository interface for access logs
 */
@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    /**
     * ユーザーIDでアクセスログを検索
     * Find access logs by user ID
     *
     * @param userId ユーザーID
     * @param pageable ページング情報
     * @return アクセスログのページ
     */
    Page<AccessLog> findByUserId(UUID userId, Pageable pageable);
    
    /**
     * アクションタイプでアクセスログを検索
     * Find access logs by action type
     *
     * @param action アクションタイプ
     * @param pageable ページング情報
     * @return アクセスログのページ
     */
    Page<AccessLog> findByAction(String action, Pageable pageable);
    
    /**
     * ステータスでアクセスログを検索
     * Find access logs by status
     *
     * @param status ステータス
     * @param pageable ページング情報
     * @return アクセスログのページ
     */
    Page<AccessLog> findByStatus(String status, Pageable pageable);
    
    /**
     * IPアドレスでアクセスログを検索
     * Find access logs by IP address
     *
     * @param ipAddress IPアドレス
     * @param pageable ページング情報
     * @return アクセスログのページ
     */
    Page<AccessLog> findByIpAddress(String ipAddress, Pageable pageable);
    
    /**
     * 日時範囲でアクセスログを検索
     * Find access logs by timestamp range
     *
     * @param from 開始日時
     * @param to 終了日時
     * @param pageable ページング情報
     * @return アクセスログのページ
     */
    Page<AccessLog> findByTimestampBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
    
    /**
     * キーワードでアクセスログを検索（メッセージフィールドの部分一致）
     * Search access logs by keyword (partial match in message field)
     *
     * @param keyword 検索キーワード
     * @param pageable ページング情報
     * @return アクセスログのページ
     */
    @Query("SELECT al FROM AccessLog al WHERE LOWER(al.message) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<AccessLog> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 複合条件でアクセスログを検索
     * Search access logs by multiple criteria
     *
     * @param from 開始日時
     * @param to 終了日時
     * @param userId ユーザーID（nullの場合は条件に含めない）
     * @param action アクション（nullの場合は条件に含めない）
     * @param status ステータス（nullの場合は条件に含めない）
     * @param ipAddress IPアドレス（nullの場合は条件に含めない）
     * @param keyword 検索キーワード（nullの場合は条件に含めない）
     * @param pageable ページング情報
     * @return アクセスログのページ
     */
    @Query("SELECT al FROM AccessLog al WHERE " +
           "(:from IS NULL OR al.timestamp >= :from) AND " +
           "(:to IS NULL OR al.timestamp <= :to) AND " +
           "(:userId IS NULL OR al.userId = :userId) AND " +
           "(:action IS NULL OR al.action = :action) AND " +
           "(:status IS NULL OR al.status = :status) AND " +
           "(:ipAddress IS NULL OR al.ipAddress = :ipAddress) AND " +
           "(:keyword IS NULL OR LOWER(al.message) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<AccessLog> searchByMultipleCriteria(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("userId") UUID userId,
            @Param("action") String action,
            @Param("status") String status,
            @Param("ipAddress") String ipAddress,
            @Param("keyword") String keyword,
            Pageable pageable);
            
    
    /**
     * ログレベル別の集計を取得
     * Get count by log level
     *
     * @param from 開始日時
     * @param to 終了日時
     * @return ログレベルとその件数のリスト
     */
    @Query("SELECT al.level, COUNT(al) FROM AccessLog al " +
           "WHERE al.timestamp BETWEEN :from AND :to " +
           "GROUP BY al.level")
    List<Object[]> countByLevel(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    
    /**
     * アクション別の集計を取得
     * Get count by action
     *
     * @param from 開始日時
     * @param to 終了日時
     * @return アクションとその件数のリスト
     */
    @Query("SELECT al.action, COUNT(al) FROM AccessLog al " +
           "WHERE al.timestamp BETWEEN :from AND :to " +
           "GROUP BY al.action")
    List<Object[]> countByAction(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    
    /**
     * ステータス別の集計を取得
     * Get count by status
     *
     * @param from 開始日時
     * @param to 終了日時
     * @return ステータスとその件数のリスト
     */
    @Query("SELECT al.status, COUNT(al) FROM AccessLog al " +
           "WHERE al.timestamp BETWEEN :from AND :to " +
           "GROUP BY al.status")
    List<Object[]> countByStatus(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    
    /**
     * ユーザー別の集計を取得（最もアクティブなユーザーランキング）
     * Get count by user (most active user ranking)
     *
     * @param from 開始日時
     * @param to 終了日時
     * @param limit 取得件数
     * @return ユーザーIDとその件数のリスト
     */
    @Query("SELECT al.userId, al.username, COUNT(al) FROM AccessLog al " +
           "WHERE al.timestamp BETWEEN :from AND :to " +
           "GROUP BY al.userId, al.username " +
           "ORDER BY COUNT(al) DESC")
    List<Object[]> countByUser(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable limit);
    
    /**
     * 時間帯別の集計を取得（アクセス頻度の時間分布）
     * Get count by hour of day (access frequency by hour)
     *
     * @param from 開始日時
     * @param to 終了日時
     * @return 時間帯とその件数のリスト
     */
    @Query("SELECT FUNCTION('HOUR', al.timestamp) as hour, COUNT(al) FROM AccessLog al " +
           "WHERE al.timestamp BETWEEN :from AND :to " +
           "GROUP BY FUNCTION('HOUR', al.timestamp) " +
           "ORDER BY hour")
    List<Object[]> countByHourOfDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    
    /**
     * 指定した時間範囲内のアクセスログ数を取得
     * Count access logs within specified time range
     *
     * @param from 開始日時
     * @param to 終了日時
     * @return ログの件数
     */
    @Query("SELECT COUNT(al) FROM AccessLog al WHERE al.timestamp BETWEEN :from AND :to")
    long countByTimeRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}