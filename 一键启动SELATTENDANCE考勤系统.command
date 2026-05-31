#!/bin/zsh
set -e

# Finder 双击 .command 时常常拿不到交互式 shell PATH，这里先补齐常见 Homebrew 与系统命令路径。
export PATH="/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:$PATH"
# 如果用户本机在 zprofile 里声明了 Node、Java 或 nvm，这里尽量补载一次，避免双击启动时找不到命令。
[ -f "$HOME/.zprofile" ] && source "$HOME/.zprofile" >/dev/null 2>&1 || true
# zshrc 中若额外配置了 npm 或 java 的 PATH，这里也一并兼容加载。
[ -f "$HOME/.zshrc" ] && source "$HOME/.zshrc" >/dev/null 2>&1 || true

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_SCRIPT="${SCRIPT_DIR}/SELSP/启动SELATTENDANCE考勤后台服务.command"
FRONTEND_SCRIPT="${SCRIPT_DIR}/SELVUE/打开SELATTENDANCE考勤前端.command"
BACKEND_URL="http://127.0.0.1:8090/api/attendance/bootstrap"
FRONTEND_URL="http://127.0.0.1:5180/"
BACKEND_WRAPPER_PID=""
FRONTEND_WRAPPER_PID=""

ensure_command() {
  local command_name="$1"
  local command_label="$2"
  if ! command -v "$command_name" >/dev/null 2>&1; then
    echo "${command_label} 未安装或当前 PATH 不可见：${command_name}"
    exit 1
  fi
}

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

ensure_command "curl" "启动脚本依赖"

"$BACKEND_SCRIPT" &
BACKEND_WRAPPER_PID=$!
wait_for_url "$BACKEND_URL" "SELATTENDANCE 考勤后台" 120

"$FRONTEND_SCRIPT" &
FRONTEND_WRAPPER_PID=$!
wait_for_url "$FRONTEND_URL" "SELATTENDANCE 考勤前端" 90

echo "前后端已按顺序启动完成。关闭这个窗口后，两套服务会一并停止。"
wait "$BACKEND_WRAPPER_PID" "$FRONTEND_WRAPPER_PID"
