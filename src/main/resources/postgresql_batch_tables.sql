-- PostgreSQL用バッチ管理システム用テーブル定義

-- バッチジョブ定義テーブル
CREATE TABLE batch_job (
    job_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    job_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    parameters JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

COMMENT ON TABLE batch_job IS 'バッチジョブ定義';
COMMENT ON COLUMN batch_job.job_id IS 'バッチジョブID';
COMMENT ON COLUMN batch_job.name IS 'バッチジョブ名';
COMMENT ON COLUMN batch_job.description IS 'バッチジョブの説明';
COMMENT ON COLUMN batch_job.category IS 'カテゴリ（report, billing, import, maintenance等）';
COMMENT ON COLUMN batch_job.job_type IS 'ジョブタイプ（技術的な分類）';
COMMENT ON COLUMN batch_job.status IS 'ステータス（active, inactive）';
COMMENT ON COLUMN batch_job.parameters IS 'デフォルトのパラメータ設定（JSON形式）';

CREATE INDEX idx_batch_job_category ON batch_job(category);
CREATE INDEX idx_batch_job_status ON batch_job(status);
CREATE INDEX idx_batch_job_job_type ON batch_job(job_type);

-- バッチスケジュール定義テーブル
CREATE TABLE batch_schedule (
    schedule_id VARCHAR(50) PRIMARY KEY,
    job_id VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    cron_expression VARCHAR(100) NOT NULL,
    schedule_type VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    parameters JSONB,
    next_run TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    FOREIGN KEY (job_id) REFERENCES batch_job(job_id)
);

COMMENT ON TABLE batch_schedule IS 'バッチスケジュール定義';
COMMENT ON COLUMN batch_schedule.schedule_id IS 'スケジュールID';
COMMENT ON COLUMN batch_schedule.job_id IS 'バッチジョブID';
COMMENT ON COLUMN batch_schedule.description IS 'スケジュールの説明';
COMMENT ON COLUMN batch_schedule.cron_expression IS 'cron式';
COMMENT ON COLUMN batch_schedule.schedule_type IS 'スケジュールタイプ（daily, weekly, monthly等）';
COMMENT ON COLUMN batch_schedule.is_active IS '有効フラグ';
COMMENT ON COLUMN batch_schedule.parameters IS 'パラメータ上書き設定（JSON形式）';
COMMENT ON COLUMN batch_schedule.next_run IS '次回実行予定日時';

CREATE INDEX idx_batch_schedule_job_id ON batch_schedule(job_id);
CREATE INDEX idx_batch_schedule_is_active ON batch_schedule(is_active);
CREATE INDEX idx_batch_schedule_next_run ON batch_schedule(next_run);

-- バッチ実行履歴テーブル
CREATE TABLE batch_execution_history (
    execution_id VARCHAR(50) PRIMARY KEY,
    job_id VARCHAR(50) NOT NULL,
    schedule_id VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    execution_time INTEGER,
    executed_by VARCHAR(100) NOT NULL,
    trigger_type VARCHAR(20) NOT NULL,
    records_processed INTEGER,
    error_message TEXT,
    parameters JSONB,
    description VARCHAR(255),
    logs TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES batch_job(job_id),
    FOREIGN KEY (schedule_id) REFERENCES batch_schedule(schedule_id)
);

COMMENT ON TABLE batch_execution_history IS 'バッチ実行履歴';
COMMENT ON COLUMN batch_execution_history.execution_id IS '実行ID';
COMMENT ON COLUMN batch_execution_history.job_id IS 'バッチジョブID';
COMMENT ON COLUMN batch_execution_history.schedule_id IS 'スケジュールID（手動実行の場合はNULL）';
COMMENT ON COLUMN batch_execution_history.status IS 'ステータス（scheduled, running, success, error, canceled, timeout）';
COMMENT ON COLUMN batch_execution_history.start_time IS '開始日時';
COMMENT ON COLUMN batch_execution_history.end_time IS '終了日時';
COMMENT ON COLUMN batch_execution_history.execution_time IS '実行時間（秒）';
COMMENT ON COLUMN batch_execution_history.executed_by IS '実行者（user:ユーザーID または scheduler）';
COMMENT ON COLUMN batch_execution_history.trigger_type IS 'トリガータイプ（scheduled, manual）';
COMMENT ON COLUMN batch_execution_history.records_processed IS '処理レコード数';
COMMENT ON COLUMN batch_execution_history.error_message IS 'エラーメッセージ';
COMMENT ON COLUMN batch_execution_history.parameters IS '実行時パラメータ（JSON形式）';
COMMENT ON COLUMN batch_execution_history.description IS '実行時の説明（手動実行時の理由など）';
COMMENT ON COLUMN batch_execution_history.logs IS '実行ログ（要約）';

CREATE INDEX idx_batch_execution_job_id ON batch_execution_history(job_id);
CREATE INDEX idx_batch_execution_schedule_id ON batch_execution_history(schedule_id);
CREATE INDEX idx_batch_execution_status ON batch_execution_history(status);
CREATE INDEX idx_batch_execution_start_time ON batch_execution_history(start_time);
CREATE INDEX idx_batch_execution_trigger_type ON batch_execution_history(trigger_type);

-- バッチジョブ依存関係テーブル
CREATE TABLE batch_job_dependency (
    dependency_id SERIAL PRIMARY KEY,
    job_id VARCHAR(50) NOT NULL,
    dependent_job_id VARCHAR(50) NOT NULL,
    dependency_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES batch_job(job_id),
    FOREIGN KEY (dependent_job_id) REFERENCES batch_job(job_id),
    UNIQUE (job_id, dependent_job_id)
);

COMMENT ON TABLE batch_job_dependency IS 'バッチジョブの依存関係';
COMMENT ON COLUMN batch_job_dependency.dependency_id IS '依存関係ID';
COMMENT ON COLUMN batch_job_dependency.job_id IS '依存元ジョブID';
COMMENT ON COLUMN batch_job_dependency.dependent_job_id IS '依存先ジョブID';
COMMENT ON COLUMN batch_job_dependency.dependency_type IS '依存タイプ（waitForCompletion, waitForSuccess）';

CREATE INDEX idx_batch_job_dependency_job_id ON batch_job_dependency(job_id);
CREATE INDEX idx_batch_job_dependency_dependent_job_id ON batch_job_dependency(dependent_job_id);

-- テストデータの挿入
-- バッチジョブ
INSERT INTO batch_job (job_id, name, description, category, job_type, status, parameters)
VALUES 
('batch001', '日次データ集計', '前日のデータを集計し、サマリーテーブルを更新するバッチ', 'report', 'daily_summary', 'active', 
 '{"targetTables": ["project_stats", "engineer_stats", "sales_stats"], "daysToKeep": 365, "notifyOnCompletion": true, "notificationEmail": "system_admin@example.com"}'::jsonb),
 
('batch002', '月次請求データ作成', '月次の請求データを作成するバッチ', 'billing', 'monthly_billing', 'active', 
 '{"targetMonth": "current", "formatVersion": "v2", "notifyFinance": true}'::jsonb),
 
('batch003', '顧客データインポート', '外部システムから顧客データをインポートするバッチ', 'import', 'customer_import', 'active', 
 '{"source": "external_crm", "includeDeleted": false, "mappingVersion": "v3"}'::jsonb),
 
('batch004', '日次バックアップ', 'データベースの日次バックアップを実行', 'maintenance', 'database_backup', 'active', 
 '{"compressionLevel": 9, "retentionDays": 30, "backupLocation": "cloud_storage"}'::jsonb),
 
('batch005', 'ログ整理', '古いログデータの整理・アーカイブ', 'maintenance', 'log_maintenance', 'active', 
 '{"olderThan": 30, "archiveFormat": "zip", "deleteAfterArchive": true}'::jsonb);

-- バッチスケジュール
INSERT INTO batch_schedule (schedule_id, job_id, description, cron_expression, schedule_type, is_active, parameters, next_run)
VALUES 
('schedule001', 'batch001', '毎日午前1時に実行', '0 0 1 * * ?', 'daily', TRUE, '{}'::jsonb, CURRENT_DATE + INTERVAL '1 day'),
('schedule002', 'batch002', '毎月1日午前2時に実行', '0 0 2 1 * ?', 'monthly', TRUE, '{}'::jsonb, 
 (DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month')::date),
('schedule003', 'batch003', '毎日午前3時に実行', '0 0 3 * * ?', 'daily', TRUE, 
 '{"source": "external_crm", "includeDeleted": false}'::jsonb, CURRENT_DATE + INTERVAL '1 day'),
('schedule004', 'batch004', '毎日午前0時に実行', '0 0 0 * * ?', 'daily', TRUE, '{}'::jsonb, CURRENT_DATE + INTERVAL '1 day'),
('schedule005', 'batch005', '毎週日曜日午前1時に実行', '0 0 1 ? * SUN', 'weekly', TRUE, '{}'::jsonb, 
 CURRENT_DATE + INTERVAL '1 day' * (7 - EXTRACT(DOW FROM CURRENT_DATE)));

-- バッチ実行履歴（サンプル）
INSERT INTO batch_execution_history (execution_id, job_id, schedule_id, status, start_time, end_time, execution_time, executed_by, trigger_type, records_processed, error_message, parameters, description)
VALUES 
-- 成功例
('exec12345', 'batch001', 'schedule001', 'success', 
 CURRENT_TIMESTAMP - INTERVAL '1 day', 
 CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '10 minutes', 
 600, 'scheduler', 'scheduled', 15420, NULL, 
 '{"targetDate": "2024-05-03"}'::jsonb, NULL),

-- 別の成功例（手動実行）
('exec12346', 'batch001', NULL, 'success', 
 CURRENT_TIMESTAMP - INTERVAL '10 hours', 
 CURRENT_TIMESTAMP - INTERVAL '10 hours' + INTERVAL '10 minutes', 
 600, 'user:admin', 'manual', 15420, NULL, 
 '{"targetDate": "2024-05-01", "forceReprocess": true}'::jsonb, '手動実行（月初データ再集計）'),

-- エラー例
('exec12347', 'batch003', 'schedule003', 'error', 
 CURRENT_TIMESTAMP - INTERVAL '1 day', 
 CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '5 minutes', 
 300, 'scheduler', 'scheduled', NULL, 
 '接続エラー: 外部システムが応答しません', 
 '{"source": "external_crm", "includeDeleted": false}'::jsonb, NULL);

-- バッチジョブ依存関係
INSERT INTO batch_job_dependency (job_id, dependent_job_id, dependency_type)
VALUES 
('batch001', 'batch004', 'waitForCompletion');