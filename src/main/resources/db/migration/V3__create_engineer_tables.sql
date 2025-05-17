-- スキルカテゴリテーブル
CREATE TABLE engineer.skill_category (
    id SERIAL PRIMARY KEY,
    category_code VARCHAR(50) NOT NULL UNIQUE,
    category_name VARCHAR(100) NOT NULL,
    parent_category_id INT REFERENCES engineer.skill_category(id),
    sort_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- スキル定義テーブル
CREATE TABLE engineer.skill_definition (
    id SERIAL PRIMARY KEY,
    skill_code VARCHAR(50) NOT NULL UNIQUE,
    skill_name VARCHAR(100) NOT NULL,
    skill_category_id INT NOT NULL REFERENCES engineer.skill_category(id),
    description TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 資格種別テーブル
CREATE TABLE engineer.certification_type (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    issuing_organization VARCHAR(100),
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 技術者基本情報テーブル
CREATE TABLE engineer.engineer (
    id BIGSERIAL PRIMARY KEY,
    employee_code VARCHAR(50) UNIQUE,
    last_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name_kana VARCHAR(100),
    first_name_kana VARCHAR(100),
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    birth_date DATE,
    gender VARCHAR(10),
    postal_code VARCHAR(20),
    address_1 VARCHAR(255),
    address_2 VARCHAR(255),
    profile_photo_file_id BIGINT REFERENCES common.files(id),
    employment_type VARCHAR(50) NOT NULL,
    entry_date DATE,
    retirement_date DATE,
    status_code VARCHAR(50) NOT NULL,
    memo TEXT,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_engineer_status_code ON engineer.engineer(status_code);
CREATE INDEX ix_engineer_entry_date ON engineer.engineer(entry_date);
CREATE INDEX ix_engineer_employment_type ON engineer.engineer(employment_type);

-- 技術者スキル関連テーブル
CREATE TABLE engineer.engineer_skill (
    id BIGSERIAL PRIMARY KEY,
    engineer_id BIGINT NOT NULL REFERENCES engineer.engineer(id),
    skill_definition_id INT NOT NULL REFERENCES engineer.skill_definition(id),
    skill_level VARCHAR(50) NOT NULL,
    years_of_experience NUMERIC(4,1),
    last_used_date DATE,
    description TEXT,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (engineer_id, skill_definition_id)
);

-- インデックス作成
CREATE INDEX ix_engineer_skill_engineer_id ON engineer.engineer_skill(engineer_id);
CREATE INDEX ix_engineer_skill_skill_level ON engineer.engineer_skill(skill_level);
CREATE INDEX ix_engineer_skill_skill_def_id ON engineer.engineer_skill(skill_definition_id);

-- 技術者保有資格テーブル
CREATE TABLE engineer.certification (
    id BIGSERIAL PRIMARY KEY,
    engineer_id BIGINT NOT NULL REFERENCES engineer.engineer(id),
    certification_type_id INT NOT NULL REFERENCES engineer.certification_type(id),
    certification_number VARCHAR(100),
    acquisition_date DATE NOT NULL,
    expiration_date DATE,
    verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    file_id BIGINT REFERENCES common.files(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_certification_engineer_id ON engineer.certification(engineer_id);
CREATE INDEX ix_certification_type_id ON engineer.certification(certification_type_id);

-- 稼働状況テーブル
CREATE TABLE engineer.engineer_availability (
    id BIGSERIAL PRIMARY KEY,
    engineer_id BIGINT NOT NULL REFERENCES engineer.engineer(id),
    availability_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    project_id BIGINT,  -- 後で外部キー制約を追加
    utilization_rate NUMERIC(5,2),  -- 稼働率（％）
    memo TEXT,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_engineer_availability_engineer_id ON engineer.engineer_availability(engineer_id);
CREATE INDEX ix_engineer_availability_date_range ON engineer.engineer_availability(start_date, end_date);
CREATE INDEX ix_engineer_availability_type ON engineer.engineer_availability(availability_type);

-- 職務経歴テーブル
CREATE TABLE engineer.work_history (
    id BIGSERIAL PRIMARY KEY,
    engineer_id BIGINT NOT NULL REFERENCES engineer.engineer(id),
    company_name VARCHAR(255) NOT NULL,
    project_name VARCHAR(255) NOT NULL,
    role VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    description TEXT,
    technologies TEXT,
    team_size INT,
    responsibilities TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_work_history_engineer_id ON engineer.work_history(engineer_id);
CREATE INDEX ix_work_history_date_range ON engineer.work_history(start_date, end_date);

-- スキルシートテンプレートテーブル
CREATE TABLE engineer.skill_sheet_template (
    id SERIAL PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL,
    template_format VARCHAR(50) NOT NULL,
    template_file_id BIGINT REFERENCES common.files(id),
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- スキルシートテーブル
CREATE TABLE engineer.skill_sheet (
    id BIGSERIAL PRIMARY KEY,
    engineer_id BIGINT NOT NULL REFERENCES engineer.engineer(id),
    template_id INT REFERENCES engineer.skill_sheet_template(id),
    version INT NOT NULL DEFAULT 1,
    title VARCHAR(255) NOT NULL,
    file_id BIGINT REFERENCES common.files(id),
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_skill_sheet_engineer_id ON engineer.skill_sheet(engineer_id);
CREATE INDEX ix_skill_sheet_status ON engineer.skill_sheet(status);

-- 初期データ: スキルカテゴリ
INSERT INTO engineer.skill_category (category_code, category_name, sort_order) VALUES
    ('PROGRAMMING', 'プログラミング言語', 10),
    ('DATABASE', 'データベース', 20),
    ('FRAMEWORK', 'フレームワーク', 30),
    ('OS', 'オペレーティングシステム', 40),
    ('MIDDLEWARE', 'ミドルウェア', 50),
    ('TOOL', '開発ツール', 60),
    ('METHODOLOGY', '開発手法', 70),
    ('CLOUD', 'クラウドサービス', 80),
    ('QUALIFICATION', '資格', 90),
    ('OTHER', 'その他', 100);

-- 初期データ: スキル定義（一部）
INSERT INTO engineer.skill_definition (skill_code, skill_name, skill_category_id, sort_order) VALUES
    ('JAVA', 'Java', 1, 10),
    ('CSHARP', 'C#', 1, 20),
    ('PYTHON', 'Python', 1, 30),
    ('JAVASCRIPT', 'JavaScript', 1, 40),
    ('TYPESCRIPT', 'TypeScript', 1, 50),
    ('ORACLE', 'Oracle Database', 2, 10),
    ('POSTGRESQL', 'PostgreSQL', 2, 20),
    ('MYSQL', 'MySQL', 2, 30),
    ('SQLSERVER', 'SQL Server', 2, 40),
    ('SPRING', 'Spring Framework', 3, 10),
    ('ANGULAR', 'Angular', 3, 20),
    ('REACT', 'React', 3, 30),
    ('WINDOWS', 'Windows', 4, 10),
    ('LINUX', 'Linux', 4, 20),
    ('DOCKER', 'Docker', 5, 10),
    ('AWS', 'Amazon Web Services', 8, 10),
    ('AZURE', 'Microsoft Azure', 8, 20),
    ('GCP', 'Google Cloud Platform', 8, 30);

-- 初期データ: 資格種別
INSERT INTO engineer.certification_type (code, name, issuing_organization) VALUES
    ('JAVASILVER', 'Java Silver', 'Oracle'),
    ('JAVAGOLD', 'Java Gold', 'Oracle'),
    ('AWSSAA', 'AWS Solutions Architect Associate', 'Amazon Web Services'),
    ('AWSSAP', 'AWS Solutions Architect Professional', 'Amazon Web Services'),
    ('AZUREFUNDAMENTALS', 'Microsoft Azure Fundamentals (AZ-900)', 'Microsoft'),
    ('GCPACE', 'Google Cloud Professional Cloud Architect', 'Google'),
    ('PMP', 'Project Management Professional', 'Project Management Institute'),
    ('IPA_FE', '基本情報技術者試験', 'IPA'),
    ('IPA_AP', '応用情報技術者試験', 'IPA'),
    ('IPA_NWSP', 'ネットワークスペシャリスト試験', 'IPA'),
    ('IPA_DBSP', 'データベーススペシャリスト試験', 'IPA'),
    ('LPIC1', 'LPIC Level 1', 'Linux Professional Institute'),
    ('ORACLEMCP', 'Oracle Master Certified Professional', 'Oracle');