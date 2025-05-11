# DB詳細設計 - PostgreSQL拡張機能

## 1. PostgreSQL拡張機能の活用方針

本システムでは、PostgreSQLの標準機能に加えて、各種拡張機能を積極的に活用することで機能性、性能、保守性の向上を図る。拡張機能の選定・活用には以下の方針を適用する。

### 1.1 拡張機能選定基準

- **安定性**: メンテナンスが継続的に行われ、安定性が高い拡張機能を選定
- **互換性**: PostgreSQL 17との互換性が確認されている拡張機能を選定
- **性能影響**: データベース全体のパフォーマンスに悪影響を与えない拡張機能を選定
- **運用性**: 運用管理の複雑さを増大させない拡張機能を選定
- **ライセンス**: オープンソースライセンスの拡張機能を優先

### 1.2 活用方針

- **標準拡張**: PostgreSQLに標準添付されている拡張機能を優先的に活用
- **公式エコシステム**: PostgreSQL公式のエコシステムに含まれる拡張機能を次に検討
- **サードパーティ**: 広く普及し実績のあるサードパーティ拡張機能を慎重に検討
- **独自拡張**: 業務要件に応じて必要な場合のみ独自拡張を検討

## 2. 主要拡張機能と活用方法

本システムで活用する主要なPostgreSQL拡張機能とその用途を示す。

### 2.1 データ型拡張

#### 2.1.1 uuid-ossp

UUIDを生成するための関数を提供する拡張機能。グローバルに一意な識別子が必要な場合に使用。

##### 活用例:

```sql
-- 拡張機能の有効化
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- UUID生成の活用例
CREATE TABLE common.session (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id integer NOT NULL REFERENCES common.user(id),
    ip_address inet,
    user_agent text,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at timestamptz NOT NULL
);
```

#### 2.1.2 citext

大文字小文字を区別しないテキスト型を提供する拡張機能。メールアドレスなど大文字小文字の区別が不要なデータに使用。

##### 活用例:

```sql
-- 拡張機能の有効化
CREATE EXTENSION IF NOT EXISTS citext;

-- citextの活用例
CREATE TABLE common.user (
    id serial PRIMARY KEY,
    email citext NOT NULL UNIQUE,
    name varchar(100) NOT NULL,
    password_hash varchar(255) NOT NULL
);

-- 大文字小文字を区別しない検索が可能
SELECT * FROM common.user WHERE email = 'User@Example.com';  -- 'user@example.com'も検索可能
```

#### 2.1.3 postgis

地理空間情報を扱うための拡張機能。位置情報に基づく検索や分析が必要な場合に使用。

##### 活用例:

```sql
-- 拡張機能の有効化
CREATE EXTENSION IF NOT EXISTS postgis;

-- PostGISの活用例
CREATE TABLE project.location (
    id serial PRIMARY KEY,
    project_id integer NOT NULL REFERENCES project.project(id),
    name varchar(100) NOT NULL,
    location geometry(Point, 4326) NOT NULL,
    address text
);

-- 地理空間検索の例
SELECT p.title, l.name, l.address
FROM project.project p
JOIN project.location l ON p.id = l.project_id
WHERE ST_DWithin(
    l.location,
    ST_SetSRID(ST_MakePoint(139.767125, 35.681236), 4326),  -- 東京駅
    5000  -- 5km以内
);
```

### 2.2 全文検索拡張

#### 2.2.1 pg_trgm

トライグラム（3文字の連続）をベースとした類似度検索や高速なインデックス検索を提供する拡張機能。あいまい検索や類似検索に使用。

##### 活用例:

```sql
-- 拡張機能の有効化
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- トライグラムインデックスの作成
CREATE INDEX ix_engineer_name_trgm ON engineer.engineer USING gin (name gin_trgm_ops);
CREATE INDEX ix_engineer_skills_trgm ON engineer.engineer USING gin (skills gin_trgm_ops);

-- あいまい検索の例
SELECT name, skills
FROM engineer.engineer
WHERE name % 'suzki'  -- 'suzuki'などの類似名を検索
ORDER BY similarity(name, 'suzki') DESC;

-- 部分一致検索の最適化
SELECT name, skills
FROM engineer.engineer
WHERE skills ILIKE '%java%spring%'
ORDER BY similarity(skills, 'java spring') DESC;
```

#### 2.2.2 pg_bigm

2-gramベースの日本語全文検索を提供する拡張機能。日本語テキストの高精度な検索に使用。

