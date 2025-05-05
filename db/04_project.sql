-- SES Management System PostgreSQL Schema
-- 04_project.sql: 案件管理テーブル
-- Created: 2025-05-06

-- ============================
-- PROJECT MANAGEMENT
-- ============================

CREATE TABLE IF NOT EXISTS projects (
    project_id SERIAL PRIMARY KEY,
    project_code VARCHAR(20) UNIQUE,
    customer_id INTEGER NOT NULL REFERENCES customers(customer_id),
    project_name VARCHAR(100) NOT NULL,
    project_overview TEXT,
    project_type TEXT CHECK (project_type IN ('SES', '請負', '自社開発', '共同開発', 'その他')) DEFAULT 'SES',
    phase TEXT NOT NULL CHECK (phase IN ('リード', '提案', '交渉', '受注', '進行中', '完了', '中止')) DEFAULT 'リード',
    priority TEXT CHECK (priority IN ('低', '中', '高', '緊急')) DEFAULT '中',
    start_date DATE,
    end_date DATE,
    estimated_end_date DATE,
    required_personnel INTEGER,
    current_personnel INTEGER DEFAULT 0,
    development_environment TEXT,
    technical_requirements TEXT,
    project_location VARCHAR(200),
    work_style TEXT CHECK (work_style IN ('常駐', 'リモート', 'ハイブリッド', 'その他')) DEFAULT '常駐',
    distribution_type TEXT NOT NULL CHECK (distribution_type IN ('直請', '一次', '二次', 'その他')) DEFAULT '直請',
    contract_form TEXT CHECK (contract_form IN ('準委任', '請負', 'その他')) DEFAULT '準委任',
    budget NUMERIC(15,2),
    total_cost NUMERIC(15,2) DEFAULT 0,
    margin_rate NUMERIC(5,2),
    status TEXT CHECK (status IN ('計画中', '進行中', '完了', '中止', '保留')) DEFAULT '計画中',
    project_manager UUID REFERENCES users(user_id),
    sales_representative UUID REFERENCES users(user_id),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_project_name ON projects(project_name);
CREATE INDEX IF NOT EXISTS idx_project_code ON projects(project_code);
CREATE INDEX IF NOT EXISTS idx_project_phase ON projects(phase);
CREATE INDEX IF NOT EXISTS idx_project_start_date ON projects(start_date);
CREATE INDEX IF NOT EXISTS idx_project_end_date ON projects(end_date);
CREATE INDEX IF NOT EXISTS idx_project_customer_id ON projects(customer_id);
CREATE INDEX IF NOT EXISTS idx_project_status ON projects(status);
CREATE INDEX IF NOT EXISTS idx_project_project_manager ON projects(project_manager);
CREATE INDEX IF NOT EXISTS idx_project_sales_representative ON projects(sales_representative);

CREATE TRIGGER update_projects_timestamp
BEFORE UPDATE ON projects
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS project_requirements (
    requirement_id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(project_id),
    position_title VARCHAR(100),
    skill_requirement TEXT,
    required_skills INTEGER[] DEFAULT '{}',  -- スキルIDの配列
    technical_skill_level TEXT CHECK (technical_skill_level IN ('初級', '中級', '上級', 'エキスパート')) DEFAULT '中級',
    years_of_experience INTEGER,
    business_knowledge TEXT,
    qualification_requirements TEXT,
    work_hours VARCHAR(100),
    overtime_requirement TEXT,
    preferred_unit_price NUMERIC(10,2),
    minimum_unit_price NUMERIC(10,2),
    maximum_unit_price NUMERIC(10,2),
    interview_process TEXT,
    selection_criteria TEXT,
    assigned_personnel INTEGER, -- 外部キー制約を削除（循環参照を避けるため）
    status TEXT CHECK (status IN ('募集中', '選考中', '決定済', '保留', 'キャンセル')) DEFAULT '募集中',
    priority INTEGER NOT NULL DEFAULT 0 CHECK (priority BETWEEN 0 AND 10),
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_project_requirements_project_id ON project_requirements(project_id);
CREATE INDEX IF NOT EXISTS idx_project_requirements_status ON project_requirements(status);
CREATE INDEX IF NOT EXISTS idx_project_requirements_assigned_personnel ON project_requirements(assigned_personnel);
CREATE INDEX IF NOT EXISTS idx_project_requirements_priority ON project_requirements(priority);

CREATE TRIGGER update_project_requirements_timestamp
BEFORE UPDATE ON project_requirements
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS project_members (
    member_id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(project_id),
    engineer_id INTEGER NOT NULL REFERENCES engineers(engineer_id),
    requirement_id INTEGER REFERENCES project_requirements(requirement_id),
    role TEXT CHECK (role IN ('PM', 'リーダー', 'メンバー', 'その他')) DEFAULT 'メンバー',
    start_date DATE NOT NULL,
    end_date DATE,
    unit_price NUMERIC(10,2) NOT NULL,
    work_location VARCHAR(200),
    work_style TEXT CHECK (work_style IN ('常駐', 'リモート', 'ハイブリッド', 'その他')) DEFAULT '常駐',
    billing_rate NUMERIC(5,2) DEFAULT 1.0,  -- 例: 1.0 = 100%稼働
    status TEXT CHECK (status IN ('アサイン予定', 'アサイン中', '終了', '延長交渉中')) DEFAULT 'アサイン予定',
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (project_id, engineer_id, start_date)
);

CREATE INDEX IF NOT EXISTS idx_project_members_project_id ON project_members(project_id);
CREATE INDEX IF NOT EXISTS idx_project_members_engineer_id ON project_members(engineer_id);
CREATE INDEX IF NOT EXISTS idx_project_members_requirement_id ON project_members(requirement_id);
CREATE INDEX IF NOT EXISTS idx_project_members_start_date ON project_members(start_date);
CREATE INDEX IF NOT EXISTS idx_project_members_end_date ON project_members(end_date);
CREATE INDEX IF NOT EXISTS idx_project_members_status ON project_members(status);

CREATE TRIGGER update_project_members_timestamp
BEFORE UPDATE ON project_members
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS project_documents (
    document_id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(project_id),
    document_name VARCHAR(100) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size INTEGER,
    file_hash VARCHAR(100),
    document_type TEXT NOT NULL CHECK (document_type IN ('NDA', '提案書', '仕様書', '契約書', '報告書', '議事録', 'その他')) DEFAULT 'その他',
    version VARCHAR(20),
    author UUID REFERENCES users(user_id),
    description TEXT,
    visibility TEXT CHECK (visibility IN ('社内のみ', '顧客共有', '限定公開', '公開')) DEFAULT '社内のみ',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_project_documents_project_id ON project_documents(project_id);
CREATE INDEX IF NOT EXISTS idx_document_type ON project_documents(document_type);
CREATE INDEX IF NOT EXISTS idx_project_documents_visibility ON project_documents(visibility);

CREATE TRIGGER update_project_documents_timestamp
BEFORE UPDATE ON project_documents
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS project_milestones (
    milestone_id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(project_id),
    milestone_name VARCHAR(100) NOT NULL,
    description TEXT,
    planned_date DATE NOT NULL,
    actual_date DATE,
    status TEXT CHECK (status IN ('予定', '進行中', '完了', '延期', '中止')) DEFAULT '予定',
    responsible_person UUID REFERENCES users(user_id),
    priority TEXT CHECK (priority IN ('低', '中', '高', '緊急')) DEFAULT '中',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_project_milestones_project_id ON project_milestones(project_id);
CREATE INDEX IF NOT EXISTS idx_project_milestones_planned_date ON project_milestones(planned_date);
CREATE INDEX IF NOT EXISTS idx_project_milestones_status ON project_milestones(status);

CREATE TRIGGER update_project_milestones_timestamp
BEFORE UPDATE ON project_milestones
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS project_progress (
    progress_id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(project_id),
    report_date DATE NOT NULL,
    progress_percentage NUMERIC(5,2) CHECK (progress_percentage BETWEEN 0 AND 100),
    status TEXT CHECK (status IN ('順調', '要注意', '遅延', '中止')) DEFAULT '順調',
    achievements TEXT,
    issues TEXT,
    next_actions TEXT,
    reported_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (project_id, report_date)
);

CREATE INDEX IF NOT EXISTS idx_project_progress_project_id ON project_progress(project_id);
CREATE INDEX IF NOT EXISTS idx_project_progress_report_date ON project_progress(report_date);
CREATE INDEX IF NOT EXISTS idx_project_progress_status ON project_progress(status);

CREATE TRIGGER update_project_progress_timestamp
BEFORE UPDATE ON project_progress
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TABLE projects IS '案件情報を管理するテーブル';
COMMENT ON TABLE project_requirements IS '案件の要員要件を管理するテーブル';
COMMENT ON TABLE project_members IS '案件のアサイン状況を管理するテーブル';
COMMENT ON TABLE project_documents IS '案件の関連文書を管理するテーブル';
COMMENT ON TABLE project_milestones IS '案件のマイルストーンを管理するテーブル';
COMMENT ON TABLE project_progress IS '案件の進捗状況を管理するテーブル';