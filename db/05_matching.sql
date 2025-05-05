-- SES Management System PostgreSQL Schema
-- 05_matching.sql: マッチングエンジンテーブル
-- Created: 2025-05-06

-- ============================
-- MATCHING ENGINE
-- ============================

CREATE TABLE IF NOT EXISTS matching (
    matching_id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(project_id),
    requirement_id INTEGER REFERENCES project_requirements(requirement_id),
    engineer_id INTEGER NOT NULL REFERENCES engineers(engineer_id),
    matching_score INTEGER NOT NULL DEFAULT 0 CHECK (matching_score BETWEEN 0 AND 100),
    algorithm_version VARCHAR(20),
    status TEXT NOT NULL CHECK (status IN ('推薦候補', '提案中', '面談調整中', '面談実施済', '内定', '契約締結', '見送り', 'キャンセル')) DEFAULT '推薦候補',
    proposed_unit_price NUMERIC(10,2),
    final_unit_price NUMERIC(10,2),
    availability_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    priority INTEGER NOT NULL DEFAULT 0 CHECK (priority BETWEEN 0 AND 10),
    matching_notes TEXT,
    sales_notes TEXT,
    engineer_feedback TEXT,
    client_feedback TEXT,
    created_by UUID REFERENCES users(user_id),
    updated_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (project_id, engineer_id, requirement_id)
);

CREATE INDEX IF NOT EXISTS idx_matching_project_id ON matching(project_id);
CREATE INDEX IF NOT EXISTS idx_matching_requirement_id ON matching(requirement_id);
CREATE INDEX IF NOT EXISTS idx_matching_engineer_id ON matching(engineer_id);
CREATE INDEX IF NOT EXISTS idx_matching_status ON matching(status);
CREATE INDEX IF NOT EXISTS idx_matching_score ON matching(matching_score);
CREATE INDEX IF NOT EXISTS idx_matching_priority ON matching(priority);
CREATE INDEX IF NOT EXISTS idx_matching_created_at ON matching(created_at);

