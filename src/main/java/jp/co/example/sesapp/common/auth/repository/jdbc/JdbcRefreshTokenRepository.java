package jp.co.example.sesapp.common.auth.repository.jdbc;

import jp.co.example.sesapp.common.auth.domain.RefreshToken;
import jp.co.example.sesapp.common.auth.repository.RefreshTokenRepository;
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
 * RefreshTokenRepository インターフェースの JDBC 実装
 */
@Repository
public class JdbcRefreshTokenRepository implements RefreshTokenRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * リフレッシュトークンエンティティの行マッパー
     */
    private final RowMapper<RefreshToken> refreshTokenRowMapper = (rs, rowNum) -> {
        RefreshToken token = new RefreshToken();
        token.setId(UUID.fromString(rs.getString("id")));
        token.setUserId(UUID.fromString(rs.getString("user_id")));
        token.setToken(rs.getString("token"));
        token.setExpiryDate(rs.getTimestamp("expiry_date").toLocalDateTime());
        token.setDeviceInfo(rs.getString("device_info"));
        token.setDeviceId(rs.getString("device_id"));
        token.setIpAddress(rs.getString("ip_address"));
        token.setIssuedAt(rs.getTimestamp("issued_at").toLocalDateTime());
        token.setLastUsedAt(rs.getTimestamp("last_used_at") != null ? 
                rs.getTimestamp("last_used_at").toLocalDateTime() : null);
        token.setRevoked(rs.getBoolean("is_revoked"));
        token.setRevokedReason(rs.getString("revoked_reason"));
        token.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        token.setUpdatedAt(rs.getTimestamp("updated_at") != null ? 
                rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return token;
    };

    public JdbcRefreshTokenRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Optional<RefreshToken> findById(UUID id) {
        try {
            String sql = "SELECT * FROM auth.refresh_tokens WHERE id = ?";
            RefreshToken token = jdbcTemplate.queryForObject(sql, refreshTokenRowMapper, id.toString());
            return Optional.ofNullable(token);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        try {
            String sql = "SELECT * FROM auth.refresh_tokens WHERE token = ?";
            RefreshToken refreshToken = jdbcTemplate.queryForObject(sql, refreshTokenRowMapper, token);
            return Optional.ofNullable(refreshToken);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<RefreshToken> findByUserId(UUID userId) {
        String sql = "SELECT * FROM auth.refresh_tokens WHERE user_id = ? ORDER BY issued_at DESC";
        return jdbcTemplate.query(sql, refreshTokenRowMapper, userId.toString());
    }

    @Override
    public Optional<RefreshToken> findByUserIdAndDeviceId(UUID userId, String deviceId) {
        try {
            String sql = "SELECT * FROM auth.refresh_tokens WHERE user_id = ? AND device_id = ? AND is_revoked = false";
            RefreshToken token = jdbcTemplate.queryForObject(
                    sql, 
                    refreshTokenRowMapper, 
                    userId.toString(),
                    deviceId
            );
            return Optional.ofNullable(token);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        if (refreshToken.getId() == null) {
            return insertRefreshToken(refreshToken);
        } else {
            return updateRefreshToken(refreshToken);
        }
    }

    private RefreshToken insertRefreshToken(RefreshToken refreshToken) {
        String sql = "INSERT INTO auth.refresh_tokens (" +
                "id, user_id, token, expiry_date, device_info, device_id, ip_address, " +
                "issued_at, last_used_at, is_revoked, revoked_reason, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        UUID tokenId = refreshToken.getId() != null ? refreshToken.getId() : UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        
        jdbcTemplate.update(sql,
                tokenId.toString(),
                refreshToken.getUserId().toString(),
                refreshToken.getToken(),
                Timestamp.valueOf(refreshToken.getExpiryDate()),
                refreshToken.getDeviceInfo(),
                refreshToken.getDeviceId(),
                refreshToken.getIpAddress(),
                Timestamp.valueOf(refreshToken.getIssuedAt()),
                refreshToken.getLastUsedAt() != null ? Timestamp.valueOf(refreshToken.getLastUsedAt()) : null,
                refreshToken.isRevoked(),
                refreshToken.getRevokedReason(),
                Timestamp.valueOf(now),
                null
        );
        
        refreshToken.setId(tokenId);
        refreshToken.setCreatedAt(now);
        
        return refreshToken;
    }

    private RefreshToken updateRefreshToken(RefreshToken refreshToken) {
        String sql = "UPDATE auth.refresh_tokens SET " +
                "user_id = ?, token = ?, expiry_date = ?, device_info = ?, device_id = ?, " +
                "ip_address = ?, issued_at = ?, last_used_at = ?, is_revoked = ?, " +
                "revoked_reason = ?, updated_at = ? " +
                "WHERE id = ?";
        
        LocalDateTime now = LocalDateTime.now();
        
        int updatedRows = jdbcTemplate.update(sql,
                refreshToken.getUserId().toString(),
                refreshToken.getToken(),
                Timestamp.valueOf(refreshToken.getExpiryDate()),
                refreshToken.getDeviceInfo(),
                refreshToken.getDeviceId(),
                refreshToken.getIpAddress(),
                Timestamp.valueOf(refreshToken.getIssuedAt()),
                refreshToken.getLastUsedAt() != null ? Timestamp.valueOf(refreshToken.getLastUsedAt()) : null,
                refreshToken.isRevoked(),
                refreshToken.getRevokedReason(),
                Timestamp.valueOf(now),
                refreshToken.getId().toString()
        );
        
        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Refresh token not found with id: " + refreshToken.getId());
        }
        
        refreshToken.setUpdatedAt(now);
        
        return refreshToken;
    }

    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM auth.refresh_tokens WHERE id = ?";
        int deletedRows = jdbcTemplate.update(sql, id.toString());
        
        if (deletedRows == 0) {
            throw new ResourceNotFoundException("Refresh token not found with id: " + id);
        }
    }

    @Override
    public void deleteByToken(String token) {
        String sql = "DELETE FROM auth.refresh_tokens WHERE token = ?";
        jdbcTemplate.update(sql, token);
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        String sql = "DELETE FROM auth.refresh_tokens WHERE user_id = ?";
        jdbcTemplate.update(sql, userId.toString());
    }

    @Override
    public void deleteAllExpired() {
        String sql = "DELETE FROM auth.refresh_tokens WHERE expiry_date < ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(LocalDateTime.now()));
    }
}