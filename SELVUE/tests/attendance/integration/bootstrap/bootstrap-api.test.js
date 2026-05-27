// 模拟共享请求桥，验证 bootstrap 首页壳接口仍会命中拆分后的独立后端入口。
import { requestJson } from '@shared/services/request.js'
import { fetchBootstrap } from '@tests-attendance'

// 用模块 mock 接管真实网络请求，确保这层测试只关注路径和契约而不依赖后端环境。
vi.mock('@shared/services/request.js', () => ({
  requestJson: vi.fn()
}))

describe('attendance integration bootstrap api', () => {
  // 每个用例前都重置请求 mock，避免前一次断言残留到下一条 bootstrap 验证。
  beforeEach(() => {
    requestJson.mockReset()
  })

  // 校验轻量首页壳接口路径，保证首屏仍然只走 bootstrap 轻量接口而不是整页大接口。
  it('calls the lightweight bootstrap endpoint', async () => {
    requestJson.mockResolvedValue({ recommendedNextAction: 'wizard.schedule' })

    const result = await fetchBootstrap()

    expect(requestJson).toHaveBeenCalledWith('/api/attendance/bootstrap')
    expect(result.recommendedNextAction).toBe('wizard.schedule')
  })
})
