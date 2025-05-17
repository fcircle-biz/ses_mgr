-- パスワードリセットトークンテーブルの作成
CREATE TABLE common.password_reset_tokens (
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES common.users(id) ON DELETE CASCADE
);

-- インデックス作成
CREATE UNIQUE INDEX idx_password_reset_tokens_token ON common.password_reset_tokens(token);
CREATE INDEX idx_password_reset_tokens_user_id ON common.password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_expiry_date ON common.password_reset_tokens(expiry_date);

-- 説明追加
COMMENT ON TABLE common.password_reset_tokens IS 'パスワードリセット用のトークン情報を管理するテーブル';
COMMENT ON COLUMN common.password_reset_tokens.id IS 'トークンのID';
COMMENT ON COLUMN common.password_reset_tokens.user_id IS 'ユーザーID';
COMMENT ON COLUMN common.password_reset_tokens.token IS 'パスワードリセット用トークン';
COMMENT ON COLUMN common.password_reset_tokens.expiry_date IS 'トークンの有効期限';
COMMENT ON COLUMN common.password_reset_tokens.used IS 'トークンの使用済みフラグ';
COMMENT ON COLUMN common.password_reset_tokens.created_at IS 'トークンの作成日時';