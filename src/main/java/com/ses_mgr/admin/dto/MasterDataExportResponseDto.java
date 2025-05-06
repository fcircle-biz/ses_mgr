package com.ses_mgr.admin.dto;

/**
 * マスタデータエクスポートレスポンスDTO
 * Master data export response DTO
 */
public class MasterDataExportResponseDto {

    /**
     * ファイルコンテンツ（Base64エンコード）
     * File content (Base64 encoded)
     */
    private String fileContent;

    /**
     * ファイル名
     * File name
     */
    private String fileName;

    /**
     * MIME タイプ
     * MIME type
     */
    private String mimeType;

    /**
     * エクスポートされたレコード数
     * Number of exported records
     */
    private Integer recordCount;

    public MasterDataExportResponseDto() {
    }

    public MasterDataExportResponseDto(String fileContent, String fileName, String mimeType, Integer recordCount) {
        this.fileContent = fileContent;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.recordCount = recordCount;
    }

    // Getters and Setters

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }
}