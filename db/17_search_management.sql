-- ----------------------------------------------------------------------------
-- 検索機能関連テーブル定義
-- ----------------------------------------------------------------------------

-- 検索履歴テーブル
CREATE TABLE search_history (
    search_id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL COMMENT 'ユーザーID',
    query VARCHAR(255) NOT NULL COMMENT '検索キーワード',
    resource_types JSON COMMENT '検索対象リソースタイプ',
    filters JSON COMMENT '検索フィルター条件',
    sort_field VARCHAR(50) COMMENT 'ソートフィールド',
    sort_order VARCHAR(10) COMMENT 'ソート順序 (asc/desc)',
    group_by_resource_type BOOLEAN DEFAULT FALSE COMMENT 'リソースタイプでグループ化するかどうか',
    result_count INT DEFAULT 0 COMMENT '検索結果件数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    CONSTRAINT fk_search_history_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    INDEX idx_search_history_user_id (user_id),
    INDEX idx_search_history_created_at (created_at)
) COMMENT='ユーザーの検索履歴';

-- 検索インデックステーブル（全文検索用）
CREATE TABLE search_index (
    index_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id VARCHAR(36) NOT NULL COMMENT 'リソースID（UUID形式）',
    resource_type VARCHAR(50) NOT NULL COMMENT 'リソースタイプ (engineers, projects, contracts, etc...)',
    title VARCHAR(255) NOT NULL COMMENT 'リソースのタイトル',
    subtitle VARCHAR(255) COMMENT 'リソースのサブタイトル',
    description TEXT COMMENT 'リソースの説明',
    attributes JSON COMMENT 'リソースの属性（検索用）',
    url VARCHAR(255) COMMENT 'リソースへのURL',
    is_public BOOLEAN DEFAULT FALSE COMMENT '公開リソースかどうか',
    access_roles JSON COMMENT 'このリソースにアクセス可能なロールのリスト',
    created_by BINARY(16) COMMENT '作成者ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    UNIQUE INDEX idx_resource_unique (resource_id, resource_type),
    INDEX idx_search_resource_type (resource_type),
    INDEX idx_search_created_by (created_by),
    INDEX idx_search_updated_at (updated_at),
    FULLTEXT INDEX idx_search_fulltext (title, subtitle, description)
) COMMENT='検索インデックス';

-- テスト用データ
INSERT INTO search_history (search_id, user_id, query, resource_types, filters, sort_field, sort_order, group_by_resource_type, result_count, created_at)
VALUES 
(UUID_TO_BIN('a1b2c3d4-e5f6-7890-abcd-ef1234567890'), 
 (SELECT user_id FROM user WHERE username = 'admin'),
 'Java 開発',
 '["engineers", "projects"]',
 '{"skills": ["Java", "Spring"], "location": ["東京", "神奈川"]}',
 'matching_score',
 'desc',
 true,
 42,
 DATE_SUB(NOW(), INTERVAL 2 DAY)),

(UUID_TO_BIN('b2c3d4e5-f6a7-8901-bcde-f12345678901'), 
 (SELECT user_id FROM user WHERE username = 'admin'),
 'Python データ分析',
 '["engineers"]',
 '{"skills": ["Python", "pandas", "scikit-learn"]}',
 'matching_score',
 'desc',
 false,
 15,
 DATE_SUB(NOW(), INTERVAL 5 DAY));

-- テスト用検索インデックスデータ
INSERT INTO search_index (resource_id, resource_type, title, subtitle, description, attributes, url, is_public, access_roles, created_by, updated_at)
VALUES
('e12345', 'engineers', '鈴木 一郎', 'Javaエンジニア', 'Java, Spring Bootでの開発経験10年。金融系システム開発のスペシャリスト。',
 '{"skills": ["Java", "Spring", "MySQL", "AWS"], "experience_years": 10, "current_status": "稼働可能", "preferred_location": "東京", "monthly_rate": 800000}',
 '/engineers/e12345', false, '["ROLE_ADMIN", "ROLE_MANAGER"]', (SELECT user_id FROM user WHERE username = 'admin'), NOW()),

('p67890', 'projects', '金融系Webアプリケーション開発', '株式会社ABCテクノロジー', 'Java / Spring Bootを使用した金融系Webアプリケーションの開発案件。',
 '{"skills": ["Java", "Spring Boot", "MySQL", "AWS"], "location": "東京", "period": "2023-06-01 ～ 2023-12-31", "monthly_rate": 850000, "status": "募集中"}',
 '/projects/p67890', true, '["ROLE_ADMIN", "ROLE_MANAGER", "ROLE_USER"]', (SELECT user_id FROM user WHERE username = 'admin'), NOW()),

('e24680', 'engineers', '高橋 次郎', 'Pythonエンジニア', 'Python, Django, Flaskでの開発経験5年。AIと機械学習の実務経験あり。',
 '{"skills": ["Python", "Django", "Flask", "TensorFlow", "pandas"], "experience_years": 5, "current_status": "稼働可能", "preferred_location": "大阪", "monthly_rate": 750000}',
 '/engineers/e24680', false, '["ROLE_ADMIN", "ROLE_MANAGER"]', (SELECT user_id FROM user WHERE username = 'admin'), NOW());