# ユーザー管理機能 詳細設計

## 概要
SES業務システムにおけるユーザーアカウントのライフサイクル管理、ロール割り当て、権限管理を担当する基盤的な共通機能。システム利用者の認証情報、基本情報、アクセス権限を一元管理し、システム全体のセキュリティと運用管理の基盤となる。Spring Securityフレームワークを活用して、堅牢なセキュリティ機能を提供します。

## ドキュメント構成

| ドキュメント | 内容 | 最終更新日 |
|------------|------|----------|
| [01_概要](./01_概要.md) | モジュールの目的、主要機能、アーキテクチャ | 2025-05-15 |
| [02_ドメインモデル](./02_ドメインモデル.md) | エンティティ定義、値オブジェクト | 2025-05-15 |
| [03_インターフェース定義](./03_インターフェース定義.md) | モジュールが提供/利用するインターフェース仕様 | 2025-05-15 |
| [04_ユーザーアカウント管理](./04_ユーザーアカウント管理.md) | ユーザーアカウント管理機能の詳細設計 | 2025-05-15 |
| [05_ロール管理](./05_ロール管理.md) | ロール管理機能の詳細設計 | 2025-05-15 |
| [06_権限管理](./06_権限管理.md) | 権限管理機能の詳細設計 | 2025-05-15 |

## Spring Security 統合概要

本モジュールは、Spring Securityフレームワークを活用して認証・認可機能を実装します。以下に主な統合ポイントを示します：

### 1. UserDetailsService 実装

ユーザー認証情報の取得を担当する `UserDetailsService` を実装して、Spring Securityの認証フローと連携します。

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return new CustomUserDetails(user, getAuthorities(user));
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        // ユーザーに割り当てられたロールと権限を取得する処理
        return authorities;
    }
}
```

### 2. ロール階層 (Role Hierarchy)

ロールの階層関係を定義し、上位ロールが下位ロールの権限を継承する仕組みを実装します。

```java
@Bean
public RoleHierarchy roleHierarchy() {
    RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
    hierarchy.setHierarchy(
        "ROLE_SYSTEM_ADMIN > ROLE_SECURITY_ADMIN\n" +
        "ROLE_SECURITY_ADMIN > ROLE_AUDITOR\n" +
        "ROLE_AUDITOR > ROLE_USER"
    );
    return hierarchy;
}
```

### 3. 権限評価 (Permission Evaluation)

ドメインオブジェクトに対する細かい権限チェックを実現するために `PermissionEvaluator` を実装します。

```java
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // オブジェクトタイプに応じた権限チェックロジック
        return hasPermissionLogic(authentication, targetDomainObject, permission);
    }
    
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        // ID指定による権限チェックロジック
        return hasPermissionLogic(authentication, targetId, targetType, permission);
    }
}
```

### 4. メソッドセキュリティ

Spring Security のメソッドレベルセキュリティを使用して、コントローラーやサービスメソッドへのアクセス制御を実装します。

```java
@PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'User', 'EDIT')")
public UserDTO updateUser(UUID id, UserUpdateDTO dto) {
    // メソッド実装
}
```

## 提供インターフェース一覧

| インターフェース名 | 概要 | 利用モジュール |
|-----------------|------|--------------|
| UserService | ユーザーアカウントの操作を提供 | 認証認可機能, システム管理機能 |
| RoleService | ロール定義の操作を提供 | システム管理機能 |
| PermissionService | 権限定義の操作を提供 | システム管理機能 |
| UserRoleService | ユーザーとロールの関連付け操作を提供 | システム管理機能 |
| UserDetailsService | Spring Securityの認証情報サービス | 認証認可機能 |

## 要求インターフェース一覧

| インターフェース名 | 提供モジュール | 概要 |
|-----------------|--------------|------|
| AuthenticationManager | Spring Security | 認証処理を実行 |
| PasswordEncoder | Spring Security | パスワードのハッシュ化と検証 |
| NotificationService | 通知機能 | パスワードリセットなどの通知送信に利用 |
| AuditLogService | ロギング機能 | セキュリティ関連操作の監査ログ記録に利用 |

## 認証フロー

Spring Securityと連携した認証フローを以下に示します：

```
1. ユーザーがログイン情報を送信
2. AuthenticationFilter が認証リクエストを処理
3. AuthenticationManager が認証を試行
   3.1. DaoAuthenticationProvider が CustomUserDetailsService を呼び出す
   3.2. CustomUserDetailsService がユーザー情報を取得
   3.3. パスワードが検証され、認証結果が返される
4. 認証が成功した場合、SecurityContext に Authentication オブジェクトが設定される
5. 必要に応じて JWT トークンが生成され、クライアントに返却される
```

## 認可フロー

Spring Securityと連携した認可フローを以下に示します：

```
1. 認証済みユーザーがAPIリクエストを送信
2. FilterSecurityInterceptor がリクエストを傍受
3. AccessDecisionManager が認可を判断
   3.1. SecurityExpressionHandler が式を評価
   3.2. 必要に応じて PermissionEvaluator が呼び出される
   3.3. RoleHierarchy によるロールの継承関係が考慮される
4. 認可が成功した場合、リクエストが処理される
5. 認可が失敗した場合、AccessDeniedException が発生
```

## 関連モジュール

- [認証認可機能](../01_認証認可機能/)
- [通知機能](../09_通知機能/)
- [ロギング機能](../06_ロギング機能/)

## Spring Security 依存関係

本モジュールの実装には、以下のSpring Security関連の依存関係が必要です：

```gradle
dependencies {
    // Spring Security Core
    implementation 'org.springframework.boot:spring-boot-starter-security'
    
    // JWT Support (optional)
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
    
    // Test
    testImplementation 'org.springframework.security:spring-security-test'
}
```