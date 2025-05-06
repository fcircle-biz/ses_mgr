package com.ses_mgr.common.repository.search;

import com.ses_mgr.common.entity.search.SearchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * 検索履歴リポジトリ
 * 検索履歴の永続化を担当します
 */
@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, UUID> {

    /**
     * 特定のユーザーの検索履歴を取得
     *
     * @param userId ユーザーID
     * @param pageable ページネーション情報
     * @return ユーザーの検索履歴のページ
     */
    Page<SearchHistory> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * 特定のユーザーの検索履歴を削除
     *
     * @param userId ユーザーID
     * @param searchId 検索履歴ID
     * @return 削除した件数
     */
    long deleteByUserIdAndSearchId(UUID userId, UUID searchId);
    
    /**
     * 特定のユーザーの検索履歴の件数を取得
     *
     * @param userId ユーザーID
     * @return 検索履歴の件数
     */
    long countByUserId(UUID userId);
}