##### 活用例:

```sql
-- 拡張機能の有効化
CREATE EXTENSION IF NOT EXISTS pg_bigm;

-- 日本語全文検索インデックスの作成
CREATE INDEX ix_project_description_bigm ON project.project USING gin (description gin_bigm_ops);

-- 日本語全文検索の例
SELECT title, description
FROM project.project
WHERE description LIKE '%システム開発%'
ORDER BY description <-> 'システム開発' LIMIT 10;
```

### 2.3 性能最適化拡張

#### 2.3.1 pg_stat_statements

クエリの実行統計情報を収集する拡張機能。性能監視や最適化に使用。

##### 活用例:

```sql
-- 拡張機能の有効化
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- 設定の調整（postgresql.conf）
-- pg_stat_statements.max = 10000
-- pg_stat_statements.track = all

-- 実行時間の長いクエリの特定
SELECT
    query,
    calls,
    total_exec_time / calls AS avg_exec_time,
    rows / calls AS avg_rows,
    total_exec_time,
    max_exec_time,
    stddev_exec_time
FROM pg_stat_statements
ORDER BY total_exec_time DESC
LIMIT 20;
```

#### 2.3.2 pg_hint_plan

クエリヒントを使用して実行計画を制御するための拡張機能。特定のクエリの性能最適化に使用。

##### 活用例:

```sql
-- 拡張機能の有効化
CREATE EXTENSION IF NOT EXISTS pg_hint_plan;

-- クエリヒントの活用例
/*+ IndexScan(e engineer_skill_idx) */
SELECT e.name, e.email
FROM engineer.engineer e
WHERE e.skill_id = 123
AND e.experience_years > 5;

/*+ BitmapScan(p project_status_idx) */
SELECT p.title, p.description
FROM project.project p
WHERE p.status_code = 2;
```

#### 2.3.3 pg_cron

データベース内でCronジョブをスケジュールするための拡張機能。定期的なメンテナンスタスクに使用。

##### 活用例:

```sql
-- 拡張機能の有効化
CREATE EXTENSION IF NOT EXISTS pg_cron;

-- 定期メンテナンスタスクの設定
-- 統計情報を毎日更新
SELECT cron.schedule('0 1 * * *', 'ANALYZE');

-- 毎週日曜日に履歴テーブルのVACUUM FULL
SELECT cron.schedule('0 2 * * 0', 'VACUUM FULL audit.system_log');

-- 毎月1日にパーティション管理を実行
SELECT cron.schedule('0 3 1 * *', 'SELECT common.create_monthly_partitions()');
```

### 2.4 データ処理拡張

#### 2.4.1 tablefunc

テーブル関数を提供する拡張機能。クロス集計や複雑なピボット処理に使用。

##### 活用例:

```sql
-- 拡張機能の有効化
CREATE EXTENSION IF NOT EXISTS tablefunc;

-- クロス集計の活用例
SELECT *
FROM crosstab(
    'SELECT
        engineer_id,
        EXTRACT(MONTH FROM work_date) AS month,
        SUM(hours) AS total_hours
    FROM timesheet.working_hours
    WHERE EXTRACT(YEAR FROM work_date) = 2024
    GROUP BY engineer_id, EXTRACT(MONTH FROM work_date)
    ORDER BY 1, 2',
    'SELECT m FROM generate_series(1, 12) m'
) AS ct (
    engineer_id int,
    "Jan" numeric, "Feb" numeric, "Mar" numeric, "Apr" numeric,
    "May" numeric, "Jun" numeric, "Jul" numeric, "Aug" numeric,
    "Sep" numeric, "Oct" numeric, "Nov" numeric, "Dec" numeric
);
```

#### 2.4.2 hstore

キーと値のペアを格納するためのデータ型を提供する拡張機能。スキーマレスデータの格納に使用。

##### 活用例:

```sql
-- 拡張機能の有効化
CREATE EXTENSION IF NOT EXISTS hstore;

-- hstoreの活用例
CREATE TABLE engineer.profile_attributes (
    engineer_id integer PRIMARY KEY REFERENCES engineer.engineer(id),
    attributes hstore
);

-- データ操作例
INSERT INTO engineer.profile_attributes VALUES (
    123,
    'education => "Computer Science", graduation_year => "2018", certification => "AWS Solutions Architect, LPIC Level 3"'
);

-- 特定のキーで検索
SELECT engineer_id, attributes -> 'education' AS education
FROM engineer.profile_attributes
WHERE attributes ? 'certification'
AND attributes @> 'graduation_year => "2018"';
```

