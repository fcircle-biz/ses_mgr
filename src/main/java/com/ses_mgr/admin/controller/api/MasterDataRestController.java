package com.ses_mgr.admin.controller.api;

import com.ses_mgr.admin.dto.*;
import com.ses_mgr.admin.service.MasterDataService;
import com.ses_mgr.common.dto.ApiResponseDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * マスタデータ管理REST APIコントローラ
 * Master data REST API controller
 */
@RestController
@RequestMapping("/api/v1/admin/master-data")
public class MasterDataRestController {

    private static final Logger logger = LoggerFactory.getLogger(MasterDataRestController.class);
    
    private final MasterDataService masterDataService;
    
    @Autowired
    public MasterDataRestController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }
    
    // Master Type endpoints
    
    /**
     * マスタデータタイプ一覧を取得
     * Get all master data types
     *
     * @return マスタデータタイプのリスト
     */
    @GetMapping("/types")
    @PreAuthorize("hasAuthority('master_data.read')")
    public ResponseEntity<ApiResponseDto<List<MasterTypeDto>>> getAllMasterTypes() {
        List<MasterTypeDto> masterTypes = masterDataService.getAllMasterTypes();
        return ResponseEntity.ok(new ApiResponseDto<>(masterTypes));
    }
    
    /**
     * マスタデータタイプを取得
     * Get master data type by code
     *
     * @param code タイプコード
     * @return マスタデータタイプ
     */
    @GetMapping("/types/{code}")
    @PreAuthorize("hasAuthority('master_data.read')")
    public ResponseEntity<ApiResponseDto<MasterTypeDto>> getMasterTypeByCode(@PathVariable String code) {
        MasterTypeDto masterType = masterDataService.getMasterTypeByCode(code);
        return ResponseEntity.ok(new ApiResponseDto<>(masterType));
    }
    
    /**
     * マスタデータタイプを作成
     * Create master data type
     *
     * @param masterTypeDto マスタデータタイプ情報
     * @return 作成されたマスタデータタイプ
     */
    @PostMapping("/types")
    @PreAuthorize("hasAuthority('master_data.create')")
    public ResponseEntity<ApiResponseDto<MasterTypeDto>> createMasterType(
            @Valid @RequestBody MasterTypeDto masterTypeDto) {
        MasterTypeDto createdType = masterDataService.createMasterType(masterTypeDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>(createdType));
    }
    
    /**
     * マスタデータタイプを更新
     * Update master data type
     *
     * @param code タイプコード
     * @param masterTypeDto 更新情報
     * @return 更新されたマスタデータタイプ
     */
    @PutMapping("/types/{code}")
    @PreAuthorize("hasAuthority('master_data.update')")
    public ResponseEntity<ApiResponseDto<MasterTypeDto>> updateMasterType(
            @PathVariable String code,
            @Valid @RequestBody MasterTypeDto masterTypeDto) {
        MasterTypeDto updatedType = masterDataService.updateMasterType(code, masterTypeDto);
        return ResponseEntity.ok(new ApiResponseDto<>(updatedType));
    }
    
    /**
     * マスタデータタイプを削除
     * Delete master data type
     *
     * @param code タイプコード
     * @return 削除結果
     */
    @DeleteMapping("/types/{code}")
    @PreAuthorize("hasAuthority('master_data.delete')")
    public ResponseEntity<ApiResponseDto<Void>> deleteMasterType(@PathVariable String code) {
        masterDataService.deleteMasterType(code);
        return ResponseEntity.ok(new ApiResponseDto<>(null, "Master data type deleted successfully"));
    }
    
    // Master Data endpoints
    
    /**
     * マスタデータ一覧を取得
     * Get master data list
     *
     * @param typeCode タイプコード
     * @param search 検索キーワード
     * @param status ステータス
     * @param page ページ番号
     * @param size ページサイズ
     * @param sort ソート条件
     * @return マスタデータリスト
     */
    @GetMapping("/{typeCode}")
    @PreAuthorize("hasAuthority('master_data.read')")
    public ResponseEntity<ApiResponseDto<Page<MasterDataDto>>> getMasterDataList(
            @PathVariable String typeCode,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder,asc") String sort) {
        
        Sort.Direction direction = sort.endsWith(",desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        String property = sort.split(",")[0];
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));
        Page<MasterDataDto> masterDataPage = masterDataService.getMasterDataList(typeCode, search, status, pageable);
        
        return ResponseEntity.ok(new ApiResponseDto<>(masterDataPage));
    }
    
    /**
     * マスタデータを取得
     * Get master data by code
     *
     * @param typeCode タイプコード
     * @param code データコード
     * @return マスタデータ
     */
    @GetMapping("/{typeCode}/{code}")
    @PreAuthorize("hasAuthority('master_data.read')")
    public ResponseEntity<ApiResponseDto<MasterDataDto>> getMasterDataByCode(
            @PathVariable String typeCode,
            @PathVariable String code) {
        MasterDataDto masterData = masterDataService.getMasterDataByCode(typeCode, code);
        return ResponseEntity.ok(new ApiResponseDto<>(masterData));
    }
    
    /**
     * マスタデータを作成
     * Create master data
     *
     * @param typeCode タイプコード
     * @param masterDataDto マスタデータ情報
     * @return 作成されたマスタデータ
     */
    @PostMapping("/{typeCode}")
    @PreAuthorize("hasAuthority('master_data.create')")
    public ResponseEntity<ApiResponseDto<MasterDataDto>> createMasterData(
            @PathVariable String typeCode,
            @Valid @RequestBody MasterDataDto masterDataDto) {
        MasterDataDto createdData = masterDataService.createMasterData(typeCode, masterDataDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>(createdData));
    }
    
    /**
     * マスタデータを更新
     * Update master data
     *
     * @param typeCode タイプコード
     * @param code データコード
     * @param masterDataDto 更新情報
     * @return 更新されたマスタデータ
     */
    @PutMapping("/{typeCode}/{code}")
    @PreAuthorize("hasAuthority('master_data.update')")
    public ResponseEntity<ApiResponseDto<MasterDataDto>> updateMasterData(
            @PathVariable String typeCode,
            @PathVariable String code,
            @Valid @RequestBody MasterDataDto masterDataDto) {
        MasterDataDto updatedData = masterDataService.updateMasterData(typeCode, code, masterDataDto);
        return ResponseEntity.ok(new ApiResponseDto<>(updatedData));
    }
    
    /**
     * マスタデータを削除
     * Delete master data
     *
     * @param typeCode タイプコード
     * @param code データコード
     * @return 削除結果
     */
    @DeleteMapping("/{typeCode}/{code}")
    @PreAuthorize("hasAuthority('master_data.delete')")
    public ResponseEntity<ApiResponseDto<Void>> deleteMasterData(
            @PathVariable String typeCode,
            @PathVariable String code) {
        masterDataService.deleteMasterData(typeCode, code);
        return ResponseEntity.ok(new ApiResponseDto<>(null, "Master data deleted successfully"));
    }
    
    /**
     * 属性値をまとめて更新
     * Update multiple attributes
     *
     * @param typeCode タイプコード
     * @param code データコード
     * @param attributes 属性値のリスト
     * @return 更新されたマスタデータ
     */
    @PutMapping("/{typeCode}/{code}/attributes")
    @PreAuthorize("hasAuthority('master_data.update')")
    public ResponseEntity<ApiResponseDto<MasterDataDto>> updateAttributes(
            @PathVariable String typeCode,
            @PathVariable String code,
            @Valid @RequestBody List<MasterDataAttributeDto> attributes) {
        MasterDataDto updatedData = masterDataService.updateAttributes(typeCode, code, attributes);
        return ResponseEntity.ok(new ApiResponseDto<>(updatedData));
    }
    
    /**
     * マスタデータ一括インポート
     * Bulk import master data
     *
     * @param typeCode タイプコード
     * @param importRequest インポートリクエスト
     * @return インポート結果
     */
    @PostMapping("/{typeCode}/import")
    @PreAuthorize("hasAuthority('master_data.import')")
    public ResponseEntity<ApiResponseDto<MasterDataImportResponseDto>> importMasterData(
            @PathVariable String typeCode,
            @Valid @RequestBody MasterDataImportRequestDto importRequest) {
        MasterDataImportResponseDto importResult = masterDataService.importMasterData(typeCode, importRequest);
        return ResponseEntity.ok(new ApiResponseDto<>(importResult));
    }
    
    /**
     * マスタデータエクスポート
     * Export master data
     *
     * @param typeCode タイプコード
     * @param format フォーマット（csv/excel）
     * @param status ステータス（オプション）
     * @return エクスポートレスポンス
     */
    @GetMapping("/{typeCode}/export")
    @PreAuthorize("hasAuthority('master_data.export')")
    public ResponseEntity<ApiResponseDto<MasterDataExportResponseDto>> exportMasterData(
            @PathVariable String typeCode,
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String status) {
        MasterDataExportResponseDto exportResult = masterDataService.exportMasterData(typeCode, format, status);
        return ResponseEntity.ok(new ApiResponseDto<>(exportResult));
    }
}