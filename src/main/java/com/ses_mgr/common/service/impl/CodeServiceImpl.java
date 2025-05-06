package com.ses_mgr.common.service.impl;

import com.ses_mgr.admin.entity.MasterData;
import com.ses_mgr.admin.entity.MasterType;
import com.ses_mgr.admin.repository.MasterDataRepository;
import com.ses_mgr.admin.repository.MasterTypeRepository;
import com.ses_mgr.common.dto.CodeCategoryDto;
import com.ses_mgr.common.dto.CodeValueDetailDto;
import com.ses_mgr.common.dto.CodeValueDto;
import com.ses_mgr.common.service.CodeService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * コード値サービス実装
 * Code service implementation
 */
@Service
@Transactional(readOnly = true)
public class CodeServiceImpl implements CodeService {

    private final MasterTypeRepository masterTypeRepository;
    private final MasterDataRepository masterDataRepository;

    @Autowired
    public CodeServiceImpl(
            MasterTypeRepository masterTypeRepository,
            MasterDataRepository masterDataRepository) {
        this.masterTypeRepository = masterTypeRepository;
        this.masterDataRepository = masterDataRepository;
    }

    @Override
    @Cacheable(value = "codeCategories")
    public List<CodeCategoryDto> getAllCategories() {
        List<MasterType> masterTypes = masterTypeRepository.findAllActiveOrderByDisplayOrder();
        
        return masterTypes.stream()
                .map(this::convertToCodeCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "codeValues", key = "#category + '-' + #keyword + '-' + #parent + '-' + #activeOnly")
    public List<CodeValueDto> getCodesByCategory(String category, String keyword, String parent, boolean activeOnly) {
        // カテゴリが存在するか確認
        MasterType masterType = masterTypeRepository.findByTypeCode(category)
                .orElseThrow(() -> new EntityNotFoundException("指定されたカテゴリが存在しません。"));
        
        List<MasterData> masterDataList;
        
        // 検索条件に応じて取得方法を分岐
        if (StringUtils.hasText(keyword)) {
            // キーワード検索の場合
            Page<MasterData> page = masterDataRepository.searchByKeyword(
                    category, 
                    keyword, 
                    PageRequest.of(0, 1000, Sort.by(Sort.Direction.ASC, "displayOrder")));
            masterDataList = page.getContent();
        } else if (StringUtils.hasText(parent)) {
            // 親コード指定の場合
            Optional<MasterData> parentData = masterDataRepository.findByTypeCodeAndCode(category, parent);
            if (parentData.isPresent()) {
                masterDataList = masterDataRepository.findByParentIdAndIsActiveTrue(parentData.get().getId());
            } else {
                masterDataList = new ArrayList<>();
            }
        } else {
            // 通常検索の場合
            if (activeOnly) {
                masterDataList = masterDataRepository.findAllActiveByMasterTypeCodeOrderByDisplayOrder(category);
            } else {
                masterDataList = masterDataRepository.findByTypeCodeOrderByDisplayOrderAsc(category);
            }
        }
        
        return masterDataList.stream()
                .filter(md -> !activeOnly || md.getIsActive())
                .map(this::convertToCodeValueDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "categoryInfo", key = "#category")
    public CodeCategoryDto getCategoryInfo(String category) {
        MasterType masterType = masterTypeRepository.findByTypeCode(category)
                .orElseThrow(() -> new EntityNotFoundException("指定されたカテゴリが存在しません。"));
        
        return convertToCodeCategoryDto(masterType);
    }

    @Override
    @Cacheable(value = "codeDetail", key = "#category + '-' + #code")
    public CodeValueDetailDto getCodeDetail(String category, String code) {
        // カテゴリが存在するか確認
        MasterType masterType = masterTypeRepository.findByTypeCode(category)
                .orElseThrow(() -> new EntityNotFoundException("指定されたカテゴリが存在しません。"));
        
        // コード値が存在するか確認
        MasterData masterData = masterDataRepository.findByTypeCodeAndCode(category, code)
                .orElseThrow(() -> new EntityNotFoundException("指定されたカテゴリまたはコード値が存在しません。"));
        
        return convertToCodeValueDetailDto(masterType, masterData);
    }

    /**
     * MasterTypeをCodeCategoryDtoに変換
     * Convert MasterType to CodeCategoryDto
     */
    private CodeCategoryDto convertToCodeCategoryDto(MasterType masterType) {
        long count = masterDataRepository.countByTypeCode(masterType.getTypeCode());
        
        return CodeCategoryDto.builder()
                .id(masterType.getTypeCode())
                .name(masterType.getTypeNameJa())
                .description(masterType.getDescription())
                .count((int) count)
                .build();
    }

    /**
     * MasterDataをCodeValueDtoに変換
     * Convert MasterData to CodeValueDto
     */
    private CodeValueDto convertToCodeValueDto(MasterData masterData) {
        return CodeValueDto.builder()
                .code(masterData.getCode())
                .name(masterData.getNameJa())
                .sortOrder(masterData.getDisplayOrder())
                .isActive(masterData.getIsActive())
                .attributes(masterData.getAttributes())
                .build();
    }

    /**
     * MasterDataをCodeValueDetailDtoに変換
     * Convert MasterData to CodeValueDetailDto
     */
    private CodeValueDetailDto convertToCodeValueDetailDto(MasterType masterType, MasterData masterData) {
        // カテゴリ情報の作成
        CodeCategoryDto categoryDto = CodeCategoryDto.builder()
                .id(masterType.getTypeCode())
                .name(masterType.getTypeNameJa())
                .description(masterType.getDescription())
                .build();
        
        // 親コードの取得
        String parentCode = null;
        if (masterData.getParentId() != null) {
            Optional<MasterData> parentData = masterDataRepository.findById(masterData.getParentId());
            if (parentData.isPresent()) {
                parentCode = parentData.get().getCode();
            }
        }
        
        // 子コードリストの取得
        List<String> childCodes = masterDataRepository.findByParentIdAndIsActiveTrue(masterData.getId())
                .stream()
                .map(MasterData::getCode)
                .collect(Collectors.toList());
        
        return CodeValueDetailDto.builder()
                .category(categoryDto)
                .code(masterData.getCode())
                .name(masterData.getNameJa())
                .description(masterData.getDescription())
                .sortOrder(masterData.getDisplayOrder())
                .isActive(masterData.getIsActive())
                .attributes(masterData.getAttributes())
                .parentCode(parentCode)
                .childCodes(childCodes)
                .createdAt(masterData.getCreatedAt())
                .updatedAt(masterData.getUpdatedAt())
                .build();
    }
}