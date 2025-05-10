# ADR-003: RESTful APIにおけるJWT認証の採用

## ステータス
承認

## 作成日
2025-05-08

## 作成者
XXXX (xxxx@example.com)

## コンテキスト
SES業務システムのRESTful API認証方式を決定する必要がある。システムはWebフロントエンドとモバイルアプリからアクセスされ、APIのステートレス性が求められる。モノリシックアプリケーションとして設計されたシステムでの認証基盤として適切な方式を選定する必要がある。

考慮すべき点：
- セキュリティレベル：機密情報を扱うため、高いセキュリティが必要
- スケーラビリティ：負荷分散環境での動作に対応
- 開発効率：実装の容易さと保守性
- 現代的な認証方式の採用

## 決定
RESTful APIの認証にはJWT（JSON Web Token）を使用する。具体的な実装方針：

1. Spring Security＋JWTによる認証基盤の構築
2. アクセストークンとリフレッシュトークンの2種類のトークンを使用
3. アクセストークンの有効期限は30分
4. リフレッシュトークンの有効期限は2週間
5. HMAC SHA-256 (HS256) をトークン署名アルゴリズムとして使用
6. ペイロードには最小限の情報（ユーザーID、権限情報、発行時間、有効期限）のみを含める
7. トークンはHTTPヘッダーのAuthorizationフィールドで Bearer形式で送信

```java
// トークンプロバイダの実装例
@Component
public class JwtTokenProvider {
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.accessTokenExpirationInMs}")
    private int jwtAccessTokenExpirationInMs;
    
    @Value("${app.jwt.refreshTokenExpirationInMs}")
    private int jwtRefreshTokenExpirationInMs;
    
    // アクセストークン生成
    public String generateAccessToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtAccessTokenExpirationInMs);
        
        return Jwts.builder()
                .setSubject(userPrincipal.getId().toString())
                .claim("roles", userPrincipal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // リフレッシュトークン生成
    public String generateRefreshToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtRefreshTokenExpirationInMs);
        
        return Jwts.builder()
                .setSubject(userPrincipal.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // JWTからユーザーIDを取得
    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return Long.parseLong(claims.getSubject());
    }
    
    // トークンの検証
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            // 検証失敗
            return false;
        }
    }
}
```

## 結果
この決定による影響と結果：

### メリット
- **ステートレス性**: サーバー側でセッション状態を保持する必要がない
- **スケーラビリティ**: 負荷分散環境でも追加設定なしで動作可能
- **クロスドメイン**: 異なるドメイン間でのAPI利用が容易
- **モバイル互換性**: モバイルアプリとの連携が容易
- **実装ライブラリ**: Spring Securityとの統合が容易で実装が単純化

### デメリット
- **トークン無効化**: 一度発行したトークンはサーバー側で直接無効化できない（ブラックリスト管理が必要）
- **ペイロードサイズ**: トークンに含める情報が増えるとサイズが大きくなる
- **セキュリティリスク**: トークン盗難時の影響範囲（対策として有効期限の短縮が必要）

### トレードオフ
- **セキュリティと利便性のバランス**: アクセストークンの有効期限を短くし、リフレッシュトークンで再取得する仕組みにすることで両立
- **トークンサイズと情報量**: 必要最小限の情報だけをトークンに含めることでサイズを最適化

## 代替案
検討した他の選択肢：

### 1. セッションベース認証
- **メリット**:
  - 従来の手法で実装が容易
  - 直接無効化可能
  - サーバー側で完全に制御可能
- **デメリット**:
  - ステートフル性によりスケーリングが複雑化
  - モバイルアプリとの連携が煩雑
  - CORSの問題が発生しやすい
- **不採用理由**:
  - スケーラビリティとクロスプラットフォーム対応の制約
  - モバイルアプリからのアクセスに不向き

### 2. OAuth 2.0 / OpenID Connect (フル実装)
- **メリット**:
  - 標準化された堅牢な認証フレームワーク
  - フェデレーション認証に対応
  - サードパーティ連携に最適
- **デメリット**:
  - 実装の複雑さ
  - セットアップコストの高さ
  - 現時点での要件に対してオーバースペック
- **不採用理由**:
  - 実装コストと複雑性が現在の要件に見合わない
  - 将来的に外部サービス連携が必要になった場合は拡張可能

### 3. API Key認証
- **メリット**:
  - 単純な実装
  - ヘッダーベースの認証で導入が容易
- **デメリット**:
  - キー管理の課題
  - 権限の粒度管理が複雑
  - ユーザーコンテキストの取り扱いが難しい
- **不採用理由**:
  - ユーザー認証とロールベースアクセス制御の要件を満たさない
  - セキュリティレベルが要件を満たさない

## セキュリティ対策
JWT採用に伴う追加のセキュリティ対策：

1. **リフレッシュトークンのローテーション**: リフレッシュトークン使用時に新しいリフレッシュトークンを発行し、以前のトークンを無効化
2. **トークンブラックリスト**: ログアウト時やパスワード変更時にトークンをブラックリストに追加
3. **シークレットキーのセキュア管理**: 環境変数またはVaultなどのシークレット管理サービスを使用
4. **HTTPS通信の強制**: トークン送信は常にHTTPS経由のみ許可
5. **機密情報の除外**: ペイロードには機密情報を含めない
6. **定期的な監査**: トークン発行・使用ログの定期監査

## 実装計画
1. Spring Security設定の実装
2. JWTユーティリティクラスの作成
3. 認証フィルターの実装
4. ユーザーサービスとの連携
5. セキュリティテストの実施

## 参照情報
- [JWT公式ドキュメント](https://jwt.io/)
- [Spring Security JWT実装ガイド](https://spring.io/guides/tutorials/spring-security-and-angular-js/)
- [OWASP JWTセキュリティチートシート](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
- [The OAuth 2.0 Authorization Framework](https://tools.ietf.org/html/rfc6749)