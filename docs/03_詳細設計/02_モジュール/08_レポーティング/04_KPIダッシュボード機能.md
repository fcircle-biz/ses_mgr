# KPIダッシュボード機能

## 1. 機能概要

KPIダッシュボード機能は、システム利用者に対して重要な業績評価指標（KPI）をリアルタイムで可視化し、ビジネス状況の把握と意思決定を支援する機能を提供します。

### 1.1 主要機能

- ダッシュボードの作成・編集・削除
- ウィジェットの追加・配置・サイズ変更・削除
- 各種KPI指標の表示と更新
- ドリルダウン分析
- ダッシュボード共有と権限管理
- KPI閾値アラート設定

### 1.2 ユースケース

- 経営者が全社KPIの状況を確認する
- 営業担当者が案件獲得状況や売上予測を確認する
- プロジェクトマネージャが技術者の稼働状況を確認する
- 人事担当者が技術者のスキル構成や稼働状況を分析する
- 経理担当者が売上・原価・利益状況を確認する

## 2. 機能詳細

### 2.1 ダッシュボード管理機能

#### 2.1.1 ダッシュボード作成

ダッシュボードを新規作成する機能です。

**機能仕様**:
- ダッシュボード名、説明、カテゴリ、共有範囲を設定可能
- テンプレートからの作成、または空のダッシュボードからの作成が選択可能
- 作成者情報と作成日時を自動記録
- 標準レイアウト（グリッドベース）を初期設定

**処理フロー**:
1. ユーザーがダッシュボード作成画面を表示
2. ダッシュボード情報入力（名前、説明等）
3. テンプレート選択または空のダッシュボード選択
4. ダッシュボード作成処理実行
5. 作成完了後、ダッシュボード編集画面へ遷移

**例外処理**:
- 必須項目未入力時のバリデーションエラー処理
- 同一ユーザーで同名ダッシュボード存在時の重複エラー処理

#### 2.1.2 ダッシュボード編集

既存ダッシュボードの設定を編集する機能です。

**機能仕様**:
- ダッシュボード名、説明、カテゴリ、共有範囲の変更
- 更新者情報と更新日時を自動記録
- ダッシュボードレイアウト設定の変更

**処理フロー**:
1. 対象ダッシュボードの編集モード起動
2. ダッシュボード情報編集
3. 変更内容保存
4. 変更履歴記録

**例外処理**:
- 他ユーザーによる同時編集の競合制御
- 編集権限チェック

#### 2.1.3 ダッシュボード削除

不要となったダッシュボードを削除する機能です。

**機能仕様**:
- 論理削除による削除処理
- 削除者情報と削除日時を記録
- 権限に応じた削除可否制御

**処理フロー**:
1. 削除対象ダッシュボード選択
2. 削除確認ダイアログ表示
3. 削除処理実行
4. 削除完了通知

**例外処理**:
- システム標準ダッシュボード削除防止制御
- 削除権限チェック

### 2.2 ウィジェット管理機能

#### 2.2.1 ウィジェット追加

ダッシュボードにKPI表示用のウィジェットを追加する機能です。

**機能仕様**:
- 利用可能なウィジェットタイプ：
  - 数値指標（単一値）
  - グラフ（棒グラフ、折れ線グラフ、円グラフ、散布図など）
  - テーブル
  - ゲージ
  - マップ
- ウィジェットに表示するKPI指標の選択
- データソース設定とフィルター条件設定
- 更新頻度設定
- 表示スタイル設定

**処理フロー**:
1. ウィジェット追加ボタン選択
2. ウィジェットタイプ選択
3. KPI指標選択と設定
4. 表示条件・スタイル設定
5. プレビュー表示
6. ダッシュボードへの配置位置指定
7. ウィジェット保存

**例外処理**:
- データソース接続エラー処理
- 無効なKPI指標選択時のエラー処理
- パフォーマンス低下防止のためのウィジェット数制限

#### 2.2.2 ウィジェット配置・サイズ変更

ダッシュボード上のウィジェットの配置とサイズを調整する機能です。

**機能仕様**:
- ドラッグ＆ドロップによる位置変更
- リサイズハンドルによるサイズ変更
- グリッドスナップ機能（整列補助）
- レイアウト自動調整機能

**処理フロー**:
1. ウィジェット選択
2. ドラッグまたはリサイズ操作
3. 変更内容の一時保存
4. レイアウト確定時に永続化

