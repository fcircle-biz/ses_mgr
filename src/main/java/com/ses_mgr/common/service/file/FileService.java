package com.ses_mgr.common.service.file;

import com.ses_mgr.common.dto.file.FileMetadataDto;
import com.ses_mgr.common.dto.file.FileUploadRequestDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * ファイル管理サービスインターフェース
 */
public interface FileService {

    /**
     * ファイルをアップロードする
     *
     * @param uploadRequest アップロードリクエスト
     * @param userId ユーザーID
     * @param userName ユーザー名
     * @return アップロードされたファイルのメタデータ
     */
    FileMetadataDto uploadFile(FileUploadRequestDto uploadRequest, UUID userId, String userName);

    /**
     * ファイルをダウンロードする
     *
     * @param fileId ファイルID
     * @param userId ユーザーID
     * @return ファイルリソース
     */
    Resource downloadFile(UUID fileId, UUID userId);

    /**
     * ファイルのメタデータを取得する
     *
     * @param fileId ファイルID
     * @param userId ユーザーID
     * @return ファイルメタデータ
     */
    FileMetadataDto getFileMetadata(UUID fileId, UUID userId);

    /**
     * ファイルを削除する
     *
     * @param fileId ファイルID
     * @param userId ユーザーID
     * @param permanent 物理削除フラグ
     */
    void deleteFile(UUID fileId, UUID userId, boolean permanent);

    /**
     * ファイルのアクセス権限をチェックする
     *
     * @param fileId ファイルID
     * @param userId ユーザーID
     * @return アクセス権限があればtrue
     */
    boolean hasAccessPermission(UUID fileId, UUID userId);

    /**
     * ファイルのダウンロードカウントを増やす
     *
     * @param fileId ファイルID
     */
    void incrementDownloadCount(UUID fileId);

    /**
     * 期限切れファイルの削除処理（バッチ処理用）
     */
    void cleanupExpiredFiles();

    /**
     * 指定されたユーザーの使用容量を取得する
     *
     * @param userId ユーザーID
     * @return 使用容量（バイト）
     */
    long getUserStorageUsage(UUID userId);

    /**
     * ユーザーがストレージ容量制限に達していないかチェックする
     *
     * @param userId ユーザーID
     * @param additionalSize 追加サイズ（バイト）
     * @return 制限に達していなければtrue
     */
    boolean checkStorageQuota(UUID userId, long additionalSize);
}