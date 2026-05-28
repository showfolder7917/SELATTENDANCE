// 引入 Playwright 断言与测试工具，专门验证托管本地 Vite 服务后 runner 是否仍会卡在退出阶段。
import { expect, test } from '@playwright/test'

// 把 webServer 托管退出诊断聚合到单独的 describe 块里，便于和纯浏览器链路的表现逐项对比。
test.describe('playwright diagnostic webserver exit', () => {
  // 这条最小用例只依赖宿主首页的静态切换器，不接复杂业务接口，用来聚焦服务托管和退出回收链路。
  test('exits cleanly after visiting the host shell through the managed web server', async ({ page }) => {
    // 用最小 bootstrap mock 兜底 attendance 默认首屏，避免宿主默认工程在初始化时请求真实后端造成诊断噪音。
    await page.route('**/api/attendance/bootstrap', async (route) => {
      // 返回最小成功壳数据，让宿主首页只走最短渲染路径。
      await route.fulfill({
        // HTTP 状态保持 200，模拟后端成功响应场景。
        status: 200,
        // 内容类型声明为 JSON，保持前端请求封装的正常解析分支。
        contentType: 'application/json',
        // 响应体只保留宿主首页渲染所需的最小字段，避免诊断被其他业务数据拖复杂。
        body: JSON.stringify({
          code: 0,
          data: {
            steps: [],
            recommendedNextAction: 'wizard.schedule',
            tenant: {
              tenantCode: 'TENANT_DIAGNOSTIC',
              tenantName: 'Diagnostic Tenant'
            }
          }
        })
      })
    })

    // 当前宿主还会独立读取租户摘要，这里同步用 mock 填平，确保首页渲染链路完整闭合。
    await page.route('**/api/attendance/tenant/current', async (route) => {
      // 返回宿主切换器和 attendance 默认工程都能接受的最小租户对象。
      await route.fulfill({
        // 保持 HTTP 成功状态，避免额外进入错误态渲染分支。
        status: 200,
        // 明确返回 JSON，让前端请求封装继续走既有解包逻辑。
        contentType: 'application/json',
        // 仅返回诊断所需字段，聚焦“起服务后 runner 能否退出”这一件事。
        body: JSON.stringify({
          code: 0,
          data: {
            tenantCode: 'TENANT_DIAGNOSTIC',
            tenantName: 'Diagnostic Tenant',
            timezone: 'Asia/Tokyo'
          }
        })
      })
    })

    // 访问宿主根入口，触发 Playwright webServer 托管的最小前端页面启动流程。
    await page.goto('/')
    // 用宿主切换器作为稳定选择器，确认 Vite 服务、页面脚本和浏览器交互都已经正常完成。
    await expect(page.locator('#project-host-select')).toBeVisible()
  })
})
