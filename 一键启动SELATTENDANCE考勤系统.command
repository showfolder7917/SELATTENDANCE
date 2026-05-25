#!/bin/zsh
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_SCRIPT="${SCRIPT_DIR}/SELSP/启动SELATTENDANCE考勤后台服务.command"
FRONTEND_SCRIPT="${SCRIPT_DIR}/SELVUE/打开SELATTENDANCE考勤前端.command"
BACKEND_URL="http://127.0.0.1:8090/api/attendance/bootstrap"
FRONTEND_URL="http://127.0.0.1:5180/"
BACKEND_WRAPPER_PID=""
FRONTEND_WRAPPER_PID=""

stop_wrapper() {
  local pid="$1"
  if [ -z "$pid" ]; then
    return
  fi
  if ps -p "$pid" >/dev/null 2>&1; then
    kill -TERM "$pid" >/dev/null 2>&1 || true
    wait "$pid" 2>/dev/null || true
  fi
  if ps -p "$pid" >/dev/null 2>&1; then
    kill -KILL "$pid" >/dev/null 2>&1 || true
    wait "$pid" 2>/dev/null || true
  fi
}

wait_for_url() {
  local url="$1"
  local label="$2"
  local retries="${3:-90}"
  for _ in $(seq 1 "$retries"); do
    if curl -fsS "$url" >/dev/null 2>&1; then
      echo "${label} 已就绪：${url}"
      return 0
    fi
    sleep 1
  done
  echo "${label} 启动超时：${url}"
  return 1
}

cleanup() {
  echo ""
  echo "正在停止 SELATTENDANCE 前后端服务..."
  stop_wrapper "$FRONTEND_WRAPPER_PID"
  stop_wrapper "$BACKEND_WRAPPER_PID"
}

trap cleanup EXIT INT TERM HUP

echo ""
echo "正在一键启动 SELATTENDANCE 考勤系统..."
echo "后端脚本: ${BACKEND_SCRIPT}"
echo "前端脚本: ${FRONTEND_SCRIPT}"
echo ""

"$BACKEND_SCRIPT" &
BACKEND_WRAPPER_PID=$!
wait_for_url "$BACKEND_URL" "SELATTENDANCE 考勤后台" 120

"$FRONTEND_SCRIPT" &
FRONTEND_WRAPPER_PID=$!
wait_for_url "$FRONTEND_URL" "SELATTENDANCE 考勤前端" 90

echo "前后端已按顺序启动完成。关闭这个窗口后，两套服务会一并停止。"
wait "$BACKEND_WRAPPER_PID" "$FRONTEND_WRAPPER_PID"
