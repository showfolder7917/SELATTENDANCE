// 引入 Playwright 断言与测试工具，在真实浏览器里验证 attendance 首页壳可以在 mock API 下稳定打开。
import { expect, test } from '@playwright/test'

test.describe('attendance e2e workbench shell', () => {
  // 在真实浏览器里接管 bootstrap 与 tenant 接口，保证首屏壳验证不依赖本地后端是否已启动。
  test('renders the attendance workbench shell with mocked bootstrap data', async ({ page }) => {
    // 用 mock bootstrap 响应给首页壳补足推荐动作和租户摘要，让真实浏览器能完整渲染首屏。
    await page.route('**/api/attendance/bootstrap', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 0,
          data: {
            steps: [],
            recommendedNextAction: 'wizard.schedule',
            tenant: {
              tenantCode: 'TENANT_DEMO',
              tenantName: 'Tokyo School'
            }
          }
        })
      })
    })

    // 用 mock tenant 响应兜底独立租户读取路径，避免壳层补租户时因缺后端而失败。
    await page.route('**/api/attendance/tenant/current', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 0,
          data: {
            tenantCode: 'TENANT_DEMO',
            tenantName: 'Tokyo School',
            timezone: 'Asia/Tokyo'
          }
        })
      })
    })

    // 直接打开 attendance 工程入口，验证真实浏览器里首页壳、工作台标题和侧边区块可以稳定出现。
    await page.goto('/?project=attendance')

    await expect(page.getByRole('heading', { name: /考勤系统第二阶段工作台|勤怠システム Phase 2/ })).toBeVisible()
    await expect(page.getByText(/功能导航|機能ナビ/)).toBeVisible()
    await expect(page.getByRole('heading', { name: /推荐下一步|次の推奨アクション/ })).toBeVisible()
  })
})