#### 2.4.3 ltree

ラベル付きツリー構造を表現するためのデータ型と関数を提供する拡張機能。階層データの管理に使用。

##### 活用例:

```sql
-- 拡張機能の有効化
CREATE EXTENSION IF NOT EXISTS ltree;

-- 階層構造を持つカテゴリテーブル
CREATE TABLE common.category (
    id serial PRIMARY KEY,
    name varchar(100) NOT NULL,
    path ltree NOT NULL,
    UNIQUE (path)
);

-- インデックス作成
CREATE INDEX ix_category_path ON common.category USING gist (path);
CREATE INDEX ix_category_path_match ON common.category USING btree (path);

-- ルートカテゴリ
INSERT INTO common.category (name, path) VALUES ('IT Skills', 'it_skills');
INSERT INTO common.category (name, path) VALUES ('Business Skills', 'business_skills');

-- サブカテゴリ
INSERT INTO common.category (name, path) VALUES ('Programming', 'it_skills.programming');
INSERT INTO common.category (name, path) VALUES ('Database', 'it_skills.database');
INSERT INTO common.category (name, path) VALUES ('Project Management', 'business_skills.project_management');

-- 下位層
INSERT INTO common.category (name, path) VALUES ('Java', 'it_skills.programming.java');
INSERT INTO common.category (name, path) VALUES ('Python', 'it_skills.programming.python');
INSERT INTO common.category (name, path) VALUES ('PostgreSQL', 'it_skills.database.postgresql');

-- ツリー検索の例（サブツリーの取得）
SELECT id, name, path
FROM common.category
WHERE path <@ 'it_skills.programming'
ORDER BY path;

-- 祖先の取得
SELECT id, name, path
FROM common.category
WHERE path @> 'it_skills.programming.java'
ORDER BY path;
```

### 2.5 監視・運用拡張

#### 2.5.1 pg_stat_monitor

拡張されたクエリパフォーマンスモニタリングを提供する拡張機能。詳細な性能分析に使用。

##### 活用例:

```sql
-- 拡張機能の有効化
CREATE EXTENSION IF NOT EXISTS pg_stat_monitor;

-- 詳細な実行統計情報の取得
SELECT
    bucket_start_time,
    query,
    calls,
    total_exec_time,
    mean_exec_time,
    shared_blks_hit,
    shared_blks_read,
    shared_blks_dirtied,
    shared_blks_written,
    temp_blks_read,
    temp_blks_written
FROM pg_stat_monitor
WHERE calls > 100
ORDER BY total_exec_time DESC
LIMIT 20;

-- 時間帯別のパフォーマンス分析
SELECT
    bucket_start_time,
    substr(query, 1, 50) AS query_prefix,
    SUM(calls) AS total_calls,
    SUM(total_exec_time) AS total_time,
    AVG(mean_exec_time) AS avg_exec_time
FROM pg_stat_monitor
WHERE bucket_start_time > now() - interval '24 hours'
GROUP BY bucket_start_time, query_prefix
ORDER BY bucket_start_time, total_time DESC;
```

#### 2.5.2 auto_explain

自動的に実行計画をログに記録する拡張機能。性能問題の分析に使用。

##### 活用例:

```sql
-- 拡張機能の有効化
LOAD 'auto_explain';

-- 設定の調整（postgresql.conf）
-- auto_explain.log_min_duration = '1s'
-- auto_explain.log_analyze = on
-- auto_explain.log_buffers = on
-- auto_explain.log_timing = on
-- auto_explain.log_nested_statements = on
-- auto_explain.sample_rate = 1

-- セッションレベルでの設定
SET auto_explain.log_min_duration = '500ms';
SET auto_explain.log_analyze = on;
SET auto_explain.log_buffers = on;
SET auto_explain.log_timing = on;
```

#### 2.5.3 pg_repack

テーブルの断片化を解消するための拡張機能。オンラインでの再編成が可能。

##### 活用例:

```sql
-- pg_repackはコマンドラインツール形式で使用

-- テーブルの再編成
-- $ pg_repack -h <host> -p <port> -U <user> -d <database> -t engineer.engineer

-- スキーマ内の全テーブル再編成
-- $ pg_repack -h <host> -p <port> -U <user> -d <database> -s timesheet

-- インデックスのみ再編成
-- $ pg_repack -h <host> -p <port> -U <user> -d <database> -t project.project --only-indexes
```

