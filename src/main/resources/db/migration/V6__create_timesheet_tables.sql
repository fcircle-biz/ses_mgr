-- 勤務設定テーブル
CREATE TABLE timesheet.work_setting (
    id BIGSERIAL PRIMARY KEY,
    engineer_id BIGINT REFERENCES engineer.engineer(id),
    project_id BIGINT REFERENCES project.project(id),
    work_type VARCHAR(50) NOT NULL,
    working_days TEXT,
    working_start_time TIME,
    working_end_time TIME,
    break_start_time TIME,
    break_end_time TIME,
    regular_hours NUMERIC(4,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    valid_from DATE NOT NULL,
    valid_to DATE,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_work_setting_engineer_id ON timesheet.work_setting(engineer_id);
CREATE INDEX ix_work_setting_project_id ON timesheet.work_setting(project_id);
CREATE INDEX ix_work_setting_work_type ON timesheet.work_setting(work_type);
CREATE INDEX ix_work_setting_validity ON timesheet.work_setting(valid_from, valid_to);

-- 祝日マスタテーブル
CREATE TABLE timesheet.holiday (
    id SERIAL PRIMARY KEY,
    holiday_date DATE NOT NULL UNIQUE,
    holiday_name VARCHAR(100) NOT NULL,
    is_national_holiday BOOLEAN NOT NULL DEFAULT TRUE,
    is_company_holiday BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_holiday_holiday_date ON timesheet.holiday(holiday_date);

-- 月次勤怠テーブル
CREATE TABLE timesheet.monthly_attendance (
    id BIGSERIAL PRIMARY KEY,
    engineer_id BIGINT NOT NULL REFERENCES engineer.engineer(id),
    project_id BIGINT REFERENCES project.project(id),
    year INT NOT NULL,
    month INT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    total_working_days INT NOT NULL DEFAULT 0,
    total_working_hours NUMERIC(6,2) NOT NULL DEFAULT 0,
    total_overtime_hours NUMERIC(6,2) NOT NULL DEFAULT 0,
    total_late_hours NUMERIC(6,2) NOT NULL DEFAULT 0,
    total_early_leave_hours NUMERIC(6,2) NOT NULL DEFAULT 0,
    total_absence_days INT NOT NULL DEFAULT 0,
    total_holidays INT NOT NULL DEFAULT 0,
    total_paid_leave_days NUMERIC(4,1) NOT NULL DEFAULT 0,
    total_half_day_leave INT NOT NULL DEFAULT 0,
    total_special_leave_days NUMERIC(4,1) NOT NULL DEFAULT 0,
    comment TEXT,
    submitted_at TIMESTAMP,
    submitted_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(engineer_id, project_id, year, month)
);

-- インデックス作成
CREATE INDEX ix_monthly_attendance_engineer_id ON timesheet.monthly_attendance(engineer_id);
CREATE INDEX ix_monthly_attendance_project_id ON timesheet.monthly_attendance(project_id);
CREATE INDEX ix_monthly_attendance_year_month ON timesheet.monthly_attendance(year, month);
CREATE INDEX ix_monthly_attendance_status ON timesheet.monthly_attendance(status);

-- 日次勤怠テーブル
CREATE TABLE timesheet.daily_attendance (
    id BIGSERIAL PRIMARY KEY,
    monthly_attendance_id BIGINT NOT NULL REFERENCES timesheet.monthly_attendance(id),
    engineer_id BIGINT NOT NULL REFERENCES engineer.engineer(id),
    project_id BIGINT REFERENCES project.project(id),
    attendance_date DATE NOT NULL,
    attendance_type VARCHAR(50) NOT NULL DEFAULT 'WORKING_DAY',
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    break_hours NUMERIC(4,2) NOT NULL DEFAULT 0,
    working_hours NUMERIC(4,2) NOT NULL DEFAULT 0,
    overtime_hours NUMERIC(4,2) NOT NULL DEFAULT 0,
    late_hours NUMERIC(4,2) NOT NULL DEFAULT 0,
    early_leave_hours NUMERIC(4,2) NOT NULL DEFAULT 0,
    is_modified BOOLEAN NOT NULL DEFAULT FALSE,
    modification_reason TEXT,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(engineer_id, project_id, attendance_date)
);

-- インデックス作成
CREATE INDEX ix_daily_attendance_monthly_attendance_id ON timesheet.daily_attendance(monthly_attendance_id);
CREATE INDEX ix_daily_attendance_engineer_id ON timesheet.daily_attendance(engineer_id);
CREATE INDEX ix_daily_attendance_project_id ON timesheet.daily_attendance(project_id);
CREATE INDEX ix_daily_attendance_attendance_date ON timesheet.daily_attendance(attendance_date);
CREATE INDEX ix_daily_attendance_attendance_type ON timesheet.daily_attendance(attendance_type);

-- 工数テーブル
CREATE TABLE timesheet.work_hour (
    id BIGSERIAL PRIMARY KEY,
    daily_attendance_id BIGINT NOT NULL REFERENCES timesheet.daily_attendance(id),
    engineer_id BIGINT NOT NULL REFERENCES engineer.engineer(id),
    project_id BIGINT NOT NULL REFERENCES project.project(id),
    work_date DATE NOT NULL,
    task_category VARCHAR(100),
    task_description TEXT,
    hours NUMERIC(4,2) NOT NULL,
    is_billable BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_work_hour_daily_attendance_id ON timesheet.work_hour(daily_attendance_id);
CREATE INDEX ix_work_hour_engineer_id ON timesheet.work_hour(engineer_id);
CREATE INDEX ix_work_hour_project_id ON timesheet.work_hour(project_id);
CREATE INDEX ix_work_hour_work_date ON timesheet.work_hour(work_date);
CREATE INDEX ix_work_hour_is_billable ON timesheet.work_hour(is_billable);

-- 承認フローテーブル
CREATE TABLE timesheet.approval_flow (
    id SERIAL PRIMARY KEY,
    flow_name VARCHAR(100) NOT NULL,
    flow_type VARCHAR(50) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_approval_flow_flow_type ON timesheet.approval_flow(flow_type);
CREATE INDEX ix_approval_flow_is_active ON timesheet.approval_flow(is_active);

-- 承認ステップテーブル
CREATE TABLE timesheet.approval_step (
    id SERIAL PRIMARY KEY,
    approval_flow_id INT NOT NULL REFERENCES timesheet.approval_flow(id),
    step_number INT NOT NULL,
    role_id INT REFERENCES common.roles(id),
    approver_type VARCHAR(50) NOT NULL,
    approver_id BIGINT REFERENCES common.users(id),
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(approval_flow_id, step_number)
);

-- インデックス作成
CREATE INDEX ix_approval_step_approval_flow_id ON timesheet.approval_step(approval_flow_id);
CREATE INDEX ix_approval_step_approver_id ON timesheet.approval_step(approver_id);

-- 勤怠承認テーブル
CREATE TABLE timesheet.attendance_approval (
    id BIGSERIAL PRIMARY KEY,
    monthly_attendance_id BIGINT NOT NULL REFERENCES timesheet.monthly_attendance(id),
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
CREATE INDEX ix_attendance_approval_monthly_attendance_id ON timesheet.attendance_approval(monthly_attendance_id);
CREATE INDEX ix_attendance_approval_approval_flow_id ON timesheet.attendance_approval(approval_flow_id);
CREATE INDEX ix_attendance_approval_status ON timesheet.attendance_approval(status);

-- 勤怠承認履歴テーブル
CREATE TABLE timesheet.approval_history (
    id BIGSERIAL PRIMARY KEY,
    attendance_approval_id BIGINT NOT NULL REFERENCES timesheet.attendance_approval(id),
    step_number INT NOT NULL,
    approval_step_id INT REFERENCES timesheet.approval_step(id),
    approver_id BIGINT REFERENCES common.users(id),
    action VARCHAR(50) NOT NULL,
    action_at TIMESTAMP NOT NULL,
    comments TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_approval_history_attendance_approval_id ON timesheet.approval_history(attendance_approval_id);
CREATE INDEX ix_approval_history_approver_id ON timesheet.approval_history(approver_id);
CREATE INDEX ix_approval_history_action ON timesheet.approval_history(action);

-- 勤怠通知テーブル
CREATE TABLE timesheet.attendance_notification (
    id BIGSERIAL PRIMARY KEY,
    notification_type VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL REFERENCES common.users(id),
    reference_id BIGINT,
    reference_type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_attendance_notification_notification_type ON timesheet.attendance_notification(notification_type);
CREATE INDEX ix_attendance_notification_user_id ON timesheet.attendance_notification(user_id);
CREATE INDEX ix_attendance_notification_is_read ON timesheet.attendance_notification(is_read);

-- 初期データ: 承認フロー
INSERT INTO timesheet.approval_flow (flow_name, flow_type, description, is_active) VALUES
    ('標準勤怠承認フロー', 'ATTENDANCE', '一般的な勤怠承認フロー', true),
    ('管理者のみ承認フロー', 'ATTENDANCE', '管理者のみが承認するシンプルな承認フロー', true);

-- 初期データ: 2023年の祝日（日本）
INSERT INTO timesheet.holiday (holiday_date, holiday_name, is_national_holiday, is_company_holiday) VALUES
    ('2023-01-01', '元日', true, true),
    ('2023-01-02', '振替休日', true, true),
    ('2023-01-09', '成人の日', true, true),
    ('2023-02-11', '建国記念の日', true, true),
    ('2023-02-23', '天皇誕生日', true, true),
    ('2023-03-21', '春分の日', true, true),
    ('2023-04-29', '昭和の日', true, true),
    ('2023-05-03', '憲法記念日', true, true),
    ('2023-05-04', 'みどりの日', true, true),
    ('2023-05-05', 'こどもの日', true, true),
    ('2023-07-17', '海の日', true, true),
    ('2023-08-11', '山の日', true, true),
    ('2023-09-18', '敬老の日', true, true),
    ('2023-09-23', '秋分の日', true, true),
    ('2023-10-09', 'スポーツの日', true, true),
    ('2023-11-03', '文化の日', true, true),
    ('2023-11-23', '勤労感謝の日', true, true);