// 这里封装排班区块的看板读取、单条排班和批量操作，避免工作台入口继续承载排班细节。

// 排班区块依赖排班独立接口，并会和班次模板、员工行数据联动。
import {
  batchAssignSchedules,
  checkUnassignedSchedules,
  copyLastMonthSchedules,
  copyLastWeekSchedules,
  createSchedule,
  deleteSchedule,
  exportSchedules,
  fetchScheduleBoard
} from '../../services'

// 创建排班区块控制器，供工作台入口按需装配排班行为。
export const createScheduleSection = ({
  state,
  activeSection,
  setSectionLoading,
  setSectionError,
  pushToast,
  t,
  downloadCsv,
  requestConfirm
}) => {
  // 统一打开模板引导气泡，把刚刚失败的点击上下文写给右侧模板区显示。
  const openScheduleTemplateTip = (employeeName, workDate) => {
    state.scheduleTemplateTip.open = true
    state.scheduleTemplateTip.employeeName = employeeName
    state.scheduleTemplateTip.workDate = workDate
  }

  // 用户手动关闭气泡时只收起提示，不清空当前排班选择上下文。
  const closeScheduleTemplateTip = () => {
    state.scheduleTemplateTip.open = false
  }

  // 独立读取排班看板，供排班区块按当前筛选条件局部刷新。
  const loadScheduleBoard = async () => {
    // 标记排班区块进入加载态，支持局部刷新不阻塞整页。
    setSectionLoading('schedule', true)
    // 清空排班区块上一轮错误，保证新请求独立。
    setSectionError('schedule', '')
    try {
      // 按当前排班筛选条件读取看板，避免继续从 bootstrap 拿整页数据。
      const payload = await fetchScheduleBoard({ ...state.scheduleFilters })
      // 把服务端返回的看板结构整体写回状态，供排班区块直接渲染。
      state.scheduleBoard = {
        ...payload,
        endDate: payload.endDate || state.scheduleBoard.endDate || `${state.scheduleFilters.month}-01`
      }
      // 同步看板里返回的模板集合，供单条排班选择模板。
      state.scheduleBoard.shiftTemplates = payload.shiftTemplates || []
      // 成功后标记排班区块已完成首次加载。
      state.bootstrapShell.sectionStates.schedule = true
    } catch (error) {
      // 记录排班区块错误，供局部错误展示和排障定位。
      setSectionError('schedule', error?.message || 'load schedule board failed')
      // 抛出异常给入口层决定是否终止后续联动。
      throw error
    } finally {
      // 无论成功失败都结束排班区块加载态。
      setSectionLoading('schedule', false)
    }
  }

  // 选择排班模板，供排班区块后续把同一模板应用到单条或批量排班。
  const selectScheduleTemplate = (template) => {
    // 把模板主键写入排班表单，供后续保存动作引用。
    state.scheduleForm.selectedTemplateId = template.id
    // 把模板名称写入排班表单，供覆盖确认和界面提示复用。
    state.scheduleForm.selectedTemplateName = template.templateName
    // 一旦已经选中模板，就把“先选模板”的引导气泡收起，避免继续遮挡模板区。
    closeScheduleTemplateTip()
  }

  // 应用单条排班，供排班区块把选中模板写到某位员工某一天。
  const applySchedule = async (row, workDate, existingItem) => {
    // 先把当前操作上下文写入排班表单，供确认提示和删除回填复用。
    Object.assign(state.scheduleForm, {
      selectedEmployeeId: row.employeeId,
      selectedEmployeeName: row.employeeName,
      selectedWorkDate: workDate,
      selectedScheduleId: existingItem?.id || null,
      selectedTemplateName: existingItem?.templateName || state.scheduleForm.selectedTemplateName
    })
    // 没有先选模板时直接阻断保存，避免写入无效排班。
    if (!state.scheduleForm.selectedTemplateId) {
      // 未选模板时在右侧模板区打开指向性气泡，引导用户去正确位置选择模板。
      openScheduleTemplateTip(row.employeeName, workDate)
      return
    }
    // 已存在排班项时弹覆盖确认，避免用户误覆盖原排班。
    if (existingItem) {
      // 已有排班时先走统一确认弹窗，避免覆盖动作继续落回浏览器原生确认框。
      const confirmed = await requestConfirm({
        title: t('scheduleReplaceConfirmAction'),
        message: t('scheduleOverwriteConfirm')
          .replace('{current}', existingItem.templateName)
          .replace('{next}', state.scheduleForm.selectedTemplateName || '-'),
        confirmLabel: t('scheduleReplaceConfirmAction')
      })
      if (!confirmed) {
        return
      }
    }
    // 提交单条排班创建请求，让后端按模板建立该日排班项。
    await createSchedule({
      employeeId: row.employeeId,
      workDate,
      shiftTemplateId: state.scheduleForm.selectedTemplateId,
      remark: state.scheduleForm.remark
    })
    // 保存后重新同步当前模板名称，保证后续连续排班提示一致。
    const selectedTemplate = state.scheduleBoard.shiftTemplates.find(
      (item) => item.id === state.scheduleForm.selectedTemplateId
    )
    state.scheduleForm.selectedTemplateName = selectedTemplate?.templateName || ''
    // 给出保存或覆盖成功提示，保持排班区块当前交互体验。
    pushToast(existingItem ? t('scheduleToastReplaced') : t('scheduleToastSaved'))
    // 重新读取排班看板，保证单条排班结果立即体现在看板上。
    await loadScheduleBoard()
  }

  // 删除单条排班项，供排班区块移除错误或过期排班。
  const removeScheduleItem = async (id) => {
    // 调用排班删除接口，删除目标排班项。
    await deleteSchedule(id)
    // 清空当前排班表单的选中上下文，避免界面残留已删除记录。
    Object.assign(state.scheduleForm, {
      selectedScheduleId: null,
      selectedTemplateName: '',
      selectedWorkDate: '',
      selectedEmployeeId: null,
      selectedEmployeeName: ''
    })
    // 给出统一删除成功提示，保持工作台反馈一致。
    pushToast(t('toastDeleted'))
    // 删除后重读排班看板，保证看板立即反映最新结果。
    await loadScheduleBoard()
  }

  // 打开批量排班向导，供排班区块批量给当前看板员工分配同一模板。
  const openBatchWizard = () => {
    // 预填向导当前上下文，减少用户再次选择员工范围和日期范围。
    Object.assign(state.batchWizard, {
      open: true,
      step: 1,
      employeeIds: state.scheduleBoard.employeeRows.map((item) => item.employeeId),
      startDate: `${state.scheduleFilters.month}-01`,
      endDate: state.scheduleBoard.endDate || `${state.scheduleFilters.month}-01`,
      shiftTemplateId: state.scheduleForm.selectedTemplateId,
      skipExisting: false,
      overwriteExisting: true,
      remark: state.scheduleForm.remark
    })
  }

  // 关闭批量排班向导，供排班区块退出批量操作流程。
  const closeBatchWizard = () => {
    // 只关闭向导并重置步骤，保留其他字段由下次打开时整体覆盖。
    state.batchWizard.open = false
    state.batchWizard.step = 1
  }

  // 向前推进批量排班向导步骤，供向导流程逐步确认配置。
  const nextBatchStep = () => {
    // 把向导步骤限制在 1 到 5 之间，避免越界。
    state.batchWizard.step = Math.min(5, state.batchWizard.step + 1)
  }

  // 回退批量排班向导步骤，供用户返回上一步调整配置。
  const prevBatchStep = () => {
    // 把向导步骤限制在 1 到 5 之间，避免越界。
    state.batchWizard.step = Math.max(1, state.batchWizard.step - 1)
  }

  // 确认批量排班向导，供排班区块一次性写入一段日期范围的排班。
  const confirmBatchWizard = async () => {
    // 批量排班先走统一确认弹窗，避免大范围写入动作继续弹出系统原生确认框。
    const confirmed = await requestConfirm({
      title: t('scheduleBatchConfirm'),
      message: t('scheduleBatchConfirmDialog'),
      confirmLabel: t('scheduleBatchConfirm')
    })
    if (!confirmed) {
      return
    }
    // 组装批量排班载荷，按向导当前配置提交给后端。
    const payload = {
      employeeIds: [...state.batchWizard.employeeIds],
      startDate: state.batchWizard.startDate,
      endDate: state.batchWizard.endDate,
      shiftTemplateId: Number(state.batchWizard.shiftTemplateId),
      skipExisting: state.batchWizard.skipExisting,
      overwriteExisting: state.batchWizard.overwriteExisting,
      remark: state.batchWizard.remark
    }
    // 执行批量排班，让后端统一返回新增、更新和跳过统计。
    const result = await batchAssignSchedules(payload)
    // 用统一提示展示批量排班结果，让用户快速看到处理统计。
    pushToast(
      t('scheduleBatchResultToast')
        .replace('{created}', String(result.createdCount))
        .replace('{updated}', String(result.updatedCount))
        .replace('{skipped}', String(result.skippedCount))
    )
    // 批量排班完成后关闭向导，回到排班看板主视图。
    closeBatchWizard()
    // 重新读取排班看板，保证批量结果立即反映到当前月份。
    await loadScheduleBoard()
  }

  // 复制上周排班到当前月区间，供排班区块快速铺排重复班表。
  const copySchedulesFromLastWeek = async () => {
    // 复制上周前统一走页面确认弹窗，保证复制类动作和删除类动作复用同一套组件。
    const confirmed = await requestConfirm({
      title: t('scheduleCopyLastWeek'),
      message: t('scheduleCopyWeekConfirm'),
      confirmLabel: t('scheduleCopyLastWeek')
    })
    if (!confirmed) {
      return
    }
    // 提交复制上周请求，让后端按当前看板员工和日期范围复制排班。
    const result = await copyLastWeekSchedules({
      employeeIds: state.scheduleBoard.employeeRows.map((item) => item.employeeId),
      startDate: `${state.scheduleFilters.month}-01`,
      endDate: state.scheduleBoard.endDate || `${state.scheduleFilters.month}-01`,
      overwriteExisting: true
    })
    // 展示复制结果统计，让用户快速判断影响范围。
    pushToast(
      t('scheduleCopyResultToast')
        .replace('{created}', String(result.createdCount))
        .replace('{updated}', String(result.updatedCount))
        .replace('{skipped}', String(result.skippedCount))
    )
    // 复制完成后刷新看板，保证结果立即可见。
    await loadScheduleBoard()
  }

  // 复制上月排班到当前月区间，供排班区块快速复用周期性班表。
  const copySchedulesFromLastMonth = async () => {
    // 复制上月前统一走页面确认弹窗，避免本地环境继续出现浏览器原生确认框。
    const confirmed = await requestConfirm({
      title: t('scheduleCopyLastMonth'),
      message: t('scheduleCopyMonthConfirm'),
      confirmLabel: t('scheduleCopyLastMonth')
    })
    if (!confirmed) {
      return
    }
    // 提交复制上月请求，让后端按当前看板员工和日期范围复制排班。
    const result = await copyLastMonthSchedules({
      employeeIds: state.scheduleBoard.employeeRows.map((item) => item.employeeId),
      startDate: `${state.scheduleFilters.month}-01`,
      endDate: state.scheduleBoard.endDate || `${state.scheduleFilters.month}-01`,
      overwriteExisting: true
    })
    // 展示复制结果统计，帮助用户判断复制动作影响范围。
    pushToast(
      t('scheduleCopyResultToast')
        .replace('{created}', String(result.createdCount))
        .replace('{updated}', String(result.updatedCount))
        .replace('{skipped}', String(result.skippedCount))
    )
    // 复制完成后刷新看板，让结果立即反映在当前月份视图。
    await loadScheduleBoard()
  }

  // 检查未排班员工，供排班区块识别当前筛选范围下的缺排人员。
  const runUnassignedCheck = async () => {
    // 调用未排班检查接口，并把结果写回未排班列表。
    state.scheduleUnassignedItems = await checkUnassignedSchedules({ ...state.scheduleFilters })
    // 给出检查完成提示，告诉用户未排班结果已刷新。
    pushToast(t('scheduleUnassignedChecked'))
  }

  // 导出当前排班筛选结果，供排班区块对外共享或线下核对。
  const handleScheduleExport = async () => {
    // 先让后端按当前筛选条件生成排班 CSV 内容。
    const payload = await exportSchedules({ ...state.scheduleFilters })
    // 复用共享下载工具，把排班导出结果转成浏览器下载动作。
    downloadCsv(payload)
  }

  // 从部门卡片跳转到排班区块，并预置该部门的排班筛选条件。
  const openDepartmentSchedule = async (item) => {
    // 记录切换前是否已经在排班区块，便于同区块内手动刷新。
    const wasScheduleSection = activeSection.value === 'schedule'
    // 把当前场所写入排班筛选，只看该部门所在场所的数据。
    state.scheduleFilters.workplaceId = String(item.workplaceId)
    // 把当前部门写入排班筛选，只看该部门下的排班数据。
    state.scheduleFilters.departmentId = String(item.id)
    // 清空员工关键字筛选，避免沿用旧搜索词影响新部门视图。
    state.scheduleFilters.employeeKeyword = ''
    // 关闭仅看未排班筛选，恢复该部门的完整排班视图。
    state.scheduleFilters.onlyUnassigned = false
    // 切换到排班区块，让用户直接进入该部门排班视图。
    activeSection.value = 'schedule'
    // 如果本来就在排班区块，则主动刷新一次看板以响应新的部门上下文。
    if (wasScheduleSection) {
      await loadScheduleBoard()
    }
  }

  // 返回排班区块全部动作，供工作台入口统一编排。
  return {
    loadScheduleBoard,
    selectScheduleTemplate,
    closeScheduleTemplateTip,
    applySchedule,
    removeScheduleItem,
    openBatchWizard,
    closeBatchWizard,
    nextBatchStep,
    prevBatchStep,
    confirmBatchWizard,
    copySchedulesFromLastWeek,
    copySchedulesFromLastMonth,
    runUnassignedCheck,
    handleScheduleExport,
    openDepartmentSchedule
  }
}
