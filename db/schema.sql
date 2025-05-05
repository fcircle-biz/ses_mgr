-- SES Management System PostgreSQL Schema
-- Created: 2025-05-05

-- Enable UUID extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable pgcrypto for password hashing
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Update timestamp trigger function
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop tables if they exist (for clean reinstallation)
DO $$ 
DECLARE
    tables TEXT[] := ARRAY[
        'kpi_achievements', 'kpi_targets', 'kpi_definitions', 'widgets', 'dashboards',
        'approval_steps', 'approval_flows', 'work_hours', 'daily_attendances', 'monthly_attendances',
        'interview_results', 'interview_schedules', 'skill_matching_scores', 'matching_history', 'matching',
        'project_documents', 'project_requirements', 'projects', 'work_experiences', 'certifications',
        'skill_sheets', 'engineer_skills', 'skills', 'engineers', 'customers', 'companies',
        'user_role_history', 'user_roles', 'role_permissions', 'permissions', 'roles', 'users',
        'departments', 'holidays', 'attendance_notifications', 'matching_alerts'
    ];
    t TEXT;
BEGIN
    FOREACH t IN ARRAY tables
    LOOP
        EXECUTE format('DROP TABLE IF EXISTS %I CASCADE', t);
    END LOOP;
END $$;

-- ============================
-- ORGANIZATION STRUCTURE
-- ============================