### 2.6 セキュリティ拡張

#### 2.6.1 pgcrypto

暗号化機能を提供する拡張機能。機密データの暗号化に使用。

##### 活用例:

```sql
-- 拡張機能の有効化
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 暗号化の活用例
CREATE TABLE common.user_credential (
    user_id integer PRIMARY KEY REFERENCES common.user(id),
    password_hash text NOT NULL,
    secure_data bytea,
    encryption_key text
);

-- パスワードハッシュ生成
INSERT INTO common.user_credential (user_id, password_hash)
VALUES (1, crypt('secure_password', gen_salt('bf')));

-- パスワード検証
SELECT user_id FROM common.user_credential
WHERE user_id = 1 AND password_hash = crypt('input_password', password_hash);

-- データ暗号化
UPDATE common.user_credential
SET secure_data = pgp_sym_encrypt('機密情報', 'encryption_key_phrase')
WHERE user_id = 1;

-- データ復号
SELECT pgp_sym_decrypt(secure_data, 'encryption_key_phrase')
FROM common.user_credential
WHERE user_id = 1;
```

#### 2.6.2 pg_audit

監査ログ機能を提供する拡張機能。データベース操作の監査証跡を記録するために使用。

##### 活用例:

```sql
-- 拡張機能の有効化
CREATE EXTENSION IF NOT EXISTS pgaudit;

-- 設定の調整（postgresql.conf）
-- pgaudit.log = 'write, ddl'
-- pgaudit.log_catalog = off
-- pgaudit.log_relation = on
-- pgaudit.log_statement_once = on

-- 監査ログの対象追加（特定のスキーマ）
CREATE SCHEMA AUDIT engineer;
CREATE SCHEMA AUDIT contract;
```

## 3. JSONB型の活用

PostgreSQLのJSONB型は、スキーマレスデータや柔軟な構造化データを扱うために非常に有用である。本システムでのJSONB型の活用方法を示す。

### 3.1 JSONB型の基本的な活用パターン

#### 3.1.1 設定・パラメータ格納

設定情報やパラメータなどの構造化データを格納する際に活用。

```sql
-- ユーザー設定テーブル
CREATE TABLE common.user_settings (
    user_id integer PRIMARY KEY REFERENCES common.user(id),
    settings jsonb NOT NULL DEFAULT '{}'
);

-- 設定データの挿入
INSERT INTO common.user_settings (user_id, settings) VALUES (
    1,
    '{
        "theme": "dark",
        "language": "ja",
        "notifications": {
            "email": true,
            "sms": false,
            "push": true
        },
        "dashboard": {
            "widgets": ["calendar", "tasks", "projects"]
        }
    }'
);

-- 設定データの更新
UPDATE common.user_settings
SET settings = jsonb_set(settings, '{notifications,sms}', 'true')
WHERE user_id = 1;

-- 特定の設定値の取得
SELECT settings->'theme' AS theme, settings->'language' AS language
FROM common.user_settings
WHERE user_id = 1;

-- ネストされた値の取得
SELECT settings->'notifications'->>'email' AS email_notifications
FROM common.user_settings
WHERE user_id = 1;
```

#### 3.1.2 拡張プロパティ格納

拡張性を持たせたいエンティティのプロパティを格納する際に活用。

```sql
-- プロジェクト拡張属性テーブル
CREATE TABLE project.project (
    id serial PRIMARY KEY,
    title varchar(200) NOT NULL,
    description text,
    status_code smallint NOT NULL,
    start_date date,
    end_date date,
    attributes jsonb NOT NULL DEFAULT '{}'
);

-- 拡張属性を含むプロジェクト作成
INSERT INTO project.project (title, description, status_code, start_date, attributes) VALUES (
    'SES管理システム開発',
    'SES事業向け統合管理システムの開発',
    1,
    '2024-06-01',
    '{
        "priority": "high",
        "client_info": {
            "name": "株式会社ABC",
            "contact": "山田太郎",
            "email": "yamada@example.com"
        },
        "tags": ["システム開発", "Webアプリ", "PostgreSQL"],
        "custom_fields": {
            "importance_score": 85,
            "risk_level": "medium",
            "strategic_value": "high"
        }
    }'
);

-- JSONBインデックスの作成
CREATE INDEX ix_project_attributes ON project.project USING gin (attributes);
CREATE INDEX ix_project_attributes_tags ON project.project USING gin ((attributes->'tags'));

-- 属性検索
SELECT id, title, attributes->'priority' AS priority
FROM project.project
WHERE attributes @> '{"priority": "high"}'
AND attributes->'client_info'->>'name' LIKE '%ABC%';

-- 配列要素による検索
SELECT id, title
FROM project.project
WHERE attributes->'tags' ? 'PostgreSQL';
```

