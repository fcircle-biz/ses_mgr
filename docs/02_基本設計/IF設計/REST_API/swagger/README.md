# SES業務システム Swagger API定義

このディレクトリには、SES業務システムのREST API仕様をOpenAPI (Swagger) 形式で定義したファイルが含まれています。

## ファイル構成

- `openapi.yaml` - 共通定義（セキュリティスキーム、エラーレスポンス等）
- `index.yaml` - 各APIを統合したメインファイル
- `auth.yaml` - 認証API
- `projects.yaml` - 案件管理API
- その他の機能別APIファイル（順次追加予定）

## Swagger UI での閲覧方法

これらの定義ファイルは、Swagger UI を使用して視覚的に表示することができます。

### オンラインでの閲覧

1. [Swagger Editor](https://editor.swagger.io/) にアクセス
2. `index.yaml` の内容をコピー＆ペースト

### ローカルでの閲覧（開発時）

1. Docker がインストールされていることを確認
2. 以下のコマンドを実行して Swagger UI コンテナを起動

```bash
docker run -p 8080:8080 -e SWAGGER_JSON=/swagger/index.yaml -v $(pwd):/swagger swaggerapi/swagger-ui
```

3. ブラウザで http://localhost:8080 にアクセス

## 仕様の更新方法

1. 該当する機能のYAMLファイルを更新
2. 新しいAPIエンドポイントを追加した場合は、`index.yaml` にも追加
3. OpenAPI (Swagger) バリデーターで構文チェック
4. Swagger UI で表示確認

## 注意事項

- YAMLの構文に注意（インデントは2スペース推奨）
- パスの参照には正確な記法を使用（例: `$ref: './auth.yaml#/paths/~1auth~1login'`）
- スキーマの再利用を心がけ、DRYの原則に従う

## 今後の予定

- 全API定義ファイルの完成
- API定義からのモックサーバー生成
- CI/CDパイプラインでの自動バリデーション