# 請求支払管理モジュール テーブル定義補足

## 概要
請求支払管理モジュールは、SES業務システムにおけるクライアントへの請求書発行、入金管理、技術者や協力会社への支払処理を一元管理します。本モジュールは、請求情報の生成から支払処理までの資金の流れを追跡し、適切な財務管理を可能にします。

## テーブル構成
| テーブル名 | 説明 | 主要列 |
|----------|------|-------|
| invoices | 請求情報 | invoice_id, invoice_number, customer_id, billing_year, billing_month, billing_status |
| invoice_items | 請求明細情報 | invoice_item_id, invoice_id, item_type, quantity, unit_price, amount |
| payments | 支払情報 | payment_id, payment_number, company_id, engineer_id, payment_year, payment_month, payment_status |
| payment_items | 支払明細情報 | payment_item_id, payment_id, item_type, quantity, unit_price, amount |
| bank_accounts | 銀行口座情報 | account_id, account_type, bank_name, account_number, account_name |
| payment_approvals | 支払承認フロー情報 | approval_id, payment_id, approver_id, approval_order, status |
| receipts | 入金情報 | receipt_id, invoice_id, receipt_date, amount, payment_method |
| receipt_allocations | 入金割当情報 | allocation_id, receipt_id, invoice_id, amount |
| finance_settings | 財務設定情報 | setting_id, setting_key, setting_value, setting_type |

## ファイル構成
| ファイル名 | 説明 |
|-----------|------|
| README.md | 本ドキュメント |
| 01_概要.md | ER図とスキーマ設計概要 |
| 02_請求管理.md | 請求テーブルと請求明細テーブルの定義 |
| 03_支払管理.md | 支払テーブルと支払明細テーブルの定義 |
| 04_銀行口座管理.md | 銀行口座テーブルの定義 |
| 05_支払承認管理.md | 支払承認フローテーブルの定義 |
| 06_入金管理.md | 入金テーブルと入金割当テーブルの定義 |
| 07_財務設定管理.md | 財務設定テーブルの定義 |

## インターフェースポイント

### 他モジュールとの連携
- **契約管理モジュール**: 契約情報と料金条件の取得
- **勤怠工数管理モジュール**: 稼働実績データの取得
- **技術者管理モジュール**: 技術者情報の参照
- **案件管理モジュール**: 案件情報の参照
- **共通機能モジュール**: ファイル管理、通知管理、コード値管理の利用

### 外部システム連携
- **会計システム**: 仕訳データ連携
- **銀行システム**: 振込データ連携

## データ量見積もり
- 請求テーブル: 顧客数(100) × 12ヶ月 = 約1,200件/年
- 請求明細テーブル: 請求件数(1,200) × 平均5明細 = 約6,000件/年
- 支払テーブル: (技術者数(300) + 協力会社数(50)) × 12ヶ月 = 約4,200件/年
- 支払明細テーブル: 支払件数(4,200) × 平均3明細 = 約12,600件/年
- 入金テーブル: 請求件数(1,200) × 平均1.2入金 = 約1,440件/年

## 検索パターン
- 期間(年月)による請求・支払情報検索
- 顧客・案件・技術者・協力会社別の請求・支払情報検索
- ステータスによる請求・支払情報検索
- 未入金請求書の検索
- 支払予定情報の検索