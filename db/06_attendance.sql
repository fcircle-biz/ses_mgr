-- SES Management System PostgreSQL Schema
-- 06_attendance.sql: 勤怠・工数管理テーブル
-- Created: 2025-05-06

-- ============================
-- ATTENDANCE AND TIME MANAGEMENT
-- ============================

CREATE TABLE IF NOT EXISTS monthly_attendances (
    monthly_attendance_id SERIAL PRIMARY KEY,
    engineer_id INTEGER NOT NULL REFERENCES engineers(engineer_id),
    project_id INTEGER REFERENCES projects(project_id),
    target_year INTEGER NOT NULL CHECK (target_year >= 2000 AND target_year <= 2100),
    target_month INTEGER NOT NULL CHECK (target_month >= 1 AND target_month <= 12),
    contracted_working_days INTEGER NOT NULL DEFAULT 0,
    working_days INTEGER NOT NULL DEFAULT 0,
    absence_days INTEGER NOT NULL DEFAULT 0,
    paid_leave_days NUMERIC(3,1) NOT NULL DEFAULT 0,
    half_day_leave_count INTEGER NOT NULL DEFAULT 0,
    sick_leave_days NUMERIC(3,1) NOT NULL DEFAULT 0,
    special_leave_days NUMERIC(3,1) NOT NULL DEFAULT 0,
    late_arrivals INTEGER NOT NULL DEFAULT 0,
    early_departures INTEGER NOT NULL DEFAULT 0,
    total_working_hours NUMERIC(5,2) NOT NULL DEFAULT 0,
    regular_hours NUMERIC(5,2) NOT NULL DEFAULT 0,
    overtime_hours NUMERIC(5,2) NOT NULL DEFAULT 0,
    night_work_hours NUMERIC(5,2) NOT NULL DEFAULT 0,
    holiday_work_hours NUMERIC(5,2) NOT NULL DEFAULT 0,
    midnight_overtime_hours NUMERIC(5,2) NOT NULL DEFAULT 0,
    status TEXT NOT NULL CHECK (status IN ('作成中', '提出済', '承認中', '承認済', '差戻', '確定')) DEFAULT '作成中',
    remarks TEXT,
    submitted_at TIMESTAMP,
    submitted_by UUID REFERENCES users(user_id),
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (engineer_id, project_id, target_year, target_month)
);

CREATE INDEX IF NOT EXISTS idx_monthly_attendances_engineer_id ON monthly_attendances(engineer_id);
CREATE INDEX IF NOT EXISTS idx_monthly_attendances_project_id ON monthly_attendances(project_id);
CREATE INDEX IF NOT EXISTS idx_monthly_attendances_year_month ON monthly_attendances(target_year, target_month);
CREATE INDEX IF NOT EXISTS idx_monthly_attendances_status ON monthly_attendances(status);
CREATE INDEX IF NOT EXISTS idx_monthly_attendances_submitted_at ON monthly_attendances(submitted_at);

CREATE TRIGGER update_monthly_attendances_timestamp
BEFORE UPDATE ON monthly_attendances
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS daily_attendances (
    daily_attendance_id SERIAL PRIMARY KEY,
    monthly_attendance_id INTEGER NOT NULL REFERENCES monthly_attendances(monthly_attendance_id),
    attendance_date DATE NOT NULL,
    day_type TEXT NOT NULL CHECK (day_type IN ('平日', '休日', '祝日')) DEFAULT '平日',
    start_time TIME,
    end_time TIME,
    break_time NUMERIC(3,1) NOT NULL DEFAULT 1.0,
    working_hours NUMERIC(4,2) NOT NULL DEFAULT 0,
    regular_hours NUMERIC(4,2) NOT NULL DEFAULT 0,
    overtime_hours NUMERIC(4,2) NOT NULL DEFAULT 0,
    night_work_hours NUMERIC(4,2) NOT NULL DEFAULT 0,
    holiday_work_hours NUMERIC(4,2) NOT NULL DEFAULT 0,
    midnight_overtime_hours NUMERIC(4,2) NOT NULL DEFAULT 0,
    attendance_type TEXT NOT NULL CHECK (attendance_type IN ('出勤', '欠勤', '有給休暇', '半休', '特別休暇', 'リモート勤務', '休日', '振替休日', '代休', '産休・育休', 'その他')) DEFAULT '出勤',
    location_type TEXT NOT NULL CHECK (location_type IN ('客先', '社内', 'リモート', 'その他')) DEFAULT '客先',
    location_details TEXT,
    approval_status TEXT CHECK (approval_status IN ('未承認', '承認済', '差戻')) DEFAULT '未承認',
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (monthly_attendance_id, attendance_date)
);

CREATE INDEX IF NOT EXISTS idx_daily_attendances_monthly_attendance_id ON daily_attendances(monthly_attendance_id);
CREATE INDEX IF NOT EXISTS idx_daily_attendances_attendance_date ON daily_attendances(attendance_date);
CREATE INDEX IF NOT EXISTS idx_daily_attendances_attendance_type ON daily_attendances(attendance_type);
CREATE INDEX IF NOT EXISTS idx_daily_attendances_approval_status ON daily_attendances(approval_status);

