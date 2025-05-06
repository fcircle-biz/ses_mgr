package com.ses_mgr.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * コード値カテゴリ一覧レスポンスのDTO
 * Code category list response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodeCategoryListResponseDto {
    
    /**
     * カテゴリリスト
     * Category list
     */
    private List<CodeCategoryDto> categories;
}