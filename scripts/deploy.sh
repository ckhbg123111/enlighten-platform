#!/usr/bin/env bash

set -euo pipefail

# Defaults
ENV_FILE="./.env"
DO_BUILD=true

print_usage() {
  echo "Usage: $0 [-e ENV_FILE] [--no-build]"
  echo "  -e, --env-file   Path to env file (default: ./.env; falls back to ./env or ./env.sample)"
  echo "      --no-build   Skip docker compose build"
}

# Parse args
while [[ $# -gt 0 ]]; do
  case "$1" in
    -e|--env-file)
      if [[ $# -lt 2 ]]; then
        echo "Missing value for $1" >&2
        exit 1
      fi
      ENV_FILE="$2"
      shift 2
      ;;
    --no-build)
      DO_BUILD=false
      shift
      ;;
    -h|--help)
      print_usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      print_usage
      exit 1
      ;;
  esac
done

# Move to repo root (parent of this script)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${REPO_ROOT}"

echo "[Enlighten] Starting deployment with Docker Compose..."

# Resolve env file
if [[ ! -f "${ENV_FILE}" ]]; then
  if [[ -f "./env" ]]; then
    ENV_FILE="./env"
  elif [[ -f "./env.sample" ]]; then
    ENV_FILE="./env.sample"
  else
    echo "Env file not found. Provide via -e/--env-file or create ./.env" >&2
    exit 1
  fi
fi

echo "Using env file: ${ENV_FILE}"

# Load env vars for local checks (e.g., APP_IMAGE in --no-build mode)
set -a
# shellcheck source=/dev/null
. "${ENV_FILE}"
set +a

# Verify compose file exists
COMPOSE_FILE="docker-compose.yml"
if [[ ! -f "${COMPOSE_FILE}" ]]; then
  echo "${COMPOSE_FILE} not found in ${REPO_ROOT}" >&2
  exit 1
fi

# Ensure required data directories exist
echo "Ensuring data directories exist..."
mkdir -p ./data/mysql ./data/redis

# Detect docker compose command (v2: docker compose, v1: docker-compose)
compose_cmd=(docker compose)
if ! command -v docker >/dev/null 2>&1; then
  echo "docker is required but not found in PATH" >&2
  exit 1
fi
if docker compose version >/dev/null 2>&1; then
  compose_cmd=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  compose_cmd=(docker-compose)
else
  echo "Neither 'docker compose' nor 'docker-compose' is available" >&2
  exit 1
fi

# Build / Pull
if [[ "${DO_BUILD}" == "true" ]]; then
  # 显式拉取第三方基础镜像，避免隐式行为
  "${compose_cmd[@]}" --env-file "${ENV_FILE}" pull mysql redis | cat
  "${compose_cmd[@]}" --env-file "${ENV_FILE}" build
else
  # no-build 场景需确保 app 镜像可用
  if [[ -z "${APP_IMAGE:-}" ]]; then
    echo "APP_IMAGE 未设置，并且使用了 --no-build，无法拉取应用镜像。" >&2
    exit 1
  fi
  "${compose_cmd[@]}" --env-file "${ENV_FILE}" pull app mysql redis | cat
fi

# Up
"${compose_cmd[@]}" --env-file "${ENV_FILE}" up -d

echo "[Enlighten] Done. Check logs with: ${compose_cmd[*]} logs -f app"


