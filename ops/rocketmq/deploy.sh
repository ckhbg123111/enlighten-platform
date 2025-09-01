#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR=$(cd "$(dirname "$0")" && pwd)
COMPOSE_FILE="$PROJECT_DIR/docker-compose.yml"

echo "[1/6] 检查 Docker 与 docker compose..."
if ! command -v docker >/dev/null 2>&1; then
  echo "错误: 未找到 docker，请先安装 Docker Desktop 或 Docker Engine。" >&2
  exit 1
fi

# 支持 docker compose V2 (docker compose) 与 V1 (docker-compose)
if docker compose version >/dev/null 2>&1; then
  COMPOSE_CMD="docker compose"
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE_CMD="docker-compose"
else
  echo "错误: 未找到 docker compose，请安装 Docker Compose。" >&2
  exit 1
fi

echo "[2/6] 创建必要目录..."
mkdir -p "$PROJECT_DIR/conf" \
         "$PROJECT_DIR/data/namesrv/logs" \
         "$PROJECT_DIR/data/broker/logs" \
         "$PROJECT_DIR/data/broker/store"

if [ ! -f "$PROJECT_DIR/conf/broker.conf" ]; then
  echo "未发现 conf/broker.conf，自动创建默认配置..."
  cat > "$PROJECT_DIR/conf/broker.conf" <<'EOF'
brokerClusterName=DefaultCluster
brokerName=broker-a
brokerId=0
deleteWhen=04
fileReservedTime=48
brokerRole=ASYNC_MASTER
flushDiskType=ASYNC_FLUSH
listenPort=10911
autoCreateTopicEnable=true
autoCreateSubscriptionGroup=true
maxMessageSize=4194304
storePathRootDir=/home/rocketmq/store
storePathCommitLog=/home/rocketmq/store/commitlog
storePathConsumeQueue=/home/rocketmq/store/consumequeue
storePathIndex=/home/rocketmq/store/index
mappedFileSizeCommitLog=1073741824
namesrvAddr=namesrv:9876
EOF
fi

echo "[3/6] 拉取镜像..."
$COMPOSE_CMD -f "$COMPOSE_FILE" pull | cat

echo "[4/6] 启动服务..."
$COMPOSE_CMD -f "$COMPOSE_FILE" up -d | cat

echo "[5/6] 健康检查: 等待 NameServer 与 Broker 启动..."
wait_for_port() {
  local host="$1"; shift
  local port="$1"; shift
  local retries=${1:-60}
  local interval=${2:-2}
  for _ in $(seq 1 "$retries"); do
    if (echo > /dev/tcp/$host/$port) >/dev/null 2>&1; then
      return 0
    fi
    sleep "$interval"
  done
  return 1
}

if ! wait_for_port 127.0.0.1 9876 60 2; then
  echo "警告: NameServer 端口 9876 未在预期时间内就绪，请检查容器日志。" >&2
fi

if ! wait_for_port 127.0.0.1 10911 60 2; then
  echo "警告: Broker 端口 10911 未在预期时间内就绪，请检查容器日志。" >&2
fi

echo "[6/6] 信息"
echo "- RocketMQ NameServer: 127.0.0.1:9876"
echo "- RocketMQ Broker: 127.0.0.1:10911"
echo "- RocketMQ Dashboard: http://127.0.0.1:8090 (登录页加载后左上角即可看到集群)"
echo "- 数据目录: $PROJECT_DIR/data"
echo "- 配置目录: $PROJECT_DIR/conf"

echo "\n完成。一键部署成功。"


