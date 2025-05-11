# KPI管理テーブル

KPI管理に関連するテーブルは、KPI指標定義（`kpi_definitions`）、KPI目標値（`kpi_targets`）、KPI実績値（`kpi_achievements`）の3つのテーブルで構成されます。

## kpi_definitions（KPI指標定義）

システムで管理するKPI指標の定義を管理するテーブルです。KPI名、計算式、表示形式などを保持します。

### テーブル定義

| カラム名 | データ型 | NULL | 主キー | 外部キー | デフォルト | 説明 |
|---------|---------|------|-------|---------|----------|------|
| kpi_id | uuid | NO | YES | - | - | KPI定義ID |
| code | varchar(50) | NO | NO | - | - | KPIコード |
| name | varchar(100) | NO | NO | - | - | KPI名称 |
| description | varchar(500) | YES | NO | - | NULL | 説明 |
| category | varchar(30) | NO | NO | - | - | カテゴリ（sales:売上, profit:利益, utilization:稼働率, customer:顧客, hr:人事, operation:業務, quality:品質, finance:財務） |
| calculation_formula | text | NO | NO | - | - | 計算式 |
| unit | varchar(20) | NO | NO | - | - | 単位 |
| display_format | varchar(50) | NO | NO | - | - | 表示形式 |
| sort_order | integer | NO | NO | - | 0 | 表示順 |
| is_active | boolean | NO | NO | - | true | 有効フラグ |
| target_direction | varchar(20) | NO | NO | - | 'higher_better' | 目標方向（higher_better:高いほど良い, lower_better:低いほど良い, range_optimal:範囲内が最適） |
| created_at | timestamp | NO | NO | - | CURRENT_TIMESTAMP | 作成日時 |
| updated_at | timestamp | NO | NO | - | CURRENT_TIMESTAMP | 更新日時 |
| is_deleted | boolean | NO | NO | - | false | 論理削除フラグ |

### ユニーク制約

| 制約名 | カラム |
|--------|-------|
| uk_kpi_definitions_code | code, is_deleted |

### インデックス

| インデックス名 | カラム | 説明 |
|--------------|-------|------|
| idx_kpi_definitions_code | code | KPIコードによる検索用 |
| idx_kpi_definitions_category | category | カテゴリによる検索用 |
| idx_kpi_definitions_is_active | is_active | 有効フラグによる検索用 |

### ビジネスルール

- KPIコードは一意のコード体系で管理（例：SALES_MONTHLY, PROFIT_RATE_QUARTERLY）
- 計算式はシステム内部で解釈可能な形式で記述（関数形式、SQL形式、数式形式等）
- 目標方向はKPI値の評価方法を示す（数値が高いほど良いか、低いほど良いか、特定範囲内が最適か）
- 表示形式には数値フォーマット（#,###.00%など）を指定
- 単位はKPI値の測定単位（円、%、人、時間など）
- KPI定義は無効化（is_active=false）することはできるが、過去データの参照性を保つため物理削除は行わない

## kpi_targets（KPI目標値）

KPI指標の目標値を管理するテーブルです。期間（年次、四半期、月次）、目標値、部門などを保持します。

### テーブル定義

| カラム名 | データ型 | NULL | 主キー | 外部キー | デフォルト | 説明 |
|---------|---------|------|-------|---------|----------|------|
| target_id | uuid | NO | YES | - | - | KPI目標ID |
| kpi_id | uuid | NO | NO | kpi_definitions.kpi_id | - | KPI定義ID |
| target_type | varchar(20) | NO | NO | - | 'monthly' | 目標タイプ（yearly:年次, quarterly:四半期, monthly:月次） |
| target_year | integer | NO | NO | - | - | 対象年 |
| target_quarter | integer | YES | NO | - | NULL | 対象四半期（1-4） |
| target_month | integer | YES | NO | - | NULL | 対象月（1-12） |
| target_value | numeric(18,4) | NO | NO | - | - | 目標値 |
| min_value | numeric(18,4) | YES | NO | - | NULL | 最小許容値（範囲目標の場合） |
| max_value | numeric(18,4) | YES | NO | - | NULL | 最大許容値（範囲目標の場合） |
| department_id | uuid | YES | NO | departments.department_id | NULL | 部門ID |
| created_at | timestamp | NO | NO | - | CURRENT_TIMESTAMP | 作成日時 |
| updated_at | timestamp | NO | NO | - | CURRENT_TIMESTAMP | 更新日時 |
| created_by | uuid | NO | NO | users.user_id | - | 作成者ID |
| updated_by | uuid | NO | NO | users.user_id | - | 更新者ID |
| is_deleted | boolean | NO | NO | - | false | 論理削除フラグ |

### ユニーク制約

| 制約名 | カラム |
|--------|-------|
| uk_kpi_targets_period | kpi_id, target_type, target_year, target_quarter, target_month, department_id, is_deleted |

### インデックス

