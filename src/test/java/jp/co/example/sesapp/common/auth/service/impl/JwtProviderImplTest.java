package jp.co.example.sesapp.common.auth.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jp.co.example.sesapp.common.auth.domain.Role;
import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.auth.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtProviderImplTest {

    private static final String SECRET_KEY = "testSecretKeyWithAtLeast256BitsForSecureHmacSha256Signature";
    private static final long ACCESS_TOKEN_EXPIRATION = 30 * 60 * 1000; // 30 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days
    private static final String ISSUER = "test-issuer";

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    @Spy
    private JwtProviderImpl jwtProvider;

    private User testUser;
    private Role testRole;
    private List<String> testPermissions;

    @BeforeEach
    void setUp() {
        // JwtProviderに必要な値を設定
        ReflectionTestUtils.setField(jwtProvider, "jwtSecret", SECRET_KEY);
        ReflectionTestUtils.setField(jwtProvider, "accessTokenExpirationMs", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtProvider, "refreshTokenExpirationMs", REFRESH_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtProvider, "issuer", ISSUER);

        // テスト用のユーザーを作成
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setPasswordHash("hashedPassword");
        testUser.setRoleId(UUID.randomUUID());
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        // テスト用のロールを作成
        testRole = new Role();
        testRole.setId(testUser.getRoleId());
        testRole.setName("ADMIN");
        testRole.setDescription("Administrator Role");

        // テスト用の権限リストを作成
        testPermissions = Arrays.asList("user:read", "user:write", "user:delete");
    }

    @Test
    void generateAccessToken_ShouldGenerateValidToken() {
        // モックの設定
        when(roleRepository.findById(testUser.getRoleId())).thenReturn(Optional.of(testRole));

        // テスト対象メソッドの実行
        String token = jwtProvider.generateAccessToken(testUser, testPermissions);

        // 検証
        assertThat(token).isNotNull();
        assertThat(jwtProvider.validateToken(token)).isTrue();

        // トークン内のクレームを検証
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getSubject()).isEqualTo(testUser.getEmail());
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.get("userId", String.class)).isEqualTo(testUser.getId().toString());
        assertThat(claims.get("role", String.class)).isEqualTo(testRole.getName());
        assertThat(claims.get("email", String.class)).isEqualTo(testUser.getEmail());
        assertThat(claims.get("name", String.class)).isEqualTo(testUser.getName());
        
        String permissionsStr = claims.get("permissions", String.class);
        assertThat(permissionsStr).contains(String.join(",", testPermissions));
    }

    @Test
    void generateRefreshToken_ShouldGenerateValidToken() {
        // テスト対象メソッドの実行
        String token = jwtProvider.generateRefreshToken(testUser);

        // 検証
        assertThat(token).isNotNull();
        assertThat(jwtProvider.validateToken(token)).isTrue();
        assertThat(jwtProvider.isRefreshToken(token)).isTrue();

        // トークン内のクレームを検証
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getSubject()).isEqualTo(testUser.getEmail());
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.get("userId", String.class)).isEqualTo(testUser.getId().toString());
        assertThat(claims.get("tokenType", String.class)).isEqualTo("refresh");
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // モックの設定
        when(roleRepository.findById(testUser.getRoleId())).thenReturn(Optional.of(testRole));

        // テスト対象メソッドの実行
        String token = jwtProvider.generateAccessToken(testUser, testPermissions);
        boolean result = jwtProvider.validateToken(token);

        // 検証
        assertThat(result).isTrue();
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // テスト対象メソッドの実行
        boolean result = jwtProvider.validateToken("invalid.token.string");

        // 検証
        assertThat(result).isFalse();
    }

    @Test
    void getUsernameFromToken_ShouldReturnCorrectUsername() {
        // モックの設定
        when(roleRepository.findById(testUser.getRoleId())).thenReturn(Optional.of(testRole));

        // テスト対象メソッドの実行
        String token = jwtProvider.generateAccessToken(testUser, testPermissions);
        String username = jwtProvider.getUsernameFromToken(token);

        // 検証
        assertThat(username).isEqualTo(testUser.getEmail());
    }

    @Test
    void getUserIdFromToken_ShouldReturnCorrectUserId() {
        // モックの設定
        when(roleRepository.findById(testUser.getRoleId())).thenReturn(Optional.of(testRole));

        // テスト対象メソッドの実行
        String token = jwtProvider.generateAccessToken(testUser, testPermissions);
        UUID userId = jwtProvider.getUserIdFromToken(token);

        // 検証
        assertThat(userId).isEqualTo(testUser.getId());
    }

    @Test
    void getAuthentication_ShouldReturnValidAuthentication() {
        // モックの設定
        when(roleRepository.findById(testUser.getRoleId())).thenReturn(Optional.of(testRole));

        // テスト対象メソッドの実行
        String token = jwtProvider.generateAccessToken(testUser, testPermissions);
        Authentication authentication = jwtProvider.getAuthentication(token);

        // 検証
        assertThat(authentication).isNotNull();
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(authentication.getName()).isEqualTo(testUser.getEmail());
        
        // 権限の検証
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        assertThat(authorities).isNotEmpty();
        
        // ROLE_プレフィックス付きのロールが含まれていることを確認
        assertThat(authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()))
                .contains("ROLE_ADMIN");
        
        // パーミッションが含まれていることを確認
        for (String permission : testPermissions) {
            assertThat(authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()))
                    .contains(permission);
        }
    }

    @Test
    void getExpirationFromToken_ShouldReturnCorrectExpiration() {
        // モックの設定
        when(roleRepository.findById(testUser.getRoleId())).thenReturn(Optional.of(testRole));

        // テスト対象メソッドの実行
        String token = jwtProvider.generateAccessToken(testUser, testPermissions);
        Date expiration = jwtProvider.getExpirationFromToken(token);

        // 検証
        assertThat(expiration).isNotNull();
        long expirationTimeMs = expiration.getTime();
        long currentTimeMs = new Date().getTime();
        long diff = expirationTimeMs - currentTimeMs;
        
        // 有効期限が現在から約30分後であることを確認（誤差を許容）
        assertThat(diff).isGreaterThan(ACCESS_TOKEN_EXPIRATION - 5000)
                       .isLessThan(ACCESS_TOKEN_EXPIRATION + 5000);
    }
}