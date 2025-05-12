# 案件管理モジュール テーブル定義補足

## 概要
案件管理モジュールは、SES事業における案件（プロジェクト）の登録、検索、ステータス管理、要員要件管理など案件のライフサイクル全体を管理するためのテーブル設計を提供します。

## テーブル構成

| テーブル名 | 説明 | 主要列 |
|----------|------|-------|
| project | 案件基本情報 | project_id, project_code, project_name, status_id |
| project_status | 案件ステータス | status_id, status_code, status_name |
| project_status_history | 案件ステータス履歴 | history_id, project_id, previous_status_id, new_status_id |
| resource_requirement | 要員要件 | requirement_id, project_id, position_name, required_number |
| skill_requirement | スキル要件 | skill_requirement_id, resource_requirement_id, skill_definition_id |
| project_budget | 案件予算情報 | budget_id, project_id, budget_type, amount |
| project_document | 案件ドキュメント | document_id, project_id, document_type, title, file_id |
| project_member | 案件担当者 | member_id, project_id, user_id, role_type |

## ドキュメント構成

| ドキュメント | 内容 | 最終更新日 |
|------------|------|----------|
| [01_概要](./01_概要.md) | テーブル構成の概要、関連図 | 2025-05-11 |
| [02_基本情報](./02_基本情報.md) | 案件基本情報テーブル詳細 | 2025-05-11 |
| [03_ステータス管理](./03_ステータス管理.md) | 案件ステータス管理テーブル詳細 | 2025-05-11 |
| [04_関連情報](./04_関連情報.md) | 案件関連情報テーブル詳細 | 2025-05-11 |

## インターフェーステーブル

| テーブル名 | 連携モジュール | 概要 |
|----------|--------------|------|
| resource_requirement | 技術者管理 | 技術者情報との連携、アサイン管理 |
| skill_requirement | 技術者管理 | 技術者スキルとの連携、マッチング |
| project_budget | 請求支払管理 | 案件予算情報と請求情報の連携 |
| project | マッチング | マッチング対象案件情報の提供 |

## データ量見積もり

| テーブル名 | 想定レコード数 | 増加率 | 備考 |
|----------|--------------|-------|------|
| project | 10,000 | 月300件 | 年間約3,600件の新規案件 |
| project_status_history | 50,000 | 月1,500件 | 案件あたり平均5回のステータス変更 |
| resource_requirement | 30,000 | 月900件 | 案件あたり平均3件の要員要件 |
| skill_requirement | 150,000 | 月4,500件 | 要員要件あたり平均5件のスキル要件 |
| project_document | 40,000 | 月1,200件 | 案件あたり平均4件のドキュメント |