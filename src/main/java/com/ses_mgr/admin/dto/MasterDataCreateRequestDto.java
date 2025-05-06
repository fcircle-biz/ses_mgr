package com.ses_mgr.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * マスタデータ作成リクエストDTO
 * Master data creation request DTO
 */
public class MasterDataCreateRequestDto {

    /**
     * コード
     * Code value
     */
    @NotBlank(message = "コードは必須です")
    @Size(min = 1, max = 50, message = "コードは1～50文字で入力してください")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "コードは英数字、ハイフン、アンダースコアのみ使用可能です")
    private String code;

    /**
     * 名称（日本語）
     * Name in Japanese
     */
    @NotBlank(message = "名称（日本語）は必須です")
    @Size(min = 1, max = 200, message = "名称（日本語）は1～200文字で入力してください")
    private String nameJa;

    /**
     * 名称（英語）
     * Name in English
     */
    @Size(max = 200, message = "名称（英語）は200文字以内で入力してください")
    private String nameEn;

    /**
     * 短縮名（日本語）
     * Short name in Japanese
     */
    @Size(max = 50, message = "短縮名（日本語）は50文字以内で入力してください")
    private String shortNameJa;

    /**
     * 短縮名（英語）
     * Short name in English
     */
    @Size(max = 50, message = "短縮名（英語）は50文字以内で入力してください")
    private String shortNameEn;

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
     * 親マスタデータID（階層構造用）
     * Parent master data ID (for hierarchical structure)
     */
    private Long parentId;

    /**
     * 追加属性
     * Additional attributes
     */
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * 開始日
     * Start date (validity period start)
     */
    private LocalDateTime validFrom;

    /**
     * 終了日
     * End date (validity period end)
     */
    private LocalDateTime validTo;

    // Getters and Setters

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