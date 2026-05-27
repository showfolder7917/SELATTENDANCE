// 引入 Playwright 配置工厂，统一声明本地真实浏览器 e2e 验证的启动与断言边界。
import { defineConfig, devices } from '@playwright/test'

// e2e 统一在本地 5180 端口拉起真实 Vite 页面，验证 host 与 attendance 的浏览器级链路。
export default defineConfig({
  // 测试文件统一收敛到 tests 下的 e2e 入口，避免把 unit/integration 误当浏览器测试执行。
  testDir: './tests',
  // 只执行以 .e2e.test.js 结尾的真实浏览器用例，保持目录分层和命名语义一致。
  testMatch: '**/*.e2e.test.js',
  // 浏览器测试本身较少，单进程串行更利于定位真实页面失败原因和保留稳定日志顺序。
  workers: 1,
  // CI 与本地都统一禁止 accidentally committed only()，避免漏跑关键回归链路。
  forbidOnly: true,
  // 失败时保留截图，收口时可以直接引用 Playwright 产物定位真实页面问题。
  use: {
    // 当前 e2e 统一验证本地启动的 Vite 页面，所有相对路径都会挂到这个根地址下。
    baseURL: 'http://127.0.0.1:5180',
    // 出错时自动截屏，补足真实页面验证证据。
    screenshot: 'only-on-failure',
    // 保留 trace，便于回放交互过程而不是只看最终报错。
    trace: 'retain-on-failure'
  },
  // 进入 e2e 前由 Playwright 自己托管本地前端服务生命周期，避免遗留旧进程。
  webServer: {
    // 直接复用项目的一键本地开发脚本，让真实浏览器验证和日常开发入口保持一致。
    command: 'npm run dev:local',
    // 当前 e2e 只依赖前端 5180 端口存活，不要求预先手工起服务。
    url: 'http://127.0.0.1:5180',
    // 开发机重复执行时允许复用已拉起的同端口服务，减少等待。
    reuseExistingServer: true,
    // 首次拉起 Vite 需要一点时间，这里显式给足等待窗口。
    timeout: 120000
  },
  // 先用 Chromium 完成真实浏览器闭环，后续如有需要再平移到更多引擎。
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    }
  ]
})
