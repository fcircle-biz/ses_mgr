-- 契約書テンプレートテーブル
CREATE TABLE contract.contract_template (
    id SERIAL PRIMARY KEY,
    template_code VARCHAR(50) NOT NULL UNIQUE,
    template_name VARCHAR(255) NOT NULL,
    description TEXT,
    template_file_id BIGINT REFERENCES common.files(id),
    template_type VARCHAR(50) NOT NULL,
    variables JSONB,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_contract_template_template_type ON contract.contract_template(template_type);
CREATE INDEX ix_contract_template_is_active ON contract.contract_template(is_active);

-- 契約テーブル
CREATE TABLE contract.contract (
    id BIGSERIAL PRIMARY KEY,
    contract_number VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    contract_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    template_id INT REFERENCES contract.contract_template(id),
    contract_file_id BIGINT REFERENCES common.files(id),
    project_id BIGINT REFERENCES project.project(id),
    customer_id BIGINT REFERENCES project.customer(id),
    contract_date DATE,
    start_date DATE,
    end_date DATE,
    auto_renewal BOOLEAN NOT NULL DEFAULT FALSE,
    renewal_notice_days INT,
    termination_notice_days INT,
    amount NUMERIC(18,0),
    currency VARCHAR(10) NOT NULL DEFAULT 'JPY',
    payment_terms TEXT,
    special_terms TEXT,
    is_signed BOOLEAN NOT NULL DEFAULT FALSE,
    all_signatures_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_contract_contract_type ON contract.contract(contract_type);
CREATE INDEX ix_contract_status ON contract.contract(status);
CREATE INDEX ix_contract_project_id ON contract.contract(project_id);
CREATE INDEX ix_contract_customer_id ON contract.contract(customer_id);
CREATE INDEX ix_contract_date_range ON contract.contract(start_date, end_date);
CREATE INDEX ix_contract_is_signed ON contract.contract(is_signed);

-- 契約署名者テーブル
CREATE TABLE contract.signatory (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL REFERENCES contract.contract(id),
    party_type VARCHAR(50) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    department VARCHAR(100),
    position VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    signing_order INT NOT NULL DEFAULT 1,
    has_signed BOOLEAN NOT NULL DEFAULT FALSE,
    signed_at TIMESTAMP,
    signed_ip VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_signatory_contract_id ON contract.signatory(contract_id);
CREATE INDEX ix_signatory_party_type ON contract.signatory(party_type);
CREATE INDEX ix_signatory_signing_order ON contract.signatory(signing_order);
CREATE INDEX ix_signatory_has_signed ON contract.signatory(has_signed);

-- 電子署名テーブル
CREATE TABLE contract.e_signature (
    id BIGSERIAL PRIMARY KEY,
    signatory_id BIGINT NOT NULL REFERENCES contract.signatory(id),
    contract_id BIGINT NOT NULL REFERENCES contract.contract(id),
    signature_type VARCHAR(50) NOT NULL,
    signature_data TEXT,
    certificate TEXT,
    status VARCHAR(50) NOT NULL,
    verification_token VARCHAR(255),
    verification_expires_at TIMESTAMP,
    verified_at TIMESTAMP,
    signed_at TIMESTAMP,
    device_info JSONB,
    ip_address VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_e_signature_signatory_id ON contract.e_signature(signatory_id);
CREATE INDEX ix_e_signature_contract_id ON contract.e_signature(contract_id);
CREATE INDEX ix_e_signature_status ON contract.e_signature(status);
CREATE INDEX ix_e_signature_signed_at ON contract.e_signature(signed_at);

-- 契約変更テーブル
CREATE TABLE contract.contract_change (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL REFERENCES contract.contract(id),
    change_type VARCHAR(50) NOT NULL,
    change_description TEXT NOT NULL,
    previous_value TEXT,
    new_value TEXT,
    changed_by BIGINT REFERENCES common.users(id),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    change_status VARCHAR(50) NOT NULL,
    approval_date DATE,
    approved_by BIGINT REFERENCES common.users(id),
    change_document_id BIGINT REFERENCES common.files(id)
);

-- インデックス作成
CREATE INDEX ix_contract_change_contract_id ON contract.contract_change(contract_id);
CREATE INDEX ix_contract_change_change_type ON contract.contract_change(change_type);
CREATE INDEX ix_contract_change_change_status ON contract.contract_change(change_status);

-- 契約ドキュメント履歴テーブル
CREATE TABLE contract.contract_document_history (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL REFERENCES contract.contract(id),
    document_type VARCHAR(50) NOT NULL,
    version INT NOT NULL,
    file_id BIGINT REFERENCES common.files(id),
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    changed_by BIGINT REFERENCES common.users(id),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    change_reason TEXT
);

-- インデックス作成
CREATE INDEX ix_contract_document_history_contract_id ON contract.contract_document_history(contract_id);
CREATE INDEX ix_contract_document_history_is_current ON contract.contract_document_history(is_current);

-- 契約ステータス履歴テーブル
CREATE TABLE contract.contract_status_history (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL REFERENCES contract.contract(id),
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by BIGINT REFERENCES common.users(id),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    change_reason TEXT
);

-- インデックス作成
CREATE INDEX ix_contract_status_history_contract_id ON contract.contract_status_history(contract_id);
CREATE INDEX ix_contract_status_history_new_status ON contract.contract_status_history(new_status);

-- 初期データ: 契約書テンプレートタイプ
INSERT INTO contract.contract_template (template_code, template_name, description, template_type, variables, is_active) VALUES
    ('SES_BASIC', '基本SES契約書', 'SES契約の基本テンプレート', 'SES_CONTRACT', 
     '{"variables": ["お客様名", "業務内容", "契約期間", "料金", "支払条件"]}', true),
    ('NDA_BASIC', '基本機密保持契約書', '一般的な機密保持契約書テンプレート', 'NDA', 
     '{"variables": ["お客様名", "秘密情報の定義", "契約期間", "違約金"]}', true),
    ('EMPLOYMENT', '雇用契約書', '技術者との雇用契約書', 'EMPLOYMENT_CONTRACT', 
     '{"variables": ["従業員名", "職種", "給与", "勤務条件", "福利厚生"]}', true);