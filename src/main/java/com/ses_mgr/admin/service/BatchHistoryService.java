package com.ses_mgr.admin.service;

import com.ses_mgr.admin.dto.batch.BatchHistoryItemDto;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface BatchHistoryService {

    /**
     * バッチ実行履歴を取得する
     * 
     * @param jobId ジョブIDでフィルタリング
     * @param status ステータスでフィルタリング
     * @param from 開始日時
     * @param to 終了日時
     * @param executedBy 実行者でフィルタリング
     * @param page ページ番号
     * @param limit 1ページあたりの件数
     * @param sort ソート条件
     * @return バッチ実行履歴一覧
     */
    Page<BatchHistoryItemDto> getBatchHistory(String jobId, String status, 
                                             LocalDateTime from, LocalDateTime to, 
                                             String executedBy, Integer page, Integer limit, String sort);

    /**
     * 特定の実行IDのバッチ実行履歴を取得する
     * 
     * @param executionId 実行ID
     * @return バッチ実行履歴
     */
    BatchHistoryItemDto getBatchHistoryById(String executionId);
}