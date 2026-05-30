// 这里是 workbench 目录化后的统一入口，负责把首页壳和各 section 模块组装回页面原有调用面。

// Vue 组合式 API 用于承接工作台状态、生命周期和区块切换联动。
import { computed, onMounted, reactive, ref, watch } from 'vue'
// 多语言消息和语言选项继续沿用原常量定义，避免视图层改动。
import { localeOptions, messages } from '../../constants/i18nMessages'
// 轻量首页壳接口用于初始化租户摘要、步骤状态和推荐动作。
import { fetchBootstrap, fetchCurrentTenant, saveTenant } from '../../services'
// 共享状态工厂用于创建目录化后的统一工作台状态。
import { createWorkbenchState } from './state'
// 共享辅助函数用于 toast、CSV 下载和壳层计数同步。
import { downloadCsv, pushToast, syncFilterReferences, syncFormDefaults, syncShellCounters } from './helpers'
// 场所区块模块负责场所列表和场所表单动作。
import { createWorkplaceSection } from './workplaceSection'
// 部门区块模块负责部门列表、部门表单和场所到部门的跳转。
import { createDepartmentSection } from './departmentSection'
// 员工区块模块负责员工列表、映射、导入导出和部门到员工的跳转。
import { createEmployeeSection } from './employeeSection'
// 班次区块模块负责模板列表、模板表单和推荐模板生成。
import { createShiftSection } from './shiftSection'
// 排班区块模块负责看板加载、单条排班和批量排班流程。
import { createScheduleSection } from './scheduleSection'
// 打卡区块模块负责第三阶段原始打卡记录、补录和导入处理。
import { createPunchSection } from './punchSection'
// 日次区块模块负责第四阶段日次结果、详情和重算处理。
import { createDailySection } from './dailySection'
// 异常处理区块模块负责第五阶段处理单、审批和锁定动作。
import { createCaseSection } from './caseSection'
// 月次汇总区块模块负责第六阶段月汇总、月结、反结和导出动作。
import { createMonthlySection } from './monthlySection'

