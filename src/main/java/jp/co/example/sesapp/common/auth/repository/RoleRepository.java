package jp.co.example.sesapp.common.auth.repository;

import jp.co.example.sesapp.common.auth.domain.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 役割情報のデータアクセスを担当するリポジトリインターフェース
 */
public interface RoleRepository {

    /**
     * 役割IDに基づいて役割を検索
     *
     * @param roleId 役割ID
     * @return 指定されたIDの役割（存在する場合）
     */
    Optional<Role> findById(UUID roleId);

    /**
     * 役割名に基づいて役割を検索
     *
     * @param name 役割名
     * @return 指定された名前の役割（存在する場合）
     */
    Optional<Role> findByName(String name);

    /**
     * すべての役割を取得
     *
     * @return すべての役割のリスト
     */
    List<Role> findAll();

    /**
     * ユーザーIDに基づいて役割を検索
     *
     * @param userId ユーザーID
     * @return 指定されたユーザーに割り当てられた役割のリスト
     */
    List<Role> findByUserId(UUID userId);

    /**
     * 役割を保存または更新
     *
     * @param role 保存または更新する役割
     * @return 保存された役割
     */
    Role save(Role role);

    /**
     * 指定されたIDの役割を削除
     *
     * @param roleId 削除する役割のID
     */
    void deleteById(UUID roleId);

    /**
     * ユーザーに役割を割り当てる
     *
     * @param userId ユーザーID
     * @param roleId 役割ID
     */
    void assignRoleToUser(UUID userId, UUID roleId);

    /**
     * ユーザーから役割を削除する
     *
     * @param userId ユーザーID
     * @param roleId 役割ID
     */
    void removeRoleFromUser(UUID userId, UUID roleId);

    /**
     * 指定された権限を持つ役割を検索
     *
     * @param permissionId 権限ID
     * @return 指定された権限を持つ役割のリスト
     */
    List<Role> findByPermissionId(UUID permissionId);
}