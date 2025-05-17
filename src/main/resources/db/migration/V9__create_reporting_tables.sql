-- KPI定義テーブル
CREATE TABLE reporting.kpi_definition (
    id SERIAL PRIMARY KEY,
    kpi_code VARCHAR(50) NOT NULL UNIQUE,
    kpi_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    calculation_method TEXT NOT NULL,
    data_source VARCHAR(255) NOT NULL,
    unit VARCHAR(50),
    display_format VARCHAR(50) NOT NULL DEFAULT 'NUMBER',
    target_value NUMERIC(18,2),
    threshold_warning NUMERIC(18,2),
    threshold_danger NUMERIC(18,2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_kpi_definition_category ON reporting.kpi_definition(category);
CREATE INDEX ix_kpi_definition_is_active ON reporting.kpi_definition(is_active);

-- KPI値テーブル
CREATE TABLE reporting.kpi_value (
    id BIGSERIAL PRIMARY KEY,
    kpi_id INT NOT NULL REFERENCES reporting.kpi_definition(id),
    target_type VARCHAR(50) NOT NULL,
    target_id VARCHAR(255),
    year INT NOT NULL,
    month INT,
    week INT,
    day INT,
    value NUMERIC(18,2) NOT NULL,
    target_value NUMERIC(18,2),
    previous_value NUMERIC(18,2),
    calculation_date TIMESTAMP NOT NULL,
    calculation_details JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_kpi_value_kpi_id ON reporting.kpi_value(kpi_id);
CREATE INDEX ix_kpi_value_target ON reporting.kpi_value(target_type, target_id);
CREATE INDEX ix_kpi_value_year_month ON reporting.kpi_value(year, month);
CREATE INDEX ix_kpi_value_calculation_date ON reporting.kpi_value(calculation_date);

-- ダッシュボード定義テーブル
CREATE TABLE reporting.dashboard (
    id SERIAL PRIMARY KEY,
    dashboard_code VARCHAR(50) NOT NULL UNIQUE,
    dashboard_name VARCHAR(100) NOT NULL,
    description TEXT,
    layout JSONB,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    owner_id BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_dashboard_owner_id ON reporting.dashboard(owner_id);
CREATE INDEX ix_dashboard_is_system ON reporting.dashboard(is_system);
CREATE INDEX ix_dashboard_is_public ON reporting.dashboard(is_public);

-- ダッシュボードウィジェットテーブル
CREATE TABLE reporting.dashboard_widget (
    id SERIAL PRIMARY KEY,
    dashboard_id INT NOT NULL REFERENCES reporting.dashboard(id),
    widget_type VARCHAR(50) NOT NULL,
    widget_title VARCHAR(100) NOT NULL,
    position_x INT NOT NULL,
    position_y INT NOT NULL,
    width INT NOT NULL,
    height INT NOT NULL,
    configuration JSONB NOT NULL,
    data_source VARCHAR(255) NOT NULL,
    refresh_interval INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_dashboard_widget_dashboard_id ON reporting.dashboard_widget(dashboard_id);
CREATE INDEX ix_dashboard_widget_widget_type ON reporting.dashboard_widget(widget_type);

-- レポート定義テーブル
CREATE TABLE reporting.report_definition (
    id SERIAL PRIMARY KEY,
    report_code VARCHAR(50) NOT NULL UNIQUE,
    report_name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    template_file_id BIGINT REFERENCES common.files(id),
    query_definition TEXT,
    parameters JSONB,
    output_formats VARCHAR(255) NOT NULL,
    is_scheduled BOOLEAN NOT NULL DEFAULT FALSE,
    schedule_expression VARCHAR(100),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_report_definition_category ON reporting.report_definition(category);
CREATE INDEX ix_report_definition_report_type ON reporting.report_definition(report_type);
CREATE INDEX ix_report_definition_is_scheduled ON reporting.report_definition(is_scheduled);
CREATE INDEX ix_report_definition_is_system ON reporting.report_definition(is_system);
CREATE INDEX ix_report_definition_is_active ON reporting.report_definition(is_active);

-- レポート生成履歴テーブル
CREATE TABLE reporting.report_generation (
    id BIGSERIAL PRIMARY KEY,
    report_definition_id INT NOT NULL REFERENCES reporting.report_definition(id),
    report_file_id BIGINT REFERENCES common.files(id),
    parameters JSONB,
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    error_message TEXT,
    generated_by BIGINT REFERENCES common.users(id),
    is_scheduled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_report_generation_report_definition_id ON reporting.report_generation(report_definition_id);
CREATE INDEX ix_report_generation_status ON reporting.report_generation(status);
CREATE INDEX ix_report_generation_start_time ON reporting.report_generation(start_time);
CREATE INDEX ix_report_generation_generated_by ON reporting.report_generation(generated_by);
CREATE INDEX ix_report_generation_is_scheduled ON reporting.report_generation(is_scheduled);

-- 予測分析モデルテーブル
CREATE TABLE reporting.prediction_model (
    id SERIAL PRIMARY KEY,
    model_code VARCHAR(50) NOT NULL UNIQUE,
    model_name VARCHAR(100) NOT NULL,
    description TEXT,
    target_entity VARCHAR(50) NOT NULL,
    prediction_type VARCHAR(50) NOT NULL,
    algorithm VARCHAR(100) NOT NULL,
    parameters JSONB,
    training_dataset_query TEXT,
    performance_metrics JSONB,
    last_trained_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_prediction_model_target_entity ON reporting.prediction_model(target_entity);
CREATE INDEX ix_prediction_model_prediction_type ON reporting.prediction_model(prediction_type);
CREATE INDEX ix_prediction_model_is_active ON reporting.prediction_model(is_active);

-- 予測結果テーブル
CREATE TABLE reporting.prediction_result (
    id BIGSERIAL PRIMARY KEY,
    model_id INT NOT NULL REFERENCES reporting.prediction_model(id),
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    prediction_date TIMESTAMP NOT NULL,
    prediction_value NUMERIC(18,2),
    prediction_label VARCHAR(255),
    confidence NUMERIC(5,2),
    feature_importance JSONB,
    explanation TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_prediction_result_model_id ON reporting.prediction_result(model_id);
CREATE INDEX ix_prediction_result_entity ON reporting.prediction_result(entity_type, entity_id);
CREATE INDEX ix_prediction_result_prediction_date ON reporting.prediction_result(prediction_date);

-- データセット定義テーブル
CREATE TABLE reporting.dataset_definition (
    id SERIAL PRIMARY KEY,
    dataset_code VARCHAR(50) NOT NULL UNIQUE,
    dataset_name VARCHAR(100) NOT NULL,
    description TEXT,
    query_definition TEXT NOT NULL,
    refresh_schedule VARCHAR(100),
    last_refreshed_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_dataset_definition_is_active ON reporting.dataset_definition(is_active);
CREATE INDEX ix_dataset_definition_last_refreshed_at ON reporting.dataset_definition(last_refreshed_at);

-- 初期データ: KPI定義
INSERT INTO reporting.kpi_definition (kpi_code, kpi_name, category, description, calculation_method, data_source, unit, display_format, target_value) VALUES
    ('UTILIZATION_RATE', '稼働率', 'ENGINEER', '技術者の稼働率', '(稼働中の技術者数 / 全技術者数) * 100', 'engineer.engineer_availability', '%', 'PERCENTAGE', 90),
    ('BILLABLE_RATE', '請求可能率', 'FINANCIAL', '請求可能な稼働の割合', '(請求可能な稼働時間 / 総稼働時間) * 100', 'timesheet.work_hour', '%', 'PERCENTAGE', 95),
    ('AVG_HOURLY_RATE', '平均時給', 'FINANCIAL', '技術者の平均時給', 'SUM(契約時給) / COUNT(技術者)', 'contract.contract', 'JPY', 'CURRENCY', null),
    ('REVENUE_MONTHLY', '月間売上', 'FINANCIAL', '月間の総売上', 'SUM(請求額)', 'billing.invoice', 'JPY', 'CURRENCY', null),
    ('PROFIT_MARGIN', '粗利率', 'FINANCIAL', '売上に対する粗利の割合', '(売上 - 原価) / 売上 * 100', 'billing.invoice, billing.payment', '%', 'PERCENTAGE', 30),
    ('PROJECT_SUCCESS_RATE', '案件成約率', 'SALES', '提案から成約に至った案件の割合', '(成約案件数 / 提案案件数) * 100', 'matching.proposal', '%', 'PERCENTAGE', 40),
    ('AVG_PROJECT_DURATION', '平均案件期間', 'PROJECT', '案件の平均期間', 'AVG(案件終了日 - 案件開始日)', 'project.project', 'DAYS', 'NUMBER', null),
    ('ENGINEER_RETENTION_RATE', '技術者定着率', 'ENGINEER', '12ヶ月以上在籍している技術者の割合', '(12ヶ月以上在籍技術者数 / 全技術者数) * 100', 'engineer.engineer', '%', 'PERCENTAGE', 85);

-- 初期データ: ダッシュボード
INSERT INTO reporting.dashboard (dashboard_code, dashboard_name, description, layout, is_system, is_public) VALUES
    ('SYSTEM_MAIN', 'メインダッシュボード', 'システム全体の主要KPIを表示するダッシュボード', 
     '{"rows": 3, "columns": 4}', true, true),
    ('FINANCIAL_DASHBOARD', '財務ダッシュボード', '売上・利益に関する財務情報を表示するダッシュボード', 
     '{"rows": 3, "columns": 4}', true, false),
    ('ENGINEER_DASHBOARD', '技術者ダッシュボード', '技術者の稼働状況やスキル分布を表示するダッシュボード', 
     '{"rows": 3, "columns": 4}', true, false),
    ('PROJECT_DASHBOARD', '案件ダッシュボード', '案件の状況や進捗を表示するダッシュボード', 
     '{"rows": 3, "columns": 4}', true, false);

-- 初期データ: レポート定義
INSERT INTO reporting.report_definition (report_code, report_name, description, category, report_type, query_definition, output_formats, is_system, is_active) VALUES
    ('MONTHLY_SALES', '月次売上レポート', '月単位での売上集計レポート', 'FINANCIAL', 'REGULAR', 
     'SELECT year, month, SUM(total_amount) as revenue FROM billing.invoice GROUP BY year, month ORDER BY year, month', 
     'PDF,EXCEL,CSV', true, true),
    ('ENGINEER_UTILIZATION', '技術者稼働状況レポート', '技術者の稼働状況の詳細レポート', 'ENGINEER', 'REGULAR', 
     'SELECT e.last_name, e.first_name, ea.availability_type, COUNT(*) as days FROM engineer.engineer e JOIN engineer.engineer_availability ea ON e.id = ea.engineer_id GROUP BY e.last_name, e.first_name, ea.availability_type', 
     'PDF,EXCEL,CSV', true, true),
    ('PROJECT_STATUS', '案件状況レポート', '案件の進捗状況レポート', 'PROJECT', 'REGULAR', 
     'SELECT p.project_name, ps.status_name, COUNT(*) as count FROM project.project p JOIN project.project_status ps ON p.status_id = ps.id GROUP BY p.project_name, ps.status_name', 
     'PDF,EXCEL,CSV', true, true),
    ('SKILL_DISTRIBUTION', 'スキル分布レポート', '技術者のスキル分布レポート', 'ENGINEER', 'REGULAR', 
     'SELECT sd.skill_name, es.skill_level, COUNT(*) as count FROM engineer.engineer_skill es JOIN engineer.skill_definition sd ON es.skill_definition_id = sd.id GROUP BY sd.skill_name, es.skill_level ORDER BY count DESC', 
     'PDF,EXCEL,CSV', true, true);

-- 初期データ: 予測分析モデル
INSERT INTO reporting.prediction_model (model_code, model_name, description, target_entity, prediction_type, algorithm, parameters, is_active) VALUES
    ('ENGINEER_CHURN', '技術者離職予測', '技術者の離職リスクを予測するモデル', 'ENGINEER', 'CLASSIFICATION', 'RANDOM_FOREST', 
     '{"n_estimators": 100, "max_depth": 10}', true),
    ('REVENUE_FORECAST', '売上予測', '月次売上を予測するモデル', 'FINANCIAL', 'REGRESSION', 'LINEAR_REGRESSION', 
     '{"normalize": true}', true),
    ('PROJECT_MATCH', '案件マッチング予測', '技術者と案件のマッチング成功確率を予測するモデル', 'MATCHING', 'CLASSIFICATION', 'GRADIENT_BOOSTING', 
     '{"learning_rate": 0.1, "n_estimators": 200}', true);