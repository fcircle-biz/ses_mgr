-- SES Management System PostgreSQL Test Data
-- 11_test_data_company_engineer.sql: 企業とエンジニア関連テストデータ
-- Created: 2025-05-06

-- 企業データ
INSERT INTO companies (company_id, company_name, company_type, address, phone_number, email, website, contract_date)
VALUES
    (1, '株式会社テクノシステム', '自社', '東京都千代田区丸の内1-1-1', '03-1234-5678', 'info@technosystem.co.jp', 'https://technosystem.co.jp', '2020-04-01'),
    (2, '株式会社デジタルソリューション', 'パートナー企業', '東京都新宿区西新宿2-2-2', '03-2345-6789', 'info@digitalsolution.co.jp', 'https://digitalsolution.co.jp', '2021-06-15'),
    (3, '株式会社クラウドテクノロジー', 'パートナー企業', '東京都渋谷区渋谷3-3-3', '03-3456-7890', 'info@cloudtech.co.jp', 'https://cloudtech.co.jp', '2022-01-10'),
    (4, '株式会社AIイノベーション', 'パートナー企業', '東京都品川区大崎4-4-4', '03-4567-8901', 'info@ai-innovation.co.jp', 'https://ai-innovation.co.jp', '2022-09-20'),
    (5, '株式会社サイバーセキュリティ', 'パートナー企業', '東京都港区芝5-5-5', '03-5678-9012', 'info@cybersecurity.co.jp', 'https://cybersecurity.co.jp', '2023-03-01'),
    (6, '株式会社ITコンサルティング', '顧客', '大阪府大阪市北区梅田1-1-1', '06-1234-5678', 'info@itconsulting.co.jp', 'https://itconsulting.co.jp', NULL),
    (7, '株式会社フィナンシャルテック', '顧客', '東京都中央区銀座2-2-2', '03-8765-4321', 'info@fintech.co.jp', 'https://fintech.co.jp', NULL),
    (8, '株式会社ヘルスケアシステム', '顧客', '福岡県福岡市博多区博多駅前3-3-3', '092-345-6789', 'info@healthcare-system.co.jp', 'https://healthcare-system.co.jp', NULL),
    (9, '株式会社メディアコンテンツ', '顧客', '東京都渋谷区恵比寿4-4-4', '03-2345-6789', 'info@media-contents.co.jp', 'https://media-contents.co.jp', NULL),
    (10, '株式会社製造テクノロジー', '顧客', '愛知県名古屋市中区栄5-5-5', '052-345-6789', 'info@manufacturing-tech.co.jp', 'https://manufacturing-tech.co.jp', NULL);

-- 顧客データ
INSERT INTO customers (customer_id, company_id, customer_name, address, phone_number, email)
VALUES
    (1, 6, '株式会社ITコンサルティング', '大阪府大阪市北区梅田1-1-1', '06-1234-5678', 'info@itconsulting.co.jp'),
    (2, 7, '株式会社フィナンシャルテック', '東京都中央区銀座2-2-2', '03-8765-4321', 'info@fintech.co.jp'),
    (3, 8, '株式会社ヘルスケアシステム', '福岡県福岡市博多区博多駅前3-3-3', '092-345-6789', 'info@healthcare-system.co.jp'),
    (4, 9, '株式会社メディアコンテンツ', '東京都渋谷区恵比寿4-4-4', '03-2345-6789', 'info@media-contents.co.jp'),
    (5, 10, '株式会社製造テクノロジー', '愛知県名古屋市中区栄5-5-5', '052-345-6789', 'info@manufacturing-tech.co.jp');