**例外処理**:
- ウィジェット重複配置の防止
- 最小・最大サイズ制限

#### 2.2.3 ウィジェット編集・削除

既存ウィジェットの設定変更や削除を行う機能です。

**機能仕様**:
- KPI指標設定変更
- データソース・フィルター条件変更
- 表示スタイル変更
- ウィジェット削除

**処理フロー**:
1. 対象ウィジェット選択
2. 編集メニュー表示
3. 設定変更または削除操作
4. 変更内容保存

**例外処理**:
- データソース変更時の互換性チェック
- システム標準ウィジェット保護

### 2.3 KPI指標管理機能

#### 2.3.1 KPI指標定義

システムで利用可能なKPI指標を定義・管理する機能です。

**機能仕様**:
- KPI指標の名称、説明、カテゴリ、単位設定
- 計算ロジック定義（単純集計、比率計算、複合計算など）
- 参照データソース設定
- 表示フォーマット設定
- 権限設定（閲覧可能ユーザー・グループ）

**処理フロー**:
1. KPI指標の作成・編集画面表示
2. 基本情報設定
3. 計算ロジック定義
4. データソース設定
5. 権限設定
6. 保存処理

**例外処理**:
- 計算ロジック構文エラー検出
- 循環参照防止チェック
- 無効なデータソース参照チェック

#### 2.3.2 KPI閾値アラート設定

KPI指標に対して閾値監視とアラート通知を設定する機能です。

**機能仕様**:
- KPI指標ごとに複数閾値設定が可能
  - 警告レベル（黄色表示）
  - 危険レベル（赤色表示）
- 閾値超過時の通知設定
  - メール通知
  - システム内通知
  - 外部システム連携（Webhook等）
- アラート頻度設定（即時/日次/週次）
- アラート自動解除条件設定

**処理フロー**:
1. KPI指標選択
2. 閾値設定画面表示
3. 閾値・通知条件設定
4. 保存処理
5. アラート監視開始

**例外処理**:
- 無効な閾値設定のバリデーション
- 通知先設定の検証
- 過剰通知防止制御

### 2.4 データ更新・表示機能

#### 2.4.1 リアルタイムデータ更新

ダッシュボード上のKPI指標データをリアルタイムに更新する機能です。

**機能仕様**:
- 各ウィジェットの更新頻度に基づく自動更新
- オンデマンド更新（手動更新ボタン）
- 最終更新時刻表示
- バックグラウンド更新処理によるUI操作影響の最小化

**処理フロー**:
1. 更新タイミング到達または手動更新指示
2. バックグラウンドでデータ取得処理
3. UI更新処理
4. 最終更新時刻更新

**例外処理**:
- データ取得エラー時のリトライ制御
- タイムアウト処理
- 部分更新失敗時の表示制御

#### 2.4.2 ドリルダウン分析

KPI指標の詳細データを階層的に分析する機能です。

**機能仕様**:
- グラフデータポイントのクリックによるドリルダウン
- 階層型データの展開表示
- フィルター条件の自動適用
- ドリルダウン履歴管理と戻る機能

**処理フロー**:
1. ドリルダウン対象データ選択
2. 詳細データ取得処理
3. 詳細ビュー表示
4. ナビゲーション履歴更新

**例外処理**:
- 詳細データ取得不可時の代替表示
- 大量データ取得時のページング処理

### 2.5 ダッシュボード共有・権限管理機能

#### 2.5.1 ダッシュボード共有設定

作成したダッシュボードを他ユーザーと共有するための設定機能です。

**機能仕様**:
- 共有範囲設定：
  - 非公開（作成者のみ）
  - 特定ユーザー
  - 特定グループ/ロール
  - 全社公開
- 権限レベル設定：
  - 閲覧のみ
  - 編集可能
  - 管理者権限
- 共有リンク生成と有効期限設定

**処理フロー**:
1. 共有設定画面表示
2. 共有範囲・権限設定
3. 保存処理
4. 共有通知（オプション）

**例外処理**:
- 権限整合性チェック
- 外部共有時のセキュリティ制御

#### 2.5.2 ダッシュボードアクセス制御

ダッシュボードへのアクセス権限を管理する機能です。

**機能仕様**:
- ダッシュボードごとのアクセス権限チェック
- KPI指標レベルでのアクセス制御
- データフィルタリングによるデータアクセス制御
- アクセス履歴の記録

