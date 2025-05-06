package com.ses_mgr.config;

import com.ses_mgr.common.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * カスタムユーザーを使用したセキュリティコンテキストファクトリ
 * テスト時にUUID形式のユーザーIDを持つユーザーを提供
 */
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        
        // アノテーションからユーザー情報を取得
        UUID userId = UUID.fromString(annotation.userId());
        String loginId = annotation.loginId();
        String name = annotation.name();
        String email = annotation.email();
        String[] roles = annotation.roles();
        
        // 権限（ロール）リストを作成
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        
        // モックユーザーの作成
        User user = User.builder()
                .userId(userId)
                .loginId(loginId)
                .email(email)
                .name(name)
                .passwordHash("$2a$10$testHashedPasswordForTesting")
                .status("active")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // 認証オブジェクトの作成と設定
        Authentication auth = new UsernamePasswordAuthenticationToken(user, "N/A", authorities);
        context.setAuthentication(auth);
        
        return context;
    }
}