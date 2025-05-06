package com.ses_mgr.admin.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * マスタデータエクスポートリクエストDTO
 * Master data export request DTO
 */
public class MasterDataExportRequestDto {

    /**
     * マスタデータタイプコード
     * Master data type code
     */
    @NotBlank(message = "マスタデータタイプコードは必須です")
    private String typeCode;

    /**
     * ファイル形式（csv, excel）
     * File format (csv, excel)
     */
    @NotBlank(message = "ファイル形式は必須です")
    private String fileFormat;

    /**
     * 文字コード（CSVの場合のみ）
     * Character encoding (CSV only)
     */
    private String encoding = "UTF-8";

    /**
     * 無効データを含めるフラグ
     * Include inactive data flag
     */
    private Boolean includeInactive = false;

    /**
     * 属性を含めるフラグ
     * Include attributes flag
     */
    private Boolean includeAttributes = true;

    // Getters and Setters

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public Boolean getIncludeInactive() {
        return includeInactive;
    }

    public void setIncludeInactive(Boolean includeInactive) {
        this.includeInactive = includeInactive;
    }

    public Boolean getIncludeAttributes() {
        return includeAttributes;
    }

    public void setIncludeAttributes(Boolean includeAttributes) {
        this.includeAttributes = includeAttributes;
    }
}