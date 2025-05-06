package com.ses_mgr.common.entity.file;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ファイルエンティティ
 * ファイルのメタデータを管理するエンティティクラス
 */
@Entity
@Table(name = "files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity {

    /**
     * ファイルID
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "file_id", columnDefinition = "UUID")
    private UUID fileId;

    /**
     * ファイル名
     */
    @Column(name = "file_name", nullable = false)
    private String fileName;

    /**
     * ファイルタイプ
     */
    @Column(name = "file_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private FileType fileType;

    /**
     * MIMEタイプ
     */
    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    /**
     * ファイルサイズ (bytes)
     */
    @Column(name = "size", nullable = false)
    private Long size;

    /**
     * ファイルの説明
     */
    @Column(name = "description")
    private String description;

    /**
     * ファイルのタグ (カンマ区切り)
     */
    @Column(name = "tags")
    private String tags;

    /**
     * 関連リソースID
     */
    @Column(name = "resource_id")
    private String resourceId;

    /**
     * 関連リソースタイプ
     */
    @Column(name = "resource_type")
    private String resourceType;

    /**
     * ファイルの格納パス
     */
    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    /**
     * ファイルの公開フラグ
     */
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    /**
     * ダウンロード回数
     */
    @Column(name = "download_count", nullable = false)
    private Integer downloadCount;

    /**
     * ファイルのSHA-256ハッシュ値
     */
    @Column(name = "sha256_hash", nullable = false)
    private String sha256Hash;

    /**
     * 有効期限
     */
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    /**
     * 削除フラグ（論理削除用）
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    /**
     * 削除日時
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 作成者ID
     */
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    /**
     * 作成者名
     */
    @Column(name = "created_by_name")
    private String createdByName;

    /**
     * 作成日時
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * メタデータ (JSON形式)
     */
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    /**
     * メタデータをMapに変換
     *
     * @return メタデータのMap
     */
    @Transient
    public Map<String, Object> getMetadataMap() {
        // 実際のアプリケーションではJSONをパースしてMapに変換する
        // このサンプルでは空のMapを返す
        return new HashMap<>();
    }

    /**
     * 初期化メソッド
     */
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.downloadCount == null) {
            this.downloadCount = 0;
        }
        if (this.isPublic == null) {
            this.isPublic = false;
        }
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
    }

    /**
     * 更新前処理
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}