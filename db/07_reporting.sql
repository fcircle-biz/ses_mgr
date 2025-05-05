-- SES Management System PostgreSQL Schema
-- 07_reporting.sql: レポート関連テーブル
-- Created: 2025-05-06

-- ============================
-- REPORTING SYSTEM
-- ============================

CREATE TABLE IF NOT EXISTS dashboards (
    dashboard_id SERIAL PRIMARY KEY,
    dashboard_name VARCHAR(100) NOT NULL,
    dashboard_code VARCHAR(50) UNIQUE,
    user_id UUID REFERENCES users(user_id),
    dashboard_type TEXT NOT NULL CHECK (dashboard_type IN ('個人', '部門', '全社', 'その他')) DEFAULT '個人',
    layout_config JSONB DEFAULT '{}',
    refresh_rate INTEGER DEFAULT 0,  -- 0: 手動更新, その他: 自動更新間隔（分）
    description TEXT,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    url_path VARCHAR(100),
    tags VARCHAR[],
    theme VARCHAR(50) DEFAULT 'default',
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_dashboards_user_id ON dashboards(user_id);
CREATE INDEX IF NOT EXISTS idx_dashboards_dashboard_type ON dashboards(dashboard_type);
CREATE INDEX IF NOT EXISTS idx_dashboards_is_public ON dashboards(is_public);
CREATE INDEX IF NOT EXISTS idx_dashboards_dashboard_code ON dashboards(dashboard_code);
CREATE INDEX IF NOT EXISTS idx_dashboards_tags ON dashboards USING GIN(tags);

CREATE TRIGGER update_dashboards_timestamp
BEFORE UPDATE ON dashboards
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS widgets (
    widget_id SERIAL PRIMARY KEY,
    dashboard_id INTEGER NOT NULL REFERENCES dashboards(dashboard_id),
    widget_name VARCHAR(100) NOT NULL,
    widget_type TEXT NOT NULL CHECK (widget_type IN ('グラフ', '表', 'カード', 'フィルター', 'テキスト', 'その他')) DEFAULT 'グラフ',
    visualization_type TEXT NOT NULL CHECK (visualization_type IN ('折れ線', '棒', '円', '散布図', 'ヒートマップ', 'テーブル', 'ピボット', 'カウンター', 'リッチテキスト', 'リンク集', 'その他')) DEFAULT '折れ線',
    data_source TEXT NOT NULL CHECK (data_source IN ('SQL', 'API', 'CSV', 'メトリクス', 'カスタム', 'その他')) DEFAULT 'SQL',
    query_config JSONB DEFAULT '{}',
    filter_config JSONB DEFAULT '{}',
    display_config JSONB DEFAULT '{}',
    position_config JSONB DEFAULT '{}',
    refresh_interval INTEGER DEFAULT 0 CHECK (refresh_interval >= 0),
    is_visible BOOLEAN DEFAULT TRUE,
    custom_css TEXT,
    description TEXT,
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_widgets_dashboard_id ON widgets(dashboard_id);
CREATE INDEX IF NOT EXISTS idx_widgets_widget_type ON widgets(widget_type);
CREATE INDEX IF NOT EXISTS idx_widgets_data_source ON widgets(data_source);
CREATE INDEX IF NOT EXISTS idx_widgets_is_visible ON widgets(is_visible);

CREATE TRIGGER update_widgets_timestamp
BEFORE UPDATE ON widgets
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS kpi_definitions (
    kpi_id SERIAL PRIMARY KEY,
    kpi_code VARCHAR(50) NOT NULL UNIQUE,
    kpi_name VARCHAR(100) NOT NULL,
    kpi_category TEXT NOT NULL CHECK (kpi_category IN ('売上', '利益', '顧客', '技術者', '案件', '稼働率', 'その他')) DEFAULT '売上',
    description TEXT,
    calculation_formula TEXT,
    unit VARCHAR(20),
    data_source TEXT,
    data_query TEXT,
    frequency TEXT NOT NULL CHECK (frequency IN ('日次', '週次', '月次', '四半期', '年次')) DEFAULT '月次',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    target_direction TEXT NOT NULL CHECK (target_direction IN ('上昇', '下降', '範囲内', '一定')) DEFAULT '上昇',
    display_precision INTEGER DEFAULT 2,
    display_format VARCHAR(50),
    responsible_department INTEGER REFERENCES departments(department_id),
    tags VARCHAR[],
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_kpi_definitions_code ON kpi_definitions(kpi_code);
CREATE INDEX IF NOT EXISTS idx_kpi_definitions_category ON kpi_definitions(kpi_category);
CREATE INDEX IF NOT EXISTS idx_kpi_definitions_frequency ON kpi_definitions(frequency);
CREATE INDEX IF NOT EXISTS idx_kpi_definitions_is_active ON kpi_definitions(is_active);
CREATE INDEX IF NOT EXISTS idx_kpi_definitions_tags ON kpi_definitions USING GIN(tags);

CREATE TRIGGER update_kpi_definitions_timestamp
BEFORE UPDATE ON kpi_definitions
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS kpi_targets (
    target_id SERIAL PRIMARY KEY,
    kpi_id INTEGER NOT NULL REFERENCES kpi_definitions(kpi_id),
    target_year INTEGER NOT NULL CHECK (target_year >= 2000 AND target_year <= 2100),
    target_month INTEGER CHECK (target_month IS NULL OR (target_month >= 1 AND target_month <= 12)),
    target_quarter INTEGER CHECK (target_quarter IS NULL OR (target_quarter >= 1 AND target_quarter <= 4)),
    target_value NUMERIC(15,2) NOT NULL DEFAULT 0,
    minimum_value NUMERIC(15,2),
    maximum_value NUMERIC(15,2),
    target_type TEXT CHECK (target_type IN ('全社', '部門', '個人', 'その他')) DEFAULT '全社',
    target_department_id INTEGER REFERENCES departments(department_id),
    target_user_id UUID REFERENCES users(user_id),
    notes TEXT,
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK ((target_month IS NOT NULL AND target_quarter IS NULL) OR 
           (target_month IS NULL AND target_quarter IS NOT NULL) OR 
           (target_month IS NULL AND target_quarter IS NULL))
);

CREATE INDEX IF NOT EXISTS idx_kpi_targets_kpi_id ON kpi_targets(kpi_id);
CREATE INDEX IF NOT EXISTS idx_kpi_targets_target_year ON kpi_targets(target_year);
CREATE INDEX IF NOT EXISTS idx_kpi_targets_target_month ON kpi_targets(target_month);
CREATE INDEX IF NOT EXISTS idx_kpi_targets_target_quarter ON kpi_targets(target_quarter);
CREATE INDEX IF NOT EXISTS idx_kpi_targets_target_department_id ON kpi_targets(target_department_id);
CREATE INDEX IF NOT EXISTS idx_kpi_targets_target_user_id ON kpi_targets(target_user_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_kpi_targets_unique_period ON kpi_targets(kpi_id, target_year, COALESCE(target_month, 0), COALESCE(target_quarter, 0), COALESCE(target_department_id, 0), COALESCE(target_user_id::text, ''));

CREATE TRIGGER update_kpi_targets_timestamp
BEFORE UPDATE ON kpi_targets
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS kpi_achievements (
    achievement_id SERIAL PRIMARY KEY,
    kpi_id INTEGER NOT NULL REFERENCES kpi_definitions(kpi_id),
    target_id INTEGER REFERENCES kpi_targets(target_id),
    achievement_year INTEGER NOT NULL CHECK (achievement_year >= 2000 AND achievement_year <= 2100),
    achievement_month INTEGER CHECK (achievement_month IS NULL OR (achievement_month >= 1 AND achievement_month <= 12)),
    achievement_quarter INTEGER CHECK (achievement_quarter IS NULL OR (achievement_quarter >= 1 AND achievement_quarter <= 4)),
    achievement_date DATE,
    achievement_value NUMERIC(15,2) NOT NULL DEFAULT 0,
    achievement_percentage NUMERIC(6,2),
    year_on_year_percentage NUMERIC(6,2),
    month_on_month_percentage NUMERIC(6,2),
    achievement_type TEXT CHECK (achievement_type IN ('全社', '部門', '個人', 'その他')) DEFAULT '全社',
    target_department_id INTEGER REFERENCES departments(department_id),
    target_user_id UUID REFERENCES users(user_id),
    notes TEXT,
    data_source TEXT DEFAULT 'システム',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK ((achievement_month IS NOT NULL AND achievement_quarter IS NULL) OR 
           (achievement_month IS NULL AND achievement_quarter IS NOT NULL) OR 
           (achievement_month IS NULL AND achievement_quarter IS NULL))
);

CREATE INDEX IF NOT EXISTS idx_kpi_achievements_kpi_id ON kpi_achievements(kpi_id);
CREATE INDEX IF NOT EXISTS idx_kpi_achievements_target_id ON kpi_achievements(target_id);
CREATE INDEX IF NOT EXISTS idx_kpi_achievements_achievement_year ON kpi_achievements(achievement_year);
CREATE INDEX IF NOT EXISTS idx_kpi_achievements_achievement_month ON kpi_achievements(achievement_month);
CREATE INDEX IF NOT EXISTS idx_kpi_achievements_achievement_quarter ON kpi_achievements(achievement_quarter);
CREATE INDEX IF NOT EXISTS idx_kpi_achievements_achievement_date ON kpi_achievements(achievement_date);
CREATE INDEX IF NOT EXISTS idx_kpi_achievements_target_department_id ON kpi_achievements(target_department_id);
CREATE INDEX IF NOT EXISTS idx_kpi_achievements_target_user_id ON kpi_achievements(target_user_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_kpi_achievements_unique_period ON kpi_achievements(kpi_id, achievement_year, COALESCE(achievement_month, 0), COALESCE(achievement_quarter, 0), COALESCE(target_department_id, 0), COALESCE(target_user_id::text, ''));

CREATE TRIGGER update_kpi_achievements_timestamp
BEFORE UPDATE ON kpi_achievements
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS reports (
    report_id SERIAL PRIMARY KEY,
    report_name VARCHAR(100) NOT NULL,
    report_code VARCHAR(50) UNIQUE,
    report_type TEXT CHECK (report_type IN ('売上', '稼働', '損益', '予測', 'パフォーマンス', 'プロジェクト', 'その他')) DEFAULT 'その他',
    description TEXT,
    template_path VARCHAR(255),
    parameters JSONB DEFAULT '{}',
    query_config JSONB DEFAULT '{}',
    access_roles TEXT[] DEFAULT '{}',
    schedule_config JSONB DEFAULT '{}',
    last_generated_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reports_report_type ON reports(report_type);
CREATE INDEX IF NOT EXISTS idx_reports_is_active ON reports(is_active);
CREATE INDEX IF NOT EXISTS idx_reports_report_code ON reports(report_code);
CREATE INDEX IF NOT EXISTS idx_reports_access_roles ON reports USING GIN(access_roles);

CREATE TRIGGER update_reports_timestamp
BEFORE UPDATE ON reports
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS report_executions (
    execution_id SERIAL PRIMARY KEY,
    report_id INTEGER NOT NULL REFERENCES reports(report_id),
    parameters JSONB DEFAULT '{}',
    execution_status TEXT CHECK (execution_status IN ('待機中', '実行中', '完了', 'エラー', 'キャンセル')) DEFAULT '待機中',
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration_seconds INTEGER,
    output_file_path VARCHAR(255),
    output_file_format VARCHAR(20),
    error_message TEXT,
    requested_by UUID REFERENCES users(user_id),
    execution_type TEXT CHECK (execution_type IN ('手動', 'スケジュール', 'API')) DEFAULT '手動',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_report_executions_report_id ON report_executions(report_id);
CREATE INDEX IF NOT EXISTS idx_report_executions_execution_status ON report_executions(execution_status);
CREATE INDEX IF NOT EXISTS idx_report_executions_requested_by ON report_executions(requested_by);
CREATE INDEX IF NOT EXISTS idx_report_executions_start_time ON report_executions(start_time);

CREATE TRIGGER update_report_executions_timestamp
BEFORE UPDATE ON report_executions
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TABLE dashboards IS 'ダッシュボード設定情報を管理するテーブル';
COMMENT ON TABLE widgets IS 'ダッシュボード内のウィジェット情報を管理するテーブル';
COMMENT ON TABLE kpi_definitions IS 'KPI指標の定義情報を管理するテーブル';
COMMENT ON TABLE kpi_targets IS 'KPI指標の目標値を管理するテーブル';
COMMENT ON TABLE kpi_achievements IS 'KPI指標の実績値を管理するテーブル';
COMMENT ON TABLE reports IS 'レポート定義情報を管理するテーブル';
COMMENT ON TABLE report_executions IS 'レポート実行履歴を管理するテーブル';