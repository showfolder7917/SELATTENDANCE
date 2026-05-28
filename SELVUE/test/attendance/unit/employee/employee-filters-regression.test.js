// 引入工作台状态工厂和筛选修复工具，验证员工区块的部门筛选和基础表单默认值保持稳定。
import { createWorkbenchState, syncFilterReferences } from '@tests-attendance'

describe('attendance unit employee regression', () => {
  // 校验员工筛选引用的部门被删除后会自动清空，避免列表长期卡在无结果状态。
  it('clears the orphaned employee department filter after department refresh', () => {
    const state = createWorkbenchState()

    state.departments = [{ id: 21 }]
    state.employeeFilters.departmentId = '88'

    syncFilterReferences(state)

    expect(state.employeeFilters.departmentId).toBe('')
  })

  // 校验员工表单、映射弹层和导入结果默认值，保证员工模块首次打开时新增、绑定和导入三条链都有稳定初始态。
  it('keeps employee module defaults aligned with the current attendance workflow', () => {
    const state = createWorkbenchState()

    expect(state.employeeForm.employmentType).toBe('FULL_TIME')
    expect(state.employeeForm.status).toBe('ACTIVE')
    expect(state.mappingForm.sourceSystem).toBe('KING_OF_TIME')
    expect(state.importResult).toBeNull()
  })
})
