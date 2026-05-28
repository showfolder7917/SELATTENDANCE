// 引入 Playwright 断言与测试工具，在真实浏览器里验证宿主切换器可以从 attendance 切到 memory。
import { expect, test } from '@playwright/test'

test.describe('host e2e project switcher', () => {
  // 通过真实浏览器点击宿主下拉框，验证多工程宿主的切换主链路可用。
  test('switches from attendance to memory through the host selector', async ({ page }) => {
    // 用 mock bootstrap 响应兜底 attendance 首屏壳，避免默认打开宿主时被后端依赖阻塞。
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

    // 租户独立接口也返回最小 mock，保证宿主默认工程渲染过程完整通过。
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

    // 先打开宿主根入口，再通过宿主下拉框切换到 memory 工程。
    await page.goto('/')
    await page.selectOption('#project-host-select', 'memory')

    await expect(page.getByRole('heading', { name: 'Memory Workspace' })).toBeVisible()
    await expect(page.getByText('工程已被宿主自动发现并可切换')).toBeVisible()
  })
})
