package com.ses_mgr.common.dto.file;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ファイルアップロードリクエストDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequestDto {

    /**
     * アップロードするファイル
     */
    private MultipartFile file;

    /**
     * ファイルの種類
     */
    @NotBlank(message = "ファイルの種類は必須です")
    @JsonProperty("file_type")
    private String fileType;

    /**
     * 関連リソースID
     */
    @JsonProperty("resource_id")
    private String resourceId;

    /**
     * ファイルの説明
     */
    @Size(max = 1000, message = "説明は1000文字以内で入力してください")
    private String description;

    /**
     * ファイルに付けるタグ（カンマ区切りの文字列）
     */
    @Size(max = 255, message = "タグは255文字以内で入力してください")
    private String tags;

    /**
     * ファイルを公開するかどうか
     */
    @JsonProperty("is_public")
    private Boolean isPublic;

    /**
     * ファイルの有効期限
     */
    @JsonProperty("expiry_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime expiryDate;
}