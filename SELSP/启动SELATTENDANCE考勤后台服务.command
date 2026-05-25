#!/bin/zsh
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

PORT=8090
URL="http://127.0.0.1:${PORT}/api/attendance/bootstrap"
LOG_FILE="/tmp/selattendance_backend.log"
SERVER_PID=""

stop_port_processes() {
  local pids
  pids="$(/usr/sbin/lsof -ti tcp:${PORT} 2>/dev/null || true)"
  if [ -n "$pids" ]; then
    echo "检测到 ${PORT} 端口已有旧进程，先停止后再由当前脚本接管..."
    for pid in $pids; do
      kill "$pid" >/dev/null 2>&1 || true
    done
    sleep 1
    pids="$(/usr/sbin/lsof -ti tcp:${PORT} 2>/dev/null || true)"
    if [ -n "$pids" ]; then
      for pid in $pids; do
        kill -9 "$pid" >/dev/null 2>&1 || true
      done
    fi
  fi
}

cleanup() {
  if [ -n "$SERVER_PID" ]; then
    kill "$SERVER_PID" >/dev/null 2>&1 || true
    pkill -TERM -P "$SERVER_PID" >/dev/null 2>&1 || true
    sleep 1
    pkill -KILL -P "$SERVER_PID" >/dev/null 2>&1 || true
  fi
  stop_port_processes
}

trap cleanup EXIT INT TERM HUP

echo ""
echo "SELATTENDANCE 考勤后台服务正在启动..."
echo "项目目录: ${SCRIPT_DIR}"
echo "访问地址: ${URL}"
echo ""

stop_port_processes

./gradlew bootRun >"$LOG_FILE" 2>&1 &
SERVER_PID=$!

for _ in {1..120}; do
  if curl -fsS "$URL" >/dev/null 2>&1; then
    echo "后端已启动。关闭这个终端窗口时，后端服务会一并停止。"
    wait "$SERVER_PID"
    exit 0
  fi
  sleep 1
done

echo "启动超时：后端未能在 120 秒内连接到 ${URL}"
if [ -f "$LOG_FILE" ]; then
  echo ""
  echo "最近日志："
  tail -n 30 "$LOG_FILE" || true
fi
exit 1
