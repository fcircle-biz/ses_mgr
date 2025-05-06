package com.ses_mgr.common.service.search;

import com.ses_mgr.common.dto.search.SearchHistoryResponseDto;
import com.ses_mgr.common.dto.search.SearchRequestDto;
import com.ses_mgr.common.dto.search.SearchResultDto;

import java.util.UUID;

/**
 * 検索サービスインターフェース
 * システム全体を横断的に検索する機能を提供します
 */
public interface SearchService {

    /**
     * 検索を実行
     *
     * @param searchRequestDto 検索リクエストDTO
     * @param userId ユーザーID
     * @return 検索結果
     */
    SearchResultDto search(SearchRequestDto searchRequestDto, UUID userId);

    /**
     * ユーザーの検索履歴を取得
     *
     * @param userId ユーザーID
     * @param page ページ番号（0から開始）
     * @param pageSize ページサイズ
     * @return 検索履歴レスポンス
     */
    SearchHistoryResponseDto getSearchHistory(UUID userId, int page, int pageSize);

    /**
     * 検索履歴を削除
     *
     * @param searchId 検索履歴ID
     * @param userId ユーザーID
     * @return 削除に成功したらtrue
     */
    boolean deleteSearchHistory(UUID searchId, UUID userId);

    /**
     * 検索インデックスを更新（または作成）
     *
     * @param resourceId リソースID
     * @param resourceType リソースタイプ
     * @param title タイトル
     * @param subtitle サブタイトル（オプション）
     * @param description 説明（オプション）
     * @param attributes 属性（オプション、JSONとして保存される）
     * @param url リソースへのURL（オプション）
     * @param isPublic 公開リソースかどうか
     * @param accessRoles アクセス可能なロールのリスト（オプション、JSONとして保存される）
     * @param createdBy 作成者ID
     * @return 更新に成功したらtrue
     */
    boolean updateSearchIndex(
            String resourceId,
            String resourceType,
            String title,
            String subtitle,
            String description,
            String attributes,
            String url,
            boolean isPublic,
            String accessRoles,
            UUID createdBy);

    /**
     * 検索インデックスを削除
     *
     * @param resourceId リソースID
     * @param resourceType リソースタイプ
     * @return 削除に成功したらtrue
     */
    boolean deleteSearchIndex(String resourceId, String resourceType);
}