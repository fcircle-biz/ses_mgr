package com.ses_mgr.admin.dto;

/**
 * マスタデータ検索リクエストDTO
 * Master data search request DTO
 */
public class MasterDataSearchRequestDto {

    /**
     * キーワード
     * Keyword for search
     */
    private String keyword;

    /**
     * アクティブフラグ（指定がなければ全て）
     * Active flag (all if not specified)
     */
    private Boolean isActive;

    /**
     * 親ID（階層構造の絞り込み用）
     * Parent ID (for hierarchical filtering)
     */
    private Long parentId;

    /**
     * ページ番号（0から開始）
     * Page number (starting from 0)
     */
    private Integer page = 0;

    /**
     * ページサイズ
     * Page size
     */
    private Integer size = 50;

    /**
     * ソートフィールド
     * Sort field
     */
    private String sort = "displayOrder,asc";

    // Getters and Setters

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}