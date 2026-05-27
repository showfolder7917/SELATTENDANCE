// 模拟共享请求桥，验证 workplace 模块的增删改查都会落到正确的场所后端入口。
import { requestJson } from '@shared/services/request.js'
import {
  createWorkplace,
  deleteWorkplace,
  listWorkplaces,
  updateWorkplace
} from '@tests-attendance'

// 用模块 mock 隔离网络，把验证重点收口到场所模块的请求路径和方法。
vi.mock('@shared/services/request.js', () => ({
  requestJson: vi.fn()
}))

describe('attendance integration workplace api', () => {
  // 每个用例前重置请求 mock，保证不同场所动作之间的断言顺序稳定。
  beforeEach(() => {
    requestJson.mockReset()
  })

  // 校验场所列表读取接口，保证场所模块仍从统一场所入口初始化主数据。
  it('loads the workplace list from the workplace endpoint', async () => {
    requestJson.mockResolvedValue([{ id: 1 }])

    await listWorkplaces()

    expect(requestJson).toHaveBeenCalledWith('/api/attendance/workplaces')
  })

  // 校验场所新增、更新和删除动作的路径与方法，保证场所区块写回仍遵守 REST 契约。
  it('writes workplace create, update and delete requests to the expected endpoints', async () => {
    const payload = { workplaceName: 'Tokyo HQ' }

    requestJson.mockResolvedValue({})

    await createWorkplace(payload)
    await updateWorkplace(11, payload)
    await deleteWorkplace(11)

    expect(requestJson).toHaveBeenNthCalledWith(1, '/api/attendance/workplaces', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
    expect(requestJson).toHaveBeenNthCalledWith(2, '/api/attendance/workplaces/11', {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
    expect(requestJson).toHaveBeenNthCalledWith(3, '/api/attendance/workplaces/11', {
      method: 'DELETE'
    })
  })
})
