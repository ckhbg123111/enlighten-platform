#!/usr/bin/env bash
set -euo pipefail

# Configurable variables (can be overridden by environment variables)
ELK_VERSION="${ELK_VERSION:-8.14.1}"
ES_JAVA_OPTS="${ES_JAVA_OPTS:--Xms1g -Xmx1g}"
LOG_DIR="${LOG_DIR:-/opt/enlighten/logs/app}"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "[1/6] Preparing directories..."
sudo mkdir -p "$LOG_DIR"
# Determine app user for directory ownership (prefer the non-root invoker)
APP_USER="${APP_USER:-${SUDO_USER:-$(id -un)}}"
APP_GROUP="${APP_GROUP:-$(id -gn "$APP_USER" 2>/dev/null || id -gn)}"
sudo chown -R "$APP_USER:$APP_GROUP" "$LOG_DIR"
sudo chmod 775 "$LOG_DIR"

echo "[2/6] Checking docker & compose..."
if ! command -v docker >/dev/null 2>&1; then
  echo "Docker not installed. Please install Docker first." >&2
  exit 1
fi
if ! docker compose version >/dev/null 2>&1; then
  echo "Docker Compose V2 not found. Please update Docker to include 'docker compose'." >&2
  exit 1
fi

echo "[3/6] Kernel settings for Elasticsearch (vm.max_map_count)"
CURRENT=$(sysctl -n vm.max_map_count || echo 65530)
if [ "$CURRENT" -lt 262144 ]; then
  echo "Setting vm.max_map_count to 262144 (requires sudo)"
  echo "vm.max_map_count=262144" | sudo tee /etc/sysctl.d/99-elastic.conf >/dev/null
  sudo sysctl -w vm.max_map_count=262144 >/dev/null
fi

echo "[4/6] Exporting environment for compose..."
export ELK_VERSION
export ES_JAVA_OPTS
export LOG_DIR

echo "[5/6] Starting ELK stack..."
cd "$SCRIPT_DIR"
docker compose pull
docker compose up -d

echo "[6/6] Health checks"
set +e
for i in {1..60}; do
  if curl -fsS "http://localhost:9200" >/dev/null 2>&1; then
    echo "Elasticsearch is up."
    break
  fi
  echo "Waiting for Elasticsearch... ($i/60)"; sleep 2
done
for i in {1..60}; do
  if curl -fsS "http://localhost:5601/api/status" >/dev/null 2>&1; then
    echo "Kibana is up."
    break
  fi
  echo "Waiting for Kibana... ($i/60)"; sleep 2
done
set -e

echo
echo "Done. Access Kibana at: http://<server_ip>:5601"
echo "Log directory mounted: $LOG_DIR"
echo "Index pattern in Kibana: app-logs-* (timestamp: @timestamp)"


