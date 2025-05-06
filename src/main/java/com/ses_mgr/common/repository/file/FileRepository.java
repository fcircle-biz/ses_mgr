package com.ses_mgr.common.repository.file;

import com.ses_mgr.common.entity.file.FileEntity;
import com.ses_mgr.common.entity.file.FileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ファイルリポジトリインターフェース
 */
@Repository
public interface FileRepository extends JpaRepository<FileEntity, UUID> {

    /**
     * 論理削除フラグが立っていないファイルを取得
     *
     * @param fileId ファイルID
     * @return ファイルエンティティ
     */
    @Query("SELECT f FROM FileEntity f WHERE f.fileId = :fileId AND f.isDeleted = false")
    Optional<FileEntity> findByIdAndNotDeleted(@Param("fileId") UUID fileId);

    /**
     * ファイルIDと作成者IDでファイルを取得
     *
     * @param fileId ファイルID
     * @param createdBy 作成者ID
     * @return ファイルエンティティ
     */
    Optional<FileEntity> findByFileIdAndCreatedByAndIsDeletedFalse(UUID fileId, UUID createdBy);

    /**
     * ファイルIDとリソースIDでファイルを取得
     *
     * @param fileId ファイルID
     * @param resourceId リソースID
     * @return ファイルエンティティ
     */
    Optional<FileEntity> findByFileIdAndResourceIdAndIsDeletedFalse(UUID fileId, String resourceId);

    /**
     * リソースIDとファイルタイプでファイルを検索
     *
     * @param resourceId リソースID
     * @param fileType ファイルタイプ
     * @param pageable ページネーション情報
     * @return ファイルエンティティのページ
     */
    Page<FileEntity> findByResourceIdAndFileTypeAndIsDeletedFalse(String resourceId, FileType fileType, Pageable pageable);

    /**
     * リソースIDでファイルを検索
     *
     * @param resourceId リソースID
     * @param pageable ページネーション情報
     * @return ファイルエンティティのページ
     */
    Page<FileEntity> findByResourceIdAndIsDeletedFalse(String resourceId, Pageable pageable);

    /**
     * 作成者IDでファイルを検索
     *
     * @param createdBy 作成者ID
     * @param pageable ページネーション情報
     * @return ファイルエンティティのページ
     */
    Page<FileEntity> findByCreatedByAndIsDeletedFalse(UUID createdBy, Pageable pageable);

    /**
     * ファイルタイプでファイルを検索
     *
     * @param fileType ファイルタイプ
     * @param pageable ページネーション情報
     * @return ファイルエンティティのページ
     */
    Page<FileEntity> findByFileTypeAndIsDeletedFalse(FileType fileType, Pageable pageable);

    /**
     * 公開ファイルを検索
     *
     * @param pageable ページネーション情報
     * @return ファイルエンティティのページ
     */
    Page<FileEntity> findByIsPublicTrueAndIsDeletedFalse(Pageable pageable);

    /**
     * タグで検索（部分一致）
     *
     * @param tag タグ（部分一致）
     * @param pageable ページネーション情報
     * @return ファイルエンティティのページ
     */
    @Query("SELECT f FROM FileEntity f WHERE f.tags LIKE %:tag% AND f.isDeleted = false")
    Page<FileEntity> findByTagsContainingAndIsDeletedFalse(@Param("tag") String tag, Pageable pageable);

    /**
     * ファイル名で検索（部分一致）
     *
     * @param fileName ファイル名（部分一致）
     * @param pageable ページネーション情報
     * @return ファイルエンティティのページ
     */
    Page<FileEntity> findByFileNameContainingAndIsDeletedFalse(String fileName, Pageable pageable);

    /**
     * 有効期限が切れたファイルを検索
     *
     * @param now 現在時刻
     * @return ファイルエンティティのリスト
     */
    List<FileEntity> findByExpiryDateLessThanAndIsDeletedFalse(LocalDateTime now);

    /**
     * 論理削除を実行
     *
     * @param fileId ファイルID
     * @param deletedAt 削除日時
     * @return 更新件数
     */
    @Modifying
    @Query("UPDATE FileEntity f SET f.isDeleted = true, f.deletedAt = :deletedAt WHERE f.fileId = :fileId")
    int softDeleteById(@Param("fileId") UUID fileId, @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * ユーザーのファイル総サイズを取得
     *
     * @param createdBy 作成者ID
     * @return ファイルの総サイズ（バイト）
     */
    @Query("SELECT SUM(f.size) FROM FileEntity f WHERE f.createdBy = :createdBy AND f.isDeleted = false")
    Long sumSizeByCreatedBy(@Param("createdBy") UUID createdBy);

    /**
     * ユーザーが作成した特定のタイプのファイル数を取得
     *
     * @param createdBy 作成者ID
     * @param fileType ファイルタイプ
     * @return ファイル数
     */
    long countByCreatedByAndFileTypeAndIsDeletedFalse(UUID createdBy, FileType fileType);

    /**
     * リソースに紐づくファイル数を取得
     *
     * @param resourceId リソースID
     * @return ファイル数
     */
    long countByResourceIdAndIsDeletedFalse(String resourceId);
}