**処理フロー**:
1. ダッシュボードアクセス要求
2. 権限チェック処理
3. アクセス可否判定
4. アクセス履歴記録

**例外処理**:
- 権限不足時のエラーメッセージ表示
- アクセス拒否ログ記録

## 3. 非機能要件

### 3.1 性能要件

- KPIダッシュボード表示レスポンスタイム：2秒以内（95パーセンタイル）
- 同時アクセスユーザー数：100ユーザー
- データ更新間隔：最小1分
- ウィジェット表示数：1ダッシュボードあたり最大20個

### 3.2 可用性要件

- サービス稼働率：99.5%
- 定期メンテナンス時間外のシステム可用性
- 障害復旧時間：30分以内（RTO）
- 障害時のデータ損失：5分以内（RPO）

### 3.3 セキュリティ要件

- ダッシュボードアクセス権限の厳格な管理
- 機密データ（売上・利益等）表示時の追加認証
- 全操作履歴のログ記録と監査証跡
- 外部共有時のセキュリティ制御

### 3.4 拡張性要件

- カスタムKPI指標の追加容易性
- 新規ウィジェットタイプの追加しやすさ
- 外部BIツールとの連携可能性

## 4. インターフェース詳細

### 4.1 提供インターフェース

#### KPIDashboardService

ダッシュボード管理機能を提供するインターフェースです。ドメインモデルで定義した `KpiDashboardService` の実装詳細となります。

```
public interface KPIDashboardService {
    // ダッシュボード作成
    DashboardId createDashboard(CreateDashboardRequest request);
    
    // ダッシュボード更新
    void updateDashboard(DashboardId dashboardId, UpdateDashboardRequest request);
    
    // ダッシュボード削除
    void deleteDashboard(DashboardId dashboardId);
    
    // ダッシュボード情報取得
    DashboardDetail getDashboardDetail(DashboardId dashboardId);
    
    // ダッシュボード一覧取得
    SearchResult<DashboardSummary> searchDashboards(DashboardSearchCriteria criteria, Pageable pageable);
    
    // ウィジェット追加
    WidgetId addWidget(DashboardId dashboardId, AddWidgetRequest request);
    
    // ウィジェット更新
    void updateWidget(DashboardId dashboardId, WidgetId widgetId, UpdateWidgetRequest request);
    
    // ウィジェット削除
    void deleteWidget(DashboardId dashboardId, WidgetId widgetId);
    
    // ウィジェットレイアウト更新
    void updateWidgetLayout(DashboardId dashboardId, List<WidgetLayoutInfo> layoutInfoList);
    
    // ダッシュボード共有設定
    void updateSharingSettings(DashboardId dashboardId, SharingSettings settings);
}
```

#### KPIDefinitionService

KPI指標定義を管理するインターフェースです。

```
public interface KPIDefinitionService {
    // KPI指標定義作成
    KpiDefinitionId createKpiDefinition(CreateKpiDefinitionRequest request);
    
    // KPI指標定義更新
    void updateKpiDefinition(KpiDefinitionId kpiId, UpdateKpiDefinitionRequest request);
    
    // KPI指標定義削除
    void deleteKpiDefinition(KpiDefinitionId kpiId);
    
    // KPI指標一覧取得
    SearchResult<KpiDefinitionSummary> searchKpiDefinitions(KpiDefinitionSearchCriteria criteria, Pageable pageable);
    
    // KPI指標詳細取得
    KpiDefinitionDetail getKpiDefinitionDetail(KpiDefinitionId kpiId);
    
    // KPI閾値設定更新
    void updateKpiThresholds(KpiDefinitionId kpiId, List<KpiThreshold> thresholds);
    
    // KPI指標のカテゴリ一覧取得
    List<KpiCategory> getKpiCategories();
}
```

#### KPIDataQueryService

KPI指標データの取得と計算を行うインターフェースです。

