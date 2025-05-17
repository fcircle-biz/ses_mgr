package jp.co.example.sesapp.common.auth.service;

import jp.co.example.sesapp.common.auth.domain.ActionType;
import jp.co.example.sesapp.common.auth.domain.ResourceType;

import java.util.List;
import java.util.UUID;

/**
 * 認可サービスのインターフェース。
 * 権限確認と認可判定を提供します。
 */
public interface AuthorizationService {
    /**
     * ユーザーが指定されたリソースに対して権限があるか確認します。
     * @param userId ユーザーID
     * @param resourceType リソース種別
     * @param actionType アクション種別
     * @return 権限あり
     */
    boolean hasPermission(UUID userId, ResourceType resourceType, ActionType actionType);
    
    /**
     * ユーザーが指定されたリソースに対して権限があるか確認します（リソースID指定）
     * @param userId ユーザーID
     * @param resourceType リソース種別
     * @param resourceId リソースID
     * @param actionType アクション種別
     * @return 権限あり
     */
    boolean hasPermission(UUID userId, ResourceType resourceType, UUID resourceId, ActionType actionType);
    
    /**
     * ユーザーが権限名に対する権限があるか確認します。
     * @param userId ユーザーID
     * @param permission 権限名例: "user:read"
     * @return 権限あり
     */
    boolean hasPermission(UUID userId, String permission);
    
    /**
     * ユーザーが指定ロールを持つか確認します。
     * @param userId ユーザーID
     * @param roleName ロール名
     * @return ロールあり
     */
    boolean hasRole(UUID userId, String roleName);
    
    /**
     * ユーザーが持つ全ての権限一覧を返します。
     * @param userId ユーザーID
     * @return 権限一覧
     */
    List<String> getUserPermissions(UUID userId);
    
    /**
     * ユーザーが持つ全てのロール一覧を返します。
     * @param userId ユーザーID
     * @return ロール一覧
     */
    List<String> getUserRoles(UUID userId);
    
    /**
     * 現在認証されているユーザーが指定されたリソースに対して権限があるか確認します。
     * @param resourceType リソース種別
     * @param actionType アクション種別
     * @return 権限あり
     */
    boolean isCurrentUserAuthorized(ResourceType resourceType, ActionType actionType);
    
    /**
     * リソースの所有者かどうかを確認します。
     * @param userId ユーザーID
     * @param resourceId リソースID
     * @return 所有者
     */
    boolean isResourceOwner(UUID userId, UUID resourceId);
}