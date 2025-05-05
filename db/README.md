# SES管理システム データベーススキーマ

## 概要

このディレクトリには、SES管理システムのデータベーススキーマ定義が含まれています。スキーマはPostgreSQLを使用して実装されています。

## ファイル構造

- `schema.sql` - PostgreSQLスキーマの完全な定義。テーブル作成、制約、インデックス、トリガーなどを含みます。
- スキーマと分割されたSQLファイル:
  - `00_init.sql` - 拡張機能、ユーティリティ関数、監査テーブルなどの初期設定
  - `01_organization.sql` - 組織構造関連テーブル
  - `02_company.sql` - 企業・顧客関連テーブル
  - `03_engineer.sql` - エンジニア管理関連テーブル
  - `04_project.sql` - 案件管理関連テーブル
  - `05_matching.sql` - マッチングエンジン関連テーブル
  - `06_attendance.sql` - 勤怠工数管理関連テーブル
  - `07_reporting.sql` - レポーティング関連テーブル
  - `08_notification.sql` - 通知システム関連テーブル
  - `09_utility.sql` - ユーティリティテーブル（祝日など）
  - `90_create_foreign_keys.sql` - 循環参照を解決するための外部キー制約を定義
- テストデータ:
  - `10_test_data_common.sql` - 共通テストデータ（部署、ユーザー、権限など）
  - `11_test_data_company_engineer.sql` - 企業とエンジニア関連テストデータ
  - `12_test_data_project.sql` - 案件関連テストデータ
  - `13_test_data_matching.sql` - マッチング関連テストデータ
  - `14_test_data_attendance.sql` - 勤怠と工数関連テストデータ
  - `15_test_data_reporting.sql` - レポートと分析関連テストデータ
- アプリケーションでは、Liquibaseを使用してマイグレーションを管理しています: `src/main/resources/db/changelog/`

## 主要テーブル

### ユーザー・権限管理
- `users` - システムユーザー
- `roles` - ユーザーロール
- `permissions` - 権限
- `role_permissions` - ロールと権限のマッピング
- `user_roles` - ユーザーとロールのマッピング

### 企業・顧客管理
- `companies` - 企業情報（自社、パートナー企業、顧客企業）
- `customers` - 顧客情報

### エンジニア管理
- `engineers` - エンジニア基本情報
- `skills` - スキルマスタ
- `engineer_skills` - エンジニアのスキル情報
- `certifications` - 資格情報
- `work_experiences` - 職務経歴

### 案件管理
- `projects` - 案件情報
- `project_requirements` - 案件要件
- `project_documents` - 案件関連文書

### マッチング管理
- `matching` - エンジニアと案件のマッチング情報
- `matching_history` - マッチング履歴
- `interview_schedules` - 面談スケジュール
- `interview_results` - 面談結果

### 勤怠・工数管理
- `monthly_attendances` - 月次勤怠情報
- `daily_attendances` - 日次勤怠情報
- `work_hours` - 工数情報
- `approval_flows` - 承認フロー
- `approval_steps` - 承認ステップ

### レポーティング
- `dashboards` - ダッシュボード定義
- `widgets` - ダッシュボードウィジェット
- `kpi_definitions` - KPI定義
- `kpi_targets` - KPI目標値
- `kpi_achievements` - KPI実績値

## データベース設定

アプリケーションでは以下の設定でデータベースに接続します：

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ses_mgr
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## 開発環境での設定

### 手動でのデータベース初期化

開発環境でデータベースを手動で初期化するには：

1. PostgreSQLがインストールされていることを確認
2. 以下のコマンドでデータベースを作成
   ```
   createdb ses_mgr
   ```
3. 全てのスキーマを一度に作成する場合
   ```
   psql -d ses_mgr -f schema.sql
   ```
4. 分割されたSQLファイルで作成する場合（順番に実行する必要があります）
   ```
   psql -d ses_mgr -f 00_init.sql
   psql -d ses_mgr -f 01_organization.sql
   psql -d ses_mgr -f 02_company.sql
   ...（以下同様に全てのSQLファイルを順に実行）...
   ```

### 循環参照の解決

一部のテーブル間には循環参照の関係があります：

1. `engineers` テーブルと `projects` テーブル
   - エンジニアは現在のプロジェクトを参照 (`engineers.current_project -> projects.project_id`)
   - プロジェクト要件は担当エンジニアを参照 (`project_requirements.assigned_personnel -> engineers.engineer_id`)

このような循環参照を解決するために、テーブル作成時には一部の外部キー制約を省略し、
すべてのテーブルが作成された後に `90_create_foreign_keys.sql` で追加しています。

### Docker Composeでの初期化

Docker Composeを使用する場合は、以下のコマンドで環境を起動できます：

```
docker compose up -d
```

docker-compose.ymlファイルには、分割されたSQLファイルを順番に読み込むように設定されています。データベースは自動的に初期化され、テストデータも登録されます。

注意: Docker Compose 2.x 以降を使用している場合、コマンドは `docker compose` です (ハイフンなし)。

### アプリケーションでの初期化

Spring Bootアプリケーション起動時にLiquibaseマイグレーションが自動的に実行されます。