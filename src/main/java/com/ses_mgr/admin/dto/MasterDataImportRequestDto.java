package com.ses_mgr.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * マスタデータインポートリクエストDTO
 * Master data import request DTO
 */
public class MasterDataImportRequestDto {

    /**
     * マスタデータタイプコード
     * Master data type code
     */
    @NotBlank(message = "マスタデータタイプコードは必須です")
    private String typeCode;

    /**
     * ファイルコンテンツ（Base64エンコード）
     * File content (Base64 encoded)
     */
    @NotBlank(message = "ファイルコンテンツは必須です")
    private String fileContent;

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
     * 既存データ削除フラグ（インポート前に既存データを削除するか）
     * Delete existing data flag (whether to delete existing data before import)
     */
    @NotNull(message = "既存データ削除フラグは必須です")
    private Boolean deleteExistingData = false;

    /**
     * 1行目をヘッダーとして扱うフラグ
     * Treat first row as header flag
     */
    @NotNull(message = "ヘッダー行フラグは必須です")
    private Boolean hasHeaderRow = true;
    
    /**
     * データ上書きフラグ
     * Data overwrite flag
     */
    private boolean overwrite = true;
    
    /**
     * インポートデータリスト
     * Import data list
     */
    private List<MasterDataDto> data = new ArrayList<>();

    // Getters and Setters

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
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

    public Boolean getDeleteExistingData() {
        return deleteExistingData;
    }

    public void setDeleteExistingData(Boolean deleteExistingData) {
        this.deleteExistingData = deleteExistingData;
    }

    public Boolean getHasHeaderRow() {
        return hasHeaderRow;
    }

    public void setHasHeaderRow(Boolean hasHeaderRow) {
        this.hasHeaderRow = hasHeaderRow;
    }
    
    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }
    
    public List<MasterDataDto> getData() {
        return data;
    }
    
    public void setData(List<MasterDataDto> data) {
        this.data = data;
    }
}