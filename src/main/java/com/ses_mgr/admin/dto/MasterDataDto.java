package com.ses_mgr.admin.dto;

import com.ses_mgr.admin.entity.MasterData;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * マスタデータのDTO
 * Master data DTO
 */
public class MasterDataDto {

    private Long id;
    private Long masterTypeId;
    private String typeCode; // マスタタイプコード
    private String code;
    private String nameJa;
    private String nameEn;
    private String shortNameJa;
    private String shortNameEn;
    private String description;
    private Integer displayOrder;
    private Long parentId;
    private Map<String, Object> attributes = new HashMap<>();
    private Boolean isActive;
    private Boolean isSystemReserved;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    public MasterDataDto() {
    }

    /**
     * エンティティからDTOを作成するコンストラクタ
     * Constructor to create DTO from entity
     */
    public MasterDataDto(MasterData masterData) {
        this.id = masterData.getId();
        this.masterTypeId = masterData.getMasterType().getId();
        this.typeCode = masterData.getMasterType().getTypeCode();
        this.code = masterData.getCode();
        this.nameJa = masterData.getNameJa();
        this.nameEn = masterData.getNameEn();
        this.shortNameJa = masterData.getShortNameJa();
        this.shortNameEn = masterData.getShortNameEn();
        this.description = masterData.getDescription();
        this.displayOrder = masterData.getDisplayOrder();
        this.parentId = masterData.getParentId();
        this.attributes = masterData.getAttributes() != null ? masterData.getAttributes() : new HashMap<>();
        this.isActive = masterData.getIsActive();
        this.isSystemReserved = masterData.getIsSystemReserved();
        this.validFrom = masterData.getValidFrom();
        this.validTo = masterData.getValidTo();
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNameJa() {
        return nameJa;
    }

    public void setNameJa(String nameJa) {
        this.nameJa = nameJa;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getShortNameJa() {
        return shortNameJa;
    }

    public void setShortNameJa(String shortNameJa) {
        this.shortNameJa = shortNameJa;
    }

    public String getShortNameEn() {
        return shortNameEn;
    }

    public void setShortNameEn(String shortNameEn) {
        this.shortNameEn = shortNameEn;
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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
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

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDateTime validTo) {
        this.validTo = validTo;
    }
}