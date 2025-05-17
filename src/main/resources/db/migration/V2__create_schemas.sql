-- スキーマ作成
CREATE SCHEMA common;
CREATE SCHEMA engineer;
CREATE SCHEMA project;
CREATE SCHEMA matching;
CREATE SCHEMA contract;
CREATE SCHEMA timesheet;
CREATE SCHEMA billing;
CREATE SCHEMA reporting;
CREATE SCHEMA audit;

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

-- シーケンス更新
ALTER SEQUENCE users_id_seq SET SCHEMA common;
ALTER SEQUENCE roles_id_seq SET SCHEMA common;
ALTER SEQUENCE permissions_id_seq SET SCHEMA common;
ALTER SEQUENCE code_categories_id_seq SET SCHEMA common;
ALTER SEQUENCE code_values_id_seq SET SCHEMA common;
ALTER SEQUENCE file_categories_id_seq SET SCHEMA common;
ALTER SEQUENCE files_id_seq SET SCHEMA common;
ALTER SEQUENCE notification_templates_id_seq SET SCHEMA common;
ALTER SEQUENCE notifications_id_seq SET SCHEMA common;
ALTER SEQUENCE audit_logs_id_seq SET SCHEMA audit;