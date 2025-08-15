#!/usr/bin/env bash

set -euo pipefail

print_usage() {
  cat <<EOF
初始化 MySQL 数据库：若不存在则根据 SQL 脚本创建并初始化。

用法:
  $0 [options]

选项:
  -H, --host HOST            MySQL 主机 (默认: 127.0.0.1 或 $DB_HOST)
  -P, --port PORT            MySQL 端口 (默认: 3306 或 $DB_PORT)
  -u, --user USER            MySQL 用户 (默认: root 或 $DB_USER)
  -p, --password PASSWORD    MySQL 密码 (默认: 空 或 $DB_PASSWORD)
  -d, --database NAME        目标数据库名 (默认: enlighten_platform 或 $DB_NAME)
  -f, --sql-file FILE        初始化 SQL 文件 (默认: 仓库根目录的 database_init.sql 或 $DB_INIT_SQL)
      --use-container        通过 Docker 容器执行 (如果容器存在会自动启用)
      --container-name NAME  MySQL 容器名 (默认: enlighten-mysql)
  -h, --help                 显示帮助
EOF
}

# Defaults (env overrides)
HOST="${DB_HOST:-127.0.0.1}"
PORT="${DB_PORT:-3306}"
USER_NAME="${DB_USER:-root}"
PASSWORD="${DB_PASSWORD:-}"
DATABASE="${DB_NAME:-enlighten_platform}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
DEFAULT_SQL_FILE="${REPO_ROOT}/database_init.sql"
SQL_FILE="${DB_INIT_SQL:-$DEFAULT_SQL_FILE}"

USE_CONTAINER=false
CONTAINER_NAME="enlighten-mysql"

# Parse args
while [[ $# -gt 0 ]]; do
  case "$1" in
    -H|--host) HOST="$2"; shift 2 ;;
    -P|--port) PORT="$2"; shift 2 ;;
    -u|--user) USER_NAME="$2"; shift 2 ;;
    -p|--password) PASSWORD="$2"; shift 2 ;;
    -d|--database) DATABASE="$2"; shift 2 ;;
    -f|--sql-file) SQL_FILE="$2"; shift 2 ;;
    --use-container) USE_CONTAINER=true; shift ;;
    --container-name) CONTAINER_NAME="$2"; shift 2 ;;
    -h|--help) print_usage; exit 0 ;;
    *) echo "未知参数: $1" >&2; print_usage; exit 1 ;;
  esac
done

if [[ ! -f "$SQL_FILE" ]]; then
  echo "找不到初始化 SQL 文件: $SQL_FILE" >&2
  exit 1
fi

# Auto-detect container if present
if docker ps -a --format '{{.Names}}' | grep -qw "$CONTAINER_NAME"; then
  USE_CONTAINER=true
fi

echo "数据库主机: $HOST"
echo "数据库端口: $PORT"
echo "数据库用户: $USER_NAME"
echo "目标数据库: $DATABASE"
echo "初始化脚本: $SQL_FILE"
echo "通过容器执行: $USE_CONTAINER (容器名: $CONTAINER_NAME)"

mysql_cmd() {
  local mysql_args=("--protocol=TCP" "-h" "$HOST" "-P" "$PORT" "-u" "$USER_NAME")
  if [[ -n "$PASSWORD" ]]; then mysql_args+=("-p$PASSWORD"); fi
  if [[ "$USE_CONTAINER" == "true" ]]; then
    docker exec -i "$CONTAINER_NAME" mysql "${mysql_args[@]}"
  else
    command -v mysql >/dev/null 2>&1 || { echo "未找到 mysql 客户端，请安装或使用 --use-container" >&2; exit 1; }
    mysql "${mysql_args[@]}"
  fi
}

wait_for_mysql_healthy() {
  if [[ "$USE_CONTAINER" != "true" ]]; then return 0; fi
  if ! docker inspect "$CONTAINER_NAME" >/dev/null 2>&1; then return 0; fi
  echo "等待 MySQL 容器变为 healthy..."
  local start_ts=$(date +%s)
  local timeout=120
  while true; do
    local status
    status=$(docker inspect -f '{{.State.Health.Status}}' "$CONTAINER_NAME" 2>/dev/null || echo "unknown")
    if [[ "$status" == "healthy" ]]; then
      break
    fi
    if (( $(date +%s) - start_ts > timeout )); then
      echo "等待超时（> ${timeout}s），继续尝试连接 MySQL..." >&2
      break
    fi
    sleep 2
  done
}

database_exists() {
  local out
  if ! out=$(mysql_cmd -N -B -e "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='${DATABASE}';" 2>/dev/null); then
    return 1
  fi
  [[ "${out}" == "${DATABASE}" ]]
}

wait_for_mysql_healthy

if database_exists; then
  echo "数据库已存在：$DATABASE，无需初始化。"
  exit 0
fi

echo "数据库不存在，将执行初始化 ..."
if ! mysql_cmd < "$SQL_FILE"; then
  echo "初始化失败。" >&2
  exit 1
fi
echo "初始化完成。"


