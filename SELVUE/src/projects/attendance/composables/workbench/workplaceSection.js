// 这里封装场所区块的读取、编辑和删除动作，避免工作台入口继续塞满场所细节。

// 场所区块模块只依赖场所服务和少量共享状态回调。
import { createWorkplace, deleteWorkplace, listWorkplaces, updateWorkplace } from '../../services'

// 创建场所区块控制器，供工作台入口按需装配场所行为。
export const createWorkplaceSection = ({ state, setSectionLoading, setSectionError, pushToast, t, refreshShell }) => {
  // 独立读取场所列表，供场所区块和依赖场所的其他区块共享主数据。
  const loadWorkplaces = async () => {
    // 标记场所区块进入加载中，避免整页只靠全局 loading。
    setSectionLoading('workplace', true)
    // 清空上一轮场所区块错误，避免旧错误残留到新请求。
    setSectionError('workplace', '')
    try {
      // 调用独立场所接口，避免继续依赖重 bootstrap 返回全量数据。
      state.workplaces = await listWorkplaces()
      // 读取成功后把 section 标记为已完成首次加载。
      state.bootstrapShell.sectionStates.workplace = true
    } catch (error) {
      // 记录场所区块错误，供页面后续扩展局部错误展示。
      setSectionError('workplace', error?.message || 'load workplace failed')
      // 失败时继续抛出异常，让入口层决定是否吞掉或继续联动。
      throw error
    } finally {
      // 无论成功失败都结束场所区块加载态。
      setSectionLoading('workplace', false)
    }
  }

  // 提交场所表单，供场所区块新增或更新主数据。
  const submitWorkplace = async () => {
    // 从场所表单中复制一份提交载荷，避免请求过程直接污染原响应式对象。
    const payload = { ...state.workplaceForm }
    // 有主键时走更新流程，保持编辑与新增共用一个表单。
    if (payload.id) {
      await updateWorkplace(payload.id, payload)
    } else {
      // 无主键时走新增流程，登记新的工作地点。
      await createWorkplace(payload)
    }
    // 提交完成后重新读取场所列表，保证页面看到数据库最终状态。
    await loadWorkplaces()
    // 刷新首页壳步骤和推荐动作，保证轻量壳计数同步更新。
    await refreshShell()
    // 重置场所表单，避免编辑状态残留到下一次新增。
    resetWorkplaceForm()
    // 给出统一保存成功提示，保持当前工作台交互习惯。
    pushToast(t('toastSaved'))
  }

  // 删除场所主数据，供场所区块移除不再使用的工作地点。
  const removeWorkplace = async (id) => {
    // 调用场所删除接口，移除指定场所记录。
    await deleteWorkplace(id)
    // 删除后重新加载场所列表，保证页面和筛选数据同步。
    await loadWorkplaces()
    // 刷新首页壳步骤和推荐动作，反映删除后的基础数据状态。
    await refreshShell()
    // 给出统一删除成功提示，保持当前工作台反馈方式。
    pushToast(t('toastDeleted'))
  }

  // 把选中的场所回填到表单，供场所区块进入编辑模式。
  const editWorkplace = (item) => {
    // 直接把现有场所资料写入表单，供用户在原值基础上修改。
    Object.assign(state.workplaceForm, item)
  }

  // 重置场所表单，供场所区块返回新增状态。
  const resetWorkplaceForm = () => {
    // 用初始空值覆盖场所表单，避免旧编辑数据残留。
    Object.assign(state.workplaceForm, {
      id: null,
      workplaceCode: '',
      workplaceName: '',
      address: '',
      phone: '',
      status: 'ACTIVE'
    })
  }

  // 返回场所区块全部动作，供工作台入口统一编排。
  return {
    loadWorkplaces,
    submitWorkplace,
    removeWorkplace,
    editWorkplace,
    resetWorkplaceForm
  }
}
