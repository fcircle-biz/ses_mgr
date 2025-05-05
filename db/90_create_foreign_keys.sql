-- SES Management System PostgreSQL Schema
-- 90_create_foreign_keys.sql: 循環参照の外部キー制約を追加
-- Created: 2025-05-06

-- 本ファイルは、循環参照を避けるために最後に実行するSQLファイルです
-- テーブル間の循環参照がある場合、外部キー制約をテーブル作成時には設定せず、
-- 全てのテーブルが作成された後にALTER TABLEで追加します

-- engineers.current_project のプロジェクト参照制約を追加
ALTER TABLE engineers 
ADD CONSTRAINT fk_engineers_current_project 
FOREIGN KEY (current_project) REFERENCES projects(project_id);

-- project_requirements.assigned_personnel のエンジニア参照制約を追加
ALTER TABLE project_requirements 
ADD CONSTRAINT fk_project_requirements_assigned_personnel 
FOREIGN KEY (assigned_personnel) REFERENCES engineers(engineer_id);

-- これらの外部キー制約が追加されることで、循環参照が適切に処理されます
COMMENT ON TABLE engineers IS 'エンジニア情報を管理するテーブル。current_projectは循環参照を避けるため、外部キー制約を後から追加しています。';
COMMENT ON TABLE project_requirements IS '案件の要員要件を管理するテーブル。assigned_personnelは循環参照を避けるため、外部キー制約を後から追加しています。';