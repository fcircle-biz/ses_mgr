package com.ses_mgr.admin.dto;

import com.ses_mgr.admin.entity.MasterType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Map<String, Object>> attributes = new ArrayList<>();

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
        this.createdAt = masterType.getCreatedAt();
        this.updatedAt = masterType.getUpdatedAt();
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

    /**
     * テスト互換性のためのコードゲッター
     * Code getter for test compatibility
     */
    public String getCode() {
        return typeCode;
    }

    /**
     * テスト互換性のためのコードセッター
     * Code setter for test compatibility
     */
    public void setCode(String code) {
        this.typeCode = code;
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

    /**
     * テスト互換性のための名前ゲッター
     * Name getter for test compatibility
     */
    public String getName() {
        return typeNameJa;
    }

    /**
     * テスト互換性のための名前セッター
     * Name setter for test compatibility
     */
    public void setName(String name) {
        this.typeNameJa = name;
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
    
    public List<Map<String, Object>> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Map<String, Object>> attributes) {
        this.attributes = attributes;
    }
}