package com.ses_mgr.common.controller.api;

import com.ses_mgr.common.dto.CodeCategoryDto;
import com.ses_mgr.common.dto.CodeValueDetailDto;
import com.ses_mgr.common.dto.CodeValueDto;
import com.ses_mgr.common.service.CodeService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CodeRestControllerTest {

    @Mock
    private CodeService codeService;

    @InjectMocks
    private CodeRestController codeRestController;

    private List<CodeCategoryDto> categories;
    private List<CodeValueDto> codeValues;
    private CodeValueDetailDto codeDetail;
    private CodeCategoryDto jobTypeCategory;

    @BeforeEach
    public void setup() {
        // カテゴリ一覧のモックデータ
        jobTypeCategory = CodeCategoryDto.builder()
                .id("job_type")
                .name("職種")
                .description("技術者の職種区分")
                .count(5)
                .build();
        
        CodeCategoryDto skillCategory = CodeCategoryDto.builder()
                .id("skill")
                .name("スキル")
                .description("技術スキル一覧")
                .count(120)
                .build();
        
        categories = Arrays.asList(jobTypeCategory, skillCategory);

        // コード値一覧のモックデータ
        Map<String, Object> pmAttributes = new HashMap<>();
        pmAttributes.put("abbreviation", "PM");
        pmAttributes.put("skill_level", "senior");
        
        CodeValueDto pmCode = CodeValueDto.builder()
                .code("PM")
                .name("プロジェクトマネージャ")
                .sortOrder(1)
                .isActive(true)
                .attributes(pmAttributes)
                .build();
        
        Map<String, Object> seAttributes = new HashMap<>();
        seAttributes.put("abbreviation", "SE");
        seAttributes.put("skill_level", "middle");
        
        CodeValueDto seCode = CodeValueDto.builder()
                .code("SE")
                .name("システムエンジニア")
                .sortOrder(2)
                .isActive(true)
                .attributes(seAttributes)
                .build();
        
        codeValues = Arrays.asList(pmCode, seCode);

        // コード値詳細のモックデータ
        codeDetail = CodeValueDetailDto.builder()
                .category(jobTypeCategory)
                .code("PM")
                .name("プロジェクトマネージャ")
                .description("プロジェクト全体の管理と調整を担当する職種")
                .sortOrder(1)
                .isActive(true)
                .attributes(pmAttributes)
                .childCodes(Arrays.asList("SE"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    public void getAllCategories_ShouldReturnCategoriesList() {
        // Arrange
        when(codeService.getAllCategories()).thenReturn(categories);

        // Act
        ResponseEntity<?> response = codeRestController.getAllCategories();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void getAllCategories_WhenExceptionThrown_ShouldReturnErrorResponse() {
        // Arrange
        when(codeService.getAllCategories()).thenThrow(new RuntimeException("Test exception"));

        // Act
        ResponseEntity<?> response = codeRestController.getAllCategories();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void getCodesByCategory_ShouldReturnCodesList() {
        // Arrange
        when(codeService.getCategoryInfo("job_type")).thenReturn(jobTypeCategory);
        when(codeService.getCodesByCategory("job_type", null, null, true)).thenReturn(codeValues);

        // Act
        ResponseEntity<?> response = codeRestController.getCodesByCategory("job_type", null, null, true);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void getCodesByCategory_WithInvalidCategory_ShouldReturnNotFound() {
        // Arrange
        when(codeService.getCategoryInfo("invalid_category")).thenThrow(new EntityNotFoundException("指定されたカテゴリが存在しません。"));

        // Act
        ResponseEntity<?> response = codeRestController.getCodesByCategory("invalid_category", null, null, true);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void getCodesByCategory_WhenExceptionThrown_ShouldReturnErrorResponse() {
        // Arrange
        when(codeService.getCategoryInfo("job_type")).thenThrow(new RuntimeException("Test exception"));

        // Act
        ResponseEntity<?> response = codeRestController.getCodesByCategory("job_type", null, null, true);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void getCodeDetail_ShouldReturnCodeDetail() {
        // Arrange
        when(codeService.getCodeDetail("job_type", "PM")).thenReturn(codeDetail);

        // Act
        ResponseEntity<?> response = codeRestController.getCodeDetail("job_type", "PM");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void getCodeDetail_WithInvalidCategoryOrCode_ShouldReturnNotFound() {
        // Arrange
        when(codeService.getCodeDetail("job_type", "invalid_code")).thenThrow(new EntityNotFoundException("指定されたカテゴリまたはコード値が存在しません。"));

        // Act
        ResponseEntity<?> response = codeRestController.getCodeDetail("job_type", "invalid_code");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void getCodeDetail_WhenExceptionThrown_ShouldReturnErrorResponse() {
        // Arrange
        when(codeService.getCodeDetail("job_type", "PM")).thenThrow(new RuntimeException("Test exception"));

        // Act
        ResponseEntity<?> response = codeRestController.getCodeDetail("job_type", "PM");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}