# ファイル管理機能

## 概要

ファイル管理機能は、SES管理システム全体で使用される各種ファイル（技術者のスキルシート、契約書、請求書など）を統一的に管理するための共通機能モジュールです。ファイルのアップロード、ダウンロード、検索、バージョン管理などの基本機能を提供し、ストレージ実装の詳細を抽象化することで、システムの他の部分から透過的にファイルにアクセスできるようにします。

## 目的

* システム全体で一貫したファイル管理方法を提供する
* ファイルのセキュアな保存と取得を可能にする
* ファイルに関するメタデータを統一的に管理する
* ストレージの実装詳細を抽象化し、将来的な変更に対応しやすくする
* 業務モジュールからシンプルなインターフェースでファイルを操作できるようにする

## 対象読者

* アプリケーション開発者
* システムアーキテクト
* インフラストラクチャエンジニア
* セキュリティ担当者

## ドキュメント構成

| ドキュメント | 説明 |
|------------|------|
| [01_概要](./01_概要.md) | ファイル管理機能の全体像、位置づけ、アーキテクチャを説明 |
| [02_ドメインモデル](./02_ドメインモデル.md) | ファイル管理のドメインモデル（エンティティ、値オブジェクト）を定義 |
| [03_インターフェース定義](./03_インターフェース定義.md) | 提供・依存するインターフェース、API仕様を定義 |
| [04_ファイルストレージ管理](./04_ファイルストレージ管理.md) | 物理ファイルの保存、取得、削除の詳細設計 |
| [05_ファイルメタデータ管理](./05_ファイルメタデータ管理.md) | ファイルメタデータの管理、検索の詳細設計 |

## 主要コンポーネント

* **FileService**: ファイルの登録、取得、削除などの主要操作を提供するサービス
* **FileRepository**: ファイルメタデータのデータアクセスを担当するリポジトリ
* **StorageProvider**: 物理的なファイル操作を抽象化するインターフェース
  * LocalStorageProvider: ローカルファイルシステムに保存する実装
  * S3StorageProvider: Amazon S3に保存する実装
* **FileMetadata**: ファイルに関するメタデータを表現するエンティティ
* **FileController**: REST APIでファイル操作を提供するコントローラ

## 関連するシステムコンポーネント

* 認証・認可機能：ファイルアクセス制御
* 監査ログ機能：ファイル操作の監査記録
* 各業務モジュール：ファイル管理機能を利用して業務ファイルを管理

## 提供インターフェース

ファイル管理機能は以下のインターフェースを提供します：

### 1. FileService インターフェース

ファイル操作の主要エントリポイント。他のモジュールはこのインターフェースを通じてファイル管理機能を利用します。

```java
public interface FileService {
    FileMetadata uploadFile(InputStream content, String fileName, String contentType, 
                         String moduleCode, String entityId, Map<String, String> attributes);
    Optional<FileContent> downloadFile(String fileId);
    void deleteFile(String fileId);
    Optional<FileMetadata> getFileMetadata(String fileId);
    List<FileMetadata> findFilesByEntityReference(String moduleCode, String entityId);
    // その他のメソッド
}
```

### 2. REST API

ファイル操作のためのRESTful APIエンドポイント：

| メソッド | パス | 説明 |
|--------|-----|------|
| POST | /api/files | ファイルのアップロード |
| GET | /api/files/{id} | ファイルのダウンロード |
| GET | /api/files/{id}/metadata | ファイルメタデータの取得 |
| DELETE | /api/files/{id} | ファイルの削除 |
| GET | /api/files/search | ファイルの検索 |

## 依存インターフェース

ファイル管理機能が依存する外部インターフェース：

### 1. 認証・認可サービス

```java
public interface AuthorizationService {
    boolean hasPermission(String userId, String resourceType, String resourceId, String permission);
}
```

### 2. 監査ログサービス

```java
public interface AuditLogService {
    void logAction(String userId, String action, String resourceType, 
                  String resourceId, Map<String, Object> details);
}
```