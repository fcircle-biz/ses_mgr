package com.ses_mgr.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * コード値詳細のDTO
 * Code value detail DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodeValueDetailDto {
    
    /**
     * カテゴリ情報
     * Category information
     */
    private CodeCategoryDto category;
    
    /**
     * コード
     * Code
     */
    private String code;
    
    /**
     * 名称
     * Name
     */
    private String name;
    
    /**
     * 説明
     * Description
     */
    private String description;
    
    /**
     * 表示順
     * Display order
     */
    private Integer sortOrder;
    
    /**
     * アクティブ状態
     * Active state
     */
    private Boolean isActive;
    
    /**
     * 追加属性
     * Additional attributes
     */
    private Map<String, Object> attributes;
    
    /**
     * 親コード
     * Parent code
     */
    private String parentCode;
    
    /**
     * 子コードリスト
     * Child code list
     */
    private List<String> childCodes;
    
    /**
     * 作成日時
     * Created date
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新日時
     * Updated date
     */
    private LocalDateTime updatedAt;
}