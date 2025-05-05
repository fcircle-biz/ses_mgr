-- SES Management System PostgreSQL Schema
-- 09_utility.sql: ユーティリティテーブル
-- Created: 2025-05-06

-- ============================
-- UTILITY TABLES
-- ============================

CREATE TABLE IF NOT EXISTS holidays (
    holiday_id SERIAL PRIMARY KEY,
    holiday_date DATE NOT NULL,
    holiday_name VARCHAR(100) NOT NULL,
    holiday_type TEXT NOT NULL CHECK (holiday_type IN ('祝日', '会社休業日', '振替休日', '特別休業日', 'その他')) DEFAULT '祝日',
    is_half_day BOOLEAN NOT NULL DEFAULT FALSE,
    is_recurring BOOLEAN NOT NULL DEFAULT FALSE,
    recurring_pattern TEXT,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (holiday_date, holiday_type)
);

CREATE INDEX IF NOT EXISTS idx_holidays_date ON holidays(holiday_date);
CREATE INDEX IF NOT EXISTS idx_holidays_type ON holidays(holiday_type);
CREATE INDEX IF NOT EXISTS idx_holidays_is_recurring ON holidays(is_recurring);

CREATE TRIGGER update_holidays_timestamp
BEFORE UPDATE ON holidays
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS code_master (
    code_id SERIAL PRIMARY KEY,
    code_type VARCHAR(50) NOT NULL,
    code_key VARCHAR(50) NOT NULL,
    code_value VARCHAR(200) NOT NULL,
    display_order INTEGER DEFAULT 0,
    is_default BOOLEAN DEFAULT FALSE,
    parent_code_id INTEGER REFERENCES code_master(code_id),
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    valid_from DATE,
    valid_until DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (code_type, code_key)
);

CREATE INDEX IF NOT EXISTS idx_code_master_type ON code_master(code_type);
CREATE INDEX IF NOT EXISTS idx_code_master_key ON code_master(code_key);
CREATE INDEX IF NOT EXISTS idx_code_master_is_active ON code_master(is_active);
CREATE INDEX IF NOT EXISTS idx_code_master_parent_code_id ON code_master(parent_code_id);
CREATE INDEX IF NOT EXISTS idx_code_master_valid_period ON code_master(valid_from, valid_until);

