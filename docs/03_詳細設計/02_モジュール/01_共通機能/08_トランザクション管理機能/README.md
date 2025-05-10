# トランザクション管理機能

## 概要

トランザクション管理機能は、SES管理システム全体でのデータの一貫性と整合性を保証するための共通機能モジュールです。この機能は、単一データソース内のトランザクション制御だけでなく、複数のデータソースやサービスにまたがる分散トランザクションの管理も提供します。

本モジュールは、業務プロセス全体を通じてデータの完全性を確保し、障害発生時にも一貫した状態を維持するための仕組みを提供します。

## 目的

* システム全体で一貫したトランザクション管理方法を提供する
* データの整合性を維持し、不整合な状態を防止する
* トランザクションの境界と伝播を適切に管理する
* マイクロサービス間や分散システムでの一貫性を確保する
* 障害発生時の回復メカニズムを提供する

## 対象読者

* アプリケーション開発者
* システムアーキテクト
* データベース設計者
* インフラストラクチャエンジニア

## ドキュメント構成

| ドキュメント | 説明 |
|------------|------|
| [01_概要](./01_概要.md) | トランザクション管理機能の全体像、位置づけ、アーキテクチャを説明 |
| [02_ドメインモデル](./02_ドメインモデル.md) | トランザクション管理のドメインモデル（エンティティ、値オブジェクト）を定義 |
| [03_インターフェース定義](./03_インターフェース定義.md) | 提供・依存するインターフェース、API仕様を定義 |
| [04_トランザクション制御](./04_トランザクション制御.md) | ローカルトランザクションの制御メカニズムの詳細設計 |
| [05_分散トランザクション](./05_分散トランザクション.md) | 分散トランザクション管理の詳細設計 |

## 主要コンポーネント

* **TransactionManager**: トランザクションの開始、コミット、ロールバックを制御するマネージャ
* **TransactionTemplate**: 宣言的トランザクション管理を提供するテンプレート
* **TransactionSynchronizer**: トランザクションの前後に処理を追加するためのフック機構
* **DistributedTransactionCoordinator**: 分散トランザクションを調整するコーディネータ
* **CompensationManager**: 補償トランザクションを管理するマネージャ
* **TransactionEventPublisher**: トランザクション関連イベントを発行するパブリッシャー
* **TransactionLogService**: トランザクションの実行履歴を記録するサービス

## 関連するシステムコンポーネント

* データベースアクセス機能: トランザクション内でのデータベース操作
* メッセージングサービス: トランザクションと連携したメッセージ処理
* API連携機能: 外部システムとの連携におけるトランザクション管理
* 認証・認可機能: トランザクション操作の権限管理
* 監査ログ機能: トランザクション実行の監査記録

## 提供インターフェース

トランザクション管理機能は以下のインターフェースを提供します：

### 1. TransactionManager インターフェース

トランザクションの基本操作を提供するインターフェース。

```java
public interface TransactionManager {
    /**
     * 新しいトランザクションを開始する
     * @param propagation 伝播方式
     * @param isolation 分離レベル
     * @param timeout タイムアウト（秒）
     * @return トランザクション情報
     */
    TransactionStatus begin(Propagation propagation, Isolation isolation, int timeout);
    
    /**
     * 現在のトランザクションをコミットする
     * @param status トランザクションステータス
     */
    void commit(TransactionStatus status);
    
    /**
     * 現在のトランザクションをロールバックする
     * @param status トランザクションステータス
     */
    void rollback(TransactionStatus status);
    
    /**
     * 現在アクティブなトランザクションがあるか確認する
     * @return トランザクションがアクティブな場合はtrue
     */
    boolean isActive();
}
```

### 2. DistributedTransactionManager インターフェース

分散トランザクションを管理するインターフェース。

```java
public interface DistributedTransactionManager {
    /**
     * 分散トランザクションを開始する
     * @param participants 参加サービスリスト
     * @param timeout タイムアウト（秒）
     * @return 分散トランザクションID
     */
    String beginDistributedTransaction(List<String> participants, int timeout);
    
    /**
     * 分散トランザクションをコミットする
     * @param transactionId トランザクションID
     * @return コミット結果
     */
    DistributedTransactionResult commitDistributedTransaction(String transactionId);
    
    /**
     * 分散トランザクションをロールバックする
     * @param transactionId トランザクションID
     */
    void rollbackDistributedTransaction(String transactionId);
    
    /**
     * 分散トランザクションの状態を取得する
     * @param transactionId トランザクションID
     * @return トランザクション状態
     */
    DistributedTransactionStatus getTransactionStatus(String transactionId);
}
```

### 3. TransactionTemplate インターフェース

トランザクション内での処理を簡潔に記述するためのテンプレート。

```java
public interface TransactionTemplate {
    /**
     * トランザクション内で処理を実行する
     * @param action 実行する処理
     * @return 処理結果
     */
    <T> T execute(TransactionCallback<T> action);
    
    /**
     * 読み取り専用トランザクション内で処理を実行する
     * @param action 実行する処理
     * @return 処理結果
     */
    <T> T executeReadOnly(TransactionCallback<T> action);
}
```

## 依存インターフェース

トランザクション管理機能が依存する外部インターフェース：

### 1. DataSourceProvider インターフェース

```java
public interface DataSourceProvider {
    /**
     * データソースを取得する
     * @param name データソース名
     * @return 対応するデータソース
     */
    DataSource getDataSource(String name);
    
    /**
     * すべてのデータソース名を取得する
     * @return データソース名のリスト
     */
    List<String> getAllDataSourceNames();
}
```

### 2. TransactionLogger インターフェース

```java
public interface TransactionLogger {
    /**
     * トランザクション開始を記録する
     * @param transactionId トランザクションID
     * @param attributes 属性情報
     */
    void logTransactionBegin(String transactionId, Map<String, Object> attributes);
    
    /**
     * トランザクション完了を記録する
     * @param transactionId トランザクションID
     * @param status 完了ステータス
     * @param executionTime 実行時間（ミリ秒）
     */
    void logTransactionComplete(String transactionId, String status, long executionTime);
}
```