```
public interface KPIDataQueryService {
    // KPI指標データ取得（単一指標）
    KpiData getKpiData(KpiDefinitionId kpiId, KpiDataQueryParams params);
    
    // KPI指標データ一括取得（複数指標）
    Map<KpiDefinitionId, KpiData> getBulkKpiData(List<KpiDefinitionId> kpiIds, KpiDataQueryParams params);
    
    // 時系列データ取得
    KpiTimeSeriesData getKpiTimeSeriesData(KpiDefinitionId kpiId, 
                                           KpiTimeSeriesQueryParams params);
    
    // ドリルダウンデータ取得
    KpiDrilldownData getKpiDrilldownData(KpiDefinitionId kpiId, 
                                         KpiDrilldownParams params);
    
    // KPI指標のリアルタイム更新購読
    Subscription subscribeToKpiUpdates(List<KpiDefinitionId> kpiIds, 
                                      KpiUpdateListener listener);
}
```

### 4.2 必要インターフェース

KPIダッシュボード機能が他モジュールから利用するインターフェースです。

#### AuthorizationService（認証認可モジュール）

```
public interface AuthorizationService {
    // リソースアクセス権限チェック
    boolean hasPermission(UserId userId, ResourceId resourceId, Permission permission);
    
    // データフィルタリングポリシー取得
    DataFilteringPolicy getDataFilteringPolicy(UserId userId, ResourceType resourceType);
}
```

#### NotificationService（通知モジュール）

```
public interface NotificationService {
    // KPI閾値アラート通知
    void sendKpiAlertNotification(KpiAlertNotification notification);
    
    // ダッシュボード共有通知
    void sendDashboardSharingNotification(DashboardSharingNotification notification);
}
```

#### DataSourceService（各業務モジュール）

```
public interface DataSourceService {
    // 集計データ取得
    AggregationData getAggregationData(AggregationQuery query);
    
    // マスタデータ取得
    ReferenceData getReferenceData(ReferenceQuery query);
}
```

## 5. 例外処理

### 5.1 ビジネス例外

| 例外クラス | 説明 | HTTPステータス |
|------------|------|---------------|
| `KpiDashboardNotFoundException` | 指定されたダッシュボードが存在しない | 404 Not Found |
| `KpiWidgetNotFoundException` | 指定されたウィジェットが存在しない | 404 Not Found |
| `KpiDefinitionNotFoundException` | 指定されたKPI定義が存在しない | 404 Not Found |
| `DashboardEditConflictException` | ダッシュボード編集の競合発生 | 409 Conflict |
| `InvalidKpiDefinitionException` | 無効なKPI定義（計算式エラーなど） | 400 Bad Request |
| `DashboardPermissionDeniedException` | ダッシュボードアクセス権限エラー | 403 Forbidden |
| `KpiDataAccessException` | KPIデータアクセスエラー | 500 Internal Server Error |

### 5.2 システム例外

| 例外クラス | 説明 | HTTPステータス |
|------------|------|---------------|
| `KpiCalculationTimeoutException` | KPI計算のタイムアウト | 504 Gateway Timeout |
| `DataSourceUnavailableException` | データソース接続不可 | 503 Service Unavailable |
| `DashboardServiceInternalException` | ダッシュボードサービス内部エラー | 500 Internal Server Error |

## 6. セキュリティ

### 6.1 認証・認可

- ダッシュボードアクセスは認証済みユーザーのみ許可
- ダッシュボードごとに閲覧・編集・管理者権限を設定
- KPI指標ごとにアクセス制御設定
- データレベルセキュリティによる表示データフィルタリング

### 6.2 監査ログ

以下の操作ログを記録します：

- ダッシュボードの作成・編集・削除
- ウィジェットの追加・編集・削除
- KPI指標定義の作成・編集・削除
- ダッシュボード共有設定の変更
- ダッシュボードアクセス（閲覧）

## 7. 運用設計

### 7.1 パフォーマンスチューニング

- KPI計算結果のキャッシュ戦略
- 頻繁に参照されるダッシュボードの事前計算
- 大量データ集計時の分散処理

### 7.2 監視設計

- ダッシュボード表示パフォーマンスの監視
- データ更新処理の遅延監視
- KPI計算エラー率の監視
- ユーザーごとのダッシュボード使用状況分析

### 7.3 バックアップ・リストア

- ダッシュボード定義情報の定期バックアップ
- KPI指標定義のエクスポート・インポート機能
- システム障害時の復旧手順

## 8. 拡張性

### 8.1 カスタムウィジェット対応

- ウィジェットプラグイン機構
- カスタムデータソース連携
- 外部可視化ライブラリ活用

### 8.2 外部システム連携

- 外部BIツールとのデータ連携
- データエクスポート形式（CSV, Excel, PDF）
- 外部ダッシュボードの埋め込み表示