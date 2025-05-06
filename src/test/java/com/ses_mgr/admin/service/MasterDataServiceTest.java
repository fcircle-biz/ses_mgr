package com.ses_mgr.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.admin.dto.MasterDataDto;
import com.ses_mgr.admin.dto.MasterDataImportRequestDto;
import com.ses_mgr.admin.dto.MasterDataImportResponseDto;
import com.ses_mgr.admin.dto.MasterTypeDto;
import com.ses_mgr.admin.entity.MasterData;
import com.ses_mgr.admin.entity.MasterType;
import com.ses_mgr.admin.repository.MasterDataAttributeRepository;
import com.ses_mgr.admin.repository.MasterDataRepository;
import com.ses_mgr.admin.repository.MasterTypeRepository;
import com.ses_mgr.admin.service.impl.MasterDataServiceImpl;
import com.ses_mgr.common.exception.ResourceAlreadyExistsException;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class MasterDataServiceTest {

    @Mock
    private MasterTypeRepository masterTypeRepository;

    @Mock
    private MasterDataRepository masterDataRepository;

    @Mock
    private MasterDataAttributeRepository attributeRepository;

    @Mock
    private ObjectMapper objectMapper;

    private MasterDataService masterDataService;
    private LocalDateTime testTime;

    @BeforeEach
    public void setup() {
        masterDataService = new MasterDataServiceImpl(
                masterTypeRepository,
                masterDataRepository,
                attributeRepository,
                objectMapper
        );
        testTime = LocalDateTime.now();
    }

    @Test
    public void getAllMasterTypes_ShouldReturnAllTypes() {
        // Arrange
        List<MasterType> masterTypes = new ArrayList<>();
        MasterType type1 = new MasterType();
        type1.setId(1L);
        type1.setTypeCode("CODE1");
        type1.setTypeNameJa("Type 1");
        type1.setAttributeDefinition("[]");
        type1.setCreatedAt(testTime);
        type1.setUpdatedAt(testTime);
        masterTypes.add(type1);

        MasterType type2 = new MasterType();
        type2.setId(2L);
        type2.setTypeCode("CODE2");
        type2.setTypeNameJa("Type 2");
        type2.setAttributeDefinition("[]");
        type2.setCreatedAt(testTime);
        type2.setUpdatedAt(testTime);
        masterTypes.add(type2);

        when(masterTypeRepository.findAll()).thenReturn(masterTypes);

        // Act
        List<MasterTypeDto> result = masterDataService.getAllMasterTypes();

        // Assert
        assertEquals(2, result.size());
        assertEquals("CODE1", result.get(0).getCode());
        assertEquals("Type 1", result.get(0).getName());
        assertEquals("CODE2", result.get(1).getCode());
        assertEquals("Type 2", result.get(1).getName());
    }

    @Test
    public void getMasterTypeByCode_ShouldReturnTypeWhenExists() {
        // Arrange
        String typeCode = "CODE1";
        MasterType type = new MasterType();
        type.setId(1L);
        type.setTypeCode(typeCode);
        type.setTypeNameJa("Type 1");
        type.setAttributeDefinition("[]");
        type.setCreatedAt(testTime);
        type.setUpdatedAt(testTime);

        when(masterTypeRepository.findByCode(typeCode)).thenReturn(Optional.of(type));

        // Act
        MasterTypeDto result = masterDataService.getMasterTypeByCode(typeCode);

        // Assert
        assertNotNull(result);
        assertEquals(typeCode, result.getCode());
        assertEquals("Type 1", result.getName());
    }

    @Test
    public void getMasterTypeByCode_ShouldThrowExceptionWhenNotExists() {
        // Arrange
        String typeCode = "NONEXISTENT";
        when(masterTypeRepository.findByCode(typeCode)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> masterDataService.getMasterTypeByCode(typeCode));
    }

    @Test
    public void createMasterType_ShouldCreateNewType() {
        // Arrange
        MasterTypeDto dto = new MasterTypeDto();
        dto.setCode("NEWTYPE");
        dto.setName("New Type");
        dto.setDescription("Description");
        dto.setAttributes(new ArrayList<>());

        when(masterTypeRepository.findByCode(dto.getCode())).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        MasterType savedType = new MasterType();
        savedType.setId(1L);
        savedType.setTypeCode(dto.getCode());
        savedType.setTypeNameJa(dto.getName());
        savedType.setDescription(dto.getDescription());
        savedType.setAttributeDefinition("[]");
        savedType.setCreatedAt(testTime);
        savedType.setUpdatedAt(testTime);

        when(masterTypeRepository.save(any(MasterType.class))).thenReturn(savedType);

        // Act
        MasterTypeDto result = masterDataService.createMasterType(dto);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getCode(), result.getCode());
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getDescription(), result.getDescription());

        ArgumentCaptor<MasterType> typeCaptor = ArgumentCaptor.forClass(MasterType.class);
        verify(masterTypeRepository).save(typeCaptor.capture());
        assertEquals(dto.getCode(), typeCaptor.getValue().getCode());
        assertEquals(dto.getName(), typeCaptor.getValue().getName());
    }

    @Test
    public void getMasterDataList_ShouldReturnDataList() {
        // Arrange
        String typeCode = "CODE1";
        MasterType type = new MasterType();
        type.setId(1L);
        type.setTypeCode(typeCode);
        when(masterTypeRepository.findByCode(typeCode)).thenReturn(Optional.of(type));

        List<MasterData> dataList = new ArrayList<>();
        MasterData data1 = new MasterData();
        data1.setId(1L);
        data1.setMasterType(type);
        data1.setCode("DATA1");
        data1.setNameJa("Data 1");
        data1.setIsActive(true);
        data1.setCreatedAt(testTime);
        data1.setUpdatedAt(testTime);
        dataList.add(data1);

        Pageable pageable = PageRequest.of(0, 10);
        PageImpl<MasterData> page = new PageImpl<>(dataList, pageable, 1);
        when(masterDataRepository.findByTypeCode(typeCode, pageable)).thenReturn(page);

        // Act
        Page<MasterDataDto> result = masterDataService.getMasterDataList(typeCode, null, null, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(typeCode, result.getContent().get(0).getTypeCode());
        assertEquals("DATA1", result.getContent().get(0).getCode());
        assertEquals("Data 1", result.getContent().get(0).getName());
    }

    @Test
    public void importMasterData_ShouldReturnResponse() {
        // Arrange
        String typeCode = "CODE1";
        MasterTypeDto dto = new MasterTypeDto();
        dto.setCode(typeCode);
        dto.setName("Type 1");

        MasterType type = new MasterType();
        type.setId(1L);
        type.setTypeCode(typeCode);
        type.setTypeNameJa("Type 1");
        type.setAttributeDefinition("[]");

        when(masterTypeRepository.findByCode(typeCode)).thenReturn(Optional.of(type));

        MasterDataImportRequestDto importRequest = new MasterDataImportRequestDto();
        importRequest.setTypeCode(typeCode);
        importRequest.setData(new ArrayList<>());

        // Act
        MasterDataImportResponseDto result = masterDataService.importMasterData(typeCode, importRequest);

        // Assert
        assertNotNull(result);
        assertEquals(typeCode, result.getTypeCode());
    }
}