-- スキルデータ
INSERT INTO skills (skill_id, skill_name, skill_category, description)
VALUES
    (1, 'Java', '言語', 'Javaプログラミング言語'),
    (2, 'Python', '言語', 'Pythonプログラミング言語'),
    (3, 'JavaScript', '言語', 'JavaScriptプログラミング言語'),
    (4, 'TypeScript', '言語', 'TypeScriptプログラミング言語'),
    (5, 'C#', '言語', 'C#プログラミング言語'),
    (6, 'Kotlin', '言語', 'Kotlinプログラミング言語'),
    (7, 'Swift', '言語', 'Swiftプログラミング言語'),
    (8, 'PHP', '言語', 'PHPプログラミング言語'),
    (9, 'Ruby', '言語', 'Rubyプログラミング言語'),
    (10, 'Go', '言語', 'Goプログラミング言語'),
    (11, 'Spring Boot', 'フレームワーク', 'Javaベースのフレームワーク'),
    (12, 'Django', 'フレームワーク', 'Pythonベースのフレームワーク'),
    (13, 'Flask', 'フレームワーク', 'Pythonベースの軽量フレームワーク'),
    (14, 'React', 'フレームワーク', 'JavaScriptライブラリ'),
    (15, 'Angular', 'フレームワーク', 'TypeScriptベースのフレームワーク'),
    (16, 'Vue.js', 'フレームワーク', 'JavaScriptフレームワーク'),
    (17, 'Next.js', 'フレームワーク', 'Reactベースのフレームワーク'),
    (18, 'Ruby on Rails', 'フレームワーク', 'Rubyベースのフレームワーク'),
    (19, 'Laravel', 'フレームワーク', 'PHPベースのフレームワーク'),
    (20, '.NET Core', 'フレームワーク', 'Microsoftのオープンソースフレームワーク'),
    (21, 'PostgreSQL', 'DB', 'オープンソースリレーショナルデータベース'),
    (22, 'MySQL', 'DB', 'オープンソースリレーショナルデータベース'),
    (23, 'Oracle', 'DB', '商用リレーショナルデータベース'),
    (24, 'SQL Server', 'DB', 'Microsoftのリレーショナルデータベース'),
    (25, 'MongoDB', 'DB', 'NoSQLドキュメント指向データベース'),
    (26, 'Redis', 'DB', 'インメモリデータストア'),
    (27, 'Elasticsearch', 'DB', '分散検索エンジン'),
    (28, 'DynamoDB', 'DB', 'AWSのNoSQLデータベース'),
    (29, 'Linux', 'OS', 'オープンソースOS'),
    (30, 'Windows Server', 'OS', 'Microsoftサーバーオペレーティングシステム'),
    (31, 'Docker', 'ミドルウェア', 'コンテナプラットフォーム'),
    (32, 'Kubernetes', 'ミドルウェア', 'コンテナオーケストレーション'),
    (33, 'Apache', 'ミドルウェア', 'Webサーバー'),
    (34, 'Nginx', 'ミドルウェア', 'Webサーバー'),
    (35, 'Tomcat', 'ミドルウェア', 'Javaサーブレットコンテナ'),
    (36, 'AWS', 'クラウド', 'Amazon Web Services'),
    (37, 'Azure', 'クラウド', 'Microsoft Azure'),
    (38, 'GCP', 'クラウド', 'Google Cloud Platform'),
    (39, 'Firebase', 'クラウド', 'Googleのアプリケーション開発プラットフォーム'),
    (40, 'Terraform', 'ツール', 'IaC（Infrastructure as Code）ツール'),
    (41, 'Git', 'ツール', 'バージョン管理システム'),
    (42, 'Jenkins', 'ツール', 'CI/CDツール'),
    (43, 'GitHub Actions', 'ツール', 'CI/CDツール'),
    (44, 'JIRA', 'ツール', 'プロジェクト管理ツール'),
    (45, 'Selenium', 'ツール', '自動テストツール'),
    (46, 'Ansible', 'ツール', '構成管理ツール'),
    (47, 'TensorFlow', 'ツール', '機械学習ライブラリ'),
    (48, 'PyTorch', 'ツール', '機械学習ライブラリ'),
    (49, '基本情報技術者', '資格', '情報処理技術者試験'),
    (50, '応用情報技術者', '資格', '情報処理技術者試験');

