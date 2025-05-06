package com.ses_mgr.common.dto.file;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ファイルメタデータDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataDto {

    /**
     * ファイルID
     */
    private UUID id;

    /**
     * ファイル名
     */
    @JsonProperty("file_name")
    private String fileName;

    /**
     * ファイルタイプ
     */
    @JsonProperty("file_type")
    private String fileType;

    /**
     * MIMEタイプ
     */
    @JsonProperty("mime_type")
    private String mimeType;

    /**
     * ファイルサイズ (bytes)
     */
    private Long size;

    /**
     * ファイルの説明
     */
    private String description;

    /**
     * ファイルのタグ
     */
    private List<String> tags;

    /**
     * 関連リソースID
     */
    @JsonProperty("resource_id")
    private String resourceId;

    /**
     * 関連リソースタイプ
     */
    @JsonProperty("resource_type")
    private String resourceType;

    /**
     * ファイルの公開フラグ
     */
    @JsonProperty("is_public")
    private Boolean isPublic;

    /**
     * ダウンロードURL
     */
    @JsonProperty("download_url")
    private String downloadUrl;

    /**
     * ダウンロード回数
     */
    @JsonProperty("download_count")
    private Integer downloadCount;

    /**
     * 有効期限
     */
    @JsonProperty("expiry_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime expiryDate;

    /**
     * 作成日時
     */
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updatedAt;

    /**
     * 作成者情報
     */
    @JsonProperty("created_by")
    private UserInfoDto createdBy;

    /**
     * ファイルのSHA-256ハッシュ値
     */
    @JsonProperty("sha256_hash")
    private String sha256Hash;

    /**
     * 作成者情報DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDto {
        /**
         * ユーザーID
         */
        private UUID id;
        
        /**
         * ユーザー名
         */
        private String name;
    }
    
    /**
     * タグ文字列をリストに変換
     *
     * @param tagsString カンマ区切りのタグ文字列
     * @return タグのリスト
     */
    public static List<String> tagsToList(String tagsString) {
        List<String> tagList = new ArrayList<>();
        if (tagsString != null && !tagsString.isEmpty()) {
            for (String tag : tagsString.split(",")) {
                tagList.add(tag.trim());
            }
        }
        return tagList;
    }
    
    /**
     * タグリストを文字列に変換
     *
     * @param tagList タグのリスト
     * @return カンマ区切りのタグ文字列
     */
    public static String tagsToString(List<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            return "";
        }
        return String.join(",", tagList);
    }
}