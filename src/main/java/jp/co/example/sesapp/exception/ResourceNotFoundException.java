package jp.co.example.sesapp.exception;

/**
 * リソースが見つからない場合にスローされる例外クラス.
 * ID検索などでリソースが存在しない場合に使用します。
 */
public class ResourceNotFoundException extends BusinessException {

    /**
     * コンストラクタ.
     *
     * @param resourceName リソース名（例: "User", "Project"）
     * @param fieldName フィールド名（例: "id", "email"）
     * @param fieldValue フィールド値
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
            String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue),
            "RESOURCE_NOT_FOUND"
        );
    }
}