CREATE TRIGGER update_code_master_timestamp
BEFORE UPDATE ON code_master
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS system_parameters (
    parameter_id SERIAL PRIMARY KEY,
    parameter_key VARCHAR(100) NOT NULL UNIQUE,
    parameter_value TEXT,
    data_type VARCHAR(20) NOT NULL CHECK (data_type IN ('文字列', '数値', '真偽値', '日付', 'JSON', 'その他')) DEFAULT '文字列',
    description TEXT,
    is_sensitive BOOLEAN NOT NULL DEFAULT FALSE,
    is_readonly BOOLEAN NOT NULL DEFAULT FALSE,
    valid_from TIMESTAMP,
    valid_until TIMESTAMP,
    modified_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_system_parameters_key ON system_parameters(parameter_key);
CREATE INDEX IF NOT EXISTS idx_system_parameters_is_sensitive ON system_parameters(is_sensitive);
CREATE INDEX IF NOT EXISTS idx_system_parameters_valid_period ON system_parameters(valid_from, valid_until);

CREATE TRIGGER update_system_parameters_timestamp
BEFORE UPDATE ON system_parameters
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS file_storage (
    file_id SERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_type VARCHAR(100),
    mime_type VARCHAR(100),
    file_size BIGINT,
    file_hash VARCHAR(100),
    storage_location TEXT CHECK (storage_location IN ('ローカル', 'S3', 'その他')) DEFAULT 'ローカル',
    entity_type VARCHAR(100),
    entity_id INTEGER,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    access_token VARCHAR(100),
    expiry_date TIMESTAMP,
    uploaded_by UUID REFERENCES users(user_id),
    description TEXT,
    tags VARCHAR[],
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_file_storage_file_name ON file_storage(file_name);
CREATE INDEX IF NOT EXISTS idx_file_storage_entity ON file_storage(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_file_storage_is_public ON file_storage(is_public);
CREATE INDEX IF NOT EXISTS idx_file_storage_uploaded_by ON file_storage(uploaded_by);
CREATE INDEX IF NOT EXISTS idx_file_storage_tags ON file_storage USING GIN(tags);
CREATE INDEX IF NOT EXISTS idx_file_storage_expiry_date ON file_storage(expiry_date);

CREATE TRIGGER update_file_storage_timestamp
BEFORE UPDATE ON file_storage
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS system_logs (
    log_id SERIAL PRIMARY KEY,
    log_level VARCHAR(20) NOT NULL CHECK (log_level IN ('DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL')) DEFAULT 'INFO',
    log_category VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    user_id UUID REFERENCES users(user_id),
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_url TEXT,
    request_method VARCHAR(10),
    request_params JSONB,
    response_status INTEGER,
    response_time INTEGER,
    stack_trace TEXT,
    additional_data JSONB,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_system_logs_log_level ON system_logs(log_level);
CREATE INDEX IF NOT EXISTS idx_system_logs_occurred_at ON system_logs(occurred_at);
CREATE INDEX IF NOT EXISTS idx_system_logs_user_id ON system_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_system_logs_log_category ON system_logs(log_category);

CREATE TABLE IF NOT EXISTS batch_jobs (
    job_id SERIAL PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    job_description TEXT,
    job_class VARCHAR(255) NOT NULL,
    cron_expression VARCHAR(100),
    parameters JSONB DEFAULT '{}',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_execution_time TIMESTAMP,
    last_execution_status VARCHAR(20),
    last_execution_message TEXT,
    next_execution_time TIMESTAMP,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    timeout_seconds INTEGER,
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_batch_jobs_job_name ON batch_jobs(job_name);
CREATE INDEX IF NOT EXISTS idx_batch_jobs_is_active ON batch_jobs(is_active);
CREATE INDEX IF NOT EXISTS idx_batch_jobs_next_execution_time ON batch_jobs(next_execution_time);
CREATE INDEX IF NOT EXISTS idx_batch_jobs_last_execution_status ON batch_jobs(last_execution_status);

CREATE TRIGGER update_batch_jobs_timestamp
BEFORE UPDATE ON batch_jobs
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS batch_job_executions (
    execution_id SERIAL PRIMARY KEY,
    job_id INTEGER NOT NULL REFERENCES batch_jobs(job_id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    execution_status VARCHAR(20) NOT NULL CHECK (execution_status IN ('実行中', '完了', 'エラー', 'タイムアウト', 'キャンセル')) DEFAULT '実行中',
    exit_code VARCHAR(20),
    exit_message TEXT,
    execution_parameters JSONB,
    execution_results JSONB,
    triggered_by VARCHAR(100) CHECK (triggered_by IN ('スケジュール', '手動', 'API')) DEFAULT 'スケジュール',
    triggered_by_user UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_batch_job_executions_job_id ON batch_job_executions(job_id);
CREATE INDEX IF NOT EXISTS idx_batch_job_executions_start_time ON batch_job_executions(start_time);
CREATE INDEX IF NOT EXISTS idx_batch_job_executions_execution_status ON batch_job_executions(execution_status);
CREATE INDEX IF NOT EXISTS idx_batch_job_executions_triggered_by ON batch_job_executions(triggered_by);

COMMENT ON TABLE holidays IS '休日情報を管理するテーブル';
COMMENT ON TABLE code_master IS 'コード値マスターを管理するテーブル';
COMMENT ON TABLE system_parameters IS 'システムパラメータを管理するテーブル';
COMMENT ON TABLE file_storage IS 'ファイル保存情報を管理するテーブル';
COMMENT ON TABLE system_logs IS 'システムログを管理するテーブル';
COMMENT ON TABLE batch_jobs IS 'バッチジョブ定義を管理するテーブル';
COMMENT ON TABLE batch_job_executions IS 'バッチジョブ実行履歴を管理するテーブル';