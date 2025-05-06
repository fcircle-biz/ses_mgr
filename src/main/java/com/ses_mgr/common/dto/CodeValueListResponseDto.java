package com.ses_mgr.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * コード値一覧レスポンスのDTO
 * Code value list response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodeValueListResponseDto {
    
    /**
     * カテゴリ情報
     * Category information
     */
    private CodeCategoryDto category;
    
    /**
     * コード値リスト
     * Code value list
     */
    private List<CodeValueDto> codes;
}