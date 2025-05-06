package com.ses_mgr.common.service.impl;

import com.ses_mgr.common.dto.*;
import com.ses_mgr.common.entity.Permission;
import com.ses_mgr.common.entity.Role;
import com.ses_mgr.common.entity.RolePermission;
import com.ses_mgr.common.entity.User;
import com.ses_mgr.common.exception.ResourceAlreadyExistsException;
import com.ses_mgr.common.repository.PermissionRepository;
import com.ses_mgr.common.repository.RolePermissionRepository;
import com.ses_mgr.common.repository.RoleRepository;
import com.ses_mgr.common.repository.UserRepository;
import com.ses_mgr.common.service.RoleManagementService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleManagementServiceImpl implements RoleManagementService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RoleResponseDto> getRoles(RoleSearchRequestDto searchRequestDto, Pageable pageable) {
        Page<Role> rolePage;
        
        if (searchRequestDto != null && searchRequestDto.getSearch() != null && !searchRequestDto.getSearch().trim().isEmpty()) {
            rolePage = roleRepository.searchByNameOrCode(searchRequestDto.getSearch(), pageable);
        } else {
            rolePage = roleRepository.findAll(pageable);
        }
        
        // システムロールをフィルタリング
        if (searchRequestDto != null && searchRequestDto.getIncludeSystem() != null && !searchRequestDto.getIncludeSystem()) {
            List<Role> filteredRoles = rolePage.getContent().stream()
                    .filter(role -> !"system".equals(role.getRoleType()))
                    .collect(Collectors.toList());
            
            rolePage = new PageImpl<>(filteredRoles, pageable, filteredRoles.size());
        }
        
        List<RoleResponseDto> roleDtos = rolePage.getContent().stream()
                .map(this::convertToRoleResponseDto)
                .collect(Collectors.toList());
        
        return new PageImpl<>(roleDtos, pageable, rolePage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponseDto getRoleById(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("指定されたロールが見つかりません"));
        
        RoleResponseDto roleDto = convertToRoleResponseDto(role);
        
        // 権限情報を設定
        List<PermissionResponseDto> permissions = role.getRolePermissions().stream()
                .map(rp -> convertToPermissionResponseDto(rp.getPermission(), rp.getAccessLevel(), true))
                .collect(Collectors.toList());
        roleDto.setPermissions(permissions);
        
        // ユーザー情報（最大10名まで）を取得
        List<RoleResponseDto.SimpleUserDto> users = role.getUserRoles().stream()
                .map(ur -> ur.getUser())
                .limit(10)
                .map(this::convertToSimpleUserDto)
                .collect(Collectors.toList());
        roleDto.setUsers(users);
        
        return roleDto;
    }

    @Override
    @Transactional
    public RoleResponseDto createRole(RoleCreateRequestDto createRequestDto) {
        // ロールコードの重複チェック
        if (createRequestDto.getRoleCode() != null && 
                roleRepository.existsByRoleCode(createRequestDto.getRoleCode())) {
            throw new ResourceAlreadyExistsException("ロール名「" + createRequestDto.getName() + "」は既に使用されています");
        }
        
        // ロールエンティティを作成
        Role role = Role.builder()
                .name(createRequestDto.getName())
                .description(createRequestDto.getDescription())
                .roleCode(createRequestDto.getRoleCode() != null ? 
                        createRequestDto.getRoleCode() : 
                        generateRoleCode(createRequestDto.getName()))
                .roleType("business")
                .build();
        
        Role savedRole = roleRepository.save(role);
        
        // 権限を追加（指定されている場合）
        if (createRequestDto.getPermissions() != null && !createRequestDto.getPermissions().isEmpty()) {
            List<Permission> permissions = permissionRepository.findAllById(
                    createRequestDto.getPermissions().stream()
                            .map(code -> permissionRepository.findByPermissionCode(code)
                                    .orElseThrow(() -> new EntityNotFoundException("権限「" + code + "」が見つかりません"))
                                    .getPermissionId())
                            .collect(Collectors.toList()));
            
            for (Permission permission : permissions) {
                savedRole.addPermission(permission, "write");
            }
            
            savedRole = roleRepository.save(savedRole);
        }
        
        return getRoleById(savedRole.getRoleId());
    }

    @Override
    @Transactional
    public RoleResponseDto updateRole(UUID roleId, RoleUpdateRequestDto updateRequestDto) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("指定されたロールが見つかりません"));
        
        // システムロールのチェック
        if ("system".equals(role.getRoleType())) {
            throw new IllegalStateException("システムロールは変更できません");
        }
        
        // 更新処理
        role.setName(updateRequestDto.getName());
        role.setDescription(updateRequestDto.getDescription());
        
        Role updatedRole = roleRepository.save(role);
        
        return convertToRoleResponseDto(updatedRole);
    }

    @Override
    @Transactional
    public Map<String, Object> deleteRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("指定されたロールが見つかりません"));
        
        // システムロールのチェック
        if ("system".equals(role.getRoleType())) {
            throw new IllegalStateException("システムロールは削除できません");
        }
        
        // ユーザーに割り当てられているかチェック
        if (!role.getUserRoles().isEmpty()) {
            int userCount = role.getUserRoles().size();
            throw new IllegalStateException("このロールは" + userCount + "人のユーザーに割り当てられているため削除できません");
        }
        
        roleRepository.delete(role);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "ロールが正常に削除されました");
        result.put("id", roleId);
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponseDto> getRolePermissions(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("指定されたロールが見つかりません"));
        
        // すべての権限を取得
        List<Permission> allPermissions = permissionRepository.findAll();
        
        // ロールに割り当てられている権限のIDを取得
        Set<UUID> assignedPermissionIds = role.getRolePermissions().stream()
                .map(rp -> rp.getPermission().getPermissionId())
                .collect(Collectors.toSet());
        
        // すべての権限について、割り当て状態を設定したDTOを作成
        return allPermissions.stream()
                .map(permission -> {
                    boolean isAssigned = assignedPermissionIds.contains(permission.getPermissionId());
                    String accessLevel = isAssigned ? 
                            role.getRolePermissions().stream()
                                    .filter(rp -> rp.getPermission().getPermissionId().equals(permission.getPermissionId()))
                                    .findFirst()
                                    .map(RolePermission::getAccessLevel)
                                    .orElse("none") : 
                            "none";
                    
                    return convertToPermissionResponseDto(permission, accessLevel, isAssigned);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> updateRolePermissions(UUID roleId, RolePermissionUpdateRequestDto updateRequestDto) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("指定されたロールが見つかりません"));
        
        // システムロールのチェック（一部の基本権限は変更不可能だが、追加は可能）
        
        // 現在の権限コードセットを取得
        Set<String> currentPermissionCodes = role.getRolePermissions().stream()
                .map(rp -> rp.getPermission().getPermissionCode())
                .collect(Collectors.toSet());
        
        // 更新後の権限コードセット
        Set<String> newPermissionCodes = new HashSet<>(updateRequestDto.getPermissions());
        
        // 追加された権限と削除された権限を特定
        Set<String> addedPermissions = new HashSet<>(newPermissionCodes);
        addedPermissions.removeAll(currentPermissionCodes);
        
        Set<String> removedPermissions = new HashSet<>(currentPermissionCodes);
        removedPermissions.removeAll(newPermissionCodes);
        
        // 削除された権限を処理
        for (String permCode : removedPermissions) {
            Permission permission = permissionRepository.findByPermissionCode(permCode)
                    .orElseThrow(() -> new EntityNotFoundException("権限「" + permCode + "」が見つかりません"));
            
            role.removePermission(permission);
        }
        
        // 追加された権限を処理
        for (String permCode : addedPermissions) {
            Permission permission = permissionRepository.findByPermissionCode(permCode)
                    .orElseThrow(() -> new EntityNotFoundException("権限「" + permCode + "」が見つかりません"));
            
            role.addPermission(permission, "write");
        }
        
        roleRepository.save(role);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", roleId);
        result.put("name", role.getName());
        result.put("permissionCount", role.getRolePermissions().size());
        result.put("added", new ArrayList<>(addedPermissions));
        result.put("removed", new ArrayList<>(removedPermissions));
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponseDto> getAllPermissions(String resourceType, String action) {
        List<Permission> permissions;
        
        if (resourceType != null && action != null) {
            permissions = permissionRepository.findByResourceTypeAndResourceName(resourceType, action);
        } else if (resourceType != null) {
            permissions = permissionRepository.findByResourceType(resourceType);
        } else if (action != null) {
            permissions = permissionRepository.findByAction(action);
        } else {
            permissions = permissionRepository.findAll();
        }
        
        return permissions.stream()
                .map(permission -> convertToPermissionResponseDto(permission, null, null))
                .collect(Collectors.toList());
    }
    
    // ユーティリティメソッド
    
    private RoleResponseDto convertToRoleResponseDto(Role role) {
        return RoleResponseDto.builder()
                .id(role.getRoleId())
                .roleCode(role.getRoleCode())
                .name(role.getName())
                .description(role.getDescription())
                .roleType(role.getRoleType())
                .isSystem("system".equals(role.getRoleType()))
                .permissionCount(role.getRolePermissions().size())
                .userCount(role.getUserRoles().size())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
    
    private PermissionResponseDto convertToPermissionResponseDto(Permission permission, String accessLevel, Boolean isAssigned) {
        return PermissionResponseDto.builder()
                .id(permission.getPermissionId())
                .permissionCode(permission.getPermissionCode())
                .name(permission.getName())
                .description(permission.getDescription())
                .resourceType(permission.getResourceType())
                .resourceName(permission.getResourceName())
                .action(permission.getAction())
                .accessLevel(accessLevel)
                .isAssigned(isAssigned)
                .build();
    }
    
    private RoleResponseDto.SimpleUserDto convertToSimpleUserDto(User user) {
        return RoleResponseDto.SimpleUserDto.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getName())
                .build();
    }
    
    private String generateRoleCode(String roleName) {
        // ロール名からロールコードを生成する
        String baseCode = roleName.toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        
        // 既に同じコードが存在する場合、接尾辞を付ける
        String roleCode = baseCode;
        int suffix = 1;
        
        while (roleRepository.existsByRoleCode(roleCode)) {
            roleCode = baseCode + "_" + suffix;
            suffix++;
        }
        
        return roleCode;
    }
}