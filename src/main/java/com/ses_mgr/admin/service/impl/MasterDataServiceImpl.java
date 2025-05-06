package com.ses_mgr.admin.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.admin.dto.*;
import com.ses_mgr.admin.entity.MasterData;
import com.ses_mgr.admin.entity.MasterDataAttribute;
import com.ses_mgr.admin.entity.MasterType;
import com.ses_mgr.admin.repository.MasterDataAttributeRepository;
import com.ses_mgr.admin.repository.MasterDataRepository;
import com.ses_mgr.admin.repository.MasterTypeRepository;
import com.ses_mgr.admin.service.MasterDataService;
import com.ses_mgr.common.exception.ResourceAlreadyExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * マスタデータサービス実装クラス
 * Master data service implementation
 */
@Service
public class MasterDataServiceImpl implements MasterDataService {

    private static final Logger logger = LoggerFactory.getLogger(MasterDataServiceImpl.class);
    
    private final MasterTypeRepository masterTypeRepository;
    private final MasterDataRepository masterDataRepository;
    private final MasterDataAttributeRepository attributeRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public MasterDataServiceImpl(
            MasterTypeRepository masterTypeRepository,
            MasterDataRepository masterDataRepository,
            MasterDataAttributeRepository attributeRepository,
            ObjectMapper objectMapper) {
        this.masterTypeRepository = masterTypeRepository;
        this.masterDataRepository = masterDataRepository;
        this.attributeRepository = attributeRepository;
        this.objectMapper = objectMapper;
    }

    // Master Type Management

