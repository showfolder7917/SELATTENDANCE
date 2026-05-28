// 模拟共享请求桥，验证 shift 模块的模板读写和推荐生成动作仍连接到正确的后端边界。
import { requestJson } from '@shared/services/request.js'
import {
  createShiftTemplate,
  deleteShiftTemplate,
  generateRecommendedShiftTemplates,
  listShiftTemplates,
  updateShiftTemplate
} from '@tests-attendance'

// 用请求 mock 接住真实网络，确保这里只验证模板模块的路径和方法契约。
vi.mock('@shared/services/request.js', () => ({
  requestJson: vi.fn()
}))

describe('attendance integration shift template api', () => {
  // 每个用例前都重置请求 mock，保持模板各动作的断言不会互相污染。
  beforeEach(() => {
    requestJson.mockReset()
  })

  // 校验模板列表读取和推荐模板生成入口，保证班次模块初始数据和快速初始化动作都能命中正确路径。
  it('loads shift templates and generates recommended templates through the expected endpoints', async () => {
    requestJson.mockResolvedValue([])

    await listShiftTemplates()
    await generateRecommendedShiftTemplates()

    expect(requestJson).toHaveBeenNthCalledWith(1, '/api/attendance/shift-templates')
    expect(requestJson).toHaveBeenNthCalledWith(2, '/api/attendance/shift-templates/recommended', {
      method: 'POST'
    })
  })

  // 校验模板新增、更新和删除接口的路径与方法，保证班次模板维护继续遵守 REST 契约。
  it('writes shift-template create, update and delete requests to the expected endpoints', async () => {
    const payload = { templateName: 'Day Shift' }

    requestJson.mockResolvedValue({})

    await createShiftTemplate(payload)
    await updateShiftTemplate(41, payload)
    await deleteShiftTemplate(41)

    expect(requestJson).toHaveBeenNthCalledWith(1, '/api/attendance/shift-templates', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
    expect(requestJson).toHaveBeenNthCalledWith(2, '/api/attendance/shift-templates/41', {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
    expect(requestJson).toHaveBeenNthCalledWith(3, '/api/attendance/shift-templates/41', {
      method: 'DELETE'
    })
  })
})
