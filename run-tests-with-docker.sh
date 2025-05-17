#!/bin/bash

# エラーが発生したら即座に終了
set -e

# 色の定義
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}SES管理システム テスト実行スクリプト${NC}"
echo "-------------------------------------"

# 引数からテストクラスを取得（指定がなければ全テスト実行）
TEST_CLASS=""
if [ $# -gt 0 ]; then
  TEST_CLASS="--tests $1"
  echo -e "${YELLOW}テスト対象: $1${NC}"
fi

# Dockerコンテナが起動しているか確認
if ! docker info > /dev/null 2>&1; then
  echo -e "${RED}エラー: Dockerが起動していません。Dockerを起動してください。${NC}"
  exit 1
fi

# テスト用のDockerコンテナを起動
echo -e "${GREEN}テスト用のDockerコンテナを起動しています...${NC}"
docker compose -f docker-compose.test.yml up -d

# コンテナの起動を待機
echo -e "${YELLOW}PostgreSQLコンテナの起動を待機しています...${NC}"
sleep 5

# ヘルスチェックが成功するまで待機
MAX_RETRIES=30
RETRY_COUNT=0

while ! docker exec ses-mgr-postgres-test pg_isready -U postgres_test > /dev/null 2>&1; do
  RETRY_COUNT=$((RETRY_COUNT+1))
  if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
    echo -e "${RED}エラー: PostgreSQLコンテナの起動タイムアウト${NC}"
    docker compose -f docker-compose.test.yml down
    exit 1
  fi
  echo "PostgreSQLの準備を待機中... ($RETRY_COUNT/$MAX_RETRIES)"
  sleep 2
done

echo -e "${GREEN}PostgreSQLが準備完了しました${NC}"

# テストデータベースをクリーンアップ
echo -e "${YELLOW}テストデータベースをクリーンアップしています...${NC}"
cat ./src/test/resources/scripts/cleanup_test_db.sql | docker exec -i ses-mgr-postgres-test psql -U postgres_test -d ses_mgr_test
echo -e "${GREEN}テストデータベースのクリーンアップが完了しました${NC}"

# テストの種類に基づいてスキーマ初期化
if [[ "$1" == *"auth"* || "$1" == *"Auth"* || -z "$1" ]]; then
    echo -e "${YELLOW}認証スキーマを初期化しています...${NC}"
    if [ -f "./src/test/resources/db/testdata/init_auth_schema.sql" ]; then
        cat ./src/test/resources/db/testdata/init_auth_schema.sql | docker exec -i ses-mgr-postgres-test psql -U postgres_test -d ses_mgr_test
        echo -e "${GREEN}認証スキーマの初期化が完了しました${NC}"
    fi
fi

# マイグレーションはスキップ（テストクラスで直接初期化スクリプトを実行）

# テスト実行
echo -e "${YELLOW}テストを実行しています...${NC}"
# -Dspring.test.database.initializeで毎回初期化するように設定
./gradlew test $TEST_CLASS -Pspring.profiles.active=test-docker

# テスト結果を保存
TEST_RESULT=$?

# テスト結果に応じたメッセージ
if [ $TEST_RESULT -eq 0 ]; then
  echo -e "${GREEN}すべてのテストが成功しました！${NC}"
else
  echo -e "${RED}テスト実行中にエラーが発生しました。詳細はテストレポートを確認してください。${NC}"
fi

# Dockerコンテナを停止
echo -e "${YELLOW}テスト用のDockerコンテナを停止しています...${NC}"
docker compose -f docker-compose.test.yml down

echo -e "${GREEN}テスト環境のクリーンアップが完了しました${NC}"

# テスト結果に応じた終了コードを返す
exit $TEST_RESULT