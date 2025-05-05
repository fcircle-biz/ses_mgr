-- SES Management System PostgreSQL Test Data
-- 10_test_data_common.sql: 共通テストデータ（部署、ユーザー、権限など）
-- Created: 2025-05-06

-- 部署データ
INSERT INTO departments (department_id, department_name, department_code, parent_department_id, description, is_active)
VALUES 
    (1, 'システム管理部', 'SYS_ADMIN', NULL, 'システム全体の管理を担当する部署', TRUE),
    (2, '営業部', 'SALES', NULL, '営業活動を担当する部署', TRUE),
    (3, 'エンジニアリング部', 'ENG', NULL, 'エンジニアリング業務を担当する部署', TRUE),
    (4, '管理部', 'ADMIN', NULL, '一般管理業務を担当する部署', TRUE),
    (5, '人事部', 'HR', 4, '人事管理を担当する部署', TRUE),
    (6, '経理部', 'ACCOUNTING', 4, '財務・経理を担当する部署', TRUE),
    (7, 'AI・DX推進部', 'AI_DX', 3, 'AIとDXプロジェクトを担当する部署', TRUE),
    (8, 'クラウド基盤部', 'CLOUD', 3, 'クラウド基盤技術を担当する部署', TRUE),
    (9, 'Web開発部', 'WEB_DEV', 3, 'Webシステム開発を担当する部署', TRUE),
    (10, 'モバイル開発部', 'MOBILE_DEV', 3, 'モバイルアプリ開発を担当する部署', TRUE);

-- ユーザーロールデータ
INSERT INTO roles (role_id, role_code, name, description, role_type)
VALUES 
    ('11111111-1111-1111-1111-111111111111', 'ADMIN', 'システム管理者', 'システム全体の管理権限を持つロール', 'system'),
    ('22222222-2222-2222-2222-222222222222', 'MANAGER', '管理者', '業務機能の管理権限を持つロール', 'business'),
    ('33333333-3333-3333-3333-333333333333', 'SALES', '営業担当', '営業機能の利用権限を持つロール', 'business'),
    ('44444444-4444-4444-4444-444444444444', 'ENGINEER', 'エンジニア', 'エンジニア関連機能の利用権限を持つロール', 'business'),
    ('55555555-5555-5555-5555-555555555555', 'HR', '人事担当', '人事機能の利用権限を持つロール', 'business'),
    ('66666666-6666-6666-6666-666666666666', 'ACCOUNTANT', '経理担当', '経理機能の利用権限を持つロール', 'business'),
    ('77777777-7777-7777-7777-777777777777', 'USER', '一般ユーザー', '基本的な業務機能を利用するロール', 'business');