#### 3.1.3 履歴・変更記録

エンティティの変更履歴を効率的に格納する際に活用。

```sql
-- エンティティ変更履歴テーブル
CREATE TABLE common.entity_history (
    id serial PRIMARY KEY,
    entity_type varchar(50) NOT NULL,
    entity_id integer NOT NULL,
    operation_type varchar(10) NOT NULL,
    changed_by integer REFERENCES common.user(id),
    changed_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    old_values jsonb,
    new_values jsonb,
    diff jsonb
);

-- 変更履歴の記録トリガー
CREATE OR REPLACE FUNCTION common.record_entity_change()
RETURNS trigger AS $$
DECLARE
    old_values jsonb;
    new_values jsonb;
    diff jsonb := '{}'::jsonb;
BEGIN
    IF TG_OP = 'INSERT' THEN
        old_values := NULL;
        new_values := to_jsonb(NEW);
        diff := new_values;
    ELSIF TG_OP = 'UPDATE' THEN
        old_values := to_jsonb(OLD);
        new_values := to_jsonb(NEW);
        
        -- 差分の計算
        FOR key IN SELECT jsonb_object_keys(new_values)
        LOOP
            IF old_values->key IS DISTINCT FROM new_values->key THEN
                diff := jsonb_set(diff, ARRAY[key], new_values->key);
            END IF;
        END LOOP;
    ELSIF TG_OP = 'DELETE' THEN
        old_values := to_jsonb(OLD);
        new_values := NULL;
        diff := old_values;
    END IF;
    
    -- 履歴の挿入
    INSERT INTO common.entity_history (
        entity_type, entity_id, operation_type, 
        changed_by, old_values, new_values, diff
    ) VALUES (
        TG_TABLE_NAME, 
        CASE 
            WHEN TG_OP = 'DELETE' THEN OLD.id 
            ELSE NEW.id 
        END,
        TG_OP,
        current_setting('app.current_user_id')::integer,
        old_values,
        new_values,
        diff
    );
    
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- トリガーの適用例
CREATE TRIGGER trg_project_history
AFTER INSERT OR UPDATE OR DELETE ON project.project
FOR EACH ROW EXECUTE FUNCTION common.record_entity_change();
```

### 3.2 JSONB型のパフォーマンス最適化

#### インデックス戦略

```sql
-- JSONB全体に対するGINインデックス（containment演算子 @>用）
CREATE INDEX ix_project_attributes_gin ON project.project USING gin (attributes);

-- 特定のパスに対するGINインデックス
CREATE INDEX ix_project_client_info ON project.project USING gin ((attributes->'client_info'));

-- jsonb_path_ops演算子クラスを使用したインデックス（containment演算子のみ最適化）
CREATE INDEX ix_project_attributes_path_ops ON project.project USING gin (attributes jsonb_path_ops);

-- 特定のキーに対するbtreeインデックス
CREATE INDEX ix_project_priority ON project.project ((attributes->>'priority'));

-- 配列要素に対するGINインデックス
CREATE INDEX ix_project_tags ON project.project USING gin ((attributes->'tags'));
```

#### クエリ最適化テクニック

```sql
-- 効率的なJSONB検索クエリ

-- 等価検索（インデックス使用）
SELECT * FROM project.project WHERE attributes->>'priority' = 'high';

-- 包含検索（GINインデックス使用）
SELECT * FROM project.project WHERE attributes @> '{"priority": "high", "tags": ["PostgreSQL"]}';

-- 配列要素検索（インデックス使用）
SELECT * FROM project.project WHERE attributes->'tags' ? 'PostgreSQL';

-- 非効率的なクエリ（インデックス不使用の可能性）
SELECT * FROM project.project WHERE attributes::text LIKE '%PostgreSQL%';  -- 避けるべき

-- より効率的なクエリへの書き換え
SELECT * FROM project.project WHERE attributes @> '{"tags": ["PostgreSQL"]}';
```

## 4. マテリアライズドビューの活用

