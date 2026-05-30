// 这里封装第五阶段异常处理与审批区块的列表、详情、建单和审批动作。

// 第五阶段区块依赖处理单接口和日次锁定接口，并会和场所、部门筛选一起完成列表过滤。
import {
  applyCaseAction,
  createCase,
  getCaseDetail,
  listCases
} from '../../services'
import { lockDaily, unlockDaily } from '../../services/dailyApi'

// 创建第五阶段区块控制器，供工作台入口按需装配审批行为。
export const createCaseSection = ({
  state,
  setSectionLoading,
  setSectionError,
  pushToast,
  t,
  refreshShell
}) => {
  // 独立读取第五阶段列表，供异常处理 / 审批区块按当前筛选和分页局部刷新。
  const loadCases = async () => {
    // 标记第五阶段区块进入加载态，支持审批页单独刷新不阻塞整页。
    setSectionLoading('case', true)
    // 清空第五阶段区块上一轮错误，保证新请求结果独立。
    setSectionError('case', '')
    try {
      // 按当前第五阶段筛选条件读取分页列表和顶部摘要。
      state.caseList = await listCases({ ...state.caseFilters })
      // 如果当前页已经超过后端给出的总页数，立即回退到最后一页并重查。
      if (state.caseList.total > 0 && state.caseFilters.page > state.caseList.totalPages) {
        state.caseFilters.page = state.caseList.totalPages
        state.caseList = await listCases({ ...state.caseFilters })
      }
      // 列表成功后标记第五阶段区块已完成首次加载。
      state.bootstrapShell.sectionStates.case = true
      const currentFocusId = state.caseFocusItem?.caseId || state.caseFocusItem?.attendanceDailyId
      const firstItem = state.caseList.items[0] || null
      const nextFocusItem =
        state.caseList.items.find((item) => (item.caseId || item.attendanceDailyId) === currentFocusId) || firstItem
      state.caseFocusItem = nextFocusItem
      // 当前聚焦项是真实处理单时读取详情，否则清空详情并停留在建单视图。
      if (nextFocusItem?.caseId) {
        state.caseDetail = await getCaseDetail(nextFocusItem.caseId)
      } else {
        state.caseDetail = null
      }
    } catch (error) {
      // 记录第五阶段区块错误，供局部错误展示和后续排障定位。
      setSectionError('case', error?.message || 'load attendance cases failed')
      throw error
    } finally {
      // 无论成功失败都结束第五阶段区块加载态。
      setSectionLoading('case', false)
    }
  }

  // 点击列表行时按行类型切换右侧详情，未建单异常保持在建单表单，已建单记录读取真实详情。
  const openCaseDetail = async (item) => {
    state.caseFocusItem = item
    if (item?.caseId) {
      state.caseDetail = await getCaseDetail(item.caseId)
      // 打开真实处理单时，把默认审批表单同步到当前详情，减少审批人重复填写。
      state.caseActionForm.finalStatus = state.caseDetail.finalStatus || state.caseDetail.dailyDetail?.status || 'NORMAL'
      state.caseActionForm.finalClockIn = state.caseDetail.finalClockIn ? state.caseDetail.finalClockIn.slice(0, 16) : ''
      state.caseActionForm.finalClockOut = state.caseDetail.finalClockOut ? state.caseDetail.finalClockOut.slice(0, 16) : ''
      state.caseActionForm.finalBreakMinutes = String(state.caseDetail.finalBreakMinutes ?? '')
      state.caseActionForm.comment = ''
      state.caseActionForm.finalExceptionFlag = false
      return
    }
    state.caseDetail = null
  }

  // 对当前未建单异常创建真实处理单，并在成功后直接切入审批详情。
  const submitCaseCreate = async () => {
    if (!state.caseFocusItem?.attendanceDailyId) return
    const payload = {
      attendanceDailyId: Number(state.caseFocusItem.attendanceDailyId),
      caseType: state.caseFocusItem.caseType || state.caseFocusItem.currentException,
      applicantId: Number(state.caseCreateForm.applicantId || 9001),
      applicantRole: state.caseCreateForm.applicantRole || 'MANAGER',
      reasonCategory: state.caseCreateForm.reasonCategory,
      reasonText: state.caseCreateForm.reasonText,
      expectedResolution: state.caseCreateForm.expectedResolution
    }
    const result = await createCase(payload)
    await loadCases()
    if (result.caseId) {
      const targetItem = state.caseList.items.find((item) => item.caseId === result.caseId)
      if (targetItem) {
        await openCaseDetail(targetItem)
      }
    }
    await refreshShell()
    pushToast(t('caseToastCreated'))
  }

  // 对当前处理单执行审批动作，并在成功后刷新列表和右侧详情状态。
  const submitCaseAction = async (actionType) => {
    if (!state.caseDetail?.caseId) return
    const patchPayload = actionType === 'APPROVE'
      ? {
          finalStatus: state.caseActionForm.finalStatus,
          actualClockIn: state.caseActionForm.finalClockIn || undefined,
          actualClockOut: state.caseActionForm.finalClockOut || undefined,
          finalBreakMinutes: state.caseActionForm.finalBreakMinutes === '' ? undefined : Number(state.caseActionForm.finalBreakMinutes),
          finalExceptionFlag: Boolean(state.caseActionForm.finalExceptionFlag)
        }
      : {}
    await applyCaseAction(state.caseDetail.caseId, {
      actionType,
      approverId: 9001,
      comment: state.caseActionForm.comment,
      patchPayload
    })
    await loadCases()
    if (state.caseDetail?.caseId) {
      const targetItem = state.caseList.items.find((item) => item.caseId === state.caseDetail.caseId)
      if (targetItem) {
        await openCaseDetail(targetItem)
      }
    }
    await refreshShell()
    pushToast(
      actionType === 'APPROVE'
        ? t('caseToastApproved')
        : actionType === 'RETURN'
          ? t('caseToastReturned')
          : t('caseToastRejected')
    )
  }

  // 对当前详情执行锁定动作，让审批通过后的最终结果进入不可随意改动状态。
  const submitCaseLock = async () => {
    if (!state.caseDetail?.attendanceDailyId) return
    await lockDaily(state.caseDetail.attendanceDailyId)
    await loadCases()
    await refreshShell()
    pushToast(t('caseToastLocked'))
  }

  // 对当前详情执行解锁动作，让管理员在必要时重新开放少量修正窗口。
  const submitCaseUnlock = async () => {
    if (!state.caseDetail?.attendanceDailyId) return
    await unlockDaily(state.caseDetail.attendanceDailyId)
    await loadCases()
    await refreshShell()
    pushToast(t('caseToastUnlocked'))
  }

  // 返回第五阶段区块全部动作，供工作台入口统一编排。
  return {
    loadCases,
    openCaseDetail,
    submitCaseCreate,
    submitCaseAction,
    submitCaseLock,
    submitCaseUnlock
  }
}
