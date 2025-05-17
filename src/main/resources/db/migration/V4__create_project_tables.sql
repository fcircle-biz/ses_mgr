-- 案件ステータステーブル
CREATE TABLE project.project_status (
    id SERIAL PRIMARY KEY,
    status_code VARCHAR(50) NOT NULL UNIQUE,
    status_name VARCHAR(100) NOT NULL,
    description TEXT,
    color_code VARCHAR(20),
    sort_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 顧客情報テーブル
CREATE TABLE project.customer (
    id BIGSERIAL PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    company_name_kana VARCHAR(255),
    postal_code VARCHAR(20),
    address_1 VARCHAR(255),
    address_2 VARCHAR(255),
    tel VARCHAR(50),
    fax VARCHAR(50),
    url VARCHAR(255),
    industry_type VARCHAR(100),
    business_description TEXT,
    employee_count INT,
    established_year INT,
    capital_amount NUMERIC(18,0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    note TEXT,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_customer_company_name ON project.customer(company_name);
CREATE INDEX ix_customer_industry_type ON project.customer(industry_type);

-- 顧客担当者テーブル
CREATE TABLE project.customer_contact (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES project.customer(id),
    department VARCHAR(100),
    position VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name_kana VARCHAR(100),
    first_name_kana VARCHAR(100),
    email VARCHAR(255),
    tel VARCHAR(50),
    mobile_phone VARCHAR(50),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_customer_contact_customer_id ON project.customer_contact(customer_id);
CREATE INDEX ix_customer_contact_is_primary ON project.customer_contact(is_primary);

-- 案件基本情報テーブル
CREATE TABLE project.project (
    id BIGSERIAL PRIMARY KEY,
    project_code VARCHAR(50) NOT NULL UNIQUE,
    project_name VARCHAR(255) NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES project.customer(id),
    primary_contact_id BIGINT REFERENCES project.customer_contact(id),
    status_id INT NOT NULL REFERENCES project.project_status(id),
    project_type VARCHAR(50) NOT NULL,
    start_date DATE,
    end_date DATE,
    contract_type VARCHAR(50),
    description TEXT,
    requirement_overview TEXT,
    development_environment TEXT,
    special_requirement TEXT,
    estimated_total_cost NUMERIC(18,0),
    location VARCHAR(255),
    is_remote BOOLEAN NOT NULL DEFAULT FALSE,
    working_hours VARCHAR(100),
    breaking_hours VARCHAR(100),
    overtime_hours VARCHAR(100),
    working_days VARCHAR(100),
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_project_customer_id ON project.project(customer_id);
CREATE INDEX ix_project_status_id ON project.project(status_id);
CREATE INDEX ix_project_date_range ON project.project(start_date, end_date);
CREATE INDEX ix_project_project_type ON project.project(project_type);
CREATE INDEX ix_project_contract_type ON project.project(contract_type);

-- ステータス履歴テーブル
CREATE TABLE project.project_status_history (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES project.project(id),
    previous_status_id INT REFERENCES project.project_status(id),
    new_status_id INT NOT NULL REFERENCES project.project_status(id),
    changed_by BIGINT REFERENCES common.users(id),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason TEXT
);

-- インデックス作成
CREATE INDEX ix_project_status_history_project_id ON project.project_status_history(project_id);
CREATE INDEX ix_project_status_history_changed_at ON project.project_status_history(changed_at);

-- 要員要件テーブル
CREATE TABLE project.resource_requirement (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES project.project(id),
    position_name VARCHAR(100) NOT NULL,
    required_number INT NOT NULL DEFAULT 1,
    priority VARCHAR(50),
    minimum_experience_years INT,
    preferred_experience_years INT,
    start_date DATE,
    end_date DATE,
    budget_min_rate NUMERIC(10,0),
    budget_max_rate NUMERIC(10,0),
    work_description TEXT,
    required_skills TEXT,
    preferred_skills TEXT,
    is_fulfilled BOOLEAN NOT NULL DEFAULT FALSE,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_resource_requirement_project_id ON project.resource_requirement(project_id);
CREATE INDEX ix_resource_requirement_date_range ON project.resource_requirement(start_date, end_date);
CREATE INDEX ix_resource_requirement_is_fulfilled ON project.resource_requirement(is_fulfilled);

-- スキル要件テーブル
CREATE TABLE project.skill_requirement (
    id BIGSERIAL PRIMARY KEY,
    resource_requirement_id BIGINT NOT NULL REFERENCES project.resource_requirement(id),
    skill_definition_id INT NOT NULL REFERENCES engineer.skill_definition(id),
    minimum_skill_level VARCHAR(50) NOT NULL,
    preferred_skill_level VARCHAR(50),
    importance VARCHAR(50) NOT NULL DEFAULT 'NORMAL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (resource_requirement_id, skill_definition_id)
);

-- インデックス作成
CREATE INDEX ix_skill_requirement_resource_requirement_id ON project.skill_requirement(resource_requirement_id);
CREATE INDEX ix_skill_requirement_skill_definition_id ON project.skill_requirement(skill_definition_id);
CREATE INDEX ix_skill_requirement_importance ON project.skill_requirement(importance);

-- 案件予算テーブル
CREATE TABLE project.project_budget (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES project.project(id),
    budget_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    amount NUMERIC(18,0) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'JPY',
    memo TEXT,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_project_budget_project_id ON project.project_budget(project_id);
CREATE INDEX ix_project_budget_budget_type ON project.project_budget(budget_type);
CREATE INDEX ix_project_budget_date_range ON project.project_budget(start_date, end_date);

-- 案件ドキュメントテーブル
CREATE TABLE project.project_document (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES project.project(id),
    document_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_id BIGINT REFERENCES common.files(id),
    version INT NOT NULL DEFAULT 1,
    is_latest BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_project_document_project_id ON project.project_document(project_id);
CREATE INDEX ix_project_document_document_type ON project.project_document(document_type);
CREATE INDEX ix_project_document_is_latest ON project.project_document(is_latest);

-- 案件担当者テーブル
CREATE TABLE project.project_member (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES project.project(id),
    user_id BIGINT NOT NULL REFERENCES common.users(id),
    role_type VARCHAR(50) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    start_date DATE NOT NULL,
    end_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (project_id, user_id, role_type)
);

-- インデックス作成
CREATE INDEX ix_project_member_project_id ON project.project_member(project_id);
CREATE INDEX ix_project_member_user_id ON project.project_member(user_id);
CREATE INDEX ix_project_member_role_type ON project.project_member(role_type);

-- 稼働状況テーブル追加の制約（engineer.engineer_availabilityのproject_id参照）
ALTER TABLE engineer.engineer_availability 
ADD CONSTRAINT fk_engineer_availability_project 
FOREIGN KEY (project_id) REFERENCES project.project(id);

-- 初期データ: 案件ステータス
INSERT INTO project.project_status (status_code, status_name, description, color_code, sort_order) VALUES
    ('DRAFT', '下書き', '作成中の案件', '#6c757d', 10),
    ('OPEN', '募集中', '技術者募集中の案件', '#28a745', 20),
    ('IN_SELECTION', '選考中', '技術者選考中の案件', '#fd7e14', 30),
    ('ASSIGNED', 'アサイン済', '技術者がアサインされた案件', '#17a2b8', 40),
    ('IN_PROGRESS', '進行中', '作業が開始された案件', '#007bff', 50),
    ('COMPLETED', '完了', '終了した案件', '#6f42c1', 60),
    ('CANCELLED', 'キャンセル', 'キャンセルされた案件', '#dc3545', 70),
    ('ON_HOLD', '保留中', '一時的に保留されている案件', '#ffc107', 80);