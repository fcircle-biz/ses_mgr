#!/bin/bash

# エラーが発生したら即座に終了
set -e

# 色の定義
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}テスト用データベースの初期化を開始...${NC}"

# ヘルスチェックが成功するまで待機
MAX_RETRIES=30
RETRY_COUNT=0

while ! docker exec ses-mgr-postgres-test pg_isready -U postgres_test > /dev/null 2>&1; do
  RETRY_COUNT=$((RETRY_COUNT+1))
  if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
    echo -e "${RED}エラー: PostgreSQLコンテナの起動タイムアウト${NC}"
    exit 1
  fi
  echo "PostgreSQLの準備を待機中... ($RETRY_COUNT/$MAX_RETRIES)"
  sleep 2
done

echo -e "${GREEN}PostgreSQL接続準備完了${NC}"

# クリーンアップスクリプトを実行
echo -e "${YELLOW}テストデータベースのクリーンアップを実行...${NC}"
cat ./src/test/resources/scripts/cleanup_test_db.sql | docker exec -i ses-mgr-postgres-test psql -U postgres_test -d ses_mgr_test

echo -e "${GREEN}テストデータベースのクリーンアップ完了${NC}"

# Flyway移行を実行
echo -e "${YELLOW}Flyway移行を実行...${NC}"
./gradlew flywayMigrateTest

echo -e "${GREEN}テストデータベース初期化完了${NC}"