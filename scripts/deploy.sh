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

# Verify compose file exists
COMPOSE_FILE="docker-compose.yml"
if [[ ! -f "${COMPOSE_FILE}" ]]; then
  echo "${COMPOSE_FILE} not found in ${REPO_ROOT}" >&2
  exit 1
fi

# Ensure required data directories exist (MinIO)
echo "Ensuring data directories exist..."
mkdir -p ./data/minio/data ./data/minio/config

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

# Build
if [[ "${DO_BUILD}" == "true" ]]; then
  "${compose_cmd[@]}" --env-file "${ENV_FILE}" build
fi

# Up
"${compose_cmd[@]}" --env-file "${ENV_FILE}" up -d

echo "[Enlighten] Done. Check logs with: ${compose_cmd[*]} logs -f app"


