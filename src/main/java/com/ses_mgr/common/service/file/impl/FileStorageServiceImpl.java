package com.ses_mgr.common.service.file.impl;

import com.ses_mgr.common.service.file.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * ファイルストレージサービス実装クラス
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${file.max.size:10485760}") // 10MB
    private long maxFileSize;

    private final Path fileStorageLocation;

    /**
     * サポートされているMIMEタイプ
     */
    private static final Set<String> SUPPORTED_MIME_TYPES = new HashSet<>(Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/svg+xml",
            "application/zip",
            "text/csv",
            "application/json"
    ));

    public FileStorageServiceImpl() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            logger.error("ファイル保存ディレクトリの作成に失敗しました: {}", ex.getMessage());
            throw new RuntimeException("ファイル保存ディレクトリの作成に失敗しました", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, UUID fileId) {
        try {
            // ファイル名を取得
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            if (fileName.contains("..")) {
                throw new IllegalArgumentException("ファイル名に不正な文字が含まれています: " + fileName);
            }

            // 年月日でディレクトリを分ける
            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path targetLocation = this.fileStorageLocation.resolve(dateDir);
            Files.createDirectories(targetLocation);

            // ファイルIDをファイル名として保存
            String storedFileName = fileId.toString();
            if (fileName.lastIndexOf(".") > 0) {
                String extension = fileName.substring(fileName.lastIndexOf("."));
                storedFileName += extension;
            }

            Path filePath = targetLocation.resolve(storedFileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            return dateDir + "/" + storedFileName;
        } catch (IOException ex) {
            logger.error("ファイルの保存に失敗しました: {}", ex.getMessage());
            throw new RuntimeException("ファイルの保存に失敗しました", ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String storagePath) {
        try {
            Path filePath = this.fileStorageLocation.resolve(storagePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                logger.error("ファイルが見つかりません: {}", storagePath);
                throw new RuntimeException("ファイルが見つかりません: " + storagePath);
            }
        } catch (MalformedURLException ex) {
            logger.error("不正なファイルパス: {}", ex.getMessage());
            throw new RuntimeException("不正なファイルパス: " + storagePath, ex);
        }
    }

    @Override
    public boolean deleteFile(String storagePath) {
        try {
            Path filePath = this.fileStorageLocation.resolve(storagePath).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            logger.error("ファイルの削除に失敗しました: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public String calculateSha256Hash(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException ex) {
            logger.error("ハッシュ計算中にエラーが発生しました: {}", ex.getMessage());
            throw new RuntimeException("ハッシュ計算中にエラーが発生しました", ex);
        }
    }

    @Override
    public boolean validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // ファイルサイズの確認
        if (!checkFileSizeLimit(file)) {
            return false;
        }

        // MIMEタイプの確認
        return isSupportedMimeType(file);
    }

    @Override
    public boolean checkFileSizeLimit(MultipartFile file) {
        return file.getSize() <= maxFileSize;
    }

    @Override
    public boolean isSupportedMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        // file.getContentType()がnullの場合がある
        if (contentType == null) {
            return false;
        }

        return SUPPORTED_MIME_TYPES.contains(contentType);
    }
}