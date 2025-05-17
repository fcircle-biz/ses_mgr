-- 初期スキーマ作成

-- 共通モジュール: ユーザー管理
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_id INT NOT NULL REFERENCES roles(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

-- 共通モジュール: 権限管理
CREATE TABLE permissions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE role_permissions (
    role_id INT NOT NULL REFERENCES roles(id),
    permission_id INT NOT NULL REFERENCES permissions(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)
);

-- 共通モジュール: コード値管理
CREATE TABLE code_categories (
    id SERIAL PRIMARY KEY,
    category_key VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE code_values (
    id SERIAL PRIMARY KEY,
    category_id INT NOT NULL REFERENCES code_categories(id),
    code_key VARCHAR(50) NOT NULL,
    value VARCHAR(100) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (category_id, code_key)
);

-- 共通モジュール: ファイル管理
CREATE TABLE file_categories (
    id SERIAL PRIMARY KEY,
    category_key VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    path_prefix VARCHAR(255),
    allowed_extensions VARCHAR(255),
    max_file_size_kb INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE files (
    id BIGSERIAL PRIMARY KEY,
    category_id INT NOT NULL REFERENCES file_categories(id),
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    content_type VARCHAR(100),
    file_size_kb INT NOT NULL,
    uploaded_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 共通モジュール: 通知管理
CREATE TABLE notification_templates (
    id SERIAL PRIMARY KEY,
    template_key VARCHAR(100) NOT NULL UNIQUE,
    title_template TEXT NOT NULL,
    body_template TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    notification_type VARCHAR(50) NOT NULL,
    reference_id VARCHAR(100),
    reference_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

-- 機能モジュール: 監査ログ
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    change_details JSONB,
    client_ip VARCHAR(50),
    user_agent VARCHAR(500),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 初期データ: 基本ロールと権限
INSERT INTO roles (name, description) VALUES
    ('ROLE_ADMIN', '管理者ロール'),
    ('ROLE_USER', '一般ユーザーロール');

INSERT INTO permissions (name, description) VALUES
    ('user:read', 'ユーザー情報参照権限'),
    ('user:write', 'ユーザー情報更新権限'),
    ('admin:access', '管理者画面アクセス権限');

INSERT INTO role_permissions (role_id, permission_id) VALUES
    (1, 1), -- ROLE_ADMIN -> user:read
    (1, 2), -- ROLE_ADMIN -> user:write
    (1, 3), -- ROLE_ADMIN -> admin:access
    (2, 1); -- ROLE_USER -> user:read

-- 初期データ: コード値カテゴリ
INSERT INTO code_categories (category_key, name, description) VALUES
    ('SKILL_CATEGORY', 'スキルカテゴリ', '技術者のスキルカテゴリ'),
    ('SKILL_LEVEL', 'スキルレベル', '技術者のスキルレベル'),
    ('PROJECT_STATUS', '案件ステータス', '案件の状態を表すステータス'),
    ('CONTRACT_TYPE', '契約種別', '契約の種類');

-- 初期データ: コード値
INSERT INTO code_values (category_id, code_key, value, display_order) VALUES
    (1, 'LANGUAGE', 'プログラミング言語', 1),
    (1, 'DB', 'データベース', 2),
    (1, 'FW', 'フレームワーク', 3),
    (1, 'OS', 'OS', 4),
    (1, 'TOOL', '開発ツール', 5),
    (1, 'OTHER', 'その他', 6),
    
    (2, 'BEGINNER', '初級者', 1),
    (2, 'INTERMEDIATE', '中級者', 2),
    (2, 'ADVANCED', '上級者', 3),
    (2, 'EXPERT', 'エキスパート', 4),
    
    (3, 'DRAFT', '下書き', 1),
    (3, 'PUBLISHED', '公開中', 2),
    (3, 'IN_PROGRESS', '交渉中', 3),
    (3, 'CLOSED', '完了', 4),
    (3, 'CANCELLED', 'キャンセル', 5),
    
    (4, 'FIXED', '固定価格', 1),
    (4, 'TIME_MATERIAL', '時間精算', 2);

-- 初期データ: ファイルカテゴリ
INSERT INTO file_categories (category_key, name, description, path_prefix, allowed_extensions, max_file_size_kb) VALUES
    ('PROFILE_IMAGE', 'プロフィール画像', 'ユーザーのプロフィール画像', 'profiles/', 'jpg,jpeg,png', 5120),
    ('SKILL_SHEET', 'スキルシート', '技術者のスキルシート', 'skillsheets/', 'pdf,docx', 10240),
    ('CONTRACT_DOCUMENT', '契約書', '契約関連ドキュメント', 'contracts/', 'pdf', 15360),
    ('PROJECT_DOCUMENT', '案件資料', '案件に関する資料', 'projects/', 'pdf,docx,xlsx,pptx', 20480);