CREATE TABLE departments (
    department_id SERIAL PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL,
    department_code VARCHAR(50) UNIQUE,
    parent_department_id INTEGER REFERENCES departments(department_id),
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_departments_parent ON departments(parent_department_id);
CREATE INDEX idx_departments_is_active ON departments(is_active);

CREATE TRIGGER update_departments_timestamp
BEFORE UPDATE ON departments
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- ============================
-- USER MANAGEMENT
-- ============================

CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    login_id VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    department_id INTEGER REFERENCES departments(department_id),
    position VARCHAR(255),
    phone VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    mfa_enabled BOOLEAN DEFAULT FALSE,
    status TEXT CHECK (status IN ('active', 'inactive', 'locked')) DEFAULT 'active',
    last_login_at TIMESTAMP,
    login_attempts INTEGER DEFAULT 0,
    password_expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_department_id ON users(department_id);
CREATE INDEX idx_users_status ON users(status);

CREATE TRIGGER update_users_timestamp
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE roles (
    role_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    role_code VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    role_type TEXT CHECK (role_type IN ('system', 'business')) DEFAULT 'business',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_roles_type ON roles(role_type);

CREATE TRIGGER update_roles_timestamp
BEFORE UPDATE ON roles
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE permissions (
    permission_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    permission_code VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    resource_type VARCHAR(255) NOT NULL,
    resource_name VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_permissions_resource ON permissions(resource_type, resource_name);
CREATE INDEX idx_permissions_action ON permissions(action);

CREATE TRIGGER update_permissions_timestamp
BEFORE UPDATE ON permissions
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE role_permissions (
    role_id UUID REFERENCES roles(role_id),
    permission_id UUID REFERENCES permissions(permission_id),
    access_level TEXT CHECK (access_level IN ('none', 'read', 'write')) DEFAULT 'none',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TRIGGER update_role_permissions_timestamp
BEFORE UPDATE ON role_permissions
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE user_roles (
    user_id UUID REFERENCES users(user_id),
    role_id UUID REFERENCES roles(role_id),
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by UUID REFERENCES users(user_id),
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_expires_at ON user_roles(expires_at);

CREATE TRIGGER update_user_roles_timestamp
BEFORE UPDATE ON user_roles
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE user_role_history (
    history_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(user_id),
    role_id UUID NOT NULL REFERENCES roles(role_id),
    operation_type TEXT CHECK (operation_type IN ('assign', 'remove')),
    performed_by UUID NOT NULL REFERENCES users(user_id),
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_role_history_user_id ON user_role_history(user_id);
CREATE INDEX idx_user_role_history_performed_at ON user_role_history(performed_at);

-- ============================
-- COMPANY MANAGEMENT
-- ============================

CREATE TABLE companies (
    company_id SERIAL PRIMARY KEY,
    company_name VARCHAR(100) NOT NULL,
    company_type TEXT NOT NULL CHECK (company_type IN ('自社', 'パートナー企業', '顧客')) DEFAULT '自社',
    address VARCHAR(200),
    phone_number VARCHAR(20),
    email VARCHAR(100),
    website VARCHAR(200),
    contract_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_companies_name ON companies(company_name);
CREATE INDEX idx_companies_type ON companies(company_type);

CREATE TRIGGER update_companies_timestamp
BEFORE UPDATE ON companies
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE customers (
    customer_id SERIAL PRIMARY KEY,
    company_id INTEGER REFERENCES companies(company_id),
    customer_name VARCHAR(100) NOT NULL,
    address VARCHAR(200),
    phone_number VARCHAR(20),
    email VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customer_name ON customers(customer_name);
CREATE INDEX idx_customer_company_id ON customers(company_id);

CREATE TRIGGER update_customers_timestamp
BEFORE UPDATE ON customers
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- ============================
-- ENGINEER MANAGEMENT
-- ============================

CREATE TABLE engineers (
    engineer_id SERIAL PRIMARY KEY,
    company_id INTEGER NOT NULL REFERENCES companies(company_id),
    engineer_name VARCHAR(100) NOT NULL,
    furigana VARCHAR(100),
    email VARCHAR(100),
    phone_number VARCHAR(20),
    birth_date DATE,
    employment_type TEXT NOT NULL CHECK (employment_type IN ('正社員', '契約社員', 'パートナー', 'フリーランス')) DEFAULT '正社員',
    join_date DATE,
    years_of_experience INTEGER DEFAULT 0,
    preferred_work_location TEXT,
    status TEXT NOT NULL CHECK (status IN ('稼働可能', 'アサイン中', '研修中', '休職中', '退職')) DEFAULT '稼働可能',
    availability_date DATE,
    preferred_unit_price NUMERIC(10,2),
    profile_text TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_engineers_company_id ON engineers(company_id);
CREATE INDEX idx_engineers_status ON engineers(status);
CREATE INDEX idx_engineers_availability_date ON engineers(availability_date);
CREATE INDEX idx_engineers_preferred_unit_price ON engineers(preferred_unit_price);

CREATE TRIGGER update_engineers_timestamp
BEFORE UPDATE ON engineers
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE skills (
    skill_id SERIAL PRIMARY KEY,
    skill_name VARCHAR(100) NOT NULL,
    skill_category TEXT NOT NULL CHECK (skill_category IN ('言語', 'フレームワーク', 'DB', 'OS', 'ミドルウェア', 'クラウド', 'ツール', '資格', 'その他')),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_skills_name ON skills(skill_name);
CREATE INDEX idx_skills_category ON skills(skill_category);

CREATE TRIGGER update_skills_timestamp
BEFORE UPDATE ON skills
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE engineer_skills (
    engineer_skill_id SERIAL PRIMARY KEY,
    engineer_id INTEGER NOT NULL REFERENCES engineers(engineer_id),
    skill_id INTEGER NOT NULL REFERENCES skills(skill_id),
    proficiency_level INTEGER NOT NULL DEFAULT 1 CHECK (proficiency_level BETWEEN 1 AND 5),
    years_of_experience NUMERIC(3,1) DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (engineer_id, skill_id)
);

CREATE INDEX idx_engineer_skills_engineer_id ON engineer_skills(engineer_id);
CREATE INDEX idx_engineer_skills_skill_id ON engineer_skills(skill_id);
CREATE INDEX idx_engineer_skills_proficiency ON engineer_skills(proficiency_level);

CREATE TRIGGER update_engineer_skills_timestamp
BEFORE UPDATE ON engineer_skills
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE skill_sheets (
    sheet_id SERIAL PRIMARY KEY,
    engineer_id INTEGER NOT NULL REFERENCES engineers(engineer_id),
    sheet_name VARCHAR(100) NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    file_path VARCHAR(255),
    file_format TEXT NOT NULL CHECK (file_format IN ('PDF', 'DOCX', 'HTML')) DEFAULT 'PDF',
    is_current BOOLEAN NOT NULL DEFAULT TRUE,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_skill_sheets_engineer_id ON skill_sheets(engineer_id);
CREATE INDEX idx_skill_sheets_is_current ON skill_sheets(is_current);

CREATE TRIGGER update_skill_sheets_timestamp
BEFORE UPDATE ON skill_sheets
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE certifications (
    certification_id SERIAL PRIMARY KEY,
    engineer_id INTEGER NOT NULL REFERENCES engineers(engineer_id),
    certification_name VARCHAR(100) NOT NULL,
    issuing_organization VARCHAR(100),
    acquisition_date DATE,
    expiration_date DATE,
    certification_number VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_certifications_engineer_id ON certifications(engineer_id);

CREATE TRIGGER update_certifications_timestamp
BEFORE UPDATE ON certifications
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE work_experiences (
    experience_id SERIAL PRIMARY KEY,
    engineer_id INTEGER NOT NULL REFERENCES engineers(engineer_id),
    project_name VARCHAR(100) NOT NULL,
    client_name VARCHAR(100),
    start_date DATE NOT NULL,
    end_date DATE,
    role VARCHAR(100),
    description TEXT,
    technologies_used TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_work_experiences_engineer_id ON work_experiences(engineer_id);
CREATE INDEX idx_work_experiences_start_date ON work_experiences(start_date);

CREATE TRIGGER update_work_experiences_timestamp
BEFORE UPDATE ON work_experiences
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- ============================
-- PROJECT MANAGEMENT
-- ============================

CREATE TABLE projects (
    project_id SERIAL PRIMARY KEY,
    customer_id INTEGER NOT NULL REFERENCES customers(customer_id),
    project_name VARCHAR(100) NOT NULL,
    project_overview TEXT,
    phase TEXT NOT NULL CHECK (phase IN ('リード', '提案', '交渉', '受注')) DEFAULT 'リード',
    required_personnel INTEGER,
    start_date DATE,
    end_date DATE,
    distribution_type TEXT NOT NULL CHECK (distribution_type IN ('直請', '一次', '二次')) DEFAULT '直請',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_project_name ON projects(project_name);
CREATE INDEX idx_phase ON projects(phase);
CREATE INDEX idx_start_date ON projects(start_date);
CREATE INDEX idx_project_customer_id ON projects(customer_id);

CREATE TRIGGER update_projects_timestamp
BEFORE UPDATE ON projects
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE project_requirements (
    requirement_id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(project_id),
    skill_requirement TEXT,
    years_of_experience INTEGER,
    preferred_unit_price NUMERIC(10,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_project_id ON project_requirements(project_id);

CREATE TRIGGER update_project_requirements_timestamp
BEFORE UPDATE ON project_requirements
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE project_documents (
    document_id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(project_id),
    document_name VARCHAR(100) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    document_type TEXT NOT NULL CHECK (document_type IN ('NDA', '提案書', '仕様書', 'その他')) DEFAULT 'その他',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_project_documents_project_id ON project_documents(project_id);
CREATE INDEX idx_document_type ON project_documents(document_type);

CREATE TRIGGER update_project_documents_timestamp
BEFORE UPDATE ON project_documents
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- ============================
-- MATCHING ENGINE
-- ============================

CREATE TABLE matching (
    matching_id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(project_id),
    engineer_id INTEGER NOT NULL REFERENCES engineers(engineer_id),
    matching_score INTEGER NOT NULL DEFAULT 0 CHECK (matching_score BETWEEN 0 AND 100),
    status TEXT NOT NULL CHECK (status IN ('推薦候補', '提案中', '面談調整中', '面談実施済', '内定', '契約締結', '見送り')) DEFAULT '推薦候補',
    proposed_unit_price NUMERIC(10,2),
    availability_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    priority INTEGER NOT NULL DEFAULT 0 CHECK (priority BETWEEN 0 AND 10),
    matching_notes TEXT,
    created_by UUID REFERENCES users(user_id),
    updated_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (project_id, engineer_id)
);

CREATE INDEX idx_matching_project_id ON matching(project_id);
CREATE INDEX idx_matching_engineer_id ON matching(engineer_id);
CREATE INDEX idx_matching_status ON matching(status);
CREATE INDEX idx_matching_score ON matching(matching_score);
CREATE INDEX idx_matching_priority ON matching(priority);
CREATE INDEX idx_matching_created_at ON matching(created_at);

CREATE TRIGGER update_matching_timestamp
BEFORE UPDATE ON matching
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE matching_history (
    history_id SERIAL PRIMARY KEY,
    matching_id INTEGER NOT NULL REFERENCES matching(matching_id),
    previous_status TEXT,
    new_status TEXT NOT NULL,
    action_type TEXT NOT NULL CHECK (action_type IN ('ステータス変更', '単価交渉', '面談設定', '面談結果登録', '内定通知', '見送り')) DEFAULT 'ステータス変更',
    description TEXT,
    performed_by UUID REFERENCES users(user_id),
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_matching_history_matching_id ON matching_history(matching_id);
CREATE INDEX idx_matching_history_performed_at ON matching_history(performed_at);
CREATE INDEX idx_matching_history_performed_by ON matching_history(performed_by);

CREATE TABLE skill_matching_scores (
    score_id SERIAL PRIMARY KEY,
    matching_id INTEGER NOT NULL REFERENCES matching(matching_id),
    requirement_id INTEGER REFERENCES project_requirements(requirement_id),
    skill_id INTEGER REFERENCES skills(skill_id),
    score INTEGER NOT NULL DEFAULT 0 CHECK (score BETWEEN 0 AND 100),
    weight NUMERIC(3,2) NOT NULL DEFAULT 1.00 CHECK (weight > 0),
    weighted_score NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_skill_matching_scores_matching_id ON skill_matching_scores(matching_id);
CREATE INDEX idx_skill_matching_scores_requirement_id ON skill_matching_scores(requirement_id);
CREATE INDEX idx_skill_matching_scores_skill_id ON skill_matching_scores(skill_id);

CREATE TRIGGER update_skill_matching_scores_timestamp
BEFORE UPDATE ON skill_matching_scores
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE interview_schedules (
    interview_id SERIAL PRIMARY KEY,
    matching_id INTEGER NOT NULL REFERENCES matching(matching_id),
    interview_date TIMESTAMP NOT NULL,
    interview_type TEXT NOT NULL CHECK (interview_type IN ('オンライン', '訪問', '電話', '社内')) DEFAULT 'オンライン',
    location TEXT,
    status TEXT NOT NULL CHECK (status IN ('予定', '実施済', 'キャンセル', '延期')) DEFAULT '予定',
    interviewer_names TEXT,
    notes TEXT,
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_interview_schedules_matching_id ON interview_schedules(matching_id);
CREATE INDEX idx_interview_schedules_interview_date ON interview_schedules(interview_date);
CREATE INDEX idx_interview_schedules_status ON interview_schedules(status);

CREATE TRIGGER update_interview_schedules_timestamp
BEFORE UPDATE ON interview_schedules
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE interview_results (
    result_id SERIAL PRIMARY KEY,
    interview_id INTEGER NOT NULL REFERENCES interview_schedules(interview_id),
    technical_evaluation INTEGER CHECK (technical_evaluation BETWEEN 1 AND 5),
    communication_evaluation INTEGER CHECK (communication_evaluation BETWEEN 1 AND 5),
    overall_evaluation INTEGER CHECK (overall_evaluation BETWEEN 1 AND 5),
    client_feedback TEXT,
    internal_feedback TEXT,
    decision TEXT NOT NULL CHECK (decision IN ('検討中', '採用', '不採用', '保留')) DEFAULT '検討中',
    decision_reason TEXT,
    recorded_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_interview_results_interview_id ON interview_results(interview_id);
CREATE INDEX idx_interview_results_decision ON interview_results(decision);

CREATE TRIGGER update_interview_results_timestamp
BEFORE UPDATE ON interview_results
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- ============================
-- ATTENDANCE AND TIME MANAGEMENT
-- ============================

CREATE TABLE monthly_attendances (
    monthly_attendance_id SERIAL PRIMARY KEY,
    engineer_id INTEGER NOT NULL REFERENCES engineers(engineer_id),
    project_id INTEGER REFERENCES projects(project_id),
    target_year INTEGER NOT NULL CHECK (target_year >= 2000 AND target_year <= 2100),
    target_month INTEGER NOT NULL CHECK (target_month >= 1 AND target_month <= 12),
    contracted_working_days INTEGER NOT NULL DEFAULT 0,
    working_days INTEGER NOT NULL DEFAULT 0,
    absence_days INTEGER NOT NULL DEFAULT 0,
    paid_leave_days NUMERIC(3,1) NOT NULL DEFAULT 0,
    total_working_hours NUMERIC(5,2) NOT NULL DEFAULT 0,
    overtime_hours NUMERIC(5,2) NOT NULL DEFAULT 0,
    night_work_hours NUMERIC(5,2) NOT NULL DEFAULT 0,
    holiday_work_hours NUMERIC(5,2) NOT NULL DEFAULT 0,
    status TEXT NOT NULL CHECK (status IN ('作成中', '提出済', '承認済', '差戻', '確定')) DEFAULT '作成中',
    remarks TEXT,
    submitted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (engineer_id, project_id, target_year, target_month)
);

CREATE INDEX idx_monthly_attendances_engineer_id ON monthly_attendances(engineer_id);
CREATE INDEX idx_monthly_attendances_project_id ON monthly_attendances(project_id);
CREATE INDEX idx_monthly_attendances_year_month ON monthly_attendances(target_year, target_month);
CREATE INDEX idx_monthly_attendances_status ON monthly_attendances(status);

CREATE TRIGGER update_monthly_attendances_timestamp
BEFORE UPDATE ON monthly_attendances
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE daily_attendances (
    daily_attendance_id SERIAL PRIMARY KEY,
    monthly_attendance_id INTEGER NOT NULL REFERENCES monthly_attendances(monthly_attendance_id),
    attendance_date DATE NOT NULL,
    day_type TEXT NOT NULL CHECK (day_type IN ('平日', '休日', '祝日')) DEFAULT '平日',
    start_time TIME,
    end_time TIME,
    break_time NUMERIC(3,1) NOT NULL DEFAULT 1.0,
    working_hours NUMERIC(4,2) NOT NULL DEFAULT 0,
    overtime_hours NUMERIC(4,2) NOT NULL DEFAULT 0,
    night_work_hours NUMERIC(4,2) NOT NULL DEFAULT 0,
    attendance_type TEXT NOT NULL CHECK (attendance_type IN ('出勤', '欠勤', '有給休暇', '半休', '特別休暇', 'リモート勤務')) DEFAULT '出勤',
    location_type TEXT NOT NULL CHECK (location_type IN ('客先', '社内', 'リモート', 'その他')) DEFAULT '客先',
    location_details TEXT,
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (monthly_attendance_id, attendance_date)
);

CREATE INDEX idx_daily_attendances_monthly_attendance_id ON daily_attendances(monthly_attendance_id);
CREATE INDEX idx_daily_attendances_attendance_date ON daily_attendances(attendance_date);
CREATE INDEX idx_daily_attendances_attendance_type ON daily_attendances(attendance_type);

CREATE TRIGGER update_daily_attendances_timestamp
BEFORE UPDATE ON daily_attendances
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE work_hours (
    work_hour_id SERIAL PRIMARY KEY,
    daily_attendance_id INTEGER NOT NULL REFERENCES daily_attendances(daily_attendance_id),
    project_id INTEGER NOT NULL REFERENCES projects(project_id),
    task_name VARCHAR(100) NOT NULL,
    task_category TEXT CHECK (task_category IN ('要件定義', '設計', '開発', 'テスト', 'レビュー', '会議', '調査', '保守', 'その他')) DEFAULT '開発',
    work_hours NUMERIC(4,2) NOT NULL DEFAULT 0 CHECK (work_hours >= 0),
    billable BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_work_hours_daily_attendance_id ON work_hours(daily_attendance_id);
CREATE INDEX idx_work_hours_project_id ON work_hours(project_id);
CREATE INDEX idx_work_hours_task_category ON work_hours(task_category);
CREATE INDEX idx_work_hours_billable ON work_hours(billable);

CREATE TRIGGER update_work_hours_timestamp
BEFORE UPDATE ON work_hours
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE approval_flows (
    flow_id SERIAL PRIMARY KEY,
    monthly_attendance_id INTEGER NOT NULL REFERENCES monthly_attendances(monthly_attendance_id),
    current_step INTEGER NOT NULL DEFAULT 1 CHECK (current_step >= 0),
    flow_type TEXT NOT NULL CHECK (flow_type IN ('標準', '緊急', '特別')) DEFAULT '標準',
    status TEXT NOT NULL CHECK (status IN ('申請中', '承認中', '差戻', '完了', 'キャンセル')) DEFAULT '申請中',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (monthly_attendance_id)
);

CREATE INDEX idx_approval_flows_monthly_attendance_id ON approval_flows(monthly_attendance_id);
CREATE INDEX idx_approval_flows_status ON approval_flows(status);

CREATE TRIGGER update_approval_flows_timestamp
BEFORE UPDATE ON approval_flows
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE approval_steps (
    step_id SERIAL PRIMARY KEY,
    flow_id INTEGER NOT NULL REFERENCES approval_flows(flow_id),
    step_number INTEGER NOT NULL CHECK (step_number >= 1),
    approver_id UUID NOT NULL REFERENCES users(user_id),
    approver_role TEXT NOT NULL CHECK (approver_role IN ('PM', '営業', '管理者', '経理', 'その他')) DEFAULT 'PM',
    status TEXT NOT NULL CHECK (status IN ('未対応', '承認', '差戻', '保留', 'スキップ')) DEFAULT '未対応',
    approved_at TIMESTAMP,
    comments TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (flow_id, step_number)
);

CREATE INDEX idx_approval_steps_flow_id ON approval_steps(flow_id);
CREATE INDEX idx_approval_steps_approver_id ON approval_steps(approver_id);
CREATE INDEX idx_approval_steps_status ON approval_steps(status);

CREATE TRIGGER update_approval_steps_timestamp
BEFORE UPDATE ON approval_steps
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- ============================
-- REPORTING SYSTEM
-- ============================

CREATE TABLE dashboards (
    dashboard_id SERIAL PRIMARY KEY,
    dashboard_name VARCHAR(100) NOT NULL,
    user_id UUID REFERENCES users(user_id),
    dashboard_type TEXT NOT NULL CHECK (dashboard_type IN ('個人', '部門', '全社', 'その他')) DEFAULT '個人',
    layout_config JSONB DEFAULT '{}',
    description TEXT,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dashboards_user_id ON dashboards(user_id);
CREATE INDEX idx_dashboards_dashboard_type ON dashboards(dashboard_type);
CREATE INDEX idx_dashboards_is_public ON dashboards(is_public);

CREATE TRIGGER update_dashboards_timestamp
BEFORE UPDATE ON dashboards
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE widgets (
    widget_id SERIAL PRIMARY KEY,
    dashboard_id INTEGER NOT NULL REFERENCES dashboards(dashboard_id),
    widget_name VARCHAR(100) NOT NULL,
    widget_type TEXT NOT NULL CHECK (widget_type IN ('グラフ', '表', 'カード', 'フィルター', 'テキスト', 'その他')) DEFAULT 'グラフ',
    visualization_type TEXT NOT NULL CHECK (visualization_type IN ('折れ線', '棒', '円', '散布図', 'ヒートマップ', 'テーブル', 'ピボット', 'カウンター', 'その他')) DEFAULT '折れ線',
    data_source TEXT NOT NULL CHECK (data_source IN ('SQL', 'API', 'CSV', 'メトリクス', 'その他')) DEFAULT 'SQL',
    query_config JSONB DEFAULT '{}',
    display_config JSONB DEFAULT '{}',
    position_config JSONB DEFAULT '{}',
    refresh_interval INTEGER DEFAULT 0 CHECK (refresh_interval >= 0),
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_widgets_dashboard_id ON widgets(dashboard_id);
CREATE INDEX idx_widgets_widget_type ON widgets(widget_type);
CREATE INDEX idx_widgets_data_source ON widgets(data_source);

CREATE TRIGGER update_widgets_timestamp
BEFORE UPDATE ON widgets
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE kpi_definitions (
    kpi_id SERIAL PRIMARY KEY,
    kpi_code VARCHAR(50) NOT NULL UNIQUE,
    kpi_name VARCHAR(100) NOT NULL,
    kpi_category TEXT NOT NULL CHECK (kpi_category IN ('売上', '利益', '顧客', '技術者', '案件', '稼働率', 'その他')) DEFAULT '売上',
    description TEXT,
    calculation_formula TEXT,
    unit VARCHAR(20),
    data_source TEXT,
    frequency TEXT NOT NULL CHECK (frequency IN ('日次', '週次', '月次', '四半期', '年次')) DEFAULT '月次',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    target_direction TEXT NOT NULL CHECK (target_direction IN ('上昇', '下降', '範囲内', '一定')) DEFAULT '上昇',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_kpi_definitions_code ON kpi_definitions(kpi_code);
CREATE INDEX idx_kpi_definitions_category ON kpi_definitions(kpi_category);
CREATE INDEX idx_kpi_definitions_frequency ON kpi_definitions(frequency);
CREATE INDEX idx_kpi_definitions_is_active ON kpi_definitions(is_active);

CREATE TRIGGER update_kpi_definitions_timestamp
BEFORE UPDATE ON kpi_definitions
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE kpi_targets (
    target_id SERIAL PRIMARY KEY,
    kpi_id INTEGER NOT NULL REFERENCES kpi_definitions(kpi_id),
    target_year INTEGER NOT NULL CHECK (target_year >= 2000 AND target_year <= 2100),
    target_month INTEGER CHECK (target_month IS NULL OR (target_month >= 1 AND target_month <= 12)),
    target_quarter INTEGER CHECK (target_quarter IS NULL OR (target_quarter >= 1 AND target_quarter <= 4)),
    target_value NUMERIC(15,2) NOT NULL DEFAULT 0,
    minimum_value NUMERIC(15,2),
    maximum_value NUMERIC(15,2),
    target_department_id INTEGER REFERENCES departments(department_id),
    notes TEXT,
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK ((target_month IS NOT NULL AND target_quarter IS NULL) OR 
           (target_month IS NULL AND target_quarter IS NOT NULL) OR 
           (target_month IS NULL AND target_quarter IS NULL))
);

CREATE INDEX idx_kpi_targets_kpi_id ON kpi_targets(kpi_id);
CREATE INDEX idx_kpi_targets_target_year ON kpi_targets(target_year);
CREATE INDEX idx_kpi_targets_target_month ON kpi_targets(target_month);
CREATE INDEX idx_kpi_targets_target_quarter ON kpi_targets(target_quarter);
CREATE INDEX idx_kpi_targets_target_department_id ON kpi_targets(target_department_id);
CREATE UNIQUE INDEX idx_kpi_targets_unique_period ON kpi_targets(kpi_id, target_year, COALESCE(target_month, 0), COALESCE(target_quarter, 0), COALESCE(target_department_id, 0));

CREATE TRIGGER update_kpi_targets_timestamp
BEFORE UPDATE ON kpi_targets
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE kpi_achievements (
    achievement_id SERIAL PRIMARY KEY,
    kpi_id INTEGER NOT NULL REFERENCES kpi_definitions(kpi_id),
    target_id INTEGER REFERENCES kpi_targets(target_id),
    achievement_year INTEGER NOT NULL CHECK (achievement_year >= 2000 AND achievement_year <= 2100),
    achievement_month INTEGER CHECK (achievement_month IS NULL OR (achievement_month >= 1 AND achievement_month <= 12)),
    achievement_quarter INTEGER CHECK (achievement_quarter IS NULL OR (achievement_quarter >= 1 AND achievement_quarter <= 4)),
    achievement_date DATE,
    achievement_value NUMERIC(15,2) NOT NULL DEFAULT 0,
    achievement_percentage NUMERIC(6,2),
    target_department_id INTEGER REFERENCES departments(department_id),
    notes TEXT,
    data_source TEXT DEFAULT 'システム',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK ((achievement_month IS NOT NULL AND achievement_quarter IS NULL) OR 
           (achievement_month IS NULL AND achievement_quarter IS NOT NULL) OR 
           (achievement_month IS NULL AND achievement_quarter IS NULL))
);

CREATE INDEX idx_kpi_achievements_kpi_id ON kpi_achievements(kpi_id);
CREATE INDEX idx_kpi_achievements_target_id ON kpi_achievements(target_id);
CREATE INDEX idx_kpi_achievements_achievement_year ON kpi_achievements(achievement_year);
CREATE INDEX idx_kpi_achievements_achievement_month ON kpi_achievements(achievement_month);
CREATE INDEX idx_kpi_achievements_achievement_quarter ON kpi_achievements(achievement_quarter);
CREATE INDEX idx_kpi_achievements_achievement_date ON kpi_achievements(achievement_date);
CREATE INDEX idx_kpi_achievements_target_department_id ON kpi_achievements(target_department_id);
CREATE UNIQUE INDEX idx_kpi_achievements_unique_period ON kpi_achievements(kpi_id, achievement_year, COALESCE(achievement_month, 0), COALESCE(achievement_quarter, 0), COALESCE(target_department_id, 0));

CREATE TRIGGER update_kpi_achievements_timestamp
BEFORE UPDATE ON kpi_achievements
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- ============================
-- NOTIFICATION SYSTEM
-- ============================

CREATE TABLE matching_alerts (
    alert_id SERIAL PRIMARY KEY,
    alert_type TEXT NOT NULL CHECK (alert_type IN ('新規マッチング', '状態変更', '面談設定', '面談結果', '単価交渉', '内定通知', 'その他')),
    subject VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    related_entity TEXT NOT NULL CHECK (related_entity IN ('matching', 'project', 'engineer', 'interview')),
    entity_id INTEGER NOT NULL,
    target_user_id UUID NOT NULL REFERENCES users(user_id),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    priority TEXT NOT NULL CHECK (priority IN ('低', '通常', '高', '緊急')) DEFAULT '通常',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_matching_alerts_target_user_id ON matching_alerts(target_user_id);
CREATE INDEX idx_matching_alerts_is_read ON matching_alerts(is_read);
CREATE INDEX idx_matching_alerts_created_at ON matching_alerts(created_at);
CREATE INDEX idx_matching_alerts_priority ON matching_alerts(priority);

CREATE TABLE attendance_notifications (
    notification_id SERIAL PRIMARY KEY,
    notification_type TEXT NOT NULL CHECK (notification_type IN ('勤怠提出リマインド', '勤怠承認依頼', '勤怠承認完了', '勤怠差戻', 'その他')),
    user_id UUID NOT NULL REFERENCES users(user_id),
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    related_entity TEXT NOT NULL CHECK (related_entity IN ('monthly_attendance', 'daily_attendance', 'approval', 'other')),
    entity_id INTEGER,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    notify_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attendance_notifications_user_id ON attendance_notifications(user_id);
CREATE INDEX idx_attendance_notifications_is_read ON attendance_notifications(is_read);
CREATE INDEX idx_attendance_notifications_notify_at ON attendance_notifications(notify_at);
CREATE INDEX idx_attendance_notifications_type ON attendance_notifications(notification_type);

-- ============================
-- UTILITY TABLES
-- ============================

CREATE TABLE holidays (
    holiday_id SERIAL PRIMARY KEY,
    holiday_date DATE NOT NULL,
    holiday_name VARCHAR(100) NOT NULL,
    holiday_type TEXT NOT NULL CHECK (holiday_type IN ('祝日', '会社休業日', 'その他')) DEFAULT '祝日',
    is_half_day BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (holiday_date, holiday_type)
);

CREATE INDEX idx_holidays_date ON holidays(holiday_date);
CREATE INDEX idx_holidays_type ON holidays(holiday_type);

CREATE TRIGGER update_holidays_timestamp
BEFORE UPDATE ON holidays
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- ============================
-- INITIAL DATA
-- ============================

-- Insert default admin user
INSERT INTO departments (department_name, department_code, description)
VALUES ('システム管理', 'ADMIN', 'システム管理部門');

INSERT INTO users (
    login_id, 
    email, 
    name, 
    department_id, 
    position, 
    password_hash
) VALUES (
    'admin', 
    'admin@example.com', 
    'システム管理者', 
    1, 
    'システム管理者', 
    crypt('password', gen_salt('bf'))
);

-- Insert base roles
INSERT INTO roles (role_code, name, description, role_type)
VALUES 
    ('ADMIN', 'システム管理者', 'システム全体の管理権限を持つロール', 'system'),
    ('MANAGER', '管理者', '業務機能の管理権限を持つロール', 'business'),
    ('USER', '一般ユーザー', '基本的な業務機能を利用するロール', 'business');

-- Assign admin role to admin user
INSERT INTO user_roles (user_id, role_id)
VALUES ((SELECT user_id FROM users WHERE login_id = 'admin'), (SELECT role_id FROM roles WHERE role_code = 'ADMIN'));

-- コンパニーの初期データ (自社)
INSERT INTO companies (company_name, company_type, address, phone_number, email, website)
VALUES ('自社', '自社', '東京都千代田区丸の内1-1-1', '03-1234-5678', 'info@example.com', 'https://example.com');

-- スキルの初期データ
INSERT INTO skills (skill_name, skill_category, description)
VALUES 
    ('Java', '言語', 'Javaプログラミング言語'),
    ('Spring Boot', 'フレームワーク', 'Javaベースのフレームワーク'),
    ('PostgreSQL', 'DB', 'オープンソースRDB'),
    ('Docker', 'ミドルウェア', 'コンテナプラットフォーム');

COMMENT ON TABLE users IS 'システムユーザー情報を管理するテーブル';
COMMENT ON TABLE roles IS '役割と権限のセットを管理するテーブル';
COMMENT ON TABLE permissions IS '各リソースに対する権限を管理するテーブル';
COMMENT ON TABLE role_permissions IS '役割と権限の関連付けを管理するテーブル';
COMMENT ON TABLE user_roles IS 'ユーザーと役割の関連付けを管理するテーブル';
COMMENT ON TABLE companies IS '自社、パートナー、顧客の企業情報を管理するテーブル';
COMMENT ON TABLE engineers IS 'エンジニア情報を管理するテーブル';
COMMENT ON TABLE skills IS 'スキル情報を管理するテーブル';
COMMENT ON TABLE engineer_skills IS 'エンジニアのスキル情報を管理するテーブル';
COMMENT ON TABLE projects IS '案件情報を管理するテーブル';
COMMENT ON TABLE matching IS 'エンジニアと案件のマッチング情報を管理するテーブル';