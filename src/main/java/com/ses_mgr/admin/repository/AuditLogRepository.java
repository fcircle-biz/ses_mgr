package com.ses_mgr.admin.repository;

import com.ses_mgr.admin.entity.AuditLog;
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
 * 監査ログリポジトリインターフェース
 * Repository interface for audit logs
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * ユーザーIDで監査ログを検索
     * Find audit logs by user ID
     *
     * @param userId ユーザーID
     * @param pageable ページング情報
     * @return 監査ログのページ
     */
    Page<AuditLog> findByUserId(UUID userId, Pageable pageable);
    
    /**
     * アクションタイプで監査ログを検索
     * Find audit logs by action type
     *
     * @param action アクションタイプ
     * @param pageable ページング情報
     * @return 監査ログのページ
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);
    
    /**
     * リソースタイプで監査ログを検索
     * Find audit logs by resource type
     *
     * @param resourceType リソースタイプ
     * @param pageable ページング情報
     * @return 監査ログのページ
     */
    Page<AuditLog> findByResourceType(String resourceType, Pageable pageable);
    
    /**
     * リソースIDで監査ログを検索
     * Find audit logs by resource ID
     *
     * @param resourceId リソースID
     * @param pageable ページング情報
     * @return 監査ログのページ
     */
    Page<AuditLog> findByResourceId(String resourceId, Pageable pageable);
    
    /**
     * リソースタイプとリソースIDで監査ログを検索
     * Find audit logs by resource type and resource ID
     *
     * @param resourceType リソースタイプ
     * @param resourceId リソースID
     * @param pageable ページング情報
     * @return 監査ログのページ
     */
    Page<AuditLog> findByResourceTypeAndResourceId(String resourceType, String resourceId, Pageable pageable);
    
    /**
     * 日時範囲で監査ログを検索
     * Find audit logs by timestamp range
     *
     * @param from 開始日時
     * @param to 終了日時
     * @param pageable ページング情報
     * @return 監査ログのページ
     */
    Page<AuditLog> findByTimestampBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
    
    /**
     * キーワードで監査ログを検索（メッセージフィールドの部分一致）
     * Search audit logs by keyword (partial match in message field)
     *
     * @param keyword 検索キーワード
     * @param pageable ページング情報
     * @return 監査ログのページ
     */
    @Query("SELECT al FROM AuditLog al WHERE LOWER(al.message) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<AuditLog> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 複合条件で監査ログを検索
     * Search audit logs by multiple criteria
     *
     * @param from 開始日時
     * @param to 終了日時
     * @param userId ユーザーID（nullの場合は条件に含めない）
     * @param action アクション（nullの場合は条件に含めない）
     * @param resourceType リソースタイプ（nullの場合は条件に含めない）
     * @param resourceId リソースID（nullの場合は条件に含めない）
     * @param keyword 検索キーワード（nullの場合は条件に含めない）
     * @param pageable ページング情報
     * @return 監査ログのページ
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "(:from IS NULL OR al.timestamp >= :from) AND " +
           "(:to IS NULL OR al.timestamp <= :to) AND " +
           "(:userId IS NULL OR al.userId = :userId) AND " +
           "(:action IS NULL OR al.action = :action) AND " +
           "(:resourceType IS NULL OR al.resourceType = :resourceType) AND " +
           "(:resourceId IS NULL OR al.resourceId = :resourceId) AND " +
           "(:keyword IS NULL OR LOWER(al.message) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<AuditLog> searchByMultipleCriteria(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("userId") UUID userId,
            @Param("action") String action,
            @Param("resourceType") String resourceType,
            @Param("resourceId") String resourceId,
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
    @Query("SELECT al.level, COUNT(al) FROM AuditLog al " +
           "WHERE al.timestamp BETWEEN :from AND :to " +
           "GROUP BY al.level")
    List<Object[]> countByLevel(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    
    /**
     * 総数を取得
     * Get count of logs for a date range
     *
     * @param from 開始日時
     * @param to 終了日時
     * @return ログの総数
     */
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.timestamp BETWEEN :from AND :to")
    long countByTimeRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    
    /**
     * アクション別の集計を取得
     * Get count by action
     *
     * @param from 開始日時
     * @param to 終了日時
     * @return アクションとその件数のリスト
     */
    @Query("SELECT al.action, COUNT(al) FROM AuditLog al " +
           "WHERE al.timestamp BETWEEN :from AND :to " +
           "GROUP BY al.action")
    List<Object[]> countByAction(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    
    /**
     * リソースタイプ別の集計を取得
     * Get count by resource type
     *
     * @param from 開始日時
     * @param to 終了日時
     * @return リソースタイプとその件数のリスト
     */
    @Query("SELECT al.resourceType, COUNT(al) FROM AuditLog al " +
           "WHERE al.timestamp BETWEEN :from AND :to " +
           "GROUP BY al.resourceType")
    List<Object[]> countByResourceType(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    
    /**
     * ユーザー別の集計を取得（アクティブユーザーランキング）
     * Get count by user (active user ranking)
     *
     * @param from 開始日時
     * @param to 終了日時
     * @param limit 取得件数
     * @return ユーザーIDとその件数のリスト
     */
    @Query("SELECT al.userId, al.username, COUNT(al) FROM AuditLog al " +
           "WHERE al.timestamp BETWEEN :from AND :to " +
           "GROUP BY al.userId, al.username " +
           "ORDER BY COUNT(al) DESC")
    List<Object[]> countByUser(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable limit);
}