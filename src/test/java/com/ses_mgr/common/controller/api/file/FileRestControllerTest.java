package com.ses_mgr.common.controller.api.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.common.dto.file.FileMetadataDto;
import com.ses_mgr.common.entity.User;
import com.ses_mgr.common.service.file.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FileRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileRestController fileRestController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private UUID fileId;
    private UUID userId;
    private User mockUser;
    private FileMetadataDto fileMetadataDto;
    private MockMultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(fileRestController).build();

        // テスト用ID
        fileId = UUID.randomUUID();
        userId = UUID.randomUUID();

        // テスト用ユーザー
        mockUser = User.builder()
                .userId(userId)
                .name("Test User")
                .build();

        // テスト用メタデータ
        fileMetadataDto = new FileMetadataDto();
        fileMetadataDto.setId(fileId);
        fileMetadataDto.setFileName("test.txt");
        fileMetadataDto.setFileType("other");
        fileMetadataDto.setMimeType("text/plain");
        fileMetadataDto.setSize(18L);
        fileMetadataDto.setDescription("Test description");
        fileMetadataDto.setTags(Arrays.asList("test", "file"));
        fileMetadataDto.setIsPublic(false);
        fileMetadataDto.setDownloadUrl("/api/v1/common/files/" + fileId);
        fileMetadataDto.setDownloadCount(0);
        fileMetadataDto.setCreatedAt(LocalDateTime.now());
        fileMetadataDto.setUpdatedAt(LocalDateTime.now());
        fileMetadataDto.setSha256Hash("testhash");

        FileMetadataDto.UserInfoDto userInfo = new FileMetadataDto.UserInfoDto();
        userInfo.setId(userId);
        userInfo.setName("Test User");
        fileMetadataDto.setCreatedBy(userInfo);

        // マルチパートファイル
        multipartFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "This is a test file".getBytes()
        );
    }

    @Test
    void getFileMetadata_ShouldReturnMetadata() throws Exception {
        // モックの設定
        when(fileService.getFileMetadata(fileId, userId)).thenReturn(fileMetadataDto);

        // テスト実行と検証
        mockMvc.perform(get("/api/v1/common/files/metadata/" + fileId)
                .principal(() -> mockUser.getLoginId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(fileId.toString()))
                .andExpect(jsonPath("$.data.file_name").value("test.txt"))
                .andExpect(jsonPath("$.data.file_type").value("other"))
                .andExpect(jsonPath("$.data.mime_type").value("text/plain"))
                .andExpect(jsonPath("$.data.size").value(18));
    }

    @Test
    void uploadFile_ShouldReturnMetadata() throws Exception {
        // モックの設定
        when(fileService.uploadFile(any(), eq(userId), eq("Test User"))).thenReturn(fileMetadataDto);

        // テスト実行と検証
        mockMvc.perform(multipart("/api/v1/common/files/upload")
                .file(multipartFile)
                .param("file_type", "other")
                .param("description", "Test description")
                .param("tags", "test,file")
                .param("is_public", "false")
                .principal(() -> mockUser.getLoginId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(fileId.toString()))
                .andExpect(jsonPath("$.data.file_name").value("test.txt"))
                .andExpect(jsonPath("$.data.file_type").value("other"));
    }

    @Test
    void downloadFile_ShouldReturnFileResource() throws Exception {
        // モックリソース
        Resource mockResource = mock(Resource.class);

        // モックの設定
        when(fileService.getFileMetadata(fileId, userId)).thenReturn(fileMetadataDto);
        when(fileService.downloadFile(fileId, userId)).thenReturn(mockResource);

        // テスト実行と検証
        mockMvc.perform(get("/api/v1/common/files/" + fileId)
                .principal(() -> mockUser.getLoginId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/plain"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"test.txt\""));
    }

    @Test
    void deleteFile_ShouldReturnNoContent() throws Exception {
        // テスト実行と検証
        mockMvc.perform(delete("/api/v1/common/files/" + fileId)
                .principal(() -> mockUser.getLoginId()))
                .andExpect(status().isNoContent());
    }
}