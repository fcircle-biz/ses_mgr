package com.ses_mgr.admin.service;

import com.ses_mgr.admin.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * マスタデータサービスインターフェース
 * Master data service interface
 */
public interface MasterDataService {

    // Master Type Management

    /**
     * マスタデータタイプ一覧を取得
     * Get list of master data types
     *
     * @return マスタデータタイプのリスト
     */
    List<MasterTypeDto> getAllMasterTypes();

    /**
     * マスタデータタイプを取得
     * Get master data type by code
     *
     * @param code タイプコード
     * @return マスタデータタイプ
     */
    MasterTypeDto getMasterTypeByCode(String code);

    /**
     * マスタデータタイプを作成
     * Create master data type
     *
     * @param masterTypeDto マスタデータタイプ情報
     * @return 作成されたマスタデータタイプ
     */
    MasterTypeDto createMasterType(MasterTypeDto masterTypeDto);

    /**
     * マスタデータタイプを更新
     * Update master data type
     *
     * @param code           タイプコード
     * @param masterTypeDto 更新情報
     * @return 更新されたマスタデータタイプ
     */
    MasterTypeDto updateMasterType(String code, MasterTypeDto masterTypeDto);

    /**
     * マスタデータタイプを削除
     * Delete master data type
     *
     * @param code タイプコード
     */
    void deleteMasterType(String code);

    // Master Data Management

    /**
     * マスタデータ一覧を取得
     * Get master data list
     *
     * @param typeCode    タイプコード
     * @param search      検索キーワード（オプション）
     * @param status      ステータス（オプション）
     * @param pageable    ページング情報
     * @return マスタデータのページ
     */
    Page<MasterDataDto> getMasterDataList(String typeCode, String search, String status, Pageable pageable);

    /**
     * マスタデータを取得
     * Get master data by code
     *
     * @param typeCode タイプコード
     * @param code     データコード
     * @return マスタデータ
     */
    MasterDataDto getMasterDataByCode(String typeCode, String code);

    /**
     * マスタデータを作成
     * Create master data
     *
     * @param typeCode     タイプコード
     * @param masterDataDto マスタデータ情報
     * @return 作成されたマスタデータ
     */
    MasterDataDto createMasterData(String typeCode, MasterDataDto masterDataDto);

    /**
     * マスタデータを更新
     * Update master data
     *
     * @param typeCode     タイプコード
     * @param code         データコード
     * @param masterDataDto 更新情報
     * @return 更新されたマスタデータ
     */
    MasterDataDto updateMasterData(String typeCode, String code, MasterDataDto masterDataDto);

    /**
     * マスタデータを削除
     * Delete master data
     *
     * @param typeCode タイプコード
     * @param code     データコード
     */
    void deleteMasterData(String typeCode, String code);

    /**
     * 属性値をまとめて更新
     * Update multiple attributes
     *
     * @param typeCode タイプコード
     * @param code     データコード
     * @param attributes 属性値のリスト
     * @return 更新されたマスタデータ
     */
    MasterDataDto updateAttributes(String typeCode, String code, List<MasterDataAttributeDto> attributes);

    /**
     * マスタデータ一括インポート
     * Bulk import master data
     *
     * @param typeCode タイプコード
     * @param importRequest インポートリクエスト
     * @return インポート結果
     */
    MasterDataImportResponseDto importMasterData(String typeCode, MasterDataImportRequestDto importRequest);

    /**
     * マスタデータエクスポート
     * Export master data
     *
     * @param typeCode タイプコード
     * @param format   フォーマット（csv/excel）
     * @param status   ステータス（オプション）
     * @return エクスポートレスポンス
     */
    MasterDataExportResponseDto exportMasterData(String typeCode, String format, String status);
}