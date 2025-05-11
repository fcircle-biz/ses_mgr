# 勤怠工数管理モジュール テーブル定義補足

## 概要
勤怠工数管理モジュールは、SES業務システムにおける技術者の勤怠情報と工数実績を管理し、適切な承認ワークフローを通じて請求・支払データの元となる情報を提供します。日次の勤怠データ（出退勤時間、休憩時間、勤務場所など）、工数データ（作業内容、工数、請求可否など）、月次のタイムシート作成と承認プロセスをサポートするためのテーブル設計を提供します。

## テーブル構成

| テーブル名 | 説明 | 主要列 |
|----------|------|-------|
| monthly_attendances | 月次勤怠情報 | monthly_attendance_id, engineer_id, target_year, target_month, status |
| daily_attendances | 日次勤怠情報 | daily_attendance_id, monthly_attendance_id, attendance_date, working_hours |
| work_hours | 作業工数情報 | work_hour_id, daily_attendance_id, project_id, task_name, work_hours |
| approval_flows | 承認フロー管理 | flow_id, monthly_attendance_id, current_step, status |
| approval_steps | 承認ステップ情報 | step_id, flow_id, step_number, approver_id, status |
| work_settings | 勤務設定情報 | setting_id, engineer_id, project_id, regular_work_hours |
| holidays | 休日カレンダー | holiday_id, holiday_date, holiday_name, holiday_type |
| attendance_notifications | 勤怠関連通知 | notification_id, notification_type, user_id, title |

## ドキュメント構成

| ドキュメント | 内容 | 最終更新日 |
|------------|------|----------|
| [01_概要](./01_概要.md) | テーブル構成の概要、関連図 | YYYY-MM-DD |
| [02_勤怠管理](./02_勤怠管理.md) | 月次・日次勤怠テーブルの詳細 | YYYY-MM-DD |
| [03_工数管理](./03_工数管理.md) | 工数テーブル詳細 | YYYY-MM-DD |
| [04_承認管理](./04_承認管理.md) | 承認フローと承認ステップテーブルの詳細 | YYYY-MM-DD |
| [05_勤務設定](./05_勤務設定.md) | 勤務設定と休日カレンダーテーブルの詳細 | YYYY-MM-DD |

## インターフェーステーブル

| テーブル名 | 連携モジュール | 概要 |
|----------|--------------|------|
| monthly_attendances | 技術者管理、契約管理 | 技術者の契約に基づく月次勤怠情報 |
| work_hours | 案件管理 | 案件ごとの作業工数実績 |
| approval_flows | 共通（ユーザー管理） | 承認者情報と承認状態の管理 |
| monthly_attendances | 請求支払管理 | 確定した勤怠情報を請求データとして提供 |

## データ量見積もり

| テーブル名 | 想定レコード数 | 増加率 | 備考 |
|----------|--------------|-------|------|
| monthly_attendances | 10,000 | 月1,000件 | 技術者数×12ヶ月 |
| daily_attendances | 200,000 | 月20,000件 | 技術者数×営業日×12ヶ月 |
| work_hours | 500,000 | 月50,000件 | 日次勤怠×平均2.5タスク |
| approval_flows | 10,000 | 月1,000件 | 月次勤怠と1:1 |
| approval_steps | 30,000 | 月3,000件 | 承認フロー×平均3ステップ |
| work_settings | 1,000 | 月10件 | 技術者数＋追加設定 |
| holidays | 200 | 年50件 | 祝日＋会社休業日 |
| attendance_notifications | 100,000 | 月10,000件 | 各種通知・リマインダー |