// 引入子进程能力，供项目自己托管 Vite 服务和 Playwright 测试进程，而不是继续依赖 webServer 托管。
import { spawn } from 'node:child_process'
// 引入事件工具，便于等待服务子进程或测试子进程的退出结果。
import { once } from 'node:events'
// 引入路径工具，保证在 Windows 下也能稳定拼出项目根目录与 CLI 入口路径。
import path from 'node:path'
// 引入 URL 转路径工具，把当前脚本的 import.meta.url 还原成本机绝对路径。
import { fileURLToPath } from 'node:url'

// 固定 e2e 服务监听主机，保证探活地址和 Playwright baseURL 永远指向同一个入口。
const E2E_HOST = '127.0.0.1'
// 固定 e2e 服务监听端口，保证测试链路不会因为随机端口而找不到页面。
const E2E_PORT = 5180
// 为本地服务探活设置统一超时时间，避免服务起不来时无休止卡住。
const SERVER_READY_TIMEOUT_MS = 120000
// 为服务关闭预留单独等待时间，超过后再执行更强制的清理动作。
const SERVER_SHUTDOWN_TIMEOUT_MS = 10000

// 根据当前脚本物理位置反推出项目根目录，避免命令从别的工作目录触发时找不到相对路径。
const scriptFilePath = fileURLToPath(import.meta.url)
// scripts 目录的上一级就是 SELVUE 项目根目录，后续所有子进程都统一以它为 cwd。
const projectRoot = path.resolve(path.dirname(scriptFilePath), '..')
// 显式定位 Playwright CLI 入口，绕过 npm/npx 包装层，减少 Windows 下命令包装造成的退出噪音。
const playwrightCliPath = path.join(projectRoot, 'node_modules', '@playwright', 'test', 'cli.js')
// 复用现有受控 Vite 启动脚本，把服务生命周期继续收口到项目代码内部。
const viteServerScriptPath = path.join(projectRoot, 'scripts', 'playwright-vite-server.mjs')

// 保留当前托管的 Vite 服务子进程句柄，便于测试完成、异常或中断时都能显式回收。
let serverProcess = null
// 用单独标记避免同一次执行里重复触发服务清理，防止二次 kill 反而卡住当前收尾链路。
let cleaningUp = false

// 用 Promise 包一层 setTimeout，便于在探活轮询和关停等待里复用统一的异步睡眠动作。
const delay = (milliseconds) => new Promise((resolve) => {
  // 通过短暂停顿把轮询节奏控制在稳定区间，避免疯狂请求本地服务或占满事件循环。
  setTimeout(resolve, milliseconds)
})

// 把服务探活逻辑独立成函数，确保只有真正拿到 200 页面后才进入浏览器测试阶段。
const waitForServerReady = async () => {
  // 记录探活开始时间，后续用来统一判断是否已经超过允许的冷启动窗口。
  const startedAt = Date.now()

  while (true) {
    try {
      // 直接请求 Vite 首页根地址，只要返回成功状态就说明本地服务和 HTML 入口都已经可用。
      const response = await fetch(`http://${E2E_HOST}:${E2E_PORT}`, {
        // 明确要求不跟随重定向或缓存旧结果，让探活始终针对当前新拉起的服务实例。
        redirect: 'manual',
        cache: 'no-store'
      })

      // 只要已经拿到成功状态，就立即结束探活，进入真实浏览器执行阶段。
      if (response.ok) {
        return
      }
    } catch {
      // 服务冷启动期间请求失败属于预期现象，这里吞掉异常并继续下一轮探活。
    }

    // 如果已经超过允许的冷启动窗口，就直接抛错并阻止后续 Playwright 在不可用服务上继续等待。
    if (Date.now() - startedAt > SERVER_READY_TIMEOUT_MS) {
      throw new Error(`Timed out waiting for Vite e2e server on http://${E2E_HOST}:${E2E_PORT}`)
    }

    // 每轮探活之间短暂停顿，给 Vite 预留实际启动时间，也避免本地请求过于密集。
    await delay(500)
  }
}

// 把强制清理动作独立出来，便于普通关停失败时在 Windows 上兜底结束整棵服务进程树。
const forceKillServerProcess = async (processId) => {
  // 在 Windows 上使用 taskkill 递归结束服务子进程树，避免残留的 Vite/FileWatcher 继续挂住。
  const killer = spawn('taskkill', ['/PID', String(processId), '/T', '/F'], {
    // 继续在项目根目录执行，保证系统命令输出与当前任务日志保持一致。
    cwd: projectRoot,
    // 标准输出继承给当前终端，便于必要时看到系统回收结果。
    stdio: 'inherit',
    // 隐藏额外命令窗口，避免每次清理都弹出单独的 cmd 窗口。
    windowsHide: true
  })

  // 等待 taskkill 自己退出，确保强制清理动作已经真正完成。
  await once(killer, 'exit')
}