-- 権限データ
INSERT INTO permissions (permission_id, permission_code, name, description, resource_type, resource_name, action)
VALUES
    ('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 'USER_VIEW', 'ユーザー表示', 'ユーザー情報の閲覧権限', 'user', 'user', 'view'),
    ('b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', 'USER_EDIT', 'ユーザー編集', 'ユーザー情報の編集権限', 'user', 'user', 'edit'),
    ('c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3', 'USER_DELETE', 'ユーザー削除', 'ユーザー情報の削除権限', 'user', 'user', 'delete'),
    ('d4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4', 'USER_CREATE', 'ユーザー作成', 'ユーザー情報の作成権限', 'user', 'user', 'create'),
    ('e5e5e5e5-e5e5-e5e5-e5e5-e5e5e5e5e5e5', 'ROLE_VIEW', 'ロール表示', 'ロール情報の閲覧権限', 'role', 'role', 'view'),
    ('f6f6f6f6-f6f6-f6f6-f6f6-f6f6f6f6f6f6', 'ROLE_EDIT', 'ロール編集', 'ロール情報の編集権限', 'role', 'role', 'edit'),
    ('a7a7a7a7-a7a7-a7a7-a7a7-a7a7a7a7a7a7', 'ROLE_DELETE', 'ロール削除', 'ロール情報の削除権限', 'role', 'role', 'delete'),
    ('b8b8b8b8-b8b8-b8b8-b8b8-b8b8b8b8b8b8', 'ROLE_CREATE', 'ロール作成', 'ロール情報の作成権限', 'role', 'role', 'create'),
    ('c9c9c9c9-c9c9-c9c9-c9c9-c9c9c9c9c9c9', 'ENGINEER_VIEW', 'エンジニア表示', 'エンジニア情報の閲覧権限', 'engineer', 'engineer', 'view'),
    ('d0d0d0d0-d0d0-d0d0-d0d0-d0d0d0d0d0d0', 'ENGINEER_EDIT', 'エンジニア編集', 'エンジニア情報の編集権限', 'engineer', 'engineer', 'edit'),
    ('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'PROJECT_VIEW', 'プロジェクト表示', 'プロジェクト情報の閲覧権限', 'project', 'project', 'view'),
    ('f2f2f2f2-f2f2-f2f2-f2f2-f2f2f2f2f2f2', 'PROJECT_EDIT', 'プロジェクト編集', 'プロジェクト情報の編集権限', 'project', 'project', 'edit'),
    ('a3a3a3a3-a3a3-a3a3-a3a3-a3a3a3a3a3a3', 'MATCHING_VIEW', 'マッチング表示', 'マッチング情報の閲覧権限', 'matching', 'matching', 'view'),
    ('b4b4b4b4-b4b4-b4b4-b4b4-b4b4b4b4b4b4', 'MATCHING_EDIT', 'マッチング編集', 'マッチング情報の編集権限', 'matching', 'matching', 'edit'),
    ('c5c5c5c5-c5c5-c5c5-c5c5-c5c5c5c5c5c5', 'REPORT_VIEW', 'レポート表示', 'レポート情報の閲覧権限', 'report', 'report', 'view'),
    ('d6d6d6d6-d6d6-d6d6-d6d6-d6d6d6d6d6d6', 'DASHBOARD_VIEW', 'ダッシュボード表示', 'ダッシュボード情報の閲覧権限', 'dashboard', 'dashboard', 'view');

-- ロール権限マッピング
INSERT INTO role_permissions (role_id, permission_id, access_level)
VALUES
    -- システム管理者の権限
    ('11111111-1111-1111-1111-111111111111', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 'write'),
    ('11111111-1111-1111-1111-111111111111', 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', 'write'),
    ('11111111-1111-1111-1111-111111111111', 'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3', 'write'),
    ('11111111-1111-1111-1111-111111111111', 'd4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4', 'write'),
    ('11111111-1111-1111-1111-111111111111', 'e5e5e5e5-e5e5-e5e5-e5e5-e5e5e5e5e5e5', 'write'),
    ('11111111-1111-1111-1111-111111111111', 'f6f6f6f6-f6f6-f6f6-f6f6-f6f6f6f6f6f6', 'write'),
    ('11111111-1111-1111-1111-111111111111', 'a7a7a7a7-a7a7-a7a7-a7a7-a7a7a7a7a7a7', 'write'),
    ('11111111-1111-1111-1111-111111111111', 'b8b8b8b8-b8b8-b8b8-b8b8-b8b8b8b8b8b8', 'write'),
    ('11111111-1111-1111-1111-111111111111', 'c9c9c9c9-c9c9-c9c9-c9c9-c9c9c9c9c9c9', 'write'),
    ('11111111-1111-1111-1111-111111111111', 'd0d0d0d0-d0d0-d0d0-d0d0-d0d0d0d0d0d0', 'write'),
    ('11111111-1111-1111-1111-111111111111', 'e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'write'),
    ('11111111-1111-1111-1111-111111111111', 'f2f2f2f2-f2f2-f2f2-f2f2-f2f2f2f2f2f2', 'write'),
    ('11111111-1111-1111-1111-111111111111', 'a3a3a3a3-a3a3-a3a3-a3a3-a3a3a3a3a3a3', 'write'),
    ('11111111-1111-1111-1111-111111111111', 'b4b4b4b4-b4b4-b4b4-b4b4-b4b4b4b4b4b4', 'write'),
    ('11111111-1111-1111-1111-111111111111', 'c5c5c5c5-c5c5-c5c5-c5c5-c5c5c5c5c5c5', 'write'),
    ('11111111-1111-1111-1111-111111111111', 'd6d6d6d6-d6d6-d6d6-d6d6-d6d6d6d6d6d6', 'write'),
    
    -- 管理者の権限
    ('22222222-2222-2222-2222-222222222222', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 'write'),
    ('22222222-2222-2222-2222-222222222222', 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', 'write'),
    ('22222222-2222-2222-2222-222222222222', 'd4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4', 'write'),
    ('22222222-2222-2222-2222-222222222222', 'e5e5e5e5-e5e5-e5e5-e5e5-e5e5e5e5e5e5', 'write'),
    ('22222222-2222-2222-2222-222222222222', 'c9c9c9c9-c9c9-c9c9-c9c9-c9c9c9c9c9c9', 'write'),
    ('22222222-2222-2222-2222-222222222222', 'd0d0d0d0-d0d0-d0d0-d0d0-d0d0d0d0d0d0', 'write'),
    ('22222222-2222-2222-2222-222222222222', 'e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'write'),
    ('22222222-2222-2222-2222-222222222222', 'f2f2f2f2-f2f2-f2f2-f2f2-f2f2f2f2f2f2', 'write'),
    ('22222222-2222-2222-2222-222222222222', 'a3a3a3a3-a3a3-a3a3-a3a3-a3a3a3a3a3a3', 'write'),
    ('22222222-2222-2222-2222-222222222222', 'b4b4b4b4-b4b4-b4b4-b4b4-b4b4b4b4b4b4', 'write'),
    ('22222222-2222-2222-2222-222222222222', 'c5c5c5c5-c5c5-c5c5-c5c5-c5c5c5c5c5c5', 'write'),
    ('22222222-2222-2222-2222-222222222222', 'd6d6d6d6-d6d6-d6d6-d6d6-d6d6d6d6d6d6', 'write'),
    
    -- 営業担当の権限
    ('33333333-3333-3333-3333-333333333333', 'c9c9c9c9-c9c9-c9c9-c9c9-c9c9c9c9c9c9', 'read'),
    ('33333333-3333-3333-3333-333333333333', 'e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'write'),
    ('33333333-3333-3333-3333-333333333333', 'f2f2f2f2-f2f2-f2f2-f2f2-f2f2f2f2f2f2', 'write'),
    ('33333333-3333-3333-3333-333333333333', 'a3a3a3a3-a3a3-a3a3-a3a3-a3a3a3a3a3a3', 'write'),
    ('33333333-3333-3333-3333-333333333333', 'b4b4b4b4-b4b4-b4b4-b4b4-b4b4b4b4b4b4', 'write'),
    ('33333333-3333-3333-3333-333333333333', 'c5c5c5c5-c5c5-c5c5-c5c5-c5c5c5c5c5c5', 'read'),
    ('33333333-3333-3333-3333-333333333333', 'd6d6d6d6-d6d6-d6d6-d6d6-d6d6d6d6d6d6', 'read'),
    
    -- エンジニアの権限
    ('44444444-4444-4444-4444-444444444444', 'c9c9c9c9-c9c9-c9c9-c9c9-c9c9c9c9c9c9', 'read'),
    ('44444444-4444-4444-4444-444444444444', 'e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'read'),
    ('44444444-4444-4444-4444-444444444444', 'a3a3a3a3-a3a3-a3a3-a3a3-a3a3a3a3a3a3', 'read'),
    
    -- 人事担当の権限
    ('55555555-5555-5555-5555-555555555555', 'c9c9c9c9-c9c9-c9c9-c9c9-c9c9c9c9c9c9', 'write'),
    ('55555555-5555-5555-5555-555555555555', 'd0d0d0d0-d0d0-d0d0-d0d0-d0d0d0d0d0d0', 'write'),
    ('55555555-5555-5555-5555-555555555555', 'c5c5c5c5-c5c5-c5c5-c5c5-c5c5c5c5c5c5', 'read'),

    -- 経理担当の権限
    ('66666666-6666-6666-6666-666666666666', 'e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'read'),
    ('66666666-6666-6666-6666-666666666666', 'c5c5c5c5-c5c5-c5c5-c5c5-c5c5c5c5c5c5', 'write'),
    
    -- 一般ユーザーの権限
    ('77777777-7777-7777-7777-777777777777', 'c9c9c9c9-c9c9-c9c9-c9c9-c9c9c9c9c9c9', 'read'),
    ('77777777-7777-7777-7777-777777777777', 'e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'read'),
    ('77777777-7777-7777-7777-777777777777', 'a3a3a3a3-a3a3-a3a3-a3a3-a3a3a3a3a3a3', 'read'),
    ('77777777-7777-7777-7777-777777777777', 'd6d6d6d6-d6d6-d6d6-d6d6-d6d6d6d6d6d6', 'read');

-- ユーザーデータ
INSERT INTO users (user_id, login_id, email, name, department_id, position, password_hash)
VALUES
    ('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 'admin', 'admin@example.com', 'システム管理者', 1, 'システム管理者', crypt('password', gen_salt('bf'))),
    ('b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', 'yamada', 'yamada@example.com', '山田太郎', 2, '営業部長', crypt('password', gen_salt('bf'))),
    ('c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3', 'tanaka', 'tanaka@example.com', '田中次郎', 3, 'エンジニアリング部長', crypt('password', gen_salt('bf'))),
    ('d4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4', 'suzuki', 'suzuki@example.com', '鈴木三郎', 4, '管理部長', crypt('password', gen_salt('bf'))),
    ('e5e5e5e5-e5e5-e5e5-e5e5-e5e5e5e5e5e5', 'sato', 'sato@example.com', '佐藤四郎', 5, '人事部長', crypt('password', gen_salt('bf'))),
    ('f6f6f6f6-f6f6-f6f6-f6f6-f6f6f6f6f6f6', 'watanabe', 'watanabe@example.com', '渡辺五郎', 6, '経理部長', crypt('password', gen_salt('bf'))),
    ('a7a7a7a7-a7a7-a7a7-a7a7-a7a7a7a7a7a7', 'ito', 'ito@example.com', '伊藤六郎', 7, 'AI・DX推進部長', crypt('password', gen_salt('bf'))),
    ('b8b8b8b8-b8b8-b8b8-b8b8-b8b8b8b8b8b8', 'kato', 'kato@example.com', '加藤七郎', 8, 'クラウド基盤部長', crypt('password', gen_salt('bf'))),
    ('c9c9c9c9-c9c9-c9c9-c9c9-c9c9c9c9c9c9', 'yoshida', 'yoshida@example.com', '吉田八郎', 9, 'Web開発部長', crypt('password', gen_salt('bf'))),
    ('d0d0d0d0-d0d0-d0d0-d0d0-d0d0d0d0d0d0', 'yamamoto', 'yamamoto@example.com', '山本九郎', 10, 'モバイル開発部長', crypt('password', gen_salt('bf')));

-- ユーザーロール割り当て
INSERT INTO user_roles (user_id, role_id, assigned_by)
VALUES
    ('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', '11111111-1111-1111-1111-111111111111', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    ('b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', '33333333-3333-3333-3333-333333333333', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    ('b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', '22222222-2222-2222-2222-222222222222', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    ('c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3', '44444444-4444-4444-4444-444444444444', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    ('c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3', '22222222-2222-2222-2222-222222222222', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    ('d4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4', '22222222-2222-2222-2222-222222222222', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    ('e5e5e5e5-e5e5-e5e5-e5e5-e5e5e5e5e5e5', '55555555-5555-5555-5555-555555555555', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    ('f6f6f6f6-f6f6-f6f6-f6f6-f6f6f6f6f6f6', '66666666-6666-6666-6666-666666666666', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    ('a7a7a7a7-a7a7-a7a7-a7a7-a7a7a7a7a7a7', '44444444-4444-4444-4444-444444444444', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    ('b8b8b8b8-b8b8-b8b8-b8b8-b8b8b8b8b8b8', '44444444-4444-4444-4444-444444444444', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    ('c9c9c9c9-c9c9-c9c9-c9c9-c9c9c9c9c9c9', '44444444-4444-4444-4444-444444444444', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    ('d0d0d0d0-d0d0-d0d0-d0d0-d0d0d0d0d0d0', '44444444-4444-4444-4444-444444444444', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1');

-- 祝日データ
INSERT INTO holidays (holiday_date, holiday_name, holiday_type)
VALUES
    ('2025-01-01', '元日', '祝日'),
    ('2025-01-13', '成人の日', '祝日'),
    ('2025-02-11', '建国記念の日', '祝日'),
    ('2025-02-23', '天皇誕生日', '祝日'),
    ('2025-03-20', '春分の日', '祝日'),
    ('2025-04-29', '昭和の日', '祝日'),
    ('2025-05-03', '憲法記念日', '祝日'),
    ('2025-05-04', 'みどりの日', '祝日'),
    ('2025-05-05', 'こどもの日', '祝日'),
    ('2025-07-21', '海の日', '祝日'),
    ('2025-08-11', '山の日', '祝日'),
    ('2025-09-15', '敬老の日', '祝日'),
    ('2025-09-23', '秋分の日', '祝日'),
    ('2025-10-13', 'スポーツの日', '祝日'),
    ('2025-11-03', '文化の日', '祝日'),
    ('2025-11-23', '勤労感謝の日', '祝日'),
    ('2025-12-29', '年末休業', '会社休業日'),
    ('2025-12-30', '年末休業', '会社休業日'),
    ('2025-12-31', '年末休業', '会社休業日');

-- ユーザーロール履歴
INSERT INTO user_role_history (history_id, user_id, role_id, operation_type, performed_by)
VALUES
    (uuid_generate_v4(), 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', '11111111-1111-1111-1111-111111111111', 'assign', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (uuid_generate_v4(), 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', '33333333-3333-3333-3333-333333333333', 'assign', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (uuid_generate_v4(), 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', '22222222-2222-2222-2222-222222222222', 'assign', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (uuid_generate_v4(), 'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3', '44444444-4444-4444-4444-444444444444', 'assign', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1'),
    (uuid_generate_v4(), 'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3', '22222222-2222-2222-2222-222222222222', 'assign', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1');

-- 監査ログのサンプル
INSERT INTO audit_logs (table_name, action_type, record_id, old_data, new_data, changed_by, ip_address, performed_at)
VALUES
    ('users', 'INSERT', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', NULL, '{"name": "システム管理者", "email": "admin@example.com"}', 'SYSTEM', '127.0.0.1', '2025-05-01 10:00:00'),
    ('departments', 'INSERT', '1', NULL, '{"department_name": "システム管理部", "department_code": "SYS_ADMIN"}', 'SYSTEM', '127.0.0.1', '2025-05-01 10:01:00'),
    ('users', 'UPDATE', 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', '{"name": "山田一郎", "position": "営業担当"}', '{"name": "山田太郎", "position": "営業部長"}', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', '192.168.1.100', '2025-05-02 11:30:00'),
    ('user_roles', 'INSERT', 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2-22222222-2222-2222-2222-222222222222', NULL, '{"user_id": "b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2", "role_id": "22222222-2222-2222-2222-222222222222"}', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', '192.168.1.100', '2025-05-02 11:35:00');