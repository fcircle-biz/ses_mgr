package com.ses_mgr.common.service;

import java.util.List;
import java.util.Map;

/**
 * バッチ処理の管理サービスインタフェース
 */
public interface BatchService {

    /**
     * 全てのバッチを取得する
     *
     * @return バッチのリスト
     */
    List<Map<String, Object>> getAllBatches();

    /**
     * 定期バッチを取得する
     *
     * @return 定期バッチのリスト
     */
    List<Map<String, Object>> getScheduledBatches();

    /**
     * オンデマンドバッチを取得する
     *
     * @return オンデマンドバッチのリスト
     */
    List<Map<String, Object>> getOnDemandBatches();

    /**
     * バッチ実行履歴を取得する
     *
     * @param searchParams 検索条件
     * @return 実行履歴のリスト
     */
    List<Map<String, Object>> getBatchHistory(Map<String, Object> searchParams);

    /**
     * バッチの詳細情報を取得する
     *
     * @param batchId バッチID
     * @return バッチ詳細情報
     */
    Map<String, Object> getBatchDetails(String batchId);

    /**
     * バッチの実行詳細情報を取得する
     *
     * @param executionId 実行ID
     * @return 実行詳細情報
     */
    Map<String, Object> getExecutionDetails(String executionId);

    /**
     * バッチを実行する
     *
     * @param batchId バッチID
     * @param parameters 実行パラメータ
     * @return 実行結果
     */
    Map<String, Object> executeBatch(String batchId, Map<String, Object> parameters);

    /**
     * バッチのスケジュールを更新する
     *
     * @param batchId バッチID
     * @param scheduleParams スケジュール設定
     * @return 更新結果
     */
    Map<String, Object> updateBatchSchedule(String batchId, Map<String, Object> scheduleParams);

    /**
     * バッチのパラメータ設定を更新する
     *
     * @param batchId バッチID
     * @param paramSettings パラメータ設定
     * @return 更新結果
     */
    Map<String, Object> updateBatchParameters(String batchId, Map<String, Object> paramSettings);

    /**
     * バッチの有効/無効状態を変更する
     *
     * @param batchId バッチID
     * @param enabled 有効フラグ（true: 有効、false: 無効）
     * @return 更新結果
     */
    Map<String, Object> toggleBatchStatus(String batchId, boolean enabled);

    /**
     * 実行中のバッチを停止する
     *
     * @param executionId 実行ID
     * @return 停止結果
     */
    Map<String, Object> stopBatchExecution(String executionId);
}