// 这里封装班次模板区块的读取和维护动作，避免工作台入口继续承担模板细节流程。

// 班次区块依赖班次模板独立接口和推荐模板生成接口。
import {
  createShiftTemplate,
  deleteShiftTemplate,
  generateRecommendedShiftTemplates,
  listShiftTemplates,
  updateShiftTemplate
} from '../../services'

// 创建班次模板区块控制器，供工作台入口按需装配模板行为。
export const createShiftSection = ({ state, setSectionLoading, setSectionError, pushToast, t, refreshShell }) => {
  // 独立读取班次模板列表，供班次区块和排班模板选择共享使用。
  const loadShiftTemplates = async () => {
    // 标记班次区块进入加载态，支持多区块独立加载反馈。
    setSectionLoading('shift', true)
    // 清空班次区块上一轮错误，保证新请求独立。
    setSectionError('shift', '')
    try {
      // 调用班次模板独立接口，按区块需要读取模板数据。
      state.shiftTemplates = await listShiftTemplates()
      // 成功后标记班次区块已完成首次加载。
      state.bootstrapShell.sectionStates.shift = true
    } catch (error) {
      // 记录班次区块错误，供局部错误展示和排障定位。
      setSectionError('shift', error?.message || 'load shift template failed')
      // 抛出异常给入口层决定后续联动是否继续。
      throw error
    } finally {
      // 无论成功失败都结束班次区块加载态。
      setSectionLoading('shift', false)
    }
  }

  // 提交班次模板表单，供班次区块新增或更新模板。
  const submitShiftTemplate = async () => {
    // 复制班次模板表单，避免请求过程直接改写响应式对象。
    const payload = { ...state.shiftForm }
    // 已有主键时走更新逻辑，维护既有模板资料。
    if (payload.id) {
      await updateShiftTemplate(payload.id, payload)
    } else {
      // 没有主键时走新增逻辑，创建新的排班模板。
      await createShiftTemplate(payload)
    }
    // 保存后重读模板列表，保证页面看到数据库最新模板。
    await loadShiftTemplates()
    // 刷新首页壳步骤和推荐动作，保持模板计数与引导同步。
    await refreshShell()
    // 重置班次模板表单，避免编辑态残留到下一次新增。
    resetShiftForm()
    // 给出统一保存成功提示，保持工作台反馈一致。
    pushToast(t('toastSaved'))
  }

  // 触发推荐模板生成，供班次区块快速初始化模板集合。
  const generateRecommended = async () => {
    // 调用推荐模板接口，生成一组业务预置模板。
    await generateRecommendedShiftTemplates()
    // 生成后重读模板列表，确保页面立即看到推荐模板结果。
    await loadShiftTemplates()
    // 刷新首页壳步骤和推荐动作，反映模板数量变化。
    await refreshShell()
    // 给出统一保存成功提示，告诉用户推荐模板已生成。
    pushToast(t('toastSaved'))
  }

  // 删除班次模板，供班次区块移除不再使用的排班模板。
  const removeShiftTemplate = async (id) => {
    // 调用模板删除接口，移除指定班次模板。
    await deleteShiftTemplate(id)
    // 删除后重新读取模板列表，保持页面和模板选择一致。
    await loadShiftTemplates()
    // 刷新首页壳步骤和推荐动作，反映删除后的模板状态。
    await refreshShell()
    // 给出统一删除成功提示，保持反馈一致。
    pushToast(t('toastDeleted'))
  }

  // 把选中的模板回填到模板表单，供班次区块进入编辑模式。
  const editShiftTemplate = (item) => {
    // 直接用模板记录覆盖表单，让用户在原值基础上修改。
    Object.assign(state.shiftForm, item)
  }

  // 重置班次模板表单，供班次区块回到新增状态。
  const resetShiftForm = () => {
    // 用默认业务值覆盖表单，保证新的模板录入从稳定初始值开始。
    Object.assign(state.shiftForm, {
      id: null,
      templateCode: '',
      templateName: '',
      shiftType: 'WORK',
      startTime: '09:00:00',
      endTime: '18:00:00',
      crossDay: false,
      scheduledBreakMinutes: 60,
      color: 'BLUE',
      active: true
    })
  }

  // 返回班次模板区块全部动作，供工作台入口统一编排。
  return {
    loadShiftTemplates,
    submitShiftTemplate,
    generateRecommended,
    removeShiftTemplate,
    editShiftTemplate,
    resetShiftForm
  }
}
