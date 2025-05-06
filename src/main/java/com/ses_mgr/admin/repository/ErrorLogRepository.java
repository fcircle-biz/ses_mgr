package com.ses_mgr.admin.repository;

import com.ses_mgr.admin.entity.ErrorLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * エラーログリポジトリインターフェース
 * Repository interface for error logs
 */
@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

    /**
     * ログレベルでエラーログを検索
     * Find error logs by level
     *
     * @param level ログレベル
     * @param pageable ページング情報
     * @return エラーログのページ
     */
    Page<ErrorLog> findByLevel(String level, Pageable pageable);
    
    /**
     * エラーコードでエラーログを検索
     * Find error logs by error code
     *
     * @param errorCode エラーコード
     * @param pageable ページング情報
     * @return エラーログのページ
     */
    Page<ErrorLog> findByErrorCode(String errorCode, Pageable pageable);
    
    /**
     * モジュール名でエラーログを検索
     * Find error logs by module name
     *
     * @param module モジュール名
     * @param pageable ページング情報
     * @return エラーログのページ
     */
    Page<ErrorLog> findByModule(String module, Pageable pageable);
    
    /**
     * 日時範囲でエラーログを検索
     * Find error logs by timestamp range
     *
     * @param from 開始日時
     * @param to 終了日時
     * @param pageable ページング情報
     * @return エラーログのページ
     */
    Page<ErrorLog> findByTimestampBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
    
    /**
     * キーワードでエラーログを検索（メッセージまたはエラーメッセージフィールドの部分一致）
     * Search error logs by keyword (partial match in message or errorMessage field)
     *
     * @param keyword 検索キーワード
     * @param pageable ページング情報
     * @return エラーログのページ
     */
    @Query("SELECT el FROM ErrorLog el WHERE " +
           "LOWER(el.message) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(el.errorMessage) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ErrorLog> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 複合条件でエラーログを検索
     * Search error logs by multiple criteria
     *
     * @param from 開始日時
     * @param to 終了日時
     * @param level ログレベル（nullの場合は条件に含めない）
     * @param module モジュール名（nullの場合は条件に含めない）
     * @param errorCode エラーコード（nullの場合は条件に含めない）
     * @param keyword 検索キーワード（nullの場合は条件に含めない）
     * @param pageable ページング情報
     * @return エラーログのページ
     */
    @Query("SELECT el FROM ErrorLog el WHERE " +
           "(:from IS NULL OR el.timestamp >= :from) AND " +
           "(:to IS NULL OR el.timestamp <= :to) AND " +
           "(:level IS NULL OR el.level = :level) AND " +
           "(:module IS NULL OR el.module = :module) AND " +
           "(:errorCode IS NULL OR el.errorCode = :errorCode) AND " +
           "(:keyword IS NULL OR LOWER(el.message) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(el.errorMessage) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ErrorLog> searchByMultipleCriteria(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("level") String level,
            @Param("module") String module,
            @Param("errorCode") String errorCode,
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
    @Query("SELECT el.level, COUNT(el) FROM ErrorLog el " +
           "WHERE el.timestamp BETWEEN :from AND :to " +
           "GROUP BY el.level")
    List<Object[]> countByLevel(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    
    /**
     * エラーコード別の集計を取得（発生頻度順）
     * Get count by error code (ordered by frequency)
     *
     * @param from 開始日時
     * @param to 終了日時
     * @return エラーコードとその件数のリスト
     */
    @Query("SELECT el.errorCode, COUNT(el) FROM ErrorLog el " +
           "WHERE el.timestamp BETWEEN :from AND :to " +
           "GROUP BY el.errorCode " +
           "ORDER BY COUNT(el) DESC")
    List<Object[]> countByErrorCode(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    
    /**
     * モジュール別の集計を取得
     * Get count by module
     *
     * @param from 開始日時
     * @param to 終了日時
     * @return モジュール名とその件数のリスト
     */
    @Query("SELECT el.module, COUNT(el) FROM ErrorLog el " +
           "WHERE el.timestamp BETWEEN :from AND :to " +
           "GROUP BY el.module " +
           "ORDER BY COUNT(el) DESC")
    List<Object[]> countByModule(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    
    /**
     * 最近のエラーコード別の最終発生時間を取得
     * Get latest occurrence by error code
     *
     * @param from 開始日時
     * @param to 終了日時
     * @return エラーコードと最終発生時間のリスト
     */
    @Query("SELECT el.errorCode, MAX(el.timestamp) FROM ErrorLog el " +
           "WHERE el.timestamp BETWEEN :from AND :to " +
           "GROUP BY el.errorCode " +
           "ORDER BY MAX(el.timestamp) DESC")
    List<Object[]> getLatestOccurrenceByErrorCode(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    
    /**
     * 指定した時間範囲内のエラーログ数を取得
     * Count error logs within specified time range
     *
     * @param from 開始日時
     * @param to 終了日時
     * @return ログの件数
     */
    @Query("SELECT COUNT(el) FROM ErrorLog el WHERE el.timestamp BETWEEN :from AND :to")
    long countByTimeRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}