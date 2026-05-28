// 这里封装第三阶段打卡区块的列表、详情、手动补录和 CSV 导入动作。

// 第三阶段打卡区块依赖打卡接口，并会和员工主数据联动完成未匹配处理。
import {
  bindPunchEmployee,
  createManualPunch,
  getPunchLogDetail,
  ignorePunchLog,
  importPunchCsv,
  listPunchLogs,
  previewPunchImport,
  reprocessPunchLog
} from '../../services'

// 创建打卡区块控制器，供工作台入口按需装配第三阶段行为。
export const createPunchSection = ({
  state,
  setSectionLoading,
  setSectionError,
  pushToast,
  t,
  refreshShell
}) => {
  // 独立读取打卡列表，供第三阶段区块按当前筛选条件局部刷新。
  const loadPunchLogs = async () => {
    // 标记打卡区块进入加载态，支持第三阶段单独刷新不阻塞整页。
    setSectionLoading('punch', true)
    // 清空打卡区块上一轮错误，保证新请求结果独立。
    setSectionError('punch', '')
    try {
      // 按当前打卡筛选条件读取原始记录列表和摘要，避免首页壳继续聚合全量数据。
      state.punchLogList = await listPunchLogs({ ...state.punchFilters })
      // 如果当前页已经超过后端给出的总页数，立即回退到最后一页并重查，避免筛选后出现空白页。
      if (
        state.punchLogList.total > 0 &&
        state.punchFilters.page > state.punchLogList.totalPages
      ) {
        state.punchFilters.page = state.punchLogList.totalPages
        state.punchLogList = await listPunchLogs({ ...state.punchFilters })
      }
      // 列表成功后标记第三阶段打卡区块已完成首次加载。
      state.bootstrapShell.sectionStates.punch = true
      // 当前详情不存在或已经不在当前列表中时，默认选中第一条记录便于用户直接处理。
      const currentDetailId = state.punchDetail?.id
      const firstItem = state.punchLogList.items[0]
      if (!currentDetailId || !state.punchLogList.items.some((item) => item.id === currentDetailId)) {
        state.punchDetail = firstItem ? await getPunchLogDetail(firstItem.id) : null
      }
    } catch (error) {
      // 记录打卡区块错误，供局部错误展示和后续排障定位。
      setSectionError('punch', error?.message || 'load punch logs failed')
      // 把错误继续抛给入口层，由入口层决定是否终止联动。
      throw error
    } finally {
      // 无论成功失败都结束第三阶段打卡区块加载态。
      setSectionLoading('punch', false)
    }
  }

  // 读取单条打卡详情，供右侧详情面板和未匹配处理动作复用。
  const openPunchDetail = async (item) => {
    // 点击列表行时按主键读取详情，确保右侧拿到原始 payload 和可执行动作。
    state.punchDetail = await getPunchLogDetail(item.id)
    // 把当前记录主键写入动作表单上下文，便于后续绑定和忽略复用同一状态。
    state.punchActionForm.employeeId = state.punchDetail.employeeId || ''
  }

  // 提交手动补录打卡，供管理员处理员工漏打卡场景。
  const submitManualPunch = async () => {
    // 复制手动补录表单，避免请求期间直接污染响应式源对象。
    const payload = { ...state.punchManualForm }
    // 提交第三阶段手动补录请求，让后端统一落原始打卡事实。
    await createManualPunch(payload)
    // 补录成功后重读打卡列表，保证新事实立即出现在顶部列表中。
    await loadPunchLogs()
    // 刷新首页壳推荐动作和计数，让第三阶段进度同步回首页壳。
    await refreshShell()
    // 提示用户手动补录已经处理完成。
    pushToast(t('punchToastManualSaved'))
  }

  // 先预览 CSV 文本，供用户在正式导入前看到匹配和错误风险。
  const runPunchImportPreview = async () => {
    // 调用预览接口，把前 10 行结果和汇总写回当前页面状态。
    state.punchImportPreview = await previewPunchImport({ ...state.punchImportForm })
    // 提示用户预览结果已刷新，便于继续进入正式导入。
    pushToast(t('punchToastPreviewReady'))
  }

  // 正式提交 CSV 导入，供第三阶段批量接收原始打卡数据。
  const submitPunchImport = async () => {
    // 提交正式导入后端接口，让 CSV 文本落为原始打卡记录和导入批次。
    const result = await importPunchCsv({ ...state.punchImportForm })
    // 导入完成后重读列表，保证刚导入的记录和统计立即可见。
    await loadPunchLogs()
    // 刷新首页壳步骤和推荐动作，让第三阶段进度回写到首页壳。
    await refreshShell()
    // 把正式导入结果折叠写回预览位，方便用户继续在右侧看到本次结果摘要。
    state.punchImportPreview = {
      previewRows: state.punchImportPreview?.previewRows || [],
      summary: {
        totalCount: result.totalCount,
        readyCount: result.successCount,
        unmatchedCount: result.unmatchedCount,
        errorCount: result.errorCount
      }
    }
    // 用统一提示告诉用户本次正式导入已落库。
    pushToast(t('punchToastImported'))
  }

  // 对未匹配记录绑定已有员工，供第三阶段把外部编号修复到系统员工。
  const submitPunchBind = async () => {
    // 当前没有打开详情或没有选员工时，不进入无意义请求。
    if (!state.punchDetail?.id || !state.punchActionForm.employeeId) return
    // 提交绑定动作后，把当前记录状态更新为已处理。
    state.punchDetail = await bindPunchEmployee(state.punchDetail.id, {
      employeeId: Number(state.punchActionForm.employeeId)
    })
    // 绑定成功后重读列表，让摘要和当前列表状态同步变化。
    await loadPunchLogs()
    // 提示用户未匹配记录已经完成归属修复。
    pushToast(t('punchToastBound'))
  }

  // 忽略当前详情记录，供误导入或确认不处理的数据退出待处理流。
  const submitPunchIgnore = async () => {
    // 没有当前详情时直接返回，避免无主键请求。
    if (!state.punchDetail?.id) return
    // 提交忽略动作，并把忽略原因一并回写到当前记录。
    state.punchDetail = await ignorePunchLog(state.punchDetail.id, {
      reason: state.punchActionForm.ignoreReason
    })
    // 忽略后重读列表，保证摘要和状态分布立即更新。
    await loadPunchLogs()
    // 提示用户当前记录已经从待处理流转到已忽略。
    pushToast(t('punchToastIgnored'))
  }

  // 重处理当前详情记录，供映射修复后重新尝试自动匹配员工。
  const submitPunchReprocess = async () => {
    // 没有当前详情时不发请求，避免重处理动作失去目标。
    if (!state.punchDetail?.id) return
    // 调用后端重处理动作，让服务层重新跑去重和员工匹配逻辑。
    state.punchDetail = await reprocessPunchLog(state.punchDetail.id)
    // 重读列表后让摘要和当前记录状态同步刷新。
    await loadPunchLogs()
    // 提示用户当前记录已经重新处理完成。
    pushToast(t('punchToastReprocessed'))
  }

  // 返回第三阶段打卡区块全部动作，供工作台入口统一编排。
  return {
    loadPunchLogs,
    openPunchDetail,
    submitManualPunch,
    runPunchImportPreview,
    submitPunchImport,
    submitPunchBind,
    submitPunchIgnore,
    submitPunchReprocess
  }
}
