package com.ses_mgr.admin.entity;

import com.ses_mgr.admin.util.AttributeConverter;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * マスタデータを表すエンティティクラス
 * Master data entity representing individual master data items
 */
@Entity
@Table(name = "master_data")
public class MasterData {

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
     * コード
     * Code value
     */
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    /**
     * 名称（日本語）
     * Name in Japanese
     */
    @Column(name = "name_ja", nullable = false, length = 200)
    private String nameJa;

    /**
     * 名称（英語）
     * Name in English
     */
    @Column(name = "name_en", length = 200)
    private String nameEn;

    /**
     * 短縮名（日本語）
     * Short name in Japanese
     */
    @Column(name = "short_name_ja", length = 50)
    private String shortNameJa;

    /**
     * 短縮名（英語）
     * Short name in English
     */
    @Column(name = "short_name_en", length = 50)
    private String shortNameEn;

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
     * 親マスタデータID（階層構造用）
     * Parent master data ID (for hierarchical structure)
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 追加属性（JSON形式で保存）
     * Additional attributes (stored as JSON)
     */
    @Column(name = "attributes", columnDefinition = "jsonb")
    @Convert(converter = AttributeConverter.class)
    private Map<String, Object> attributes = new HashMap<>();

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
     * 開始日
     * Start date (validity period start)
     */
    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    /**
     * 終了日
     * End date (validity period end)
     */
    @Column(name = "valid_to")
    private LocalDateTime validTo;

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
    
    /**
     * 属性値を取得
     * Get attribute value
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    /**
     * 属性値を設定
     * Set attribute value
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    /**
     * 属性が存在するかチェック
     * Check if attribute exists
     */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }
    
    /**
     * 属性を削除
     * Remove attribute
     */
    public void removeAttribute(String key) {
        attributes.remove(key);
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