package com.ses_mgr.config;

import com.ses_mgr.common.TestDataUtil;
import com.ses_mgr.common.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

/**
 * JWTトークンをテスト用に生成・検証するユーティリティ
 */
public class JwtTestUtil {

    // テスト用のJWTシークレットキー（application-test.propertiesと一致させる）
    private static final String JWT_SECRET = "TestJwtSecretKeyForTesting123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    // トークン有効期限（ミリ秒）
    private static final long JWT_EXPIRATION = 60000; // 1分
    
    /**
     * テスト用のJWTトークンを生成
     * 
     * @param user ユーザーエンティティ
     * @return 生成されたJWTトークン
     */
    public static String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);
        
        Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
        
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getUserId().toString())
                .claim("name", user.getName())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }
    
    /**
     * デフォルトのテストユーザー用トークンを生成
     * 
     * @return 生成されたJWTトークン
     */
    public static String generateDefaultUserToken() {
        return generateToken(TestDataUtil.createTestUser());
    }
    
    /**
     * テスト用の管理者トークンを生成
     * 
     * @return 生成されたJWTトークン
     */
    public static String generateAdminToken() {
        return generateToken(TestDataUtil.createTestAdmin());
    }
    
    /**
     * 認証ヘッダー付きのリクエストビルダーを作成
     * 
     * @param builder 元のリクエストビルダー
     * @param token JWTトークン
     * @return 認証ヘッダー付きのリクエストビルダー
     */
    public static MockHttpServletRequestBuilder addAuthHeader(MockHttpServletRequestBuilder builder, String token) {
        return builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }
    
    /**
     * デフォルトのテストユーザートークンを追加したリクエストビルダーを作成
     * 
     * @param builder 元のリクエストビルダー
     * @return 認証ヘッダー付きのリクエストビルダー
     */
    public static MockHttpServletRequestBuilder addDefaultUserAuthHeader(MockHttpServletRequestBuilder builder) {
        return addAuthHeader(builder, generateDefaultUserToken());
    }
    
    /**
     * 管理者トークンを追加したリクエストビルダーを作成
     * 
     * @param builder 元のリクエストビルダー
     * @return 認証ヘッダー付きのリクエストビルダー
     */
    public static MockHttpServletRequestBuilder addAdminAuthHeader(MockHttpServletRequestBuilder builder) {
        return addAuthHeader(builder, generateAdminToken());
    }
}