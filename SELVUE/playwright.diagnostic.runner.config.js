// 引入 Playwright 配置工厂和 Chromium 桌面设备模板，供最小 runner 退出诊断复用统一浏览器配置。
import { defineConfig, devices } from '@playwright/test'

// 这份配置只验证 Playwright 浏览器自身的启动和退出，不托管任何本地 Vite 服务。
export default defineConfig({
  // 诊断文件统一放到 test/diagnostics/e2e，下游命令只会扫描这一小块而不会触发业务 e2e。
  testDir: './test/diagnostics/e2e',
  // 这一份配置只跑纯浏览器退出诊断用例，用最小输入验证 runner 是否也会卡住。
  testMatch: 'playwright-runner-exit.e2e.test.js',
  // 保持单 worker 串行，避免并发测试把退出诊断现象掩盖掉。
  workers: 1,
  // 禁止 only，防止临时诊断文件误留下局部执行状态。
  forbidOnly: true,
  use: {
    // 仍然沿用当前项目统一的 Chromium 设备配置，保证诊断结论和正式 e2e 环境一致。
    ...devices['Desktop Chrome'],
    // 失败时保留 trace，便于回看浏览器本身的收尾链路。
    trace: 'retain-on-failure'
  }
})