-- エンジニアデータ
INSERT INTO engineers (engineer_id, company_id, engineer_name, furigana, email, phone_number, birth_date, employment_type, join_date, years_of_experience, preferred_work_location, status, availability_date, preferred_unit_price, profile_text)
VALUES
    (1, 1, '鈴木一郎', 'スズキイチロウ', 'suzuki@technosystem.co.jp', '090-1111-2222', '1985-05-10', '正社員', '2020-04-01', 10, '東京', '稼働可能', '2025-06-01', 800000, 'Javaを中心としたバックエンド開発が得意です。大規模Webアプリケーションの開発経験が豊富です。'),
    (2, 1, '佐藤二郎', 'サトウジロウ', 'sato@technosystem.co.jp', '090-2222-3333', '1988-08-15', '正社員', '2020-06-01', 8, '東京', 'アサイン中', '2025-09-01', 750000, 'フロントエンド開発が専門で、React/TypeScriptを使った開発経験が豊富です。'),
    (3, 1, '田中三郎', 'タナカサブロウ', 'tanaka@technosystem.co.jp', '090-3333-4444', '1990-11-20', '正社員', '2021-02-01', 6, '東京', 'アサイン中', '2025-08-15', 700000, 'インフラ/クラウド系エンジニア。AWS認定ソリューションアーキテクトの資格を保有。'),
    (4, 1, '高橋四郎', 'タカハシシロウ', 'takahashi@technosystem.co.jp', '090-4444-5555', '1992-04-25', '正社員', '2021-07-01', 5, '東京', 'アサイン中', '2025-10-01', 650000, 'Java/Spring Bootでのバックエンド開発が得意です。金融系システム開発の経験があります。'),
    (5, 1, '伊藤五郎', 'イトウゴロウ', 'ito@technosystem.co.jp', '090-5555-6666', '1993-09-30', '正社員', '2022-01-01', 4, '東京', '稼働可能', '2025-05-15', 600000, 'フルスタックエンジニア。React/Node.jsでの開発経験があります。'),
    (6, 2, '渡辺太郎', 'ワタナベタロウ', 'watanabe@digitalsolution.co.jp', '090-6666-7777', '1987-06-12', '正社員', '2021-06-15', 9, '東京', 'アサイン中', '2025-09-15', 780000, 'データベースエンジニア。PostgreSQL/MySQLの設計・構築・チューニングが得意です。'),
    (7, 2, '小林次郎', 'コバヤシジロウ', 'kobayashi@digitalsolution.co.jp', '090-7777-8888', '1989-07-18', '正社員', '2021-08-01', 7, '東京', '稼働可能', '2025-06-01', 730000, 'DevOpsエンジニア。CI/CD環境の構築・運用の経験が豊富です。'),
    (8, 2, '加藤三郎', 'カトウサブロウ', 'kato@digitalsolution.co.jp', '090-8888-9999', '1991-10-25', '正社員', '2022-01-10', 6, '東京', 'アサイン中', '2025-11-01', 680000, 'フロントエンドエンジニア。Vue.js/Nuxt.jsを使った開発経験があります。'),
    (9, 2, '山田花子', 'ヤマダハナコ', 'yamada@digitalsolution.co.jp', '090-9999-0000', '1994-03-08', '正社員', '2022-05-01', 4, '東京', '稼働可能', '2025-05-20', 620000, 'UI/UXデザインが得意なフロントエンドエンジニアです。'),
    (10, 3, '中村太郎', 'ナカムラタロウ', 'nakamura@cloudtech.co.jp', '080-1111-2222', '1986-12-05', '正社員', '2022-01-10', 10, '東京', 'アサイン中', '2025-08-01', 800000, 'クラウドアーキテクト。AWS/Azure/GCPの豊富な経験があります。'),
    (11, 3, '山本次郎', 'ヤマモトジロウ', 'yamamoto@cloudtech.co.jp', '080-2222-3333', '1990-02-15', '正社員', '2022-04-01', 7, '東京', '稼働可能', '2025-06-01', 750000, 'バックエンドエンジニア。Python/Djangoでの開発経験が豊富です。'),
    (12, 3, '斎藤健', 'サイトウケン', 'saito@cloudtech.co.jp', '080-3333-4444', '1992-08-25', '正社員', '2022-07-01', 5, '東京', 'アサイン中', '2025-10-01', 680000, 'セキュリティエンジニア。セキュリティ設計・脆弱性診断の経験があります。'),
    (13, 4, '松本大輔', 'マツモトダイスケ', 'matsumoto@ai-innovation.co.jp', '080-4444-5555', '1988-11-03', '正社員', '2022-09-20', 8, '東京', 'アサイン中', '2025-12-01', 780000, 'AI/MLエンジニア。TensorFlow/PyTorchを使った機械学習モデルの開発経験があります。'),
    (14, 4, '井上和也', 'イノウエカズヤ', 'inoue@ai-innovation.co.jp', '080-5555-6666', '1991-05-17', '正社員', '2022-11-01', 6, '東京', '稼働可能', '2025-06-01', 720000, 'データサイエンティスト。統計分析・機械学習の経験が豊富です。'),
    (15, 5, '木村達也', 'キムラタツヤ', 'kimura@cybersecurity.co.jp', '080-6666-7777', '1987-09-22', '正社員', '2023-03-01', 9, '東京', 'アサイン中', '2025-08-01', 800000, 'セキュリティアーキテクト。セキュリティポリシー策定・脆弱性診断の経験が豊富です。');

