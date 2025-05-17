#!/bin/bash

# 色の設定
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}基本リポジトリテストを実行します...${NC}"

# Dockerコンテナが起動しているか確認
if [ $(docker ps | grep ses-mgr-postgres-test | wc -l) -eq 0 ]; then
    echo -e "${RED}テストデータベースのDockerコンテナが起動していません。${NC}"
    echo -e "${YELLOW}docker-compose upを実行してください。${NC}"
    exit 1
fi

# テストデータベースをクリーンアップ
echo -e "${YELLOW}テストデータベースをクリーンアップしています...${NC}"
docker exec -i ses-mgr-postgres-test psql -U postgres_test -d ses_mgr_test -c "DROP SCHEMA IF EXISTS auth CASCADE;"

# 基本リポジトリテストを実行
echo -e "${YELLOW}BasicUserRepositoryTestを実行します...${NC}"
./gradlew test --tests "jp.co.example.sesapp.common.auth.repository.jdbc.BasicUserRepositoryTest"

echo -e "${GREEN}基本リポジトリテストの実行が完了しました。${NC}"