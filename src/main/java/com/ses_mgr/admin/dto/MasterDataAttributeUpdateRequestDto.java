package com.ses_mgr.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * マスタデータ属性定義更新リクエストDTO
 * Master data attribute definition update request DTO
 */
public class MasterDataAttributeUpdateRequestDto {

    /**
     * 属性表示名（日本語）
     * Attribute display name in Japanese
     */
    @NotBlank(message = "表示名（日本語）は必須です")
    @Size(min = 1, max = 100, message = "表示名（日本語）は1～100文字で入力してください")
    private String displayNameJa;

    /**
     * 属性表示名（英語）
     * Attribute display name in English
     */
    @NotBlank(message = "表示名（英語）は必須です")
    @Size(min = 1, max = 100, message = "表示名（英語）は1～100文字で入力してください")
    private String displayNameEn;

    /**
     * データ型
     * Data type (string, number, boolean, date, etc.)
     */
    @NotBlank(message = "データ型は必須です")
    @Size(min = 1, max = 20, message = "データ型は1～20文字で入力してください")
    private String dataType;

    /**
     * 必須フラグ
     * Required flag
     */
    private Boolean isRequired = false;

    /**
     * デフォルト値
     * Default value
     */
    @Size(max = 500, message = "デフォルト値は500文字以内で入力してください")
    private String defaultValue;

    /**
     * 入力制約（正規表現など）
     * Input constraint (regex, etc.)
     */
    @Size(max = 500, message = "入力制約は500文字以内で入力してください")
    private String validationRule;

    /**
     * 表示順序
     * Display order
     */
    private Integer displayOrder;

    // Getters and Setters

    public String getDisplayNameJa() {
        return displayNameJa;
    }

    public void setDisplayNameJa(String displayNameJa) {
        this.displayNameJa = displayNameJa;
    }

    public String getDisplayNameEn() {
        return displayNameEn;
    }

    public void setDisplayNameEn(String displayNameEn) {
        this.displayNameEn = displayNameEn;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getValidationRule() {
        return validationRule;
    }

    public void setValidationRule(String validationRule) {
        this.validationRule = validationRule;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}