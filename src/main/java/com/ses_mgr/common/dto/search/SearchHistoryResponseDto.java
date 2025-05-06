package com.ses_mgr.common.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 検索履歴レスポンスDTO
 * ユーザーの検索履歴を返すためのクラス
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryResponseDto {

    /**
     * 検索履歴のリスト
     */
    private List<SearchHistoryItemDto> history;

    /**
     * ページネーション情報
     */
    private PaginationInfo pagination;

    /**
     * 検索履歴アイテムDTO
     * 個々の検索履歴を表現するクラス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchHistoryItemDto {
        private String id;
        private String query;
        private List<String> resourceTypes;
        private Map<String, Object> filters;
        private LocalDateTime createdAt;
        private Integer resultCount;
    }

    /**
     * ページネーション情報DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private Integer currentPage;
        private Integer pageSize;
        private Integer totalPages;
        private Integer totalItems;
    }
}