| インデックス名 | カラム | 説明 |
|--------------|-------|------|
| idx_kpi_targets_kpi_id | kpi_id | KPI定義IDによる検索用 |
| idx_kpi_targets_department_id | department_id | 部門IDによる検索用 |
| idx_kpi_targets_target_type | target_type | 目標タイプによる検索用 |
| idx_kpi_targets_target_year | target_year | 対象年による検索用 |
| idx_kpi_targets_target_year_quarter | target_year, target_quarter | 年・四半期による検索用 |
| idx_kpi_targets_target_year_month | target_year, target_month | 年・月による検索用 |

### ビジネスルール

- 目標タイプに応じて必須項目が変わる
  - 年次（yearly）: target_year が必須、target_quarter と target_month は NULL
  - 四半期（quarterly）: target_year と target_quarter が必須、target_month は NULL
  - 月次（monthly）: target_year と target_month が必須、target_quarter は NULL
- 同一KPI、同一期間、同一部門の目標は重複して登録できない
- 部門IDが指定されない場合は全社目標
- KPIの目標方向が「範囲内が最適」の場合は、min_value と max_value が必須
- 目標値は数値のみ許容（文字列やブール値は不可）

## kpi_achievements（KPI実績値）

KPI指標の実績値を管理するテーブルです。期間（年次、四半期、月次）、実績値、達成率などを保持します。

### テーブル定義

| カラム名 | データ型 | NULL | 主キー | 外部キー | デフォルト | 説明 |
|---------|---------|------|-------|---------|----------|------|
| achievement_id | uuid | NO | YES | - | - | KPI実績ID |
| kpi_id | uuid | NO | NO | kpi_definitions.kpi_id | - | KPI定義ID |
| target_id | uuid | YES | NO | kpi_targets.target_id | NULL | KPI目標ID |
| achievement_year | integer | NO | NO | - | - | 実績年 |
| achievement_quarter | integer | YES | NO | - | NULL | 実績四半期（1-4） |
| achievement_month | integer | YES | NO | - | NULL | 実績月（1-12） |
| achievement_value | numeric(18,4) | NO | NO | - | - | 実績値 |
| achievement_percentage | numeric(8,2) | YES | NO | - | NULL | 達成率(%) |
| department_id | uuid | YES | NO | departments.department_id | NULL | 部門ID |
| created_at | timestamp | NO | NO | - | CURRENT_TIMESTAMP | 作成日時 |
| updated_at | timestamp | NO | NO | - | CURRENT_TIMESTAMP | 更新日時 |
| is_deleted | boolean | NO | NO | - | false | 論理削除フラグ |

### ユニーク制約

| 制約名 | カラム |
|--------|-------|
| uk_kpi_achievements_period | kpi_id, achievement_year, achievement_quarter, achievement_month, department_id, is_deleted |

### インデックス

| インデックス名 | カラム | 説明 |
|--------------|-------|------|
| idx_kpi_achievements_kpi_id | kpi_id | KPI定義IDによる検索用 |
| idx_kpi_achievements_target_id | target_id | KPI目標IDによる検索用 |
| idx_kpi_achievements_department_id | department_id | 部門IDによる検索用 |
| idx_kpi_achievements_achievement_year | achievement_year | 実績年による検索用 |
| idx_kpi_achievements_achievement_year_quarter | achievement_year, achievement_quarter | 年・四半期による検索用 |
| idx_kpi_achievements_achievement_year_month | achievement_year, achievement_month | 年・月による検索用 |

### ビジネスルール

- 実績タイプ（年次/四半期/月次）に応じて必須項目が変わる
  - 年次: achievement_year が必須、achievement_quarter と achievement_month は NULL
  - 四半期: achievement_year と achievement_quarter が必須、achievement_month は NULL
  - 月次: achievement_year と achievement_month が必須、achievement_quarter は NULL
- 同一KPI、同一期間、同一部門の実績は重複して登録できない
- 部門IDが指定されない場合は全社実績
- 達成率は目標値に対する実績値の割合（目標値がない場合は NULL）
- KPIの目標方向によって達成率の計算方法が異なる
  - 「高いほど良い」: 達成率 = 実績値 ÷ 目標値 × 100%
  - 「低いほど良い」: 達成率 = 目標値 ÷ 実績値 × 100%
  - 「範囲内が最適」: 実績値が範囲内なら100%、範囲外なら近い境界値との比率
- 実績データは自動計算または手動入力で作成される

## クエリパターン

### 主要なクエリパターン

1. カテゴリ別の有効KPI一覧取得
   ```
   SELECT * FROM kpi_definitions 
   WHERE category = [category] 
   AND is_active = true 
   AND is_deleted = false
   ORDER BY sort_order, name;
   ```

2. 特定期間の部門別KPI目標取得
   ```
   SELECT kd.name, kd.category, kt.* 
   FROM kpi_targets kt
   JOIN kpi_definitions kd ON kt.kpi_id = kd.kpi_id
   WHERE kt.target_type = 'monthly'
   AND kt.target_year = [year]
   AND kt.target_month = [month]
   AND (kt.department_id = [department_id] OR kt.department_id IS NULL)
   AND kt.is_deleted = false
   AND kd.is_deleted = false
   ORDER BY kd.category, kd.sort_order;
   ```

