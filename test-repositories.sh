#!/bin/bash

# 色の設定
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}認証リポジトリテストを実行します...${NC}"

# Dockerコンテナが起動しているか確認
if [ $(docker ps | grep ses-mgr-postgres-test | wc -l) -eq 0 ]; then
    echo -e "${RED}テストデータベースのDockerコンテナが起動していません。${NC}"
    echo -e "${YELLOW}docker-compose upを実行してください。${NC}"
    exit 1
fi

# テストデータベースをクリーンアップ
echo -e "${YELLOW}テストデータベースをクリーンアップしています...${NC}"
cat ./src/test/resources/scripts/cleanup_test_db.sql | docker exec -i ses-mgr-postgres-test psql -U postgres_test -d ses_mgr_test

# 認証スキーマを初期化
echo -e "${YELLOW}認証スキーマを初期化しています...${NC}"
cat ./src/test/resources/db/testdata/init_auth_schema.sql | docker exec -i ses-mgr-postgres-test psql -U postgres_test -d ses_mgr_test

# テストユーザーデータ挿入
echo -e "${YELLOW}テストユーザーデータを挿入しています...${NC}"
cat ./src/test/resources/db/testdata/test_users.sql | docker exec -i ses-mgr-postgres-test psql -U postgres_test -d ses_mgr_test

# テーブルが作成されたか確認
echo -e "${YELLOW}テーブルが正しく作成されたか確認しています...${NC}"
docker exec -i ses-mgr-postgres-test psql -U postgres_test -d ses_mgr_test -c "\dt auth.*"

# 特定のリポジトリテストだけを実行
echo -e "${YELLOW}JdbcUserRepositoryTestを実行します...${NC}"
./gradlew test --tests "jp.co.example.sesapp.common.auth.repository.jdbc.JdbcUserRepositoryTest"

echo -e "${GREEN}リポジトリテストの実行が完了しました。${NC}"