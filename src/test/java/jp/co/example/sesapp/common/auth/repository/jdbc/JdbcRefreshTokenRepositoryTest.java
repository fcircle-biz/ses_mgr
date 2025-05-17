package jp.co.example.sesapp.common.auth.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import jp.co.example.sesapp.common.auth.domain.RefreshToken;
import jp.co.example.sesapp.common.exception.ResourceNotFoundException;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JdbcRefreshTokenRepository.class)
@ActiveProfiles("test")
public class JdbcRefreshTokenRepositoryTest {

    @Autowired
    private JdbcRefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RefreshToken testToken;
    private UUID tokenId;
    private UUID userId;

    @BeforeEach
    public void setUp() {
        // テスト用のスキーマとテーブルを作成
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS auth");
        jdbcTemplate.execute("DROP TABLE IF EXISTS auth.refresh_tokens");
        jdbcTemplate.execute("CREATE TABLE auth.refresh_tokens (" +
                "id UUID PRIMARY KEY, " +
                "user_id UUID NOT NULL, " +
                "token VARCHAR(255) NOT NULL UNIQUE, " +
                "expiry_date TIMESTAMP NOT NULL, " +
                "device_info VARCHAR(255), " +
                "device_id VARCHAR(255), " +
                "ip_address VARCHAR(50), " +
                "issued_at TIMESTAMP NOT NULL, " +
                "last_used_at TIMESTAMP, " +
                "is_revoked BOOLEAN DEFAULT FALSE, " +
                "revoked_reason VARCHAR(255), " +
                "created_at TIMESTAMP NOT NULL, " +
                "updated_at TIMESTAMP" +
                ")");

        // テスト用のリフレッシュトークンデータを作成
        tokenId = UUID.randomUUID();
        userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        
        testToken = new RefreshToken();
        testToken.setId(tokenId);
        testToken.setUserId(userId);
        testToken.setToken("test-token-value");
        testToken.setExpiryDate(now.plusDays(7));
        testToken.setDeviceInfo("Test User Agent");
        testToken.setDeviceId("test-device-id");
        testToken.setIpAddress("127.0.0.1");
        testToken.setIssuedAt(now);
        testToken.setLastUsedAt(null);
        testToken.setRevoked(false);
        testToken.setRevokedReason(null);
    }

