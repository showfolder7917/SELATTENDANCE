// 模拟共享请求桥，验证 employee 模块的查询、导入导出和外部映射都接到正确后端契约。
import { requestJson } from '@shared/services/request.js'
import {
  bindExternalMapping,
  createEmployee,
  deleteEmployee,
  exportEmployees,
  importEmployees,
  listEmployees,
  updateEmployee
} from '@tests-attendance'

// 用请求 mock 隔离网络，把测试重点放在员工服务如何拼路径、方法和查询串。
vi.mock('@shared/services/request.js', () => ({
  requestJson: vi.fn()
}))

describe('attendance integration employee api', () => {
  // 每个用例前清空请求 mock，保持员工模块各动作的断言顺序清晰稳定。
  beforeEach(() => {
    requestJson.mockReset()
  })

  // 校验员工列表在有筛选条件时会只保留有效条件并拼接到查询串里。
  it('serializes employee filters into the employee list query string', async () => {
    requestJson.mockResolvedValue([])

    await listEmployees({
      keyword: 'A1001',
      departmentId: 21,
      status: 'ACTIVE',
      workplaceId: '',
      employmentType: null
    })

    expect(requestJson).toHaveBeenCalledWith(
      '/api/attendance/employees?keyword=A1001&departmentId=21&status=ACTIVE'
    )
  })

  // 校验员工新增、更新、删除、映射绑定、导入和导出动作都落到预期 endpoint。
  it('writes employee mutations, mapping, import and export requests to the expected endpoints', async () => {
    const employeePayload = { employeeName: 'Sato' }
    const mappingPayload = { sourceSystem: 'KING_OF_TIME' }
    const importPayload = { csvText: 'employeeNo,employeeName' }

    requestJson.mockResolvedValue({})

    await createEmployee(employeePayload)
    await updateEmployee(31, employeePayload)
    await deleteEmployee(31)
    await bindExternalMapping(31, mappingPayload)
    await importEmployees(importPayload)
    await exportEmployees()

    expect(requestJson).toHaveBeenNthCalledWith(1, '/api/attendance/employees', {
      method: 'POST',
      body: JSON.stringify(employeePayload)
    })
    expect(requestJson).toHaveBeenNthCalledWith(2, '/api/attendance/employees/31', {
      method: 'PUT',
      body: JSON.stringify(employeePayload)
    })
    expect(requestJson).toHaveBeenNthCalledWith(3, '/api/attendance/employees/31', {
      method: 'DELETE'
    })
    expect(requestJson).toHaveBeenNthCalledWith(4, '/api/attendance/employees/31/external-mapping', {
      method: 'PUT',
      body: JSON.stringify(mappingPayload)
    })
    expect(requestJson).toHaveBeenNthCalledWith(5, '/api/attendance/employees/import', {
      method: 'POST',
      body: JSON.stringify(importPayload)
    })
    expect(requestJson).toHaveBeenNthCalledWith(6, '/api/attendance/employees/export')
  })
})
