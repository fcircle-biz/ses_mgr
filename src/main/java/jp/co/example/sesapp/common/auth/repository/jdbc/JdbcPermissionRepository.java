package jp.co.example.sesapp.common.auth.repository.jdbc;

import jp.co.example.sesapp.common.auth.domain.Permission;
import jp.co.example.sesapp.common.auth.domain.ResourceType;
import jp.co.example.sesapp.common.auth.domain.ActionType;
import jp.co.example.sesapp.common.auth.repository.PermissionRepository;
import jp.co.example.sesapp.common.exception.ResourceNotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/**
 * PermissionRepository インターフェースの JDBC 実装
 */
@Repository
public class JdbcPermissionRepository implements PermissionRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 権限エンティティの行マッパー
     */
    private final RowMapper<Permission> permissionRowMapper = (rs, rowNum) -> {
        Permission permission = new Permission();
        permission.setId(UUID.fromString(rs.getString("id")));
        permission.setName(rs.getString("name"));
        permission.setResourceType(ResourceType.valueOf(rs.getString("resource_type")));
        permission.setActionType(ActionType.valueOf(rs.getString("action_type")));
        permission.setDescription(rs.getString("description"));
        permission.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        permission.setUpdatedAt(rs.getTimestamp("updated_at") != null ? 
                rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return permission;
    };

    public JdbcPermissionRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Optional<Permission> findById(UUID permissionId) {
        try {
            String sql = "SELECT * FROM auth.permissions WHERE id = ?";
            Permission permission = jdbcTemplate.queryForObject(sql, permissionRowMapper, permissionId.toString());
            return Optional.ofNullable(permission);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Permission> findByResourceAndAction(ResourceType resourceType, ActionType actionType) {
        try {
            String sql = "SELECT * FROM auth.permissions WHERE resource_type = ? AND action_type = ?";
            Permission permission = jdbcTemplate.queryForObject(
                    sql, 
                    permissionRowMapper, 
                    resourceType.name(), 
                    actionType.name()
            );
            return Optional.ofNullable(permission);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Permission> findByName(String name) {
        try {
            String sql = "SELECT * FROM auth.permissions WHERE name = ?";
            Permission permission = jdbcTemplate.queryForObject(sql, permissionRowMapper, name);
            return Optional.ofNullable(permission);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Permission> findAll() {
        String sql = "SELECT * FROM auth.permissions ORDER BY resource_type, action_type";
        return jdbcTemplate.query(sql, permissionRowMapper);
    }

    @Override
    public List<Permission> findByResourceType(ResourceType resourceType) {
        String sql = "SELECT * FROM auth.permissions WHERE resource_type = ? ORDER BY action_type";
        return jdbcTemplate.query(sql, permissionRowMapper, resourceType.name());
    }

    @Override
    public List<Permission> findByRoleId(UUID roleId) {
        String sql = "SELECT p.* FROM auth.permissions p " +
                "JOIN auth.role_permissions rp ON p.id = rp.permission_id " +
                "WHERE rp.role_id = ? " +
                "ORDER BY p.resource_type, p.action_type";
        return jdbcTemplate.query(sql, permissionRowMapper, roleId.toString());
    }

    @Override
    public List<Permission> findByUserId(UUID userId) {
        String sql = "SELECT DISTINCT p.* FROM auth.permissions p " +
                "JOIN auth.role_permissions rp ON p.id = rp.permission_id " +
                "JOIN auth.user_roles ur ON rp.role_id = ur.role_id " +
                "WHERE ur.user_id = ? " +
                "ORDER BY p.resource_type, p.action_type";
        return jdbcTemplate.query(sql, permissionRowMapper, userId.toString());
    }
    
    @Override
    public Optional<Permission> findByUserIdAndResourceTypeAndActionType(UUID userId, ResourceType resourceType, ActionType actionType) {
        try {
            String sql = "SELECT DISTINCT p.* FROM auth.permissions p " +
                    "JOIN auth.role_permissions rp ON p.id = rp.permission_id " +
                    "JOIN auth.user_roles ur ON rp.role_id = ur.role_id " +
                    "WHERE ur.user_id = ? " +
                    "AND p.resource_type = ? " +
                    "AND p.action_type = ? " +
                    "LIMIT 1";
            
            Permission permission = jdbcTemplate.queryForObject(
                    sql, 
                    permissionRowMapper, 
                    userId.toString(),
                    resourceType.name(),
                    actionType.name()
            );
            return Optional.ofNullable(permission);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Permission save(Permission permission) {
        if (permission.getId() == null) {
            return insertPermission(permission);
        } else {
            return updatePermission(permission);
        }
    }

    private Permission insertPermission(Permission permission) {
        String sql = "INSERT INTO auth.permissions (id, name, resource_type, action_type, description, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        UUID permissionId = permission.getId() != null ? permission.getId() : UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        
        jdbcTemplate.update(sql,
                permissionId.toString(),
                permission.getName(),
                permission.getResourceType().name(),
                permission.getActionType().name(),
                permission.getDescription(),
                Timestamp.valueOf(now),
                null
        );
        
        permission.setId(permissionId);
        permission.setCreatedAt(now);
        
        return permission;
    }

    private Permission updatePermission(Permission permission) {
        String sql = "UPDATE auth.permissions " +
                "SET name = ?, resource_type = ?, action_type = ?, description = ?, updated_at = ? " +
                "WHERE id = ?";
        
        LocalDateTime now = LocalDateTime.now();
        
        int updatedRows = jdbcTemplate.update(sql,
                permission.getName(),
                permission.getResourceType().name(),
                permission.getActionType().name(),
                permission.getDescription(),
                Timestamp.valueOf(now),
                permission.getId().toString()
        );
        
        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Permission not found with id: " + permission.getId());
        }
        
        permission.setUpdatedAt(now);
        
        return permission;
    }

    @Override
    public void deleteById(UUID permissionId) {
        // 紐づく役割権限関係を削除
        String deleteRolePermissionsSql = "DELETE FROM auth.role_permissions WHERE permission_id = ?";
        jdbcTemplate.update(deleteRolePermissionsSql, permissionId.toString());

        // 権限自体を削除
        String deletePermissionSql = "DELETE FROM auth.permissions WHERE id = ?";
        int deletedRows = jdbcTemplate.update(deletePermissionSql, permissionId.toString());
        
        if (deletedRows == 0) {
            throw new ResourceNotFoundException("Permission not found with id: " + permissionId);
        }
    }

    @Override
    public void assignPermissionToRole(UUID roleId, UUID permissionId) {
        try {
            String sql = "INSERT INTO auth.role_permissions (role_id, permission_id, created_at) " +
                    "VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, 
                    roleId.toString(), 
                    permissionId.toString(),
                    Timestamp.valueOf(LocalDateTime.now()));
        } catch (DuplicateKeyException e) {
            // すでに割り当てられている場合は何もしない
        }
    }

    @Override
    public void removePermissionFromRole(UUID roleId, UUID permissionId) {
        String sql = "DELETE FROM auth.role_permissions WHERE role_id = ? AND permission_id = ?";
        jdbcTemplate.update(sql, roleId.toString(), permissionId.toString());
    }
}