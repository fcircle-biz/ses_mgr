-- 部門データの登録
DO $$
BEGIN
    -- 部門データの登録
    BEGIN
        INSERT INTO auth.departments (id, name, description, created_at)
        VALUES
            ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', '開発部', '開発部門', CURRENT_TIMESTAMP),
            ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', '営業部', '営業部門', CURRENT_TIMESTAMP),
            ('b2eebc99-9c0b-4ef8-bb6d-6bb9bd380a24', '管理部', '管理部門', CURRENT_TIMESTAMP);
        RAISE NOTICE 'Successfully inserted department data';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Failed to insert department data: %', SQLERRM;
    END;
END $$;

-- ロールデータの登録
DO $$
BEGIN
    BEGIN
        INSERT INTO auth.roles (id, name, description, is_system_role, created_at)
        VALUES
            ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'ADMIN', '管理者', TRUE, CURRENT_TIMESTAMP),
            ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380a34', 'USER', '一般ユーザー', TRUE, CURRENT_TIMESTAMP),
            ('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a35', 'READONLY', '閲覧のみ', FALSE, CURRENT_TIMESTAMP);
        RAISE NOTICE 'Successfully inserted role data';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Failed to insert role data: %', SQLERRM;
    END;
END $$;

-- パーミッションデータの登録
DO $$
BEGIN
    BEGIN
        INSERT INTO auth.permissions (id, name, description, created_at)
        VALUES
            ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'USER_READ', 'ユーザー情報参照', CURRENT_TIMESTAMP),
            ('d1eebc99-9c0b-4ef8-bb6d-6bb9bd380a45', 'USER_WRITE', 'ユーザー情報更新', CURRENT_TIMESTAMP),
            ('d2eebc99-9c0b-4ef8-bb6d-6bb9bd380a46', 'USER_DELETE', 'ユーザー削除', CURRENT_TIMESTAMP),
            ('d3eebc99-9c0b-4ef8-bb6d-6bb9bd380a47', 'ROLE_READ', 'ロール情報参照', CURRENT_TIMESTAMP),
            ('d4eebc99-9c0b-4ef8-bb6d-6bb9bd380a48', 'ROLE_WRITE', 'ロール情報更新', CURRENT_TIMESTAMP);
        RAISE NOTICE 'Successfully inserted permission data';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Failed to insert permission data: %', SQLERRM;
    END;
END $$;

-- ユーザーデータの登録
DO $$
BEGIN
    BEGIN
        INSERT INTO auth.users (
            id, username, email, password_hash, first_name, last_name, 
            department_id, is_enabled, is_account_locked, account_expire_date, 
            credentials_expire_date, last_login_date, login_fail_count, 
            authentication_method, is_mfa_enabled, created_at
        )
        VALUES
            (
                'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'admin', 'admin@example.com',
                '$2a$10$rFJpYpWHMeK.cJgO4j7o6.PdKmHmo9BUGh2QkEZ.gF6JU0XvHbfxS', 'Admin', 'User',
                'b2eebc99-9c0b-4ef8-bb6d-6bb9bd380a24', TRUE, FALSE, NULL,
                CURRENT_TIMESTAMP + INTERVAL '90 days', CURRENT_TIMESTAMP, 0,
                'PASSWORD', FALSE, CURRENT_TIMESTAMP
            ),
            (
                'a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'user1', 'user1@example.com',
                '$2a$10$rFJpYpWHMeK.cJgO4j7o6.PdKmHmo9BUGh2QkEZ.gF6JU0XvHbfxS', 'Test', 'User',
                'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', TRUE, FALSE, NULL,
                CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '30 days', 0,
                'PASSWORD', TRUE, CURRENT_TIMESTAMP
            ),
            (
                'a2eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'locked', 'locked@example.com',
                '$2a$10$rFJpYpWHMeK.cJgO4j7o6.PdKmHmo9BUGh2QkEZ.gF6JU0XvHbfxS', 'Locked', 'User',
                'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', TRUE, TRUE, NULL,
                CURRENT_TIMESTAMP + INTERVAL '90 days', CURRENT_TIMESTAMP - INTERVAL '60 days', 5,
                'PASSWORD', FALSE, CURRENT_TIMESTAMP
            );
        RAISE NOTICE 'Successfully inserted user data';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Failed to insert user data: %', SQLERRM;
    END;
