package com.ses_mgr.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * コード値カテゴリのDTO
 * Code category DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodeCategoryDto {
    
    /**
     * カテゴリID
     * Category ID
     */
    private String id;
    
    /**
     * カテゴリ名
     * Category name
     */
    private String name;
    
    /**
     * 説明
     * Description
     */
    private String description;
    
    /**
     * コード値の数
     * Number of code values
     */
    private Integer count;
}