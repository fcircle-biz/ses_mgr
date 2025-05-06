-- マスタータイプの登録
INSERT INTO master_type (id, type_code, type_name_ja, type_name_en, description, attribute_definition, is_active, is_system_reserved, created_by, created_at, updated_by, updated_at)
VALUES 
(1, 'job_type', '職種', 'Job Type', 'エンジニアの職種区分', '[]', true, true, 1, NOW(), 1, NOW()),
(2, 'skill_category', 'スキルカテゴリ', 'Skill Category', '技術スキルのカテゴリ', '[]', true, true, 1, NOW(), 1, NOW()),
(3, 'project_status', '案件ステータス', 'Project Status', '案件の進行状況', '[]', true, true, 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;

-- マスタデータの登録
-- 職種データ
INSERT INTO master_data (id, master_type_id, code, name_ja, name_en, description, display_order, parent_id, attributes, is_active, is_system_reserved, created_by, created_at, updated_by, updated_at)
VALUES 
(1, 1, 'PM', 'プロジェクトマネージャ', 'Project Manager', 'プロジェクト全体の管理を担当', 1, NULL, '{}', true, true, 1, NOW(), 1, NOW()),
(2, 1, 'SE', 'システムエンジニア', 'System Engineer', 'システム設計・開発を担当', 2, NULL, '{}', true, true, 1, NOW(), 1, NOW()),
(3, 1, 'PG', 'プログラマ', 'Programmer', 'プログラミング・実装を担当', 3, NULL, '{}', true, true, 1, NOW(), 1, NOW()),
(4, 1, 'TL', 'テクニカルリード', 'Technical Lead', '技術面のリーダーシップを担当', 4, NULL, '{}', true, true, 1, NOW(), 1, NOW()),
(5, 1, 'QA', 'テスター', 'QA Engineer', '品質保証・テストを担当', 5, NULL, '{}', true, true, 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;

-- スキルカテゴリデータ
INSERT INTO master_data (id, master_type_id, code, name_ja, name_en, description, display_order, parent_id, attributes, is_active, is_system_reserved, created_by, created_at, updated_by, updated_at)
VALUES 
(6, 2, 'prog_lang', 'プログラミング言語', 'Programming Languages', 'プログラミング言語に関するスキル', 1, NULL, '{}', true, true, 1, NOW(), 1, NOW()),
(7, 2, 'framework', 'フレームワーク', 'Frameworks', '開発フレームワークに関するスキル', 2, NULL, '{}', true, true, 1, NOW(), 1, NOW()),
(8, 2, 'db', 'データベース', 'Databases', 'データベース関連のスキル', 3, NULL, '{}', true, true, 1, NOW(), 1, NOW()),
(9, 2, 'cloud', 'クラウド', 'Cloud Technologies', 'クラウド技術に関するスキル', 4, NULL, '{}', true, true, 1, NOW(), 1, NOW()),
(10, 2, 'java', 'Java', 'Java', 'Javaプログラミング', 1, 6, '{}', true, true, 1, NOW(), 1, NOW()),
(11, 2, 'python', 'Python', 'Python', 'Pythonプログラミング', 2, 6, '{}', true, true, 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;

-- 案件ステータスデータ
INSERT INTO master_data (id, master_type_id, code, name_ja, name_en, description, display_order, parent_id, attributes, is_active, is_system_reserved, created_by, created_at, updated_by, updated_at)
VALUES 
(12, 3, 'open', '募集中', 'Open', '技術者を募集中の案件', 1, NULL, '{}', true, true, 1, NOW(), 1, NOW()),
(13, 3, 'filled', '成約済', 'Filled', '技術者が決定した案件', 2, NULL, '{}', true, true, 1, NOW(), 1, NOW()),
(14, 3, 'in_progress', '進行中', 'In Progress', '実行中の案件', 3, NULL, '{}', true, true, 1, NOW(), 1, NOW()),
(15, 3, 'completed', '完了', 'Completed', '完了した案件', 4, NULL, '{}', true, true, 1, NOW(), 1, NOW()),
(16, 3, 'cancelled', 'キャンセル', 'Cancelled', 'キャンセルされた案件', 5, NULL, '{}', true, true, 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;