複雑な集計処理や頻繁に参照されるデータの高速アクセスのためにマテリアライズドビューを活用する方法を示す。

### 4.1 マテリアライズドビュー設計パターン

#### 4.1.1 集計データキャッシュ

複雑な集計クエリの結果をキャッシュするためのマテリアライズドビュー。

```sql
-- 月次工数集計マテリアライズドビュー
CREATE MATERIALIZED VIEW reporting.monthly_working_hours AS
SELECT
    DATE_TRUNC('month', wh.work_date) AS month,
    e.id AS engineer_id,
    e.name AS engineer_name,
    p.id AS project_id,
    p.title AS project_title,
    SUM(wh.hours) AS total_hours,
    COUNT(DISTINCT wh.work_date) AS working_days
FROM timesheet.working_hours wh
JOIN engineer.engineer e ON wh.engineer_id = e.id
JOIN project.project p ON wh.project_id = p.id
GROUP BY
    DATE_TRUNC('month', wh.work_date),
    e.id,
    e.name,
    p.id,
    p.title
WITH DATA;

-- インデックス作成
CREATE UNIQUE INDEX uix_monthly_working_hours ON reporting.monthly_working_hours 
(month, engineer_id, project_id);
CREATE INDEX ix_monthly_working_hours_engineer ON reporting.monthly_working_hours (engineer_id);
CREATE INDEX ix_monthly_working_hours_project ON reporting.monthly_working_hours (project_id);

-- マテリアライズドビューの更新
REFRESH MATERIALIZED VIEW reporting.monthly_working_hours;

-- CONCURRENTLY更新（ビューへのアクセスをブロックしない）
REFRESH MATERIALIZED VIEW CONCURRENTLY reporting.monthly_working_hours;
```

#### 4.1.2 検索最適化ビュー

検索パフォーマンスを向上させるためのマテリアライズドビュー。

```sql
-- 技術者検索最適化ビュー
CREATE MATERIALIZED VIEW engineer.engineer_search AS
SELECT
    e.id,
    e.name,
    e.email,
    e.status_code,
    to_tsvector('japanese', e.name || ' ' || COALESCE(e.biography, '')) AS search_vector,
    ARRAY(
        SELECT s.skill_name
        FROM engineer.engineer_skill es
        JOIN engineer.skill s ON es.skill_id = s.id
        WHERE es.engineer_id = e.id
    ) AS skills_array,
    jsonb_agg(
        jsonb_build_object(
            'id', s.id,
            'name', s.skill_name,
            'level', es.level,
            'years', es.years
        )
    ) AS skills_json,
    ARRAY(
        SELECT c.name
        FROM engineer.certification ec
        JOIN engineer.certification_master c ON ec.certification_id = c.id
        WHERE ec.engineer_id = e.id
    ) AS certifications
FROM engineer.engineer e
LEFT JOIN engineer.engineer_skill es ON e.id = es.engineer_id
LEFT JOIN engineer.skill s ON es.skill_id = s.id
GROUP BY e.id, e.name, e.email, e.status_code
WITH DATA;

-- インデックス作成
CREATE UNIQUE INDEX uix_engineer_search ON engineer.engineer_search (id);
CREATE INDEX ix_engineer_search_vector ON engineer.engineer_search USING gin (search_vector);
CREATE INDEX ix_engineer_search_skills ON engineer.engineer_search USING gin (skills_array);
CREATE INDEX ix_engineer_search_status ON engineer.engineer_search (status_code);

-- 定期更新関数
CREATE OR REPLACE FUNCTION engineer.refresh_engineer_search()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY engineer.engineer_search;
END;
$$ LANGUAGE plpgsql;

-- cron ジョブとして登録（pg_cron拡張使用）
SELECT cron.schedule('0 */4 * * *', $$SELECT engineer.refresh_engineer_search()$$);
```

#### 4.1.3 レポーティングビュー

レポート生成のためのマテリアライズドビュー。

