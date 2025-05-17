package jp.co.example.sesapp.common.auth.repository.jdbc;

import jp.co.example.sesapp.common.auth.domain.PasswordResetToken;
import jp.co.example.sesapp.common.auth.repository.PasswordResetTokenRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * パスワードリセットトークンリポジトリのJDBC実装
 */
@Repository
public class JdbcPasswordResetTokenRepository implements PasswordResetTokenRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final RowMapper<PasswordResetToken> rowMapper = (rs, rowNum) -> {
        PasswordResetToken token = new PasswordResetToken();
        token.setId(UUID.fromString(rs.getString("id")));
        token.setUserId(UUID.fromString(rs.getString("user_id")));
        token.setToken(rs.getString("token"));
        token.setExpiryDate(rs.getTimestamp("expiry_date").toLocalDateTime());
        token.setUsed(rs.getBoolean("used"));
        token.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return token;
    };

    public JdbcPasswordResetTokenRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("password_reset_tokens")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        if (token.getId() == null) {
            return insert(token);
        } else {
            return update(token);
        }
    }

    private PasswordResetToken insert(PasswordResetToken token) {
        token.setId(UUID.randomUUID());
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", token.getId().toString());
        parameters.put("user_id", token.getUserId().toString());
        parameters.put("token", token.getToken());
        parameters.put("expiry_date", Timestamp.valueOf(token.getExpiryDate()));
        parameters.put("used", token.isUsed());
        parameters.put("created_at", Timestamp.valueOf(LocalDateTime.now()));
        
        jdbcInsert.execute(parameters);
        return token;
    }

    private PasswordResetToken update(PasswordResetToken token) {
        String sql = "UPDATE password_reset_tokens SET user_id = :userId, token = :token, " +
                "expiry_date = :expiryDate, used = :used WHERE id = :id";
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", token.getId().toString())
                .addValue("userId", token.getUserId().toString())
                .addValue("token", token.getToken())
                .addValue("expiryDate", Timestamp.valueOf(token.getExpiryDate()))
                .addValue("used", token.isUsed());
        
        namedParameterJdbcTemplate.update(sql, params);
        return token;
    }

    @Override
    public Optional<PasswordResetToken> findById(UUID id) {
        try {
            String sql = "SELECT * FROM password_reset_tokens WHERE id = ?";
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(sql, rowMapper, id.toString()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        try {
            String sql = "SELECT * FROM password_reset_tokens WHERE token = ?";
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(sql, rowMapper, token));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<PasswordResetToken> findValidTokenByUserId(UUID userId) {
        try {
            String sql = "SELECT * FROM password_reset_tokens WHERE user_id = ? AND used = false " +
                    "AND expiry_date > ? ORDER BY created_at DESC LIMIT 1";
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(sql, rowMapper, 
                            userId.toString(), Timestamp.valueOf(LocalDateTime.now())));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<PasswordResetToken> findByTokenAndUserId(String token, UUID userId) {
        try {
            String sql = "SELECT * FROM password_reset_tokens WHERE token = ? AND user_id = ?";
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(sql, rowMapper, token, userId.toString()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public int deleteExpiredTokens() {
        String sql = "DELETE FROM password_reset_tokens WHERE expiry_date < ?";
        return jdbcTemplate.update(sql, Timestamp.valueOf(LocalDateTime.now()));
    }
}