package jp.co.example.sesapp.common.auth.repository.jdbc;

import jp.co.example.sesapp.common.auth.domain.Role;
import jp.co.example.sesapp.common.auth.repository.RoleRepository;
import jp.co.example.sesapp.common.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
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
 * RoleRepository インターフェースの JDBC 実装
 */
@Repository
public class JdbcRoleRepository implements RoleRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 役割エンティティの行マッパー
     */
    private final RowMapper<Role> roleRowMapper = (rs, rowNum) -> {
        Role role = new Role();
        role.setId(UUID.fromString(rs.getString("id")));
        role.setName(rs.getString("name"));
        role.setDescription(rs.getString("description"));
        role.setSystemRole(rs.getBoolean("is_system_role"));
        role.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        role.setUpdatedAt(rs.getTimestamp("updated_at") != null ? 
            rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return role;
    };

    public JdbcRoleRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Optional<Role> findById(UUID roleId) {
        try {
            String sql = "SELECT * FROM auth.roles WHERE id = ?";
            Role role = jdbcTemplate.queryForObject(sql, roleRowMapper, roleId.toString());
            return Optional.ofNullable(role);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Role> findByName(String name) {
        try {
            String sql = "SELECT * FROM auth.roles WHERE name = ?";
            Role role = jdbcTemplate.queryForObject(sql, roleRowMapper, name);
            return Optional.ofNullable(role);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Role> findAll() {
        String sql = "SELECT * FROM auth.roles ORDER BY name";
        return jdbcTemplate.query(sql, roleRowMapper);
    }

    @Override
    public List<Role> findByUserId(UUID userId) {
        String sql = "SELECT r.* FROM auth.roles r " +
                "JOIN auth.user_roles ur ON r.id = ur.role_id " +
                "WHERE ur.user_id = ? " +
                "ORDER BY r.name";
        return jdbcTemplate.query(sql, roleRowMapper, userId.toString());
    }

    @Override
    public Role save(Role role) {
        if (role.getId() == null) {
            return insertRole(role);
        } else {
            return updateRole(role);
        }
    }

    private Role insertRole(Role role) {
        String sql = "INSERT INTO auth.roles (id, name, description, is_system_role, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        
        UUID roleId = role.getId() != null ? role.getId() : UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        
        jdbcTemplate.update(sql,
                roleId.toString(),
                role.getName(),
                role.getDescription(),
                role.isSystemRole(),
                Timestamp.valueOf(now),
                null
        );
        
        role.setId(roleId);
        role.setCreatedAt(now);
        
        return role;
    }

    private Role updateRole(Role role) {
        String sql = "UPDATE auth.roles SET name = ?, description = ?, is_system_role = ?, updated_at = ? " +
                "WHERE id = ?";
        
        LocalDateTime now = LocalDateTime.now();
        
        int updatedRows = jdbcTemplate.update(sql,
                role.getName(),
                role.getDescription(),
                role.isSystemRole(),
                Timestamp.valueOf(now),
                role.getId().toString()
        );
        
        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Role not found with id: " + role.getId());
        }
        
        role.setUpdatedAt(now);
        
        return role;
    }

    @Override
    public void deleteById(UUID roleId) {
        // システムロールは削除できないようにチェック
        findById(roleId).ifPresent(role -> {
            if (role.isSystemRole()) {
                throw new DataIntegrityViolationException("Cannot delete system role: " + role.getName());
            }
        });

        // 紐づくユーザーロール関係を削除
        String deleteUserRolesSql = "DELETE FROM auth.user_roles WHERE role_id = ?";
        jdbcTemplate.update(deleteUserRolesSql, roleId.toString());

        // 紐づく役割権限関係を削除
        String deleteRolePermissionsSql = "DELETE FROM auth.role_permissions WHERE role_id = ?";
        jdbcTemplate.update(deleteRolePermissionsSql, roleId.toString());

        // 役割自体を削除
        String deleteRoleSql = "DELETE FROM auth.roles WHERE id = ?";
        int deletedRows = jdbcTemplate.update(deleteRoleSql, roleId.toString());
        
        if (deletedRows == 0) {
            throw new ResourceNotFoundException("Role not found with id: " + roleId);
        }
    }

    @Override
    public void assignRoleToUser(UUID userId, UUID roleId) {
        try {
            String sql = "INSERT INTO auth.user_roles (user_id, role_id, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, 
                    userId.toString(), 
                    roleId.toString(),
                    Timestamp.valueOf(LocalDateTime.now()));
        } catch (DuplicateKeyException e) {
            // すでに割り当てられている場合は何もしない
        }
    }

    @Override
    public void removeRoleFromUser(UUID userId, UUID roleId) {
        String sql = "DELETE FROM auth.user_roles WHERE user_id = ? AND role_id = ?";
        jdbcTemplate.update(sql, userId.toString(), roleId.toString());
    }

    @Override
    public List<Role> findByPermissionId(UUID permissionId) {
        String sql = "SELECT r.* FROM auth.roles r " +
                "JOIN auth.role_permissions rp ON r.id = rp.role_id " +
                "WHERE rp.permission_id = ? " +
                "ORDER BY r.name";
        return jdbcTemplate.query(sql, roleRowMapper, permissionId.toString());
    }
}