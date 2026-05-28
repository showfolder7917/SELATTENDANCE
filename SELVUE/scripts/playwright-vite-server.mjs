// 引入 Vite 的 createServer，供 Playwright e2e 在真实浏览器启动前拉起同一套前端开发服务。
import { createServer } from 'vite'

// 固定 e2e 使用的本地端口，保证 Playwright 配置里的 baseURL 和 webServer 探活地址始终一致。
const E2E_PORT = 5180
// 固定监听本机回环地址，避免真实浏览器测试意外暴露到局域网或占用不必要的外部网卡。
const E2E_HOST = '127.0.0.1'

// 记录当前已经拉起的 Vite 服务实例，便于在 Windows 信号收尾时显式关闭而不是依赖 CLI 默认行为。
let viteServer = null
// 用单独标记防止多个退出信号重复触发 close 和 process.exit，避免 Playwright 收尾阶段再次挂住。
let shuttingDown = false

// 单独封装关闭动作，让 SIGINT、SIGTERM 和异常退出都走同一条服务回收链路。
const shutdown = async (exitCode = 0) => {
  // 如果已经进入收尾，就直接返回，避免重复关闭同一条 HTTP 服务。
  if (shuttingDown) {
    return
  }
  // 第一时间翻转收尾标记，保证后续任何信号都不会再次进入真正关闭逻辑。
  shuttingDown = true

  try {
    // 只有在 Vite 服务已经成功创建后才执行关闭，避免启动失败时又访问空实例。
    if (viteServer) {
      // 显式关闭 Vite HTTP 服务和文件监听器，让 Playwright 能观察到被托管进程已经正常退出。
      await viteServer.close()
    }
  } finally {
    // 无论关闭过程是否出现异常，都主动返回进程退出码，避免 Node 在 Windows 下留着空转。
    process.exit(exitCode)
  }
}

// 先异步创建一份和日常开发一致的 Vite 服务实例，保证 e2e 测到的仍是同一套前端入口。
viteServer = await createServer({
  // 使用当前项目根目录的标准配置文件，让真实浏览器测试继续复用现有 alias、插件和代理规则。
  configFile: './vite.config.js',
  server: {
    // 对 e2e 固定回环地址，确保 Playwright 按 baseURL 命中的就是这一条本地服务。
    host: E2E_HOST,
    // 对 e2e 固定端口，避免随机端口导致 Playwright 无法探活或访问错误地址。
    port: E2E_PORT,
    // 强制占用固定端口，若端口冲突就直接失败，避免静默切到别的端口后造成假通过或长时间等待。
    strictPort: true
  }
})

// 真正拉起 Vite 监听，让 Playwright 在 webServer.url 探活时能拿到可用页面。
await viteServer.listen()
// 把 Vite 自己的启动地址打印出来，便于本地排查 e2e 环境是否确实拉起到了预期端口。
viteServer.printUrls()

// 监听 Playwright 常用的中断信号，确保测试完成后能显式关闭服务并让父进程正常收尾。
process.on('SIGINT', () => {
  // 收到 Ctrl+C 或 Playwright 中断时，把服务和 Node 进程按成功收尾方式一起关闭。
  void shutdown(0)
})

// 监听终止信号，处理 Playwright 在回收 webServer 时发出的标准终止动作。
process.on('SIGTERM', () => {
  // 收到终止信号时同样走统一关闭逻辑，避免 Vite 服务器残留成孤儿进程。
  void shutdown(0)
})

// 监听进程自身异常，确保脚本启动或运行失败时能给 Playwright 一个明确的非零退出结果。
process.on('uncaughtException', (error) => {
  // 把未捕获异常打印到标准错误，便于直接在 e2e 输出里定位服务托管失败原因。
  console.error(error)
  // 发生未捕获异常时用失败退出码收尾，阻止 Playwright 在错误服务上继续等待。
  void shutdown(1)
})
