package com.ses_mgr.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.admin.dto.*;
import com.ses_mgr.admin.entity.AccessLog;
import com.ses_mgr.admin.entity.AuditLog;
import com.ses_mgr.admin.entity.ErrorLog;
import com.ses_mgr.admin.entity.SystemLog;
import com.ses_mgr.admin.repository.AccessLogRepository;
import com.ses_mgr.admin.repository.AuditLogRepository;
import com.ses_mgr.admin.repository.ErrorLogRepository;
import com.ses_mgr.admin.repository.SystemLogRepository;
import com.ses_mgr.admin.service.impl.LogManagementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LogManagementServiceTest {

    @Mock
    private SystemLogRepository systemLogRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ErrorLogRepository errorLogRepository;

    @Mock
    private AccessLogRepository accessLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    private LogManagementService logManagementService;
    private LocalDateTime testTime;

    @BeforeEach
    public void setup() {
        logManagementService = new LogManagementServiceImpl(
                systemLogRepository, auditLogRepository, errorLogRepository, accessLogRepository, objectMapper);
        testTime = LocalDateTime.now();
        
        ReflectionTestUtils.setField(logManagementService, "exportPath", "/tmp/logs/export");
        ReflectionTestUtils.setField(logManagementService, "maxExportRecords", 100000);
        ReflectionTestUtils.setField(logManagementService, "tokenValidityMinutes", 30);
    }

    @Test
    public void getSystemLogs_ShouldReturnSystemLogs() {
        // Arrange
        LocalDateTime from = testTime.minusDays(7);
        LocalDateTime to = testTime;
        Pageable pageable = PageRequest.of(0, 10);
        
        List<SystemLog> systemLogs = new ArrayList<>();
        SystemLog systemLog = new SystemLog();
        systemLog.setId(1L);
        systemLog.setLogType("system");
        systemLog.setLevel("info");
        systemLog.setMessage("Test message");
        systemLog.setModule("TestModule");
        systemLog.setTimestamp(testTime);
        systemLogs.add(systemLog);
        
        when(systemLogRepository.searchByMultipleCriteria(
                eq(from), eq(to), any(), any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(systemLogs, pageable, 1));

        // Act
        Page<SystemLogDto> result = logManagementService.getSystemLogs(
                from, to, null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("system", result.getContent().get(0).getType());
        assertEquals("info", result.getContent().get(0).getLevel());
        assertEquals("Test message", result.getContent().get(0).getMessage());
        assertEquals("TestModule", result.getContent().get(0).getModule());
        assertEquals(testTime, result.getContent().get(0).getTimestamp());
    }

    @Test
    public void getAuditLogs_ShouldReturnAuditLogs() {
        // Arrange
        LocalDateTime from = testTime.minusDays(7);
        LocalDateTime to = testTime;
        Pageable pageable = PageRequest.of(0, 10);
        
        List<AuditLog> auditLogs = new ArrayList<>();
        AuditLog auditLog = new AuditLog();
        auditLog.setId(1L);
        auditLog.setLogType("audit");
        auditLog.setLevel("info");
        auditLog.setMessage("Test audit message");
        auditLog.setAction("create");
        auditLog.setResourceType("user");
        auditLog.setResourceId("123");
        auditLog.setTimestamp(testTime);
        auditLogs.add(auditLog);
        
        when(auditLogRepository.searchByMultipleCriteria(
                eq(from), eq(to), any(), any(), any(), any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(auditLogs, pageable, 1));

        // Act
        Page<AuditLogDto> result = logManagementService.getAuditLogs(
                from, to, null, null, null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("audit", result.getContent().get(0).getType());
        assertEquals("info", result.getContent().get(0).getLevel());
        assertEquals("Test audit message", result.getContent().get(0).getMessage());
        assertEquals("create", result.getContent().get(0).getAction());
        assertEquals("user", result.getContent().get(0).getResourceType());
        assertEquals("123", result.getContent().get(0).getResourceId());
        assertEquals(testTime, result.getContent().get(0).getTimestamp());
    }

    @Test
    public void getErrorLogs_ShouldReturnErrorLogs() {
        // Arrange
        LocalDateTime from = testTime.minusDays(7);
        LocalDateTime to = testTime;
        Pageable pageable = PageRequest.of(0, 10);
        
        List<ErrorLog> errorLogs = new ArrayList<>();
        ErrorLog errorLog = new ErrorLog();
        errorLog.setId(1L);
        errorLog.setLogType("error");
        errorLog.setLevel("error");
        errorLog.setMessage("Test error message");
        errorLog.setModule("TestModule");
        errorLog.setErrorCode("E001");
        errorLog.setTimestamp(testTime);
        errorLogs.add(errorLog);
        
        when(errorLogRepository.searchByMultipleCriteria(
                eq(from), eq(to), any(), any(), any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(errorLogs, pageable, 1));

        // Act
        Page<ErrorLogDto> result = logManagementService.getErrorLogs(
                from, to, null, null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("error", result.getContent().get(0).getType());
        assertEquals("error", result.getContent().get(0).getLevel());
        assertEquals("Test error message", result.getContent().get(0).getMessage());
        assertEquals("TestModule", result.getContent().get(0).getModule());
        assertEquals("E001", result.getContent().get(0).getErrorCode());
        assertEquals(testTime, result.getContent().get(0).getTimestamp());
    }

    @Test
    public void getAccessLogs_ShouldReturnAccessLogs() {
        // Arrange
        LocalDateTime from = testTime.minusDays(7);
        LocalDateTime to = testTime;
        Pageable pageable = PageRequest.of(0, 10);
        
        List<AccessLog> accessLogs = new ArrayList<>();
        AccessLog accessLog = new AccessLog();
        accessLog.setId(1L);
        accessLog.setLogType("access");
        accessLog.setLevel("info");
        accessLog.setMessage("Test access message");
        accessLog.setAction("login");
        accessLog.setStatus("success");
        accessLog.setTimestamp(testTime);
        accessLogs.add(accessLog);
        
        when(accessLogRepository.searchByMultipleCriteria(
                eq(from), eq(to), any(), any(), any(), any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(accessLogs, pageable, 1));

        // Act
        Page<AccessLogDto> result = logManagementService.getAccessLogs(
                from, to, null, null, null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("access", result.getContent().get(0).getType());
        assertEquals("info", result.getContent().get(0).getLevel());
        assertEquals("Test access message", result.getContent().get(0).getMessage());
        assertEquals("login", result.getContent().get(0).getAction());
        assertEquals("success", result.getContent().get(0).getStatus());
        assertEquals(testTime, result.getContent().get(0).getTimestamp());
    }

    @Test
    public void getLogStatistics_ShouldReturnStatistics() {
        // Arrange
        LocalDateTime from = testTime.minusDays(7);
        LocalDateTime to = testTime;
        LogStatisticsRequestDto request = new LogStatisticsRequestDto();
        request.setFrom(from);
        request.setTo(to);
        
        // Mock repository count methods
        Pageable singlePageRequest = PageRequest.of(0, 1);
        
        when(systemLogRepository.countByTimeRange(eq(from), eq(to)))
                .thenReturn(30L);
        
        when(auditLogRepository.countByTimeRange(eq(from), eq(to)))
                .thenReturn(40L);
        
        when(errorLogRepository.countByTimeRange(eq(from), eq(to)))
                .thenReturn(20L);
        
        when(accessLogRepository.countByTimeRange(eq(from), eq(to)))
                .thenReturn(10L);
        
        List<Object[]> systemLogLevels = new ArrayList<>();
        systemLogLevels.add(new Object[]{"info", 20L});
        systemLogLevels.add(new Object[]{"warning", 5L});
        systemLogLevels.add(new Object[]{"error", 5L});
        when(systemLogRepository.countByLevel(eq(from), eq(to)))
                .thenReturn(systemLogLevels);
        
        List<Object[]> auditLogLevels = new ArrayList<>();
        auditLogLevels.add(new Object[]{"info", 40L});
        when(auditLogRepository.countByLevel(eq(from), eq(to)))
                .thenReturn(auditLogLevels);
        
        List<Object[]> errorLogLevels = new ArrayList<>();
        errorLogLevels.add(new Object[]{"error", 15L});
        errorLogLevels.add(new Object[]{"critical", 5L});
        when(errorLogRepository.countByLevel(eq(from), eq(to)))
                .thenReturn(errorLogLevels);
        
        List<Object[]> accessLogLevels = new ArrayList<>();
        accessLogLevels.add(new Object[]{"info", 10L});
        when(accessLogRepository.countByLevel(eq(from), eq(to)))
                .thenReturn(accessLogLevels);

        // Act
        LogStatisticsResponseDto result = logManagementService.getLogStatistics(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getSummary());
        Map<String, Object> byType = (Map<String, Object>) result.getSummary().get("byType");
        Map<String, Object> byLevel = (Map<String, Object>) result.getSummary().get("byLevel");
        
        assertEquals(100, result.getSummary().get("totalCount"));
        assertEquals(30, ((Map<String, Integer>) result.getSummary().get("byType")).get("system"));
        assertEquals(40, ((Map<String, Integer>) result.getSummary().get("byType")).get("audit"));
        assertEquals(20, ((Map<String, Integer>) result.getSummary().get("byType")).get("error"));
        assertEquals(10, ((Map<String, Integer>) result.getSummary().get("byType")).get("access"));
        
        assertEquals(70, ((Map<String, Integer>) result.getSummary().get("byLevel")).get("info"));
        assertEquals(5, ((Map<String, Integer>) result.getSummary().get("byLevel")).get("warning"));
        assertEquals(20, ((Map<String, Integer>) result.getSummary().get("byLevel")).get("error"));
        assertEquals(5, ((Map<String, Integer>) result.getSummary().get("byLevel")).get("critical"));
    }

    @Test
    public void exportLogs_ShouldReturnExportResponse() {
        // Arrange
        LocalDateTime from = testTime.minusDays(7);
        LocalDateTime to = testTime;
        LogExportRequestDto request = new LogExportRequestDto();
        request.setFrom(from);
        request.setTo(to);
        request.setFormat("csv");
        request.setEncoding("UTF-8");
        
        // Mock countLogsForExport
        Pageable singlePageRequest = PageRequest.of(0, 1);
        
        when(systemLogRepository.countByTimeRange(eq(from), eq(to)))
                .thenReturn(100L);
        
        when(auditLogRepository.countByTimeRange(eq(from), eq(to)))
                .thenReturn(200L);
        
        when(errorLogRepository.countByTimeRange(eq(from), eq(to)))
                .thenReturn(50L);
        
        when(accessLogRepository.countByTimeRange(eq(from), eq(to)))
                .thenReturn(150L);

        // Act
        LogExportResponseDto result = logManagementService.exportLogs(request);

        // Assert
        assertNotNull(result);
        assertEquals("csv", result.getFileFormat());
        assertNotNull(result.getFileName());
        assertTrue(result.getFileName().startsWith("logs_export_"));
        assertTrue(result.getFileName().endsWith(".csv"));
        assertNotNull(result.getDownloadToken());
        assertEquals(500, result.getTotalRecords());
    }
}