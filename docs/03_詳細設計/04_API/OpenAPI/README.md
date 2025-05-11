# SES業務システム Swagger API定義

このディレクトリには、SES業務システムのREST API仕様をOpenAPI (Swagger) 形式で定義したファイルが含まれています。

## ファイル構成

OpenAPI仕様書は機能グループごとにフォルダ分けされています。

```
OpenAPI/
├── README.md                # このファイル
├── index.yaml               # 全APIの統合ファイル
├── openapi.yaml             # OpenAPI基本定義
├── common/                  # 共通コンポーネント
│   └── openapi.yaml         # 共通コンポーネント定義
├── auth/                    # 認証・認可API
│   └── auth.yaml            # 認証API仕様
├── projects/                # 案件管理API
│   └── projects.yaml        # 案件管理API仕様
├── engineers/               # 技術者管理API
│   └── engineers.yaml       # 技術者管理API仕様
├── matching/                # マッチング機能API
│   └── matching.yaml        # マッチング機能API仕様
├── contracts/               # 契約管理API
│   └── contracts.yaml       # 契約管理API仕様
├── timesheet/               # 勤怠工数管理API
│   └── timesheet.yaml       # 勤怠工数管理API仕様
└── billing/                 # 請求支払管理API
    ├── billing.yaml         # 請求支払管理API基本定義
    ├── billing_common.yaml  # 請求支払管理API共通定義
    ├── invoices/            # 請求管理API
    │   ├── billing_invoices.yaml          # 請求管理API基本定義
    │   ├── billing_invoices_base.yaml     # 請求書基本操作API
    │   ├── billing_invoices_search.yaml   # 請求書検索API
    │   ├── billing_invoice_items.yaml     # 請求書明細項目API
    │   └── billing_invoice_operations.yaml # 請求書関連操作API
    ├── payments/            # 支払管理API
    │   ├── billing_payments.yaml          # 支払管理API基本定義
    │   ├── billing_payments_base.yaml     # 支払基本操作API
    │   ├── billing_payments_search.yaml   # 支払検索API
    │   ├── billing_payment_items.yaml     # 支払明細項目API
    │   ├── billing_payment_approvals.yaml # 支払承認API
    │   └── billing_payment_operations.yaml # 支払関連操作API
    ├── receipts/            # 入金管理API
    │   ├── billing_receipts.yaml          # 入金管理API基本定義
    │   ├── billing_receipts_base.yaml     # 入金基本操作API
    │   ├── billing_receipts_search.yaml   # 入金検索API
    │   ├── billing_receipt_allocations.yaml # 入金配分API
    │   └── billing_receipt_operations.yaml  # 入金関連操作API
    ├── reports/             # 請求支払レポートAPI
    │   └── billing_reports.yaml           # 請求支払レポートAPI仕様
    └── bank_accounts/       # 銀行口座管理API
        └── billing_bank_accounts.yaml     # 銀行口座管理API仕様
```

## 相互参照の修正方法

フォルダ構成変更に伴い、ファイル間の相互参照パスを更新する必要があります。
具体的には以下のような修正が必要です:

### 1. billing_common.yaml への参照

- 元のパス: `./billing_common.yaml`
- 新しいパス: `../billing_common.yaml` (billing/サブディレクトリからの参照)

### 2. billing_invoices.yaml などへの参照

- index.yaml からの参照:
  - 元のパス: `./billing_invoices.yaml`
  - 新しいパス: `./billing/invoices/billing_invoices.yaml`

- サブディレクトリ内のファイル間の参照:
  元のパス: `./billing_invoice_items.yaml`
  新しいパス: `./billing_invoice_items.yaml` (同一ディレクトリ内は変更なし)

## 相互参照修正コマンド例

ファイル数が多いため、一括置換を行う場合は以下のようなコマンドが利用できます:

```bash
# billing/サブディレクトリから親ディレクトリのbilling_common.yamlへの参照を修正
find /Users/s-ichimaru/git/ses_mgr/docs/03_詳細設計/04_API/OpenAPI/billing/ -type f -name "*.yaml" -exec sed -i '' 's/$ref: \'\.\/billing_common\.yaml/$ref: \'\.\.\/billing_common\.yaml/g' {} \;
```

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
- パスの参照には正確な記法を使用（例: `$ref: './auth/auth.yaml#/paths/~1auth~1login'`）
- スキーマの再利用を心がけ、DRYの原則に従う
- 相互参照が正しく更新されない場合、OpenAPIツールでバリデーションエラーが発生します
- index.yamlは全てのAPIエンドポイントの参照を集約したマスターファイルです