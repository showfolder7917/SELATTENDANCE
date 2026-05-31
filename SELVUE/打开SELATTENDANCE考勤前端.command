#!/bin/zsh
set -e

# Finder 双击启动时先补齐常见命令路径，避免 npm / node 因 PATH 不完整而找不到。
export PATH="/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:$PATH"
# 用户若在 zprofile 中声明了 nvm、fnm 或 volta，这里优先补载。
[ -f "$HOME/.zprofile" ] && source "$HOME/.zprofile" >/dev/null 2>&1 || true
# zshrc 中若额外配置了 Node 路径，这里也兼容加载。
[ -f "$HOME/.zshrc" ] && source "$HOME/.zshrc" >/dev/null 2>&1 || true

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

PORT=5180
URL="http://127.0.0.1:${PORT}/"
LOG_FILE="/tmp/selattendance_frontend.log"
SERVER_PID=""

ensure_command() {
  local command_name="$1"
  local command_label="$2"
  if ! command -v "$command_name" >/dev/null 2>&1; then
    echo "${command_label} 未安装或当前 PATH 不可见：${command_name}"
    exit 1
  fi
}

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

if [ ! -d "node_modules" ]; then
  echo "正在安装 SELATTENDANCE 前端依赖..."
  ensure_command "npm" "前端安装依赖"
  npm install
fi

echo ""
echo "SELATTENDANCE 考勤前端正在启动..."
echo "项目目录: ${SCRIPT_DIR}"
echo "打开地址: ${URL}"
echo ""

ensure_command "npm" "前端启动依赖"
ensure_command "curl" "前端探活依赖"

stop_port_processes

# 前端开发服务在 .command 后台运行时要显式断开 stdin，避免 Vite 在非交互壳里出现 ready 后立即退出的假启动。
npm run dev:local < /dev/null >"$LOG_FILE" 2>&1 &
SERVER_PID=$!

for _ in {1..60}; do
  if curl -fsS "$URL" >/dev/null 2>&1; then
    # 浏览器打开属于增强体验，失败时不应让已启动的前端被当成启动失败。
    open "$URL" >/dev/null 2>&1 || true
    echo "前端已启动。关闭这个终端窗口时，前端服务会一并停止。"
    wait "$SERVER_PID"
    exit 0
  fi
  sleep 1
done

echo "启动超时：浏览器未能在 60 秒内连接到 ${URL}"
if [ -f "$LOG_FILE" ]; then
  echo ""
  echo "最近日志："
  tail -n 20 "$LOG_FILE" || true
fi
exit 1
