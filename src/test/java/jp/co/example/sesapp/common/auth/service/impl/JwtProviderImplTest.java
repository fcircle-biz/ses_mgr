package jp.co.example.sesapp.common.auth.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jp.co.example.sesapp.common.auth.domain.Role;
import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.auth.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtProviderImplTest {

    private static final String SECRET_KEY = "testSecretKeyWithAtLeast32BytesForHS256Algorithm";
    private static final long ACCESS_TOKEN_EXPIRATION = 3600000L; // 1 hour
    private static final long REFRESH_TOKEN_EXPIRATION = 86400000L; // 24 hours
    private static final String ISSUER = "ses-mgr";

    private RoleRepository roleRepository;
    private JwtProviderImpl jwtProvider;

    private User testUser;
    private Role testRole;
    private List<String> testPermissions;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        // モックを手動で作成
        roleRepository = mock(RoleRepository.class);
        jwtProvider = new JwtProviderImpl(roleRepository);
        
        // プロパティの設定
        ReflectionTestUtils.setField(jwtProvider, "jwtSecret", SECRET_KEY);
        ReflectionTestUtils.setField(jwtProvider, "accessTokenExpirationMs", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtProvider, "refreshTokenExpirationMs", REFRESH_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtProvider, "issuer", ISSUER);

        // 署名キーの作成
        signingKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        
        // テストユーザーの作成
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .roleId(UUID.randomUUID())
                .build();

        // テストロールの作成
        testRole = new Role(testUser.getRoleId(), "ADMIN", "Administrator Role");

        // テスト権限の作成
        testPermissions = Arrays.asList("user:read", "user:write", "user:delete");

        // ロールリポジトリモックのセットアップ
        when(roleRepository.findById(testUser.getRoleId())).thenReturn(Optional.of(testRole));
    }

    @Test
    void generateAccessToken_ShouldGenerateValidToken() {
        // トークン生成
        String token = jwtProvider.generateAccessToken(testUser, testPermissions);

        // トークンがnullでも空でもないことを確認
        assertThat(token).isNotNull().isNotEmpty();

        // トークンが検証できることを確認
        assertThat(jwtProvider.validateToken(token)).isTrue();

        // トークンのクレームを確認
        Claims claims = extractClaims(token);
        assertThat(claims.getSubject()).isEqualTo(testUser.getEmail());
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.get("userId", String.class)).isEqualTo(testUser.getId().toString());
        assertThat(claims.get("role", String.class)).isEqualTo(testRole.getName());
        assertThat(claims.get("email", String.class)).isEqualTo(testUser.getEmail());
        assertThat(claims.get("name", String.class)).isEqualTo(testUser.getName());
        assertThat(claims.get("permissions", String.class))
                .isEqualTo(String.join(",", testPermissions));
    }

    @Test
    void generateAccessToken_WithoutPermissions_ShouldGenerateValidToken() {
        // 権限なしでトークン生成
        String token = jwtProvider.generateAccessToken(testUser, null);

        // トークンがnullでも空でもないことを確認
        assertThat(token).isNotNull().isNotEmpty();

        // トークンが検証できることを確認
        assertThat(jwtProvider.validateToken(token)).isTrue();

        // トークンのクレームを確認
        Claims claims = extractClaims(token);
        assertThat(claims.getSubject()).isEqualTo(testUser.getEmail());
        assertThat(claims.get("userId", String.class)).isEqualTo(testUser.getId().toString());
        assertThat(claims.get("role", String.class)).isEqualTo(testRole.getName());
        assertThat(claims.get("email", String.class)).isEqualTo(testUser.getEmail());
        assertThat(claims.get("name", String.class)).isEqualTo(testUser.getName());
        
        // 権限クレームが存在しないことを確認
        assertThat(claims.get("permissions")).isNull();
    }

    @Test
    void generateRefreshToken_ShouldGenerateValidToken() {
        // リフレッシュトークン生成
        String token = jwtProvider.generateRefreshToken(testUser);

        // トークンがnullでも空でもないことを確認
        assertThat(token).isNotNull().isNotEmpty();

        // トークンが検証できることを確認
        assertThat(jwtProvider.validateToken(token)).isTrue();

        // トークンのクレームを確認
        Claims claims = extractClaims(token);
        assertThat(claims.getSubject()).isEqualTo(testUser.getEmail());
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.get("userId", String.class)).isEqualTo(testUser.getId().toString());
        assertThat(claims.get("tokenType", String.class)).isEqualTo("refresh");
        
        // リフレッシュトークンの有効期限がアクセストークンより長いことを確認
        Date refreshExpiration = jwtProvider.getExpirationFromToken(token);
        String accessToken = jwtProvider.generateAccessToken(testUser, testPermissions);
        Date accessExpiration = jwtProvider.getExpirationFromToken(accessToken);
        
        assertThat(refreshExpiration.getTime()).isGreaterThan(accessExpiration.getTime());
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // トークン生成
        String token = jwtProvider.generateAccessToken(testUser, testPermissions);

        // トークン検証
        boolean isValid = jwtProvider.validateToken(token);

        // 検証結果の確認
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // アクセストークンの有効期限を非常に短く設定
        ReflectionTestUtils.setField(jwtProvider, "accessTokenExpirationMs", 1L); // 1ms

        // 即座に期限切れになるトークンを生成
        String token = jwtProvider.generateAccessToken(testUser, testPermissions);

        // トークンが期限切れになるのを待つ
        try {
            Thread.sleep(10); // 10ms待機してトークンが確実に期限切れになるようにする
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // トークン検証
        boolean isValid = jwtProvider.validateToken(token);

        // 検証結果の確認
        assertThat(isValid).isFalse();
        
        // 元の値に戻す
        ReflectionTestUtils.setField(jwtProvider, "accessTokenExpirationMs", ACCESS_TOKEN_EXPIRATION);
    }

    @Test
    void validateToken_WithMalformedToken_ShouldReturnFalse() {
        // 不正な形式のトークンを作成
        String malformedToken = "not.a.valid.jwt.token";

        // 不正な形式のトークンを検証
        boolean isValid = jwtProvider.validateToken(malformedToken);

        // 検証結果の確認
        assertThat(isValid).isFalse();
    }

    @Test
    void getEmailFromToken_ShouldReturnEmail() {
        // トークン生成
        String token = jwtProvider.generateAccessToken(testUser, testPermissions);

        // トークンからメールアドレスを取得
        String email = jwtProvider.getUsernameFromToken(token);

        // メールアドレスの確認
        assertThat(email).isEqualTo(testUser.getEmail());
    }

    @Test
    void getUserIdFromToken_ShouldReturnUserId() {
        // トークン生成
        String token = jwtProvider.generateAccessToken(testUser, testPermissions);

        // トークンからユーザーIDを取得
        UUID userId = jwtProvider.getUserIdFromToken(token);

        // ユーザーIDの確認
        assertThat(userId).isEqualTo(testUser.getId());
    }

    @Test
    void getRoleFromToken_ShouldReturnRole() {
        // トークン生成
        String token = jwtProvider.generateAccessToken(testUser, testPermissions);

        // トークンからロールを取得
        String role = jwtProvider.getRoleFromToken(token);

        // ロールの確認
        assertThat(role).isEqualTo(testRole.getName());
    }
    
    @Test
    void getAuthentication_ShouldReturnAuthenticationWithCorrectAuthorities() {
        // 権限付きでトークンを生成
        String token = jwtProvider.generateAccessToken(testUser, testPermissions);
        
        // トークンから認証オブジェクトを取得
        Authentication authentication = jwtProvider.getAuthentication(token);
        
        // 認証オブジェクトの確認
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        
        // プリンシパルの確認
        assertThat(authentication.getPrincipal()).isInstanceOf(UserDetails.class);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        assertThat(userDetails.getUsername()).isEqualTo(testUser.getEmail());
        
        // 権限の確認（ロールと個別権限）
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        assertThat(authorities).isNotEmpty();
        
        // ロール権限の確認（ROLE_プレフィックス付き）
        assertThat(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + testRole.getName().toUpperCase())))
                .isTrue();
        
        // 個別権限の確認
        for (String permission : testPermissions) {
            assertThat(authorities.stream()
                    .anyMatch(a -> a.getAuthority().equals(permission)))
                    .isTrue();
        }
    }
    
    @Test
    void isRefreshToken_ShouldReturnTrueForRefreshToken() {
        // リフレッシュトークンを生成
        String refreshToken = jwtProvider.generateRefreshToken(testUser);
        
        // リフレッシュトークンかどうかを確認
        boolean isRefresh = jwtProvider.isRefreshToken(refreshToken);
        
        // 結果の確認
        assertThat(isRefresh).isTrue();
    }
    
    @Test
    void isRefreshToken_ShouldReturnFalseForAccessToken() {
        // アクセストークンを生成
        String accessToken = jwtProvider.generateAccessToken(testUser, testPermissions);
        
        // リフレッシュトークンかどうかを確認
        boolean isRefresh = jwtProvider.isRefreshToken(accessToken);
        
        // 結果の確認
        assertThat(isRefresh).isFalse();
    }

    // テスト用のヘルパーメソッド: トークンからクレームを抽出する
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}