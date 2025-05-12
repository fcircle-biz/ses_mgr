# レポーティングモジュール テーブル定義補足

## 概要
レポーティングモジュールは、SES業務システム全体から収集したデータを分析・可視化し、経営判断に必要なKPI、各種レポート、予測分析を提供します。ユーザー定義のカスタムレポート作成および自動配信機能も備えており、データに基づく意思決定をサポートします。

## テーブル構成
| テーブル名 | 説明 | 主要列 |
|----------|------|-------|
| dashboards | ダッシュボード設定 | dashboard_id, name, user_id, layout_config |
| widgets | ダッシュボードウィジェット | widget_id, dashboard_id, title, type, data_source_id |
| kpi_definitions | KPI指標定義 | kpi_id, code, name, category, calculation_formula |
| kpi_targets | KPI目標値 | target_id, kpi_id, target_type, target_year, target_value |
| kpi_achievements | KPI実績値 | achievement_id, kpi_id, target_id, achievement_value |
| reports | レポート定義 | report_id, code, name, category, type, template_path |
| report_executions | レポート実行履歴 | execution_id, report_id, status, parameters, output_path |
| report_schedules | レポートスケジュール | schedule_id, report_id, cron_expression, recipients |
| data_sources | データソース定義 | data_source_id, name, type, connection_details, query_text |
| forecast_models | 予測モデル | model_id, name, type, target, algorithm, parameters |
| forecast_scenarios | 予測シナリオ | scenario_id, model_id, name, parameters, variables |
| custom_reports | カスタムレポート | custom_report_id, name, data_source_ids, query_config |

## ファイル構成
| ファイル名 | 説明 |
|-----------|------|
| README.md | 本ドキュメント |
| 01_概要.md | ER図とスキーマ設計概要 |
| 02_ダッシュボード.md | ダッシュボードとウィジェットテーブルの定義 |
| 03_KPI管理.md | KPI定義、目標、実績テーブルの定義 |
| 04_レポート管理.md | レポート定義、実行履歴、スケジュールテーブルの定義 |
| 05_データソース.md | データソーステーブルの定義 |
| 06_予測分析.md | 予測モデルとシナリオテーブルの定義 |
| 07_カスタムレポート.md | カスタムレポートテーブルの定義 |

## インターフェースポイント

### 他モジュールとの連携
- **共通機能モジュール**: ユーザー情報、権限管理、ファイル管理、通知管理の利用
- **請求支払管理モジュール**: 売上データ、粗利データの取得
- **勤怠工数管理モジュール**: 稼働データの取得
- **技術者管理モジュール**: 技術者情報の取得
- **案件管理モジュール**: 案件情報の取得

### 外部システム連携
- **メール配信システム**: スケジュールレポートの配信
- **BIツール**: データエクスポート
- **データウェアハウス**: データ連携

## データ量見積もり
- ダッシュボード: ユーザー数(100) × 平均3ダッシュボード = 約300件
- ウィジェット: ダッシュボード数(300) × 平均8ウィジェット = 約2,400件
- KPI実績: KPI指標数(50) × 12ヶ月 × 5年分 = 約3,000件
- レポート実行履歴: 月間実行数(1,000) × 保存期間(3ヶ月) = 約3,000件
- カスタムレポート: ユーザー数(100) × 平均5レポート = 約500件

## 検索パターン
- ユーザーIDによるダッシュボード検索
- 期間によるKPI実績検索
- カテゴリによるレポート検索
- ステータスによるレポート実行履歴検索
- 予測モデルによるシナリオ検索