package com.ses_mgr.common.service.file.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.common.dto.file.FileMetadataDto;
import com.ses_mgr.common.dto.file.FileUploadRequestDto;
import com.ses_mgr.common.entity.file.FileEntity;
import com.ses_mgr.common.entity.file.FileType;
import com.ses_mgr.common.repository.file.FileRepository;
import com.ses_mgr.common.service.file.FileService;
import com.ses_mgr.common.service.file.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ファイル管理サービス実装クラス
 */
@Service
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    private final FileRepository fileRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    @Value("${file.storage.max.per.user:1073741824}") // 1GB
    private long maxStoragePerUser;

    @Value("${file.api.base.url:/api/v1/common/files}")
    private String fileApiBaseUrl;

    @Autowired
    public FileServiceImpl(FileRepository fileRepository, FileStorageService fileStorageService, ObjectMapper objectMapper) {
        this.fileRepository = fileRepository;
        this.fileStorageService = fileStorageService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public FileMetadataDto uploadFile(FileUploadRequestDto uploadRequest, UUID userId, String userName) {
        MultipartFile file = uploadRequest.getFile();
        
        // ファイルの検証
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("ファイルが空です");
        }
        
        // ファイルタイプの検証
        if (uploadRequest.getFileType() == null || uploadRequest.getFileType().trim().isEmpty()) {
            throw new IllegalArgumentException("ファイルタイプは必須です");
        }
        
        // ファイルの検証
        if (!fileStorageService.validateFile(file)) {
            throw new IllegalArgumentException("ファイルの検証に失敗しました");
        }
        
        // MIMEタイプの検証
        if (!fileStorageService.isSupportedMimeType(file)) {
            throw new IllegalArgumentException("このファイル形式はサポートされていません");
        }
        
        // ファイルサイズの検証
        if (!fileStorageService.checkFileSizeLimit(file)) {
            throw new IllegalArgumentException("ファイルサイズが上限を超えています");
        }
        
        // ストレージ容量の検証
        if (!checkStorageQuota(userId, file.getSize())) {
            throw new IllegalArgumentException("ストレージ容量の上限に達しています");
        }
        
        // ファイルIDの生成
        UUID fileId = UUID.randomUUID();
        
        // ファイルの保存
        String storagePath = fileStorageService.storeFile(file, fileId);
        
        // ハッシュ値の計算
        String sha256Hash = fileStorageService.calculateSha256Hash(file);
        
        // エンティティの作成
        FileEntity fileEntity = FileEntity.builder()
                .fileId(fileId)
                .fileName(file.getOriginalFilename())
                .fileType(FileType.fromString(uploadRequest.getFileType()))
                .mimeType(file.getContentType())
                .size(file.getSize())
                .description(uploadRequest.getDescription())
                .tags(uploadRequest.getTags())
                .resourceId(uploadRequest.getResourceId())
                .storagePath(storagePath)
                .isPublic(uploadRequest.getIsPublic() != null ? uploadRequest.getIsPublic() : false)
                .downloadCount(0)
                .sha256Hash(sha256Hash)
                .expiryDate(uploadRequest.getExpiryDate())
                .isDeleted(false)
                .createdBy(userId)
                .createdByName(userName)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // エンティティの保存
        FileEntity savedEntity = fileRepository.save(fileEntity);
        
        // DTOの作成と返却
        return createFileMetadataDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadFile(UUID fileId, UUID userId) {
        // ファイルエンティティの取得
        FileEntity fileEntity = fileRepository.findByIdAndNotDeleted(fileId)
                .orElseThrow(() -> new EntityNotFoundException("ファイルが見つかりません: " + fileId));
        
        // アクセス権限のチェック
        if (!hasAccessPermission(fileId, userId)) {
            throw new AccessDeniedException("このファイルにアクセスする権限がありません");
        }
        
        // ダウンロードカウントの更新（非同期で行うべき）
        incrementDownloadCount(fileId);
        
        // ファイルの取得
        return fileStorageService.loadFileAsResource(fileEntity.getStoragePath());
    }

    @Override
    @Transactional(readOnly = true)
    public FileMetadataDto getFileMetadata(UUID fileId, UUID userId) {
        // ファイルエンティティの取得
        FileEntity fileEntity = fileRepository.findByIdAndNotDeleted(fileId)
                .orElseThrow(() -> new EntityNotFoundException("ファイルが見つかりません: " + fileId));
        
        // アクセス権限のチェック
        if (!hasAccessPermission(fileId, userId)) {
            throw new AccessDeniedException("このファイルにアクセスする権限がありません");
        }
        
        // DTOの作成と返却
        return createFileMetadataDto(fileEntity);
    }

    @Override
    @Transactional
    public void deleteFile(UUID fileId, UUID userId, boolean permanent) {
        // ファイルエンティティの取得
        FileEntity fileEntity = fileRepository.findByIdAndNotDeleted(fileId)
                .orElseThrow(() -> new EntityNotFoundException("ファイルが見つかりません: " + fileId));
        
        // アクセス権限のチェック
        if (!hasAccessPermission(fileId, userId)) {
            throw new AccessDeniedException("このファイルを削除する権限がありません");
        }
        
        // 処理時刻
        LocalDateTime now = LocalDateTime.now();
        
        if (permanent) {
            // 物理削除の前にバックアップを取るべき（実際の実装ではバックアップ処理を追加）
            logger.info("ファイルの物理削除を実行します: {}", fileId);
            
            try {
                // 物理削除
                boolean deleted = fileStorageService.deleteFile(fileEntity.getStoragePath());
                if (!deleted) {
                    logger.warn("ファイルの物理削除に失敗しました: {}", fileId);
                    // 論理削除だけ実行
                    fileEntity.setIsDeleted(true);
                    fileEntity.setDeletedAt(now);
                    fileRepository.save(fileEntity);
                    throw new RuntimeException("ファイルの物理削除に失敗しました。論理削除のみ実行しました。");
                }
                
                // エンティティの削除
                fileRepository.delete(fileEntity);
                logger.info("ファイルの物理削除が完了しました: {}", fileId);
            } catch (Exception e) {
                logger.error("ファイル削除処理中にエラーが発生しました: {}", fileId, e);
                throw new RuntimeException("ファイル削除処理中にエラーが発生しました", e);
            }
        } else {
            // 論理削除
            try {
                int updated = fileRepository.softDeleteById(fileId, now);
                if (updated == 0) {
                    logger.warn("ファイルの論理削除に失敗しました: {}", fileId);
                    throw new RuntimeException("ファイルの論理削除に失敗しました");
                }
                logger.info("ファイルの論理削除が完了しました: {}", fileId);
            } catch (Exception e) {
                logger.error("ファイルの論理削除中にエラーが発生しました: {}", fileId, e);
                throw new RuntimeException("ファイルの論理削除中にエラーが発生しました", e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAccessPermission(UUID fileId, UUID userId) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("ファイルが見つかりません: " + fileId));
        
        // 公開ファイルの場合は誰でもアクセス可能
        if (fileEntity.getIsPublic()) {
            return true;
        }
        
        // ファイルの作成者はアクセス可能
        if (fileEntity.getCreatedBy().equals(userId)) {
            return true;
        }
        
        // TODO: リソースIDに基づくアクセス権限の確認
        // 実際のアプリケーションではリソースタイプごとに適切なアクセス権限チェックを行う
        
        // 管理者の場合は全てのファイルにアクセス可能
        // TODO: 管理者権限のチェック
        
        return false;
    }

    @Override
    @Transactional
    public void incrementDownloadCount(UUID fileId) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("ファイルが見つかりません: " + fileId));
        
        fileEntity.setDownloadCount(fileEntity.getDownloadCount() + 1);
        fileEntity.setUpdatedAt(LocalDateTime.now());
        
        fileRepository.save(fileEntity);
    }

    @Override
    @Transactional
    public void cleanupExpiredFiles() {
        LocalDateTime now = LocalDateTime.now();
        List<FileEntity> expiredFiles = fileRepository.findByExpiryDateLessThanAndIsDeletedFalse(now);
        
        for (FileEntity file : expiredFiles) {
            try {
                // 論理削除
                fileRepository.softDeleteById(file.getFileId(), now);
                logger.info("期限切れファイルを論理削除しました: {}", file.getFileId());
            } catch (Exception e) {
                logger.error("期限切れファイルの削除に失敗しました: {}", file.getFileId(), e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getUserStorageUsage(UUID userId) {
        Long usage = fileRepository.sumSizeByCreatedBy(userId);
        return usage != null ? usage : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkStorageQuota(UUID userId, long additionalSize) {
        long currentUsage = getUserStorageUsage(userId);
        return (currentUsage + additionalSize) <= maxStoragePerUser;
    }

    /**
     * FileEntityからFileMetadataDtoを作成する
     *
     * @param entity ファイルエンティティ
     * @return ファイルメタデータDTO
     */
    private FileMetadataDto createFileMetadataDto(FileEntity entity) {
        FileMetadataDto dto = new FileMetadataDto();
        dto.setId(entity.getFileId());
        dto.setFileName(entity.getFileName());
        dto.setFileType(entity.getFileType().getValue());
        dto.setMimeType(entity.getMimeType());
        dto.setSize(entity.getSize());
        dto.setDescription(entity.getDescription());
        dto.setTags(FileMetadataDto.tagsToList(entity.getTags()));
        dto.setResourceId(entity.getResourceId());
        dto.setResourceType(entity.getResourceType());
        dto.setIsPublic(entity.getIsPublic());
        dto.setDownloadUrl(fileApiBaseUrl + "/" + entity.getFileId());
        dto.setDownloadCount(entity.getDownloadCount());
        dto.setExpiryDate(entity.getExpiryDate());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setSha256Hash(entity.getSha256Hash());
        
        // 作成者情報
        FileMetadataDto.UserInfoDto userInfo = new FileMetadataDto.UserInfoDto();
        userInfo.setId(entity.getCreatedBy());
        userInfo.setName(entity.getCreatedByName());
        dto.setCreatedBy(userInfo);
        
        return dto;
    }
}