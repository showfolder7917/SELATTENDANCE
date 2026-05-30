// 这里封装第六阶段月次区块的列表、详情、重算、月结、反结和导出动作。

// 第六阶段区块依赖月次接口，并会和场所、部门筛选一起完成列表过滤。
import {
  exportMonthly,
  getMonthlyDetail,
  listMonthlyResults,
  recalculateMonthly,
  closeMonthly,
  reopenMonthly
} from '../../services'

// 创建第六阶段月次区块控制器，供工作台入口按需装配月结行为。
export const createMonthlySection = ({
  state,
  setSectionLoading,
  setSectionError,
  pushToast,
  t,
  refreshShell,
  downloadCsv
}) => {
  // 独立读取第六阶段列表，供月次汇总区块按当前筛选和分页局部刷新。
  const loadMonthlyResults = async () => {
    // 标记第六阶段区块进入加载态，支持月次页单独刷新不阻塞整页。
    setSectionLoading('monthly', true)
    // 清空第六阶段区块上一轮错误，保证新请求结果独立。
    setSectionError('monthly', '')
    try {
      // 按当前第六阶段筛选条件读取分页列表和顶部摘要。
      state.monthlyList = await listMonthlyResults({ ...state.monthlyFilters })
      // 如果当前页已经超过后端给出的总页数，立即回退到最后一页并重查。
      if (state.monthlyList.total > 0 && state.monthlyFilters.page > state.monthlyList.totalPages) {
        state.monthlyFilters.page = state.monthlyList.totalPages
        state.monthlyList = await listMonthlyResults({ ...state.monthlyFilters })
      }
      // 列表成功后标记第六阶段区块已完成首次加载。
      state.bootstrapShell.sectionStates.monthly = true
      // 当前详情不存在或已不在当前列表中时，默认选中第一条结果便于继续处理。
      const currentDetailId = state.monthlyDetail?.monthlyId
      const firstItem = state.monthlyList.items[0]
      if (!currentDetailId || !state.monthlyList.items.some((item) => item.monthlyId === currentDetailId)) {
        state.monthlyDetail = firstItem ? await getMonthlyDetail(firstItem.monthlyId) : null
      }
    } catch (error) {
      // 记录第六阶段区块错误，供局部错误展示和后续排障定位。
      setSectionError('monthly', error?.message || 'load monthly results failed')
      throw error
    } finally {
      // 无论成功失败都结束第六阶段区块加载态。
      setSectionLoading('monthly', false)
    }
  }

  // 读取单条月次详情，供右侧详情面板展示统计项、阻塞原因、动作日志和日次来源。
  const openMonthlyDetail = async (item) => {
    // 点击列表行时按主键读取详情，确保右侧始终拿到最新月次数据。
    state.monthlyDetail = await getMonthlyDetail(item.monthlyId)
  }

  // 对当前筛选范围执行月次重算，供用户整批刷新第六阶段结果。
  const submitMonthlyRecalculate = async () => {
    // 把当前筛选条件直接复用到月次重算请求，确保结果口径与列表一致。
    await recalculateMonthly({
      yearMonth: state.monthlyFilters.yearMonth,
      workplaceId: state.monthlyFilters.workplaceId || null,
      departmentId: state.monthlyFilters.departmentId || null,
      recalcMode: 'FULL',
      overwriteClosed: false,
      operatorId: Number(state.monthlyActionForm.operatorId || 9001)
    })
    // 月次重算完成后重读列表和当前详情，保证页面各块拿到同一轮结果。
    await loadMonthlyResults()
    if (state.monthlyDetail?.monthlyId) {
      state.monthlyDetail = await getMonthlyDetail(state.monthlyDetail.monthlyId)
    }
    // 刷新首页壳计数和推荐动作，让第六阶段推进情况同步回向导区块。
    await refreshShell()
    // 提示用户当前筛选范围已经完成月次重算。
    pushToast(t('monthlyToastRecalculated'))
  }

  // 对当前详情执行单人月次重算，供管理员只刷新选中员工当月结果。
  const submitMonthlyRecalculateOne = async () => {
    // 没有当前详情时不发请求，避免无目标重算。
    if (!state.monthlyDetail?.employeeId || !state.monthlyDetail?.yearMonth) return
    await recalculateMonthly({
      yearMonth: state.monthlyDetail.yearMonth,
      employeeId: Number(state.monthlyDetail.employeeId),
      recalcMode: 'ONE',
      overwriteClosed: false,
      operatorId: Number(state.monthlyActionForm.operatorId || 9001)
    })
    // 单人重算后重读列表和详情，保证左侧状态和右侧阻塞说明立即同步。
    await loadMonthlyResults()
    state.monthlyDetail = await getMonthlyDetail(state.monthlyDetail.monthlyId)
    // 刷新首页壳计数和推荐动作，让第六阶段进度同步回壳层。
    await refreshShell()
    // 用统一 toast 告知用户当前员工月次已经重算完成。
    pushToast(t('monthlyToastOneRecalculated'))
  }

  // 对当前筛选范围执行月结确认，供管理员把已闭环月份正式锁成已结。
  const submitMonthlyClose = async () => {
    await closeMonthly({
      yearMonth: state.monthlyFilters.yearMonth,
      scopeType: 'COMPANY',
      operatorId: Number(state.monthlyActionForm.operatorId || 9001),
      comment: state.monthlyActionForm.comment
    })
    await loadMonthlyResults()
    if (state.monthlyDetail?.monthlyId) {
      state.monthlyDetail = await getMonthlyDetail(state.monthlyDetail.monthlyId)
    }
    await refreshShell()
    pushToast(t('monthlyToastClosed'))
  }

  // 对当前选中月次执行反结，供管理员重新开放当前员工该月结果。
  const submitMonthlyReopen = async () => {
    if (!state.monthlyDetail?.monthlyId) return
    await reopenMonthly({
      monthlyId: Number(state.monthlyDetail.monthlyId),
      operatorId: Number(state.monthlyActionForm.operatorId || 9001),
      reason: state.monthlyActionForm.reopenReason
    })
    await loadMonthlyResults()
    state.monthlyDetail = await getMonthlyDetail(state.monthlyDetail.monthlyId)
    await refreshShell()
    pushToast(t('monthlyToastReopened'))
  }

  // 导出当前筛选范围的月次 CSV，供管理员直接拿去对账和留档。
  const submitMonthlyExport = async () => {
    const payload = await exportMonthly({
      yearMonth: state.monthlyFilters.yearMonth,
      workplaceId: state.monthlyFilters.workplaceId || null,
      departmentId: state.monthlyFilters.departmentId || null,
      employeeKeyword: state.monthlyFilters.employeeKeyword,
      closeStatus: state.monthlyFilters.closeStatus,
      blockedOnly: state.monthlyFilters.blockedOnly
    })
    // 导出结果继续复用统一下载能力，避免第六阶段单独维护下载逻辑。
    downloadCsv(payload)
    pushToast(t('monthlyToastExported'))
  }

  // 返回第六阶段区块全部动作，供工作台入口统一编排。
  return {
    loadMonthlyResults,
    openMonthlyDetail,
    submitMonthlyRecalculate,
    submitMonthlyRecalculateOne,
    submitMonthlyClose,
    submitMonthlyReopen,
    submitMonthlyExport
  }
}