CREATE TRIGGER update_matching_timestamp
BEFORE UPDATE ON matching
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS matching_history (
    history_id SERIAL PRIMARY KEY,
    matching_id INTEGER NOT NULL REFERENCES matching(matching_id),
    previous_status TEXT,
    new_status TEXT NOT NULL,
    action_type TEXT NOT NULL CHECK (action_type IN ('ステータス変更', '単価交渉', '面談設定', '面談結果登録', '内定通知', '見送り', 'その他')) DEFAULT 'ステータス変更',
    description TEXT,
    performed_by UUID REFERENCES users(user_id),
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_matching_history_matching_id ON matching_history(matching_id);
CREATE INDEX IF NOT EXISTS idx_matching_history_performed_at ON matching_history(performed_at);
CREATE INDEX IF NOT EXISTS idx_matching_history_performed_by ON matching_history(performed_by);
CREATE INDEX IF NOT EXISTS idx_matching_history_action_type ON matching_history(action_type);

CREATE TABLE IF NOT EXISTS skill_matching_scores (
    score_id SERIAL PRIMARY KEY,
    matching_id INTEGER NOT NULL REFERENCES matching(matching_id),
    requirement_id INTEGER REFERENCES project_requirements(requirement_id),
    skill_id INTEGER REFERENCES skills(skill_id),
    score INTEGER NOT NULL DEFAULT 0 CHECK (score BETWEEN 0 AND 100),
    weight NUMERIC(3,2) NOT NULL DEFAULT 1.00 CHECK (weight > 0),
    weighted_score NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    match_criteria TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_skill_matching_scores_matching_id ON skill_matching_scores(matching_id);
CREATE INDEX IF NOT EXISTS idx_skill_matching_scores_requirement_id ON skill_matching_scores(requirement_id);
CREATE INDEX IF NOT EXISTS idx_skill_matching_scores_skill_id ON skill_matching_scores(skill_id);
CREATE INDEX IF NOT EXISTS idx_skill_matching_scores_score ON skill_matching_scores(score);

CREATE TRIGGER update_skill_matching_scores_timestamp
BEFORE UPDATE ON skill_matching_scores
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS interview_schedules (
    interview_id SERIAL PRIMARY KEY,
    matching_id INTEGER NOT NULL REFERENCES matching(matching_id),
    interview_date TIMESTAMP NOT NULL,
    interview_round INTEGER NOT NULL DEFAULT 1,
    interview_type TEXT NOT NULL CHECK (interview_type IN ('オンライン', '訪問', '電話', '社内', 'その他')) DEFAULT 'オンライン',
    interview_tool TEXT,
    location TEXT,
    duration_minutes INTEGER DEFAULT 60,
    status TEXT NOT NULL CHECK (status IN ('予定', '実施済', 'キャンセル', '延期', '確認中')) DEFAULT '予定',
    interviewer_names TEXT,
    client_attendees TEXT,
    interview_agenda TEXT,
    special_instructions TEXT,
    notes TEXT,
    confirmation_sent BOOLEAN DEFAULT FALSE,
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_interview_schedules_matching_id ON interview_schedules(matching_id);
CREATE INDEX IF NOT EXISTS idx_interview_schedules_interview_date ON interview_schedules(interview_date);
CREATE INDEX IF NOT EXISTS idx_interview_schedules_status ON interview_schedules(status);
CREATE INDEX IF NOT EXISTS idx_interview_schedules_interview_round ON interview_schedules(interview_round);

CREATE TRIGGER update_interview_schedules_timestamp
BEFORE UPDATE ON interview_schedules
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS interview_results (
    result_id SERIAL PRIMARY KEY,
    interview_id INTEGER NOT NULL REFERENCES interview_schedules(interview_id),
    technical_evaluation INTEGER CHECK (technical_evaluation BETWEEN 1 AND 5),
    communication_evaluation INTEGER CHECK (communication_evaluation BETWEEN 1 AND 5),
    teamwork_evaluation INTEGER CHECK (teamwork_evaluation BETWEEN 1 AND 5),
    experience_evaluation INTEGER CHECK (experience_evaluation BETWEEN 1 AND 5),
    cultural_fit_evaluation INTEGER CHECK (cultural_fit_evaluation BETWEEN 1 AND 5),
    overall_evaluation INTEGER CHECK (overall_evaluation BETWEEN 1 AND 5),
    client_feedback TEXT,
    internal_feedback TEXT,
    engineer_strengths TEXT,
    engineer_weaknesses TEXT,
    decision TEXT NOT NULL CHECK (decision IN ('検討中', '採用', '不採用', '保留', '次回面談')) DEFAULT '検討中',
    decision_reason TEXT,
    proposed_start_date DATE,
    proposed_terms TEXT,
    recorded_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_interview_results_interview_id ON interview_results(interview_id);
CREATE INDEX IF NOT EXISTS idx_interview_results_decision ON interview_results(decision);
CREATE INDEX IF NOT EXISTS idx_interview_results_overall_evaluation ON interview_results(overall_evaluation);

CREATE TRIGGER update_interview_results_timestamp
BEFORE UPDATE ON interview_results
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS proposal_documents (
    proposal_id SERIAL PRIMARY KEY,
    matching_id INTEGER NOT NULL REFERENCES matching(matching_id),
    document_name VARCHAR(100) NOT NULL,
    file_path VARCHAR(255),
    file_hash VARCHAR(100),
    file_size INTEGER,
    content_type VARCHAR(100),
    version INTEGER NOT NULL DEFAULT 1,
    status TEXT CHECK (status IN ('作成中', '審査中', '承認済', '提出済', '差戻', 'その他')) DEFAULT '作成中',
    submitted_date TIMESTAMP,
    response_date TIMESTAMP,
    response_result TEXT CHECK (response_result IN ('未回答', '好評', '検討中', 'NG', 'その他')) DEFAULT '未回答',
    comments TEXT,
    created_by UUID REFERENCES users(user_id),
    updated_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_proposal_documents_matching_id ON proposal_documents(matching_id);
CREATE INDEX IF NOT EXISTS idx_proposal_documents_status ON proposal_documents(status);
CREATE INDEX IF NOT EXISTS idx_proposal_documents_submitted_date ON proposal_documents(submitted_date);
CREATE INDEX IF NOT EXISTS idx_proposal_documents_response_result ON proposal_documents(response_result);

CREATE TRIGGER update_proposal_documents_timestamp
BEFORE UPDATE ON proposal_documents
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TABLE matching IS 'エンジニアと案件のマッチング情報を管理するテーブル';
COMMENT ON TABLE matching_history IS 'マッチングプロセスの状態変化履歴を管理するテーブル';
COMMENT ON TABLE skill_matching_scores IS 'スキルごとのマッチングスコア詳細を管理するテーブル';
COMMENT ON TABLE interview_schedules IS '面談スケジュール情報を管理するテーブル';
COMMENT ON TABLE interview_results IS '面談結果情報を管理するテーブル';
COMMENT ON TABLE proposal_documents IS '提案書に関する情報を管理するテーブル';