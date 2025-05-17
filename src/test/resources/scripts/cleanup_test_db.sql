-- テスト用データベースのクリーンアップスクリプト
-- テスト実行前に実行して、クリーンな状態からテストを実行するために使用します

-- テーブルリレーションシップからの制約エラーを避けるために一時的にトランザクションを設定
SET session_replication_role = 'replica';

-- Flyway履歴テーブルを削除
DROP TABLE IF EXISTS public.flyway_schema_history CASCADE;

-- 既存テーブルをクリーンアップ（スキーマを保持したまま）
DO $$
DECLARE
    schema_var text;
    tables RECORD;
BEGIN
    -- テスト関連のすべてのスキーマに対して
    FOR schema_var IN 
        SELECT nspname FROM pg_namespace 
        WHERE nspname IN ('auth', 'common', 'engineer', 'project', 'matching', 
                         'contract', 'timesheet', 'billing', 'reporting', 'audit')
    LOOP
        -- スキーマ内のすべてのテーブルをループ
        FOR tables IN 
            SELECT tablename FROM pg_tables WHERE schemaname = schema_var
        LOOP
            EXECUTE 'DROP TABLE IF EXISTS ' || schema_var || '.' || tables.tablename || ' CASCADE;';
        END LOOP;
    END LOOP;

    -- スキーマを削除（存在する場合のみ）
    DROP SCHEMA IF EXISTS auth CASCADE;
    DROP SCHEMA IF EXISTS common CASCADE;
    DROP SCHEMA IF EXISTS engineer CASCADE;
    DROP SCHEMA IF EXISTS project CASCADE;
    DROP SCHEMA IF EXISTS matching CASCADE;
    DROP SCHEMA IF EXISTS contract CASCADE;
    DROP SCHEMA IF EXISTS timesheet CASCADE;
    DROP SCHEMA IF EXISTS billing CASCADE;
    DROP SCHEMA IF EXISTS reporting CASCADE;
    DROP SCHEMA IF EXISTS audit CASCADE;
    
    RAISE NOTICE 'All schemas and tables have been dropped successfully';
END $$;

-- 通常のトランザクション設定に戻す
SET session_replication_role = 'origin';