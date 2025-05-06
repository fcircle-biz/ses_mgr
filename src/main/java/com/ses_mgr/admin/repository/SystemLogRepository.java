package com.ses_mgr.admin.repository;

import com.ses_mgr.admin.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * システムログリポジトリインターフェース
 * Repository interface for system logs
 */
@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {

    /**
     * ログレベルでシステムログを検索
     * Find system logs by level
     *
     * @param level ログレベル
     * @param pageable ページング情報
     * @return システムログのページ
     */
    Page<SystemLog> findByLevel(String level, Pageable pageable);
    
    /**
     * 日時範囲でシステムログを検索
     * Find system logs by timestamp range
     *
     * @param from 開始日時
     * @param to 終了日時
     * @param pageable ページング情報
     * @return システムログのページ
     */
    Page<SystemLog> findByTimestampBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
    
    /**
     * モジュール名でシステムログを検索
     * Find system logs by module name
     *
     * @param module モジュール名
     * @param pageable ページング情報
     * @return システムログのページ
     */
    Page<SystemLog> findByModule(String module, Pageable pageable);
    
    /**
     * キーワードでシステムログを検索（メッセージフィールドの部分一致）
     * Search system logs by keyword (partial match in message field)
     *
     * @param keyword 検索キーワード
     * @param pageable ページング情報
     * @return システムログのページ
     */
    @Query("SELECT sl FROM SystemLog sl WHERE LOWER(sl.message) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<SystemLog> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 複合条件でシステムログを検索
     * Search system logs by multiple criteria
     *
     * @param from 開始日時
     * @param to 終了日時
     * @param level ログレベル（nullの場合は条件に含めない）
     * @param module モジュール名（nullの場合は条件に含めない）
     * @param keyword 検索キーワード（nullの場合は条件に含めない）
     * @param pageable ページング情報
     * @return システムログのページ
     */
    @Query("SELECT sl FROM SystemLog sl WHERE " +
           "(:from IS NULL OR sl.timestamp >= :from) AND " +
           "(:to IS NULL OR sl.timestamp <= :to) AND " +
           "(:level IS NULL OR sl.level = :level) AND " +
           "(:module IS NULL OR sl.module = :module) AND " +
           "(:keyword IS NULL OR LOWER(sl.message) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<SystemLog> searchByMultipleCriteria(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("level") String level,
            @Param("module") String module,
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
    @Query("SELECT sl.level, COUNT(sl) FROM SystemLog sl " +
           "WHERE sl.timestamp BETWEEN :from AND :to " +
           "GROUP BY sl.level")
    List<Object[]> countByLevel(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    
    /**
     * 総数を取得
     * Get count of logs for a date range
     *
     * @param from 開始日時
     * @param to 終了日時
     * @return ログの総数
     */
    @Query("SELECT COUNT(sl) FROM SystemLog sl WHERE sl.timestamp BETWEEN :from AND :to")
    long countByTimeRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    
    /**
     * モジュール別の集計を取得
     * Get count by module
     *
     * @param from 開始日時
     * @param to 終了日時
     * @return モジュール名とその件数のリスト
     */
    @Query("SELECT sl.module, COUNT(sl) FROM SystemLog sl " +
           "WHERE sl.timestamp BETWEEN :from AND :to " +
           "GROUP BY sl.module")
    List<Object[]> countByModule(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}