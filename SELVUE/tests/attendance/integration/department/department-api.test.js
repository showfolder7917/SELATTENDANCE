// 模拟共享请求桥，验证 department 模块的增删改查请求不会因为测试目录重排而接错后端入口。
import { requestJson } from '@shared/services/request.js'
import {
  createDepartment,
  deleteDepartment,
  listDepartments,
  updateDepartment
} from '@tests-attendance'

// 用请求 mock 接管真实网络，把断言聚焦在部门模块的路径、方法和 JSON 回写格式。
vi.mock('@shared/services/request.js', () => ({
  requestJson: vi.fn()
}))

describe('attendance integration department api', () => {
  // 每次都重置请求 mock，保证部门读写动作之间的计数与参数断言独立。
  beforeEach(() => {
    requestJson.mockReset()
  })

  // 校验部门列表读取入口，保证部门主数据仍然从独立部门接口加载。
  it('loads departments from the department endpoint', async () => {
    requestJson.mockResolvedValue([{ id: 1 }])

    await listDepartments()

    expect(requestJson).toHaveBeenCalledWith('/api/attendance/departments')
  })

  // 校验部门新增、更新和删除动作的路径与方法，确保部门写回继续遵守 REST 契约。
  it('writes create, update and delete department requests to the expected endpoints', async () => {
    const payload = { departmentName: 'General Affairs' }

    requestJson.mockResolvedValue({})

    await createDepartment(payload)
    await updateDepartment(21, payload)
    await deleteDepartment(21)

    expect(requestJson).toHaveBeenNthCalledWith(1, '/api/attendance/departments', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
    expect(requestJson).toHaveBeenNthCalledWith(2, '/api/attendance/departments/21', {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
    expect(requestJson).toHaveBeenNthCalledWith(3, '/api/attendance/departments/21', {
      method: 'DELETE'
    })
  })
})
