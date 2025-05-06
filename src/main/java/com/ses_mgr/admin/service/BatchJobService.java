package com.ses_mgr.admin.service;

import com.ses_mgr.admin.dto.batch.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.UUID;

public interface BatchJobService {

    /**
     * バッチジョブ一覧を取得する
     * 
     * @param status ステータスでフィルタリング
     * @param category カテゴリでフィルタリング
     * @param search 検索キーワード
     * @param page ページ番号
     * @param limit 1ページあたりの件数
     * @param sort ソート条件
     * @return バッチジョブ一覧
     */
    Page<BatchJobDto> getBatchJobs(String status, String category, String search, 
                                   Integer page, Integer limit, String sort);

    /**
     * バッチジョブの詳細を取得する
     * 
     * @param jobId バッチジョブID
     * @return バッチジョブ詳細
     */
    BatchJobDetailDto getBatchJobById(String jobId);

    /**
     * バッチジョブのステータスを更新する
     * 
     * @param jobId バッチジョブID
     * @param statusUpdateRequest ステータス更新リクエスト
     * @param updatedBy 更新者ID
     * @return 更新後のバッチジョブ情報
     */
    BatchJobDto updateBatchJobStatus(String jobId, BatchJobStatusUpdateRequestDto statusUpdateRequest, UUID updatedBy);

    /**
     * バッチジョブを実行する
     * 
     * @param jobId バッチジョブID
     * @param executeRequest 実行リクエスト
     * @param executedBy 実行者ID
     * @return 実行結果
     */
    BatchJobExecuteResponseDto executeBatchJob(String jobId, BatchJobExecuteRequestDto executeRequest, String executedBy);

    /**
     * バッチジョブの実行ステータスを更新する (内部用)
     * 
     * @param executionId 実行ID
     * @param status 新しいステータス
     * @param endTime 終了時間
     * @param recordsProcessed 処理レコード数
     * @param errorMessage エラーメッセージ
     * @return 更新に成功したかどうか
     */
    boolean updateJobExecutionStatus(String executionId, String status, LocalDateTime endTime, 
                                    Integer recordsProcessed, String errorMessage);
}