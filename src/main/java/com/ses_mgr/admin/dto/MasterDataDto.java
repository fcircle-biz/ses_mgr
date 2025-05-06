package com.ses_mgr.admin.dto;

import com.ses_mgr.admin.entity.MasterData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private List<MasterDataAttributeDto> attributeList = new ArrayList<>();
    private Boolean isActive;
    private Boolean isSystemReserved;
    private String status; // テスト互換性のためのステータス（isActiveのエイリアス）
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MasterDataDto() {
    }

    /**
     * エンティティからDTOを作成するコンストラクタ
     * Constructor to create DTO from entity
     */
    public MasterDataDto(MasterData masterData) {
        this.id = masterData.getId();
        
        if (masterData.getMasterType() != null) {
            this.masterTypeId = masterData.getMasterType().getId();
            this.typeCode = masterData.getMasterType().getTypeCode();
        }
        
        this.code = masterData.getCode();
        this.nameJa = masterData.getNameJa();
        this.nameEn = masterData.getNameEn();
        this.shortNameJa = masterData.getShortNameJa();
        this.shortNameEn = masterData.getShortNameEn();
        this.description = masterData.getDescription();
        this.displayOrder = masterData.getDisplayOrder();
        this.parentId = masterData.getParentId();
        
        if (masterData.getAttributes() != null) {
            this.attributes = masterData.getAttributes();
        }
        
        this.isActive = masterData.getIsActive();
        this.status = masterData.getIsActive() ? "active" : "inactive"; // ステータス設定
        this.isSystemReserved = masterData.getIsSystemReserved();
        this.validFrom = masterData.getValidFrom();
        this.validTo = masterData.getValidTo();
        this.createdAt = masterData.getCreatedAt();
        this.updatedAt = masterData.getUpdatedAt();
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * テスト互換性のための名前ゲッター
     * Name getter for test compatibility
     */
    public String getName() {
        return nameJa;
    }

    /**
     * テスト互換性のための名前セッター
     * Name setter for test compatibility
     */
    public void setName(String name) {
        this.nameJa = name;
    }
    
    /**
     * テスト互換性のためのステータスゲッター
     * Status getter for test compatibility
     */
    public String getStatus() {
        return status;
    }

    /**
     * テスト互換性のためのステータスセッター
     * Status setter for test compatibility
     */
    public void setStatus(String status) {
        this.status = status;
        this.isActive = "active".equals(status);
    }
    
    public List<MasterDataAttributeDto> getAttributeList() {
        return attributeList;
    }

    public void setAttributeList(List<MasterDataAttributeDto> attributeList) {
        this.attributeList = attributeList;
    }
}