// 这里集中放 workbench 共享辅助函数，避免各 section 模块重复处理相同状态修复逻辑。

// 把提示消息写入 toast，并在短暂展示后自动清空，保持最初版本的轻量提示节奏。
export const pushToast = (toast, message) => {
  // 立即写入最新提示语，供页面右下角提示层渲染。
  toast.value = message
  // 延时清空提示语，避免旧消息长期停留干扰下一步操作。
  window.setTimeout(() => {
    // 到期后清空 toast 内容，恢复页面安静状态。
    toast.value = ''
  }, 2200)
}

// 把 CSV 导出结果转成浏览器下载动作，供员工和排班导出共用。
export const downloadCsv = (payload) => {
  // 用服务端返回的文本内容构造 CSV Blob，保持 UTF-8 下载编码。
  const blob = new Blob([payload.content], { type: 'text/csv;charset=utf-8;' })
  // 动态创建下载链接，避免引入额外的下载库。
  const link = document.createElement('a')
  // 为下载链接绑定临时对象 URL，供浏览器直接拉起下载。
  link.href = URL.createObjectURL(blob)
  // 使用服务端提供的文件名，保证业务导出命名稳定。
  link.download = payload.fileName
  // 主动触发点击事件，执行浏览器下载动作。
  link.click()
  // 下载触发后释放临时 URL，避免浏览器长期持有内存对象。
  URL.revokeObjectURL(link.href)
}

// 用当前已经加载的数据回填默认表单选择，避免各 section 首次打开时出现空下拉。
export const syncFormDefaults = (state) => {
  // 部门表单没有场所默认值时，优先回填第一个场所。
  if (!state.departmentForm.workplaceId && state.workplaces[0]) {
    state.departmentForm.workplaceId = state.workplaces[0].id
  }
  // 员工表单没有场所默认值时，优先回填第一个场所。
  if (!state.employeeForm.workplaceId && state.workplaces[0]) {
    state.employeeForm.workplaceId = state.workplaces[0].id
  }
  // 员工表单没有部门默认值时，优先回填第一个部门。
  if (!state.employeeForm.departmentId && state.departments[0]) {
    state.employeeForm.departmentId = state.departments[0].id
  }
}

// 清理因为数据刷新而失效的筛选条件，避免前端继续保留不存在的选项。
export const syncFilterReferences = (state) => {
  // 部门筛选引用的场所不存在时清空场所筛选。
  if (
    state.departmentFilters.workplaceId &&
    !state.workplaces.some((item) => String(item.id) === String(state.departmentFilters.workplaceId))
  ) {
    state.departmentFilters.workplaceId = ''
  }
  // 员工筛选引用的部门不存在时清空部门筛选。
  if (
    state.employeeFilters.departmentId &&
    !state.departments.some((item) => String(item.id) === String(state.employeeFilters.departmentId))
  ) {
    state.employeeFilters.departmentId = ''
  }
  // 排班筛选引用的场所不存在时清空场所筛选。
  if (
    state.scheduleFilters.workplaceId &&
    !state.workplaces.some((item) => String(item.id) === String(state.scheduleFilters.workplaceId))
  ) {
    state.scheduleFilters.workplaceId = ''
  }
  // 排班筛选引用的部门不存在时清空部门筛选。
  if (
    state.scheduleFilters.departmentId &&
    !state.departments.some((item) => String(item.id) === String(state.scheduleFilters.departmentId))
  ) {
    state.scheduleFilters.departmentId = ''
  }
}

// 把当前各区块的已加载数据量写回轻量壳，供导航徽标和壳层状态汇总复用。
export const syncShellCounters = (state) => {
  // 用场所列表长度回填场所计数。
  state.bootstrapShell.sectionCounters.workplace = state.workplaces.length
  // 用部门列表长度回填部门计数。
  state.bootstrapShell.sectionCounters.department = state.departments.length
  // 用员工列表长度回填员工计数。
  state.bootstrapShell.sectionCounters.employee = state.employees.length
  // 用班次模板列表长度回填班次计数。
  state.bootstrapShell.sectionCounters.shift = state.shiftTemplates.length
  // 用排班项列表长度回填排班计数。
  state.bootstrapShell.sectionCounters.schedule = state.scheduleBoard.scheduleItems.length
  // 用打卡总记录数回填第三阶段打卡计数，供导航徽标和工作台状态展示。
  state.bootstrapShell.sectionCounters.punch = state.punchLogList.total
  // 用日次总记录数回填第四阶段日次计数，供导航徽标和工作台状态展示。
  state.bootstrapShell.sectionCounters.daily = state.dailyList.total
  // 用第五阶段处理单总数回填异常处理计数，供导航徽标和工作台状态展示。
  state.bootstrapShell.sectionCounters.case = state.caseList.total
}
