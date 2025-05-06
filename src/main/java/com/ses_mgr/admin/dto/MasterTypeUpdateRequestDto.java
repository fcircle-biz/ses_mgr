package com.ses_mgr.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * マスタデータタイプ更新リクエストDTO
 * Master data type update request DTO
 */
public class MasterTypeUpdateRequestDto {

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

    /**
     * アクティブフラグ
     * Active flag
     */
    @NotNull(message = "アクティブフラグは必須です")
    private Boolean isActive;

    // Getters and Setters

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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}