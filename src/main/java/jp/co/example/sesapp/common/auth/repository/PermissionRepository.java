package jp.co.example.sesapp.common.auth.repository;

import jp.co.example.sesapp.common.auth.domain.Permission;
import jp.co.example.sesapp.common.auth.domain.ResourceType;
import jp.co.example.sesapp.common.auth.domain.ActionType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 権限情報のデータアクセスを担当するリポジトリインターフェース
 */
public interface PermissionRepository {

    /**
     * 権限IDに基づいて権限を検索
     *
     * @param permissionId 権限ID
     * @return 指定されたIDの権限（存在する場合）
     */
    Optional<Permission> findById(UUID permissionId);

    /**
     * リソースタイプとアクションに基づいて権限を検索
     *
     * @param resourceType リソースタイプ
     * @param actionType アクションタイプ
     * @return 指定されたリソースとアクションの権限（存在する場合）
     */
    Optional<Permission> findByResourceAndAction(ResourceType resourceType, ActionType actionType);

    /**
     * 権限名に基づいて権限を検索
     *
     * @param name 権限名（例: "ENGINEER:READ"）
     * @return 指定された名前の権限（存在する場合）
     */
    Optional<Permission> findByName(String name);

    /**
     * すべての権限を取得
     *
     * @return すべての権限のリスト
     */
    List<Permission> findAll();

    /**
     * リソースタイプに基づいて権限を検索
     *
     * @param resourceType リソースタイプ
     * @return 指定されたリソースタイプに関連する権限のリスト
     */
    List<Permission> findByResourceType(ResourceType resourceType);

    /**
     * 役割IDに基づいて権限を検索
     *
     * @param roleId 役割ID
     * @return 指定された役割に割り当てられた権限のリスト
     */
    List<Permission> findByRoleId(UUID roleId);

    /**
     * ユーザーIDに基づいて権限を検索
     *
     * @param userId ユーザーID
     * @return 指定されたユーザーに割り当てられた権限のリスト（役割を通じて）
     */
    List<Permission> findByUserId(UUID userId);
    
    /**
     * ユーザーID、リソースタイプ、アクションタイプに基づいて特定の権限を検索
     *
     * @param userId ユーザーID
     * @param resourceType リソースタイプ
     * @param actionType アクションタイプ
     * @return 指定された条件に一致する権限（存在する場合）
     */
    Optional<Permission> findByUserIdAndResourceTypeAndActionType(UUID userId, ResourceType resourceType, ActionType actionType);

    /**
     * 権限を保存または更新
     *
     * @param permission 保存または更新する権限
     * @return 保存された権限
     */
    Permission save(Permission permission);

    /**
     * 指定されたIDの権限を削除
     *
     * @param permissionId 削除する権限のID
     */
    void deleteById(UUID permissionId);

    /**
     * 役割に権限を割り当てる
     *
     * @param roleId 役割ID
     * @param permissionId 権限ID
     */
    void assignPermissionToRole(UUID roleId, UUID permissionId);

    /**
     * 役割から権限を削除する
     *
     * @param roleId 役割ID
     * @param permissionId 権限ID
     */
    void removePermissionFromRole(UUID roleId, UUID permissionId);
}