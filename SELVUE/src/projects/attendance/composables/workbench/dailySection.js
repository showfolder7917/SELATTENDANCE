// 这里封装第四阶段日次区块的列表、详情和重算动作。

// 第四阶段区块依赖日次接口，并会和场所、部门筛选一起完成列表过滤。
import {
  getDailyDetail,
  listDailyResults,
  recalculateDaily,
  recalculateDailyRange
} from '../../services'

// 创建第四阶段日次区块控制器，供工作台入口按需装配日次行为。
export const createDailySection = ({
  state,
  setSectionLoading,
  setSectionError,
  pushToast,
  t,
  refreshShell
}) => {
  // 独立读取日次列表，供第四阶段区块按当前筛选和分页局部刷新。
  const loadDailyResults = async () => {
    // 标记第四阶段区块进入加载态，支持日次单独刷新不阻塞整页。
    setSectionLoading('daily', true)
    // 清空第四阶段区块上一轮错误，保证新请求结果独立。
    setSectionError('daily', '')
    try {
      // 按当前第四阶段筛选条件读取分页列表和顶部摘要。
      state.dailyList = await listDailyResults({ ...state.dailyFilters })
      // 如果当前页已经超过后端给出的总页数，立即回退到最后一页并重查。
      if (
        state.dailyList.total > 0 &&
        state.dailyFilters.page > state.dailyList.totalPages
      ) {
        state.dailyFilters.page = state.dailyList.totalPages
        state.dailyList = await listDailyResults({ ...state.dailyFilters })
      }
      // 列表成功后标记第四阶段区块已完成首次加载。
      state.bootstrapShell.sectionStates.daily = true
      // 当前详情不存在或已不在当前列表中时，默认选中第一条结果便于继续处理。
      const currentDetailId = state.dailyDetail?.id
      const firstItem = state.dailyList.items[0]
      if (!currentDetailId || !state.dailyList.items.some((item) => item.id === currentDetailId)) {
        state.dailyDetail = firstItem ? await getDailyDetail(firstItem.id) : null
      }
    } catch (error) {
      // 记录第四阶段区块错误，供局部错误展示和后续排障定位。
      setSectionError('daily', error?.message || 'load daily results failed')
      // 把错误继续抛给入口层，由入口层决定是否终止联动。
      throw error
    } finally {
      // 无论成功失败都结束第四阶段区块加载态。
      setSectionLoading('daily', false)
    }
  }

  // 读取单条日次详情，供右侧详情面板展示排班、打卡、异常和计算过程。
  const openDailyDetail = async (item) => {
    // 点击列表行时按主键读取详情，确保右侧始终拿到最新重算结果。
    state.dailyDetail = await getDailyDetail(item.id)
  }

  // 对当前选中日次执行单日重算，供异常处理后立即刷新当天结论。
  const submitDailyRecalculate = async () => {
    // 没有当前详情时不发请求，避免无目标重算。
    if (!state.dailyDetail?.employeeId || !state.dailyDetail?.workDate) return
    // 把当前选中员工和工作日提交给后端，刷新单日结果。
    const result = await recalculateDaily({
      employeeId: Number(state.dailyDetail.employeeId),
      workDate: state.dailyDetail.workDate
    })
    // 单日重算后重读列表，保证左侧状态和顶部摘要立即同步。
    await loadDailyResults()
    // 优先使用后端回传的最新详情，避免再次多发一次详情请求。
    state.dailyDetail = result.detail || state.dailyDetail
    // 刷新首页壳计数和推荐动作，让第四阶段进度同步回壳层。
    await refreshShell()
    // 用统一 toast 告知用户当前日次已经重算完成。
    pushToast(t('dailyToastRecalculated'))
  }

  // 对当前筛选范围执行批量重算，供用户整批刷新阶段四结论。
  const submitDailyRangeRecalculate = async () => {
    // 把当前筛选条件直接复用到范围重算请求，确保结果口径与列表一致。
    await recalculateDailyRange({
      startDate: state.dailyFilters.startDate,
      endDate: state.dailyFilters.endDate,
      workplaceId: state.dailyFilters.workplaceId || null,
      departmentId: state.dailyFilters.departmentId || null,
      employeeKeyword: state.dailyFilters.employeeKeyword,
      exceptionOnly: state.dailyFilters.exceptionOnly
    })
    // 范围重算完成后重读列表和当前详情，保证页面各块拿到同一轮结果。
    await loadDailyResults()
    if (state.dailyDetail?.id) {
      state.dailyDetail = await getDailyDetail(state.dailyDetail.id)
    }
    // 刷新首页壳计数和推荐动作，让第四阶段推进情况同步回向导区块。
    await refreshShell()
    // 提示用户当前筛选范围已经完成重算。
    pushToast(t('dailyToastRangeRecalculated'))
  }

  // 返回第四阶段区块全部动作，供工作台入口统一编排。
  return {
    loadDailyResults,
    openDailyDetail,
    submitDailyRecalculate,
    submitDailyRangeRecalculate
  }
}
