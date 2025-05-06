package com.ses_mgr.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * コード値のDTO
 * Code value DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodeValueDto {
    
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
}