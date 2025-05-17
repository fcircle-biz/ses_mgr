-- スキーマ作成
CREATE SCHEMA IF NOT EXISTS common;
CREATE SCHEMA IF NOT EXISTS engineer;
CREATE SCHEMA IF NOT EXISTS project;
CREATE SCHEMA IF NOT EXISTS matching;
CREATE SCHEMA IF NOT EXISTS contract;
CREATE SCHEMA IF NOT EXISTS timesheet;
CREATE SCHEMA IF NOT EXISTS billing;
CREATE SCHEMA IF NOT EXISTS reporting;
CREATE SCHEMA IF NOT EXISTS audit;

-- 既存のテーブルをcommonスキーマに移動
ALTER TABLE users SET SCHEMA common;
ALTER TABLE roles SET SCHEMA common;
ALTER TABLE user_roles SET SCHEMA common;
ALTER TABLE permissions SET SCHEMA common;
ALTER TABLE role_permissions SET SCHEMA common;
ALTER TABLE code_categories SET SCHEMA common;
ALTER TABLE code_values SET SCHEMA common;
ALTER TABLE file_categories SET SCHEMA common;
ALTER TABLE files SET SCHEMA common;
ALTER TABLE notification_templates SET SCHEMA common;
ALTER TABLE notifications SET SCHEMA common;
ALTER TABLE audit_logs SET SCHEMA audit;