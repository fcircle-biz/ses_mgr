# エラー処理機能 詳細設計

## 概要
SES業務システムにおいて発生するさまざまな例外や異常状態を適切に処理し、ユーザーにわかりやすいフィードバックを提供するとともに、開発者・運用者が問題を迅速に特定して対応できるようにする基盤的な共通機能。システム全体で一貫したエラー処理方法を提供し、安定性と保守性を向上させる。Spring Frameworkの標準機能を最大限に活用し、保守性と拡張性の高いエラー処理を実現する。

## ドキュメント構成

| ドキュメント | 内容 | 最終更新日 |
|------------|------|----------|
| [01_概要](./01_概要.md) | モジュールの目的、主要機能、アーキテクチャ | 2025-05-15 |
| [02_ドメインモデル](./02_ドメインモデル.md) | ProblemDetail、エラーコード、例外階層 | 2025-05-15 |
| [03_インターフェース定義](./03_インターフェース定義.md) | ProblemDetailFactory、ErrorEventListener | 2025-05-15 |
| [04_統一エラーモデル](./04_統一エラーモデル.md) | RFC 7807準拠のProblemDetail活用設計 | 2025-05-15 |
| [05_例外ハンドリング](./05_例外ハンドリング.md) | Spring MVC例外ハンドリングと変換 | 2025-05-15 |
| [06_エラーログ機能](./06_エラーログ機能.md) | Spring Eventベースのエラーログ処理 | 2025-05-15 |

## 提供インターフェース一覧

| インターフェース名 | 概要 | 利用モジュール |
|-----------------|------|----------------|
| GlobalExceptionHandler | Spring標準の集中例外ハンドリングを提供 | 全モジュール |
| ProblemDetailFactory | RFC 7807準拠のProblemDetail生成を提供 | 全モジュール |
| ErrorEventListener | エラーイベント処理とログ記録を提供 | 全モジュール |
| ErrorLogRepository | エラーログの永続化と検索を提供 | 全モジュール |

## 要求インターフェース一覧

| インターフェース名 | 提供モジュール/フレームワーク | 概要 |
|-----------------|--------------|------|
| MessageSource | Spring Framework | メッセージの国際化と解決に利用 |
| CrudRepository | Spring Data JDBC | エラーログの永続化に利用 |
| ApplicationEventPublisher | Spring Framework | エラーイベントの発行に利用 |
| ErrorAttributesCustomizer | Spring Boot | エラー属性のカスタマイズに利用 |
| MeterRegistry | Micrometer (Spring Boot) | エラーメトリクスの収集に利用 |
| ObjectMapper | Jackson (Spring Boot) | エラーJSONシリアライズに利用 |
| Clock | java.time | タイムスタンプの一貫性確保 |
| TraceContextProvider | Spring Cloud | トレースIDの取得に利用 |

## 関連モジュール

- [ロギング機能](../06_ロギング機能/)
- [通知機能](../09_通知機能/)
- [システム管理機能](../../03_管理系モジュール/システム管理機能/)

## Spring標準機能の活用

| Spring機能 | 活用方法 | メリット |
|-----------|--------|----------|
| ProblemDetail | RFC 7807準拠のエラーレスポンス | 標準的なエラー表現、API設計の一貫性 |
| @ControllerAdvice/@RestControllerAdvice | グローバル例外ハンドリング | コードの集約、一貫性の確保 |
| ResponseEntityExceptionHandler | Spring MVC例外の標準ハンドリング | 標準例外の自動処理 |
| @ExceptionHandler | 例外タイプ別ハンドリング | 例外処理の分離と明確化 |
| ApplicationEvent & @EventListener | イベント駆動エラー処理 | 疎結合アーキテクチャ |
| @Async | 非同期エラーログ処理 | パフォーマンス向上 |
| Spring Data JDBC | エラーログの永続化 | 宣言的データアクセス |
| Spring Cache & @Cacheable | エラーメッセージとログ検索結果のキャッシング | 性能最適化 |
| Spring Boot Actuator | エラーメトリクスとカスタムエンドポイント | 運用監視の統合 |
| Spring Integration | 外部システムへの通知連携 | メッセージ指向の柔軟な連携 |
| Spring AOP | 機密情報マスキング | 横断的関心事の分離 |
| Spring Profiles | 環境別設定 | 環境に応じたログ詳細度の制御 |
| Spring Scheduling | エラーログの自動クリーンアップ | 定期実行タスクの管理