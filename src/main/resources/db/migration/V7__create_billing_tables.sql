-- 銀行口座テーブル
CREATE TABLE billing.bank_account (
    id BIGSERIAL PRIMARY KEY,
    account_holder VARCHAR(255) NOT NULL,
    account_holder_kana VARCHAR(255),
    bank_name VARCHAR(100) NOT NULL,
    bank_code VARCHAR(10),
    branch_name VARCHAR(100) NOT NULL,
    branch_code VARCHAR(10),
    account_type VARCHAR(50) NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    swift_code VARCHAR(20),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    account_purpose VARCHAR(50) NOT NULL,
    reference_type VARCHAR(50) NOT NULL,
    reference_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_bank_account_reference ON billing.bank_account(reference_type, reference_id);
CREATE INDEX ix_bank_account_is_primary ON billing.bank_account(is_primary);
CREATE INDEX ix_bank_account_account_purpose ON billing.bank_account(account_purpose);

-- 請求テーブル
CREATE TABLE billing.invoice (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    contract_id BIGINT REFERENCES contract.contract(id),
    project_id BIGINT REFERENCES project.project(id),
    customer_id BIGINT NOT NULL REFERENCES project.customer(id),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    billing_period_start DATE NOT NULL,
    billing_period_end DATE NOT NULL,
    subtotal NUMERIC(18,0) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(18,0) NOT NULL DEFAULT 0,
    total_amount NUMERIC(18,0) NOT NULL DEFAULT 0,
    currency VARCHAR(10) NOT NULL DEFAULT 'JPY',
    payment_terms TEXT,
    notes TEXT,
    receipt_date DATE,
    receipt_amount NUMERIC(18,0),
    receipt_confirmation_number VARCHAR(100),
    file_id BIGINT REFERENCES common.files(id),
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_invoice_contract_id ON billing.invoice(contract_id);
CREATE INDEX ix_invoice_project_id ON billing.invoice(project_id);
CREATE INDEX ix_invoice_customer_id ON billing.invoice(customer_id);
CREATE INDEX ix_invoice_status ON billing.invoice(status);
CREATE INDEX ix_invoice_invoice_date ON billing.invoice(invoice_date);
CREATE INDEX ix_invoice_due_date ON billing.invoice(due_date);
CREATE INDEX ix_invoice_billing_period ON billing.invoice(billing_period_start, billing_period_end);

-- 請求明細テーブル
CREATE TABLE billing.invoice_item (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES billing.invoice(id),
    item_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    quantity NUMERIC(10,2) NOT NULL DEFAULT 1,
    unit_price NUMERIC(18,0) NOT NULL,
    amount NUMERIC(18,0) NOT NULL,
    tax_rate NUMERIC(5,2) NOT NULL DEFAULT 10.00,
    tax_amount NUMERIC(18,0) NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    engineer_id BIGINT REFERENCES engineer.engineer(id),
    monthly_attendance_id BIGINT REFERENCES timesheet.monthly_attendance(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_invoice_item_invoice_id ON billing.invoice_item(invoice_id);
CREATE INDEX ix_invoice_item_item_type ON billing.invoice_item(item_type);
CREATE INDEX ix_invoice_item_engineer_id ON billing.invoice_item(engineer_id);

-- 請求ステータス履歴テーブル
CREATE TABLE billing.invoice_status_history (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES billing.invoice(id),
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by BIGINT REFERENCES common.users(id),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    comments TEXT
);

-- インデックス作成
CREATE INDEX ix_invoice_status_history_invoice_id ON billing.invoice_status_history(invoice_id);
CREATE INDEX ix_invoice_status_history_new_status ON billing.invoice_status_history(new_status);

-- 支払テーブル
CREATE TABLE billing.payment (
    id BIGSERIAL PRIMARY KEY,
    payment_number VARCHAR(50) NOT NULL UNIQUE,
    payment_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    engineer_id BIGINT REFERENCES engineer.engineer(id),
    contract_id BIGINT REFERENCES contract.contract(id),
    project_id BIGINT REFERENCES project.project(id),
    payment_date DATE,
    scheduled_payment_date DATE NOT NULL,
    payment_period_start DATE NOT NULL,
    payment_period_end DATE NOT NULL,
    subtotal NUMERIC(18,0) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(18,0) NOT NULL DEFAULT 0,
    total_amount NUMERIC(18,0) NOT NULL DEFAULT 0,
    currency VARCHAR(10) NOT NULL DEFAULT 'JPY',
    payment_method VARCHAR(50),
    recipient_name VARCHAR(255) NOT NULL,
    recipient_bank_account_id BIGINT REFERENCES billing.bank_account(id),
    sender_bank_account_id BIGINT REFERENCES billing.bank_account(id),
    payment_confirmation_number VARCHAR(100),
    notes TEXT,
    file_id BIGINT REFERENCES common.files(id),
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_payment_engineer_id ON billing.payment(engineer_id);
CREATE INDEX ix_payment_contract_id ON billing.payment(contract_id);
CREATE INDEX ix_payment_project_id ON billing.payment(project_id);
CREATE INDEX ix_payment_status ON billing.payment(status);
CREATE INDEX ix_payment_payment_date ON billing.payment(payment_date);
CREATE INDEX ix_payment_scheduled_payment_date ON billing.payment(scheduled_payment_date);
CREATE INDEX ix_payment_payment_period ON billing.payment(payment_period_start, payment_period_end);
CREATE INDEX ix_payment_payment_type ON billing.payment(payment_type);

-- 支払明細テーブル
CREATE TABLE billing.payment_item (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES billing.payment(id),
    item_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    quantity NUMERIC(10,2) NOT NULL DEFAULT 1,
    unit_price NUMERIC(18,0) NOT NULL,
    amount NUMERIC(18,0) NOT NULL,
    tax_rate NUMERIC(5,2) NOT NULL DEFAULT 10.00,
    tax_amount NUMERIC(18,0) NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    monthly_attendance_id BIGINT REFERENCES timesheet.monthly_attendance(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_payment_item_payment_id ON billing.payment_item(payment_id);
CREATE INDEX ix_payment_item_item_type ON billing.payment_item(item_type);
CREATE INDEX ix_payment_item_monthly_attendance_id ON billing.payment_item(monthly_attendance_id);

-- 支払承認フローテーブル
CREATE TABLE billing.payment_approval (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES billing.payment(id),
    approval_flow_id INT NOT NULL REFERENCES timesheet.approval_flow(id),
    current_step_number INT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    submitted_at TIMESTAMP,
    submitted_by BIGINT REFERENCES common.users(id),
    completed_at TIMESTAMP,
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_payment_approval_payment_id ON billing.payment_approval(payment_id);
CREATE INDEX ix_payment_approval_approval_flow_id ON billing.payment_approval(approval_flow_id);
CREATE INDEX ix_payment_approval_status ON billing.payment_approval(status);

-- 支払承認履歴テーブル
CREATE TABLE billing.payment_approval_history (
    id BIGSERIAL PRIMARY KEY,
    payment_approval_id BIGINT NOT NULL REFERENCES billing.payment_approval(id),
    step_number INT NOT NULL,
    approval_step_id INT REFERENCES timesheet.approval_step(id),
    approver_id BIGINT REFERENCES common.users(id),
    action VARCHAR(50) NOT NULL,
    action_at TIMESTAMP NOT NULL,
    comments TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_payment_approval_history_payment_approval_id ON billing.payment_approval_history(payment_approval_id);
CREATE INDEX ix_payment_approval_history_approver_id ON billing.payment_approval_history(approver_id);
CREATE INDEX ix_payment_approval_history_action ON billing.payment_approval_history(action);

-- 入金テーブル
CREATE TABLE billing.receipt (
    id BIGSERIAL PRIMARY KEY,
    receipt_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL REFERENCES project.customer(id),
    receipt_date DATE NOT NULL,
    amount NUMERIC(18,0) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'JPY',
    receipt_method VARCHAR(50) NOT NULL,
    bank_account_id BIGINT REFERENCES billing.bank_account(id),
    transaction_id VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'UNALLOCATED',
    remaining_amount NUMERIC(18,0) NOT NULL,
    remarks TEXT,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_receipt_customer_id ON billing.receipt(customer_id);
CREATE INDEX ix_receipt_receipt_date ON billing.receipt(receipt_date);
CREATE INDEX ix_receipt_bank_account_id ON billing.receipt(bank_account_id);
CREATE INDEX ix_receipt_status ON billing.receipt(status);

-- 入金割当テーブル
CREATE TABLE billing.receipt_allocation (
    id BIGSERIAL PRIMARY KEY,
    receipt_id BIGINT NOT NULL REFERENCES billing.receipt(id),
    invoice_id BIGINT NOT NULL REFERENCES billing.invoice(id),
    allocated_amount NUMERIC(18,0) NOT NULL,
    allocation_date DATE NOT NULL,
    allocated_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_receipt_allocation_receipt_id ON billing.receipt_allocation(receipt_id);
CREATE INDEX ix_receipt_allocation_invoice_id ON billing.receipt_allocation(invoice_id);
CREATE INDEX ix_receipt_allocation_allocation_date ON billing.receipt_allocation(allocation_date);

-- 財務設定テーブル
CREATE TABLE billing.finance_setting (
    id SERIAL PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_name VARCHAR(255) NOT NULL,
    setting_value TEXT NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    description TEXT,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 初期データ: 財務設定
INSERT INTO billing.finance_setting (setting_key, setting_name, setting_value, data_type, description) VALUES
    ('DEFAULT_TAX_RATE', '標準税率', '10.0', 'NUMERIC', '標準消費税率'),
    ('INVOICE_DUE_DAYS', '請求書支払期限日数', '30', 'INTEGER', '請求書発行日から支払期限までの標準日数'),
    ('PAYMENT_TERM_MESSAGE', '支払条件メッセージ', '請求書発行日より30日以内にお支払いください。', 'TEXT', '請求書に表示する標準支払条件メッセージ'),
    ('COMPANY_BANK_ACCOUNT_ID', '会社振込先口座ID', '1', 'INTEGER', '標準の会社振込先口座ID'),
    ('INVOICE_PREFIX', '請求書番号プレフィックス', 'INV-', 'TEXT', '請求書番号の自動採番プレフィックス'),
    ('PAYMENT_PREFIX', '支払番号プレフィックス', 'PAY-', 'TEXT', '支払番号の自動採番プレフィックス'),
    ('RECEIPT_PREFIX', '入金番号プレフィックス', 'REC-', 'TEXT', '入金番号の自動採番プレフィックス'),
    ('FISCAL_YEAR_START_MONTH', '会計年度開始月', '4', 'INTEGER', '会計年度の開始月（4=4月開始）'),
    ('INVOICE_TEMPLATE_ID', '標準請求書テンプレートID', '1', 'INTEGER', '標準使用する請求書テンプレートID');