// 统一暴露工作台 composable，保持页面视图仍从一个入口拿状态和动作。
export function useAttendanceWorkbench() {
  // 先从 URL 中读取初始 section，支持直接带 section 参数进入某个区块。
  const initialSection = new URLSearchParams(window.location.search).get('section')
  // 限定可用区块集合，避免 URL 传入未知值污染工作台状态。
  const allowedSections = ['wizard', 'workplace', 'department', 'employee', 'shift', 'schedule', 'punch', 'daily', 'case', 'monthly']
  // 从 URL 中读取当前语言，没有时回退到中文环境。
  const locale = ref(new URLSearchParams(window.location.search).get('locale') || 'zh-CN')
  // 用 section 参数或默认向导区块作为工作台当前激活区块。
  const activeSection = ref(allowedSections.includes(initialSection) ? initialSection : 'wizard')
  // 用于承接首页壳和各 section 共享的业务主状态。
  const state = reactive(createWorkbenchState())
  // 记录首页壳自身加载状态，和各 section 局部加载区分开。
  const shellLoading = ref(false)
  // 统一 toast 引用，供所有区块复用当前页面的消息提示层。
  const toast = ref('')
  // 页面级确认弹窗状态放在工作台入口，供删除、复制、覆盖等动作共享同一套确认能力。
  const confirmDialog = reactive({
    open: false,
    title: '',
    message: '',
    confirmLabel: '',
    confirmVariant: 'primary',
    resolver: null
  })

  // 基于当前语言环境翻译文案 key，保持页面原有 `t()` 调用方式不变。
  const t = (key) => messages[locale.value][key] || key

  // 用壳层加载态和各 section 加载态汇总出页面级 loading，兼容现有视图写法。
  const loading = computed(
    () => shellLoading.value || Object.values(state.sectionLoaders).some(Boolean)
  )

  // 维持原有导航项结构，供侧边栏导航直接复用。
  const navItems = computed(() => [
    { key: 'wizard', label: t('navWizard') },
    { key: 'workplace', label: t('navWorkplace') },
    { key: 'department', label: t('navDepartment') },
    { key: 'employee', label: t('navEmployee') },
    { key: 'shift', label: t('navShift') },
    { key: 'schedule', label: t('navSchedule') },
    { key: 'punch', label: t('navPunch') },
    { key: 'daily', label: t('navDaily') },
    { key: 'case', label: t('navCase') },
    { key: 'monthly', label: t('navMonthly') }
  ])

  // 继续提供推荐下一动作文案，优先使用轻量壳给出的推荐动作。
  const recommendedNextLabel = computed(
    () => t(state.recommendedNextAction || state.bootstrapShell.recommendedNextAction || 'wizard.schedule')
  )

  // 部门列表继续在前端按场所筛选，保持现有部门区块交互方式不变。
  const filteredDepartments = computed(() =>
    state.departments.filter((item) => {
      if (!state.departmentFilters.workplaceId) return true
      return String(item.workplaceId) === String(state.departmentFilters.workplaceId)
    })
  )

  // 员工列表继续在前端按关键字和筛选项过滤，兼容现有员工区块交互。
  const filteredEmployees = computed(() =>
    state.employees.filter((item) => {
      const matchKeyword =
        !state.employeeFilters.keyword ||
        item.employeeNo.includes(state.employeeFilters.keyword) ||
        item.employeeName.includes(state.employeeFilters.keyword)
      const matchDepartment =
        !state.employeeFilters.departmentId ||
        String(item.departmentId) === String(state.employeeFilters.departmentId)
      const matchEmployment =
        !state.employeeFilters.employmentType ||
        item.employmentType === state.employeeFilters.employmentType
      const matchStatus =
        !state.employeeFilters.status ||
        item.status === state.employeeFilters.status
      return matchKeyword && matchDepartment && matchEmployment && matchStatus
    })
  )

  // 给部门区块补充当前筛选场所名称，保持现有表头文案不变。
  const currentDepartmentWorkplaceName = computed(() => {
    if (!state.departmentFilters.workplaceId) {
      return ''
    }
    const workplace = state.workplaces.find(
      (item) => String(item.id) === String(state.departmentFilters.workplaceId)
    )
    return workplace?.workplaceName || ''
  })

  // 统一写入 section 级加载状态，供各模块按区块维护独立 loading。
  const setSectionLoading = (sectionKey, nextValue) => {
    state.sectionLoaders[sectionKey] = nextValue
  }

  // 统一写入 section 级错误状态，供各模块按区块维护独立错误。
  const setSectionError = (sectionKey, nextValue) => {
    state.sectionErrors[sectionKey] = nextValue
  }

  // 用共享 toast 工具包装消息提示，保持所有区块反馈方式一致。
  const showToast = (message) => pushToast(toast, message)

  // 每次关闭确认弹窗都清空旧文案和旧回调，避免下一次动作误复用上一轮确认状态。
  const resetConfirmDialog = () => {
    confirmDialog.open = false
    confirmDialog.title = ''
    confirmDialog.message = ''
    confirmDialog.confirmLabel = ''
    confirmDialog.confirmVariant = 'primary'
    confirmDialog.resolver = null
  }

  // 给各区块提供统一确认入口，让业务动作只描述确认内容，不再直接依赖浏览器原生 confirm。
  const requestConfirm = ({ title, message, confirmLabel, confirmVariant = 'primary' }) =>
    new Promise((resolve) => {
      confirmDialog.open = true
      confirmDialog.title = title
      confirmDialog.message = message
      confirmDialog.confirmLabel = confirmLabel
      confirmDialog.confirmVariant = confirmVariant
      // 把当前确认结果回传给调用方，供删除、复制、覆盖动作决定是否继续执行。
      confirmDialog.resolver = resolve
    })

  // 用户取消时统一回写 false，再清空弹窗状态，保证调用方能稳定终止后续动作。
  const cancelConfirmDialog = () => {
    const resolver = confirmDialog.resolver
    resetConfirmDialog()
    if (resolver) {
      resolver(false)
    }
  }

  // 用户确认时统一回写 true，再清空弹窗状态，保证调用方在弹窗关闭后继续真正写操作。
  const submitConfirmDialog = () => {
    const resolver = confirmDialog.resolver
    resetConfirmDialog()
    if (resolver) {
      resolver(true)
    }
  }

  // 在轻量壳或各 section 刷新后统一修复默认表单和筛选引用。
  const syncDerivedState = () => {
    syncFormDefaults(state)
    syncFilterReferences(state)
    syncShellCounters(state)
  }

  // 读取轻量首页壳数据，只更新租户摘要、步骤状态和推荐动作。
  const loadBootstrap = async () => {
    shellLoading.value = true
    try {
      const payload = await fetchBootstrap()
      state.steps = payload.steps || []
      state.recommendedNextAction = payload.recommendedNextAction || ''
      Object.assign(state.tenant, payload.tenant || createWorkbenchState().tenant)
      state.bootstrapShell.tenantSummary = { ...state.tenant }
      state.bootstrapShell.recommendedNextAction =
        payload.recommendedNextAction || state.bootstrapShell.recommendedNextAction
      // 当轻量壳未返回租户资料时，使用独立租户接口补齐当前租户摘要。
      if (!payload.tenant?.tenantCode && !payload.tenant?.tenantName) {
        const currentTenant = await fetchCurrentTenant()
        if (currentTenant) {
          Object.assign(state.tenant, currentTenant)
          state.bootstrapShell.tenantSummary = { ...state.tenant }
        }
      }
      syncDerivedState()
    } finally {
      shellLoading.value = false
    }
  }

  // 供各 section 在主数据变化后刷新轻量壳步骤和推荐动作。
  const refreshShell = async () => {
    await loadBootstrap()
  }

  // 组装场所区块动作，供工作台入口和视图层复用。
  const workplaceSection = createWorkplaceSection({
    state,
    setSectionLoading,
    setSectionError,
    pushToast: showToast,
    t,
    refreshShell
  })

  // 组装部门区块动作，供工作台入口和跨区块跳转复用。
  const departmentSection = createDepartmentSection({
    state,
    activeSection,
    setSectionLoading,
    setSectionError,
    pushToast: showToast,
    t,
    refreshShell
  })

  // 组装员工区块动作，供工作台入口和部门联动复用。
  const employeeSection = createEmployeeSection({
    state,
    activeSection,
    setSectionLoading,
    setSectionError,
    pushToast: showToast,
    t,
    refreshShell,
    downloadCsv
  })

  // 组装班次模板区块动作，供工作台入口和排班流程复用。
  const shiftSection = createShiftSection({
    state,
    setSectionLoading,
    setSectionError,
    pushToast: showToast,
    t,
    refreshShell
  })

  // 组装排班区块动作，供工作台入口和部门联动复用。
  const scheduleSection = createScheduleSection({
    state,
    activeSection,
    setSectionLoading,
    setSectionError,
    pushToast: showToast,
    t,
    downloadCsv,
    requestConfirm
  })

  // 组装第三阶段打卡区块动作，供工作台入口和打卡记录页面复用。
  const punchSection = createPunchSection({
    state,
    setSectionLoading,
    setSectionError,
    pushToast: showToast,
    t,
    refreshShell
  })

  // 组装第四阶段日次区块动作，供工作台入口和日次结果页面复用。
  const dailySection = createDailySection({
    state,
    setSectionLoading,
    setSectionError,
    pushToast: showToast,
    t,
    refreshShell
  })

  // 组装第五阶段异常处理区块动作，供工作台入口和审批页面复用。
  const caseSection = createCaseSection({
    state,
    setSectionLoading,
    setSectionError,
    pushToast: showToast,
    t,
    refreshShell
  })

  // 组装第六阶段月次汇总区块动作，供工作台入口和月结页面复用。
  const monthlySection = createMonthlySection({
    state,
    setSectionLoading,
    setSectionError,
    pushToast: showToast,
    t,
    refreshShell,
    downloadCsv
  })

  // 保障场所主数据已加载，供部门、员工和排班等依赖场所的区块复用。
  const ensureWorkplacesLoaded = async (force = false) => {
    if (!force && state.bootstrapShell.sectionStates.workplace) return
    await workplaceSection.loadWorkplaces()
    syncDerivedState()
  }

  // 保障部门主数据已加载，供员工和排班等依赖部门的区块复用。
  const ensureDepartmentsLoaded = async (force = false) => {
    if (!force && state.bootstrapShell.sectionStates.department) return
    await departmentSection.loadDepartments()
    syncDerivedState()
  }

  // 保障员工主数据已加载，供员工区块和部门跳转员工视图使用。
  const ensureEmployeesLoaded = async (force = false) => {
    if (!force && state.bootstrapShell.sectionStates.employee) return
    await employeeSection.loadEmployees()
    syncDerivedState()
  }

  // 保障班次模板已加载，供班次区块和排班模板选择使用。
  const ensureShiftTemplatesLoaded = async (force = false) => {
    if (!force && state.bootstrapShell.sectionStates.shift) return
    await shiftSection.loadShiftTemplates()
    syncDerivedState()
  }

  // 保障排班看板已加载，供排班区块展示当前月份数据。
  const ensureScheduleLoaded = async (force = false) => {
    if (!force && state.bootstrapShell.sectionStates.schedule) return
    await scheduleSection.loadScheduleBoard()
    syncDerivedState()
  }

  // 保障第三阶段打卡列表已加载，供打卡区块展示原始打卡事实。
  const ensurePunchLoaded = async (force = false) => {
    if (!force && state.bootstrapShell.sectionStates.punch) return
    await punchSection.loadPunchLogs()
    syncDerivedState()
  }

  // 保障第四阶段日次结果已加载，供日次区块展示分页结果与详情。
  const ensureDailyLoaded = async (force = false) => {
    if (!force && state.bootstrapShell.sectionStates.daily) return
    await dailySection.loadDailyResults()
    syncDerivedState()
  }

  // 保障第五阶段异常处理列表已加载，供异常处理区块展示处理单和审批状态。
  const ensureCasesLoaded = async (force = false) => {
    if (!force && state.bootstrapShell.sectionStates.case) return
    await caseSection.loadCases()
    syncDerivedState()
  }

  // 保障第六阶段月次结果已加载，供月次区块展示分页结果与详情。
  const ensureMonthlyLoaded = async (force = false) => {
    if (!force && state.bootstrapShell.sectionStates.monthly) return
    await monthlySection.loadMonthlyResults()
    syncDerivedState()
  }

  // 根据当前激活区块按需加载所需数据，形成轻量壳 + section 独立加载模式。
  const ensureActiveSectionLoaded = async (sectionKey, force = false) => {
    if (sectionKey === 'wizard') {
      syncDerivedState()
      return
    }
    if (sectionKey === 'workplace') {
      await ensureWorkplacesLoaded(force)
      return
    }
    if (sectionKey === 'department') {
      await ensureWorkplacesLoaded(force)
      await ensureDepartmentsLoaded(force)
      return
    }
    if (sectionKey === 'employee') {
      await ensureWorkplacesLoaded(force)
      await ensureDepartmentsLoaded(force)
      await ensureEmployeesLoaded(force)
      return
    }
    if (sectionKey === 'shift') {
      await ensureShiftTemplatesLoaded(force)
      return
    }
    if (sectionKey === 'schedule') {
      await ensureWorkplacesLoaded(force)
      await ensureDepartmentsLoaded(force)
      await ensureShiftTemplatesLoaded(force)
      await ensureScheduleLoaded(force)
      return
    }
    if (sectionKey === 'punch') {
      await ensureEmployeesLoaded(force)
      await ensurePunchLoaded(force)
      return
    }
    if (sectionKey === 'daily') {
      await ensureWorkplacesLoaded(force)
      await ensureDepartmentsLoaded(force)
      await ensureDailyLoaded(force)
      return
    }
    if (sectionKey === 'case') {
      await ensureWorkplacesLoaded(force)
      await ensureDepartmentsLoaded(force)
      await ensureCasesLoaded(force)
      return
    }
    if (sectionKey === 'monthly') {
      await ensureWorkplacesLoaded(force)
      await ensureDepartmentsLoaded(force)
      await ensureMonthlyLoaded(force)
    }
  }

  // 保存当前租户资料，供首页租户面板回写基础主数据。
  const submitTenant = async () => {
    await saveTenant({ ...state.tenant })
    Object.assign(state.tenant, await fetchCurrentTenant())
    state.bootstrapShell.tenantSummary = { ...state.tenant }
    await refreshShell()
    showToast(t('toastSaved'))
  }

  // 页面首次挂载时先读取轻量首页壳，再按初始 section 按需加载对应区块数据。
  onMounted(async () => {
    await loadBootstrap()
    await ensureActiveSectionLoaded(activeSection.value)
  })

  // 当前区块切换时按需懒加载对应 section，避免继续走整页统一重聚合。
  watch(activeSection, async (nextSection) => {
    await ensureActiveSectionLoaded(nextSection)
  })

  // 员工筛选变化且当前位于员工区块时，重新拉取员工列表以支持独立 API 拼装。
  watch(
    () => ({ ...state.employeeFilters }),
    async () => {
      if (activeSection.value !== 'employee') return
      await employeeSection.loadEmployees()
      syncDerivedState()
    },
    { deep: true }
  )

  // 打卡筛选条件变化时先回到第 1 页，再按新的数据库条件重查列表。
  watch(
    () => ({
      dateFrom: state.punchFilters.dateFrom,
      dateTo: state.punchFilters.dateTo,
      employeeKeyword: state.punchFilters.employeeKeyword,
      sourceSystem: state.punchFilters.sourceSystem,
      processStatus: state.punchFilters.processStatus,
      punchType: state.punchFilters.punchType
    }),
    async () => {
      if (activeSection.value !== 'punch') return
      // 非第 1 页时先回到首页，后续由分页监听接手真实重查，避免同一轮筛选触发两次请求。
      if (state.punchFilters.page !== 1) {
        state.punchFilters.page = 1
        return
      }
      await punchSection.loadPunchLogs()
      syncDerivedState()
    },
    { deep: true }
  )

  // 打卡页码或每页条数变化时只重查当前分页，不再重置其他筛选条件。
  watch(
    () => ({
      page: state.punchFilters.page,
      pageSize: state.punchFilters.pageSize
    }),
    async () => {
      if (activeSection.value !== 'punch') return
      await punchSection.loadPunchLogs()
      syncDerivedState()
    },
    { deep: true }
  )

  // 日次筛选条件变化时先回到第 1 页，再按新的数据库条件重查列表。
  watch(
    () => ({
      startDate: state.dailyFilters.startDate,
      endDate: state.dailyFilters.endDate,
      workplaceId: state.dailyFilters.workplaceId,
      departmentId: state.dailyFilters.departmentId,
      employeeKeyword: state.dailyFilters.employeeKeyword,
      status: state.dailyFilters.status,
      exceptionOnly: state.dailyFilters.exceptionOnly
    }),
    async () => {
      if (activeSection.value !== 'daily') return
      // 非第 1 页时先回到首页，后续由分页监听接手真实重查，避免筛选触发两次请求。
      if (state.dailyFilters.page !== 1) {
        state.dailyFilters.page = 1
        return
      }
      await dailySection.loadDailyResults()
      syncDerivedState()
    },
    { deep: true }
  )

  // 日次页码或每页条数变化时只重查当前分页，不再重置其他筛选条件。
  watch(
    () => ({
      page: state.dailyFilters.page,
      pageSize: state.dailyFilters.pageSize
    }),
    async () => {
      if (activeSection.value !== 'daily') return
      await dailySection.loadDailyResults()
      syncDerivedState()
    },
    { deep: true }
  )

  // 第五阶段筛选条件变化时先回到第 1 页，再按新的数据库条件重查处理单列表。
  watch(
    () => ({
      startDate: state.caseFilters.startDate,
      endDate: state.caseFilters.endDate,
      workplaceId: state.caseFilters.workplaceId,
      departmentId: state.caseFilters.departmentId,
      employeeKeyword: state.caseFilters.employeeKeyword,
      caseStatus: state.caseFilters.caseStatus,
      handlingStatus: state.caseFilters.handlingStatus,
      mineOnly: state.caseFilters.mineOnly
    }),
    async () => {
      if (activeSection.value !== 'case') return
      if (state.caseFilters.page !== 1) {
        state.caseFilters.page = 1
        return
      }
      await caseSection.loadCases()
      syncDerivedState()
    },
    { deep: true }
  )

  // 第五阶段页码或每页条数变化时只重查当前分页，不再重置其他筛选条件。
  watch(
    () => ({
      page: state.caseFilters.page,
      pageSize: state.caseFilters.pageSize
    }),
    async () => {
      if (activeSection.value !== 'case') return
      await caseSection.loadCases()
      syncDerivedState()
    },
    { deep: true }
  )

  // 第六阶段筛选条件变化时先回到第 1 页，再按新的数据库条件重查月次列表。
  watch(
    () => ({
      yearMonth: state.monthlyFilters.yearMonth,
      workplaceId: state.monthlyFilters.workplaceId,
      departmentId: state.monthlyFilters.departmentId,
      employeeKeyword: state.monthlyFilters.employeeKeyword,
      closeStatus: state.monthlyFilters.closeStatus,
      blockedOnly: state.monthlyFilters.blockedOnly
    }),
    async () => {
      if (activeSection.value !== 'monthly') return
      if (state.monthlyFilters.page !== 1) {
        state.monthlyFilters.page = 1
        return
      }
      await monthlySection.loadMonthlyResults()
      syncDerivedState()
    },
    { deep: true }
  )

  // 第六阶段页码或每页条数变化时只重查当前分页，不再重置其他筛选条件。
  watch(
    () => ({
      page: state.monthlyFilters.page,
      pageSize: state.monthlyFilters.pageSize
    }),
    async () => {
      if (activeSection.value !== 'monthly') return
      await monthlySection.loadMonthlyResults()
      syncDerivedState()
    },
    { deep: true }
  )

  // 返回与原页面兼容的状态和动作接口，保证视图层无需整体重写。
  return {
    locale,
    localeOptions,
    activeSection,
    loading,
    toast,
    confirmDialog,
    state,
    t,
    navItems,
    recommendedNextLabel,
    filteredDepartments,
    currentDepartmentWorkplaceName,
    filteredEmployees,
    showToast,
    loadBootstrap,
    submitTenant,
    submitWorkplace: workplaceSection.submitWorkplace,
    removeWorkplace: workplaceSection.removeWorkplace,
    submitDepartment: departmentSection.submitDepartment,
    removeDepartment: departmentSection.removeDepartment,
    submitEmployee: employeeSection.submitEmployee,
    removeEmployee: employeeSection.removeEmployee,
    submitMapping: employeeSection.submitMapping,
    submitImport: employeeSection.submitImport,
    handleExport: employeeSection.handleExport,
    requestConfirm,
    cancelConfirmDialog,
    submitConfirmDialog,
    loadScheduleBoard: scheduleSection.loadScheduleBoard,
    loadPunchLogs: punchSection.loadPunchLogs,
    loadDailyResults: dailySection.loadDailyResults,
    loadCases: caseSection.loadCases,
    loadMonthlyResults: monthlySection.loadMonthlyResults,
    selectScheduleTemplate: scheduleSection.selectScheduleTemplate,
    closeScheduleTemplateTip: scheduleSection.closeScheduleTemplateTip,
    applySchedule: scheduleSection.applySchedule,
    removeScheduleItem: scheduleSection.removeScheduleItem,
    openBatchWizard: scheduleSection.openBatchWizard,
    closeBatchWizard: scheduleSection.closeBatchWizard,
    nextBatchStep: scheduleSection.nextBatchStep,
    prevBatchStep: scheduleSection.prevBatchStep,
    confirmBatchWizard: scheduleSection.confirmBatchWizard,
    copySchedulesFromLastWeek: scheduleSection.copySchedulesFromLastWeek,
    copySchedulesFromLastMonth: scheduleSection.copySchedulesFromLastMonth,
    runUnassignedCheck: scheduleSection.runUnassignedCheck,
    handleScheduleExport: scheduleSection.handleScheduleExport,
    openPunchDetail: punchSection.openPunchDetail,
    submitManualPunch: punchSection.submitManualPunch,
    runPunchImportPreview: punchSection.runPunchImportPreview,
    submitPunchImport: punchSection.submitPunchImport,
    submitPunchBind: punchSection.submitPunchBind,
    submitPunchIgnore: punchSection.submitPunchIgnore,
    submitPunchReprocess: punchSection.submitPunchReprocess,
    openDailyDetail: dailySection.openDailyDetail,
    submitDailyRecalculate: dailySection.submitDailyRecalculate,
    submitDailyRangeRecalculate: dailySection.submitDailyRangeRecalculate,
    openCaseDetail: caseSection.openCaseDetail,
    submitCaseCreate: caseSection.submitCaseCreate,
    submitCaseAction: caseSection.submitCaseAction,
    submitCaseLock: caseSection.submitCaseLock,
    submitCaseUnlock: caseSection.submitCaseUnlock,
    openMonthlyDetail: monthlySection.openMonthlyDetail,
    submitMonthlyRecalculate: monthlySection.submitMonthlyRecalculate,
    submitMonthlyRecalculateOne: monthlySection.submitMonthlyRecalculateOne,
    submitMonthlyClose: monthlySection.submitMonthlyClose,
    submitMonthlyReopen: monthlySection.submitMonthlyReopen,
    submitMonthlyExport: monthlySection.submitMonthlyExport,
    submitShiftTemplate: shiftSection.submitShiftTemplate,
    generateRecommended: shiftSection.generateRecommended,
    removeShiftTemplate: shiftSection.removeShiftTemplate,
    editWorkplace: workplaceSection.editWorkplace,
    openWorkplaceDepartments: departmentSection.openWorkplaceDepartments,
    editDepartment: departmentSection.editDepartment,
    openDepartmentEmployees: employeeSection.openDepartmentEmployees,
    openDepartmentSchedule: scheduleSection.openDepartmentSchedule,
    editEmployee: employeeSection.editEmployee,
    editMapping: employeeSection.editMapping,
    editShiftTemplate: shiftSection.editShiftTemplate,
    resetWorkplaceForm: workplaceSection.resetWorkplaceForm,
    resetDepartmentForm: departmentSection.resetDepartmentForm,
    clearDepartmentWorkplaceFilter: departmentSection.clearDepartmentWorkplaceFilter,
    resetEmployeeForm: employeeSection.resetEmployeeForm,
    resetMappingForm: employeeSection.resetMappingForm,
    resetShiftForm: shiftSection.resetShiftForm
  }
}
