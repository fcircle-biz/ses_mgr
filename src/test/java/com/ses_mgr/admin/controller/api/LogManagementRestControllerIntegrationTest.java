package com.ses_mgr.admin.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.admin.dto.LogExportRequestDto;
import com.ses_mgr.admin.entity.SystemLog;
import com.ses_mgr.admin.repository.SystemLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LogManagementRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SystemLogRepository systemLogRepository;

    private LocalDateTime testTime;
    private DateTimeFormatter formatter;

    @BeforeEach
    public void setup() {
        testTime = LocalDateTime.now();
        formatter = DateTimeFormatter.ISO_DATE_TIME;
        
        // テストデータを作成
        createTestData();
    }

    private void createTestData() {
        // システムログのテストデータ
        SystemLog systemLog1 = new SystemLog();
        systemLog1.setLogType("system");
        systemLog1.setLevel("info");
        systemLog1.setMessage("Test system log 1");
        systemLog1.setModule("TestModule");
        systemLog1.setFunction("testFunction");
        systemLog1.setTimestamp(testTime.minusDays(1));
        
        Map<String, Object> details1 = new HashMap<>();
        details1.put("key1", "value1");
        details1.put("key2", 123);
        systemLog1.setDetails(details1);
        
        SystemLog systemLog2 = new SystemLog();
        systemLog2.setLogType("system");
        systemLog2.setLevel("warning");
        systemLog2.setMessage("Test system log 2");
        systemLog2.setModule("TestModule");
        systemLog2.setFunction("testFunction");
        systemLog2.setTimestamp(testTime.minusHours(12));
        
        Map<String, Object> details2 = new HashMap<>();
        details2.put("key1", "value2");
        details2.put("key2", 456);
        systemLog2.setDetails(details2);
        
        SystemLog systemLog3 = new SystemLog();
        systemLog3.setLogType("system");
        systemLog3.setLevel("error");
        systemLog3.setMessage("Test system log 3");
        systemLog3.setModule("OtherModule");
        systemLog3.setFunction("otherFunction");
        systemLog3.setTimestamp(testTime.minusHours(6));
        
        Map<String, Object> details3 = new HashMap<>();
        details3.put("key1", "value3");
        details3.put("key2", 789);
        systemLog3.setDetails(details3);
        
        // 保存
        systemLogRepository.save(systemLog1);
        systemLogRepository.save(systemLog2);
        systemLogRepository.save(systemLog3);
    }

    @Test
    @WithMockUser(authorities = {"system.logs.read"})
    public void getSystemLogs_ShouldReturnSystemLogs() throws Exception {
        // Arrange
        String fromDate = testTime.minusDays(2).format(formatter);
        String toDate = testTime.format(formatter);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/logs/system")
                .param("from", fromDate)
                .param("to", toDate)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(3))
                .andExpect(jsonPath("$.data.content[0].type").value("system"));
    }

    @Test
    @WithMockUser(authorities = {"system.logs.read"})
    public void getSystemLogs_WithLevelFilter_ShouldReturnFilteredLogs() throws Exception {
        // Arrange
        String fromDate = testTime.minusDays(2).format(formatter);
        String toDate = testTime.format(formatter);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/logs/system")
                .param("from", fromDate)
                .param("to", toDate)
                .param("level", "error")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].level").value("error"));
    }

    @Test
    @WithMockUser(authorities = {"system.logs.read"})
    public void getSystemLogs_WithModuleFilter_ShouldReturnFilteredLogs() throws Exception {
        // Arrange
        String fromDate = testTime.minusDays(2).format(formatter);
        String toDate = testTime.format(formatter);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/logs/system")
                .param("from", fromDate)
                .param("to", toDate)
                .param("module", "TestModule")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].module").value("TestModule"));
    }

    @Test
    @WithMockUser(authorities = {"system.logs.read"})
    public void getSystemLogs_WithSearch_ShouldReturnFilteredLogs() throws Exception {
        // Arrange
        String fromDate = testTime.minusDays(2).format(formatter);
        String toDate = testTime.format(formatter);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/logs/system")
                .param("from", fromDate)
                .param("to", toDate)
                .param("search", "log 2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].message").value("Test system log 2"));
    }

    @Test
    @WithMockUser(authorities = {"system.logs.read"})
    public void searchLogs_ShouldReturnSearchResults() throws Exception {
        // Arrange
        String fromDate = testTime.minusDays(2).format(formatter);
        String toDate = testTime.format(formatter);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/logs/search")
                .param("from", fromDate)
                .param("to", toDate)
                .param("search", "Test")
                .param("types", "system")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").isNumber());
    }

    @Test
    @WithMockUser(authorities = {"system.logs.read"})
    public void getLogStatistics_ShouldReturnStatistics() throws Exception {
        // Arrange
        String fromDate = testTime.minusDays(2).format(formatter);
        String toDate = testTime.format(formatter);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/logs/statistics")
                .param("from", fromDate)
                .param("to", toDate)
                .param("types", "system")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.summary").exists())
                .andExpect(jsonPath("$.data.summary.totalCount").isNumber())
                .andExpect(jsonPath("$.data.summary.byType").exists())
                .andExpect(jsonPath("$.data.summary.byLevel").exists());
    }

    @Test
    @WithMockUser(authorities = {"system.logs.export"})
    public void exportLogs_ShouldStartExport() throws Exception {
        // Arrange
        LogExportRequestDto exportRequest = new LogExportRequestDto();
        exportRequest.setFrom(testTime.minusDays(2));
        exportRequest.setTo(testTime);
        exportRequest.setFormat("csv");
        exportRequest.setEncoding("UTF-8");
        exportRequest.setIncludeDetails(true);
        
        String requestJson = objectMapper.writeValueAsString(exportRequest);

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/logs/export")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.fileFormat").value("csv"))
                .andExpect(jsonPath("$.data.fileName").exists())
                .andExpect(jsonPath("$.data.downloadToken").exists())
                .andExpect(jsonPath("$.data.totalRecords").isNumber());
    }
}