package com.ses_mgr.admin.service;

import com.ses_mgr.admin.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * マスタデータ管理サービスインターフェース
 * Master data management service interface
 */
public interface MasterDataManagementService {

    // Master Type Management

    /**
     * マスタデータタイプ一覧を取得
     * Get list of master data types
     *
     * @param pageable ページング情報
     * @return マスタデータタイプのページ
     */
    Page<MasterTypeDto> getMasterTypes(Pageable pageable);

    /**
     * アクティブなマスタデータタイプ一覧を取得
     * Get list of active master data types
     *
     * @return マスタデータタイプのリスト
     */
    List<MasterTypeDto> getActiveMasterTypes();

    /**
     * IDでマスタデータタイプを取得
     * Get master data type by ID
     *
     * @param id マスタデータタイプID
     * @return マスタデータタイプ
     */
    MasterTypeDto getMasterTypeById(Long id);

    /**
     * タイプコードでマスタデータタイプを取得
     * Get master data type by type code
     *
     * @param typeCode タイプコード
     * @return マスタデータタイプ
     */
    MasterTypeDto getMasterTypeByCode(String typeCode);

    /**
     * マスタデータタイプを作成
     * Create master data type
     *
     * @param requestDto マスタデータタイプ作成リクエスト
     * @param userId     ユーザーID
     * @return 作成されたマスタデータタイプ
     */
    MasterTypeDto createMasterType(MasterTypeCreateRequestDto requestDto, Long userId);

    /**
     * マスタデータタイプを更新
     * Update master data type
     *
     * @param typeCode   タイプコード
     * @param requestDto マスタデータタイプ更新リクエスト
     * @param userId     ユーザーID
     * @return 更新されたマスタデータタイプ
     */
    MasterTypeDto updateMasterType(String typeCode, MasterTypeUpdateRequestDto requestDto, Long userId);

    /**
     * マスタデータタイプを削除
     * Delete master data type
     *
     * @param typeCode タイプコード
     */
    void deleteMasterType(String typeCode);

    // Master Data Management

    /**
     * マスタデータ一覧を取得
     * Get master data list by type code
     *
     * @param typeCode タイプコード
     * @param pageable ページング情報
     * @return マスタデータのページ
     */
    Page<MasterDataDto> getMasterDataList(String typeCode, Pageable pageable);

    /**
     * アクティブなマスタデータ一覧を取得
     * Get active master data list by type code
     *
     * @param typeCode タイプコード
     * @return マスタデータのリスト
     */
    List<MasterDataDto> getActiveMasterDataList(String typeCode);

    /**
     * マスタデータを検索
     * Search master data
     *
     * @param typeCode   タイプコード
     * @param requestDto 検索リクエスト
     * @return マスタデータのページ
     */
    Page<MasterDataDto> searchMasterData(String typeCode, MasterDataSearchRequestDto requestDto);

    /**
     * マスタデータを取得
     * Get master data by type code and data code
     *
     * @param typeCode タイプコード
     * @param code     マスタデータコード
     * @return マスタデータ
     */
    MasterDataDto getMasterData(String typeCode, String code);

    /**
     * マスタデータを作成
     * Create master data
     *
     * @param typeCode   タイプコード
     * @param requestDto マスタデータ作成リクエスト
     * @param userId     ユーザーID
     * @return 作成されたマスタデータ
     */
    MasterDataDto createMasterData(String typeCode, MasterDataCreateRequestDto requestDto, Long userId);

    /**
     * マスタデータを一括作成
     * Bulk create master data
     *
     * @param requestDto マスタデータ一括作成リクエスト
     * @param userId     ユーザーID
     * @return 作成されたマスタデータのリスト
     */
    List<MasterDataDto> bulkCreateMasterData(MasterDataBulkCreateRequestDto requestDto, Long userId);

    /**
     * マスタデータを更新
     * Update master data
     *
     * @param typeCode   タイプコード
     * @param code       マスタデータコード
     * @param requestDto マスタデータ更新リクエスト
     * @param userId     ユーザーID
     * @return 更新されたマスタデータ
     */
    MasterDataDto updateMasterData(String typeCode, String code, MasterDataUpdateRequestDto requestDto, Long userId);

    /**
     * マスタデータを削除
     * Delete master data
     *
     * @param typeCode タイプコード
     * @param code     マスタデータコード
     */
    void deleteMasterData(String typeCode, String code);

    // Master Data Attribute Management

    /**
     * マスタデータ属性定義一覧を取得
     * Get master data attribute definitions
     *
     * @param typeCode タイプコード
     * @return 属性定義のリスト
     */
    List<MasterDataAttributeDto> getMasterDataAttributes(String typeCode);

    /**
     * マスタデータ属性定義を取得
     * Get master data attribute definition
     *
     * @param typeCode      タイプコード
     * @param attributeName 属性名
     * @return 属性定義
     */
    MasterDataAttributeDto getMasterDataAttribute(String typeCode, String attributeName);

    /**
     * マスタデータ属性定義を作成
     * Create master data attribute definition
     *
     * @param typeCode   タイプコード
     * @param requestDto 属性定義作成リクエスト
     * @param userId     ユーザーID
     * @return 作成された属性定義
     */
    MasterDataAttributeDto createMasterDataAttribute(String typeCode, MasterDataAttributeCreateRequestDto requestDto, Long userId);

    /**
     * マスタデータ属性定義を更新
     * Update master data attribute definition
     *
     * @param typeCode      タイプコード
     * @param attributeName 属性名
     * @param requestDto    属性定義更新リクエスト
     * @param userId        ユーザーID
     * @return 更新された属性定義
     */
    MasterDataAttributeDto updateMasterDataAttribute(String typeCode, String attributeName, MasterDataAttributeUpdateRequestDto requestDto, Long userId);

    /**
     * マスタデータ属性定義を削除
     * Delete master data attribute definition
     *
     * @param typeCode      タイプコード
     * @param attributeName 属性名
     */
    void deleteMasterDataAttribute(String typeCode, String attributeName);

    // Import/Export

    /**
     * マスタデータをインポート
     * Import master data
     *
     * @param requestDto インポートリクエスト
     * @param userId     ユーザーID
     * @return インポート結果
     */
    MasterDataImportResultDto importMasterData(MasterDataImportRequestDto requestDto, Long userId);

    /**
     * マスタデータをエクスポート
     * Export master data
     *
     * @param requestDto エクスポートリクエスト
     * @return エクスポート結果
     */
    MasterDataExportResponseDto exportMasterData(MasterDataExportRequestDto requestDto);
}