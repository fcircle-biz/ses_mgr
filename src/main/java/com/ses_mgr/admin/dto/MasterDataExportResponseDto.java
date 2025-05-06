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
    
    /**
     * マスタデータタイプコード
     * Master data type code
     */
    private String typeCode;
    
    /**
     * マスタデータタイプ名
     * Master data type name
     */
    private String typeName;
    
    /**
     * 出力フォーマット
     * Output format
     */
    private String format;
    
    /**
     * 総レコード数
     * Total number of records
     */
    private Integer totalRecords;
    
    /**
     * コンテントタイプ
     * Content type
     */
    private String contentType;
    
    /**
     * エンコードされたデータ
     * Encoded data
     */
    private String data;

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
    
    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}