    @Test
    public void saveRefreshToken_ShouldInsertNewToken() {
        // テスト実行
        RefreshToken savedToken = refreshTokenRepository.save(testToken);

        // 検証
        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getId()).isEqualTo(tokenId);
        assertThat(savedToken.getUserId()).isEqualTo(userId);
        assertThat(savedToken.getToken()).isEqualTo("test-token-value");
        assertThat(savedToken.getCreatedAt()).isNotNull();
    }

    @Test
    public void findById_ShouldReturnToken_WhenTokenExists() {
        // 事前準備
        refreshTokenRepository.save(testToken);

        // テスト実行
        Optional<RefreshToken> foundToken = refreshTokenRepository.findById(tokenId);

        // 検証
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getId()).isEqualTo(tokenId);
        assertThat(foundToken.get().getUserId()).isEqualTo(userId);
        assertThat(foundToken.get().getToken()).isEqualTo("test-token-value");
    }

    @Test
    public void findById_ShouldReturnEmpty_WhenTokenDoesNotExist() {
        // テスト実行
        Optional<RefreshToken> foundToken = refreshTokenRepository.findById(UUID.randomUUID());

        // 検証
        assertThat(foundToken).isEmpty();
    }

    @Test
    public void findByToken_ShouldReturnToken_WhenTokenExists() {
        // 事前準備
        refreshTokenRepository.save(testToken);

        // テスト実行
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByToken("test-token-value");

        // 検証
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getId()).isEqualTo(tokenId);
        assertThat(foundToken.get().getUserId()).isEqualTo(userId);
    }

    @Test
    public void findByUserId_ShouldReturnTokens_ForUser() {
        // 事前準備
        refreshTokenRepository.save(testToken);
        
        // 同じユーザーの別のトークンを作成
        RefreshToken secondToken = new RefreshToken();
        secondToken.setId(UUID.randomUUID());
        secondToken.setUserId(userId);
        secondToken.setToken("second-token-value");
        secondToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        secondToken.setDeviceInfo("Second User Agent");
        secondToken.setDeviceId("second-device-id");
        secondToken.setIpAddress("127.0.0.2");
        secondToken.setIssuedAt(LocalDateTime.now());
        refreshTokenRepository.save(secondToken);

        // テスト実行
        List<RefreshToken> userTokens = refreshTokenRepository.findByUserId(userId);

        // 検証
        assertThat(userTokens).hasSize(2);
        assertThat(userTokens).extracting("token").contains("test-token-value", "second-token-value");
    }

    @Test
    public void findByUserIdAndDeviceId_ShouldReturnToken_WhenExists() {
        // 事前準備
        refreshTokenRepository.save(testToken);

        // テスト実行
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByUserIdAndDeviceId(userId, "test-device-id");

        // 検証
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getId()).isEqualTo(tokenId);
        assertThat(foundToken.get().getDeviceId()).isEqualTo("test-device-id");
    }

    @Test
    public void updateRefreshToken_ShouldUpdateExistingToken() {
        // 事前準備
        refreshTokenRepository.save(testToken);

        // トークン情報の更新
        testToken.setLastUsedAt(LocalDateTime.now());
        testToken.setRevoked(true);
        testToken.setRevokedReason("Token revoked for testing");

        // テスト実行
        RefreshToken updatedToken = refreshTokenRepository.save(testToken);

        // 検証
        assertThat(updatedToken.isRevoked()).isTrue();
        assertThat(updatedToken.getRevokedReason()).isEqualTo("Token revoked for testing");
        assertThat(updatedToken.getLastUsedAt()).isNotNull();

        // データベースから再取得して確認
        Optional<RefreshToken> retrievedToken = refreshTokenRepository.findById(tokenId);
        assertThat(retrievedToken).isPresent();
        assertThat(retrievedToken.get().isRevoked()).isTrue();
        assertThat(retrievedToken.get().getRevokedReason()).isEqualTo("Token revoked for testing");
        assertThat(retrievedToken.get().getLastUsedAt()).isNotNull();
    }

    @Test
    public void updateRefreshToken_ShouldThrowException_WhenTokenDoesNotExist() {
        // 存在しないトークンを更新しようとする
        RefreshToken nonExistentToken = new RefreshToken();
        nonExistentToken.setId(UUID.randomUUID());
        nonExistentToken.setUserId(UUID.randomUUID());
        nonExistentToken.setToken("non-existent-token");
        nonExistentToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        nonExistentToken.setIssuedAt(LocalDateTime.now());

        // テスト実行と検証
        assertThatThrownBy(() -> refreshTokenRepository.save(nonExistentToken))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Refresh token not found with id");
    }

    @Test
    public void deleteById_ShouldRemoveToken() {
        // 事前準備
        refreshTokenRepository.save(testToken);

        // テスト実行
        refreshTokenRepository.deleteById(tokenId);

        // 検証
        Optional<RefreshToken> deletedToken = refreshTokenRepository.findById(tokenId);
        assertThat(deletedToken).isEmpty();
    }

    @Test
    public void deleteById_ShouldThrowException_WhenTokenDoesNotExist() {
        // テスト実行と検証
        UUID nonExistentId = UUID.randomUUID();
        assertThatThrownBy(() -> refreshTokenRepository.deleteById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Refresh token not found with id");
    }

    @Test
    public void deleteByToken_ShouldRemoveToken() {
        // 事前準備
        refreshTokenRepository.save(testToken);

        // テスト実行
        refreshTokenRepository.deleteByToken("test-token-value");

        // 検証
        Optional<RefreshToken> deletedToken = refreshTokenRepository.findByToken("test-token-value");
        assertThat(deletedToken).isEmpty();
    }

    @Test
    public void deleteAllByUserId_ShouldRemoveAllUserTokens() {
        // 事前準備
        refreshTokenRepository.save(testToken);
        
        // 同じユーザーの別のトークンを作成
        RefreshToken secondToken = new RefreshToken();
        secondToken.setId(UUID.randomUUID());
        secondToken.setUserId(userId);
        secondToken.setToken("second-token-value");
        secondToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        secondToken.setIssuedAt(LocalDateTime.now());
        refreshTokenRepository.save(secondToken);
        
        // 別のユーザーのトークンを作成
        RefreshToken otherUserToken = new RefreshToken();
        otherUserToken.setId(UUID.randomUUID());
        otherUserToken.setUserId(UUID.randomUUID());
        otherUserToken.setToken("other-user-token");
        otherUserToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        otherUserToken.setIssuedAt(LocalDateTime.now());
        refreshTokenRepository.save(otherUserToken);

        // テスト実行
        refreshTokenRepository.deleteAllByUserId(userId);

        // 検証
        List<RefreshToken> userTokens = refreshTokenRepository.findByUserId(userId);
        assertThat(userTokens).isEmpty();
        
        // 別のユーザーのトークンは削除されていないことを確認
        Optional<RefreshToken> otherToken = refreshTokenRepository.findByToken("other-user-token");
        assertThat(otherToken).isPresent();
    }

    @Test
    public void deleteAllExpired_ShouldRemoveExpiredTokens() {
        // 事前準備
        // 有効期限切れのトークンを作成
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setId(UUID.randomUUID());
        expiredToken.setUserId(userId);
        expiredToken.setToken("expired-token-value");
        expiredToken.setExpiryDate(LocalDateTime.now().minusDays(1));
        expiredToken.setIssuedAt(LocalDateTime.now().minusDays(7));
        refreshTokenRepository.save(expiredToken);
        
        // 有効なトークンを作成
        refreshTokenRepository.save(testToken);

        // テスト実行
        refreshTokenRepository.deleteAllExpired();

        // 検証
        Optional<RefreshToken> foundExpiredToken = refreshTokenRepository.findByToken("expired-token-value");
        assertThat(foundExpiredToken).isEmpty();
        
        Optional<RefreshToken> foundValidToken = refreshTokenRepository.findByToken("test-token-value");
        assertThat(foundValidToken).isPresent();
    }
}