-- マッチング設定テーブル
CREATE TABLE matching.matching_setting (
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

-- マッチング結果テーブル
CREATE TABLE matching.matching_result (
    id BIGSERIAL PRIMARY KEY,
    engineer_id BIGINT NOT NULL REFERENCES engineer.engineer(id),
    project_id BIGINT NOT NULL REFERENCES project.project(id),
    resource_requirement_id BIGINT NOT NULL REFERENCES project.resource_requirement(id),
    match_score NUMERIC(5,2) NOT NULL,
    match_details JSONB,
    status VARCHAR(50) NOT NULL DEFAULT 'SUGGESTED',
    selection_stage VARCHAR(50),
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(engineer_id, resource_requirement_id)
);

-- インデックス作成
CREATE INDEX ix_matching_result_engineer_id ON matching.matching_result(engineer_id);
CREATE INDEX ix_matching_result_project_id ON matching.matching_result(project_id);
CREATE INDEX ix_matching_result_resource_requirement_id ON matching.matching_result(resource_requirement_id);
CREATE INDEX ix_matching_result_match_score ON matching.matching_result(match_score);
CREATE INDEX ix_matching_result_status ON matching.matching_result(status);
CREATE INDEX ix_matching_result_selection_stage ON matching.matching_result(selection_stage);
CREATE INDEX ix_matching_result_is_favorite ON matching.matching_result(is_favorite);

-- 提案テーブル
CREATE TABLE matching.proposal (
    id BIGSERIAL PRIMARY KEY,
    matching_result_id BIGINT NOT NULL REFERENCES matching.matching_result(id),
    engineer_id BIGINT NOT NULL REFERENCES engineer.engineer(id),
    project_id BIGINT NOT NULL REFERENCES project.project(id),
    customer_id BIGINT NOT NULL REFERENCES project.customer(id),
    resource_requirement_id BIGINT NOT NULL REFERENCES project.resource_requirement(id),
    proposal_code VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    proposed_start_date DATE,
    proposed_end_date DATE,
    proposed_rate NUMERIC(10,0),
    proposed_rate_unit VARCHAR(50) NOT NULL DEFAULT 'MONTHLY',
    currency VARCHAR(10) NOT NULL DEFAULT 'JPY',
    skill_sheet_id BIGINT REFERENCES engineer.skill_sheet(id),
    proposal_document_id BIGINT REFERENCES common.files(id),
    notes TEXT,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_proposal_matching_result_id ON matching.proposal(matching_result_id);
CREATE INDEX ix_proposal_engineer_id ON matching.proposal(engineer_id);
CREATE INDEX ix_proposal_project_id ON matching.proposal(project_id);
CREATE INDEX ix_proposal_customer_id ON matching.proposal(customer_id);
CREATE INDEX ix_proposal_resource_requirement_id ON matching.proposal(resource_requirement_id);
CREATE INDEX ix_proposal_status ON matching.proposal(status);
CREATE INDEX ix_proposal_date_range ON matching.proposal(proposed_start_date, proposed_end_date);

-- 提案履歴テーブル
CREATE TABLE matching.proposal_history (
    id BIGSERIAL PRIMARY KEY,
    proposal_id BIGINT NOT NULL REFERENCES matching.proposal(id),
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by BIGINT REFERENCES common.users(id),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    comments TEXT
);

-- インデックス作成
CREATE INDEX ix_proposal_history_proposal_id ON matching.proposal_history(proposal_id);
CREATE INDEX ix_proposal_history_new_status ON matching.proposal_history(new_status);

-- 面談情報テーブル
CREATE TABLE matching.interview (
    id BIGSERIAL PRIMARY KEY,
    proposal_id BIGINT NOT NULL REFERENCES matching.proposal(id),
    interview_type VARCHAR(50) NOT NULL,
    interview_date TIMESTAMP NOT NULL,
    location VARCHAR(255),
    is_remote BOOLEAN NOT NULL DEFAULT FALSE,
    interview_url VARCHAR(255),
    interviewer_names TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    result VARCHAR(50),
    feedback TEXT,
    next_steps TEXT,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_interview_proposal_id ON matching.interview(proposal_id);
CREATE INDEX ix_interview_interview_date ON matching.interview(interview_date);
CREATE INDEX ix_interview_status ON matching.interview(status);
CREATE INDEX ix_interview_result ON matching.interview(result);

-- スキルマッチスコアテーブル
CREATE TABLE matching.skill_match_score (
    id BIGSERIAL PRIMARY KEY,
    matching_result_id BIGINT NOT NULL REFERENCES matching.matching_result(id),
    skill_definition_id INT NOT NULL REFERENCES engineer.skill_definition(id),
    required_level VARCHAR(50) NOT NULL,
    actual_level VARCHAR(50) NOT NULL,
    score NUMERIC(5,2) NOT NULL,
    importance VARCHAR(50) NOT NULL DEFAULT 'NORMAL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(matching_result_id, skill_definition_id)
);

-- インデックス作成
CREATE INDEX ix_skill_match_score_matching_result_id ON matching.skill_match_score(matching_result_id);
CREATE INDEX ix_skill_match_score_skill_definition_id ON matching.skill_match_score(skill_definition_id);

-- 提案書テンプレートテーブル
CREATE TABLE matching.proposal_template (
    id SERIAL PRIMARY KEY,
    template_code VARCHAR(50) NOT NULL UNIQUE,
    template_name VARCHAR(255) NOT NULL,
    description TEXT,
    template_file_id BIGINT REFERENCES common.files(id),
    variables JSONB,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT REFERENCES common.users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES common.users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX ix_proposal_template_is_active ON matching.proposal_template(is_active);

-- 初期データ: マッチング設定
INSERT INTO matching.matching_setting (setting_key, setting_name, setting_value, data_type, description) VALUES
    ('SKILL_MATCH_WEIGHT', 'スキルマッチの重み', '0.6', 'NUMERIC', 'マッチングスコア計算でのスキルマッチの重み'),
    ('EXPERIENCE_MATCH_WEIGHT', '経験年数マッチの重み', '0.2', 'NUMERIC', 'マッチングスコア計算での経験年数マッチの重み'),
    ('AVAILABILITY_MATCH_WEIGHT', '稼働状況マッチの重み', '0.2', 'NUMERIC', 'マッチングスコア計算での稼働状況マッチの重み'),
    ('MIN_MATCH_SCORE', '最小マッチスコア', '60.0', 'NUMERIC', 'マッチング結果として表示する最小スコア'),
    ('SKILL_MATCH_EXACT', '完全一致スコア', '100.0', 'NUMERIC', 'スキルレベルが完全一致した場合のスコア'),
    ('SKILL_MATCH_ONE_LEVEL_UP', '1レベル上スコア', '80.0', 'NUMERIC', 'スキルレベルが要求より1レベル上の場合のスコア'),
    ('SKILL_MATCH_ONE_LEVEL_DOWN', '1レベル下スコア', '60.0', 'NUMERIC', 'スキルレベルが要求より1レベル下の場合のスコア'),
    ('SKILL_MATCH_TWO_LEVEL_DOWN', '2レベル下スコア', '30.0', 'NUMERIC', 'スキルレベルが要求より2レベル下の場合のスコア');