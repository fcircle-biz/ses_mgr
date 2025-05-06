package com.ses_mgr.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * マスタデータタイプ作成リクエストDTO
 * Master data type creation request DTO
 */
public class MasterTypeCreateRequestDto {

    /**
     * マスタデータタイプコード
     * Master data type code
     */
    @NotBlank(message = "タイプコードは必須です")
    @Size(min = 1, max = 50, message = "タイプコードは1～50文字で入力してください")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "タイプコードは小文字英数字とアンダースコアのみ使用可能で、英字で始まる必要があります")
    private String typeCode;

    /**
     * マスタデータタイプ名（日本語）
     * Master data type name in Japanese
     */
    @NotBlank(message = "タイプ名（日本語）は必須です")
    @Size(min = 1, max = 100, message = "タイプ名（日本語）は1～100文字で入力してください")
    private String typeNameJa;

    /**
     * マスタデータタイプ名（英語）
     * Master data type name in English
     */
    @NotBlank(message = "タイプ名（英語）は必須です")
    @Size(min = 1, max = 100, message = "タイプ名（英語）は1～100文字で入力してください")
    private String typeNameEn;

    /**
     * 説明
     * Description
     */
    @Size(max = 500, message = "説明は500文字以内で入力してください")
    private String description;

    /**
     * 表示順序
     * Display order
     */
    private Integer displayOrder;

    // Getters and Setters

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeNameJa() {
        return typeNameJa;
    }

    public void setTypeNameJa(String typeNameJa) {
        this.typeNameJa = typeNameJa;
    }

    public String getTypeNameEn() {
        return typeNameEn;
    }

    public void setTypeNameEn(String typeNameEn) {
        this.typeNameEn = typeNameEn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}