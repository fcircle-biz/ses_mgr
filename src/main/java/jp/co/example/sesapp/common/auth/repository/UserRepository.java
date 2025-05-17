package jp.co.example.sesapp.common.auth.repository;

import jp.co.example.sesapp.common.auth.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ユーザー情報のデータアクセスを担当するリポジトリインターフェース
 */
public interface UserRepository {

    /**
     * ユーザーIDに基づいてユーザーを検索
     *
     * @param userId ユーザーID
     * @return 指定されたIDのユーザー（存在する場合）
     */
    Optional<User> findById(UUID userId);

    /**
     * ユーザー名に基づいてユーザーを検索
     *
     * @param username ユーザー名
     * @return 指定されたユーザー名のユーザー（存在する場合）
     */
    Optional<User> findByUsername(String username);

    /**
     * メールアドレスに基づいてユーザーを検索
     *
     * @param email メールアドレス
     * @return 指定されたメールアドレスのユーザー（存在する場合）
     */
    Optional<User> findByEmail(String email);

    /**
     * すべてのユーザーを取得
     *
     * @return すべてのユーザーのリスト
     */
    List<User> findAll();

    /**
     * 部門IDに基づいてユーザーを検索
     *
     * @param departmentId 部門ID
     * @return 指定された部門のユーザーリスト
     */
    List<User> findByDepartmentId(UUID departmentId);

    /**
     * 役割IDに基づいてユーザーを検索
     *
     * @param roleId 役割ID
     * @return 指定された役割を持つユーザーリスト
     */
    List<User> findByRoleId(UUID roleId);

    /**
     * ユーザーを保存または更新
     *
     * @param user 保存または更新するユーザー
     * @return 保存されたユーザー
     */
    User save(User user);

    /**
     * 指定されたIDのユーザーを削除
     *
     * @param userId 削除するユーザーのID
     */
    void deleteById(UUID userId);

    /**
     * 最終ログイン日時でユーザーを検索（非アクティブユーザーの特定用）
     *
     * @param daysAgo 何日前からを対象とするか
     * @return 指定された日数以前に最後にログインしたユーザーのリスト
     */
    List<User> findByLastLoginOlderThan(int daysAgo);

    /**
     * パスワード有効期限が切れているユーザーを検索
     *
     * @return パスワード有効期限が切れているユーザーのリスト
     */
    List<User> findByPasswordExpired();

    /**
     * アカウントがロックされているユーザーを検索
     *
     * @return アカウントがロックされているユーザーのリスト
     */
    List<User> findByAccountLocked();
}