-- エンジニアスキルデータ
INSERT INTO engineer_skills (engineer_id, skill_id, proficiency_level, years_of_experience, description)
VALUES
    -- 鈴木一郎のスキル
    (1, 1, 5, 10.0, 'Javaによる大規模Webアプリケーション開発'),
    (1, 11, 5, 7.0, 'Spring Bootを使ったマイクロサービス開発'),
    (1, 21, 4, 8.0, 'PostgreSQLを使ったデータベース設計・運用'),
    (1, 31, 3, 4.0, 'Dockerを使ったコンテナ化'),
    (1, 36, 3, 5.0, 'AWS上でのシステム構築'),
    
    -- 佐藤二郎のスキル
    (2, 3, 5, 8.0, 'JavaScriptによるフロントエンド開発'),
    (2, 4, 5, 6.0, 'TypeScriptを使った型安全な開発'),
    (2, 14, 5, 7.0, 'Reactを使ったSPA開発'),
    (2, 17, 4, 4.0, 'Next.jsを使ったSSRアプリケーション開発'),
    (2, 41, 4, 8.0, 'Gitを使ったバージョン管理'),
    
    -- 田中三郎のスキル
    (3, 29, 5, 6.0, 'Linux環境での運用管理'),
    (3, 31, 5, 5.0, 'Dockerを使ったコンテナ環境構築'),
    (3, 32, 4, 4.0, 'Kubernetesによるコンテナオーケストレーション'),
    (3, 36, 5, 6.0, 'AWSを使ったクラウド環境構築'),
    (3, 40, 4, 3.0, 'Terraformを使ったIaC'),
    
    -- 高橋四郎のスキル
    (4, 1, 4, 5.0, 'Javaによるバックエンド開発'),
    (4, 11, 4, 4.0, 'Spring Bootを使ったRESTful API開発'),
    (4, 21, 3, 4.0, 'PostgreSQLを使ったデータベース操作'),
    (4, 49, 5, 5.0, '基本情報技術者資格保有'),
    
    -- 伊藤五郎のスキル
    (5, 3, 4, 4.0, 'JavaScriptによるフロントエンド開発'),
    (5, 14, 4, 3.0, 'Reactを使ったSPA開発'),
    (5, 27, 3, 2.0, 'Elasticsearchを使った検索機能実装'),
    (5, 41, 3, 4.0, 'Gitを使ったバージョン管理'),
    
    -- 渡辺太郎のスキル
    (6, 21, 5, 9.0, 'PostgreSQLの設計・構築・チューニング'),
    (6, 22, 5, 8.0, 'MySQLの設計・構築・チューニング'),
    (6, 23, 4, 6.0, 'Oracleの運用管理'),
    (6, 25, 3, 4.0, 'MongoDBを使ったNoSQLデータ管理'),
    (6, 27, 4, 5.0, 'Elasticsearchを使った検索エンジン実装'),
    
    -- 小林次郎のスキル
    (7, 31, 5, 6.0, 'Dockerを使ったコンテナ環境構築'),
    (7, 32, 4, 5.0, 'Kubernetesによるコンテナオーケストレーション'),
    (7, 42, 5, 7.0, 'Jenkinsを使ったCI/CD環境構築'),
    (7, 43, 4, 3.0, 'GitHub Actionsを使ったCI/CD実装'),
    (7, 46, 4, 4.0, 'Ansibleを使った構成管理'),
    
    -- 加藤三郎のスキル
    (8, 3, 5, 6.0, 'JavaScriptによるフロントエンド開発'),
    (8, 4, 4, 4.0, 'TypeScriptを使った型安全な開発'),
    (8, 16, 5, 5.0, 'Vue.jsを使ったSPA開発'),
    (8, 41, 4, 6.0, 'Gitを使ったバージョン管理'),
    
    -- 山田花子のスキル
    (9, 3, 4, 4.0, 'JavaScriptによるフロントエンド開発'),
    (9, 14, 4, 3.0, 'Reactを使ったSPA開発'),
    (9, 16, 3, 2.0, 'Vue.jsを使ったUI実装'),
    (9, 41, 3, 4.0, 'Gitを使ったバージョン管理'),
    
    -- 中村太郎のスキル
    (10, 36, 5, 10.0, 'AWSの設計・構築・運用'),
    (10, 37, 5, 8.0, 'Azureの設計・構築・運用'),
    (10, 38, 4, 6.0, 'GCPの設計・構築'),
    (10, 32, 5, 7.0, 'Kubernetesによるコンテナオーケストレーション'),
    (10, 40, 5, 6.0, 'Terraformを使ったIaC'),
    
    -- 山本次郎のスキル
    (11, 2, 5, 7.0, 'Pythonによるバックエンド開発'),
    (11, 12, 5, 6.0, 'Djangoを使ったWebアプリケーション開発'),
    (11, 13, 4, 5.0, 'Flaskを使った軽量API開発'),
    (11, 21, 4, 5.0, 'PostgreSQLを使ったデータベース操作'),
    (11, 41, 4, 7.0, 'Gitを使ったバージョン管理'),
    
    -- 斎藤健のスキル
    (12, 29, 5, 5.0, 'Linuxセキュリティ設定'),
    (12, 36, 4, 4.0, 'AWSのセキュリティ設計'),
    (12, 37, 3, 3.0, 'Azureのセキュリティ設計'),
    (12, 50, 5, 5.0, '応用情報技術者資格保有'),
    
    -- 松本大輔のスキル
    (13, 2, 5, 8.0, 'Pythonによる機械学習開発'),
    (13, 47, 5, 6.0, 'TensorFlowを使ったディープラーニングモデル開発'),
    (13, 48, 5, 5.0, 'PyTorchを使ったAIモデル開発'),
    (13, 38, 4, 4.0, 'GCPのAIプラットフォーム活用'),
    (13, 41, 4, 8.0, 'Gitを使ったバージョン管理'),
    
    -- 井上和也のスキル
    (14, 2, 5, 6.0, 'Pythonによるデータ分析'),
    (14, 25, 4, 5.0, 'MongoDBを使った大規模データ処理'),
    (14, 27, 4, 4.0, 'Elasticsearchを使ったデータ分析'),
    (14, 47, 4, 4.0, 'TensorFlowを使った予測モデル開発'),
    
    -- 木村達也のスキル
    (15, 29, 5, 9.0, 'Linuxセキュリティ'),
    (15, 30, 5, 8.0, 'Windows Serverセキュリティ'),
    (15, 36, 5, 7.0, 'AWSセキュリティ設計'),
    (15, 37, 4, 6.0, 'Azureセキュリティ設計'),
    (15, 50, 5, 9.0, '応用情報技術者資格保有');

