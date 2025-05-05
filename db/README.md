# SES管理システム データベーススキーマ

## 概要

このディレクトリには、SES管理システムのデータベーススキーマ定義が含まれています。スキーマはPostgreSQLを使用して実装されています。

## ファイル構造

- `schema.sql` - PostgreSQLスキーマの完全な定義。テーブル作成、制約、インデックス、トリガーなどを含みます。
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

開発環境でデータベースを初期化するには：

1. PostgreSQLがインストールされていることを確認
2. 以下のコマンドでデータベースを作成
   ```
   createdb ses_mgr
   ```
3. スキーマを作成
   ```
   psql -d ses_mgr -f schema.sql
   ```

または、Spring Bootアプリケーション起動時にLiquibaseマイグレーションが自動的に実行されます。