3. KPI実績の年次推移取得
   ```
   SELECT kd.name, kd.unit, kd.display_format,
          ka.achievement_year, ka.achievement_month, 
          ka.achievement_value, ka.achievement_percentage
   FROM kpi_achievements ka
   JOIN kpi_definitions kd ON ka.kpi_id = kd.kpi_id
   WHERE ka.kpi_id = [kpi_id]
   AND ka.achievement_year = [year]
   AND ka.department_id IS NULL
   AND ka.is_deleted = false
   AND kd.is_deleted = false
   ORDER BY ka.achievement_month;
   ```

4. KPI目標と実績の比較
   ```
   SELECT kd.name, kd.category, kd.unit,
          kt.target_year, kt.target_month, kt.target_value,
          ka.achievement_value, ka.achievement_percentage
   FROM kpi_targets kt
   JOIN kpi_definitions kd ON kt.kpi_id = kd.kpi_id
   LEFT JOIN kpi_achievements ka ON kt.kpi_id = ka.kpi_id
                               AND kt.target_year = ka.achievement_year
                               AND kt.target_month = ka.achievement_month
                               AND (kt.department_id = ka.department_id OR (kt.department_id IS NULL AND ka.department_id IS NULL))
                               AND ka.is_deleted = false
   WHERE kt.target_type = 'monthly'
   AND kt.target_year = [year]
   AND kt.target_month = [month]
   AND kt.is_deleted = false
   AND kd.is_deleted = false
   ORDER BY kd.category, kd.sort_order;
   ```

5. 部門別のKPI達成状況
   ```
   SELECT d.department_name,
          AVG(CASE WHEN ka.achievement_percentage >= 100 THEN 1 ELSE 0 END) * 100 as achievement_rate,
          COUNT(*) as total_kpis
   FROM kpi_achievements ka
   JOIN kpi_definitions kd ON ka.kpi_id = kd.kpi_id
   JOIN departments d ON ka.department_id = d.department_id
   WHERE ka.achievement_year = [year]
   AND ka.achievement_month = [month]
   AND ka.achievement_percentage IS NOT NULL
   AND ka.is_deleted = false
   AND kd.is_deleted = false
   AND d.is_deleted = false
   GROUP BY d.department_id, d.department_name
   ORDER BY achievement_rate DESC;
   ```

### パフォーマンス最適化

- KPIコード、カテゴリによる検索は専用インデックスを利用
- 期間（年次、四半期、月次）による検索は複合インデックスを利用
- 部門IDによる検索は専用インデックスを利用
- KPI実績データは量が多くなるため、必要に応じて集計テーブルや統計情報の事前計算を検討
- 長期間のトレンド分析のため、時系列データの効率的な検索方法を実装

## KPI達成率計算ロジック

KPIの目標方向に応じた達成率の計算ロジックは以下の通りです。

```sql
-- 達成率計算のサンプルロジック
UPDATE kpi_achievements ka
SET achievement_percentage = 
  CASE 
    -- 目標IDがNULLの場合は達成率を計算しない
    WHEN ka.target_id IS NULL THEN NULL
    
    -- 目標方向が「高いほど良い」の場合
    WHEN kd.target_direction = 'higher_better' THEN
      CASE 
        WHEN kt.target_value = 0 THEN NULL  -- ゼロ除算防止
        ELSE ROUND((ka.achievement_value / kt.target_value) * 100, 2)
      END
    
    -- 目標方向が「低いほど良い」の場合
    WHEN kd.target_direction = 'lower_better' THEN
      CASE 
        WHEN ka.achievement_value = 0 THEN NULL  -- ゼロ除算防止
        ELSE ROUND((kt.target_value / ka.achievement_value) * 100, 2)
      END
    
    -- 目標方向が「範囲内が最適」の場合
    WHEN kd.target_direction = 'range_optimal' THEN
      CASE
        -- 実績値が範囲内の場合
        WHEN ka.achievement_value BETWEEN kt.min_value AND kt.max_value THEN 100
        
        -- 実績値が最小値より小さい場合
        WHEN ka.achievement_value < kt.min_value THEN
          ROUND((ka.achievement_value / kt.min_value) * 100, 2)
        
        -- 実績値が最大値より大きい場合
        WHEN ka.achievement_value > kt.max_value THEN
          ROUND((kt.max_value / ka.achievement_value) * 100, 2)
      END
    
    ELSE NULL
  END
FROM kpi_targets kt
JOIN kpi_definitions kd ON kt.kpi_id = kd.kpi_id
WHERE ka.target_id = kt.target_id
AND ka.achievement_percentage IS NULL;
```

## インターフェースポイント

- **ダッシュボード機能**: KPIウィジェット表示に利用
- **レポート機能**: KPIレポート生成に利用
- **通知機能**: KPI達成状況のアラート通知
- **部門管理**: 部門別KPI設定・実績参照
- **各業務モジュール**: KPI実績値の自動計算データソース（売上、粗利、稼働率など）