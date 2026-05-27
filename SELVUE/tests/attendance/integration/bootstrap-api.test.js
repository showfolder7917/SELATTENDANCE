// 模拟共享请求桥，验证考勤工程的首页壳 API 会把路径和方法映射到正确的后端入口。
import { requestJson } from '../../../src/shared/services/request.js'
import { fetchBootstrap, fetchCurrentTenant, saveTenant } from '../../../src/projects/attendance/services/bootstrapApi.js'

// 用模块 mock 接管真实网络请求，保证这层测试只关注 API 封装协议。
vi.mock('../../../src/shared/services/request.js', () => ({
  requestJson: vi.fn()
}))

describe('attendance bootstrap api', () => {
  // 每个用例前清理请求 mock，避免前一个断言残留影响当前接口。
  beforeEach(() => {
    requestJson.mockReset()
  })

  // 测试首页轻量壳读取接口，保证前台仍命中拆分后的 bootstrap 入口。
  it('calls the lightweight bootstrap endpoint', async () => {
    requestJson.mockResolvedValue({ recommendedNextAction: 'wizard.schedule' })

    const result = await fetchBootstrap()

    expect(requestJson).toHaveBeenCalledWith('/api/attendance/bootstrap')
    expect(result.recommendedNextAction).toBe('wizard.schedule')
  })

  // 测试当前租户读取接口，保证租户面板仍然从独立租户入口取数。
  it('calls the current tenant endpoint', async () => {
    requestJson.mockResolvedValue({ tenantCode: 'TENANT_DEMO' })

    const result = await fetchCurrentTenant()

    expect(requestJson).toHaveBeenCalledWith('/api/attendance/tenant/current')
    expect(result.tenantCode).toBe('TENANT_DEMO')
  })

  // 测试租户保存接口，保证前台会以 PUT + JSON body 的形式回写当前租户资料。
  it('sends tenant updates to the current tenant endpoint', async () => {
    const payload = { tenantName: 'Tokyo School' }
    requestJson.mockResolvedValue({ tenantName: 'Tokyo School' })

    await saveTenant(payload)

    expect(requestJson).toHaveBeenCalledWith('/api/attendance/tenant/current', {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
  })
})
