package jp.co.example.sesapp.common.exception;

import java.util.Collections;
import java.util.List;

/**
 * アクセス拒否時に発生する例外クラス.
 */
public class AccessDeniedException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private final String resourceType;
    private final String resourceId;
    private final List<String> requiredPermissions;

    /**
     * コンストラクタ.
     *
     * @param message エラーメッセージ
     */
    public AccessDeniedException(String message) {
        super(message);
        this.resourceType = null;
        this.resourceId = null;
        this.requiredPermissions = Collections.emptyList();
    }

    /**
     * コンストラクタ.
     *
     * @param resourceType リソースの種類
     * @param resourceId リソースのID
     * @param message エラーメッセージ
     */
    public AccessDeniedException(String resourceType, String resourceId, String message) {
        super(message);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.requiredPermissions = Collections.emptyList();
    }

    /**
     * コンストラクタ.
     *
     * @param resourceType リソースの種類
     * @param resourceId リソースのID
     * @param requiredPermissions 必要な権限のリスト
     * @param message エラーメッセージ
     */
    public AccessDeniedException(String resourceType, String resourceId, List<String> requiredPermissions, String message) {
        super(message);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.requiredPermissions = Collections.unmodifiableList(requiredPermissions);
    }

    /**
     * リソースの種類を取得する.
     *
     * @return リソースの種類
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * リソースのIDを取得する.
     *
     * @return リソースのID
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * 必要な権限のリストを取得する.
     *
     * @return 必要な権限のリスト
     */
    public List<String> getRequiredPermissions() {
        return requiredPermissions;
    }
}