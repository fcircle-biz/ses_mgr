-- SES Management System PostgreSQL Schema
-- 02_company.sql: 企業・顧客関連テーブル
-- Created: 2025-05-06

-- ============================
-- COMPANY MANAGEMENT
-- ============================

CREATE TABLE IF NOT EXISTS companies (
    company_id SERIAL PRIMARY KEY,
    company_name VARCHAR(100) NOT NULL,
    company_code VARCHAR(20) UNIQUE,
    company_type TEXT NOT NULL CHECK (company_type IN ('自社', 'パートナー企業', '顧客', 'その他')) DEFAULT '自社',
    address VARCHAR(200),
    phone_number VARCHAR(20),
    email VARCHAR(100),
    fax_number VARCHAR(20),
    website VARCHAR(200),
    establishment_date DATE,
    business_description TEXT,
    capital NUMERIC(15,2),
    employee_count INTEGER,
    representative_name VARCHAR(100),
    contract_date DATE,
    contract_expiry_date DATE,
    contract_status TEXT CHECK (contract_status IN ('契約中', '期間満了', '解約', '交渉中')) DEFAULT '契約中',
    logo_path VARCHAR(255),
    notes TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_companies_name ON companies(company_name);
CREATE INDEX IF NOT EXISTS idx_companies_type ON companies(company_type);
CREATE INDEX IF NOT EXISTS idx_companies_contract_status ON companies(contract_status);
CREATE INDEX IF NOT EXISTS idx_companies_is_active ON companies(is_active);

CREATE TRIGGER update_companies_timestamp
BEFORE UPDATE ON companies
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS company_contacts (
    contact_id SERIAL PRIMARY KEY,
    company_id INTEGER NOT NULL REFERENCES companies(company_id),
    contact_name VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    position VARCHAR(100),
    email VARCHAR(100),
    phone_number VARCHAR(20),
    mobile_number VARCHAR(20),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_company_contacts_company_id ON company_contacts(company_id);
CREATE INDEX IF NOT EXISTS idx_company_contacts_is_primary ON company_contacts(is_primary);

CREATE TRIGGER update_company_contacts_timestamp
BEFORE UPDATE ON company_contacts
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS customers (
    customer_id SERIAL PRIMARY KEY,
    company_id INTEGER REFERENCES companies(company_id),
    customer_name VARCHAR(100) NOT NULL,
    customer_code VARCHAR(20) UNIQUE,
    address VARCHAR(200),
    phone_number VARCHAR(20),
    email VARCHAR(100),
    industry VARCHAR(100),
    annual_revenue NUMERIC(15,2),
    employee_count INTEGER,
    first_transaction_date DATE,
    acquisition_channel VARCHAR(100),
    customer_status TEXT CHECK (customer_status IN ('アクティブ', '休眠', '見込み', '失注')) DEFAULT 'アクティブ',
    credit_rating VARCHAR(20),
    payment_terms VARCHAR(100),
    assigned_sales_rep UUID REFERENCES users(user_id),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_customer_name ON customers(customer_name);
CREATE INDEX IF NOT EXISTS idx_customer_company_id ON customers(company_id);
CREATE INDEX IF NOT EXISTS idx_customer_status ON customers(customer_status);
CREATE INDEX IF NOT EXISTS idx_customer_assigned_sales_rep ON customers(assigned_sales_rep);

CREATE TRIGGER update_customers_timestamp
BEFORE UPDATE ON customers
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS customer_contacts (
    contact_id SERIAL PRIMARY KEY,
    customer_id INTEGER NOT NULL REFERENCES customers(customer_id),
    contact_name VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    position VARCHAR(100),
    email VARCHAR(100),
    phone_number VARCHAR(20),
    mobile_number VARCHAR(20),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    communication_preference TEXT CHECK (communication_preference IN ('電話', 'メール', '訪問', 'その他')) DEFAULT 'メール',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_customer_contacts_customer_id ON customer_contacts(customer_id);
CREATE INDEX IF NOT EXISTS idx_customer_contacts_is_primary ON customer_contacts(is_primary);

CREATE TRIGGER update_customer_contacts_timestamp
BEFORE UPDATE ON customer_contacts
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS customer_interaction_history (
    interaction_id SERIAL PRIMARY KEY,
    customer_id INTEGER NOT NULL REFERENCES customers(customer_id),
    contact_id INTEGER REFERENCES customer_contacts(contact_id),
    interaction_type TEXT CHECK (interaction_type IN ('訪問', '電話', 'メール', 'オンラインミーティング', 'イベント', 'その他')) DEFAULT 'その他',
    summary VARCHAR(200) NOT NULL,
    details TEXT,
    interaction_date TIMESTAMP NOT NULL,
    performed_by UUID REFERENCES users(user_id),
    follow_up_required BOOLEAN DEFAULT FALSE,
    follow_up_date DATE,
    follow_up_assigned_to UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_customer_interaction_history_customer_id ON customer_interaction_history(customer_id);
CREATE INDEX IF NOT EXISTS idx_customer_interaction_history_interaction_date ON customer_interaction_history(interaction_date);
CREATE INDEX IF NOT EXISTS idx_customer_interaction_history_follow_up ON customer_interaction_history(follow_up_required, follow_up_date);

CREATE TRIGGER update_customer_interaction_history_timestamp
BEFORE UPDATE ON customer_interaction_history
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TABLE companies IS '自社、パートナー、顧客の企業情報を管理するテーブル';
COMMENT ON TABLE company_contacts IS '企業の担当者情報を管理するテーブル';
COMMENT ON TABLE customers IS '顧客情報を管理するテーブル';
COMMENT ON TABLE customer_contacts IS '顧客の担当者情報を管理するテーブル';
COMMENT ON TABLE customer_interaction_history IS '顧客とのやり取りの履歴を管理するテーブル';