```sql
-- 案件ステータスサマリービュー
CREATE MATERIALIZED VIEW reporting.project_status_summary AS
SELECT
    DATE_TRUNC('month', p.start_date) AS month,
    p.status_code,
    cv.value AS status_name,
    COUNT(*) AS project_count,
    SUM(CASE WHEN p.end_date IS NOT NULL THEN (p.end_date - p.start_date) ELSE NULL END) AS total_days,
    AVG(CASE WHEN p.end_date IS NOT NULL THEN (p.end_date - p.start_date) ELSE NULL END) AS avg_days,
    jsonb_agg(
        jsonb_build_object(
            'id', p.id,
            'title', p.title,
            'start_date', p.start_date,
            'end_date', p.end_date
        )
    ) AS projects
FROM project.project p
JOIN common.code_value cv ON p.status_code = cv.code AND cv.category_id = 2  -- 2はステータスコードカテゴリID
GROUP BY
    DATE_TRUNC('month', p.start_date),
    p.status_code,
    cv.value
WITH DATA;

-- インデックス作成
CREATE UNIQUE INDEX uix_project_status_summary ON reporting.project_status_summary 
(month, status_code);

-- マテリアライズドビューの更新スケジュール
CREATE OR REPLACE FUNCTION reporting.refresh_all_materialized_views()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY reporting.project_status_summary;
    REFRESH MATERIALIZED VIEW CONCURRENTLY reporting.monthly_working_hours;
    -- 他のマテリアライズドビューも追加
END;
$$ LANGUAGE plpgsql;

-- 毎日深夜に更新
SELECT cron.schedule('0 2 * * *', $$SELECT reporting.refresh_all_materialized_views()$$);
```

### 4.2 更新戦略

マテリアライズドビューの最適な更新戦略を示す。

#### 4.2.1 定期更新

```sql
-- 定期更新タスク
CREATE OR REPLACE FUNCTION common.schedule_materialized_view_refresh()
RETURNS void AS $$
BEGIN
    -- 頻繁に更新が必要なビュー（4時間ごと）
    PERFORM cron.schedule('0 */4 * * *', $$REFRESH MATERIALIZED VIEW CONCURRENTLY engineer.engineer_search$$);
    
    -- 日次更新が適切なビュー（毎日深夜）
    PERFORM cron.schedule('0 1 * * *', $$REFRESH MATERIALIZED VIEW CONCURRENTLY reporting.monthly_working_hours$$);
    
    -- 週次更新が適切なビュー（毎週日曜深夜）
    PERFORM cron.schedule('0 2 * * 0', $$REFRESH MATERIALIZED VIEW CONCURRENTLY reporting.project_status_summary$$);
    
    -- 月次更新が適切なビュー（毎月1日）
    PERFORM cron.schedule('0 3 1 * *', $$REFRESH MATERIALIZED VIEW reporting.yearly_summary$$);
END;
$$ LANGUAGE plpgsql;

-- スケジュール設定の実行
SELECT common.schedule_materialized_view_refresh();
```

#### 4.2.2 トリガーベース更新

```sql
-- マテリアライズドビュー更新トリガー関数
CREATE OR REPLACE FUNCTION engineer.trigger_refresh_engineer_search()
RETURNS trigger AS $$
BEGIN
    -- 変更フラグテーブルに記録
    INSERT INTO common.materialized_view_refresh_queue (view_name, refresh_requested_at)
    VALUES ('engineer.engineer_search', now())
    ON CONFLICT (view_name) DO UPDATE
    SET refresh_requested_at = now(), refresh_count = common.materialized_view_refresh_queue.refresh_count + 1;
    
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- トリガーの設定
CREATE TRIGGER trg_engineer_update
AFTER INSERT OR UPDATE OR DELETE ON engineer.engineer
FOR EACH STATEMENT EXECUTE FUNCTION engineer.trigger_refresh_engineer_search();

CREATE TRIGGER trg_engineer_skill_update
AFTER INSERT OR UPDATE OR DELETE ON engineer.engineer_skill
FOR EACH STATEMENT EXECUTE FUNCTION engineer.trigger_refresh_engineer_search();

-- バッチ処理で一括更新（5分ごと実行）
CREATE OR REPLACE FUNCTION common.process_materialized_view_refresh_queue()
RETURNS void AS $$
DECLARE
    r record;
BEGIN
    FOR r IN
        SELECT view_name, refresh_count
        FROM common.materialized_view_refresh_queue
        WHERE (refresh_requested_at > refresh_last_run_at OR refresh_last_run_at IS NULL)
        AND refresh_requested_at < now() - interval '1 minute'  -- 変更直後は更新しない（連続更新防止）
        FOR UPDATE SKIP LOCKED
    LOOP
        BEGIN
            EXECUTE format('REFRESH MATERIALIZED VIEW CONCURRENTLY %I', r.view_name);
            
            UPDATE common.materialized_view_refresh_queue
            SET refresh_last_run_at = now(),
                refresh_count = 0,
                last_refresh_success = true,
                last_error = NULL
            WHERE view_name = r.view_name;
            
            RAISE NOTICE 'Refreshed materialized view: %', r.view_name;
        EXCEPTION
            WHEN OTHERS THEN
                UPDATE common.materialized_view_refresh_queue
                SET last_refresh_success = false,
                    last_error = SQLERRM
                WHERE view_name = r.view_name;
                
                RAISE WARNING 'Failed to refresh materialized view %: %', r.view_name, SQLERRM;
        END;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- 定期実行（pg_cron拡張使用）
SELECT cron.schedule('*/5 * * * *', $$SELECT common.process_materialized_view_refresh_queue()$$);
```

