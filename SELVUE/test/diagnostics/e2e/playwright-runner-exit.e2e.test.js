// 引入 Playwright 断言与测试工具，专门验证最小浏览器用例在不依赖项目页面时能否正常结束并退出 runner。
import { expect, test } from '@playwright/test'

// 把最小浏览器退出诊断聚合到单独的 describe 块里，方便和 webServer 诊断结果分开阅读。
test.describe('playwright diagnostic runner exit', () => {
  // 这条最小用例只验证浏览器打开与关闭链路本身，不接触 Vite、路由或业务接口。
  test('exits cleanly after a bare browser-only assertion', async ({ page }) => {
    // 直接在浏览器里写入静态内容，确保当前诊断只覆盖 Playwright 浏览器生命周期而不引入任何项目脚本。
    await page.setContent('<main data-testid=\"runner-exit\">runner exit ok</main>')
    // 通过最小可见断言确认页面上下文真的被创建并完成了一次基础交互。
    await expect(page.getByTestId('runner-exit')).toHaveText('runner exit ok')
  })
})