-- 職務経歴データ
INSERT INTO work_experiences (engineer_id, project_name, client_name, start_date, end_date, role, description, technologies_used)
VALUES
    -- 鈴木一郎の職務経歴
    (1, '金融系Webアプリケーション開発', '株式会社フィナンシャルテック', '2020-05-01', '2021-03-31', 'バックエンドリーダー', '新規金融サービスのバックエンド機能開発プロジェクト。データベース設計から実装まで担当。', 'Java, Spring Boot, PostgreSQL, Docker, AWS'),
    (1, 'ECサイトリニューアル', '株式会社メディアコンテンツ', '2021-05-01', '2022-01-31', 'テックリード', '大規模ECサイトのバックエンド刷新プロジェクト。パフォーマンス改善とマイクロサービス化を実施。', 'Java, Spring Boot, MySQL, Docker, AWS, Kubernetes'),
    (1, '業務システム統合プロジェクト', '株式会社ITコンサルティング', '2022-03-01', '2023-06-30', 'アーキテクト', '複数の業務システムを統合するプロジェクト。システム設計とデータ移行を担当。', 'Java, Spring Boot, Oracle, Docker, AWS'),
    
    -- 佐藤二郎の職務経歴
    (2, 'ポータルサイト構築', '株式会社メディアコンテンツ', '2020-07-01', '2021-03-31', 'フロントエンドエンジニア', '企業向けポータルサイトの新規開発。SPA実装とUIコンポーネント設計を担当。', 'React, TypeScript, Redux, Jest'),
    (2, 'オンライン予約システム開発', '株式会社ヘルスケアシステム', '2021-05-01', '2022-02-28', 'フロントエンドリーダー', '医療機関向け予約システムのフロントエンド開発。UI/UX設計とチームマネジメントを担当。', 'React, TypeScript, Next.js, GraphQL'),
    (2, 'コーポレートサイトリニューアル', '株式会社製造テクノロジー', '2022-04-01', '2022-12-31', 'テックリード', '複数言語対応のコーポレートサイト構築。サイトパフォーマンス最適化を担当。', 'React, TypeScript, Next.js, Contentful'),
    
    -- 田中三郎の職務経歴
    (3, 'クラウド移行プロジェクト', '株式会社ITコンサルティング', '2021-03-01', '2021-12-31', 'インフラエンジニア', 'オンプレミスからAWSへの移行プロジェクト。インフラ設計と移行作業を担当。', 'AWS, Docker, Kubernetes, Terraform'),
    (3, 'マイクロサービス基盤構築', '株式会社フィナンシャルテック', '2022-02-01', '2022-10-31', 'インフラリーダー', 'コンテナベースのマイクロサービス基盤構築。Kubernetes環境設計と構築を担当。', 'Docker, Kubernetes, AWS, Terraform, Jenkins'),
    (3, 'マルチクラウド環境設計', '株式会社メディアコンテンツ', '2023-01-01', '2023-09-30', 'クラウドアーキテクト', 'AWS/Azureを活用したマルチクラウド環境の設計と構築。', 'AWS, Azure, Kubernetes, Terraform, Ansible'),
    
    -- 渡辺太郎の職務経歴
    (6, 'データベース最適化プロジェクト', '株式会社フィナンシャルテック', '2021-07-01', '2022-03-31', 'DBアーキテクト', '金融系システムのデータベース最適化。パフォーマンスチューニングと冗長構成設計を担当。', 'PostgreSQL, Oracle, AWS RDS, Redis'),
    (6, 'データウェアハウス構築', '株式会社ITコンサルティング', '2022-05-01', '2023-01-31', 'データベースリーダー', '分析基盤用データウェアハウス構築。データモデリングとETL処理設計を担当。', 'PostgreSQL, Redshift, Python, Airflow'),
    
    -- 中村太郎の職務経歴
    (10, 'グローバルクラウド基盤構築', '株式会社ITコンサルティング', '2022-02-01', '2022-10-31', 'クラウドアーキテクト', 'グローバル展開向けのマルチリージョンクラウド基盤構築。可用性設計とセキュリティ設計を担当。', 'AWS, Azure, Kubernetes, Terraform'),
    (10, 'データ分析基盤構築', '株式会社メディアコンテンツ', '2022-12-01', '2023-08-31', 'テックリード', 'ビッグデータ分析基盤の設計と構築。スケーラブルなアーキテクチャ設計を担当。', 'GCP, BigQuery, Dataflow, Kubernetes'),
    
    -- 松本大輔の職務経歴
    (13, 'レコメンデーションエンジン開発', '株式会社メディアコンテンツ', '2022-10-01', '2023-05-31', 'AIエンジニア', 'コンテンツレコメンデーションエンジンの開発。機械学習モデル設計と実装を担当。', 'Python, TensorFlow, GCP, BigQuery'),
    (13, '画像認識システム開発', '株式会社製造テクノロジー', '2023-07-01', NULL, 'AIリードエンジニア', '製造ラインの不良品検出用画像認識システム開発。深層学習モデル設計と実装を担当。', 'Python, PyTorch, OpenCV, CUDA'),
    
    -- 木村達也の職務経歴
    (15, 'セキュリティ強化プロジェクト', '株式会社フィナンシャルテック', '2023-04-01', '2023-10-31', 'セキュリティコンサルタント', '金融系システムのセキュリティ診断と強化。脆弱性診断とセキュリティポリシー策定を担当。', 'AWS Security, WAF, IAM, Penetration Testing'),
    (15, 'クラウドセキュリティ設計', '株式会社ITコンサルティング', '2023-12-01', NULL, 'セキュリティアーキテクト', 'マルチクラウド環境のセキュリティ設計。ゼロトラストアーキテクチャ実装を担当。', 'AWS, Azure, IAM, WAF, Security Groups, SIEM');

