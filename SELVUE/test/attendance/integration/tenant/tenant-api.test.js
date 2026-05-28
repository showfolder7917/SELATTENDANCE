// 模拟共享请求桥，验证 tenant 面板相关接口在测试分层重排后仍保持原有读写契约。
import { requestJson } from '@shared/services/request.js'
import { fetchCurrentTenant, saveTenant } from '@tests-attendance'

// 统一用请求 mock 隔离真实网络，确保这里只验证 tenant 端点的前端接线。
vi.mock('@shared/services/request.js', () => ({
  requestJson: vi.fn()
}))

describe('attendance integration tenant api', () => {
  // 每个用例前清空请求 mock，保证租户读取和租户保存断言互不污染。
  beforeEach(() => {
    requestJson.mockReset()
  })

  // 校验当前租户读取接口路径，保证租户面板仍从独立租户入口补齐基础资料。
  it('calls the current tenant endpoint', async () => {
    requestJson.mockResolvedValue({ tenantCode: 'TENANT_DEMO' })

    const result = await fetchCurrentTenant()

    expect(requestJson).toHaveBeenCalledWith('/api/attendance/tenant/current')
    expect(result.tenantCode).toBe('TENANT_DEMO')
  })

  // 校验租户保存使用 PUT + JSON body 回写，保证面板保存动作不会退化成错误方法或空请求体。
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
