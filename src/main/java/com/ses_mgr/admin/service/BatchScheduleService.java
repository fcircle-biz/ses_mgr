package com.ses_mgr.admin.service;

import com.ses_mgr.admin.dto.batch.BatchScheduleCreateRequestDto;
import com.ses_mgr.admin.dto.batch.BatchScheduleDto;
import com.ses_mgr.admin.dto.batch.BatchScheduleUpdateRequestDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface BatchScheduleService {

    /**
     * バッチスケジュール一覧を取得する
     * 
     * @param jobId ジョブIDでフィルタリング
     * @param isActive 有効なスケジュールのみ取得
     * @param type スケジュールタイプでフィルタリング
     * @param page ページ番号
     * @param limit 1ページあたりの件数
     * @return バッチスケジュール一覧
     */
    Page<BatchScheduleDto> getBatchSchedules(String jobId, Boolean isActive, String type, 
                                           Integer page, Integer limit);

    /**
     * バッチスケジュールを登録する
     * 
     * @param createRequest 登録リクエスト
     * @param createdBy 作成者ID
     * @return 登録したバッチスケジュール
     */
    BatchScheduleDto createBatchSchedule(BatchScheduleCreateRequestDto createRequest, UUID createdBy);

    /**
     * バッチスケジュールを更新する
     * 
     * @param scheduleId スケジュールID
     * @param updateRequest 更新リクエスト
     * @param updatedBy 更新者ID
     * @return 更新したバッチスケジュール
     */
    BatchScheduleDto updateBatchSchedule(String scheduleId, BatchScheduleUpdateRequestDto updateRequest, UUID updatedBy);

    /**
     * バッチスケジュールを削除する
     * 
     * @param scheduleId スケジュールID
     * @return 削除に成功したかどうか
     */
    boolean deleteBatchSchedule(String scheduleId);

    /**
     * 次回実行日時を計算して更新する
     * 
     * @param scheduleId スケジュールID
     * @return 更新後のバッチスケジュール
     */
    BatchScheduleDto updateNextRunTime(String scheduleId);
}