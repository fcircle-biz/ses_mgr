package jp.co.example.sesapp.common.exception;

/**
 * リソースが見つからない場合に発生する例外.
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private final String resourceType;
    private final String resourceId;

    /**
     * コンストラクタ.
     *
     * @param resourceType リソースの種類
     * @param resourceId リソースのID
     */
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("リソース %s (ID: %s) が見つかりません", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    /**
     * コンストラクタ.
     *
     * @param resourceType リソースの種類
     * @param resourceId リソースのID
     * @param message エラーメッセージ
     */
    public ResourceNotFoundException(String resourceType, String resourceId, String message) {
        super(message);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    /**
     * コンストラクタ.
     *
     * @param message エラーメッセージ
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = "unknown";
        this.resourceId = "unknown";
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
}