package com.ses_mgr.common.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * 検索リクエストDTO
 * クライアントからの検索リクエストを受け取るためのクラス
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestDto {

    /**
     * 検索キーワード
     * 必須パラメータであり、空白文字で分割して複数のキーワードの指定が可能
     */
    @NotBlank(message = "検索キーワードは必須です")
    @Size(max = 255, message = "検索キーワードは255文字以内で指定してください")
    private String query;

    /**
     * 検索対象のリソースタイプ
     * 指定しない場合は全てのリソースタイプが検索対象となる
     */
    private List<String> resourceTypes;

    /**
     * 検索フィルター条件
     * キーがフィルター名、値がフィルター条件
     */
    private Map<String, Object> filters;

    /**
     * 検索結果のソート条件
     */
    private SortDto sort;

    /**
     * ページネーション設定
     */
    private PaginationDto pagination;

    /**
     * 検索結果をリソースタイプごとにグループ化するかどうか
     * デフォルトはfalse
     */
    private Boolean groupByResourceType;

    /**
     * この検索条件を検索履歴に保存するかどうか
     * デフォルトはfalse
     */
    private Boolean saveSearch;

    /**
     * ソート条件DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortDto {
        private String field;
        private String order; // "asc" または "desc"
    }

    /**
     * ページネーション設定DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationDto {
        private Integer page;
        private Integer pageSize;
    }
}