package com.ses_mgr.admin.controller.api;

import com.ses_mgr.admin.dto.*;
import com.ses_mgr.admin.service.MasterDataManagementService;
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
 * マスタデータ管理RESTコントローラ
 * Master data management REST controller
 */
@RestController
@RequestMapping("/api/v1/admin/master")
public class MasterDataManagementRestController {

    private static final Logger logger = LoggerFactory.getLogger(MasterDataManagementRestController.class);

    private final MasterDataManagementService masterDataManagementService;

    @Autowired
    public MasterDataManagementRestController(MasterDataManagementService masterDataManagementService) {
        this.masterDataManagementService = masterDataManagementService;
    }

    // Master Type Management

    /**
     * マスタデータタイプ一覧を取得
     * Get all master data types
     */
    @GetMapping("/types")
    @PreAuthorize("hasAuthority('system.master.read')")
    public ResponseEntity<ApiResponseDto<Page<MasterTypeDto>>> getMasterTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "displayOrder,asc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<MasterTypeDto> typesPage = masterDataManagementService.getMasterTypes(pageable);

        return ResponseEntity.ok(new ApiResponseDto<>(typesPage, "マスタデータタイプのリストを取得しました"));
    }

    /**
     * アクティブなマスタデータタイプのリストを取得
     * Get all active master data types as a list
     */
    @GetMapping("/types/active")
    @PreAuthorize("hasAuthority('system.master.read')")
    public ResponseEntity<ApiResponseDto<List<MasterTypeDto>>> getActiveMasterTypes() {
        List<MasterTypeDto> types = masterDataManagementService.getActiveMasterTypes();
        return ResponseEntity.ok(new ApiResponseDto<>(types, "アクティブなマスタデータタイプのリストを取得しました"));
    }

    /**
     * マスタデータタイプを取得
     * Get a specific master data type
     */
    @GetMapping("/types/{typeCode}")
    @PreAuthorize("hasAuthority('system.master.read')")
    public ResponseEntity<ApiResponseDto<MasterTypeDto>> getMasterType(
            @PathVariable String typeCode) {

        MasterTypeDto typeDto = masterDataManagementService.getMasterTypeByCode(typeCode);
        return ResponseEntity.ok(new ApiResponseDto<>(typeDto, "マスタデータタイプを取得しました"));
    }

    /**
     * マスタデータタイプを作成
     * Create a new master data type
     */
    @PostMapping("/types")
    @PreAuthorize("hasAuthority('system.master.admin')")
    public ResponseEntity<ApiResponseDto<MasterTypeDto>> createMasterType(
            @Valid @RequestBody MasterTypeCreateRequestDto requestDto,
            Authentication authentication) {

        // ユーザーIDを取得（実際の実装はJWTトークンやユーザー認証情報から取得する）
        Long userId = getUserIdFromAuthentication(authentication);

        MasterTypeDto createdType = masterDataManagementService.createMasterType(requestDto, userId);
        return new ResponseEntity<>(
                new ApiResponseDto<>(createdType, "マスタデータタイプを作成しました"),
                HttpStatus.CREATED);
    }

    /**
     * マスタデータタイプを更新
     * Update a master data type
     */
    @PutMapping("/types/{typeCode}")
    @PreAuthorize("hasAuthority('system.master.admin')")
    public ResponseEntity<ApiResponseDto<MasterTypeDto>> updateMasterType(
            @PathVariable String typeCode,
            @Valid @RequestBody MasterTypeUpdateRequestDto requestDto,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);

        MasterTypeDto updatedType = masterDataManagementService.updateMasterType(typeCode, requestDto, userId);
        return ResponseEntity.ok(new ApiResponseDto<>(updatedType, "マスタデータタイプを更新しました"));
    }

    /**
     * マスタデータタイプを削除
     * Delete a master data type
     */
    @DeleteMapping("/types/{typeCode}")
    @PreAuthorize("hasAuthority('system.master.admin')")
    public ResponseEntity<ApiResponseDto<Void>> deleteMasterType(
            @PathVariable String typeCode) {

        masterDataManagementService.deleteMasterType(typeCode);
        return ResponseEntity.ok(new ApiResponseDto<>(null, "マスタデータタイプを削除しました"));
    }

    // Master Data Management

    /**
     * マスタデータ一覧を取得
     * Get all master data for a specific type
     */
    @GetMapping("/{type}")
    @PreAuthorize("hasAuthority('system.master.read')")
    public ResponseEntity<ApiResponseDto<Page<MasterDataDto>>> getMasterDataList(
            @PathVariable("type") String typeCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "displayOrder,asc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<MasterDataDto> dataPage = masterDataManagementService.getMasterDataList(typeCode, pageable);

        return ResponseEntity.ok(new ApiResponseDto<>(dataPage, "マスタデータのリストを取得しました"));
    }

    /**
     * アクティブなマスタデータのリストを取得
     * Get all active master data as a list
     */
    @GetMapping("/{type}/active")
    @PreAuthorize("hasAuthority('system.master.read')")
    public ResponseEntity<ApiResponseDto<List<MasterDataDto>>> getActiveMasterDataList(
            @PathVariable("type") String typeCode) {

        List<MasterDataDto> dataList = masterDataManagementService.getActiveMasterDataList(typeCode);
        return ResponseEntity.ok(new ApiResponseDto<>(dataList, "アクティブなマスタデータのリストを取得しました"));
    }

    /**
     * マスタデータを検索
     * Search master data
     */
    @GetMapping("/{type}/search")
    @PreAuthorize("hasAuthority('system.master.read')")
    public ResponseEntity<ApiResponseDto<Page<MasterDataDto>>> searchMasterData(
            @PathVariable("type") String typeCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "displayOrder,asc") String sort) {

        MasterDataSearchRequestDto searchRequestDto = new MasterDataSearchRequestDto();
        searchRequestDto.setKeyword(keyword);
        searchRequestDto.setIsActive(isActive);
        searchRequestDto.setParentId(parentId);
        searchRequestDto.setPage(page);
        searchRequestDto.setSize(size);
        searchRequestDto.setSort(sort);

        Page<MasterDataDto> result = masterDataManagementService.searchMasterData(typeCode, searchRequestDto);
        return ResponseEntity.ok(new ApiResponseDto<>(result, "マスタデータの検索結果を取得しました"));
    }

    /**
     * マスタデータを取得
     * Get a specific master data
     */
    @GetMapping("/{type}/{id}")
    @PreAuthorize("hasAuthority('system.master.read')")
    public ResponseEntity<ApiResponseDto<MasterDataDto>> getMasterData(
            @PathVariable("type") String typeCode,
            @PathVariable("id") String code) {

        MasterDataDto dataDto = masterDataManagementService.getMasterData(typeCode, code);
        return ResponseEntity.ok(new ApiResponseDto<>(dataDto, "マスタデータを取得しました"));
    }

    /**
     * マスタデータを作成
     * Create a new master data
     */
    @PostMapping("/{type}")
    @PreAuthorize("hasAuthority('system.master.admin')")
    public ResponseEntity<ApiResponseDto<MasterDataDto>> createMasterData(
            @PathVariable("type") String typeCode,
            @Valid @RequestBody MasterDataCreateRequestDto requestDto,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);

        MasterDataDto createdData = masterDataManagementService.createMasterData(typeCode, requestDto, userId);
        return new ResponseEntity<>(
                new ApiResponseDto<>(createdData, "マスタデータを作成しました"),
                HttpStatus.CREATED);
    }

    /**
     * マスタデータを一括作成
     * Bulk create master data
     */
    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('system.master.admin')")
    public ResponseEntity<ApiResponseDto<List<MasterDataDto>>> bulkCreateMasterData(
            @Valid @RequestBody MasterDataBulkCreateRequestDto requestDto,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);

        List<MasterDataDto> createdData = masterDataManagementService.bulkCreateMasterData(requestDto, userId);
        return new ResponseEntity<>(
                new ApiResponseDto<>(createdData, "マスタデータを一括作成しました（" + createdData.size() + "件）"),
                HttpStatus.CREATED);
    }

    /**
     * マスタデータを更新
     * Update a master data
     */
    @PutMapping("/{type}/{id}")
    @PreAuthorize("hasAuthority('system.master.admin')")
    public ResponseEntity<ApiResponseDto<MasterDataDto>> updateMasterData(
            @PathVariable("type") String typeCode,
            @PathVariable("id") String code,
            @Valid @RequestBody MasterDataUpdateRequestDto requestDto,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);

        MasterDataDto updatedData = masterDataManagementService.updateMasterData(typeCode, code, requestDto, userId);
        return ResponseEntity.ok(new ApiResponseDto<>(updatedData, "マスタデータを更新しました"));
    }

    /**
     * マスタデータを削除
     * Delete a master data
     */
    @DeleteMapping("/{type}/{id}")
    @PreAuthorize("hasAuthority('system.master.admin')")
    public ResponseEntity<ApiResponseDto<Void>> deleteMasterData(
            @PathVariable("type") String typeCode,
            @PathVariable("id") String code) {

        masterDataManagementService.deleteMasterData(typeCode, code);
        return ResponseEntity.ok(new ApiResponseDto<>(null, "マスタデータを削除しました"));
    }

    // Master Data Attribute Management

    /**
     * マスタデータ属性定義一覧を取得
     * Get all attribute definitions for a master data type
     */
    @GetMapping("/{type}/attributes")
    @PreAuthorize("hasAuthority('system.master.read')")
    public ResponseEntity<ApiResponseDto<List<MasterDataAttributeDto>>> getMasterDataAttributes(
            @PathVariable("type") String typeCode) {

        List<MasterDataAttributeDto> attributes = masterDataManagementService.getMasterDataAttributes(typeCode);
        return ResponseEntity.ok(new ApiResponseDto<>(attributes, "マスタデータ属性定義のリストを取得しました"));
    }

    /**
     * マスタデータ属性定義を取得
     * Get a specific attribute definition
     */
    @GetMapping("/{type}/attributes/{attributeName}")
    @PreAuthorize("hasAuthority('system.master.read')")
    public ResponseEntity<ApiResponseDto<MasterDataAttributeDto>> getMasterDataAttribute(
            @PathVariable("type") String typeCode,
            @PathVariable String attributeName) {

        MasterDataAttributeDto attribute = masterDataManagementService.getMasterDataAttribute(typeCode, attributeName);
        return ResponseEntity.ok(new ApiResponseDto<>(attribute, "マスタデータ属性定義を取得しました"));
    }

    /**
     * マスタデータ属性定義を作成
     * Create a new attribute definition
     */
    @PostMapping("/{type}/attributes")
    @PreAuthorize("hasAuthority('system.master.admin')")
    public ResponseEntity<ApiResponseDto<MasterDataAttributeDto>> createMasterDataAttribute(
            @PathVariable("type") String typeCode,
            @Valid @RequestBody MasterDataAttributeCreateRequestDto requestDto,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);

        MasterDataAttributeDto createdAttribute = masterDataManagementService.createMasterDataAttribute(typeCode, requestDto, userId);
        return new ResponseEntity<>(
                new ApiResponseDto<>(createdAttribute, "マスタデータ属性定義を作成しました"),
                HttpStatus.CREATED);
    }

    /**
     * マスタデータ属性定義を更新
     * Update an attribute definition
     */
    @PutMapping("/{type}/attributes/{attributeName}")
    @PreAuthorize("hasAuthority('system.master.admin')")
    public ResponseEntity<ApiResponseDto<MasterDataAttributeDto>> updateMasterDataAttribute(
            @PathVariable("type") String typeCode,
            @PathVariable String attributeName,
            @Valid @RequestBody MasterDataAttributeUpdateRequestDto requestDto,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);

        MasterDataAttributeDto updatedAttribute = masterDataManagementService.updateMasterDataAttribute(
                typeCode, attributeName, requestDto, userId);
        return ResponseEntity.ok(new ApiResponseDto<>(updatedAttribute, "マスタデータ属性定義を更新しました"));
    }

    /**
     * マスタデータ属性定義を削除
     * Delete an attribute definition
     */
    @DeleteMapping("/{type}/attributes/{attributeName}")
    @PreAuthorize("hasAuthority('system.master.admin')")
    public ResponseEntity<ApiResponseDto<Void>> deleteMasterDataAttribute(
            @PathVariable("type") String typeCode,
            @PathVariable String attributeName) {

        masterDataManagementService.deleteMasterDataAttribute(typeCode, attributeName);
        return ResponseEntity.ok(new ApiResponseDto<>(null, "マスタデータ属性定義を削除しました"));
    }

    // Import/Export

    /**
     * マスタデータをインポート
     * Import master data
     */
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('system.master.admin')")
    public ResponseEntity<ApiResponseDto<MasterDataImportResultDto>> importMasterData(
            @Valid @RequestBody MasterDataImportRequestDto requestDto,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);

        MasterDataImportResultDto result = masterDataManagementService.importMasterData(requestDto, userId);
        String message = "マスタデータをインポートしました（成功: " + result.getSuccessCount() +
                "件, 失敗: " + result.getFailureCount() + "件）";
        return ResponseEntity.ok(new ApiResponseDto<>(result, message));
    }

    /**
     * マスタデータをエクスポート
     * Export master data
     */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('system.master.read')")
    public ResponseEntity<ApiResponseDto<MasterDataExportResponseDto>> exportMasterData(
            @RequestParam String typeCode,
            @RequestParam String fileFormat,
            @RequestParam(required = false, defaultValue = "UTF-8") String encoding,
            @RequestParam(required = false, defaultValue = "false") Boolean includeInactive,
            @RequestParam(required = false, defaultValue = "true") Boolean includeAttributes) {

        MasterDataExportRequestDto requestDto = new MasterDataExportRequestDto();
        requestDto.setTypeCode(typeCode);
        requestDto.setFileFormat(fileFormat);
        requestDto.setEncoding(encoding);
        requestDto.setIncludeInactive(includeInactive);
        requestDto.setIncludeAttributes(includeAttributes);

        MasterDataExportResponseDto result = masterDataManagementService.exportMasterData(requestDto);
        return ResponseEntity.ok(new ApiResponseDto<>(result, "マスタデータをエクスポートしました"));
    }

    // Helper methods

    private Pageable createPageable(int page, int size, String sort) {
        Sort.Direction direction = Sort.Direction.ASC;
        String property = "displayOrder";

        if (sort != null && !sort.isEmpty()) {
            String[] sortParams = sort.split(",");
            if (sortParams.length >= 2) {
                property = sortParams[0];
                direction = "desc".equalsIgnoreCase(sortParams[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
            } else {
                property = sortParams[0];
            }
        }

        return PageRequest.of(page, size, Sort.by(direction, property));
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        // 実際の実装ではJWTトークンやユーザー認証情報からユーザーIDを取得する
        // ここでは簡易的に固定値を返す
        // TODO: 認証情報からユーザーIDを取得する実装に置き換える
        return 1L;
    }
}