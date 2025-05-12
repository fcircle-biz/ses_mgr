# 契約管理モジュール テーブル定義補足

## 概要
契約管理モジュールは、SES業務システムにおける契約書の作成から締結、管理に至るまでの一連のプロセスをサポートするためのテーブル設計を提供します。契約テンプレート管理、契約基本情報管理、契約書ドキュメント管理、電子署名プロセス管理などの機能に必要なデータ構造を定義しています。

## テーブル構成

| テーブル名 | 説明 | 主要列 |
|----------|------|-------|
| contracts | 契約基本情報 | contract_id, contract_number, project_id, engineer_id, contract_type, status |
| contract_changes | 契約変更履歴 | change_id, contract_id, change_type, previous_value, new_value |
| contract_document_history | 契約書バージョン履歴 | document_history_id, contract_id, version, document_path |
| e_signatures | 電子署名情報 | signature_id, contract_id, service_provider, status |
| signatories | 署名者情報 | signatory_id, signature_id, name, email, status |
| contract_templates | 契約テンプレート | template_id, template_name, contract_type, version |

## ドキュメント構成

| ドキュメント | 内容 | 最終更新日 |
|------------|------|----------|
| [01_概要](./01_概要.md) | テーブル構成の概要、関連図 | YYYY-MM-DD |
| [02_契約基本](./02_契約基本.md) | 契約と変更履歴のテーブル詳細 | YYYY-MM-DD |
| [03_契約書ドキュメント](./03_契約書ドキュメント.md) | 契約書ドキュメントテーブル詳細 | YYYY-MM-DD |
| [04_電子署名](./04_電子署名.md) | 電子署名と署名者のテーブル詳細 | YYYY-MM-DD |
| [05_契約テンプレート](./05_契約テンプレート.md) | 契約テンプレートテーブル詳細 | YYYY-MM-DD |

## インターフェーステーブル

| テーブル名 | 連携モジュール | 概要 |
|----------|--------------|------|
| contracts | 案件管理、技術者管理 | 案件と技術者の契約情報を管理 |
| contracts | 請求支払管理 | 請求・支払処理の基礎となる契約情報を提供 |
| e_signatures | 共通（ファイル管理） | 署名済み文書の保管とアクセス管理 |
| contract_templates | 共通（ファイル管理） | テンプレートファイルの管理 |

## データ量見積もり

| テーブル名 | 想定レコード数 | 増加率 | 備考 |
|----------|--------------|-------|------|
| contracts | 50,000 | 月500件 | 主契約テーブル |
| contract_changes | 100,000 | 月1,000件 | 契約変更履歴（平均2回/契約） |
| contract_document_history | 150,000 | 月1,500件 | 契約書バージョン（平均3版/契約） |
| e_signatures | 50,000 | 月500件 | 電子署名情報（1件/契約） |
| signatories | 150,000 | 月1,500件 | 署名者情報（平均3名/契約） |
| contract_templates | 100 | 年20件 | 契約テンプレート（種類・バージョン含む） |