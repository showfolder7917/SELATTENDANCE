// 这里封装员工区块的读取、导入导出和映射动作，避免工作台入口继续承担员工细节流程。

// 员工区块依赖员工读写服务、外部映射服务以及导入导出接口。
import {
  bindExternalMapping,
  createEmployee,
  deleteEmployee,
  exportEmployees,
  importEmployees,
  listEmployees,
  updateEmployee
} from '../../services'

// 创建员工区块控制器，供工作台入口按需装配员工行为。
export const createEmployeeSection = ({
  state,
  activeSection,
  setSectionLoading,
  setSectionError,
  pushToast,
  t,
  refreshShell,
  downloadCsv
}) => {
  // 独立读取员工列表，供员工区块按筛选条件局部刷新数据。
  const loadEmployees = async () => {
    // 标记员工区块加载中，支持多区块并发时独立跟踪状态。
    setSectionLoading('employee', true)
    // 清空员工区块上一轮错误，保证新请求结果独立。
    setSectionError('employee', '')
    try {
      // 按当前员工筛选条件读取员工列表，避免重聚合全量返回。
      state.employees = await listEmployees({ ...state.employeeFilters })
      // 成功后标记员工区块已完成首次加载。
      state.bootstrapShell.sectionStates.employee = true
    } catch (error) {
      // 记录员工区块错误，供后续局部错误展示和排障定位。
      setSectionError('employee', error?.message || 'load employee failed')
      // 把错误继续抛给入口层，由入口层决定是否终止联动。
      throw error
    } finally {
      // 结束员工区块加载态，释放局部 loading。
      setSectionLoading('employee', false)
    }
  }

  // 提交员工表单，供员工区块新增或更新员工资料。
  const submitEmployee = async () => {
    // 复制员工表单，避免请求期间直接修改响应式源对象。
    const payload = { ...state.employeeForm }
    // 有主键时更新员工资料，保持新增和编辑共用一套表单。
    if (payload.id) {
      await updateEmployee(payload.id, payload)
    } else {
      // 无主键时新增员工资料，登记新的排班对象。
      await createEmployee(payload)
    }
    // 保存后重读员工列表，保证页面看到数据库最新员工。
    await loadEmployees()
    // 刷新首页壳步骤和推荐动作，保持轻量壳状态同步。
    await refreshShell()
    // 重置员工表单，避免编辑态残留到下一次新增。
    resetEmployeeForm()
    // 给出统一保存成功提示，保持现有工作台反馈一致。
    pushToast(t('toastSaved'))
  }

  // 删除员工资料，供员工区块移除离职或误建员工。
  const removeEmployee = async (id) => {
    // 调用员工删除接口，移除目标员工记录。
    await deleteEmployee(id)
    // 删除后重新读取员工列表，保持当前列表和筛选结果一致。
    await loadEmployees()
    // 刷新首页壳步骤和推荐动作，反映员工数量变化。
    await refreshShell()
    // 给出统一删除成功提示，保持工作台反馈统一。
    pushToast(t('toastDeleted'))
  }

  // 提交外部映射表单，供员工区块维护本地员工与外部考勤系统的对应关系。
  const submitMapping = async () => {
    // 取出当前映射表单快照，避免请求期间被用户继续改写。
    const payload = { ...state.mappingForm }
    // 调用外部映射接口，把映射关系绑定到指定员工。
    await bindExternalMapping(payload.employeeId, payload)
    // 映射保存后重读员工列表，保证列表列值立即反映最新映射。
    await loadEmployees()
    // 重置映射表单，避免上一位员工的映射信息残留。
    resetMappingForm()
    // 给出统一保存成功提示，保持当前交互风格。
    pushToast(t('toastSaved'))
  }

  // 提交员工 CSV 导入，供员工区块批量落地员工主数据。
  const submitImport = async () => {
    // 把文本区域中的 CSV 文本包装成后端需要的导入载荷。
    const payload = await importEmployees({ csvText: state.importCsvText })
    // 保存导入结果，供员工区块展示新增/更新统计。
    state.importResult = payload
    // 导入后重读员工列表，保证批量导入结果立即可见。
    await loadEmployees()
    // 刷新首页壳步骤和推荐动作，保持员工计数和引导同步。
    await refreshShell()
    // 给出统一导入成功提示，告诉用户批量动作已生效。
    pushToast(t('toastImported'))
  }

  // 导出员工 CSV，供员工区块批量核对或对外交换数据。
  const handleExport = async () => {
    // 先从后端获取员工导出结果，拿到文件名和文本内容。
    const payload = await exportEmployees()
    // 复用共享下载工具，把员工导出结果转成浏览器下载动作。
    downloadCsv(payload)
  }

  // 把部门卡片上下文带入员工区块，并预置员工筛选条件。
  const openDepartmentEmployees = (item) => {
    // 清空关键字筛选，避免沿用旧关键字影响新部门视图。
    state.employeeFilters.keyword = ''
    // 预置当前部门筛选，只显示该部门下的员工。
    state.employeeFilters.departmentId = String(item.id)
    // 清空雇佣类型筛选，避免旧筛选条件叠加。
    state.employeeFilters.employmentType = ''
    // 清空状态筛选，恢复该部门的完整员工视图。
    state.employeeFilters.status = ''
    // 把当前场所回填到员工表单，方便直接在该部门下新增员工。
    state.employeeForm.workplaceId = item.workplaceId
    // 把当前部门回填到员工表单，减少新增时重复选择。
    state.employeeForm.departmentId = item.id
    // 切换到员工区块，让用户直接进入该部门员工维护视图。
    activeSection.value = 'employee'
  }

  // 把员工资料回填到员工表单，供员工区块进入编辑模式。
  const editEmployee = (item) => {
    // 只挑选员工表单实际需要的字段，避免把列表展示字段误写回表单。
    Object.assign(state.employeeForm, {
      id: item.id,
      employeeNo: item.employeeNo,
      employeeName: item.employeeName,
      employeeNameKana: item.employeeNameKana,
      employmentType: item.employmentType,
      hireDate: item.hireDate,
      workplaceId: item.workplaceId,
      departmentId: item.departmentId,
      status: item.status
    })
  }

  // 把员工映射资料回填到映射表单，供员工区块进入映射编辑模式。
  const editMapping = (item) => {
    // 仅回填映射表单需要的字段，避免把列表无关字段带入映射保存。
    Object.assign(state.mappingForm, {
      employeeId: item.id,
      sourceSystem: item.externalSourceSystem || 'KING_OF_TIME',
      externalEmployeeId: item.externalEmployeeId || '',
      externalEmployeeNo: item.externalEmployeeNo || '',
      status: 'ACTIVE'
    })
  }

  // 重置员工表单，供员工区块回到新增状态。
  const resetEmployeeForm = () => {
    // 用当前已知的默认场所和部门回填表单，减少新增员工时重复操作。
    Object.assign(state.employeeForm, {
      id: null,
      employeeNo: '',
      employeeName: '',
      employeeNameKana: '',
      employmentType: 'FULL_TIME',
      hireDate: '',
      workplaceId: state.workplaces[0]?.id || '',
      departmentId: state.departments[0]?.id || '',
      status: 'ACTIVE'
    })
  }

  // 重置员工映射表单，供员工区块开始新的映射绑定。
  const resetMappingForm = () => {
    // 覆盖为默认映射初值，避免上一位员工的外部账号信息残留。
    Object.assign(state.mappingForm, {
      employeeId: null,
      sourceSystem: 'KING_OF_TIME',
      externalEmployeeId: '',
      externalEmployeeNo: '',
      status: 'ACTIVE'
    })
  }

  // 返回员工区块全部动作，供工作台入口统一编排。
  return {
    loadEmployees,
    submitEmployee,
    removeEmployee,
    submitMapping,
    submitImport,
    handleExport,
    openDepartmentEmployees,
    editEmployee,
    editMapping,
    resetEmployeeForm,
    resetMappingForm
  }
}
