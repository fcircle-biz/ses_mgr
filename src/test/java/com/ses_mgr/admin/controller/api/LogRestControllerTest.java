package com.ses_mgr.admin.controller.api;

import com.ses_mgr.admin.dto.*;
import com.ses_mgr.admin.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LogRestController.class)
public class LogRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LogService logService;

    private LocalDateTime testTimestamp;
    private DateTimeFormatter formatter;

    @BeforeEach
    public void setup() {
        testTimestamp = LocalDateTime.now();
        formatter = DateTimeFormatter.ISO_DATE_TIME;
    }

    @Test
    @WithMockUser(authorities = {"system.logs.read"})
    public void getSystemLogs_ShouldReturnSystemLogs() throws Exception {
        // Arrange
        List<SystemLogDto> systemLogs = new ArrayList<>();
        SystemLogDto logDto = new SystemLogDto();
        logDto.setId(1L);
        logDto.setType("system");
        logDto.setLevel("info");
        logDto.setMessage("Test system log");
        logDto.setTimestamp(testTimestamp);
        logDto.setModule("TestModule");
        systemLogs.add(logDto);

        when(logService.getSystemLogs(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(),
                any(),
                any(),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(systemLogs, PageRequest.of(0, 10), 1));

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/logs/system")
                .param("from", testTimestamp.minusDays(1).format(formatter))
                .param("to", testTimestamp.format(formatter))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content[0].type").value("system"))
                .andExpect(jsonPath("$.data.content[0].level").value("info"))
                .andExpect(jsonPath("$.data.content[0].message").value("Test system log"))
                .andExpect(jsonPath("$.data.content[0].module").value("TestModule"));
    }

    @Test
    @WithMockUser(authorities = {"system.logs.read"})
    public void getAuditLogs_ShouldReturnAuditLogs() throws Exception {
        // Arrange
        List<AuditLogDto> auditLogs = new ArrayList<>();
        AuditLogDto logDto = new AuditLogDto();
        logDto.setId(1L);
        logDto.setType("audit");
        logDto.setLevel("info");
        logDto.setMessage("Test audit log");
        logDto.setTimestamp(testTimestamp);
        logDto.setAction("create");
        logDto.setResourceType("user");
        auditLogs.add(logDto);

        when(logService.getAuditLogs(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(auditLogs, PageRequest.of(0, 10), 1));

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/logs/audit")
                .param("from", testTimestamp.minusDays(1).format(formatter))
                .param("to", testTimestamp.format(formatter))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content[0].type").value("audit"))
                .andExpect(jsonPath("$.data.content[0].level").value("info"))
                .andExpect(jsonPath("$.data.content[0].message").value("Test audit log"))
                .andExpect(jsonPath("$.data.content[0].action").value("create"))
                .andExpect(jsonPath("$.data.content[0].resourceType").value("user"));
    }

    @Test
    @WithMockUser(authorities = {"system.logs.read"})
    public void getErrorLogs_ShouldReturnErrorLogs() throws Exception {
        // Arrange
        List<ErrorLogDto> errorLogs = new ArrayList<>();
        ErrorLogDto logDto = new ErrorLogDto();
        logDto.setId(1L);
        logDto.setType("error");
        logDto.setLevel("error");
        logDto.setMessage("Test error log");
        logDto.setTimestamp(testTimestamp);
        logDto.setModule("TestModule");
        logDto.setErrorCode("E001");
        errorLogs.add(logDto);

        when(logService.getErrorLogs(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(),
                any(),
                any(),
                any(),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(errorLogs, PageRequest.of(0, 10), 1));

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/logs/error")
                .param("from", testTimestamp.minusDays(1).format(formatter))
                .param("to", testTimestamp.format(formatter))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content[0].type").value("error"))
                .andExpect(jsonPath("$.data.content[0].level").value("error"))
                .andExpect(jsonPath("$.data.content[0].message").value("Test error log"))
                .andExpect(jsonPath("$.data.content[0].module").value("TestModule"))
                .andExpect(jsonPath("$.data.content[0].errorCode").value("E001"));
    }

    @Test
    @WithMockUser(authorities = {"system.logs.read"})
    public void getAccessLogs_ShouldReturnAccessLogs() throws Exception {
        // Arrange
        List<AccessLogDto> accessLogs = new ArrayList<>();
        AccessLogDto logDto = new AccessLogDto();
        logDto.setId(1L);
        logDto.setType("access");
        logDto.setLevel("info");
        logDto.setMessage("Test access log");
        logDto.setTimestamp(testTimestamp);
        logDto.setAction("login");
        logDto.setStatus("success");
        accessLogs.add(logDto);

        when(logService.getAccessLogs(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(accessLogs, PageRequest.of(0, 10), 1));

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/logs/access")
                .param("from", testTimestamp.minusDays(1).format(formatter))
                .param("to", testTimestamp.format(formatter))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content[0].type").value("access"))
                .andExpect(jsonPath("$.data.content[0].level").value("info"))
                .andExpect(jsonPath("$.data.content[0].message").value("Test access log"))
                .andExpect(jsonPath("$.data.content[0].action").value("login"))
                .andExpect(jsonPath("$.data.content[0].status").value("success"));
    }

    @Test
    @WithMockUser(authorities = {"system.logs.read"})
    public void searchLogs_ShouldReturnSearchResults() throws Exception {
        // Arrange
        List<BaseLogDto> searchResults = new ArrayList<>();
        SystemLogDto systemLogDto = new SystemLogDto();
        systemLogDto.setId(1L);
        systemLogDto.setType("system");
        systemLogDto.setLevel("info");
        systemLogDto.setMessage("Test system log");
        systemLogDto.setTimestamp(testTimestamp);
        searchResults.add(systemLogDto);

        when(logService.searchLogs(any(LogSearchRequestDto.class)))
                .thenReturn(new PageImpl<>(searchResults, PageRequest.of(0, 10), 1));

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/logs/search")
                .param("from", testTimestamp.minusDays(1).format(formatter))
                .param("to", testTimestamp.format(formatter))
                .param("search", "test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content[0].type").value("system"))
                .andExpect(jsonPath("$.data.content[0].level").value("info"))
                .andExpect(jsonPath("$.data.content[0].message").value("Test system log"));
    }

    @Test
    @WithMockUser(authorities = {"system.logs.read"})
    public void getLogStatistics_ShouldReturnStatistics() throws Exception {
        // Arrange
        LogStatisticsResponseDto statisticsResponse = new LogStatisticsResponseDto();
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCount", 100);
        summary.put("byType", Map.of(
                "system", 30,
                "audit", 40,
                "error", 20,
                "access", 10
        ));
        summary.put("byLevel", Map.of(
                "info", 60,
                "warning", 20,
                "error", 15,
                "critical", 5
        ));
        statisticsResponse.setSummary(summary);

        when(logService.getLogStatistics(any(LogStatisticsRequestDto.class)))
                .thenReturn(statisticsResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/logs/statistics")
                .param("from", testTimestamp.minusDays(7).format(formatter))
                .param("to", testTimestamp.format(formatter))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.summary.totalCount").value(100))
                .andExpect(jsonPath("$.data.summary.byType.system").value(30))
                .andExpect(jsonPath("$.data.summary.byType.audit").value(40))
                .andExpect(jsonPath("$.data.summary.byLevel.info").value(60))
                .andExpect(jsonPath("$.data.summary.byLevel.error").value(15));
    }

    @Test
    @WithMockUser(authorities = {"system.logs.export"})
    public void exportLogs_ShouldStartExport() throws Exception {
        // Arrange
        LogExportResponseDto exportResponse = new LogExportResponseDto();
        exportResponse.setFileFormat("csv");
        exportResponse.setFileName("logs_export_20230101-120000.csv");
        exportResponse.setFileUrl("/api/v1/admin/logs/download/abc123");
        exportResponse.setDownloadToken("abc123");
        exportResponse.setTotalRecords(100);

        when(logService.exportLogs(any(LogExportRequestDto.class)))
                .thenReturn(exportResponse);

        String requestJson = "{"
                + "\"from\": \"" + testTimestamp.minusDays(7).format(formatter) + "\","
                + "\"to\": \"" + testTimestamp.format(formatter) + "\","
                + "\"format\": \"csv\","
                + "\"encoding\": \"UTF-8\","
                + "\"includeDetails\": true"
                + "}";

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/logs/export")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.fileFormat").value("csv"))
                .andExpect(jsonPath("$.data.fileName").value("logs_export_20230101-120000.csv"))
                .andExpect(jsonPath("$.data.downloadToken").value("abc123"))
                .andExpect(jsonPath("$.data.totalRecords").value(100));
    }
}