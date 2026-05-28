// 模拟共享请求桥，长期盯住排班查询串必须保留 month 且忽略其余空筛选，避免历史查询 bug 回流。
import { requestJson } from '@shared/services/request.js'
import { exportSchedules } from '@tests-attendance'

// 用请求 mock 接住导出动作，确保这条回归测试只验证查询串拼接规则而不依赖真实接口。
vi.mock('@shared/services/request.js', () => ({
  requestJson: vi.fn()
}))

describe('attendance integration schedule regression', () => {
  // 每个用例前都重置请求 mock，避免其他排班测试的调用参数影响这条回归断言。
  beforeEach(() => {
    requestJson.mockReset()
  })

  // 校验 month 始终会保留在查询串里，而空筛选不会被错误带出到导出接口。
  it('keeps month and omits empty optional schedule filters', async () => {
    requestJson.mockResolvedValue({})

    await exportSchedules({
      month: '2026-05',
      workplaceId: '',
      departmentId: '',
      employeeKeyword: '',
      onlyUnassigned: false
    })

    expect(requestJson).toHaveBeenCalledWith('/api/attendance/schedules/export?month=2026-05&onlyUnassigned=false')
  })
})
