package com.ses_mgr.admin.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * マスタデータインポート結果DTO
 * Master data import result DTO
 */
public class MasterDataImportResultDto {

    /**
     * 処理レコード数
     * Number of processed records
     */
    private Integer totalRecords = 0;

    /**
     * 成功レコード数
     * Number of successful records
     */
    private Integer successCount = 0;

    /**
     * 失敗レコード数
     * Number of failed records
     */
    private Integer failureCount = 0;

    /**
     * 削除レコード数（既存データ削除の場合）
     * Number of deleted records (when existing data is deleted)
     */
    private Integer deletedCount = 0;

    /**
     * エラーメッセージリスト
     * List of error messages
     */
    private List<ImportError> errors = new ArrayList<>();

    /**
     * インポートエラー情報
     * Import error information
     */
    public static class ImportError {
        private Integer rowNumber;
        private String code;
        private String message;

        public ImportError() {
        }

        public ImportError(Integer rowNumber, String code, String message) {
            this.rowNumber = rowNumber;
            this.code = code;
            this.message = message;
        }

        public Integer getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(Integer rowNumber) {
            this.rowNumber = rowNumber;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    // Getters and Setters

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

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }

    public Integer getDeletedCount() {
        return deletedCount;
    }

    public void setDeletedCount(Integer deletedCount) {
        this.deletedCount = deletedCount;
    }

    public List<ImportError> getErrors() {
        return errors;
    }

    public void setErrors(List<ImportError> errors) {
        this.errors = errors;
    }

    /**
     * エラーを追加する
     * Add an error
     */
    public void addError(Integer rowNumber, String code, String message) {
        this.errors.add(new ImportError(rowNumber, code, message));
        this.failureCount++;
    }

    /**
     * 成功カウントを増やす
     * Increment success count
     */
    public void incrementSuccessCount() {
        this.successCount++;
    }
}