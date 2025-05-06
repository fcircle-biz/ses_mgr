-- バッチ管理システム用テーブル定義

-- バッチジョブ定義テーブル
CREATE TABLE batch_job (
    job_id VARCHAR(50) PRIMARY KEY COMMENT 'バッチジョブID',
    name VARCHAR(100) NOT NULL COMMENT 'バッチジョブ名',
    description TEXT COMMENT 'バッチジョブの説明',
    category VARCHAR(50) COMMENT 'カテゴリ（report, billing, import, maintenance等）',
    job_type VARCHAR(50) NOT NULL COMMENT 'ジョブタイプ（技術的な分類）',
    status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT 'ステータス（active, inactive）',
    parameters JSON COMMENT 'デフォルトのパラメータ設定（JSON形式）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    created_by BINARY(16) COMMENT '作成者ID',
    updated_by BINARY(16) COMMENT '更新者ID',
    INDEX idx_batch_job_category (category),
    INDEX idx_batch_job_status (status),
    INDEX idx_batch_job_job_type (job_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='バッチジョブ定義';

-- バッチスケジュール定義テーブル
CREATE TABLE batch_schedule (
    schedule_id VARCHAR(50) PRIMARY KEY COMMENT 'スケジュールID',
    job_id VARCHAR(50) NOT NULL COMMENT 'バッチジョブID',
    description VARCHAR(255) COMMENT 'スケジュールの説明',
    cron_expression VARCHAR(100) NOT NULL COMMENT 'cron式',
    schedule_type VARCHAR(20) NOT NULL COMMENT 'スケジュールタイプ（daily, weekly, monthly等）',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '有効フラグ',
    parameters JSON COMMENT 'パラメータ上書き設定（JSON形式）',
    next_run DATETIME COMMENT '次回実行予定日時',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    created_by BINARY(16) COMMENT '作成者ID',
    updated_by BINARY(16) COMMENT '更新者ID',
    FOREIGN KEY (job_id) REFERENCES batch_job(job_id),
    INDEX idx_batch_schedule_job_id (job_id),
    INDEX idx_batch_schedule_is_active (is_active),
    INDEX idx_batch_schedule_next_run (next_run)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='バッチスケジュール定義';

-- バッチ実行履歴テーブル
CREATE TABLE batch_execution_history (
    execution_id VARCHAR(50) PRIMARY KEY COMMENT '実行ID',
    job_id VARCHAR(50) NOT NULL COMMENT 'バッチジョブID',
    schedule_id VARCHAR(50) COMMENT 'スケジュールID（手動実行の場合はNULL）',
    status VARCHAR(20) NOT NULL COMMENT 'ステータス（scheduled, running, success, error, canceled, timeout）',
    start_time DATETIME NOT NULL COMMENT '開始日時',
    end_time DATETIME COMMENT '終了日時',
    execution_time INT COMMENT '実行時間（秒）',
    executed_by VARCHAR(100) NOT NULL COMMENT '実行者（user:ユーザーID または scheduler）',
    trigger_type VARCHAR(20) NOT NULL COMMENT 'トリガータイプ（scheduled, manual）',
    records_processed INT COMMENT '処理レコード数',
    error_message TEXT COMMENT 'エラーメッセージ',
    parameters JSON COMMENT '実行時パラメータ（JSON形式）',
    description VARCHAR(255) COMMENT '実行時の説明（手動実行時の理由など）',
    logs TEXT COMMENT '実行ログ（要約）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    FOREIGN KEY (job_id) REFERENCES batch_job(job_id),
    FOREIGN KEY (schedule_id) REFERENCES batch_schedule(schedule_id),
    INDEX idx_batch_execution_job_id (job_id),
    INDEX idx_batch_execution_schedule_id (schedule_id),
    INDEX idx_batch_execution_status (status),
    INDEX idx_batch_execution_start_time (start_time),
    INDEX idx_batch_execution_trigger_type (trigger_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='バッチ実行履歴';

-- バッチジョブ依存関係テーブル
CREATE TABLE batch_job_dependency (
    dependency_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '依存関係ID',
    job_id VARCHAR(50) NOT NULL COMMENT '依存元ジョブID',
    dependent_job_id VARCHAR(50) NOT NULL COMMENT '依存先ジョブID',
    dependency_type VARCHAR(50) NOT NULL COMMENT '依存タイプ（waitForCompletion, waitForSuccess）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    FOREIGN KEY (job_id) REFERENCES batch_job(job_id),
    FOREIGN KEY (dependent_job_id) REFERENCES batch_job(job_id),
    UNIQUE KEY uk_batch_job_dependency (job_id, dependent_job_id),
    INDEX idx_batch_job_dependency_job_id (job_id),
    INDEX idx_batch_job_dependency_dependent_job_id (dependent_job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='バッチジョブの依存関係';

-- テストデータの挿入
-- バッチジョブ
INSERT INTO batch_job (job_id, name, description, category, job_type, status, parameters)
VALUES 
('batch001', '日次データ集計', '前日のデータを集計し、サマリーテーブルを更新するバッチ', 'report', 'daily_summary', 'active', 
 '{"targetTables": ["project_stats", "engineer_stats", "sales_stats"], "daysToKeep": 365, "notifyOnCompletion": true, "notificationEmail": "system_admin@example.com"}'),
 
('batch002', '月次請求データ作成', '月次の請求データを作成するバッチ', 'billing', 'monthly_billing', 'active', 
 '{"targetMonth": "current", "formatVersion": "v2", "notifyFinance": true}'),
 
('batch003', '顧客データインポート', '外部システムから顧客データをインポートするバッチ', 'import', 'customer_import', 'active', 
 '{"source": "external_crm", "includeDeleted": false, "mappingVersion": "v3"}'),
 
('batch004', '日次バックアップ', 'データベースの日次バックアップを実行', 'maintenance', 'database_backup', 'active', 
 '{"compressionLevel": 9, "retentionDays": 30, "backupLocation": "cloud_storage"}'),
 
('batch005', 'ログ整理', '古いログデータの整理・アーカイブ', 'maintenance', 'log_maintenance', 'active', 
 '{"olderThan": 30, "archiveFormat": "zip", "deleteAfterArchive": true}');

-- バッチスケジュール
INSERT INTO batch_schedule (schedule_id, job_id, description, cron_expression, schedule_type, is_active, parameters, next_run)
VALUES 
('schedule001', 'batch001', '毎日午前1時に実行', '0 0 1 * * ?', 'daily', TRUE, '{}', DATE_ADD(CURRENT_DATE(), INTERVAL 1 DAY)),
('schedule002', 'batch002', '毎月1日午前2時に実行', '0 0 2 1 * ?', 'monthly', TRUE, '{}', DATE_ADD(DATE_ADD(CURRENT_DATE(), INTERVAL 1 MONTH), INTERVAL -DAY(CURRENT_DATE())+1 DAY)),
('schedule003', 'batch003', '毎日午前3時に実行', '0 0 3 * * ?', 'daily', TRUE, '{"source": "external_crm", "includeDeleted": false}', DATE_ADD(CURRENT_DATE(), INTERVAL 1 DAY)),
('schedule004', 'batch004', '毎日午前0時に実行', '0 0 0 * * ?', 'daily', TRUE, '{}', DATE_ADD(CURRENT_DATE(), INTERVAL 1 DAY)),
('schedule005', 'batch005', '毎週日曜日午前1時に実行', '0 0 1 ? * SUN', 'weekly', TRUE, '{}', DATE_ADD(CURRENT_DATE(), INTERVAL (7 - WEEKDAY(CURRENT_DATE()) + 7) % 7 DAY));

-- バッチ実行履歴（サンプル）
INSERT INTO batch_execution_history (execution_id, job_id, schedule_id, status, start_time, end_time, execution_time, executed_by, trigger_type, records_processed, error_message, parameters, description)
VALUES 
-- 成功例
('exec12345', 'batch001', 'schedule001', 'success', 
 DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 1 DAY), 
 DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 1 DAY) + INTERVAL 10 MINUTE, 
 600, 'scheduler', 'scheduled', 15420, NULL, 
 '{"targetDate": "2024-05-03"}', NULL),

-- 別の成功例（手動実行）
('exec12346', 'batch001', NULL, 'success', 
 DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 10 HOUR), 
 DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 10 HOUR) + INTERVAL 10 MINUTE, 
 600, 'user:admin', 'manual', 15420, NULL, 
 '{"targetDate": "2024-05-01", "forceReprocess": true}', '手動実行（月初データ再集計）'),

-- エラー例
('exec12347', 'batch003', 'schedule003', 'error', 
 DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 1 DAY), 
 DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 1 DAY) + INTERVAL 5 MINUTE, 
 300, 'scheduler', 'scheduled', NULL, 
 '接続エラー: 外部システムが応答しません', 
 '{"source": "external_crm", "includeDeleted": false}', NULL);

-- バッチジョブ依存関係
INSERT INTO batch_job_dependency (job_id, dependent_job_id, dependency_type)
VALUES 
('batch001', 'batch004', 'waitForCompletion');