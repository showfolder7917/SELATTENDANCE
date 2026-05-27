// 模拟共享请求桥，长期盯住员工查询构造逻辑，避免空筛选条件再次污染 URL 查询串。
import { requestJson } from '@shared/services/request.js'
import { listEmployees } from '@tests-attendance'

// 用请求 mock 接住 listEmployees，确保这条回归测试只验证查询串生成规则。
vi.mock('@shared/services/request.js', () => ({
  requestJson: vi.fn()
}))

describe('attendance integration employee regression', () => {
  // 每次都清空请求 mock，避免前一次调用结果影响这条专门的回归断言。
  beforeEach(() => {
    requestJson.mockReset()
  })

  // 校验空串、null 和 undefined 不会被带到查询串里，防止后端再次收到噪音过滤参数。
  it('omits empty employee filters from the query string', async () => {
    requestJson.mockResolvedValue([])

    await listEmployees({
      keyword: '',
      departmentId: undefined,
      employmentType: null,
      status: '',
      active: false
    })

    expect(requestJson).toHaveBeenCalledWith('/api/attendance/employees?active=false')
  })
})
