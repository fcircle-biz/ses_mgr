package com.ses_mgr.common.service;

/**
 * ユーザー管理に関するサービスインターフェース
 */
public interface UserService {
    
    /**
     * ユーザーを検索する
     * @param searchCriteria 検索条件
     * @return 検索結果
     */
    Object searchUsers(Object searchCriteria);
    
    /**
     * ユーザーを作成する
     * @param userData ユーザーデータ
     * @return 作成結果
     */
    Object createUser(Object userData);
    
    /**
     * ユーザーを更新する
     * @param userId ユーザーID
     * @param userData ユーザーデータ
     * @return 更新結果
     */
    Object updateUser(String userId, Object userData);
    
    /**
     * ユーザーのステータスを変更する（有効化・無効化など）
     * @param userId ユーザーID
     * @param status 新しいステータス
     * @return 更新結果
     */
    Object changeUserStatus(String userId, String status);
    
    /**
     * パスワードをリセットする
     * @param userId ユーザーID
     * @return リセット結果
     */
    Object resetPassword(String userId);
    
    /**
     * 複数ユーザーのステータスを変更する
     * @param userIds ユーザーIDリスト
     * @param status 新しいステータス
     * @return 更新結果
     */
    Object bulkChangeUserStatus(java.util.List<String> userIds, String status);
    
    /**
     * ユーザーのロックを解除する
     * @param userId ユーザーID
     * @return 更新結果
     */
    Object unlockUser(String userId);
    
    /**
     * 複数ユーザーのロックを解除する
     * @param userIds ユーザーIDリスト
     * @return 更新結果
     */
    Object bulkUnlockUsers(java.util.List<String> userIds);
}
