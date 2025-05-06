package com.ses_mgr.common.service;

import com.ses_mgr.common.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RoleManagementService {

    /**
     * ロール一覧を取得する
     * 
     * @param searchRequestDto 検索条件
     * @param pageable ページング情報
     * @return ロール一覧
     */
    Page<RoleResponseDto> getRoles(RoleSearchRequestDto searchRequestDto, Pageable pageable);
    
    /**
     * 特定のロールの詳細情報を取得する
     * 
     * @param roleId ロールID
     * @return ロール詳細情報
     */
    RoleResponseDto getRoleById(UUID roleId);
    
    /**
     * 新規ロールを作成する
     * 
     * @param createRequestDto ロール作成リクエスト
     * @return 作成されたロール情報
     */
    RoleResponseDto createRole(RoleCreateRequestDto createRequestDto);
    
    /**
     * ロール情報を更新する
     * 
     * @param roleId ロールID
     * @param updateRequestDto ロール更新リクエスト
     * @return 更新されたロール情報
     */
    RoleResponseDto updateRole(UUID roleId, RoleUpdateRequestDto updateRequestDto);
    
    /**
     * ロールを削除する
     * 
     * @param roleId ロールID
     * @return 削除結果情報
     */
    Map<String, Object> deleteRole(UUID roleId);
    
    /**
     * 特定のロールに割り当てられた権限一覧を取得する
     * 
     * @param roleId ロールID
     * @return 権限一覧
     */
    List<PermissionResponseDto> getRolePermissions(UUID roleId);
    
    /**
     * ロールの権限を更新する
     * 
     * @param roleId ロールID
     * @param updateRequestDto 権限更新リクエスト
     * @return 更新結果情報
     */
    Map<String, Object> updateRolePermissions(UUID roleId, RolePermissionUpdateRequestDto updateRequestDto);
    
    /**
     * 全権限一覧を取得する
     * 
     * @param resourceType リソース種別（オプション）
     * @param action アクション種別（オプション）
     * @return 権限一覧
     */
    List<PermissionResponseDto> getAllPermissions(String resourceType, String action);
}