package com.ses_mgr.common.service.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.common.dto.file.FileMetadataDto;
import com.ses_mgr.common.dto.file.FileUploadRequestDto;
import com.ses_mgr.common.entity.file.FileEntity;
import com.ses_mgr.common.entity.file.FileType;
import com.ses_mgr.common.repository.file.FileRepository;
import com.ses_mgr.common.service.file.impl.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FileServiceImpl fileService;

    private UUID fileId;
    private UUID userId;
    private FileEntity fileEntity;
    private MultipartFile multipartFile;
    private FileUploadRequestDto uploadRequest;

    @BeforeEach
    void setUp() {
        // テスト用ファイルIDとユーザーID
        fileId = UUID.randomUUID();
        userId = UUID.randomUUID();

        // MockMultipartFile作成
        multipartFile = new MockMultipartFile(
                "testfile",
                "test.txt",
                "text/plain",
                "This is a test file".getBytes()
        );

        // テスト用のファイルエンティティを作成
        fileEntity = new FileEntity();
        fileEntity.setFileId(fileId);
        fileEntity.setFileName("test.txt");
        fileEntity.setFileType(FileType.OTHER);
        fileEntity.setMimeType("text/plain");
        fileEntity.setSize(18L); // "This is a test file"のサイズ
        fileEntity.setDescription("Test description");
        fileEntity.setTags("test,file");
        fileEntity.setStoragePath("test/path/test.txt");
        fileEntity.setIsPublic(false);
        fileEntity.setDownloadCount(0);
        fileEntity.setSha256Hash("testhash");
        fileEntity.setCreatedBy(userId);
        fileEntity.setCreatedByName("Test User");
        fileEntity.setCreatedAt(LocalDateTime.now());
        fileEntity.setUpdatedAt(LocalDateTime.now());
        fileEntity.setIsDeleted(false);

        // アップロードリクエスト作成
        uploadRequest = new FileUploadRequestDto();
        uploadRequest.setFile(multipartFile);
        uploadRequest.setFileType("other");
        uploadRequest.setDescription("Test description");
        uploadRequest.setTags("test,file");
        uploadRequest.setIsPublic(false);
    }

    @Test
    void uploadFile_ShouldReturnFileMetadata() {
        // maxStoragePerUserフィールドの値を設定（リフレクションを使用）
        try {
            java.lang.reflect.Field field = FileServiceImpl.class.getDeclaredField("maxStoragePerUser");
            field.setAccessible(true);
            field.set(fileService, 1073741824L); // 1GB
        } catch (Exception e) {
            fail("Failed to set maxStoragePerUser field: " + e.getMessage());
        }
        
        // モックの設定
        when(fileStorageService.validateFile(any(MultipartFile.class))).thenReturn(true);
        when(fileStorageService.isSupportedMimeType(any(MultipartFile.class))).thenReturn(true);
        when(fileStorageService.checkFileSizeLimit(any(MultipartFile.class))).thenReturn(true);
        when(fileStorageService.storeFile(any(MultipartFile.class), any(UUID.class))).thenReturn("test/path/test.txt");
        when(fileStorageService.calculateSha256Hash(any(MultipartFile.class))).thenReturn("testhash");
        when(fileRepository.save(any(FileEntity.class))).thenReturn(fileEntity);
        when(fileRepository.sumSizeByCreatedBy(userId)).thenReturn(0L); // ストレージ使用量0でモック
        
        // テスト実行
        FileMetadataDto result = fileService.uploadFile(uploadRequest, userId, "Test User");

        // 検証
        assertNotNull(result);
        assertEquals(fileId, result.getId());
        assertEquals("test.txt", result.getFileName());
        assertEquals("other", result.getFileType());
        assertEquals("text/plain", result.getMimeType());
        assertEquals(18L, result.getSize());
        assertEquals("Test description", result.getDescription());

        // モックメソッドの呼び出し検証
        verify(fileStorageService).validateFile(multipartFile);
        verify(fileStorageService).isSupportedMimeType(multipartFile);
        verify(fileStorageService).checkFileSizeLimit(multipartFile);
        verify(fileStorageService).storeFile(any(MultipartFile.class), any(UUID.class));
        verify(fileStorageService).calculateSha256Hash(multipartFile);
        verify(fileRepository).save(any(FileEntity.class));
    }

    @Test
    void getFileMetadata_ShouldReturnFileMetadata() {
        // モックの設定
        when(fileRepository.findByIdAndNotDeleted(fileId)).thenReturn(Optional.of(fileEntity));
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));

        // テスト実行
        FileMetadataDto result = fileService.getFileMetadata(fileId, userId);

        // 検証
        assertNotNull(result);
        assertEquals(fileId, result.getId());
        assertEquals("test.txt", result.getFileName());
        assertEquals("other", result.getFileType());
        assertEquals("text/plain", result.getMimeType());
        assertEquals(18L, result.getSize());
        assertEquals("Test description", result.getDescription());

        // モックメソッドの呼び出し検証
        verify(fileRepository).findByIdAndNotDeleted(fileId);
        verify(fileRepository).findById(fileId);
    }

    @Test
    void getFileMetadata_WhenFileNotFound_ShouldThrowException() {
        // モックの設定
        when(fileRepository.findByIdAndNotDeleted(fileId)).thenReturn(Optional.empty());
        // findByIdは呼ばれないはず - findByIdAndNotDeletedが空の場合にはそこで例外が発生するため

        // テスト実行＆検証
        assertThrows(EntityNotFoundException.class, () -> {
            fileService.getFileMetadata(fileId, userId);
        });

        // モックメソッドの呼び出し検証
        verify(fileRepository).findByIdAndNotDeleted(fileId);
        verify(fileRepository, never()).findById(fileId);
    }

    @Test
    void hasAccessPermission_WhenFileIsPublic_ShouldReturnTrue() {
        // ファイルを公開状態に設定
        fileEntity.setIsPublic(true);
        fileEntity.setCreatedBy(UUID.randomUUID()); // 作成者を別ユーザーに変更

        // モックの設定
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));

        // テスト実行
        boolean result = fileService.hasAccessPermission(fileId, userId);

        // 検証
        assertTrue(result);

        // モックメソッドの呼び出し検証
        verify(fileRepository).findById(fileId);
    }

    @Test
    void hasAccessPermission_WhenUserIsCreator_ShouldReturnTrue() {
        // 非公開ファイルにして、作成者をテスト用ユーザーIDに設定
        fileEntity.setIsPublic(false);
        fileEntity.setCreatedBy(userId);

        // モックの設定
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));

        // テスト実行
        boolean result = fileService.hasAccessPermission(fileId, userId);

        // 検証
        assertTrue(result);

        // モックメソッドの呼び出し検証
        verify(fileRepository).findById(fileId);
    }

    @Test
    void hasAccessPermission_WhenNoAccess_ShouldReturnFalse() {
        // 非公開ファイルにして、作成者を別ユーザーに設定
        fileEntity.setIsPublic(false);
        fileEntity.setCreatedBy(UUID.randomUUID());

        // モックの設定
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));

        // テスト実行
        boolean result = fileService.hasAccessPermission(fileId, userId);

        // 検証
        assertFalse(result);

        // モックメソッドの呼び出し検証
        verify(fileRepository).findById(fileId);
    }

    @Test
    void downloadFile_WhenUserHasAccess_ShouldReturnResource() {
        // モックの設定
        Resource mockResource = mock(Resource.class);
        
        when(fileRepository.findByIdAndNotDeleted(fileId)).thenReturn(Optional.of(fileEntity));
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));
        when(fileStorageService.loadFileAsResource(fileEntity.getStoragePath())).thenReturn(mockResource);
        when(fileRepository.save(any(FileEntity.class))).thenReturn(fileEntity);

        // テスト実行
        Resource result = fileService.downloadFile(fileId, userId);

        // 検証
        assertNotNull(result);
        assertSame(mockResource, result);

        // モックメソッドの呼び出し検証
        verify(fileRepository).findByIdAndNotDeleted(fileId);
        verify(fileRepository, times(2)).findById(fileId); // hasAccessPermissionとincrementDownloadCountで2回呼ばれる
        verify(fileStorageService).loadFileAsResource(fileEntity.getStoragePath());
        verify(fileRepository).save(any(FileEntity.class)); // ダウンロードカウントのインクリメント保存
    }

    @Test
    void downloadFile_WhenFileNotFound_ShouldThrowException() {
        // モックの設定
        when(fileRepository.findByIdAndNotDeleted(fileId)).thenReturn(Optional.empty());
        // findByIdは呼ばれないはず - findByIdAndNotDeletedが空の場合にはそこで例外が発生するため

        // テスト実行＆検証
        assertThrows(EntityNotFoundException.class, () -> {
            fileService.downloadFile(fileId, userId);
        });

        // モックメソッドの呼び出し検証
        verify(fileRepository).findByIdAndNotDeleted(fileId);
        verify(fileRepository, never()).findById(fileId);
        verify(fileStorageService, never()).loadFileAsResource(anyString());
        verify(fileRepository, never()).save(any(FileEntity.class));
    }

    @Test
    void deleteFile_WhenLogicalDelete_ShouldCallRepository() {
        // テスト用ファイルエンティティと設定
        when(fileRepository.findByIdAndNotDeleted(fileId)).thenReturn(Optional.of(fileEntity));
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));
        when(fileRepository.softDeleteById(eq(fileId), any(LocalDateTime.class))).thenReturn(1);

        // テスト実行
        fileService.deleteFile(fileId, userId, false);

        // モックメソッドの呼び出し検証
        verify(fileRepository).findByIdAndNotDeleted(fileId);
        verify(fileRepository).findById(fileId);
        verify(fileRepository).softDeleteById(eq(fileId), any(LocalDateTime.class));
        verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    void deleteFile_WhenPhysicalDelete_ShouldCallStorageService() {
        // テスト用ファイルエンティティと設定
        when(fileRepository.findByIdAndNotDeleted(fileId)).thenReturn(Optional.of(fileEntity));
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));
        when(fileStorageService.deleteFile(fileEntity.getStoragePath())).thenReturn(true);

        // テスト実行
        fileService.deleteFile(fileId, userId, true);

        // モックメソッドの呼び出し検証
        verify(fileRepository).findByIdAndNotDeleted(fileId);
        verify(fileRepository).findById(fileId);
        verify(fileStorageService).deleteFile(fileEntity.getStoragePath());
        verify(fileRepository).delete(fileEntity);
    }

    @Test
    void deleteFile_WhenNoAccess_ShouldThrowException() {
        // ファイルを非公開にして別ユーザーが作成したように設定
        fileEntity.setIsPublic(false);
        fileEntity.setCreatedBy(UUID.randomUUID());

        // モックの設定
        when(fileRepository.findByIdAndNotDeleted(fileId)).thenReturn(Optional.of(fileEntity));
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));

        // テスト実行＆検証
        assertThrows(AccessDeniedException.class, () -> {
            fileService.deleteFile(fileId, userId, false);
        });

        // モックメソッドの呼び出し検証
        verify(fileRepository).findByIdAndNotDeleted(fileId);
        verify(fileRepository).findById(fileId);
        verify(fileRepository, never()).softDeleteById(any(UUID.class), any(LocalDateTime.class));
        verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    void getUserStorageUsage_ShouldReturnUsage() {
        // モックの設定
        when(fileRepository.sumSizeByCreatedBy(userId)).thenReturn(1024L);

        // テスト実行
        long result = fileService.getUserStorageUsage(userId);

        // 検証
        assertEquals(1024L, result);

        // モックメソッドの呼び出し検証
        verify(fileRepository).sumSizeByCreatedBy(userId);
    }

    @Test
    void checkStorageQuota_WhenUnderLimit_ShouldReturnTrue() {
        // maxStoragePerUserフィールドの値を設定（リフレクションを使用）
        try {
            java.lang.reflect.Field field = FileServiceImpl.class.getDeclaredField("maxStoragePerUser");
            field.setAccessible(true);
            field.set(fileService, 1073741824L); // 1GB
        } catch (Exception e) {
            fail("Failed to set maxStoragePerUser field: " + e.getMessage());
        }
        
        // モックの設定
        when(fileRepository.sumSizeByCreatedBy(userId)).thenReturn(500L);

        // テスト実行
        boolean result = fileService.checkStorageQuota(userId, 500L);

        // 検証（1GB制限なので、合計1000バイトはOK）
        assertTrue(result);

        // モックメソッドの呼び出し検証
        verify(fileRepository).sumSizeByCreatedBy(userId);
    }

    @Test
    void checkStorageQuota_WhenOverLimit_ShouldReturnFalse() {
        // maxStoragePerUserフィールドの値を設定（リフレクションを使用）
        try {
            java.lang.reflect.Field field = FileServiceImpl.class.getDeclaredField("maxStoragePerUser");
            field.setAccessible(true);
            field.set(fileService, 1073741824L); // 1GB
        } catch (Exception e) {
            fail("Failed to set maxStoragePerUser field: " + e.getMessage());
        }
        
        // モックの設定（現在の使用量が1GB）
        when(fileRepository.sumSizeByCreatedBy(userId)).thenReturn(1073741824L);

        // テスト実行（さらに1バイト追加しようとする）
        boolean result = fileService.checkStorageQuota(userId, 1L);

        // 検証（制限オーバー）
        assertFalse(result);

        // モックメソッドの呼び出し検証
        verify(fileRepository).sumSizeByCreatedBy(userId);
    }
}