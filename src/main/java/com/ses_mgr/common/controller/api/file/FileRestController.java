package com.ses_mgr.common.controller.api.file;

import com.ses_mgr.common.dto.ApiResponseDto;
import com.ses_mgr.common.dto.file.FileMetadataDto;
import com.ses_mgr.common.dto.file.FileUploadRequestDto;
import com.ses_mgr.common.entity.User;
import com.ses_mgr.common.service.file.FileService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ファイル管理REST APIコントローラ
 */
@RestController
@RequestMapping("/api/v1/common/files")
public class FileRestController {

    private static final Logger logger = LoggerFactory.getLogger(FileRestController.class);

    private final FileService fileService;

    @Autowired
    public FileRestController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * ファイルをアップロードする
     *
     * @param file アップロードするファイル
     * @param fileType ファイルの種類
     * @param resourceId 関連リソースID
     * @param description ファイルの説明
     * @param tags タグ
     * @param isPublic 公開フラグ
     * @param expiryDate 有効期限
     * @param currentUser 現在のユーザー
     * @return アップロード結果
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("file_type") String fileType,
            @RequestParam(value = "resource_id", required = false) String resourceId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "is_public", required = false) Boolean isPublic,
            @RequestParam(value = "expiry_date", required = false) String expiryDate,
            @AuthenticationPrincipal User currentUser) {

        try {
            // リクエストDTOの作成
            FileUploadRequestDto uploadRequest = new FileUploadRequestDto();
            uploadRequest.setFile(file);
            uploadRequest.setFileType(fileType);
            uploadRequest.setResourceId(resourceId);
            uploadRequest.setDescription(description);
            uploadRequest.setTags(tags);
            uploadRequest.setIsPublic(isPublic);
            
            // 有効期限の変換（実際のアプリケーションでは適切な変換処理が必要）
            // ここでは簡略化のため省略
            
            // ファイルのアップロード
            FileMetadataDto metadata = fileService.uploadFile(
                    uploadRequest, 
                    currentUser.getUserId(), 
                    currentUser.getName()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success(metadata));
            
        } catch (IllegalArgumentException e) {
            logger.warn("ファイルアップロード中にバリデーションエラーが発生しました: {}", e.getMessage());
            
            if (e.getMessage().contains("サイズ")) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(ApiResponseDto.error("FILE_TOO_LARGE", e.getMessage()));
            } else if (e.getMessage().contains("形式")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body(ApiResponseDto.error("UNSUPPORTED_FILE_TYPE", e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDto.error("VALIDATION_ERROR", e.getMessage()));
            }
        } catch (Exception e) {
            logger.error("ファイルアップロード中にエラーが発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "ファイルアップロード中にエラーが発生しました。"));
        }
    }

    /**
     * ファイルをダウンロードする
     *
     * @param id ファイルID
     * @param download ダウンロードフラグ
     * @param currentUser 現在のユーザー
     * @return ファイルリソース
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> downloadFile(
            @PathVariable("id") String id,
            @RequestParam(value = "download", required = false, defaultValue = "false") boolean download,
            @AuthenticationPrincipal User currentUser) {

        try {
            UUID fileId = UUID.fromString(id);
            
            // メタデータを取得
            FileMetadataDto metadata = fileService.getFileMetadata(fileId, currentUser.getUserId());
            
            // ファイルリソースを取得
            Resource resource = fileService.downloadFile(fileId, currentUser.getUserId());
            
            // レスポンスヘッダーの設定
            String contentDisposition = download
                    ? "attachment; filename=\"" + metadata.getFileName() + "\""
                    : "inline; filename=\"" + metadata.getFileName() + "\"";
            
            // Content-Lengthヘッダーを設定
            long contentLength = 0;
            try {
                contentLength = resource.contentLength();
            } catch (IOException e) {
                logger.warn("ファイルサイズの取得に失敗しました: {}", e.getMessage());
            }
            
            // キャッシュ制御（公開ファイルの場合はキャッシュを許可）
            String cacheControl = metadata.getIsPublic()
                    ? "public, max-age=86400" // 1日
                    : "private, no-cache, no-store, must-revalidate";
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(metadata.getMimeType()))
                    .contentLength(contentLength)
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .header(HttpHeaders.CACHE_CONTROL, cacheControl)
                    .header(HttpHeaders.PRAGMA, metadata.getIsPublic() ? "" : "no-cache")
                    .header(HttpHeaders.EXPIRES, metadata.getIsPublic() ? "" : "0")
                    .body(resource);
            
        } catch (IllegalArgumentException e) {
            logger.warn("無効なファイルIDが指定されました: {}", id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error("INVALID_ID", "無効なファイルIDが指定されました。"));
        } catch (EntityNotFoundException e) {
            logger.warn("ファイルが見つかりません: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("RESOURCE_NOT_FOUND", "指定されたファイルが存在しません。"));
        } catch (AccessDeniedException e) {
            logger.warn("ファイルへのアクセスが拒否されました: {}", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponseDto.error("FORBIDDEN", "このファイルにアクセスする権限がありません。"));
        } catch (Exception e) {
            logger.error("ファイルのダウンロード中にエラーが発生しました: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "ファイルのダウンロード中にエラーが発生しました。"));
        }
    }

    /**
     * ファイルのメタデータを取得する
     *
     * @param id ファイルID
     * @param currentUser 現在のユーザー
     * @return ファイルメタデータ
     */
    @GetMapping("/metadata/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFileMetadata(
            @PathVariable("id") String id,
            @AuthenticationPrincipal User currentUser) {

        try {
            UUID fileId = UUID.fromString(id);
            
            // メタデータを取得
            FileMetadataDto metadata = fileService.getFileMetadata(fileId, currentUser.getUserId());
            
            // キャッシュ制御ヘッダー
            // メタデータは比較的頻繁に変更される可能性があるため、キャッシュ期間は短くする
            String cacheControl = "private, max-age=300"; // 5分
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, cacheControl)
                    .body(ApiResponseDto.success(metadata));
            
        } catch (IllegalArgumentException e) {
            logger.warn("無効なファイルIDが指定されました: {}", id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error("INVALID_ID", "無効なファイルIDが指定されました。"));
        } catch (EntityNotFoundException e) {
            logger.warn("ファイルが見つかりません: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("RESOURCE_NOT_FOUND", "指定されたファイルが存在しません。"));
        } catch (AccessDeniedException e) {
            logger.warn("ファイルへのアクセスが拒否されました: {}", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponseDto.error("FORBIDDEN", "このファイルにアクセスする権限がありません。"));
        } catch (Exception e) {
            logger.error("ファイルメタデータの取得中にエラーが発生しました: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "ファイルメタデータの取得中にエラーが発生しました。"));
        }
    }

    /**
     * ファイルのリスト/検索API
     *
     * @param fileType ファイルタイプでフィルタリング
     * @param resourceId リソースIDでフィルタリング
     * @param query 検索キーワード（ファイル名、タグに対して部分一致検索）
     * @param page ページ番号（0始まり）
     * @param size ページサイズ
     * @param sort ソート条件（例: created_at:desc）
     * @param currentUser 現在のユーザー
     * @return ファイルリスト
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> listFiles(
            @RequestParam(value = "file_type", required = false) String fileType,
            @RequestParam(value = "resource_id", required = false) String resourceId,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "created_at:desc") String sort,
            @AuthenticationPrincipal User currentUser) {
            
        // 注: このAPIはリポジトリ、サービスの実装が必要
        // 今回は未実装とし、機能追加時に実装する
        
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponseDto.error("NOT_IMPLEMENTED", "この機能は現在実装中です。"));
    }

    /**
     * ファイルを削除する
     *
     * @param id ファイルID
     * @param permanent 物理削除フラグ
     * @param currentUser 現在のユーザー
     * @return 削除結果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteFile(
            @PathVariable("id") String id,
            @RequestParam(value = "permanent", required = false, defaultValue = "false") boolean permanent,
            @AuthenticationPrincipal User currentUser) {

        try {
            UUID fileId = UUID.fromString(id);
            
            // 物理削除の場合は管理者権限が必要
            if (permanent) {
                // TODO: 管理者権限のチェック
                // ここでは簡略化のため省略
            }
            
            // ファイルの削除
            fileService.deleteFile(fileId, currentUser.getUserId(), permanent);
            
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            logger.warn("無効なファイルIDが指定されました: {}", id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error("INVALID_ID", "無効なファイルIDが指定されました。"));
        } catch (EntityNotFoundException e) {
            logger.warn("ファイルが見つかりません: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("RESOURCE_NOT_FOUND", "指定されたファイルが存在しません。"));
        } catch (AccessDeniedException e) {
            logger.warn("ファイルの削除が拒否されました: {}", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponseDto.error("FORBIDDEN", "このファイルを削除する権限がありません。"));
        } catch (Exception e) {
            logger.error("ファイルの削除中にエラーが発生しました: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "ファイルの削除中にエラーが発生しました。"));
        }
    }
}