package jp.co.example.sesapp.common.auth.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import jp.co.example.sesapp.common.auth.domain.PasswordResetToken;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JdbcPasswordResetTokenRepository.class)
@ActiveProfiles("test")
public class JdbcPasswordResetTokenRepositoryTest {

    @Autowired
    private JdbcPasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private DataSource dataSource;

    private PasswordResetToken testToken;
    private UUID tokenId;
    private UUID userId;
    private String tokenValue;

    @BeforeEach
    public void setUp() {
        // テスト用のテーブルを作成
        jdbcTemplate.execute("DROP TABLE IF EXISTS password_reset_tokens");
        jdbcTemplate.execute("CREATE TABLE password_reset_tokens (" +
                "id UUID PRIMARY KEY, " +
                "user_id UUID NOT NULL, " +
                "token VARCHAR(255) NOT NULL UNIQUE, " +
                "expiry_date TIMESTAMP NOT NULL, " +
                "used BOOLEAN DEFAULT FALSE, " +
                "created_at TIMESTAMP NOT NULL" +
                ")");

        // テスト用のパスワードリセットトークンデータを作成
        tokenId = UUID.randomUUID();
        userId = UUID.randomUUID();
        tokenValue = "test-reset-token-value";
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);
        
        testToken = new PasswordResetToken(tokenId, userId, tokenValue, expiryDate);
    }

    @Test
    public void savePasswordResetToken_ShouldInsertNewToken() {
        // テスト実行
        PasswordResetToken savedToken = passwordResetTokenRepository.save(testToken);

        // 検証
        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getUserId()).isEqualTo(userId);
        assertThat(savedToken.getToken()).isEqualTo(tokenValue);
        assertThat(savedToken.isUsed()).isFalse();
    }

    @Test
    public void findById_ShouldReturnToken_WhenTokenExists() {
        // 事前準備
        passwordResetTokenRepository.save(testToken);

        // テスト実行
        Optional<PasswordResetToken> foundToken = passwordResetTokenRepository.findById(testToken.getId());

        // 検証
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getId()).isEqualTo(testToken.getId());
        assertThat(foundToken.get().getUserId()).isEqualTo(userId);
        assertThat(foundToken.get().getToken()).isEqualTo(tokenValue);
    }

    @Test
    public void findById_ShouldReturnEmpty_WhenTokenDoesNotExist() {
        // テスト実行
        Optional<PasswordResetToken> foundToken = passwordResetTokenRepository.findById(UUID.randomUUID());

        // 検証
        assertThat(foundToken).isEmpty();
    }

    @Test
    public void findByToken_ShouldReturnToken_WhenTokenExists() {
        // 事前準備
        passwordResetTokenRepository.save(testToken);

        // テスト実行
        Optional<PasswordResetToken> foundToken = passwordResetTokenRepository.findByToken(tokenValue);

        // 検証
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getId()).isEqualTo(testToken.getId());
        assertThat(foundToken.get().getToken()).isEqualTo(tokenValue);
    }

    @Test
    public void findValidTokenByUserId_ShouldReturnToken_WhenValidTokenExists() {
        // 事前準備
        passwordResetTokenRepository.save(testToken);

        // テスト実行
        Optional<PasswordResetToken> foundToken = passwordResetTokenRepository.findValidTokenByUserId(userId);

        // 検証
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getUserId()).isEqualTo(userId);
        assertThat(foundToken.get().isValid()).isTrue();
    }

    @Test
    public void findValidTokenByUserId_ShouldReturnEmpty_WhenTokenIsExpired() {
        // 事前準備 - 期限切れのトークンを作成
        PasswordResetToken expiredToken = new PasswordResetToken(
                UUID.randomUUID(),
                userId,
                "expired-token",
                LocalDateTime.now().minusHours(1)
        );
        passwordResetTokenRepository.save(expiredToken);

        // テスト実行
        Optional<PasswordResetToken> foundToken = passwordResetTokenRepository.findValidTokenByUserId(userId);

        // 検証
        assertThat(foundToken).isEmpty();
    }

    @Test
    public void findValidTokenByUserId_ShouldReturnEmpty_WhenTokenIsUsed() {
        // 事前準備 - 使用済みのトークンを作成
        testToken.markAsUsed();
        passwordResetTokenRepository.save(testToken);

        // テスト実行
        Optional<PasswordResetToken> foundToken = passwordResetTokenRepository.findValidTokenByUserId(userId);

        // 検証
        assertThat(foundToken).isEmpty();
    }

    @Test
    public void findByTokenAndUserId_ShouldReturnToken_WhenTokenExists() {
        // 事前準備
        passwordResetTokenRepository.save(testToken);

        // テスト実行
        Optional<PasswordResetToken> foundToken = 
                passwordResetTokenRepository.findByTokenAndUserId(tokenValue, userId);

        // 検証
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getToken()).isEqualTo(tokenValue);
        assertThat(foundToken.get().getUserId()).isEqualTo(userId);
    }

    @Test
    public void findByTokenAndUserId_ShouldReturnEmpty_WhenTokenDoesNotMatch() {
        // 事前準備
        passwordResetTokenRepository.save(testToken);

        // テスト実行 - 存在しないトークン値で検索
        Optional<PasswordResetToken> foundToken = 
                passwordResetTokenRepository.findByTokenAndUserId("non-existent-token", userId);

        // 検証
        assertThat(foundToken).isEmpty();
    }

    @Test
    public void findByTokenAndUserId_ShouldReturnEmpty_WhenUserIdDoesNotMatch() {
        // 事前準備
        passwordResetTokenRepository.save(testToken);

        // テスト実行 - 存在しないユーザーIDで検索
        Optional<PasswordResetToken> foundToken = 
                passwordResetTokenRepository.findByTokenAndUserId(tokenValue, UUID.randomUUID());

        // 検証
        assertThat(foundToken).isEmpty();
    }

    @Test
    public void updateToken_ShouldUpdateExistingToken() {
        // 事前準備
        passwordResetTokenRepository.save(testToken);

        // トークンを使用済みとしてマーク
        testToken.markAsUsed();

        // テスト実行
        PasswordResetToken updatedToken = passwordResetTokenRepository.save(testToken);

        // 検証
        assertThat(updatedToken.isUsed()).isTrue();

        // データベースから再取得して確認
        Optional<PasswordResetToken> retrievedToken = passwordResetTokenRepository.findById(testToken.getId());
        assertThat(retrievedToken).isPresent();
        assertThat(retrievedToken.get().isUsed()).isTrue();
    }

    @Test
    public void deleteExpiredTokens_ShouldRemoveExpiredTokens() {
        // 事前準備
        // 有効なトークンを保存
        passwordResetTokenRepository.save(testToken);
        
        // 期限切れのトークンを作成・保存
        PasswordResetToken expiredToken = new PasswordResetToken(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "expired-token",
                LocalDateTime.now().minusHours(1)
        );
        passwordResetTokenRepository.save(expiredToken);

        // テスト実行
        int deletedCount = passwordResetTokenRepository.deleteExpiredTokens();

        // 検証
        assertThat(deletedCount).isEqualTo(1);
        
        // 有効なトークンは残っているはず
        Optional<PasswordResetToken> validToken = passwordResetTokenRepository.findById(testToken.getId());
        assertThat(validToken).isPresent();
        
        // 期限切れのトークンは削除されているはず
        Optional<PasswordResetToken> deletedToken = passwordResetTokenRepository.findById(expiredToken.getId());
        assertThat(deletedToken).isEmpty();
    }
}