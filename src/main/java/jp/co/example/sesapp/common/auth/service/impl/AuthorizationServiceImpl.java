package jp.co.example.sesapp.common.auth.service.impl;

import jp.co.example.sesapp.common.auth.domain.ActionType;
import jp.co.example.sesapp.common.auth.domain.Permission;
import jp.co.example.sesapp.common.auth.domain.ResourceType;
import jp.co.example.sesapp.common.auth.domain.Role;
import jp.co.example.sesapp.common.auth.repository.PermissionRepository;
import jp.co.example.sesapp.common.auth.repository.RoleRepository;
import jp.co.example.sesapp.common.auth.repository.UserRepository;
import jp.co.example.sesapp.common.auth.service.AuthorizationService;
import jp.co.example.sesapp.common.exception.AccessDeniedException;
import jp.co.example.sesapp.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 認可サービスの実装クラス
 */
@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public AuthorizationServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(UUID userId, ResourceType resourceType, ActionType actionType) {
        // ユーザーが存在するか確認
        if (!userRepository.findById(userId).isPresent()) {
            logger.warn("User not found with id: {}", userId);
            return false;
        }

        // ユーザーに紐づく権限を取得
        List<Permission> permissions = permissionRepository.findByUserId(userId);

        // 該当する権限があるか確認
        return permissions.stream()
                .anyMatch(permission -> 
                        permission.getResourceType() == resourceType && 
                        permission.getActionType() == actionType);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRole(UUID userId, String roleName) {
        // ユーザーが存在するか確認
        if (!userRepository.findById(userId).isPresent()) {
            logger.warn("User not found with id: {}", userId);
            return false;
        }

        // ユーザーに紐づく役割を取得
        List<Role> roles = roleRepository.findByUserId(userId);

        // 該当する役割があるか確認
        return roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    // Helper methods for internal use
    @Transactional(readOnly = true)
    protected void checkPermission(UUID userId, ResourceType resourceType, ActionType actionType) {
        if (!hasPermission(userId, resourceType, actionType)) {
            logger.warn("Access denied for user: {} to resource: {} with action: {}", 
                    userId, resourceType, actionType);
            throw new AccessDeniedException("User does not have permission to perform this action");
        }
    }

    @Transactional(readOnly = true)
    protected void checkRole(UUID userId, String roleName) {
        if (!hasRole(userId, roleName)) {
            logger.warn("Access denied for user: {} requiring role: {}", userId, roleName);
            throw new AccessDeniedException("User does not have the required role");
        }
    }

    // Support methods for role and permission management
    @Transactional
    public void assignRoleToUser(UUID userId, UUID roleId) {
        // ユーザーと役割が存在するか確認
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        if (!roleRepository.findById(roleId).isPresent()) {
            throw new ResourceNotFoundException("Role not found with id: " + roleId);
        }

        // 役割をユーザーに割り当て
        roleRepository.assignRoleToUser(userId, roleId);
        logger.info("Assigned role {} to user {}", roleId, userId);
    }

    @Transactional
    public void removeRoleFromUser(UUID userId, UUID roleId) {
        // ユーザーと役割が存在するか確認
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        if (!roleRepository.findById(roleId).isPresent()) {
            throw new ResourceNotFoundException("Role not found with id: " + roleId);
        }

        // ユーザーから役割を削除
        roleRepository.removeRoleFromUser(userId, roleId);
        logger.info("Removed role {} from user {}", roleId, userId);
    }

    @Transactional
    public void assignPermissionToRole(UUID roleId, UUID permissionId) {
        // 役割と権限が存在するか確認
        if (!roleRepository.findById(roleId).isPresent()) {
            throw new ResourceNotFoundException("Role not found with id: " + roleId);
        }

        if (!permissionRepository.findById(permissionId).isPresent()) {
            throw new ResourceNotFoundException("Permission not found with id: " + permissionId);
        }

        // 権限を役割に割り当て
        permissionRepository.assignPermissionToRole(roleId, permissionId);
        logger.info("Assigned permission {} to role {}", permissionId, roleId);
    }

    @Transactional
    public void removePermissionFromRole(UUID roleId, UUID permissionId) {
        // 役割と権限が存在するか確認
        if (!roleRepository.findById(roleId).isPresent()) {
            throw new ResourceNotFoundException("Role not found with id: " + roleId);
        }

        if (!permissionRepository.findById(permissionId).isPresent()) {
            throw new ResourceNotFoundException("Permission not found with id: " + permissionId);
        }

        // 役割から権限を削除
        permissionRepository.removePermissionFromRole(roleId, permissionId);
        logger.info("Removed permission {} from role {}", permissionId, roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserRoles(UUID userId) {
        // ユーザーが存在するか確認
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // ユーザーに紐づく役割を取得して名前のリストに変換
        List<Role> roles = roleRepository.findByUserId(userId);
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserPermissions(UUID userId) {
        // ユーザーが存在するか確認
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // ユーザーに紐づく権限を取得して文字列のリストに変換
        List<Permission> permissions = permissionRepository.findByUserId(userId);
        return permissions.stream()
                .map(Permission::getName)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCurrentUserAuthorized(ResourceType resourceType, ActionType actionType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Principal から userId を取得（実装に依存する部分）
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .map(user -> hasPermission(user.getId(), resourceType, actionType))
                .orElse(false);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isResourceOwner(UUID userId, UUID resourceId) {
        // リソースの所有者かどうかを確認する実装
        // この実装はリソースの種類によって異なるため、必要に応じてオーバーライドする
        logger.debug("Checking if user {} is owner of resource {}", userId, resourceId);
        
        // デフォルトでは所有者でないと判断
        return false;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(UUID userId, ResourceType resourceType, UUID resourceId, ActionType actionType) {
        // リソースIDに対する権限があるか確認
        // ユーザーが存在するか確認
        if (!userRepository.findById(userId).isPresent()) {
            logger.warn("User not found with id: {}", userId);
            return false;
        }
        
        // リソースの所有者であれば許可
        if (isResourceOwner(userId, resourceId)) {
            return true;
        }
        
        // ユーザーに紐づく権限を取得
        List<Permission> permissions = permissionRepository.findByUserId(userId);
        
        // 該当する権限があるか確認
        return permissions.stream()
                .anyMatch(permission -> 
                        permission.getResourceType() == resourceType && 
                        permission.getActionType() == actionType);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(UUID userId, String permission) {
        // 権限名に対する権限があるか確認
        if (!userRepository.findById(userId).isPresent()) {
            logger.warn("User not found with id: {}", userId);
            return false;
        }
        
        // ユーザーの権限一覧を取得
        List<String> permissions = getUserPermissions(userId);
        
        // 権限名が一覧に含まれるか確認
        return permissions.contains(permission);
    }
}