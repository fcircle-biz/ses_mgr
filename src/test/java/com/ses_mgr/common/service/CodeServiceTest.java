package com.ses_mgr.common.service;

import com.ses_mgr.admin.entity.MasterData;
import com.ses_mgr.admin.entity.MasterType;
import com.ses_mgr.admin.repository.MasterDataRepository;
import com.ses_mgr.admin.repository.MasterTypeRepository;
import com.ses_mgr.common.dto.CodeCategoryDto;
import com.ses_mgr.common.dto.CodeValueDetailDto;
import com.ses_mgr.common.dto.CodeValueDto;
import com.ses_mgr.common.service.impl.CodeServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CodeServiceTest {

    @Mock
    private MasterTypeRepository masterTypeRepository;

    @Mock
    private MasterDataRepository masterDataRepository;

    @InjectMocks
    private CodeServiceImpl codeService;

    private MasterType jobType;
    private MasterData pmMasterData;
    private MasterData seMasterData;
    private LocalDateTime testTime;

    @BeforeEach
    public void setup() {
        testTime = LocalDateTime.now();
        
        // MasterType設定
        jobType = new MasterType();
        jobType.setId(1L);
        jobType.setTypeCode("job_type");
        jobType.setTypeNameJa("職種");
        jobType.setTypeNameEn("Job Type");
        jobType.setDescription("技術者の職種区分");
        jobType.setDisplayOrder(1);
        jobType.setIsActive(true);
        jobType.setCreatedBy(1L);
        jobType.setCreatedAt(testTime);
        jobType.setUpdatedBy(1L);
        jobType.setUpdatedAt(testTime);

        // PM用MasterData設定
        pmMasterData = new MasterData();
        pmMasterData.setId(1L);
        pmMasterData.setMasterType(jobType);
        pmMasterData.setCode("PM");
        pmMasterData.setNameJa("プロジェクトマネージャ");
        pmMasterData.setNameEn("Project Manager");
        pmMasterData.setDescription("プロジェクト全体の管理と調整を担当する職種");
        pmMasterData.setDisplayOrder(1);
        pmMasterData.setIsActive(true);
        pmMasterData.setCreatedBy(1L);
        pmMasterData.setCreatedAt(testTime);
        pmMasterData.setUpdatedBy(1L);
        pmMasterData.setUpdatedAt(testTime);
        
        Map<String, Object> pmAttributes = new HashMap<>();
        pmAttributes.put("abbreviation", "PM");
        pmAttributes.put("skill_level", "senior");
        pmMasterData.setAttributes(pmAttributes);

        // SE用MasterData設定
        seMasterData = new MasterData();
        seMasterData.setId(2L);
        seMasterData.setMasterType(jobType);
        seMasterData.setCode("SE");
        seMasterData.setNameJa("システムエンジニア");
        seMasterData.setNameEn("System Engineer");
        seMasterData.setDescription("システム設計や開発を担当する職種");
        seMasterData.setDisplayOrder(2);
        seMasterData.setIsActive(true);
        seMasterData.setParentId(1L); // PMの子として設定
        seMasterData.setCreatedBy(1L);
        seMasterData.setCreatedAt(testTime);
        seMasterData.setUpdatedBy(1L);
        seMasterData.setUpdatedAt(testTime);
        
        Map<String, Object> seAttributes = new HashMap<>();
        seAttributes.put("abbreviation", "SE");
        seAttributes.put("skill_level", "middle");
        seMasterData.setAttributes(seAttributes);
    }

    @Test
    public void getAllCategories_ShouldReturnCategoriesList() {
        // Arrange
        List<MasterType> masterTypes = Arrays.asList(jobType);
        when(masterTypeRepository.findAllActiveOrderByDisplayOrder()).thenReturn(masterTypes);
        when(masterDataRepository.countByTypeCode(anyString())).thenReturn(5L);

        // Act
        List<CodeCategoryDto> result = codeService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("job_type", result.get(0).getId());
        assertEquals("職種", result.get(0).getName());
        assertEquals("技術者の職種区分", result.get(0).getDescription());
        assertEquals(5, result.get(0).getCount());
    }

    @Test
    public void getCodesByCategory_ShouldReturnCodesList() {
        // Arrange
        List<MasterData> masterDataList = Arrays.asList(pmMasterData, seMasterData);
        when(masterTypeRepository.findByTypeCode("job_type")).thenReturn(Optional.of(jobType));
        when(masterDataRepository.findAllActiveByMasterTypeCodeOrderByDisplayOrder("job_type")).thenReturn(masterDataList);

        // Act
        List<CodeValueDto> result = codeService.getCodesByCategory("job_type", null, null, true);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("PM", result.get(0).getCode());
        assertEquals("プロジェクトマネージャ", result.get(0).getName());
        assertEquals(1, result.get(0).getSortOrder());
        assertTrue(result.get(0).getIsActive());
        assertEquals("PM", result.get(0).getAttributes().get("abbreviation"));
        assertEquals("senior", result.get(0).getAttributes().get("skill_level"));
    }

    @Test
    public void getCodesByCategory_WithKeyword_ShouldReturnFilteredCodes() {
        // Arrange
        List<MasterData> masterDataList = Arrays.asList(pmMasterData);
        when(masterTypeRepository.findByTypeCode("job_type")).thenReturn(Optional.of(jobType));
        when(masterDataRepository.searchByKeyword(eq("job_type"), eq("PM"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(masterDataList));

        // Act
        List<CodeValueDto> result = codeService.getCodesByCategory("job_type", "PM", null, true);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PM", result.get(0).getCode());
    }

    @Test
    public void getCodesByCategory_WithParent_ShouldReturnChildCodes() {
        // Arrange
        List<MasterData> childDataList = Arrays.asList(seMasterData);
        when(masterTypeRepository.findByTypeCode("job_type")).thenReturn(Optional.of(jobType));
        when(masterDataRepository.findByTypeCodeAndCode("job_type", "PM")).thenReturn(Optional.of(pmMasterData));
        when(masterDataRepository.findByParentIdAndIsActiveTrue(1L)).thenReturn(childDataList);

        // Act
        List<CodeValueDto> result = codeService.getCodesByCategory("job_type", null, "PM", true);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SE", result.get(0).getCode());
    }

    @Test
    public void getCodesByCategory_WithInvalidCategory_ShouldThrowException() {
        // Arrange
        when(masterTypeRepository.findByTypeCode("invalid_category")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            codeService.getCodesByCategory("invalid_category", null, null, true);
        });
    }

    @Test
    public void getCategoryInfo_ShouldReturnCategoryInfo() {
        // Arrange
        when(masterTypeRepository.findByTypeCode("job_type")).thenReturn(Optional.of(jobType));
        when(masterDataRepository.countByTypeCode("job_type")).thenReturn(5L);

        // Act
        CodeCategoryDto result = codeService.getCategoryInfo("job_type");

        // Assert
        assertNotNull(result);
        assertEquals("job_type", result.getId());
        assertEquals("職種", result.getName());
        assertEquals("技術者の職種区分", result.getDescription());
        assertEquals(5, result.getCount());
    }

    @Test
    public void getCategoryInfo_WithInvalidCategory_ShouldThrowException() {
        // Arrange
        when(masterTypeRepository.findByTypeCode("invalid_category")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            codeService.getCategoryInfo("invalid_category");
        });
    }

    @Test
    public void getCodeDetail_ShouldReturnCodeDetail() {
        // Arrange
        when(masterTypeRepository.findByTypeCode("job_type")).thenReturn(Optional.of(jobType));
        when(masterDataRepository.findByTypeCodeAndCode("job_type", "PM")).thenReturn(Optional.of(pmMasterData));
        // 親IDを参照して取得する実装は使われていないため、このスタブは必要ない
        // when(masterDataRepository.findById(pmMasterData.getId())).thenReturn(Optional.of(pmMasterData));
        when(masterDataRepository.findByParentIdAndIsActiveTrue(pmMasterData.getId())).thenReturn(Arrays.asList(seMasterData));

        // Act
        CodeValueDetailDto result = codeService.getCodeDetail("job_type", "PM");

        // Assert
        assertNotNull(result);
        assertEquals("PM", result.getCode());
        assertEquals("プロジェクトマネージャ", result.getName());
        assertEquals("プロジェクト全体の管理と調整を担当する職種", result.getDescription());
        assertEquals(1, result.getSortOrder());
        assertTrue(result.getIsActive());
        assertEquals("job_type", result.getCategory().getId());
        assertEquals("職種", result.getCategory().getName());
        assertEquals(testTime, result.getCreatedAt());
        assertEquals(testTime, result.getUpdatedAt());
        assertEquals(1, result.getChildCodes().size());
        assertEquals("SE", result.getChildCodes().get(0));
    }

    @Test
    public void getCodeDetail_WithInvalidCategoryOrCode_ShouldThrowException() {
        // Arrange
        when(masterTypeRepository.findByTypeCode("job_type")).thenReturn(Optional.of(jobType));
        when(masterDataRepository.findByTypeCodeAndCode("job_type", "invalid_code")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            codeService.getCodeDetail("job_type", "invalid_code");
        });
    }
}