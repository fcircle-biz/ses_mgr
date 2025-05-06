package com.ses_mgr.admin.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * マスタデータインポートレスポンスDTO
 * Master data import response DTO
 */
public class MasterDataImportResponseDto {

    /**
     * マスタデータタイプコード
     * Master data type code
     */
    private String typeCode;

    /**
     * 処理レコード総数
     * Total number of processed records
     */
    private Integer totalRecords = 0;

    /**
     * 成功件数
     * Number of successful records
     */
    private Integer successCount = 0;

    /**
     * エラー件数
     * Number of error records
     */
    private Integer errorCount = 0;

    /**
     * エラーメッセージリスト
     * List of error messages
     */
    private List<String> errors = new ArrayList<>();

    /**
     * メッセージ
     * Message
     */
    private String message;

    // Getters and Setters

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}