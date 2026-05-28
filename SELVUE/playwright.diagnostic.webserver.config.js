// 引入 Playwright 配置工厂和 Chromium 桌面设备模板，供 webServer 退出诊断复用统一浏览器配置。
import { defineConfig, devices } from '@playwright/test'

// 这份配置专门验证启用托管 Vite 服务后，Playwright 在所有断言通过后是否还能正常退出。
export default defineConfig({
  // 诊断文件统一收敛到 test/diagnostics/e2e，避免把正式业务 e2e 和最小诊断混在一起执行。
  testDir: './test/diagnostics/e2e',
  // 当前配置只跑 webServer 退出诊断用例，聚焦服务托管这一个变量。
  testMatch: 'playwright-webserver-exit.e2e.test.js',
  // 保持单 worker 串行，确保退出阶段只存在一条受控浏览器链路。
  workers: 1,
  // 禁止 only，避免诊断配置被误提交成局部执行状态。
  forbidOnly: true,
  use: {
    // 保持和正式 e2e 同一份本地宿主地址，确保诊断覆盖到真实的服务托管场景。
    baseURL: 'http://127.0.0.1:5180',
    // 仍然沿用 Chromium 桌面设备配置，让诊断环境与正式浏览器链路一致。
    ...devices['Desktop Chrome'],
    // 失败时保留 trace，便于定位究竟是页面访问还是退出回收阶段出问题。
    trace: 'retain-on-failure'
  },
  webServer: {
    // 直接复用项目内的受控 Vite 启动脚本，让诊断覆盖当前正式 e2e 的真实服务托管实现。
    command: 'node ./scripts/playwright-vite-server.mjs',
    // 仍然以 5180 端口为探活目标，保证 webServer 成功拉起后浏览器访问的是同一条本地服务。
    url: 'http://127.0.0.1:5180',
    // 允许复用本地已拉起的服务，避免重复诊断时因为残留服务造成额外等待。
    reuseExistingServer: true,
    // 给 Vite 启动预留足够时间，避免把冷启动误判成退出问题。
    timeout: 120000
  }
})
