package jp.co.example.sesapp.common.auth.service;

import jp.co.example.sesapp.common.auth.domain.dto.UserCreateDto;
import jp.co.example.sesapp.common.auth.domain.dto.UserDto;
import jp.co.example.sesapp.common.auth.domain.dto.UserUpdateDto;

import java.util.List;
import java.util.UUID;

/**
 * ユーザー管理サービスのインターフェース
 */
public interface UserService {

    /**
     * ユーザー情報を取得する
     *
     * @param userId ユーザーID
     * @return ユーザー情報
     */
    UserDto getUserById(UUID userId);

    /**
     * ユーザー名でユーザー情報を取得する
     *
     * @param username ユーザー名
     * @return ユーザー情報
     */
    UserDto getUserByUsername(String username);

    /**
     * メールアドレスでユーザー情報を取得する
     *
     * @param email メールアドレス
     * @return ユーザー情報
     */
    UserDto getUserByEmail(String email);

    /**
     * すべてのユーザー情報を取得する
     *
     * @return ユーザー情報のリスト
     */
    List<UserDto> getAllUsers();

    /**
     * 部門IDに基づいてユーザーを検索する
     *
     * @param departmentId 部門ID
     * @return ユーザー情報のリスト
     */
    List<UserDto> getUsersByDepartmentId(UUID departmentId);

    /**
     * 役割IDに基づいてユーザーを検索する
     *
     * @param roleId 役割ID
     * @return ユーザー情報のリスト
     */
    List<UserDto> getUsersByRoleId(UUID roleId);

    /**
     * 新しいユーザーを作成する
     *
     * @param userCreateDto ユーザー作成情報
     * @return 作成されたユーザー情報
     */
    UserDto createUser(UserCreateDto userCreateDto);

    /**
     * ユーザー情報を更新する
     *
     * @param userId ユーザーID
     * @param userUpdateDto ユーザー更新情報
     * @return 更新されたユーザー情報
     */
    UserDto updateUser(UUID userId, UserUpdateDto userUpdateDto);

    /**
     * ユーザーを削除する
     *
     * @param userId ユーザーID
     */
    void deleteUser(UUID userId);

    /**
     * ユーザーのパスワードを変更する
     *
     * @param userId ユーザーID
     * @param currentPassword 現在のパスワード
     * @param newPassword 新しいパスワード
     * @return 更新されたユーザー情報
     */
    UserDto changePassword(UUID userId, String currentPassword, String newPassword);

    /**
     * ユーザーアカウントを有効化する
     *
     * @param userId ユーザーID
     * @return 更新されたユーザー情報
     */
    UserDto enableUser(UUID userId);

    /**
     * ユーザーアカウントを無効化する
     *
     * @param userId ユーザーID
     * @return 更新されたユーザー情報
     */
    UserDto disableUser(UUID userId);

    /**
     * ユーザーアカウントのロックを解除する
     *
     * @param userId ユーザーID
     * @return 更新されたユーザー情報
     */
    UserDto unlockUser(UUID userId);
}