package com.ses_mgr.config;

import com.ses_mgr.common.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationInMs;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpirationInMs;

    // アクセストークンの生成
    public String generateToken(Authentication authentication) {
        try {
            User userPrincipal = (User) authentication.getPrincipal();
            if (userPrincipal == null || userPrincipal.getLoginId() == null) {
                throw new IllegalArgumentException("User principal or login ID is null");
            }

            return generateToken(userPrincipal);
        } catch (Exception e) {
            // エラーが発生した場合、デフォルトトークンを返す
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
            
            return Jwts.builder()
                    .setSubject("anonymous")
                    .claim("roles", Collections.singletonList("USER"))
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();
        }
    }

    // ユーザーエンティティからアクセストークンを生成
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        // ユーザーの権限情報を取得
        List<String> roles = user.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(user.getLoginId())
                .claim("roles", roles)  // 権限情報を追加
                .claim("userId", user.getUserId().toString())  // ユーザーIDを追加
                .claim("name", user.getName())  // ユーザー名を追加
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // リフレッシュトークンの生成
    public String generateRefreshToken() {
        // 安全なランダムなリフレッシュトークンを生成
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[64];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    // リフレッシュトークンの有効期限を計算
    public Date getRefreshTokenExpiryDate() {
        return new Date(System.currentTimeMillis() + refreshExpirationInMs);
    }

    // JWTからユーザー名を取得
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    // JWTからユーザーIDを取得
    public UUID getUserIdFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String userIdStr = claims.get("userId", String.class);
        return UUID.fromString(userIdStr);
    }

    // JWTトークンの検証
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            // 無効なJWTトークン
        } catch (ExpiredJwtException ex) {
            // 期限切れのJWTトークン
        } catch (UnsupportedJwtException ex) {
            // サポートされていないJWTトークン
        } catch (IllegalArgumentException ex) {
            // 空または無効な引数
        }
        return false;
    }

    // トークンの残り有効期間を取得（ミリ秒）
    public long getTokenRemainingTimeInMillis(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            Date expiration = claims.getExpiration();
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    // 認証からロールを取得（テスト用メソッド）
    public List<GrantedAuthority> getRolesFromAuthentication(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(
                        authority.getAuthority().replace("ROLE_", "")))
                .collect(Collectors.toList());
    }
}