CREATE TRIGGER update_daily_attendances_timestamp
BEFORE UPDATE ON daily_attendances
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS work_hours (
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

CREATE INDEX IF NOT EXISTS idx_work_hours_daily_attendance_id ON work_hours(daily_attendance_id);
CREATE INDEX IF NOT EXISTS idx_work_hours_project_id ON work_hours(project_id);
CREATE INDEX IF NOT EXISTS idx_work_hours_task_category ON work_hours(task_category);
CREATE INDEX IF NOT EXISTS idx_work_hours_billable ON work_hours(billable);

CREATE TRIGGER update_work_hours_timestamp
BEFORE UPDATE ON work_hours
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS approval_flows (
    flow_id SERIAL PRIMARY KEY,
    monthly_attendance_id INTEGER NOT NULL REFERENCES monthly_attendances(monthly_attendance_id),
    current_step INTEGER NOT NULL DEFAULT 1 CHECK (current_step >= 0),
    flow_type TEXT NOT NULL CHECK (flow_type IN ('標準', '緊急', '特別')) DEFAULT '標準',
    status TEXT NOT NULL CHECK (status IN ('申請中', '承認中', '差戻', '完了', 'キャンセル')) DEFAULT '申請中',
    remarks TEXT,
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (monthly_attendance_id)
);

CREATE INDEX IF NOT EXISTS idx_approval_flows_monthly_attendance_id ON approval_flows(monthly_attendance_id);
CREATE INDEX IF NOT EXISTS idx_approval_flows_status ON approval_flows(status);
CREATE INDEX IF NOT EXISTS idx_approval_flows_created_by ON approval_flows(created_by);

CREATE TRIGGER update_approval_flows_timestamp
BEFORE UPDATE ON approval_flows
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS approval_steps (
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

CREATE INDEX IF NOT EXISTS idx_approval_steps_flow_id ON approval_steps(flow_id);
CREATE INDEX IF NOT EXISTS idx_approval_steps_approver_id ON approval_steps(approver_id);
CREATE INDEX IF NOT EXISTS idx_approval_steps_status ON approval_steps(status);
CREATE INDEX IF NOT EXISTS idx_approval_steps_approved_at ON approval_steps(approved_at);

CREATE TRIGGER update_approval_steps_timestamp
BEFORE UPDATE ON approval_steps
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS paid_leave_allocations (
    allocation_id SERIAL PRIMARY KEY,
    engineer_id INTEGER NOT NULL REFERENCES engineers(engineer_id),
    allocation_year INTEGER NOT NULL CHECK (allocation_year >= 2000 AND allocation_year <= 2100),
    total_days NUMERIC(3,1) NOT NULL,
    used_days NUMERIC(3,1) NOT NULL DEFAULT 0,
    remaining_days NUMERIC(3,1) NOT NULL,
    valid_from DATE NOT NULL,
    valid_until DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (engineer_id, allocation_year)
);

CREATE INDEX IF NOT EXISTS idx_paid_leave_allocations_engineer_id ON paid_leave_allocations(engineer_id);
CREATE INDEX IF NOT EXISTS idx_paid_leave_allocations_valid_from ON paid_leave_allocations(valid_from);
CREATE INDEX IF NOT EXISTS idx_paid_leave_allocations_valid_until ON paid_leave_allocations(valid_until);

CREATE TRIGGER update_paid_leave_allocations_timestamp
BEFORE UPDATE ON paid_leave_allocations
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS leave_requests (
    request_id SERIAL PRIMARY KEY,
    engineer_id INTEGER NOT NULL REFERENCES engineers(engineer_id),
    leave_type TEXT NOT NULL CHECK (leave_type IN ('有給休暇', '半休', '特別休暇', '代休', '産休・育休', '介護休暇', 'その他')) DEFAULT '有給休暇',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    start_half TEXT CHECK (start_half IN ('終日', '午前', '午後')),
    end_half TEXT CHECK (end_half IN ('終日', '午前', '午後')),
    total_days NUMERIC(3,1) NOT NULL,
    reason TEXT,
    status TEXT NOT NULL CHECK (status IN ('申請中', '承認済', '差戻', 'キャンセル')) DEFAULT '申請中',
    approver_id UUID REFERENCES users(user_id),
    approved_at TIMESTAMP,
    approver_comments TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_leave_requests_engineer_id ON leave_requests(engineer_id);
CREATE INDEX IF NOT EXISTS idx_leave_requests_start_date ON leave_requests(start_date);
CREATE INDEX IF NOT EXISTS idx_leave_requests_end_date ON leave_requests(end_date);
CREATE INDEX IF NOT EXISTS idx_leave_requests_status ON leave_requests(status);
CREATE INDEX IF NOT EXISTS idx_leave_requests_leave_type ON leave_requests(leave_type);

CREATE TRIGGER update_leave_requests_timestamp
BEFORE UPDATE ON leave_requests
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TABLE monthly_attendances IS '月次勤怠情報を管理するテーブル';
COMMENT ON TABLE daily_attendances IS '日次勤怠情報を管理するテーブル';
COMMENT ON TABLE work_hours IS '日々の工数情報を管理するテーブル';
COMMENT ON TABLE approval_flows IS '勤怠や工数のワークフロー情報を管理するテーブル';
COMMENT ON TABLE approval_steps IS 'ワークフローの承認ステップを管理するテーブル';
COMMENT ON TABLE paid_leave_allocations IS '有給休暇の付与・使用状況を管理するテーブル';
COMMENT ON TABLE leave_requests IS '休暇申請を管理するテーブル';