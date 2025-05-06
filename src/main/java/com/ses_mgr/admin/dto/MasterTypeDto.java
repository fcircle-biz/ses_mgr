package com.ses_mgr.admin.dto;

import com.ses_mgr.admin.entity.MasterType;

/**
 * マスタデータタイプのDTO
 * Master data type DTO
 */
public class MasterTypeDto {

    private Long id;
    private String typeCode;
    private String typeNameJa;
    private String typeNameEn;
    private String description;
    private Integer displayOrder;
    private Boolean isActive;
    private Boolean isSystemReserved;

    public MasterTypeDto() {
    }

    /**
     * エンティティからDTOを作成するコンストラクタ
     * Constructor to create DTO from entity
     */
    public MasterTypeDto(MasterType masterType) {
        this.id = masterType.getId();
        this.typeCode = masterType.getTypeCode();
        this.typeNameJa = masterType.getTypeNameJa();
        this.typeNameEn = masterType.getTypeNameEn();
        this.description = masterType.getDescription();
        this.displayOrder = masterType.getDisplayOrder();
        this.isActive = masterType.getIsActive();
        this.isSystemReserved = masterType.getIsSystemReserved();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsSystemReserved() {
        return isSystemReserved;
    }

    public void setIsSystemReserved(Boolean isSystemReserved) {
        this.isSystemReserved = isSystemReserved;
    }
}