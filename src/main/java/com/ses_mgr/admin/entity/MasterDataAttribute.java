package com.ses_mgr.admin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * マスタデータの属性定義を表すエンティティクラス
 * Master data attribute definition entity
 */
@Entity
@Table(name = "master_data_attribute")
public class MasterDataAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * マスタデータタイプ
     * Master data type
     */
    @ManyToOne
    @JoinColumn(name = "master_type_id", nullable = false)
    private MasterType masterType;

    /**
     * 属性名（識別子）
     * Attribute name (identifier)
     */
    @Column(name = "attribute_name", nullable = false, length = 50)
    private String attributeName;

    /**
     * 属性表示名（日本語）
     * Attribute display name in Japanese
     */
    @Column(name = "display_name_ja", nullable = false, length = 100)
    private String displayNameJa;

    /**
     * 属性表示名（英語）
     * Attribute display name in English
     */
    @Column(name = "display_name_en", nullable = false, length = 100)
    private String displayNameEn;

    /**
     * データ型
     * Data type (string, number, boolean, date, etc.)
     */
    @Column(name = "data_type", nullable = false, length = 20)
    private String dataType;

    /**
     * 必須フラグ
     * Required flag
     */
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    /**
     * デフォルト値
     * Default value
     */
    @Column(name = "default_value", length = 500)
    private String defaultValue;

    /**
     * 入力制約（正規表現など）
     * Input constraint (regex, etc.)
     */
    @Column(name = "validation_rule", length = 500)
    private String validationRule;

    /**
     * 表示順序
     * Display order
     */
    @Column(name = "display_order")
    private Integer displayOrder;

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

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MasterType getMasterType() {
        return masterType;
    }

    public void setMasterType(MasterType masterType) {
        this.masterType = masterType;
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