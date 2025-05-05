-- SES Management System PostgreSQL Schema
-- 08_notification.sql: 通知関連テーブル
-- Created: 2025-05-06

-- ============================
-- NOTIFICATION SYSTEM
-- ============================

CREATE TABLE IF NOT EXISTS notification_templates (
    template_id SERIAL PRIMARY KEY,
    template_code VARCHAR(50) UNIQUE NOT NULL,
    template_name VARCHAR(100) NOT NULL,
    template_type TEXT CHECK (template_type IN ('メール', 'システム通知', 'SMS', 'その他')) DEFAULT 'システム通知',
    template_subject VARCHAR(200),
    template_body TEXT NOT NULL,
    variables JSONB,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notification_templates_template_code ON notification_templates(template_code);
CREATE INDEX IF NOT EXISTS idx_notification_templates_template_type ON notification_templates(template_type);
CREATE INDEX IF NOT EXISTS idx_notification_templates_is_active ON notification_templates(is_active);

CREATE TRIGGER update_notification_templates_timestamp
BEFORE UPDATE ON notification_templates
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS matching_alerts (
    alert_id SERIAL PRIMARY KEY,
    alert_type TEXT NOT NULL CHECK (alert_type IN ('新規マッチング', '状態変更', '面談設定', '面談結果', '単価交渉', '内定通知', 'その他')),
    subject VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    related_entity TEXT NOT NULL CHECK (related_entity IN ('matching', 'project', 'engineer', 'interview')),
    entity_id INTEGER NOT NULL,
    target_user_id UUID NOT NULL REFERENCES users(user_id),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    priority TEXT NOT NULL CHECK (priority IN ('低', '通常', '高', '緊急')) DEFAULT '通常',
    actions JSONB,
    expiry_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_matching_alerts_target_user_id ON matching_alerts(target_user_id);
CREATE INDEX IF NOT EXISTS idx_matching_alerts_is_read ON matching_alerts(is_read);
CREATE INDEX IF NOT EXISTS idx_matching_alerts_created_at ON matching_alerts(created_at);
CREATE INDEX IF NOT EXISTS idx_matching_alerts_priority ON matching_alerts(priority);
CREATE INDEX IF NOT EXISTS idx_matching_alerts_related_entity_id ON matching_alerts(related_entity, entity_id);
CREATE INDEX IF NOT EXISTS idx_matching_alerts_expiry_date ON matching_alerts(expiry_date);

CREATE TABLE IF NOT EXISTS attendance_notifications (
    notification_id SERIAL PRIMARY KEY,
    notification_type TEXT NOT NULL CHECK (notification_type IN ('勤怠提出リマインド', '勤怠承認依頼', '勤怠承認完了', '勤怠差戻', '申請期限超過', 'その他')),
    user_id UUID NOT NULL REFERENCES users(user_id),
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    related_entity TEXT NOT NULL CHECK (related_entity IN ('monthly_attendance', 'daily_attendance', 'approval', 'other')),
    entity_id INTEGER,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    notify_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actions JSONB,
    expiry_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_attendance_notifications_user_id ON attendance_notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_attendance_notifications_is_read ON attendance_notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_attendance_notifications_notify_at ON attendance_notifications(notify_at);
CREATE INDEX IF NOT EXISTS idx_attendance_notifications_type ON attendance_notifications(notification_type);
CREATE INDEX IF NOT EXISTS idx_attendance_notifications_related_entity_id ON attendance_notifications(related_entity, entity_id);
CREATE INDEX IF NOT EXISTS idx_attendance_notifications_expiry_date ON attendance_notifications(expiry_date);

CREATE TABLE IF NOT EXISTS system_notifications (
    notification_id SERIAL PRIMARY KEY,
    notification_type TEXT NOT NULL CHECK (notification_type IN ('システム', 'アカウント', 'セキュリティ', 'メンテナンス', 'その他')) DEFAULT 'システム',
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    target_user_id UUID REFERENCES users(user_id),
    target_role_id UUID REFERENCES roles(role_id),
    is_global BOOLEAN NOT NULL DEFAULT FALSE,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    published_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    importance TEXT CHECK (importance IN ('お知らせ', '重要', '緊急')) DEFAULT 'お知らせ',
    actions JSONB,
    link_url VARCHAR(255),
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_system_notifications_target_user_id ON system_notifications(target_user_id);
CREATE INDEX IF NOT EXISTS idx_system_notifications_target_role_id ON system_notifications(target_role_id);
CREATE INDEX IF NOT EXISTS idx_system_notifications_is_global ON system_notifications(is_global);
CREATE INDEX IF NOT EXISTS idx_system_notifications_is_read ON system_notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_system_notifications_published_at ON system_notifications(published_at);
CREATE INDEX IF NOT EXISTS idx_system_notifications_expires_at ON system_notifications(expires_at);
CREATE INDEX IF NOT EXISTS idx_system_notifications_importance ON system_notifications(importance);

CREATE TRIGGER update_system_notifications_timestamp
BEFORE UPDATE ON system_notifications
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS email_notifications (
    email_id SERIAL PRIMARY KEY,
    template_id INTEGER REFERENCES notification_templates(template_id),
    recipient_email VARCHAR(255) NOT NULL,
    recipient_name VARCHAR(100),
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    cc_list TEXT[],
    bcc_list TEXT[],
    attachments TEXT[],
    status TEXT CHECK (status IN ('待機', '送信中', '送信済', '失敗', 'キャンセル')) DEFAULT '待機',
    sent_at TIMESTAMP,
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP,
    related_entity_type VARCHAR(100),
    related_entity_id INTEGER,
    created_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_email_notifications_recipient_email ON email_notifications(recipient_email);
CREATE INDEX IF NOT EXISTS idx_email_notifications_status ON email_notifications(status);
CREATE INDEX IF NOT EXISTS idx_email_notifications_sent_at ON email_notifications(sent_at);
CREATE INDEX IF NOT EXISTS idx_email_notifications_next_retry_at ON email_notifications(next_retry_at);
CREATE INDEX IF NOT EXISTS idx_email_notifications_related_entity ON email_notifications(related_entity_type, related_entity_id);

CREATE TRIGGER update_email_notifications_timestamp
BEFORE UPDATE ON email_notifications
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS notification_preferences (
    preference_id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(user_id),
    notification_type VARCHAR(100) NOT NULL,
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    system_notification_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sms_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    mobile_push_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    frequency TEXT CHECK (frequency IN ('即時', '日次', '週次', '無効')) DEFAULT '即時',
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, notification_type)
);

CREATE INDEX IF NOT EXISTS idx_notification_preferences_user_id ON notification_preferences(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_preferences_notification_type ON notification_preferences(notification_type);

CREATE TRIGGER update_notification_preferences_timestamp
BEFORE UPDATE ON notification_preferences
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

COMMENT ON TABLE notification_templates IS '通知テンプレートを管理するテーブル';
COMMENT ON TABLE matching_alerts IS 'マッチング関連の通知を管理するテーブル';
COMMENT ON TABLE attendance_notifications IS '勤怠・工数関連の通知を管理するテーブル';
COMMENT ON TABLE system_notifications IS 'システム全般の通知を管理するテーブル';
COMMENT ON TABLE email_notifications IS 'メール通知の履歴を管理するテーブル';
COMMENT ON TABLE notification_preferences IS '通知設定を管理するテーブル';