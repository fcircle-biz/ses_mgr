package com.ses_mgr.common.service.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * ファイルストレージサービスインターフェース
 * 実際のファイル保存と取得の処理を担当
 */
public interface FileStorageService {

    /**
     * ファイルを保存する
     *
     * @param file アップロードされたファイル
     * @param fileId ファイルID
     * @return 保存先のパス
     */
    String storeFile(MultipartFile file, UUID fileId);

    /**
     * ファイルを取得する
     *
     * @param storagePath ファイルの保存パス
     * @return ファイルリソース
     */
    Resource loadFileAsResource(String storagePath);

    /**
     * ファイルを削除する
     *
     * @param storagePath ファイルの保存パス
     * @return 削除に成功したらtrue
     */
    boolean deleteFile(String storagePath);

    /**
     * ファイルのSHA-256ハッシュを計算する
     *
     * @param file アップロードされたファイル
     * @return SHA-256ハッシュ文字列
     */
    String calculateSha256Hash(MultipartFile file);

    /**
     * ファイルの正当性を検証する
     *
     * @param file アップロードされたファイル
     * @return 検証に成功したらtrue
     */
    boolean validateFile(MultipartFile file);

    /**
     * ファイルサイズの制限を超えていないか確認する
     *
     * @param file アップロードされたファイル
     * @return 制限内ならtrue
     */
    boolean checkFileSizeLimit(MultipartFile file);

    /**
     * MIMEタイプをチェックする
     *
     * @param file アップロードされたファイル
     * @return サポートされているMIMEタイプならtrue
     */
    boolean isSupportedMimeType(MultipartFile file);
}