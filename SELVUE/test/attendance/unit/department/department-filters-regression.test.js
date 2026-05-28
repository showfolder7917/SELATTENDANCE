// 引入工作台状态工厂和筛选修复工具，长期盯住部门筛选在主数据刷新后不会继续保留失效场所。
import { createWorkbenchState, syncFilterReferences } from '@tests-attendance'

describe('attendance unit department regression', () => {
  // 校验部门筛选中的场所引用失效后会被清空，避免筛选栏停留在已删除的场所上。
  it('clears the orphaned workplace filter after master-data refresh', () => {
    const state = createWorkbenchState()

    state.workplaces = [{ id: 11 }]
    state.departmentFilters.workplaceId = '99'

    syncFilterReferences(state)

    expect(state.departmentFilters.workplaceId).toBe('')
  })

  // 校验部门表单默认排序和启用态，保证新增部门入口始终以可保存的基础值打开。
  it('keeps the department form on active status with zero sort order by default', () => {
    const state = createWorkbenchState()

    expect(state.departmentForm.status).toBe('ACTIVE')
    expect(state.departmentForm.sortOrder).toBe(0)
  })
})
