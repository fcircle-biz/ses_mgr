package com.ses_mgr.config;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * テスト用カスタムユーザー認証アノテーション
 * SpringのWithMockUserをカスタマイズして、UUIDベースのユーザーIDを持つモックユーザーを提供
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
    
    /**
     * ユーザーIDの文字列表現
     */
    String userId() default "00000000-0000-0000-0000-000000000001";
    
    /**
     * ログインID
     */
    String loginId() default "testuser";
    
    /**
     * ユーザー名
     */
    String name() default "Test User";
    
    /**
     * メールアドレス
     */
    String email() default "test@example.com";
    
    /**
     * 権限（ロール）
     */
    String[] roles() default {"USER"};
}