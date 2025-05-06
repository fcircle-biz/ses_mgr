package com.ses_mgr.config;

import com.ses_mgr.common.entity.User;
import com.ses_mgr.common.service.UserService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

/**
 * テスト用のセキュリティ設定
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * テスト用のパスワードエンコーダー
     */
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * テスト用のUserDetailsService
     * 認証テストで必要なユーザー情報を提供
     */
    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        return username -> {
            if ("testuser".equals(username)) {
                // テスト用ユーザーを返す
                return com.ses_mgr.common.TestDataUtil.createTestUser();
            } else if ("admin".equals(username)) {
                // 管理者ユーザーを返す
                return com.ses_mgr.common.TestDataUtil.createTestAdmin();
            }
            throw new UsernameNotFoundException("ユーザーが見つかりません: " + username);
        };
    }
}