// 把服务关闭逻辑收口到一处，保证正常完成、异常失败和 Ctrl+C 都走同一套回收路径。
const cleanupServer = async () => {
  // 如果当前没有服务子进程，或者已经进入清理阶段，就直接返回，避免重复收尾。
  if (!serverProcess || cleaningUp) {
    return
  }

  // 第一时间翻转清理标记，阻止多个退出路径并发操作同一条服务进程。
  cleaningUp = true

  // 先取出当前服务 pid，后续即使 serverProcess 被置空也能继续执行兜底清理。
  const processId = serverProcess.pid
  // 先保留当前子进程句柄供等待退出使用，再把全局引用清空，防止后续逻辑误判为仍可复用。
  const currentServerProcess = serverProcess
  // 清空全局句柄，表示新的业务逻辑不应再把这条服务当作可用实例。
  serverProcess = null

  try {
    // 先尝试用温和的终止信号通知服务正常收尾，优先走项目脚本里的 close 逻辑。
    currentServerProcess.kill('SIGTERM')
  } catch {
    // 如果温和关停本身就失败，说明进程可能已经退出，这里直接进入后续等待和兜底流程即可。
  }

  // 用竞争等待的方式给服务一个正常退出窗口，超过后再升级为强制清理。
  const exitResult = await Promise.race([
    // 如果服务进程自己退出，这条 Promise 会先返回，表示不需要进入 taskkill 兜底。
    once(currentServerProcess, 'exit').then(() => 'exited'),
    // 到达设定窗口仍未退出，就返回 timeout 标记，进入更强制的回收步骤。
    delay(SERVER_SHUTDOWN_TIMEOUT_MS).then(() => 'timeout')
  ])

  // 若服务在温和关停窗口内没有退出，就用系统级递归清理结束整棵子进程树。
  if (exitResult === 'timeout') {
    await forceKillServerProcess(processId)
  }
}

// 把整个 e2e 链路的中间子进程启动抽成统一方法，保证日志继承和 cwd 设置完全一致。
const spawnManagedProcess = (command, args) => spawn(command, args, {
  // 所有子进程都固定在项目根目录执行，避免相对路径在不同调用入口下失效。
  cwd: projectRoot,
  // 让服务日志和 Playwright 日志直接进入当前终端，方便诊断时看到真实顺序。
  stdio: 'inherit',
  // 隐藏额外命令窗口，避免 Windows 每次起子进程都弹出新的控制台。
  windowsHide: true
})

// 主执行流程由脚本自己接管，保证服务启动、探活、测试执行和回收都在同一条可控链路里完成。
const run = async () => {
  // 先拉起本地 Vite 服务，为后续 Playwright 浏览器访问提供真实页面入口。
  serverProcess = spawnManagedProcess(process.execPath, [viteServerScriptPath])

  // 如果服务脚本在启动早期就异常退出，需要立刻报错，而不是继续等待一个永远起不来的地址。
  serverProcess.once('exit', (code) => {
    // 只有在还没进入清理阶段时，服务提前退出才代表异常；正常清理阶段的退出不需要再次抛错。
    if (!cleaningUp && code !== 0) {
      // 直接把异常打印到标准错误，便于本地排查服务启动失败的原因。
      console.error(`E2E Vite server exited early with code ${code}`)
    }
  })

  try {
    // 先等待本地服务真正可访问，再进入浏览器测试，避免 Playwright 自己在空地址上长时间重试。
    await waitForServerReady()

    // 服务就绪后直接调用本地 Playwright CLI 入口，绕过 npm 包装层，减少 Windows 额外命令壳层。
    const playwrightProcess = spawnManagedProcess(process.execPath, [
      playwrightCliPath,
      'test',
      '--config',
      'playwright.config.js',
      '--reporter=line'
    ])

    // 等待 Playwright 测试进程自然退出，并把退出码原样作为本轮 e2e 执行结果带回。
    const [exitCode] = await once(playwrightProcess, 'exit')

    // 无论测试成功还是失败，都先显式回收服务，再把退出码返回给 npm。
    await cleanupServer()

    // 用 Playwright 的真实退出码作为整个脚本的退出码，保证 CI 和本地命令都能准确判断成败。
    process.exit(typeof exitCode === 'number' ? exitCode : 1)
  } catch (error) {
    // 任何服务启动、探活或测试阶段的异常都打印出来，避免 npm 只看到一个无上下文的失败退出码。
    console.error(error)

    // 发生异常时同样要先回收服务，防止当前项目目录残留 Vite 监听进程。
    await cleanupServer()

    // 异常链路统一以非零退出码返回，明确告诉调用方这轮 e2e 并未完整成功。
    process.exit(1)
  }
}

// 处理 Ctrl+C 中断，确保本地手工停止 e2e 时也能把服务一起收干净。
process.on('SIGINT', async () => {
  // 用户中断时优先回收服务，避免留下 5180 端口占用或孤儿子进程。
  await cleanupServer()
  // 再用标准中断退出码结束脚本，保持命令行语义一致。
  process.exit(130)
})

// 处理终止信号，保证外层工具或 CI 主动停止任务时也能走同一套服务清理逻辑。
process.on('SIGTERM', async () => {
  // 收到终止信号时同样先回收本地服务，避免下一次执行探活命中旧进程。
  await cleanupServer()
  // 统一返回常见的终止退出码，便于外层工具识别这是受控终止而不是业务失败。
  process.exit(143)
})

// 启动主流程，把 e2e 执行链路真正切换到项目自管模式。
void run()
