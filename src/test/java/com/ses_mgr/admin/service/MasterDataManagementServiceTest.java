package com.ses_mgr.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.admin.dto.*;
import com.ses_mgr.admin.entity.MasterData;
import com.ses_mgr.admin.entity.MasterDataAttribute;
import com.ses_mgr.admin.entity.MasterType;
import com.ses_mgr.admin.repository.MasterDataAttributeRepository;
import com.ses_mgr.admin.repository.MasterDataRepository;
import com.ses_mgr.admin.repository.MasterTypeRepository;
import com.ses_mgr.admin.service.impl.MasterDataManagementServiceImpl;
import com.ses_mgr.common.exception.ResourceAlreadyExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MasterDataManagementServiceTest {

    @Mock
    private MasterTypeRepository masterTypeRepository;

    @Mock
    private MasterDataRepository masterDataRepository;

    @Mock
    private MasterDataAttributeRepository attributeRepository;

    private ObjectMapper objectMapper;
    private MasterDataManagementService masterDataManagementService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        masterDataManagementService = new MasterDataManagementServiceImpl(
                masterTypeRepository, masterDataRepository, attributeRepository, objectMapper);
    }

    // Master Type Tests

    @Test
    void getMasterTypeByCode_WhenTypeExists_ShouldReturnType() {
        // Arrange
        String typeCode = "department";
        MasterType masterType = createSampleMasterType(1L, typeCode);
        when(masterTypeRepository.findByTypeCode(typeCode)).thenReturn(Optional.of(masterType));

        // Act
        MasterTypeDto result = masterDataManagementService.getMasterTypeByCode(typeCode);

        // Assert
        assertNotNull(result);
        assertEquals(typeCode, result.getTypeCode());
        assertEquals(masterType.getTypeNameJa(), result.getTypeNameJa());
    }

    @Test
    void getMasterTypeByCode_WhenTypeDoesNotExist_ShouldThrowException() {
        // Arrange
        String typeCode = "nonexistent";
        when(masterTypeRepository.findByTypeCode(typeCode)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> 
                masterDataManagementService.getMasterTypeByCode(typeCode));
    }

    @Test
    void createMasterType_WithValidData_ShouldCreateType() {
        // Arrange
        MasterTypeCreateRequestDto requestDto = new MasterTypeCreateRequestDto();
        requestDto.setTypeCode("newtype");
        requestDto.setTypeNameJa("新しいタイプ");
        requestDto.setTypeNameEn("New Type");
        requestDto.setDescription("テスト用の新しいタイプ");
        requestDto.setDisplayOrder(10);

        when(masterTypeRepository.existsByTypeCode(requestDto.getTypeCode())).thenReturn(false);
        when(masterTypeRepository.save(any(MasterType.class))).thenAnswer(invocation -> {
            MasterType savedType = invocation.getArgument(0);
            savedType.setId(1L);
            return savedType;
        });

        // Act
        MasterTypeDto result = masterDataManagementService.createMasterType(requestDto, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(requestDto.getTypeCode(), result.getTypeCode());
        assertEquals(requestDto.getTypeNameJa(), result.getTypeNameJa());
        assertEquals(requestDto.getTypeNameEn(), result.getTypeNameEn());
        assertEquals(requestDto.getDescription(), result.getDescription());
        assertEquals(requestDto.getDisplayOrder(), result.getDisplayOrder());
        assertTrue(result.getIsActive());
        assertFalse(result.getIsSystemReserved());

        // Verify that save was called with the expected entity
        ArgumentCaptor<MasterType> typeCaptor = ArgumentCaptor.forClass(MasterType.class);
        verify(masterTypeRepository).save(typeCaptor.capture());
        MasterType savedType = typeCaptor.getValue();
        assertEquals(requestDto.getTypeCode(), savedType.getTypeCode());
        assertEquals(1L, savedType.getCreatedBy());
        assertNotNull(savedType.getCreatedAt());
    }

    @Test
    void createMasterType_WithDuplicateCode_ShouldThrowException() {
        // Arrange
        MasterTypeCreateRequestDto requestDto = new MasterTypeCreateRequestDto();
        requestDto.setTypeCode("existing");
        requestDto.setTypeNameJa("既存タイプ");
        requestDto.setTypeNameEn("Existing Type");

        when(masterTypeRepository.existsByTypeCode(requestDto.getTypeCode())).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> 
                masterDataManagementService.createMasterType(requestDto, 1L));
        
        // Verify that save was not called
        verify(masterTypeRepository, never()).save(any(MasterType.class));
    }

    // Master Data Tests

    @Test
    void getMasterDataList_ShouldReturnPageOfMasterData() {
        // Arrange
        String typeCode = "department";
        Pageable pageable = Pageable.unpaged();
        List<MasterData> masterDataList = Arrays.asList(
                createSampleMasterData(1L, typeCode, "DEPT1"),
                createSampleMasterData(2L, typeCode, "DEPT2")
        );
        Page<MasterData> page = new PageImpl<>(masterDataList);

        when(masterDataRepository.findByMasterTypeCodeAndIsActiveTrue(typeCode, pageable)).thenReturn(page);

        // Act
        Page<MasterDataDto> result = masterDataManagementService.getMasterDataList(typeCode, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("DEPT1", result.getContent().get(0).getCode());
        assertEquals("DEPT2", result.getContent().get(1).getCode());
    }

    @Test
    void createMasterData_WithValidData_ShouldCreateData() {
        // Arrange
        String typeCode = "department";
        MasterType masterType = createSampleMasterType(1L, typeCode);
        
        MasterDataCreateRequestDto requestDto = new MasterDataCreateRequestDto();
        requestDto.setCode("NEW_DEPT");
        requestDto.setNameJa("新部門");
        requestDto.setNameEn("New Department");
        requestDto.setDisplayOrder(5);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("location", "Tokyo");
        requestDto.setAttributes(attributes);

        when(masterTypeRepository.findByTypeCode(typeCode)).thenReturn(Optional.of(masterType));
        when(masterDataRepository.existsByMasterTypeIdAndCode(masterType.getId(), requestDto.getCode())).thenReturn(false);
        when(masterDataRepository.save(any(MasterData.class))).thenAnswer(invocation -> {
            MasterData savedData = invocation.getArgument(0);
            savedData.setId(1L);
            return savedData;
        });

        // Act
        MasterDataDto result = masterDataManagementService.createMasterData(typeCode, requestDto, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(requestDto.getCode(), result.getCode());
        assertEquals(requestDto.getNameJa(), result.getNameJa());
        assertEquals(requestDto.getNameEn(), result.getNameEn());
        assertEquals(requestDto.getDisplayOrder(), result.getDisplayOrder());
        assertEquals(typeCode, result.getTypeCode());
        assertTrue(result.getIsActive());
        assertFalse(result.getIsSystemReserved());
        assertEquals("Tokyo", result.getAttributes().get("location"));

        // Verify that save was called with the expected entity
        ArgumentCaptor<MasterData> dataCaptor = ArgumentCaptor.forClass(MasterData.class);
        verify(masterDataRepository).save(dataCaptor.capture());
        MasterData savedData = dataCaptor.getValue();
        assertEquals(requestDto.getCode(), savedData.getCode());
        assertEquals(1L, savedData.getCreatedBy());
        assertNotNull(savedData.getCreatedAt());
    }

    @Test
    void updateMasterData_WithValidData_ShouldUpdateData() {
        // Arrange
        String typeCode = "department";
        String code = "DEPT1";
        MasterType masterType = createSampleMasterType(1L, typeCode);
        MasterData masterData = createSampleMasterData(1L, masterType, code);
        
        MasterDataUpdateRequestDto requestDto = new MasterDataUpdateRequestDto();
        requestDto.setNameJa("更新部門");
        requestDto.setNameEn("Updated Department");
        requestDto.setDisplayOrder(10);
        requestDto.setIsActive(true);
        Map<String, Object> updatedAttributes = new HashMap<>();
        updatedAttributes.put("location", "Osaka");
        requestDto.setAttributes(updatedAttributes);

        when(masterTypeRepository.findByTypeCode(typeCode)).thenReturn(Optional.of(masterType));
        when(masterDataRepository.findByMasterTypeIdAndCode(masterType.getId(), code)).thenReturn(Optional.of(masterData));
        when(masterDataRepository.save(any(MasterData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MasterDataDto result = masterDataManagementService.updateMasterData(typeCode, code, requestDto, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(code, result.getCode());
        assertEquals(requestDto.getNameJa(), result.getNameJa());
        assertEquals(requestDto.getNameEn(), result.getNameEn());
        assertEquals(requestDto.getDisplayOrder(), result.getDisplayOrder());
        assertEquals(requestDto.getIsActive(), result.getIsActive());
        assertEquals("Osaka", result.getAttributes().get("location"));

        // Verify that save was called with the expected entity
        ArgumentCaptor<MasterData> dataCaptor = ArgumentCaptor.forClass(MasterData.class);
        verify(masterDataRepository).save(dataCaptor.capture());
        MasterData savedData = dataCaptor.getValue();
        assertEquals(requestDto.getNameJa(), savedData.getNameJa());
        assertEquals(2L, savedData.getUpdatedBy());
        assertNotNull(savedData.getUpdatedAt());
    }

    // Helper methods

    private MasterType createSampleMasterType(Long id, String typeCode) {
        MasterType masterType = new MasterType();
        masterType.setId(id);
        masterType.setTypeCode(typeCode);
        masterType.setTypeNameJa(typeCode + "名");
        masterType.setTypeNameEn(typeCode + " Name");
        masterType.setDescription(typeCode + "の説明");
        masterType.setDisplayOrder(1);
        masterType.setIsActive(true);
        masterType.setIsSystemReserved(false);
        masterType.setCreatedBy(1L);
        masterType.setCreatedAt(LocalDateTime.now());
        masterType.setUpdatedBy(1L);
        masterType.setUpdatedAt(LocalDateTime.now());
        return masterType;
    }

    private MasterData createSampleMasterData(Long id, String typeCode, String code) {
        MasterType masterType = createSampleMasterType(id, typeCode);
        return createSampleMasterData(id, masterType, code);
    }

    private MasterData createSampleMasterData(Long id, MasterType masterType, String code) {
        MasterData masterData = new MasterData();
        masterData.setId(id);
        masterData.setMasterType(masterType);
        masterData.setCode(code);
        masterData.setNameJa(code + "名");
        masterData.setNameEn(code + " Name");
        masterData.setShortNameJa(code + "略");
        masterData.setShortNameEn(code + " Short");
        masterData.setDescription(code + "の説明");
        masterData.setDisplayOrder(1);
        masterData.setIsActive(true);
        masterData.setIsSystemReserved(false);
        masterData.setCreatedBy(1L);
        masterData.setCreatedAt(LocalDateTime.now());
        masterData.setUpdatedBy(1L);
        masterData.setUpdatedAt(LocalDateTime.now());
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        masterData.setAttributes(attributes);
        return masterData;
    }

    private MasterDataAttribute createSampleAttribute(Long id, MasterType masterType, String attributeName) {
        MasterDataAttribute attribute = new MasterDataAttribute();
        attribute.setId(id);
        attribute.setMasterType(masterType);
        attribute.setAttributeName(attributeName);
        attribute.setDisplayNameJa(attributeName + "名");
        attribute.setDisplayNameEn(attributeName + " Name");
        attribute.setDataType("string");
        attribute.setIsRequired(false);
        attribute.setDisplayOrder(1);
        attribute.setCreatedBy(1L);
        attribute.setCreatedAt(LocalDateTime.now());
        attribute.setUpdatedBy(1L);
        attribute.setUpdatedAt(LocalDateTime.now());
        return attribute;
    }
}