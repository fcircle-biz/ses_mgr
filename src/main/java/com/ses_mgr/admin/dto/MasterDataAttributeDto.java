package com.ses_mgr.admin.dto;

import com.ses_mgr.admin.entity.MasterDataAttribute;

/**
 * マスタデータ属性定義のDTO
 * Master data attribute definition DTO
 */
public class MasterDataAttributeDto {

    private Long id;
    private Long masterTypeId;
    private String typeCode; // マスタタイプコード
    private String attributeName;
    private String displayNameJa;
    private String displayNameEn;
    private String dataType;
    private Boolean isRequired;
    private String defaultValue;
    private String validationRule;
    private Integer displayOrder;
    private String value; // テスト互換性のための値（MasterDataAttributeValueから参照）

    public MasterDataAttributeDto() {
    }

    /**
     * エンティティからDTOを作成するコンストラクタ
     * Constructor to create DTO from entity
     */
    public MasterDataAttributeDto(MasterDataAttribute attribute) {
        this.id = attribute.getId();
        this.masterTypeId = attribute.getMasterType().getId();
        this.typeCode = attribute.getMasterType().getTypeCode();
        this.attributeName = attribute.getAttributeName();
        this.displayNameJa = attribute.getDisplayNameJa();
        this.displayNameEn = attribute.getDisplayNameEn();
        this.dataType = attribute.getDataType();
        this.isRequired = attribute.getIsRequired();
        this.defaultValue = attribute.getDefaultValue();
        this.validationRule = attribute.getValidationRule();
        this.displayOrder = attribute.getDisplayOrder();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMasterTypeId() {
        return masterTypeId;
    }

    public void setMasterTypeId(Long masterTypeId) {
        this.masterTypeId = masterTypeId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

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
    
    /**
     * テスト互換性のための値ゲッター
     * Value getter for test compatibility
     */
    public String getValue() {
        return value;
    }

    /**
     * テスト互換性のための値セッター
     * Value setter for test compatibility
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    /**
     * テスト互換性のための名前ゲッター
     * Name getter for test compatibility
     */
    public String getName() {
        return attributeName;
    }

    /**
     * テスト互換性のための名前セッター
     * Name setter for test compatibility
     */
    public void setName(String name) {
        this.attributeName = name;
    }
}