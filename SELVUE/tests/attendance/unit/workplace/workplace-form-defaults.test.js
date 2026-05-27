// 引入工作台状态工厂和默认值同步工具，验证场所数据首次加载后相关表单会获得稳定的默认引用。
import { createWorkbenchState, syncFormDefaults } from '@tests-attendance'

describe('attendance unit workplace defaults', () => {
  // 校验部门与员工表单都会优先拿第一个场所做默认值，避免首次打开时下拉框为空。
  it('hydrates dependent forms from the first available workplace', () => {
    const state = createWorkbenchState()

    state.workplaces = [{ id: 11 }, { id: 12 }]
    state.departments = [{ id: 21 }]

    syncFormDefaults(state)

    expect(state.departmentForm.workplaceId).toBe(11)
    expect(state.employeeForm.workplaceId).toBe(11)
    expect(state.employeeForm.departmentId).toBe(21)
  })

  // 校验场所表单自身默认状态，保证首次登记场所时始终以启用态打开表单。
  it('keeps the workplace form in active status by default', () => {
    expect(createWorkbenchState().workplaceForm).toEqual({
      id: null,
      workplaceCode: '',
      workplaceName: '',
      address: '',
      phone: '',
      status: 'ACTIVE'
    })
  })
})
