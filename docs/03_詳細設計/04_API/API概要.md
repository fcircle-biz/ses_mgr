# SES業務システム API設計概要

## 1. OpenAPI（Swagger）仕様書について

本プロジェクトでは、REST API仕様の詳細な定義にOpenAPI（Swagger）形式を採用しています。OpenAPI仕様書は以下のディレクトリに格納されています：

```
/docs/03_詳細設計/04_API/OpenAPI/
```

### 主要ファイル

- `openapi.yaml` - 共通定義（セキュリティスキーム、エラーレスポンス等）
- `index.yaml` - 各APIを統合したメインファイル
- `auth.yaml` - 認証API
- `projects.yaml` - 案件管理API
- `engineers.yaml` - 技術者管理API
- `contracts.yaml` - 契約管理API
- `timesheet.yaml` - 勤怠工数管理API
- `matching.yaml` - マッチングAPI

### API実装方針

基本設計で定義された各APIエンドポイントに対して、詳細な実装仕様をOpenAPI形式で定義しています。
この仕様書は以下の目的で活用されます：

1. フロントエンド開発とバックエンド開発の連携基盤
2. API開発時の仕様参照
3. 自動テストやモックサーバー生成の基盤
4. API開発者向けドキュメント生成

## 2. 実装ガイドライン

### コントローラ実装

APIコントローラの実装には以下の命名規則を採用します：

- **REST APIコントローラ**: `{リソース名}Controller`（例: `EngineerController`）
- **管理用APIコントローラ**: `{リソース名}AdminController`（例: `EngineerAdminController`）

### エンドポイント実装

各エンドポイントのメソッド命名規則：

| HTTPメソッド | 操作 | メソッド命名 | 例 |
|------------|------|------------|------|
| GET | 一覧取得 | findAll | findAllEngineers |
| GET | 単一取得 | findById | findEngineerById |
| POST | 作成 | create | createEngineer |
| PUT | 更新 | update | updateEngineer |
| PATCH | 部分更新 | partialUpdate | partialUpdateEngineer |
| DELETE | 削除 | delete | deleteEngineer |
| GET | 検索 | search | searchEngineers |

### レスポンス実装

APIレスポンスの統一形式：

```json
{
  "data": {
    // リソースデータまたはリソースのリスト
  },
  "meta": {
    // メタデータ（ページネーション情報など）
  }
}
```

エラーレスポンスの統一形式：

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "エラーメッセージ",
    "details": [
      {
        "field": "フィールド名",
        "message": "詳細なエラーメッセージ"
      }
    ]
  }
}
```

## 3. API開発プロセス

1. OpenAPI仕様の作成/更新
2. APIコントローラの実装
3. サービス層の実装
4. 統合テストの作成
5. APIドキュメントの生成・確認

## 4. リンク

- [基本設計: REST API設計概要](/docs/02_基本設計/IF設計/REST_API設計_概要.html)
- [OpenAPI定義ファイル](/docs/03_詳細設計/04_API/OpenAPI/)