-- スキルシートデータ
INSERT INTO skill_sheets (engineer_id, sheet_name, version, file_path, file_format, is_current, is_public)
VALUES
    (1, '鈴木一郎スキルシート', 3, '/documents/skillsheets/suzuki_v3.pdf', 'PDF', TRUE, TRUE),
    (1, '鈴木一郎スキルシート', 2, '/documents/skillsheets/suzuki_v2.pdf', 'PDF', FALSE, FALSE),
    (1, '鈴木一郎スキルシート', 1, '/documents/skillsheets/suzuki_v1.pdf', 'PDF', FALSE, FALSE),
    (2, '佐藤二郎スキルシート', 2, '/documents/skillsheets/sato_v2.pdf', 'PDF', TRUE, TRUE),
    (2, '佐藤二郎スキルシート', 1, '/documents/skillsheets/sato_v1.pdf', 'PDF', FALSE, FALSE),
    (3, '田中三郎スキルシート', 1, '/documents/skillsheets/tanaka_v1.pdf', 'PDF', TRUE, TRUE),
    (6, '渡辺太郎スキルシート', 2, '/documents/skillsheets/watanabe_v2.pdf', 'PDF', TRUE, TRUE),
    (10, '中村太郎スキルシート', 1, '/documents/skillsheets/nakamura_v1.pdf', 'PDF', TRUE, TRUE),
    (13, '松本大輔スキルシート', 1, '/documents/skillsheets/matsumoto_v1.pdf', 'PDF', TRUE, TRUE),
    (15, '木村達也スキルシート', 1, '/documents/skillsheets/kimura_v1.pdf', 'PDF', TRUE, TRUE);

