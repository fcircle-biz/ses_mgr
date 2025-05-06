package com.ses_mgr.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * マスタデータ一括作成リクエストDTO
 * Master data bulk creation request DTO
 */
public class MasterDataBulkCreateRequestDto {

    /**
     * マスタデータタイプID or コード（必須）
     * Master data type ID or code (required)
     */
    @NotNull(message = "マスタデータタイプを指定してください")
    private String typeCode;

    /**
     * マスタデータリスト
     * Master data list
     */
    @NotEmpty(message = "マスタデータリストは必須です")
    @Valid
    private List<MasterDataCreateRequestDto> items = new ArrayList<>();

    /**
     * 既存データ削除フラグ（一括登録前に既存データを削除するか）
     * Delete existing data flag (whether to delete existing data before bulk creation)
     */
    private Boolean deleteExistingData = false;

    // Getters and Setters

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public List<MasterDataCreateRequestDto> getItems() {
        return items;
    }

    public void setItems(List<MasterDataCreateRequestDto> items) {
        this.items = items;
    }

    public Boolean getDeleteExistingData() {
        return deleteExistingData;
    }

    public void setDeleteExistingData(Boolean deleteExistingData) {
        this.deleteExistingData = deleteExistingData;
    }
}