# CLAUDE.md

このファイルは、claude.ai/code の Claude Code がこのリポジトリのコードを操作する際のガイダンスを提供します。

## リポジトリ概要

SES業務システムは、SES（システムエンジニアリングサービス）事業者向けの統合業務管理システムです。技術者管理、案件管理、マッチング、契約管理、勤怠工数管理、請求支払管理、レポーティングなどの機能を提供し、SES事業の効率化と収益性向上を支援します。

## プロジェクト構成

このリポジトリは現在、設計ドキュメントに重点を置いており、実装は将来的に計画されています：

```
/
├── docs/                # ドキュメント
│   ├── 00_ガイドライン/  # 開発ガイドライン
│   ├── 01_要件定義/      # 要件定義ドキュメント
│   ├── 02_基本設計/      # 基本設計ドキュメント
│   └── 03_詳細設計/      # 詳細設計ドキュメント
```

## 技術スタック

実装が開始されると、以下の技術が使用される予定です：

- **バックエンド**: Java 21, Spring Boot 3.2.x
- **データアクセス**: Spring Data JDBC, PostgreSQL 17
- **フロントエンド**: Thymeleaf, Bootstrap 5, jQuery
- **インフラストラクチャ**: Docker, GitHub Actions
- **テスト**: JUnit 5, Mockito, TestContainers, Playwright
- **監視**: Prometheus, Grafana, ELK Stack

## アーキテクチャ

システムはモノリシックアーキテクチャを採用し、論理的に分離された機能モジュールを備えた多層構造で構成されます：

- **プレゼンテーション層**: Web UI（Thymeleaf, Bootstrap）、RESTful API（Spring MVC）
- **アプリケーション層**: コントローラー（Spring MVC）、サービス（Spring Service）
- **ドメイン層**: ドメインモデル、ドメインサービス、ビジネスロジック
- **データアクセス層**: リポジトリ（Spring Data JDBC）
- **共通基盤層**: 認証（Spring Security, JWT）、例外処理、ロギング、キャッシュ

### コアモジュール

1. **技術者管理**: 基本情報、スキル、稼働状況の管理
2. **案件管理**: 案件情報の登録、管理、検索
3. **マッチングエンジン**: 技術者と案件のマッチング
4. **契約管理**: 契約の作成、管理、電子署名
5. **勤怠工数管理**: 勤怠入力、承認、工数管理
6. **請求支払管理**: 請求書発行、入金管理、支払処理
7. **レポーティング**: 各種レポート、ダッシュボード

## 開発標準

- **パッケージ構造**: `jp.co.example.sesapp.[モジュール名].[レイヤー名]`
- **クラス命名規則**:
  - コントローラー: `[機能名]Controller`（例: `EngineerController`）
  - サービス: `[機能名]Service`（例: `EngineerService`）
  - リポジトリ: `[機能名]Repository`（例: `EngineerRepository`）
  - エンティティ: 単数形の名詞（例: `Engineer`）

- **メソッド命名規則**:
  - 取得系: `findBy[条件]`, `getBy[条件]`
  - 作成系: `create[エンティティ]`, `register[エンティティ]`
  - 更新系: `update[エンティティ]`
  - 削除系: `delete[エンティティ]`, `remove[エンティティ]`

## コマンド

実装が開始されると、以下のようなコマンドが使用される可能性があります：

### ビルドと実行

```bash
# プロジェクトのビルド
./gradlew build

# アプリケーションの実行
./gradlew bootRun

# 特定のプロファイルでの実行
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### テスト

```bash
# 全テストの実行
./gradlew test

# 特定のテストクラスの実行
./gradlew test --tests "jp.co.example.sesapp.engineer.EngineerServiceTest"

# テストカバレッジレポートの生成
./gradlew test jacocoTestReport
```

### データベース

```bash
# データベースマイグレーションの実行
./gradlew flywayMigrate

# データベースのクリーンアップ
./gradlew flywayClean
```

### Docker

```bash
# Dockerイメージのビルド
docker build -t ses-mgr .

# docker-composeでの実行（開発環境）
docker-compose up -d
```

## 現状に関する注意

このリポジトリは現在、設計フェーズにあります。実際の実装（コードやビルドコマンドを含む）は、ここに記載されているものとは異なる可能性があります。実装が開始されたら、このファイルを更新する必要があります。