-- 資格データ
INSERT INTO certifications (engineer_id, certification_name, issuing_organization, acquisition_date, expiration_date, certification_number)
VALUES
    (1, 'Oracle Certified Professional, Java SE 8 Programmer', 'Oracle', '2018-05-15', NULL, 'OCP123456'),
    (1, 'AWS Certified Solutions Architect – Associate', 'Amazon Web Services', '2020-07-10', '2023-07-10', 'AWSSA123456'),
    (3, 'AWS Certified Solutions Architect – Professional', 'Amazon Web Services', '2022-02-20', '2025-02-20', 'AWSSAP789012'),
    (3, 'Certified Kubernetes Administrator', 'Cloud Native Computing Foundation', '2021-09-15', '2024-09-15', 'CKA345678'),
    (4, '基本情報技術者', 'IPA', '2019-05-20', NULL, 'FE-2019-12345'),
    (6, 'Oracle Database Administrator Certified Professional', 'Oracle', '2020-03-10', NULL, 'ODACP654321'),
    (7, 'AWS Certified DevOps Engineer – Professional', 'Amazon Web Services', '2021-11-05', '2024-11-05', 'AWSDO234567'),
    (10, 'Microsoft Certified: Azure Solutions Architect Expert', 'Microsoft', '2021-08-15', '2023-08-15', 'MCASAE765432'),
    (10, 'Google Cloud Certified – Professional Cloud Architect', 'Google Cloud', '2022-05-10', '2024-05-10', 'GCPCA987654'),
    (12, 'Certified Information Systems Security Professional (CISSP)', 'ISC²', '2021-06-20', '2024-06-20', 'CISSP123789'),
    (13, 'TensorFlow Developer Certificate', 'Google', '2022-01-30', NULL, 'TFD234567'),
    (15, 'Certified Ethical Hacker', 'EC-Council', '2022-03-15', '2025-03-15', 'CEH789012'),
    (15, 'Certified Information Security Manager (CISM)', 'ISACA', '2021-10-25', '2024-10-25', 'CISM456789');