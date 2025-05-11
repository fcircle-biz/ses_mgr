# 技術者管理モジュール テーブル定義補足

## 概要
技術者管理モジュールは、SES業務システムにおいて技術者の基本情報、スキル、稼働状況、資格情報などを統合的に管理するためのテーブル群を提供します。マッチングや案件管理、勤怠工数管理などの他モジュールと連携し、効率的な技術者リソース管理を実現します。

## テーブル構成

| テーブル名 | 説明 | 主要列 |
|----------|------|-------|
| engineer | 技術者の基本情報を管理 | id, employee_code, last_name, first_name, email, etc. |
| engineer_skill | 技術者のスキル情報を管理 | id, engineer_id, skill_definition_id, skill_level, etc. |
| skill_definition | スキル定義マスタ | id, skill_code, skill_name, skill_category_id, etc. |
| skill_category | スキルカテゴリマスタ | id, category_code, category_name, parent_category_id, etc. |
| certification | 技術者の保有資格情報を管理 | id, engineer_id, certification_type_id, acquisition_date, etc. |
| certification_type | 資格種別マスタ | id, code, name, issuing_organization, etc. |
| engineer_availability | 技術者の稼働状況を管理 | id, engineer_id, availability_type, start_date, end_date, etc. |
| work_history | 技術者の職務経歴を管理 | id, engineer_id, company_name, project_name, role, etc. |
| skill_sheet | 技術者のスキルシートを管理 | id, engineer_id, template_id, version, title, etc. |
| skill_sheet_template | スキルシートテンプレートを管理 | id, template_name, template_format, etc. |

## ドキュメント構成

| ドキュメント | 内容 | 最終更新日 |
|------------|------|----------|
| [01_基本情報.md](./01_基本情報.md) | 技術者基本情報テーブル(engineer)の詳細 | 2025-05-11 |
| [02_スキル.md](./02_スキル.md) | スキル関連テーブル(engineer_skill, skill_definition, skill_category)の詳細 | 2025-05-11 |
| [03_稼働状況.md](./03_稼働状況.md) | 稼働状況管理テーブル(engineer_availability)の詳細 | 2025-05-11 |
| [04_スキルシート.md](./04_スキルシート.md) | スキルシート関連テーブル(skill_sheet, skill_sheet_template)の詳細 | 2025-05-11 |

## インターフェーステーブル

| テーブル名 | 連携モジュール | 概要 |
|----------|--------------|------|
| engineer | マッチング, 契約管理, 勤怠工数管理 | 技術者基本情報を他モジュールに提供 |
| engineer_skill | マッチング | スキル情報をマッチングに提供 |
| engineer_availability | マッチング, 勤怠工数管理, レポーティング | 稼働状況を他モジュールに提供 |
| skill_sheet | マッチング, 案件管理 | スキルシート情報を他モジュールに提供 |

## データ量見積もり

| テーブル名 | 想定レコード数 | 増加率 | 備考 |
|----------|--------------|-------|------|
| engineer | 10,000 | 月100件 | 技術者数に相当 |
| engineer_skill | 200,000 | 月2,000件 | 1技術者あたり平均20スキル |
| skill_definition | 1,000 | 年100件 | スキルマスタ |
| skill_category | 100 | 年10件 | スキルカテゴリマスタ |
| certification | 30,000 | 月300件 | 1技術者あたり平均3資格 |
| engineer_availability | 30,000 | 月1,000件 | 技術者の稼働状況履歴 |
| work_history | 50,000 | 月500件 | 1技術者あたり平均5件の職務経歴 |
| skill_sheet | 20,000 | 月500件 | 技術者ごとの複数バージョン |