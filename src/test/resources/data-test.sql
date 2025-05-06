-- テストデータスクリプト（H2テスト用）

-- 部署のテストデータ
INSERT INTO departments (department_id, department_name, department_code, description, is_active, created_at, updated_at)
VALUES 
(1, 'Test Department', 'TEST', 'Test department for testing', true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- ロールのテストデータ
INSERT INTO roles (role_id, role_code, name, description, role_type, created_at, updated_at)
VALUES 
('00000000-0000-0000-0000-000000000101', 'USER', '一般ユーザー', '標準的なユーザー権限を持つロール', 'system', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('00000000-0000-0000-0000-000000000102', 'ADMIN', '管理者', 'システム管理者権限を持つロール', 'system', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- パーミッションのテストデータ
INSERT INTO permissions (permission_id, permission_code, name, description, permission_type, created_at, updated_at)
VALUES 
(1, 'system.logs.read', 'ログ閲覧', 'システムログの閲覧権限', 'system', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'system.logs.export', 'ログ出力', 'システムログの出力権限', 'system', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 'system.users.manage', 'ユーザー管理', 'ユーザーの管理権限', 'system', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 'system.roles.manage', 'ロール管理', 'ロールの管理権限', 'system', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, 'common.notifications.read', '通知閲覧', '通知の閲覧権限', 'common', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- ロールパーミッションのテストデータ
INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
VALUES 
('00000000-0000-0000-0000-000000000101', 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('00000000-0000-0000-0000-000000000102', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('00000000-0000-0000-0000-000000000102', 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('00000000-0000-0000-0000-000000000102', 3, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('00000000-0000-0000-0000-000000000102', 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('00000000-0000-0000-0000-000000000102', 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- ユーザーのテストデータ
INSERT INTO users (user_id, login_id, email, name, department_id, position, phone, password_hash, mfa_enabled, status, login_attempts, created_at, updated_at)
VALUES 
('00000000-0000-0000-0000-000000000001', 'testuser', 'testuser@example.com', 'Test User', 1, 'Developer', '123-456-7890', '$2a$10$n/dEGdVkV86C/b3AjIE0v.vjHBbTLL1EKBJKZCfxSQTmCLtSx5LbG', false, 'active', 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('00000000-0000-0000-0000-000000000002', 'admin', 'admin@example.com', 'Admin User', 1, 'System Administrator', '123-456-7890', '$2a$10$n/dEGdVkV86C/b3AjIE0v.vjHBbTLL1EKBJKZCfxSQTmCLtSx5LbG', false, 'active', 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- ユーザーロールのテストデータ
INSERT INTO user_roles (user_id, role_id, assigned_at, created_at, updated_at)
VALUES 
('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000101', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000102', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());