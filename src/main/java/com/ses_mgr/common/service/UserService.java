package com.ses_mgr.common.service;

import com.ses_mgr.common.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService {

    // ユーザー一覧取得
    List<UserResponseDto> getAllUsers();
    
    // ページネーション対応ユーザー一覧取得
    Page<UserResponseDto> getAllUsers(Pageable pageable);
    
    // ユーザー検索
    Page<UserResponseDto> searchUsers(UserSearchRequestDto searchRequestDto, Pageable pageable);
    
    // ユーザー詳細取得
    UserResponseDto getUserById(UUID userId);
    
    // ユーザー作成
    UserResponseDto createUser(UserCreateRequestDto createRequestDto);
    
    // ユーザー更新
    UserResponseDto updateUser(UUID userId, UserUpdateRequestDto updateRequestDto);
    
    // ユーザーステータス変更
    UserResponseDto updateUserStatus(UUID userId, UserStatusRequestDto statusRequestDto);
    
    // ユーザーパスワードリセット
    void resetUserPassword(UUID userId);
    
    // ユーザーアカウントロック解除
    UserResponseDto unlockUserAccount(UUID userId);
    
    // 複数ユーザーステータス一括変更
    int updateBulkUserStatus(UserBulkStatusRequestDto bulkStatusRequestDto);
    
    // 複数ユーザーアカウントロック一括解除
    int unlockBulkUserAccounts(UserBulkUnlockRequestDto bulkUnlockRequestDto);
    
    // ユーザーにロールを割り当て
    void assignRoleToUser(UUID userId, String roleCode);
    
    // ユーザーからロールを削除
    void removeRoleFromUser(UUID userId, String roleCode);
    
    // ユーザーのロール一覧取得
    List<RoleResponseDto> getUserRoles(UUID userId);
    
    // メールアドレスによるユーザー検索
    UserResponseDto findByEmail(String email);
    
    // ログインIDによるユーザー検索
    UserResponseDto findByLoginId(String loginId);
    
    // ユーザーの存在チェック
    boolean existsById(UUID userId);
    boolean existsByEmail(String email);
    boolean existsByLoginId(String loginId);
    
    // 最終ログイン時間を更新
    void updateLastLoginTime(UUID userId);
}