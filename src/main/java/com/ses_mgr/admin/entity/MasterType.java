package com.ses_mgr.admin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * マスタデータタイプを表すエンティティクラス
 * Master data type entity representing categories of master data
 */
@Entity
@Table(name = "master_type")
public class MasterType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * マスタデータタイプコード（例：department, skill, industry）
     * Master data type code (e.g., department, skill, industry)
     */
    @Column(name = "type_code", nullable = false, unique = true, length = 50)
    private String typeCode;

    /**
     * マスタデータタイプ名（日本語）
     * Master data type name in Japanese
     */
    @Column(name = "type_name_ja", nullable = false, length = 100)
    private String typeNameJa;

    /**
     * マスタデータタイプ名（英語）
     * Master data type name in English
     */
    @Column(name = "type_name_en", nullable = false, length = 100)
    private String typeNameEn;

    /**
     * 説明
     * Description
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 表示順序
     * Display order
     */
    @Column(name = "display_order")
    private Integer displayOrder;

    /**
     * アクティブフラグ
     * Active flag (true = active, false = inactive)
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * システム予約フラグ（システム管理者のみ変更可能）
     * System reserved flag (only system administrators can modify)
     */
    @Column(name = "is_system_reserved", nullable = false)
    private Boolean isSystemReserved = false;

    /**
     * 作成者ID
     * Created by user ID
     */
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    /**
     * 作成日時
     * Created date and time
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新者ID
     * Updated by user ID
     */
    @Column(name = "updated_by", nullable = false)
    private Long updatedBy;

    /**
     * 更新日時
     * Updated date and time
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * マスタデータタイプに関連するマスタデータのリスト
     * List of master data associated with this master type
     */
    @OneToMany(mappedBy = "masterType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MasterData> masterDataList = new ArrayList<>();
    
    /**
     * マスタデータタイプに関連する属性定義のリスト
     * List of attribute definitions associated with this master type
     */
    @OneToMany(mappedBy = "masterType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MasterDataAttribute> attributeList = new ArrayList<>();
    
    /**
     * 属性定義を表すJSON文字列
     * JSON string representing attribute definitions
     */
    @Column(name = "attribute_definition", columnDefinition = "TEXT")
    private String attributeDefinition = "[]";

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

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<MasterData> getMasterDataList() {
        return masterDataList;
    }

    public void setMasterDataList(List<MasterData> masterDataList) {
        this.masterDataList = masterDataList;
    }

    public List<MasterDataAttribute> getAttributeList() {
        return attributeList;
    }

    public void setAttributeList(List<MasterDataAttribute> attributeList) {
        this.attributeList = attributeList;
    }
    
    public String getAttributeDefinition() {
        return attributeDefinition;
    }

    public void setAttributeDefinition(String attributeDefinition) {
        this.attributeDefinition = attributeDefinition;
    }
    
    /**
     * typeCodeのエイリアスとしてのcode getter
     * Code getter as an alias for typeCode
     */
    public String getCode() {
        return this.typeCode;
    }
    
    /**
     * typeCodeのエイリアスとしてのcode setter
     * Code setter as an alias for typeCode
     */
    public void setCode(String code) {
        this.typeCode = code;
    }
    
    /**
     * typeNameJaのエイリアスとしてのname getter
     * Name getter as an alias for typeNameJa
     */
    public String getName() {
        return this.typeNameJa;
    }
    
    /**
     * typeNameJaのエイリアスとしてのname setter
     * Name setter as an alias for typeNameJa
     */
    public void setName(String name) {
        this.typeNameJa = name;
    }

    /**
     * マスタデータをリストに追加
     * Add master data to the list
     */
    public void addMasterData(MasterData masterData) {
        masterDataList.add(masterData);
        masterData.setMasterType(this);
    }

    /**
     * マスタデータをリストから削除
     * Remove master data from the list
     */
    public void removeMasterData(MasterData masterData) {
        masterDataList.remove(masterData);
        masterData.setMasterType(null);
    }
    
    /**
     * 属性定義をリストに追加
     * Add attribute definition to the list
     */
    public void addAttribute(MasterDataAttribute attribute) {
        attributeList.add(attribute);
        attribute.setMasterType(this);
    }

    /**
     * 属性定義をリストから削除
     * Remove attribute definition from the list
     */
    public void removeAttribute(MasterDataAttribute attribute) {
        attributeList.remove(attribute);
        attribute.setMasterType(null);
    }

    /**
     * 作成前にタイムスタンプを設定
     * Set timestamps before creation
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    /**
     * 更新前にタイムスタンプを更新
     * Update timestamp before update
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}