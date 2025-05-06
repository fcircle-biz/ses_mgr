-- 通知用テーブルが存在しない場合は作成
CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(36) PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    metadata JSONB,
    sender_id BIGINT,
    sender_name VARCHAR(100),
    recipient_id BIGINT NOT NULL,
    recipient_name VARCHAR(100),
    importance VARCHAR(10),
    action_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

-- テスト用通知データを挿入
INSERT INTO notifications (
    id, type, title, body, is_read, metadata, 
    sender_id, sender_name, recipient_id, recipient_name, 
    importance, action_url, created_at, updated_at, deleted_at
) VALUES 
-- システム通知（未読）
('11111111-1111-1111-1111-111111111111', 'SYSTEM', 'メンテナンスのお知らせ', 'システムメンテナンスのため、2023年5月5日 22:00から2023年5月6日 2:00までサービスを停止いたします。', false, 
 '{"maintenance_id": "123456", "severity": "info", "affected_services": ["matching", "billing"], "start_time": "2023-05-05T22:00:00.000Z", "end_time": "2023-05-06T02:00:00.000Z"}',
 1, '管理者', 1, 'テストユーザー', 
 'medium', '/maintenance/123456', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', null),

-- タスク通知（未読、重要度高）
('22222222-2222-2222-2222-222222222222', 'TASK', '新しい案件の提案依頼があります', '株式会社テクノロジーの案件「Javaエンジニア募集」に対して提案依頼がありました。\n\n詳細を確認し、適切な技術者を選定して提案してください。', false, 
 '{"task_id": "task123", "project_id": "proj456", "project_name": "Javaエンジニア募集", "client_name": "株式会社テクノロジー", "deadline": "2023-05-10T17:00:00.000Z", "required_skills": ["Java", "Spring", "MySQL"], "importance": "high", "action_url": "/projects/proj456"}',
 2, '営業担当 佐藤', 1, 'テストユーザー', 
 'high', '/projects/proj456', NOW() - INTERVAL '12 hours', NOW() - INTERVAL '12 hours', null),

-- アラート通知（未読、緊急）
('33333333-3333-3333-3333-333333333333', 'ALERT', '契約の有効期限が近づいています', '株式会社ABCとの契約「システム開発支援」の有効期限が10日後に迫っています。契約更新の検討をお願いします。', false, 
 '{"alert_type": "contract_expiry", "contract_id": "c123456", "contract_name": "システム開発支援", "client_name": "株式会社ABC", "expiry_date": "2023-05-10", "days_remaining": 10, "severity": "warning", "action_url": "/contracts/c123456"}',
 3, 'システム', 1, 'テストユーザー', 
 'high', '/contracts/c123456', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', null),

-- イベント通知（既読）
('44444444-4444-4444-4444-444444444444', 'EVENT', '技術者がプロジェクトにアサインされました', '鈴木一郎さんが「基幹システム開発」プロジェクトにアサインされました。開始日は2023年6月1日です。', true, 
 '{"event_type": "engineer_assigned", "engineer_id": "e123456", "engineer_name": "鈴木 一郎", "project_id": "p789012", "project_name": "基幹システム開発", "client_name": "株式会社DEF", "start_date": "2023-06-01", "action_url": "/projects/p789012"}',
 3, 'システム', 1, 'テストユーザー', 
 'medium', '/projects/p789012', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', null),

-- システム通知（既読）
('55555555-5555-5555-5555-555555555555', 'SYSTEM', 'システムアップデートのお知らせ', '新機能「プロジェクト一括管理」が追加されました。詳細はお知らせページをご確認ください。', true, 
 '{"update_id": "update789", "severity": "info", "features": ["プロジェクト一括管理", "検索機能強化"], "action_url": "/news/update789"}',
 1, '管理者', 1, 'テストユーザー', 
 'low', '/news/update789', NOW() - INTERVAL '10 days', NOW() - INTERVAL '9 days', null);

-- テスト用ユーザーが存在しない場合のみユーザーを作成
-- 注意: 実際の環境ではユーザー管理テーブルの構造に合わせて調整が必要
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM users WHERE id = 1) THEN
        INSERT INTO users (id, username, email, password, created_at, updated_at)
        VALUES (1, 'testuser', 'test@example.com', 'password', NOW(), NOW());
    END IF;
EXCEPTION
    WHEN undefined_table THEN
        -- usersテーブルが存在しない場合は何もしない
END $$;