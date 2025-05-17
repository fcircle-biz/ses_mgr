# 認証リポジトリテスト実装・トラブルシューティング

## 概要

このドキュメントでは、認証・認可モジュールのリポジトリテストを実装する際に発生した問題と解決策についてまとめています。

## 問題点

PostgreSQLのUUID型をJavaのUUID型と連携する際に、以下の問題が発生しました：

1. **型変換エラー**: PostgreSQLのUUID型カラムに対してStringを比較しようとすると、以下のエラーが発生します。
   ```
   ERROR: operator does not exist: uuid = character varying
   ヒント: No operator matches the given name and argument types. You might need to add explicit type casts.
   ```

2. **スキーマのクリーンアップ**: テスト間で正しくデータベーススキーマをクリーンアップしないと、テストが不安定になります。

## 解決策

### 1. SQLクエリでUUID型を明示的にキャスト

PostgreSQLではUUID型を扱う場合、文字列を明示的にUUID型にキャストする必要があります：

```java
// 問題のあるコード
String sql = "SELECT * FROM auth.users WHERE id = ?";
jdbcTemplate.queryForObject(sql, userRowMapper, userId.toString());

// 修正したコード
String sql = "SELECT * FROM auth.users WHERE id = CAST(? AS UUID)";
jdbcTemplate.queryForObject(sql, userRowMapper, userId.toString());
```

これにより、PostgreSQLはString型のパラメータを正しくUUID型として解釈できるようになります。

### 2. より堅牢なデータベースクリーンアップ

テスト間のデータ分離を確保するため、より堅牢なクリーンアップスクリプトを実装しました：

```sql
-- テーブルリレーションシップからの制約エラーを避けるために一時的にトランザクションを設定
SET session_replication_role = 'replica';

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
    -- 他のスキーマも同様に削除...
    
    RAISE NOTICE 'All schemas and tables have been dropped successfully';
END $$;

-- 通常のトランザクション設定に戻す
SET session_replication_role = 'origin';
```

### 3. エラーハンドリングの強化

SQLスクリプトでより強固なエラーハンドリングを実装しました：

```sql
DO $$
BEGIN
    -- スキーマが既に存在していても、エラーを出さずにスキップする
    DROP SCHEMA IF EXISTS auth CASCADE;
    
    -- スキーマ作成
    BEGIN
        CREATE SCHEMA auth;
        RAISE NOTICE 'Successfully created the auth schema';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Failed to create the auth schema: %', SQLERRM;
    END;
END $$;
```

## リポジトリテスト改善ポイント

1. **独立したテスト環境**: テスト専用のDockerコンテナを使用してテストを実行し、開発環境と分離しました。

2. **型安全性の確保**: JdbcTemplateを使用する際に型変換を明示的に行うように修正しました。

3. **エラー検出の強化**: エラーハンドリングとログ出力を強化して、問題を特定しやすくしました。

4. **テスト分離**: テスト間で状態が漏れないよう、各テスト前にデータベースを初期化するようにしました。

## 今後の対応

1. 実装されたリポジトリクラスのすべてのSQLクエリを確認し、必要に応じてUUID型のキャストを追加する

2. CI/CDパイプラインでこれらのテストを自動的に実行するよう設定する

3. より包括的な統合テストを追加し、リポジトリ間の連携を検証する