## 5. 拡張機能の管理戦略

### 5.1 拡張機能のインストールと管理

拡張機能を一貫した方法で管理するための戦略を示す。

```sql
-- 拡張機能管理スクリプト
CREATE OR REPLACE FUNCTION common.setup_extensions()
RETURNS void AS $$
DECLARE
    extensions text[] := ARRAY[
        'uuid-ossp',
        'citext',
        'pg_trgm',
        'pg_bigm',
        'pg_stat_statements',
        'pg_hint_plan',
        'pg_cron',
        'tablefunc',
        'hstore',
        'ltree',
        'pgcrypto'
    ];
    ext text;
BEGIN
    FOREACH ext IN ARRAY extensions
    LOOP
        BEGIN
            EXECUTE format('CREATE EXTENSION IF NOT EXISTS %I CASCADE', ext);
            RAISE NOTICE 'Extension % installed or already exists', ext;
        EXCEPTION
            WHEN OTHERS THEN
                RAISE WARNING 'Failed to install extension %: %', ext, SQLERRM;
        END;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- 拡張機能のバージョン管理ビュー
CREATE OR REPLACE VIEW common.extension_versions AS
SELECT
    name,
    default_version,
    installed_version,
    comment
FROM pg_available_extensions
WHERE installed_version IS NOT NULL
ORDER BY name;
```

### 5.2 拡張機能の設定管理

拡張機能の設定を管理するための戦略を示す。

```sql
-- 拡張機能設定管理
CREATE TABLE common.extension_settings (
    id serial PRIMARY KEY,
    extension_name varchar(50) NOT NULL,
    environment varchar(20) NOT NULL,  -- 'development', 'staging', 'production'
    settings jsonb NOT NULL,
    description text,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by integer REFERENCES common.user(id),
    UNIQUE (extension_name, environment)
);

-- 設定適用関数
CREATE OR REPLACE FUNCTION common.apply_extension_settings(p_environment varchar)
RETURNS void AS $$
DECLARE
    r record;
    setting_key text;
    setting_value text;
BEGIN
    FOR r IN
        SELECT extension_name, settings
        FROM common.extension_settings
        WHERE environment = p_environment
    LOOP
        RAISE NOTICE 'Applying settings for extension: %', r.extension_name;
        
        FOR setting_key, setting_value IN
            SELECT key, value FROM jsonb_each_text(r.settings)
        LOOP
            -- 実行時設定のみ更新（一部の設定はpostgresql.confの変更が必要）
            BEGIN
                IF r.extension_name = 'core' THEN
                    -- コアPostgreSQL設定
                    EXECUTE format('SET %I = %L', setting_key, setting_value);
                ELSE
                    -- 拡張機能設定
                    EXECUTE format('SET %I.%I = %L', r.extension_name, setting_key, setting_value);
                END IF;
                
                RAISE NOTICE 'Applied setting %: %', 
                    CASE WHEN r.extension_name = 'core' THEN setting_key
                    ELSE r.extension_name || '.' || setting_key END,
                    setting_value;
            EXCEPTION
                WHEN OTHERS THEN
                    RAISE WARNING 'Failed to apply setting %: %', 
                        CASE WHEN r.extension_name = 'core' THEN setting_key
                        ELSE r.extension_name || '.' || setting_key END,
                        SQLERRM;
            END;
        END LOOP;
    END LOOP;
END;
$$ LANGUAGE plpgsql;
```

## 6. モジュール別テーブル定義補足との関連

各業務モジュールの「テーブル定義補足」ドキュメントには、そのモジュール固有の拡張機能活用方法を記載する。本ドキュメントは全体方針と共通パターンを定義し、各モジュールの詳細は対応する補足ドキュメントを参照すること。