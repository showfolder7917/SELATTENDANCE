// 这里封装部门区块的读取、编辑和跨区块跳转动作，减少工作台入口层的组织逻辑噪音。

// 部门区块依赖部门服务，并会驱动员工和排班筛选联动。
import { createDepartment, deleteDepartment, listDepartments, updateDepartment } from '../../services'

// 创建部门区块控制器，供工作台入口按需装配部门行为。
export const createDepartmentSection = ({
  state,
  activeSection,
  setSectionLoading,
  setSectionError,
  pushToast,
  t,
  refreshShell
}) => {
  // 独立读取部门列表，供部门区块和员工/排班筛选共享使用。
  const loadDepartments = async () => {
    // 标记部门区块进入加载态，避免整页阻塞。
    setSectionLoading('department', true)
    // 清空上一轮部门区块错误，保证新的加载结果独立展示。
    setSectionError('department', '')
    try {
      // 调用部门独立接口，按区块需要加载部门数据。
      state.departments = await listDepartments()
      // 成功后标记部门区块已完成首次加载。
      state.bootstrapShell.sectionStates.department = true
    } catch (error) {
      // 记录部门区块错误，供后续局部错误展示或日志定位。
      setSectionError('department', error?.message || 'load department failed')
      // 抛出异常给入口层决定后续联动是否继续。
      throw error
    } finally {
      // 无论成功失败都结束部门区块加载态。
      setSectionLoading('department', false)
    }
  }

  // 提交部门表单，供部门区块新增或更新组织节点。
  const submitDepartment = async () => {
    // 复制一份部门表单作为提交载荷，避免请求过程改写原对象。
    const payload = { ...state.departmentForm }
    // 有主键时走更新逻辑，维护既有部门资料。
    if (payload.id) {
      await updateDepartment(payload.id, payload)
    } else {
      // 无主键时走新增逻辑，创建新的组织节点。
      await createDepartment(payload)
    }
    // 保存后重新读取部门列表，保证页面拿到最新部门数据。
    await loadDepartments()
    // 刷新首页壳步骤和推荐动作，保持轻量壳计数同步。
    await refreshShell()
    // 重置部门表单，避免编辑状态残留到下一次新增。
    resetDepartmentForm()
    // 给出统一保存成功提示，保持现有交互反馈一致。
    pushToast(t('toastSaved'))
  }

  // 删除部门主数据，供部门区块移除不再使用的组织节点。
  const removeDepartment = async (id) => {
    // 调用部门删除接口，删除指定组织节点。
    await deleteDepartment(id)
    // 删除后重新读取部门列表，保持页面和筛选数据一致。
    await loadDepartments()
    // 刷新首页壳步骤和推荐动作，反映删除后的数据状态。
    await refreshShell()
    // 给出统一删除成功提示，保持工作台反馈统一。
    pushToast(t('toastDeleted'))
  }

  // 把选中的部门资料回填到部门表单，供部门区块进入编辑模式。
  const editDepartment = (item) => {
    // 直接用现有部门记录覆盖表单，便于用户在原值上修改。
    Object.assign(state.departmentForm, item)
  }

  // 从场所卡片跳转到部门区块，并预置该场所作为部门筛选条件。
  const openWorkplaceDepartments = (item) => {
    // 把当前场所写入部门筛选，进入部门区块后只看该场所下的部门。
    state.departmentFilters.workplaceId = String(item.id)
    // 同步回填部门表单默认场所，方便直接新建该场所下部门。
    state.departmentForm.workplaceId = item.id
    // 切换到部门区块，让用户直接进入组织维护视图。
    activeSection.value = 'department'
  }

  // 清空部门场所筛选，供部门区块恢复查看全部部门。
  const clearDepartmentWorkplaceFilter = () => {
    // 把场所筛选重置为空，恢复全部部门列表视图。
    state.departmentFilters.workplaceId = ''
  }

  // 重置部门表单，供部门区块回到新增状态。
  const resetDepartmentForm = () => {
    // 用当前已知的第一个场所作为默认值，减少用户新增部门时的重复选择。
    Object.assign(state.departmentForm, {
      id: null,
      workplaceId: state.workplaces[0]?.id || '',
      departmentCode: '',
      departmentName: '',
      sortOrder: 0,
      status: 'ACTIVE'
    })
  }

  // 返回部门区块全部动作，供工作台入口和其他区块联动复用。
  return {
    loadDepartments,
    submitDepartment,
    removeDepartment,
    editDepartment,
    openWorkplaceDepartments,
    clearDepartmentWorkplaceFilter,
    resetDepartmentForm
  }
}
