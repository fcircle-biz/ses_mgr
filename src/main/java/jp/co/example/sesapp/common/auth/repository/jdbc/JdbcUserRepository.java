package jp.co.example.sesapp.common.auth.repository.jdbc;

import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.auth.domain.AuthenticationMethod;
import jp.co.example.sesapp.common.auth.repository.UserRepository;
import jp.co.example.sesapp.common.exception.ResourceNotFoundException;
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
 * UserRepository インターフェースの JDBC 実装
 */
@Repository
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * ユーザーエンティティの行マッパー
     */
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(UUID.fromString(rs.getString("id")));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setDepartmentId(rs.getString("department_id") != null ? 
                UUID.fromString(rs.getString("department_id")) : null);
        user.setEnabled(rs.getBoolean("is_enabled"));
        user.setAccountLocked(rs.getBoolean("is_account_locked"));
        user.setAccountExpireDate(rs.getTimestamp("account_expire_date") != null ? 
                rs.getTimestamp("account_expire_date").toLocalDateTime() : null);
        user.setCredentialsExpireDate(rs.getTimestamp("credentials_expire_date") != null ? 
                rs.getTimestamp("credentials_expire_date").toLocalDateTime() : null);
        user.setLastLoginDate(rs.getTimestamp("last_login_date") != null ? 
                rs.getTimestamp("last_login_date").toLocalDateTime() : null);
        user.setLoginFailCount(rs.getInt("login_fail_count"));
        user.setAuthenticationMethod(rs.getString("authentication_method") != null ? 
                AuthenticationMethod.valueOf(rs.getString("authentication_method")) : null);
        user.setMfaEnabled(rs.getBoolean("is_mfa_enabled"));
        user.setMfaSecret(rs.getString("mfa_secret"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at") != null ? 
                rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return user;
    };

    public JdbcUserRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Optional<User> findById(UUID userId) {
        try {
            String sql = "SELECT * FROM auth.users WHERE id = ?";
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, userId.toString());
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try {
            String sql = "SELECT * FROM auth.users WHERE username = ?";
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, username);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try {
            String sql = "SELECT * FROM auth.users WHERE email = ?";
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM auth.users ORDER BY username";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public List<User> findByDepartmentId(UUID departmentId) {
        String sql = "SELECT * FROM auth.users WHERE department_id = ? ORDER BY username";
        return jdbcTemplate.query(sql, userRowMapper, departmentId.toString());
    }

    @Override
    public List<User> findByRoleId(UUID roleId) {
        String sql = "SELECT u.* FROM auth.users u " +
                "JOIN auth.user_roles ur ON u.id = ur.user_id " +
                "WHERE ur.role_id = ? " +
                "ORDER BY u.username";
        return jdbcTemplate.query(sql, userRowMapper, roleId.toString());
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            return insertUser(user);
        } else {
            return updateUser(user);
        }
    }

    private User insertUser(User user) {
        String sql = "INSERT INTO auth.users (" +
                "id, username, email, password_hash, first_name, last_name, department_id, " +
                "is_enabled, is_account_locked, account_expire_date, credentials_expire_date, " +
                "last_login_date, login_fail_count, authentication_method, is_mfa_enabled, mfa_secret, " +
                "created_at, updated_at) " +
                "VALUES (" +
                "?, ?, ?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, " +
                "?, ?)";
        
        UUID userId = user.getId() != null ? user.getId() : UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        
        jdbcTemplate.update(sql,
                userId.toString(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFirstName(),
                user.getLastName(),
                user.getDepartmentId() != null ? user.getDepartmentId().toString() : null,
                user.isEnabled(),
                user.isAccountLocked(),
                user.getAccountExpireDate() != null ? Timestamp.valueOf(user.getAccountExpireDate()) : null,
                user.getCredentialsExpireDate() != null ? Timestamp.valueOf(user.getCredentialsExpireDate()) : null,
                user.getLastLoginDate() != null ? Timestamp.valueOf(user.getLastLoginDate()) : null,
                user.getLoginFailCount(),
                user.getAuthenticationMethod() != null ? user.getAuthenticationMethod().name() : null,
                user.isMfaEnabled(),
                user.getMfaSecret(),
                Timestamp.valueOf(now),
                null
        );
        
        user.setId(userId);
        user.setCreatedAt(now);
        
        return user;
    }

    private User updateUser(User user) {
        String sql = "UPDATE auth.users SET " +
                "username = ?, email = ?, password_hash = ?, first_name = ?, last_name = ?, department_id = ?, " +
                "is_enabled = ?, is_account_locked = ?, account_expire_date = ?, credentials_expire_date = ?, " +
                "last_login_date = ?, login_fail_count = ?, authentication_method = ?, is_mfa_enabled = ?, " +
                "mfa_secret = ?, updated_at = ? " +
                "WHERE id = ?";
        
        LocalDateTime now = LocalDateTime.now();
        
        int updatedRows = jdbcTemplate.update(sql,
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFirstName(),
                user.getLastName(),
                user.getDepartmentId() != null ? user.getDepartmentId().toString() : null,
                user.isEnabled(),
                user.isAccountLocked(),
                user.getAccountExpireDate() != null ? Timestamp.valueOf(user.getAccountExpireDate()) : null,
                user.getCredentialsExpireDate() != null ? Timestamp.valueOf(user.getCredentialsExpireDate()) : null,
                user.getLastLoginDate() != null ? Timestamp.valueOf(user.getLastLoginDate()) : null,
                user.getLoginFailCount(),
                user.getAuthenticationMethod() != null ? user.getAuthenticationMethod().name() : null,
                user.isMfaEnabled(),
                user.getMfaSecret(),
                Timestamp.valueOf(now),
                user.getId().toString()
        );
        
        if (updatedRows == 0) {
            throw new ResourceNotFoundException("User not found with id: " + user.getId());
        }
        
        user.setUpdatedAt(now);
        
        return user;
    }

    @Override
    public void deleteById(UUID userId) {
        String sql = "DELETE FROM auth.users WHERE id = ?";
        int deletedRows = jdbcTemplate.update(sql, userId.toString());
        
        if (deletedRows == 0) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    @Override
    public List<User> findByLastLoginOlderThan(int daysAgo) {
        String sql = "SELECT * FROM auth.users " +
                "WHERE last_login_date < ? OR last_login_date IS NULL " +
                "ORDER BY username";
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysAgo);
        return jdbcTemplate.query(sql, userRowMapper, Timestamp.valueOf(cutoffDate));
    }

    @Override
    public List<User> findByPasswordExpired() {
        String sql = "SELECT * FROM auth.users " +
                "WHERE credentials_expire_date < ? " +
                "ORDER BY username";
        
        return jdbcTemplate.query(sql, userRowMapper, Timestamp.valueOf(LocalDateTime.now()));
    }

    @Override
    public List<User> findByAccountLocked() {
        String sql = "SELECT * FROM auth.users WHERE is_account_locked = true ORDER BY username";
        return jdbcTemplate.query(sql, userRowMapper);
    }
}