-- SES Management System PostgreSQL Schema
-- 03_engineer.sql: 技術者管理テーブル
-- Created: 2025-05-06

-- ============================
-- ENGINEER MANAGEMENT
-- ============================

CREATE TABLE IF NOT EXISTS engineers (
    engineer_id SERIAL PRIMARY KEY,
    company_id INTEGER NOT NULL REFERENCES companies(company_id),
    engineer_code VARCHAR(20) UNIQUE,
    engineer_name VARCHAR(100) NOT NULL,
    furigana VARCHAR(100),
    email VARCHAR(100),
    phone_number VARCHAR(20),
    mobile_number VARCHAR(20),
    address VARCHAR(200),
    birth_date DATE,
    gender VARCHAR(10),
    employment_type TEXT NOT NULL CHECK (employment_type IN ('正社員', '契約社員', 'パートナー', 'フリーランス', 'その他')) DEFAULT '正社員',
    join_date DATE,
    departure_date DATE,
    years_of_experience INTEGER DEFAULT 0,
    educational_background TEXT,
    specialized_field TEXT,
    preferred_work_location TEXT,
    nearest_station VARCHAR(100),
    commute_time INTEGER,
    status TEXT NOT NULL CHECK (status IN ('稼働可能', 'アサイン中', '研修中', '休職中', '退職', '面談中')) DEFAULT '稼働可能',
    availability_date DATE,
    current_project INTEGER, -- 外部キー制約を削除（循環参照を避けるため）
    current_unit_price NUMERIC(10,2),
    preferred_unit_price NUMERIC(10,2),
    max_unit_price NUMERIC(10,2),
    min_unit_price NUMERIC(10,2),
    bank_account_info TEXT,
    background_check_date DATE,
    profile_text TEXT,
    avatar_path VARCHAR(255),
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_engineers_company_id ON engineers(company_id);
CREATE INDEX IF NOT EXISTS idx_engineers_status ON engineers(status);
CREATE INDEX IF NOT EXISTS idx_engineers_availability_date ON engineers(availability_date);
CREATE INDEX IF NOT EXISTS idx_engineers_preferred_unit_price ON engineers(preferred_unit_price);
CREATE INDEX IF NOT EXISTS idx_engineers_current_project ON engineers(current_project);

CREATE TRIGGER update_engineers_timestamp
BEFORE UPDATE ON engineers
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS skills (
    skill_id SERIAL PRIMARY KEY,
    skill_name VARCHAR(100) NOT NULL,
    skill_category TEXT NOT NULL CHECK (skill_category IN ('言語', 'フレームワーク', 'DB', 'OS', 'ミドルウェア', 'クラウド', 'ツール', '資格', 'その他')),
    skill_level INTEGER CHECK (skill_level BETWEEN 1 AND 5),
    description TEXT,
    is_searchable BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_skills_name ON skills(skill_name);
CREATE INDEX IF NOT EXISTS idx_skills_category ON skills(skill_category);
CREATE INDEX IF NOT EXISTS idx_skills_is_searchable ON skills(is_searchable);

CREATE TRIGGER update_skills_timestamp
BEFORE UPDATE ON skills
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS skill_aliases (
    alias_id SERIAL PRIMARY KEY,
    skill_id INTEGER NOT NULL REFERENCES skills(skill_id),
    alias_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (alias_name)
);

CREATE INDEX IF NOT EXISTS idx_skill_aliases_skill_id ON skill_aliases(skill_id);

CREATE TRIGGER update_skill_aliases_timestamp
BEFORE UPDATE ON skill_aliases
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS engineer_skills (
    engineer_skill_id SERIAL PRIMARY KEY,
    engineer_id INTEGER NOT NULL REFERENCES engineers(engineer_id),
    skill_id INTEGER NOT NULL REFERENCES skills(skill_id),
    proficiency_level INTEGER NOT NULL DEFAULT 1 CHECK (proficiency_level BETWEEN 1 AND 5),
    years_of_experience NUMERIC(3,1) DEFAULT 0,
    last_used_date DATE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_by UUID REFERENCES users(user_id),
    verified_date TIMESTAMP,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (engineer_id, skill_id)
);

CREATE INDEX IF NOT EXISTS idx_engineer_skills_engineer_id ON engineer_skills(engineer_id);
CREATE INDEX IF NOT EXISTS idx_engineer_skills_skill_id ON engineer_skills(skill_id);
CREATE INDEX IF NOT EXISTS idx_engineer_skills_proficiency ON engineer_skills(proficiency_level);
CREATE INDEX IF NOT EXISTS idx_engineer_skills_years_experience ON engineer_skills(years_of_experience);

CREATE TRIGGER update_engineer_skills_timestamp
BEFORE UPDATE ON engineer_skills
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS skill_sheets (
    sheet_id SERIAL PRIMARY KEY,
    engineer_id INTEGER NOT NULL REFERENCES engineers(engineer_id),
    sheet_name VARCHAR(100) NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    file_path VARCHAR(255),
    file_hash VARCHAR(100),
    file_size INTEGER,
    file_format TEXT NOT NULL CHECK (file_format IN ('PDF', 'DOCX', 'HTML', 'その他')) DEFAULT 'PDF',
    is_current BOOLEAN NOT NULL DEFAULT TRUE,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    approval_status TEXT CHECK (approval_status IN ('作成中', '審査中', '承認済', '差戻', 'その他')) DEFAULT '作成中',
    approved_by UUID REFERENCES users(user_id),
    approved_at TIMESTAMP,
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_skill_sheets_engineer_id ON skill_sheets(engineer_id);
CREATE INDEX IF NOT EXISTS idx_skill_sheets_is_current ON skill_sheets(is_current);
CREATE INDEX IF NOT EXISTS idx_skill_sheets_approval_status ON skill_sheets(approval_status);

CREATE TRIGGER update_skill_sheets_timestamp
BEFORE UPDATE ON skill_sheets
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS certifications (
    certification_id SERIAL PRIMARY KEY,
    engineer_id INTEGER NOT NULL REFERENCES engineers(engineer_id),
    certification_name VARCHAR(100) NOT NULL,
    issuing_organization VARCHAR(100),
    certification_level VARCHAR(50),
    acquisition_date DATE,
    expiration_date DATE,
    certification_number VARCHAR(50),
    document_path VARCHAR(255),
    verification_status TEXT CHECK (verification_status IN ('未確認', '確認済', '確認中', '期限切れ')) DEFAULT '未確認',
    verified_by UUID REFERENCES users(user_id),
    verified_date TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_certifications_engineer_id ON certifications(engineer_id);
CREATE INDEX IF NOT EXISTS idx_certifications_acquisition_date ON certifications(acquisition_date);
CREATE INDEX IF NOT EXISTS idx_certifications_expiration_date ON certifications(expiration_date);
CREATE INDEX IF NOT EXISTS idx_certifications_verification_status ON certifications(verification_status);

CREATE TRIGGER update_certifications_timestamp
BEFORE UPDATE ON certifications
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS work_experiences (
    experience_id SERIAL PRIMARY KEY,
    engineer_id INTEGER NOT NULL REFERENCES engineers(engineer_id),
    project_name VARCHAR(100) NOT NULL,
    client_name VARCHAR(100),
    start_date DATE NOT NULL,
    end_date DATE,
    ongoing BOOLEAN DEFAULT FALSE,
    role VARCHAR(100),
    team_size INTEGER,
    description TEXT,
    technologies_used TEXT,
    responsibilities TEXT,
    achievements TEXT,
    verification_status TEXT CHECK (verification_status IN ('未確認', '確認済', '確認中')) DEFAULT '未確認',
    verified_by UUID REFERENCES users(user_id),
    verified_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_work_experiences_engineer_id ON work_experiences(engineer_id);
CREATE INDEX IF NOT EXISTS idx_work_experiences_start_date ON work_experiences(start_date);
CREATE INDEX IF NOT EXISTS idx_work_experiences_end_date ON work_experiences(end_date);
CREATE INDEX IF NOT EXISTS idx_work_experiences_role ON work_experiences(role);

CREATE TRIGGER update_work_experiences_timestamp
BEFORE UPDATE ON work_experiences
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TABLE engineers IS 'エンジニア情報を管理するテーブル';
COMMENT ON TABLE skills IS 'スキル情報を管理するテーブル';
COMMENT ON TABLE skill_aliases IS 'スキルの別名を管理するテーブル（例：JS/JavaScript）';
COMMENT ON TABLE engineer_skills IS 'エンジニアのスキル情報を管理するテーブル';
COMMENT ON TABLE skill_sheets IS 'エンジニアのスキルシート（履歴書・職務経歴書）を管理するテーブル';
COMMENT ON TABLE certifications IS 'エンジニアの資格情報を管理するテーブル';
COMMENT ON TABLE work_experiences IS 'エンジニアの職務経験情報を管理するテーブル';