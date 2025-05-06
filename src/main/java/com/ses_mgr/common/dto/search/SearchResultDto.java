package com.ses_mgr.common.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 検索結果DTO
 * 検索の結果を返すためのクラス
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDto {

    /**
     * 検索結果のリスト
     * groupByResourceType=falseの場合に使用
     */
    private List<SearchResultItemDto> results;

    /**
     * リソースタイプごとにグループ化された検索結果
     * groupByResourceType=trueの場合に使用
     * キー: リソースタイプ、値: そのリソースタイプの検索結果
     */
    private Map<String, GroupedResultDto> groupedResults;

    /**
     * ページネーション情報
     */
    private PaginationInfo pagination;

    /**
     * 検索ID
     * 検索履歴を参照するためのID
     */
    private String searchId;

    /**
     * 検索実行時間（ミリ秒）
     */
    private Long queryTimeMs;

    /**
     * 検索結果アイテムDTO
     * 個々の検索結果を表現するクラス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResultItemDto {
        private String id;
        private String type;
        private String title;
        private String subtitle;
        private String description;
        private Double matchingScore;
        private String url;
        private Map<String, Object> attributes;
    }

    /**
     * グループ化された検索結果DTO
     * リソースタイプごとにグループ化された検索結果を表現するクラス
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupedResultDto {
        private List<SearchResultItemDto> results;
        private Integer count;
        private Integer total;
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