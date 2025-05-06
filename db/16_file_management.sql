-- ファイル管理テーブル作成スクリプト

-- -----------------------------------------------------
-- Table files
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS files (
  file_id UUID NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_type VARCHAR(50) NOT NULL,
  mime_type VARCHAR(255) NOT NULL,
  size BIGINT NOT NULL,
  description TEXT,
  tags VARCHAR(255),
  resource_id VARCHAR(100),
  resource_type VARCHAR(50),
  storage_path VARCHAR(255) NOT NULL,
  is_public BOOLEAN NOT NULL DEFAULT FALSE,
  download_count INT NOT NULL DEFAULT 0,
  sha256_hash VARCHAR(64) NOT NULL,
  expiry_date TIMESTAMP,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  deleted_at TIMESTAMP,
  created_by UUID NOT NULL,
  created_by_name VARCHAR(100),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  metadata JSONB,
  PRIMARY KEY (file_id)
);

-- インデックス作成
CREATE INDEX idx_files_file_type ON files(file_type);
CREATE INDEX idx_files_resource_id ON files(resource_id);
CREATE INDEX idx_files_created_by ON files(created_by);
CREATE INDEX idx_files_created_at ON files(created_at);
CREATE INDEX idx_files_is_deleted ON files(is_deleted);
CREATE INDEX idx_files_is_public ON files(is_public);
CREATE INDEX idx_files_expiry_date ON files(expiry_date);
CREATE INDEX idx_files_file_name ON files(file_name);
CREATE INDEX idx_files_tags ON files(tags);

COMMENT ON TABLE files IS 'ファイル管理テーブル';
COMMENT ON COLUMN files.file_id IS 'ファイルID';
COMMENT ON COLUMN files.file_name IS 'ファイル名';
COMMENT ON COLUMN files.file_type IS 'ファイルタイプ (contract, invoice, skill_sheet, project_document, common, other)';
COMMENT ON COLUMN files.mime_type IS 'MIMEタイプ';
COMMENT ON COLUMN files.size IS 'ファイルサイズ（バイト）';
COMMENT ON COLUMN files.description IS 'ファイルの説明';
COMMENT ON COLUMN files.tags IS 'タグ（カンマ区切り）';
COMMENT ON COLUMN files.resource_id IS '関連リソースID';
COMMENT ON COLUMN files.resource_type IS '関連リソースタイプ';
COMMENT ON COLUMN files.storage_path IS 'ファイルの保存パス';
COMMENT ON COLUMN files.is_public IS '公開フラグ';
COMMENT ON COLUMN files.download_count IS 'ダウンロード回数';
COMMENT ON COLUMN files.sha256_hash IS 'SHA-256ハッシュ値';
COMMENT ON COLUMN files.expiry_date IS '有効期限';
COMMENT ON COLUMN files.is_deleted IS '削除フラグ（論理削除用）';
COMMENT ON COLUMN files.deleted_at IS '削除日時';
COMMENT ON COLUMN files.created_by IS '作成者ID';
COMMENT ON COLUMN files.created_by_name IS '作成者名';
COMMENT ON COLUMN files.created_at IS '作成日時';
COMMENT ON COLUMN files.updated_at IS '更新日時';
COMMENT ON COLUMN files.metadata IS 'メタデータ（JSON形式）';

-- -----------------------------------------------------
-- テスト用データ（開発環境のみ）
-- -----------------------------------------------------
INSERT INTO files (
  file_id, 
  file_name, 
  file_type, 
  mime_type, 
  size, 
  description, 
  tags, 
  resource_id, 
  resource_type,
  storage_path, 
  is_public, 
  download_count, 
  sha256_hash, 
  created_by, 
  created_by_name, 
  created_at, 
  updated_at
) VALUES (
  '00000000-0000-0000-0000-000000000001', 
  'サンプル契約書.pdf', 
  'CONTRACT', 
  'application/pdf', 
  1024, 
  'これはサンプル契約書です', 
  '契約書,サンプル,テスト', 
  'contract-123', 
  'contract',
  'sample/sample-contract.pdf', 
  FALSE, 
  0, 
  'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 
  '00000000-0000-0000-0000-000000000001', 
  'システム管理者', 
  NOW(), 
  NOW()
);

INSERT INTO files (
  file_id, 
  file_name, 
  file_type, 
  mime_type, 
  size, 
  description, 
  tags, 
  is_public, 
  storage_path, 
  download_count, 
  sha256_hash, 
  created_by, 
  created_by_name, 
  created_at, 
  updated_at
) VALUES (
  '00000000-0000-0000-0000-000000000002', 
  'マニュアル.pdf', 
  'COMMON', 
  'application/pdf', 
  2048, 
  'システムマニュアル', 
  'マニュアル,共通資料', 
  TRUE, 
  'sample/manual.pdf', 
  5, 
  'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 
  '00000000-0000-0000-0000-000000000001', 
  'システム管理者', 
  NOW() - INTERVAL '1 day', 
  NOW() - INTERVAL '1 day'
);