# 基本設計フェーズのドキュメント

## 概要

基本設計フェーズでは、[基本設計ガイドライン](https://fcircle-biz.github.io/ses_mgr/00_ガイドライン/基本設計ガイドライン.md)に従い、要件定義で明確にした機能要件・非機能要件を技術的に実現するための設計を行いました。このフェーズの成果物は、詳細設計フェーズのインプットとなります。

## 作成ドキュメント

### システム設計
- **システム設計書** - システム全体のアーキテクチャ設計
  - [01_はじめに.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/システム設計/01_はじめに.html)
  - [02_システム概要.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/システム設計/02_システム概要.html)
  - [03_システムアーキテクチャ.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/システム設計/03_システムアーキテクチャ.html)
  - [04_デプロイメント設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/システム設計/04_デプロイメント設計.html)
  - [05_セキュリティ設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/システム設計/05_セキュリティ設計.html)
  - [06_性能設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/システム設計/06_性能設計.html)
  - [07_可用性設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/システム設計/07_可用性設計.html)
  - [08_バックアップ設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/システム設計/08_バックアップ設計.html)
  - [09_バッチ処理設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/システム設計/09_バッチ処理設計.html)
  - [10_インターフェース設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/システム設計/10_インターフェース設計.html)
  - [11_エラー処理設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/システム設計/11_エラー処理設計.html)
  - [12_ログ設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/システム設計/12_ログ設計.html)
  - [13_監視設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/システム設計/13_監視設計.html)
  - [14_移行設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/システム設計/14_移行設計.html)
  - [15_付録.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/システム設計/15_付録.html)

### UI設計
- **UI設計書** - ユーザーインターフェース設計
  - [共通画面設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/UI設計/共通画面設計.html)
  - [技術者管理画面設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/UI設計/技術者管理画面設計.html)
  - [案件管理画面設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/UI設計/案件管理画面設計.html)
  - [マッチング機能画面設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/UI設計/マッチング機能画面設計.html)
  - [契約管理画面設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/UI設計/契約管理画面設計.html)
  - [勤怠工数管理画面設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/UI設計/勤怠工数管理画面設計.html)
  - [請求支払管理画面設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/UI設計/請求支払管理画面設計.html)
  - [レポーティング画面設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/UI設計/レポーティング画面設計.html)
  - [システム管理画面設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/UI設計/システム管理画面設計.html)
  - [画面一覧.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/UI設計/画面一覧.html)
  - **画面仕様書**
    - [画面/](https://fcircle-biz.github.io/ses_mgr/02_基本設計/UI設計/画面/) - 個別画面の詳細仕様
  - **画面遷移図**
    - [画面遷移図/](https://fcircle-biz.github.io/ses_mgr/02_基本設計/UI設計/画面遷移図/) - 各機能の画面遷移図
  - **モックアップ**
    - [画面/*/mockups/](https://fcircle-biz.github.io/ses_mgr/02_基本設計/UI設計/画面/) - 各機能のモックアップ

### DB設計
- **[DB設計/README.md](https://fcircle-biz.github.io/ses_mgr/02_基本設計/DB設計/README.md)** - DB設計の概要と方針
- **ER図** - 各機能領域のER図
  - [マッチングエンジン_ER図.drawio](https://fcircle-biz.github.io/ses_mgr/02_基本設計/DB設計/ER図/マッチングエンジン_ER図.drawio)
  - [技術者管理_ER図.drawio](https://fcircle-biz.github.io/ses_mgr/02_基本設計/DB設計/ER図/技術者管理_ER図.drawio)
  - [案件管理_ER図.drawio](https://fcircle-biz.github.io/ses_mgr/02_基本設計/DB設計/ER図/案件管理_ER図.drawio)
  - [契約・電子署名_ER図.drawio](https://fcircle-biz.github.io/ses_mgr/02_基本設計/DB設計/ER図/契約・電子署名_ER図.drawio)
  - [勤怠・工数管理_ER図.drawio](https://fcircle-biz.github.io/ses_mgr/02_基本設計/DB設計/ER図/勤怠・工数管理_ER図.drawio)
  - [請求・支払_ER図.drawio](https://fcircle-biz.github.io/ses_mgr/02_基本設計/DB設計/ER図/請求・支払_ER図.drawio)
  - [レポーティング_ER図.drawio](https://fcircle-biz.github.io/ses_mgr/02_基本設計/DB設計/ER図/レポーティング_ER図.drawio)
- **テーブル定義書** - 各機能領域のテーブル定義
  - [マッチングエンジン_テーブル定義書.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/DB設計/テーブル定義/マッチングエンジン_テーブル定義書.html)
  - [技術者管理_テーブル定義書.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/DB設計/テーブル定義/技術者管理_テーブル定義書.html)
  - [案件管理_テーブル定義書.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/DB設計/テーブル定義/案件管理_テーブル定義書.html)
  - [契約・電子署名_テーブル定義書.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/DB設計/テーブル定義/契約・電子署名_テーブル定義書.html)
  - [勤怠・工数管理_テーブル定義書.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/DB設計/テーブル定義/勤怠・工数管理_テーブル定義書.html)
  - [請求・支払_テーブル定義書.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/DB設計/テーブル定義/請求・支払_テーブル定義書.html)
  - [レポーティング_テーブル定義書.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/DB設計/テーブル定義/レポーティング_テーブル定義書.html)

### インターフェース設計
- **[REST_API設計_概要.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/IF設計/REST_API設計_概要.html)** - REST API設計の基本方針
- **REST API仕様書**
  - [共通API.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/IF設計/REST_API/共通API.html)
  - [システム管理API.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/IF設計/REST_API/システム管理API.html)
  - [技術者管理API.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/IF設計/REST_API/技術者管理API.html)
  - [案件管理API.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/IF設計/REST_API/案件管理API.html)
  - [マッチング機能API.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/IF設計/REST_API/マッチング機能API.html)
  - [契約管理API.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/IF設計/REST_API/契約管理API.html)
  - [勤怠工数管理API.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/IF設計/REST_API/勤怠工数管理API.html)
  - [請求支払管理API.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/IF設計/REST_API/請求支払管理API.html)
  - [レポーティングAPI.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/IF設計/REST_API/レポーティングAPI.html)
- **OpenAPI仕様**
  - 詳細なOpenAPI (Swagger) 仕様は詳細設計フェーズで作成しています
  - [03_詳細設計/04_API/OpenAPI/](https://fcircle-biz.github.io/ses_mgr/03_詳細設計/04_API/OpenAPI/) を参照してください

### 共通機能設計
- **共通機能設計書**
  - [共通機能アーキテクチャ.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/共通機能アーキテクチャ.html)
  - [共通機能設計対象範囲.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/共通機能設計対象範囲.html)
  - [認証認可機能設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/認証認可機能設計.html)
  - [ユーザー管理機能設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/ユーザー管理機能設計.html)
  - [ロギング機能設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/ロギング機能設計.html)
  - [エラー処理機能設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/エラー処理機能設計.html)
  - [コード値管理機能設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/コード値管理機能設計.html)
  - [検索機能設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/検索機能設計.html)
  - [通知機能設計_01_概要とアーキテクチャ.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/通知機能設計_01_概要とアーキテクチャ.html)
  - [通知機能設計_02_データモデル.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/通知機能設計_02_データモデル.html)
  - [通知機能設計_03_サービスとAPI.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/通知機能設計_03_サービスとAPI.html)
  - [通知機能設計_04_フロントエンドと業務モジュール統合.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/通知機能設計_04_フロントエンドと業務モジュール統合.html)
  - [通知機能設計_05_セキュリティと運用.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/通知機能設計_05_セキュリティと運用.html)
  - [ファイル管理機能設計_01_概要とアーキテクチャ.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/ファイル管理機能設計_01_概要とアーキテクチャ.html)
  - [ファイル管理機能設計_02_データモデル.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/ファイル管理機能設計_02_データモデル.html)
  - [ファイル管理機能設計_03_サービスインターフェース.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/ファイル管理機能設計_03_サービスインターフェース.html)
  - [ファイル管理機能設計_04_API設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/ファイル管理機能設計_04_API設計.html)
  - [ファイル管理機能設計_05_アクセス制御とセキュリティ.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/ファイル管理機能設計_05_アクセス制御とセキュリティ.html)
  - [ファイル管理機能設計_06_業務モジュール統合.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/ファイル管理機能設計_06_業務モジュール統合.html)
  - [ファイル管理機能設計_07_パフォーマンスと運用設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/ファイル管理機能設計_07_パフォーマンスと運用設計.html)
  - [トランザクション管理機能設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/トランザクション管理機能設計.html)
  - [国際化機能設計.html](https://fcircle-biz.github.io/ses_mgr/02_基本設計/共通機能/国際化機能設計.html)

## 技術スタック

基本設計フェーズでは、以下の技術スタックに基づいて設計を行いました：

### バックエンド
- Java 21 LTS
- Spring Boot 3.2.x
  - Spring MVC: Webアプリケーションフレームワーク
  - Spring Data JDBC: データアクセス層
  - Spring Security: 認証・認可（JWT採用）
  - Spring Validation: バリデーション
  - Spring Cache: キャッシュ抽象化
  - Spring Actuator: 運用監視
  - Spring AOP: アスペクト指向プログラミング
- QueryDSL: 型安全なクエリビルダー
- Flyway: データベースマイグレーション

### フロントエンド
- Thymeleaf 3.x: サーバーサイドテンプレートエンジン
- Bootstrap 5.x: レスポンシブUIフレームワーク
- jQuery 3.x: DOM操作とAjax
- Chart.js: データ可視化

### データストア
- PostgreSQL 17: メインデータベース
- Redis 6.x: キャッシュ
- MinIO: S3互換オブジェクトストレージ
- Elasticsearch: 全文検索（必要に応じて）

### インフラストラクチャ
- Apache Tomcat: Spring Boot組み込みサーバー
- Docker: アプリケーションコンテナ化
- GitHub Actions: CI/CD
- 監視・ロギング
  - Prometheus: メトリクス収集
  - Grafana: 可視化
  - ELK Stack: ログ管理

## 基本設計フェーズでの設計判断

基本設計フェーズでは、以下の主要な設計判断を行いました：

1. **モノリシックアーキテクチャの採用**
   - ビジネスケイパビリティに基づく論理的分割
   - モジュール間の責務と境界の明確化
   - 将来のマイクロサービス化を見据えたモジュール間結合度の低減

2. **データベース設計**
   - 機能領域ごとのスキーマ分割
   - PostgreSQLの拡張機能を活用した設計
   - 性能とメンテナンス性のバランスを考慮した正規化レベル

3. **認証・認可方式**
   - JWT（JSON Web Token）を用いたステートレス認証
   - ロールベースアクセス制御（RBAC）
   - 属性ベースアクセス制御（ABAC）の部分的導入

4. **UI設計方針**
   - レスポンシブデザイン対応
   - ユーザー体験の一貫性確保
   - アクセシビリティ配慮

## 次フェーズへの橋渡し

基本設計フェーズで作成した各種設計書は、詳細設計フェーズの入力となります。詳細設計フェーズでは、以下の作業を行います：

1. **アーキテクチャの具体化**
   - [アーキテクチャ決定記録（ADR）](https://fcircle-biz.github.io/ses_mgr/03_詳細設計/08_ADR/)の作成
   - 標準設計パターンの決定

2. **モジュール詳細設計**
   - 各モジュールのコンポーネント構成
   - 主要クラス設計
   - インターフェース定義

3. **API設計の詳細化**
   - OpenAPI (Swagger) 仕様の作成
   - エラーハンドリング詳細

4. **データベース物理設計**
   - インデックス最適化
   - パーティション設計
   - パフォーマンスチューニング

5. **セキュリティ実装設計**
   - 認証・認可メカニズム詳細
   - データ保護対策

## レビュー状況

以下の設計ドキュメントは承認済みです：
- システム設計書
- 共通機能設計書
- ER図とテーブル定義書

以下の設計ドキュメントはレビュー中・更新中です：
- 一部のUI設計書と画面仕様書
- インターフェース設計（REST API仕様）