END $$;

-- ユーザーロール関連付け
DO $$
BEGIN
    BEGIN
        INSERT INTO auth.user_roles (user_id, role_id, created_at)
        VALUES
            ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', CURRENT_TIMESTAMP),
            ('a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'c1eebc99-9c0b-4ef8-bb6d-6bb9bd380a34', CURRENT_TIMESTAMP),
            ('a2eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a35', CURRENT_TIMESTAMP);
        RAISE NOTICE 'Successfully inserted user_roles data';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Failed to insert user_roles data: %', SQLERRM;
    END;
END $$;

-- ロールパーミッション関連付け
DO $$
BEGIN
    BEGIN
        INSERT INTO auth.role_permissions (role_id, permission_id, created_at)
        VALUES
            ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', CURRENT_TIMESTAMP),
            ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'd1eebc99-9c0b-4ef8-bb6d-6bb9bd380a45', CURRENT_TIMESTAMP),
            ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'd2eebc99-9c0b-4ef8-bb6d-6bb9bd380a46', CURRENT_TIMESTAMP),
            ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'd3eebc99-9c0b-4ef8-bb6d-6bb9bd380a47', CURRENT_TIMESTAMP),
            ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'd4eebc99-9c0b-4ef8-bb6d-6bb9bd380a48', CURRENT_TIMESTAMP),
            ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380a34', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', CURRENT_TIMESTAMP),
            ('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380a34', 'd3eebc99-9c0b-4ef8-bb6d-6bb9bd380a47', CURRENT_TIMESTAMP),
            ('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a35', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', CURRENT_TIMESTAMP);
        RAISE NOTICE 'Successfully inserted role_permissions data';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Failed to insert role_permissions data: %', SQLERRM;
    END;
END $$;

-- リフレッシュトークンデータの登録
DO $$
BEGIN
    BEGIN
        INSERT INTO auth.refresh_tokens (id, user_id, token, expiry_date, is_revoked, created_at)
        VALUES
            (
                'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
                'refresh-token-admin', CURRENT_TIMESTAMP + INTERVAL '7 days', FALSE, CURRENT_TIMESTAMP
            ),
            (
                'e1eebc99-9c0b-4ef8-bb6d-6bb9bd380a56', 'a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a12',
                'refresh-token-user1', CURRENT_TIMESTAMP + INTERVAL '7 days', FALSE, CURRENT_TIMESTAMP
            ),
            (
                'e2eebc99-9c0b-4ef8-bb6d-6bb9bd380a57', 'a2eebc99-9c0b-4ef8-bb6d-6bb9bd380a13',
                'refresh-token-locked', CURRENT_TIMESTAMP - INTERVAL '1 day', TRUE, CURRENT_TIMESTAMP
            );
        RAISE NOTICE 'Successfully inserted refresh_tokens data';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Failed to insert refresh_tokens data: %', SQLERRM;
    END;
END $$;

-- パスワードリセットトークンデータの登録
DO $$
BEGIN
    BEGIN
        INSERT INTO auth.password_reset_tokens (id, user_id, token, expiry_date, used, created_at)
        VALUES
            (
                'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
                'reset-token-admin', CURRENT_TIMESTAMP + INTERVAL '1 day', FALSE, CURRENT_TIMESTAMP
            ),
            (
                'f1eebc99-9c0b-4ef8-bb6d-6bb9bd380a67', 'a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a12',
                'reset-token-user1', CURRENT_TIMESTAMP - INTERVAL '1 day', FALSE, CURRENT_TIMESTAMP
            ),
            (
                'f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a68', 'a2eebc99-9c0b-4ef8-bb6d-6bb9bd380a13',
                'reset-token-used', CURRENT_TIMESTAMP + INTERVAL '1 day', TRUE, CURRENT_TIMESTAMP
            );
        RAISE NOTICE 'Successfully inserted password_reset_tokens data';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Failed to insert password_reset_tokens data: %', SQLERRM;
    END;
END $$;