    @Override
    @Transactional(readOnly = true)
    public List<MasterTypeDto> getAllMasterTypes() {
        List<MasterType> masterTypes = masterTypeRepository.findAll();
        return masterTypes.stream()
                .map(this::convertToMasterTypeDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MasterTypeDto getMasterTypeByCode(String code) {
        MasterType masterType = findMasterTypeByCode(code);
        return convertToMasterTypeDto(masterType);
    }

    @Override
    @Transactional
    public MasterTypeDto createMasterType(MasterTypeDto masterTypeDto) {
        // Check if already exists
        if (masterTypeRepository.findByCode(masterTypeDto.getCode()).isPresent()) {
            throw new ResourceAlreadyExistsException("Master type with code " + masterTypeDto.getCode() + " already exists");
        }

        MasterType masterType = new MasterType();
        masterType.setCode(masterTypeDto.getCode());
        masterType.setName(masterTypeDto.getName());
        masterType.setDescription(masterTypeDto.getDescription());
        masterType.setAttributeDefinition(convertAttributeDefinitionToJson(masterTypeDto.getAttributes()));
        masterType.setCreatedAt(LocalDateTime.now());
        masterType.setUpdatedAt(LocalDateTime.now());

        MasterType savedMasterType = masterTypeRepository.save(masterType);
        return convertToMasterTypeDto(savedMasterType);
    }

    @Override
    @Transactional
    public MasterTypeDto updateMasterType(String code, MasterTypeDto masterTypeDto) {
        MasterType masterType = findMasterTypeByCode(code);
        
        // Update fields
        masterType.setName(masterTypeDto.getName());
        masterType.setDescription(masterTypeDto.getDescription());
        masterType.setAttributeDefinition(convertAttributeDefinitionToJson(masterTypeDto.getAttributes()));
        masterType.setUpdatedAt(LocalDateTime.now());

        MasterType updatedMasterType = masterTypeRepository.save(masterType);
        return convertToMasterTypeDto(updatedMasterType);
    }

    @Override
    @Transactional
    public void deleteMasterType(String code) {
        MasterType masterType = findMasterTypeByCode(code);
        
        // Check if master type has data
        long count = masterDataRepository.countByTypeCode(code);
        if (count > 0) {
            throw new IllegalStateException("Cannot delete master type with existing data. Delete all data first.");
        }
        
        masterTypeRepository.delete(masterType);
    }

    // Master Data Management

    @Override
    @Transactional(readOnly = true)
    public Page<MasterDataDto> getMasterDataList(String typeCode, String search, String status, Pageable pageable) {
        // Verify master type exists
        findMasterTypeByCode(typeCode);
        
        Page<MasterData> masterDataPage;
        
        if (search != null && !search.isEmpty()) {
            if (status != null && !status.isEmpty()) {
                masterDataPage = masterDataRepository.findByTypeCodeAndStatusAndNameContainingIgnoreCase(
                        typeCode, status, search, pageable);
            } else {
                masterDataPage = masterDataRepository.findByTypeCodeAndNameContainingIgnoreCase(
                        typeCode, search, pageable);
            }
        } else {
            if (status != null && !status.isEmpty()) {
                masterDataPage = masterDataRepository.findByTypeCodeAndStatus(typeCode, status, pageable);
            } else {
                masterDataPage = masterDataRepository.findByTypeCode(typeCode, pageable);
            }
        }
        
        return masterDataPage.map(this::convertToMasterDataDto);
    }

    @Override
    @Transactional(readOnly = true)
    public MasterDataDto getMasterDataByCode(String typeCode, String code) {
        MasterData masterData = findMasterDataByCode(typeCode, code);
        return convertToMasterDataDto(masterData);
    }

    @Override
    @Transactional
    public MasterDataDto createMasterData(String typeCode, MasterDataDto masterDataDto) {
        // Verify master type exists
        MasterType masterType = findMasterTypeByCode(typeCode);
        
        // Check if data already exists
        if (masterDataRepository.findByTypeCodeAndCode(typeCode, masterDataDto.getCode()).isPresent()) {
            throw new ResourceAlreadyExistsException(
                    "Master data with code " + masterDataDto.getCode() + " already exists for type " + typeCode);
        }
        
        MasterData masterData = new MasterData();
        masterData.setTypeCode(typeCode);
        masterData.setCode(masterDataDto.getCode());
        masterData.setName(masterDataDto.getName());
        masterData.setDescription(masterDataDto.getDescription());
        masterData.setStatus(masterDataDto.getStatus());
        masterData.setDisplayOrder(masterDataDto.getDisplayOrder());
        masterData.setCreatedAt(LocalDateTime.now());
        masterData.setUpdatedAt(LocalDateTime.now());
        
        MasterData savedMasterData = masterDataRepository.save(masterData);
        
        // Save attributes if provided
        if (masterDataDto.getAttributes() != null && !masterDataDto.getAttributes().isEmpty()) {
            List<MasterDataAttribute> attributes = masterDataDto.getAttributes().stream()
                    .map(attr -> {
                        MasterDataAttribute attribute = new MasterDataAttribute();
                        attribute.setMasterData(savedMasterData);
                        attribute.setName(attr.getName());
                        attribute.setValue(attr.getValue());
                        attribute.setCreatedAt(LocalDateTime.now());
                        attribute.setUpdatedAt(LocalDateTime.now());
                        return attribute;
                    })
                    .collect(Collectors.toList());
            
            attributeRepository.saveAll(attributes);
        }
        
        // Refresh to get attributes
        MasterData refreshedData = masterDataRepository.findById(savedMasterData.getId()).orElseThrow();
        return convertToMasterDataDto(refreshedData);
    }

    @Override
    @Transactional
    public MasterDataDto updateMasterData(String typeCode, String code, MasterDataDto masterDataDto) {
        MasterData masterData = findMasterDataByCode(typeCode, code);
        
        // Update fields
        masterData.setName(masterDataDto.getName());
        masterData.setDescription(masterDataDto.getDescription());
        masterData.setStatus(masterDataDto.getStatus());
        masterData.setDisplayOrder(masterDataDto.getDisplayOrder());
        masterData.setUpdatedAt(LocalDateTime.now());
        
        MasterData updatedMasterData = masterDataRepository.save(masterData);
        
        // Update attributes if provided
        if (masterDataDto.getAttributes() != null && !masterDataDto.getAttributes().isEmpty()) {
            updateAttributes(typeCode, code, masterDataDto.getAttributes());
        }
        
        // Refresh to get updated attributes
        MasterData refreshedData = masterDataRepository.findById(updatedMasterData.getId()).orElseThrow();
        return convertToMasterDataDto(refreshedData);
    }

    @Override
    @Transactional
    public void deleteMasterData(String typeCode, String code) {
        MasterData masterData = findMasterDataByCode(typeCode, code);
        
        // Delete associated attributes first
        attributeRepository.deleteByMasterDataId(masterData.getId());
        
        // Delete master data
        masterDataRepository.delete(masterData);
    }

    @Override
    @Transactional
    public MasterDataDto updateAttributes(String typeCode, String code, List<MasterDataAttributeDto> attributes) {
        MasterData masterData = findMasterDataByCode(typeCode, code);
        
        // Remove existing attributes
        attributeRepository.deleteByMasterDataId(masterData.getId());
        
        // Create new attributes
        List<MasterDataAttribute> newAttributes = attributes.stream()
                .map(attr -> {
                    MasterDataAttribute attribute = new MasterDataAttribute();
                    attribute.setMasterData(masterData);
                    attribute.setName(attr.getName());
                    attribute.setValue(attr.getValue());
                    attribute.setCreatedAt(LocalDateTime.now());
                    attribute.setUpdatedAt(LocalDateTime.now());
                    return attribute;
                })
                .collect(Collectors.toList());
        
        attributeRepository.saveAll(newAttributes);
        
        // Refresh to get updated attributes
        MasterData refreshedData = masterDataRepository.findById(masterData.getId()).orElseThrow();
        return convertToMasterDataDto(refreshedData);
    }

    @Override
    @Transactional
    public MasterDataImportResponseDto importMasterData(String typeCode, MasterDataImportRequestDto importRequest) {
        // Verify master type exists
        MasterType masterType = findMasterTypeByCode(typeCode);
        
        // Parse attribute definition
        List<Map<String, Object>> attributeDefinition = parseAttributeDefinition(masterType.getAttributeDefinition());
        
        MasterDataImportResponseDto response = new MasterDataImportResponseDto();
        response.setTypeCode(typeCode);
        response.setTotalRecords(importRequest.getData().size());
        
        List<String> errors = new ArrayList<>();
        List<MasterDataDto> importedData = new ArrayList<>();
        int successCount = 0;
        
        for (MasterDataDto dataDto : importRequest.getData()) {
            try {
                // Validate required fields
                if (dataDto.getCode() == null || dataDto.getCode().isEmpty()) {
                    errors.add("Row " + (importedData.size() + 1) + ": Code is required");
                    continue;
                }
                
                if (dataDto.getName() == null || dataDto.getName().isEmpty()) {
                    errors.add("Row " + (importedData.size() + 1) + ": Name is required");
                    continue;
                }
                
                // Check if record exists
                Optional<MasterData> existingData = masterDataRepository.findByTypeCodeAndCode(typeCode, dataDto.getCode());
                
                if (existingData.isPresent()) {
                    if (importRequest.isOverwrite()) {
                        // Update existing record
                        MasterDataDto updated = updateMasterData(typeCode, dataDto.getCode(), dataDto);
                        importedData.add(updated);
                        successCount++;
                    } else {
                        errors.add("Row " + (importedData.size() + 1) + ": Record with code " + dataDto.getCode() + " already exists");
                    }
                } else {
                    // Create new record
                    MasterDataDto created = createMasterData(typeCode, dataDto);
                    importedData.add(created);
                    successCount++;
                }
            } catch (Exception e) {
                errors.add("Row " + (importedData.size() + 1) + ": Error - " + e.getMessage());
            }
        }
        
        response.setSuccessCount(successCount);
        response.setErrorCount(errors.size());
        response.setErrors(errors);
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public MasterDataExportResponseDto exportMasterData(String typeCode, String format, String status) {
        // Verify master type exists
        MasterType masterType = findMasterTypeByCode(typeCode);
        
        // Get all data for export
        List<MasterData> dataList;
        if (status != null && !status.isEmpty()) {
            dataList = masterDataRepository.findByTypeCodeAndStatusOrderByDisplayOrderAsc(typeCode, status);
        } else {
            dataList = masterDataRepository.findByTypeCodeOrderByDisplayOrderAsc(typeCode);
        }
        
        List<MasterDataDto> dataDtos = dataList.stream()
                .map(this::convertToMasterDataDto)
                .collect(Collectors.toList());
        
        MasterDataExportResponseDto response = new MasterDataExportResponseDto();
        response.setTypeCode(typeCode);
        response.setTypeName(masterType.getName());
        response.setFormat(format);
        response.setTotalRecords(dataDtos.size());
        
        try {
            String data;
            if ("csv".equalsIgnoreCase(format)) {
                data = generateCsvData(dataDtos, masterType);
                response.setContentType("text/csv");
                response.setFileName(typeCode + "_export.csv");
            } else if ("excel".equalsIgnoreCase(format)) {
                data = generateExcelData(dataDtos, masterType);
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setFileName(typeCode + "_export.xlsx");
            } else {
                data = objectMapper.writeValueAsString(dataDtos);
                response.setContentType("application/json");
                response.setFileName(typeCode + "_export.json");
            }
            
            response.setData(Base64.encodeBase64String(data.getBytes(StandardCharsets.UTF_8)));
        } catch (JsonProcessingException e) {
            logger.error("Error generating export data", e);
            throw new RuntimeException("Failed to generate export data: " + e.getMessage(), e);
        }
        
        return response;
    }

    // Helper methods

    private MasterType findMasterTypeByCode(String code) {
        return masterTypeRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Master type not found with code: " + code));
    }

    private MasterData findMasterDataByCode(String typeCode, String code) {
        return masterDataRepository.findByTypeCodeAndCode(typeCode, code)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Master data not found with code: " + code + " for type: " + typeCode));
    }

    private MasterTypeDto convertToMasterTypeDto(MasterType masterType) {
        MasterTypeDto dto = new MasterTypeDto();
        dto.setId(masterType.getId());
        dto.setCode(masterType.getCode());
        dto.setName(masterType.getName());
        dto.setDescription(masterType.getDescription());
        dto.setAttributes(parseAttributeDefinition(masterType.getAttributeDefinition()));
        dto.setCreatedAt(masterType.getCreatedAt());
        dto.setUpdatedAt(masterType.getUpdatedAt());
        return dto;
    }

    private MasterDataDto convertToMasterDataDto(MasterData masterData) {
        MasterDataDto dto = new MasterDataDto();
        dto.setId(masterData.getId());
        dto.setTypeCode(masterData.getTypeCode());
        dto.setCode(masterData.getCode());
        dto.setName(masterData.getName());
        dto.setDescription(masterData.getDescription());
        dto.setStatus(masterData.getStatus());
        dto.setDisplayOrder(masterData.getDisplayOrder());
        dto.setCreatedAt(masterData.getCreatedAt());
        dto.setUpdatedAt(masterData.getUpdatedAt());
        
        // Convert attributes
        if (masterData.getAttributes() != null) {
            List<MasterDataAttributeDto> attributeDtos = masterData.getAttributes().stream()
                    .map(attr -> {
                        MasterDataAttributeDto attrDto = new MasterDataAttributeDto();
                        attrDto.setId(attr.getId());
                        attrDto.setName(attr.getName());
                        attrDto.setValue(attr.getValue());
                        return attrDto;
                    })
                    .collect(Collectors.toList());
            
            dto.setAttributes(attributeDtos);
        }
        
        return dto;
    }

    private String convertAttributeDefinitionToJson(List<Map<String, Object>> attributes) {
        try {
            return objectMapper.writeValueAsString(attributes);
        } catch (JsonProcessingException e) {
            logger.error("Error converting attributes to JSON", e);
            return "[]";
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseAttributeDefinition(String attributeDefinition) {
        if (attributeDefinition == null || attributeDefinition.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            return objectMapper.readValue(attributeDefinition, List.class);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing attribute definition", e);
            return Collections.emptyList();
        }
    }

    private String generateCsvData(List<MasterDataDto> dataDtos, MasterType masterType) {
        StringBuilder csv = new StringBuilder();
        
        // Add header
        csv.append("code,name,description,status,displayOrder");
        
        // Add attribute headers based on definition
        List<Map<String, Object>> attributeDefinition = parseAttributeDefinition(masterType.getAttributeDefinition());
        for (Map<String, Object> attrDef : attributeDefinition) {
            csv.append(",").append(attrDef.get("name"));
        }
        csv.append("\n");
        
        // Add data rows
        for (MasterDataDto dto : dataDtos) {
            csv.append(escapeForCsv(dto.getCode())).append(",");
            csv.append(escapeForCsv(dto.getName())).append(",");
            csv.append(escapeForCsv(dto.getDescription())).append(",");
            csv.append(escapeForCsv(dto.getStatus())).append(",");
            csv.append(dto.getDisplayOrder());
            
            // Add attribute values
            Map<String, String> attributeMap = new HashMap<>();
            if (dto.getAttributes() != null) {
                for (MasterDataAttributeDto attr : dto.getAttributes()) {
                    attributeMap.put(attr.getName(), attr.getValue());
                }
            }
            
            for (Map<String, Object> attrDef : attributeDefinition) {
                String attrName = (String) attrDef.get("name");
                String value = attributeMap.getOrDefault(attrName, "");
                csv.append(",").append(escapeForCsv(value));
            }
            
            csv.append("\n");
        }
        
        return csv.toString();
    }

    private String escapeForCsv(String value) {
        if (value == null) {
            return "";
        }
        
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }

    private String generateExcelData(List<MasterDataDto> dataDtos, MasterType masterType) {
        // This is a simplified version - normally you would use Apache POI or similar
        // For now, we'll just return CSV data with a note that in a real implementation
        // this would generate Excel data
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write("Note: This is CSV data. In a production environment, this would be Excel format.\n\n".getBytes(StandardCharsets.UTF_8));
            outputStream.write(generateCsvData(dataDtos, masterType).getBytes(StandardCharsets.UTF_8));
            return outputStream.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            logger.error("Error generating Excel data", e);
            throw new RuntimeException("Failed to generate Excel data: " + e.getMessage(), e);
        }
    }
}