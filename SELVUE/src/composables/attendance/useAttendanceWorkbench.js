import { computed, onMounted, reactive, ref, watch } from 'vue'
import { localeOptions, messages } from '../../constants/i18nMessages'
import {
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  bindExternalMapping,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  createDepartment,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  createEmployee,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  createShiftTemplate,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  createSchedule,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  createWorkplace,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  deleteDepartment,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  deleteEmployee,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  deleteShiftTemplate,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  deleteWorkplace,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  exportEmployees,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  fetchBootstrap,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  fetchScheduleBoard,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  generateRecommendedShiftTemplates,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  importEmployees,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  saveTenant,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  batchAssignSchedules,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  copyLastMonthSchedules,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  copyLastWeekSchedules,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  checkUnassignedSchedules,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  deleteSchedule,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  exportSchedules,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  updateDepartment,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  updateEmployee,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  updateSchedule,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  updateShiftTemplate,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  updateWorkplace
// 执行当前业务步骤，推进本行对应的 composable 处理。
} from '../../services/attendance'

// 执行当前业务步骤，推进本行对应的 composable 处理。
export function useAttendanceWorkbench() {
  // section 参数允许工作台从外部链接直接落到指定业务模块，便于联调、截图和跨模块回访。
  // 声明 initialSection 状态，保存当前工作台交互过程中需要的前端数据。
  const initialSection = new URLSearchParams(window.location.search).get('section')
  // 只接受当前已知模块，避免非法参数把工作台带到不存在的内容区。
  // 声明 allowedSections 状态，保存当前工作台交互过程中需要的前端数据。
  const allowedSections = ['wizard', 'workplace', 'department', 'employee', 'shift', 'schedule']
  // 语言状态允许通过 URL 参数直接切到日文页，便于中日双语联调和截图验证。
  // 声明 语言 状态，保存当前工作台交互过程中需要的前端数据。
  const locale = ref(new URLSearchParams(window.location.search).get('locale') || 'zh-CN')
  // 当前页签决定工作台展示哪个业务子域。
  // 声明 启用标记Section 状态，保存当前工作台交互过程中需要的前端数据。
  const activeSection = ref(allowedSections.includes(initialSection) ? initialSection : 'wizard')
  // 加载状态用于阻止页面在首屏和刷新时展示过期数据。
  // 声明 loading 状态，保存当前工作台交互过程中需要的前端数据。
  const loading = ref(false)
  // toast 用于承接保存、删除、导入等轻量操作反馈。
  // 声明 toast 状态，保存当前工作台交互过程中需要的前端数据。
  const toast = ref('')

  // 工作台总状态按租户、主数据和表单草稿分层保存，避免组件之间互相抢状态。
  // 声明 state 状态，保存当前工作台交互过程中需要的前端数据。
  const state = reactive({
    // 维护 租户 字段，供当前前端状态或配置直接使用。
    tenant: {
      // 维护 租户编码 字段，供当前前端状态或配置直接使用。
      tenantCode: '',
      // 维护 租户名称 字段，供当前前端状态或配置直接使用。
      tenantName: '',
      // 维护 contact名称 字段，供当前前端状态或配置直接使用。
      contactName: '',
      // 维护 contact电话 字段，供当前前端状态或配置直接使用。
      contactPhone: '',
      // 维护 contact邮箱 字段，供当前前端状态或配置直接使用。
      contactEmail: '',
      // 维护 时区 字段，供当前前端状态或配置直接使用。
      timezone: 'Asia/Tokyo'
    },
    // 维护 steps 字段，供当前前端状态或配置直接使用。
    steps: [],
    // 维护 workplaces 字段，供当前前端状态或配置直接使用。
    workplaces: [],
    // 维护 departments 字段，供当前前端状态或配置直接使用。
    departments: [],
    // 维护 部门Filters 字段，供当前前端状态或配置直接使用。
    departmentFilters: {
      // 维护 事业所Id 字段，供当前前端状态或配置直接使用。
      workplaceId: ''
    },
    // 维护 employees 字段，供当前前端状态或配置直接使用。
    employees: [],
    // 维护 班次Templates 字段，供当前前端状态或配置直接使用。
    shiftTemplates: [],
    // 维护 排班Filters 字段，供当前前端状态或配置直接使用。
    scheduleFilters: {
      // 维护 month 字段，供当前前端状态或配置直接使用。
      month: new Date().toISOString().slice(0, 7),
      // 维护 事业所Id 字段，供当前前端状态或配置直接使用。
      workplaceId: '',
      // 维护 部门Id 字段，供当前前端状态或配置直接使用。
      departmentId: '',
      // 维护 员工关键字 字段，供当前前端状态或配置直接使用。
      employeeKeyword: '',
      // 维护 onlyUnassigned 字段，供当前前端状态或配置直接使用。
      onlyUnassigned: false
    },
    // 维护 排班看板 字段，供当前前端状态或配置直接使用。
    scheduleBoard: {
      // 维护 month 字段，供当前前端状态或配置直接使用。
      month: new Date().toISOString().slice(0, 7),
      // 维护 dates 字段，供当前前端状态或配置直接使用。
      dates: [],
      // 维护 employeeRows 字段，供当前前端状态或配置直接使用。
      employeeRows: [],
      // 维护 scheduleItems 字段，供当前前端状态或配置直接使用。
      scheduleItems: [],
      // 维护 班次Templates 字段，供当前前端状态或配置直接使用。
      shiftTemplates: []
    },
    // 维护 排班Form 字段，供当前前端状态或配置直接使用。
    scheduleForm: {
      // 维护 selectedTemplateId 字段，供当前前端状态或配置直接使用。
      selectedTemplateId: null,
      // 维护 remark 字段，供当前前端状态或配置直接使用。
      remark: '',
      // 维护 selectedEmployeeId 字段，供当前前端状态或配置直接使用。
      selectedEmployeeId: null,
      // 维护 selectedEmployeeName 字段，供当前前端状态或配置直接使用。
      selectedEmployeeName: '',
      // 维护 selectedWorkDate 字段，供当前前端状态或配置直接使用。
      selectedWorkDate: '',
      // 维护 selectedScheduleId 字段，供当前前端状态或配置直接使用。
      selectedScheduleId: null,
      // 维护 selectedTemplateName 字段，供当前前端状态或配置直接使用。
      selectedTemplateName: ''
    },
    // 维护 批量排班向导 字段，供当前前端状态或配置直接使用。
    batchWizard: {
      // 维护 open 字段，供当前前端状态或配置直接使用。
      open: false,
      // 维护 step 字段，供当前前端状态或配置直接使用。
      step: 1,
      // 维护 员工Ids 字段，供当前前端状态或配置直接使用。
      employeeIds: [],
      // 维护 startDate 字段，供当前前端状态或配置直接使用。
      startDate: '',
      // 维护 endDate 字段，供当前前端状态或配置直接使用。
      endDate: '',
      // 维护 班次模板Id 字段，供当前前端状态或配置直接使用。
      shiftTemplateId: null,
      // 维护 skipExisting 字段，供当前前端状态或配置直接使用。
      skipExisting: false,
      // 维护 overwriteExisting 字段，供当前前端状态或配置直接使用。
      overwriteExisting: true,
      // 维护 remark 字段，供当前前端状态或配置直接使用。
      remark: ''
    },
    // 维护 未排班结果 字段，供当前前端状态或配置直接使用。
    scheduleUnassignedItems: [],
    // 维护 推荐NextAction 字段，供当前前端状态或配置直接使用。
    recommendedNextAction: '',
    // 维护 员工Filters 字段，供当前前端状态或配置直接使用。
    employeeFilters: {
      // 维护 keyword 字段，供当前前端状态或配置直接使用。
      keyword: '',
      // 维护 部门Id 字段，供当前前端状态或配置直接使用。
      departmentId: '',
      // 维护 employmentType 字段，供当前前端状态或配置直接使用。
      employmentType: '',
      // 维护 状态 字段，供当前前端状态或配置直接使用。
      status: ''
    },
    // 维护 事业所Form 字段，供当前前端状态或配置直接使用。
    workplaceForm: {
      // 维护 id 字段，供当前前端状态或配置直接使用。
      id: null,
      // 维护 事业所编码 字段，供当前前端状态或配置直接使用。
      workplaceCode: '',
      // 维护 事业所名称 字段，供当前前端状态或配置直接使用。
      workplaceName: '',
      // 维护 地址 字段，供当前前端状态或配置直接使用。
      address: '',
      // 维护 电话 字段，供当前前端状态或配置直接使用。
      phone: '',
      // 维护 状态 字段，供当前前端状态或配置直接使用。
      status: 'ACTIVE'
    },
    // 维护 部门Form 字段，供当前前端状态或配置直接使用。
    departmentForm: {
      // 维护 id 字段，供当前前端状态或配置直接使用。
      id: null,
      // 维护 事业所Id 字段，供当前前端状态或配置直接使用。
      workplaceId: '',
      // 维护 部门编码 字段，供当前前端状态或配置直接使用。
      departmentCode: '',
      // 维护 部门名称 字段，供当前前端状态或配置直接使用。
      departmentName: '',
      // 维护 sortOrder 字段，供当前前端状态或配置直接使用。
      sortOrder: 0,
      // 维护 状态 字段，供当前前端状态或配置直接使用。
      status: 'ACTIVE'
    },
    // 维护 员工Form 字段，供当前前端状态或配置直接使用。
    employeeForm: {
      // 维护 id 字段，供当前前端状态或配置直接使用。
      id: null,
      // 维护 员工No 字段，供当前前端状态或配置直接使用。
      employeeNo: '',
      // 维护 员工名称 字段，供当前前端状态或配置直接使用。
      employeeName: '',
      // 维护 员工名称Kana 字段，供当前前端状态或配置直接使用。
      employeeNameKana: '',
      // 维护 employmentType 字段，供当前前端状态或配置直接使用。
      employmentType: 'FULL_TIME',
      // 维护 入社Date 字段，供当前前端状态或配置直接使用。
      hireDate: '',
      // 维护 事业所Id 字段，供当前前端状态或配置直接使用。
      workplaceId: '',
      // 维护 部门Id 字段，供当前前端状态或配置直接使用。
      departmentId: '',
      // 维护 状态 字段，供当前前端状态或配置直接使用。
      status: 'ACTIVE'
    },
    // 维护 映射Form 字段，供当前前端状态或配置直接使用。
    mappingForm: {
      // 维护 员工Id 字段，供当前前端状态或配置直接使用。
      employeeId: null,
      // 维护 sourceSystem 字段，供当前前端状态或配置直接使用。
      sourceSystem: 'KING_OF_TIME',
      // 维护 外部系统员工Id 字段，供当前前端状态或配置直接使用。
      externalEmployeeId: '',
      // 维护 外部系统员工No 字段，供当前前端状态或配置直接使用。
      externalEmployeeNo: '',
      // 维护 状态 字段，供当前前端状态或配置直接使用。
      status: 'ACTIVE'
    },
    // 维护 班次Form 字段，供当前前端状态或配置直接使用。
    shiftForm: {
      // 维护 id 字段，供当前前端状态或配置直接使用。
      id: null,
      // 维护 模板编码 字段，供当前前端状态或配置直接使用。
      templateCode: '',
      // 维护 模板名称 字段，供当前前端状态或配置直接使用。
      templateName: '',
      // 维护 班次Type 字段，供当前前端状态或配置直接使用。
      shiftType: 'WORK',
      // 维护 startTime 字段，供当前前端状态或配置直接使用。
      startTime: '09:00:00',
      // 维护 endTime 字段，供当前前端状态或配置直接使用。
      endTime: '18:00:00',
      // 维护 crossDay 字段，供当前前端状态或配置直接使用。
      crossDay: false,
      // 维护 scheduledBreakMinutes 字段，供当前前端状态或配置直接使用。
      scheduledBreakMinutes: 60,
      // 维护 颜色 字段，供当前前端状态或配置直接使用。
      color: 'BLUE',
      // 维护 启用标记 字段，供当前前端状态或配置直接使用。
      active: true
    },
    // 维护 导入CsvText 字段，供当前前端状态或配置直接使用。
    importCsvText: '',
    // 维护 导入Result 字段，供当前前端状态或配置直接使用。
    importResult: null
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  })

  // 统一字典函数，避免每个子组件重复引入多语言表。
  // 声明 t 状态，保存当前工作台交互过程中需要的前端数据。
  const t = (key) => messages[locale.value][key] || key

  // 工作台导航来自当前语言字典，切换语言后标签会同步刷新。
  // 声明 导航Items 状态，保存当前工作台交互过程中需要的前端数据。
  const navItems = computed(() => [
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    { key: 'wizard', label: t('navWizard') },
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    { key: 'workplace', label: t('navWorkplace') },
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    { key: 'department', label: t('navDepartment') },
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    { key: 'employee', label: t('navEmployee') },
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    { key: 'shift', label: t('navShift') },
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    { key: 'schedule', label: t('navSchedule') }
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  ])

  // 推荐动作取自后端步骤状态，前端只负责翻译展示。
  // 声明 推荐NextLabel 状态，保存当前工作台交互过程中需要的前端数据。
  const recommendedNextLabel = computed(() => t(state.recommendedNextAction || 'wizard.schedule'))

  // 员工列表过滤只在前端做轻量筛选，不额外增加第一阶段查询接口复杂度。
  // 声明 filteredEmployees 状态，保存当前工作台交互过程中需要的前端数据。
  const filteredEmployees = computed(() =>
    // 更新 employees.filter((item) 状态，保证工作台界面与本次操作结果同步。
    state.employees.filter((item) => {
      // 声明 matchKeyword 状态，保存当前工作台交互过程中需要的前端数据。
      const matchKeyword =
        // 执行当前业务步骤，推进本行对应的 composable 处理。
        !state.employeeFilters.keyword ||
        // 执行当前业务步骤，推进本行对应的 composable 处理。
        item.employeeNo.includes(state.employeeFilters.keyword) ||
        // 执行当前业务步骤，推进本行对应的 composable 处理。
        item.employeeName.includes(state.employeeFilters.keyword)
      // 声明 match部门 状态，保存当前工作台交互过程中需要的前端数据。
      const matchDepartment =
        // 执行当前业务步骤，推进本行对应的 composable 处理。
        !state.employeeFilters.departmentId || String(item.departmentId) === String(state.employeeFilters.departmentId)
      // 声明 matchEmployment 状态，保存当前工作台交互过程中需要的前端数据。
      const matchEmployment =
        // 执行当前业务步骤，推进本行对应的 composable 处理。
        !state.employeeFilters.employmentType || item.employmentType === state.employeeFilters.employmentType
      // 声明 match状态 状态，保存当前工作台交互过程中需要的前端数据。
      const matchStatus = !state.employeeFilters.status || item.status === state.employeeFilters.status
      // 返回当前步骤产出的业务结果，继续交给上一层消费。
      return matchKeyword && matchDepartment && matchEmployment && matchStatus
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    })
  )

  // 部门列表支持按当前事业所上下文收口，进入部门模块时直接只看该事业所下的部门。
  // 声明 filteredDepartments 状态，保存当前工作台交互过程中需要的前端数据。
  const filteredDepartments = computed(() =>
    // 更新 departments.filter((item) 状态，保证工作台界面与本次操作结果同步。
    state.departments.filter((item) => {
      // 当前没有事业所上下文时返回全部部门，避免普通入口被意外收窄。
      const matchWorkplace =
        !state.departmentFilters.workplaceId || String(item.workplaceId) === String(state.departmentFilters.workplaceId)
      // 返回当前步骤产出的业务结果，继续交给上一层消费。
      return matchWorkplace
    })
  )

  // 右上角标题和部门列表筛选提示都需要当前事业所名称，统一从主数据里反查避免重复保存名称副本。
  // 声明 currentDepartmentWorkplaceName 状态，保存当前工作台交互过程中需要的前端数据。
  const currentDepartmentWorkplaceName = computed(() => {
    // 没有事业所筛选时不显示上下文提示，保持普通部门管理视图简洁。
    if (!state.departmentFilters.workplaceId) {
      return ''
    }
    // 根据当前筛选主键回查事业所名称，保证提示和最新主数据一致。
    const workplace = state.workplaces.find((item) => String(item.id) === String(state.departmentFilters.workplaceId))
    // 返回当前步骤产出的业务结果，继续交给上一层消费。
    return workplace?.workplaceName || ''
  })

  // 页面挂载时先拉第一阶段 bootstrap 聚合结果，让各子区块共享同一份基线数据。
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  onMounted(async () => {
    // 首屏先完成主数据聚合刷新，确保后续排班看板读取时已有事业所、部门和员工基线。
    await loadBootstrap()
    // 如果用户是通过深链接直接进入排班模块，需要在首屏补拉一次第二阶段看板，避免页面误判为空。
    if (activeSection.value === 'schedule') {
      await loadScheduleBoard()
    }
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  })

  // 工作区切到排班模块时立即刷新第二阶段看板，避免用户看到旧月份或空数据。
  watch(activeSection, (nextSection) => {
    if (nextSection === 'schedule') {
      loadScheduleBoard()
    }
  })

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function loadBootstrap() {
    // 刷新期间先拉起 loading，避免用户误把旧数据当成当前状态。
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    loading.value = true
    // 进入受控处理区块，统一兜底当前业务动作的异常路径。
    try {
      // 统一从 bootstrap 聚合接口刷新主数据，保持各区块之间状态一致。
      // 声明 payload 状态，保存当前工作台交互过程中需要的前端数据。
      const payload = await fetchBootstrap()
      // 更新 steps 状态，保证工作台界面与本次操作结果同步。
      state.steps = payload.steps || []
      // 更新 workplaces 状态，保证工作台界面与本次操作结果同步。
      state.workplaces = payload.workplaces || []
      // 更新 departments 状态，保证工作台界面与本次操作结果同步。
      state.departments = payload.departments || []
      // 更新 employees 状态，保证工作台界面与本次操作结果同步。
      state.employees = payload.employees || []
      // 更新 班次Templates 状态，保证工作台界面与本次操作结果同步。
      state.shiftTemplates = payload.shiftTemplates || []
      // 更新 推荐NextAction 状态，保证工作台界面与本次操作结果同步。
      state.recommendedNextAction = payload.recommendedNextAction || ''
      // 把来源数据整体回填到当前状态对象，减少逐字段重复赋值。
      Object.assign(state.tenant, payload.tenant || state.tenant)
      // 首次进入时自动给下游表单填上默认事业所和部门，减少空表单阻塞。
      // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
      if (!state.departmentForm.workplaceId && state.workplaces[0]) {
        // 更新 部门Form.workplaceId 状态，保证工作台界面与本次操作结果同步。
        state.departmentForm.workplaceId = state.workplaces[0].id
      }
      // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
      if (!state.employeeForm.workplaceId && state.workplaces[0]) {
        // 更新 员工Form.workplaceId 状态，保证工作台界面与本次操作结果同步。
        state.employeeForm.workplaceId = state.workplaces[0].id
      }
      // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
      if (!state.employeeForm.departmentId && state.departments[0]) {
        // 更新 员工Form.departmentId 状态，保证工作台界面与本次操作结果同步。
        state.employeeForm.departmentId = state.departments[0].id
      }
      // 删除或刷新主数据后，如果当前事业所筛选已不存在，就主动回退到全部部门视图。
      if (
        state.departmentFilters.workplaceId &&
        !state.workplaces.some((item) => String(item.id) === String(state.departmentFilters.workplaceId))
      ) {
        state.departmentFilters.workplaceId = ''
      }
      // 员工筛选若指向了已被删除的部门，也要及时清空，避免列表被卡在空视图。
      if (
        state.employeeFilters.departmentId &&
        !state.departments.some((item) => String(item.id) === String(state.employeeFilters.departmentId))
      ) {
        state.employeeFilters.departmentId = ''
      }
      // 排班筛选同样要随主数据刷新自愈，避免看板继续挂在失效的事业所或部门上。
      if (
        state.scheduleFilters.workplaceId &&
        !state.workplaces.some((item) => String(item.id) === String(state.scheduleFilters.workplaceId))
      ) {
        state.scheduleFilters.workplaceId = ''
      }
      if (
        state.scheduleFilters.departmentId &&
        !state.departments.some((item) => String(item.id) === String(state.scheduleFilters.departmentId))
      ) {
        state.scheduleFilters.departmentId = ''
      }
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    } finally {
      // 聚合刷新结束后释放加载态。
      // 执行当前业务步骤，推进本行对应的 composable 处理。
      loading.value = false
    }
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function submitTenant() {
    // 租户信息保存后回拉 bootstrap，保证摘要区和租户面板同时刷新。
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await saveTenant({ ...state.tenant })
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    pushToast(t('toastSaved'))
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await loadBootstrap()
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function submitWorkplace() {
    // 事业所表单通过 id 是否存在判断新增还是编辑。
    // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
    if (state.workplaceForm.id) {
      // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
      await updateWorkplace(state.workplaceForm.id, { ...state.workplaceForm })
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    } else {
      // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
      await createWorkplace({ ...state.workplaceForm })
    }
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    resetWorkplaceForm()
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    pushToast(t('toastSaved'))
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await loadBootstrap()
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function removeWorkplace(id) {
    // 删除后刷新聚合，避免部门和员工下拉还残留旧事业所。
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await deleteWorkplace(id)
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    pushToast(t('toastDeleted'))
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await loadBootstrap()
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function submitDepartment() {
    // 部门保存前把下拉返回的字符串主键转成数字，保证后端 DTO 能正确绑定。
    // 声明 payload 状态，保存当前工作台交互过程中需要的前端数据。
    const payload = { ...state.departmentForm, workplaceId: Number(state.departmentForm.workplaceId) }
    // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
    if (state.departmentForm.id) {
      // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
      await updateDepartment(state.departmentForm.id, payload)
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    } else {
      // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
      await createDepartment(payload)
    }
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    resetDepartmentForm()
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    pushToast(t('toastSaved'))
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await loadBootstrap()
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function removeDepartment(id) {
    // 删除部门后必须联动刷新员工可选部门列表。
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await deleteDepartment(id)
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    pushToast(t('toastDeleted'))
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await loadBootstrap()
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function submitEmployee() {
    // 员工保存同时提交事业所和部门主键，保持组织归属完整。
    // 声明 payload 状态，保存当前工作台交互过程中需要的前端数据。
    const payload = {
      // 执行当前业务步骤，推进本行对应的 composable 处理。
      ...state.employeeForm,
      // 维护 事业所Id 字段，供当前前端状态或配置直接使用。
      workplaceId: Number(state.employeeForm.workplaceId),
      // 维护 部门Id 字段，供当前前端状态或配置直接使用。
      departmentId: Number(state.employeeForm.departmentId)
    }
    // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
    if (state.employeeForm.id) {
      // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
      await updateEmployee(state.employeeForm.id, payload)
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    } else {
      // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
      await createEmployee(payload)
    }
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    resetEmployeeForm()
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    pushToast(t('toastSaved'))
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await loadBootstrap()
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function removeEmployee(id) {
    // 员工删除后刷新列表和向导统计，确保第一阶段完成度即时变化。
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await deleteEmployee(id)
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    pushToast(t('toastDeleted'))
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await loadBootstrap()
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function submitMapping() {
    // 外部打卡映射独立提交，避免员工基础资料和外部系统绑定互相耦合。
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await bindExternalMapping(state.mappingForm.employeeId, { ...state.mappingForm })
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    resetMappingForm()
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    pushToast(t('toastSaved'))
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await loadBootstrap()
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function submitImport() {
    // 导入结果单独保存，便于页面展示成功数和失败明细。
    // 更新 导入Result 状态，保证工作台界面与本次操作结果同步。
    state.importResult = await importEmployees({ csvText: state.importCsvText })
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    pushToast(t('toastImported'))
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await loadBootstrap()
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function handleExport() {
    // 导出仍走浏览器下载，不把 CSV 内容直接塞进页面。
    // 声明 payload 状态，保存当前工作台交互过程中需要的前端数据。
    const payload = await exportEmployees()
    // 声明 blob 状态，保存当前工作台交互过程中需要的前端数据。
    const blob = new Blob([payload.content], { type: 'text/csv;charset=utf-8;' })
    // 声明 link 状态，保存当前工作台交互过程中需要的前端数据。
    const link = document.createElement('a')
    // 配置浏览器下载链接参数，让导出文件按预期落地。
    link.href = URL.createObjectURL(blob)
    // 配置浏览器下载链接参数，让导出文件按预期落地。
    link.download = payload.fileName
    // 配置浏览器下载链接参数，让导出文件按预期落地。
    link.click()
    // 释放临时下载地址，避免浏览器长期持有无用资源。
    URL.revokeObjectURL(link.href)
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function submitShiftTemplate() {
    // 班次模板在第一阶段同时支持手工维护和后端推荐生成。
    // 声明 payload 状态，保存当前工作台交互过程中需要的前端数据。
    const payload = { ...state.shiftForm }
    // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
    if (state.shiftForm.id) {
      // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
      await updateShiftTemplate(state.shiftForm.id, payload)
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    } else {
      // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
      await createShiftTemplate(payload)
    }
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    resetShiftForm()
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    pushToast(t('toastSaved'))
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await loadBootstrap()
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function generateRecommended() {
    // 推荐模板生成后立刻刷新，便于用户继续后续配置。
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await generateRecommendedShiftTemplates()
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    pushToast(t('toastGenerated'))
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await loadBootstrap()
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function removeShiftTemplate(id) {
    // 删除模板后同步刷新，避免向导统计继续显示旧数量。
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await deleteShiftTemplate(id)
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    pushToast(t('toastDeleted'))
    // 等待异步业务结果返回，确保页面状态和后端结果保持一致。
    await loadBootstrap()
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function loadScheduleBoard() {
    // 排班模块读取独立看板时同样拉起 loading，避免月份切换时短暂看到旧格子。
    loading.value = true
    try {
      // 当前筛选条件直接透传给后端，让员工行、日期列和未排班统计共享同一口径。
      const payload = await fetchScheduleBoard({ ...state.scheduleFilters })
      // 更新 排班看板 状态，保证工作台界面与本次操作结果同步。
      state.scheduleBoard = payload
      // 右侧模板面板默认沿用看板回传模板，保证和当前月份数据同源。
      if (!state.scheduleForm.selectedTemplateId && payload.shiftTemplates?.[0]) {
        state.scheduleForm.selectedTemplateId = payload.shiftTemplates[0].id
      }
      // 切月或切筛选后清空旧的未排班检查结果，避免把上一个视图的提醒残留到当前页面。
      state.scheduleUnassignedItems = []
    } finally {
      // 看板刷新完成后释放加载态，让页面恢复可交互状态。
      loading.value = false
    }
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  function selectScheduleTemplate(template) {
    // 右侧模板面板的选择结果要落进当前表单，供点击格子时直接复用。
    state.scheduleForm.selectedTemplateId = template.id
    // 同步记录模板名称，便于右侧说明区和轻提示直接展示。
    state.scheduleForm.selectedTemplateName = template.templateName
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function applySchedule(row, workDate, existingItem) {
    // 无论本次是新增还是替换，都先把当前选中的员工和日期写进右侧说明区。
    Object.assign(state.scheduleForm, {
      selectedEmployeeId: row.employeeId,
      selectedEmployeeName: row.employeeName,
      selectedWorkDate: workDate,
      selectedScheduleId: existingItem?.id || null,
      selectedTemplateName: existingItem?.templateName || state.scheduleForm.selectedTemplateName
    })
    // 没选模板就先阻断保存，并用轻提示提醒用户先在右侧点选班次。
    if (!state.scheduleForm.selectedTemplateId) {
      pushToast(t('scheduleTemplateNeedPick'))
      return
    }
    // 覆盖已有排班前给用户一次明确确认，避免误改整周已排好的班。
    if (existingItem && !window.confirm(t('scheduleOverwriteConfirm').replace('{current}', existingItem.templateName).replace('{next}', state.scheduleForm.selectedTemplateName || '-'))) {
      return
    }
    // 后端 create 接口本身已兼容同员工同日期覆盖，因此前端只需提交统一保存负载。
    await createSchedule({
      employeeId: row.employeeId,
      workDate,
      shiftTemplateId: state.scheduleForm.selectedTemplateId,
      remark: state.scheduleForm.remark
    })
    // 统一回显当前选中模板名，让右侧说明区和最新保存结果保持一致。
    const selectedTemplate = state.scheduleBoard.shiftTemplates.find((item) => item.id === state.scheduleForm.selectedTemplateId)
    state.scheduleForm.selectedTemplateName = selectedTemplate?.templateName || ''
    pushToast(existingItem ? t('scheduleToastReplaced') : t('scheduleToastSaved'))
    await loadScheduleBoard()
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function removeScheduleItem(id) {
    // 这里仅承接真正删除动作，页面层会先弹主题化确认框，避免继续走浏览器原生 confirm。
    await deleteSchedule(id)
    // 删除后清空右侧当前格说明，避免用户还以为该格子有排班。
    Object.assign(state.scheduleForm, {
      selectedScheduleId: null,
      selectedTemplateName: '',
      selectedWorkDate: '',
      selectedEmployeeId: null,
      selectedEmployeeName: ''
    })
    pushToast(t('toastDeleted'))
    await loadScheduleBoard()
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  function openBatchWizard() {
    // 批量向导默认用当前可见员工和当前月份整月区间，减少用户重复勾选和补日期。
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

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  function closeBatchWizard() {
    // 关闭批量向导时只收起向导本身，不清空已经选择的右侧模板。
    state.batchWizard.open = false
    state.batchWizard.step = 1
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  function nextBatchStep() {
    // 向导只允许在 1 到 5 步之间前进，避免界面状态越界。
    state.batchWizard.step = Math.min(5, state.batchWizard.step + 1)
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  function prevBatchStep() {
    // 向导回退时同样钳住最小步数，保持步骤条和内容区一致。
    state.batchWizard.step = Math.max(1, state.batchWizard.step - 1)
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function confirmBatchWizard() {
    // 批量提交前给用户最后一次确认，避免带着错误范围一次性覆盖整月。
    if (!window.confirm(t('scheduleBatchConfirmDialog'))) {
      return
    }
    const payload = {
      employeeIds: [...state.batchWizard.employeeIds],
      startDate: state.batchWizard.startDate,
      endDate: state.batchWizard.endDate,
      shiftTemplateId: Number(state.batchWizard.shiftTemplateId),
      skipExisting: state.batchWizard.skipExisting,
      overwriteExisting: state.batchWizard.overwriteExisting,
      remark: state.batchWizard.remark
    }
    const result = await batchAssignSchedules(payload)
    pushToast(t('scheduleBatchResultToast').replace('{created}', String(result.createdCount)).replace('{updated}', String(result.updatedCount)).replace('{skipped}', String(result.skippedCount)))
    closeBatchWizard()
    await loadScheduleBoard()
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function copySchedulesFromLastWeek() {
    // 复制上周前先明确提示覆盖风险，让用户知道当前是跨周复制动作。
    if (!window.confirm(t('scheduleCopyWeekConfirm'))) {
      return
    }
    const result = await copyLastWeekSchedules({
      employeeIds: state.scheduleBoard.employeeRows.map((item) => item.employeeId),
      startDate: `${state.scheduleFilters.month}-01`,
      endDate: state.scheduleBoard.endDate || `${state.scheduleFilters.month}-01`,
      overwriteExisting: true
    })
    pushToast(t('scheduleCopyResultToast').replace('{created}', String(result.createdCount)).replace('{updated}', String(result.updatedCount)).replace('{skipped}', String(result.skippedCount)))
    await loadScheduleBoard()
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function copySchedulesFromLastMonth() {
    // 复制上月逻辑与复制上周同样需要显式确认，避免整月误覆盖。
    if (!window.confirm(t('scheduleCopyMonthConfirm'))) {
      return
    }
    const result = await copyLastMonthSchedules({
      employeeIds: state.scheduleBoard.employeeRows.map((item) => item.employeeId),
      startDate: `${state.scheduleFilters.month}-01`,
      endDate: state.scheduleBoard.endDate || `${state.scheduleFilters.month}-01`,
      overwriteExisting: true
    })
    pushToast(t('scheduleCopyResultToast').replace('{created}', String(result.createdCount)).replace('{updated}', String(result.updatedCount)).replace('{skipped}', String(result.skippedCount)))
    await loadScheduleBoard()
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function runUnassignedCheck() {
    // 右侧缺口卡单独读取后端检查结果，让管理员能立即看到仍未排班的人和日期。
    state.scheduleUnassignedItems = await checkUnassignedSchedules({ ...state.scheduleFilters })
    pushToast(t('scheduleUnassignedChecked'))
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  async function handleScheduleExport() {
    // 排班导出和员工导出一样走浏览器下载，避免把大段 CSV 文本直接塞到界面里。
    const payload = await exportSchedules({ ...state.scheduleFilters })
    const blob = new Blob([payload.content], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = payload.fileName
    link.click()
    URL.revokeObjectURL(link.href)
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  function editWorkplace(item) {
    // 编辑动作把行数据回填到事业所表单，并切换到对应页签。
    // 把来源数据整体回填到当前状态对象，减少逐字段重复赋值。
    Object.assign(state.workplaceForm, item)
    // 切换当前工作区页签，让界面定位到目标业务模块。
    activeSection.value = 'workplace'
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  function editDepartment(item) {
    // 部门编辑沿用当前表单，避免再开额外弹窗流程。
    // 把来源数据整体回填到当前状态对象，减少逐字段重复赋值。
    Object.assign(state.departmentForm, item)
    // 切换当前工作区页签，让界面定位到目标业务模块。
    activeSection.value = 'department'
  }

  // 从事业所管理进入部门管理时，直接把部门列表收口到当前事业所，减少用户二次筛选。
  function openWorkplaceDepartments(item) {
    // 更新 部门Filters.workplaceId 状态，保证工作台界面与本次操作结果同步。
    state.departmentFilters.workplaceId = String(item.id)
    // 新增部门时默认沿用当前事业所，避免跳过去后还要重新指定归属。
    state.departmentForm.workplaceId = item.id
    // 切换当前工作区页签，让界面定位到目标业务模块。
    activeSection.value = 'department'
  }

  // 部门管理可以继续往下钻到员工管理，并自动只看当前部门员工。
  function openDepartmentEmployees(item) {
    // 清掉旧关键字和状态条件，只保留当前部门上下文，确保用户看到的是完整部门员工清单。
    state.employeeFilters.keyword = ''
    state.employeeFilters.departmentId = String(item.id)
    state.employeeFilters.employmentType = ''
    state.employeeFilters.status = ''
    // 新增员工时默认继承当前部门和事业所，避免再次手动选择归属。
    state.employeeForm.workplaceId = item.workplaceId
    state.employeeForm.departmentId = item.id
    // 切换当前工作区页签，让界面定位到目标业务模块。
    activeSection.value = 'employee'
  }

  // 同一个部门上下文还要能直接进入排班管理，避免用户先切页再重新选部门。
  async function openDepartmentSchedule(item) {
    // 进入前先记录当前页签，只有已经停留在排班模块时才需要手动刷新，避免和 watch 触发双重请求。
    const wasScheduleSection = activeSection.value === 'schedule'
    // 排班看板只保留当前部门上下文，其他自由检索条件先清空，确保第一次进入就看到完整部门排班。
    state.scheduleFilters.workplaceId = String(item.workplaceId)
    state.scheduleFilters.departmentId = String(item.id)
    state.scheduleFilters.employeeKeyword = ''
    state.scheduleFilters.onlyUnassigned = false
    // 切换当前工作区页签，让界面定位到目标业务模块。
    activeSection.value = 'schedule'
    // 如果用户本来就在排班模块，需要主动刷新一次看板，立即把新上下文打到页面上。
    if (wasScheduleSection) {
      await loadScheduleBoard()
    }
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  function editEmployee(item) {
    // 员工编辑时只回填当前第一阶段实际维护的字段。
    // 把来源数据整体回填到当前状态对象，减少逐字段重复赋值。
    Object.assign(state.employeeForm, {
      // 维护 id 字段，供当前前端状态或配置直接使用。
      id: item.id,
      // 维护 员工No 字段，供当前前端状态或配置直接使用。
      employeeNo: item.employeeNo,
      // 维护 员工名称 字段，供当前前端状态或配置直接使用。
      employeeName: item.employeeName,
      // 维护 员工名称Kana 字段，供当前前端状态或配置直接使用。
      employeeNameKana: item.employeeNameKana,
      // 维护 employmentType 字段，供当前前端状态或配置直接使用。
      employmentType: item.employmentType,
      // 维护 入社Date 字段，供当前前端状态或配置直接使用。
      hireDate: item.hireDate,
      // 维护 事业所Id 字段，供当前前端状态或配置直接使用。
      workplaceId: item.workplaceId,
      // 维护 部门Id 字段，供当前前端状态或配置直接使用。
      departmentId: item.departmentId,
      // 维护 状态 字段，供当前前端状态或配置直接使用。
      status: item.status
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    })
    // 切换当前工作区页签，让界面定位到目标业务模块。
    activeSection.value = 'employee'
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  function editMapping(item) {
    // 外部映射编辑单独回填映射表单，避免误改员工主档。
    // 把来源数据整体回填到当前状态对象，减少逐字段重复赋值。
    Object.assign(state.mappingForm, {
      // 维护 员工Id 字段，供当前前端状态或配置直接使用。
      employeeId: item.id,
      // 维护 sourceSystem 字段，供当前前端状态或配置直接使用。
      sourceSystem: item.externalSourceSystem || 'KING_OF_TIME',
      // 维护 外部系统员工Id 字段，供当前前端状态或配置直接使用。
      externalEmployeeId: item.externalEmployeeId || '',
      // 维护 外部系统员工No 字段，供当前前端状态或配置直接使用。
      externalEmployeeNo: item.externalEmployeeNo || '',
      // 维护 状态 字段，供当前前端状态或配置直接使用。
      status: 'ACTIVE'
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    })
    // 切换当前工作区页签，让界面定位到目标业务模块。
    activeSection.value = 'employee'
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  function editShiftTemplate(item) {
    // 模板编辑时保留跨天和颜色等规则字段，便于直接重用。
    // 把来源数据整体回填到当前状态对象，减少逐字段重复赋值。
    Object.assign(state.shiftForm, item)
    // 切换当前工作区页签，让界面定位到目标业务模块。
    activeSection.value = 'shift'
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  function resetWorkplaceForm() {
    // 新建事业所时回到默认空表单。
    // 把来源数据整体回填到当前状态对象，减少逐字段重复赋值。
    Object.assign(state.workplaceForm, {
      // 维护 id 字段，供当前前端状态或配置直接使用。
      id: null,
      // 维护 事业所编码 字段，供当前前端状态或配置直接使用。
      workplaceCode: '',
      // 维护 事业所名称 字段，供当前前端状态或配置直接使用。
      workplaceName: '',
      // 维护 地址 字段，供当前前端状态或配置直接使用。
      address: '',
      // 维护 电话 字段，供当前前端状态或配置直接使用。
      phone: '',
      // 维护 状态 字段，供当前前端状态或配置直接使用。
      status: 'ACTIVE'
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    })
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  function resetDepartmentForm() {
    // 新建部门时默认挂到当前第一个事业所，减少重复选择。
    // 把来源数据整体回填到当前状态对象，减少逐字段重复赋值。
    Object.assign(state.departmentForm, {
      // 维护 id 字段，供当前前端状态或配置直接使用。
      id: null,
      // 维护 事业所Id 字段，供当前前端状态或配置直接使用。
      workplaceId: state.workplaces[0]?.id || '',
      // 维护 部门编码 字段，供当前前端状态或配置直接使用。
      departmentCode: '',
      // 维护 部门名称 字段，供当前前端状态或配置直接使用。
      departmentName: '',
      // 维护 sortOrder 字段，供当前前端状态或配置直接使用。
      sortOrder: 0,
      // 维护 状态 字段，供当前前端状态或配置直接使用。
      status: 'ACTIVE'
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    })
  }

  // 查看全部部门时只清掉事业所上下文，不打断当前部门编辑表单。
  function clearDepartmentWorkplaceFilter() {
    // 更新 部门Filters.workplaceId 状态，保证工作台界面与本次操作结果同步。
    state.departmentFilters.workplaceId = ''
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  function resetEmployeeForm() {
    // 新建员工时默认继承当前第一条事业所和部门，缩短录入路径。
    // 把来源数据整体回填到当前状态对象，减少逐字段重复赋值。
    Object.assign(state.employeeForm, {
      // 维护 id 字段，供当前前端状态或配置直接使用。
      id: null,
      // 维护 员工No 字段，供当前前端状态或配置直接使用。
      employeeNo: '',
      // 维护 员工名称 字段，供当前前端状态或配置直接使用。
      employeeName: '',
      // 维护 员工名称Kana 字段，供当前前端状态或配置直接使用。
      employeeNameKana: '',
      // 维护 employmentType 字段，供当前前端状态或配置直接使用。
      employmentType: 'FULL_TIME',
      // 维护 入社Date 字段，供当前前端状态或配置直接使用。
      hireDate: '',
      // 维护 事业所Id 字段，供当前前端状态或配置直接使用。
      workplaceId: state.workplaces[0]?.id || '',
      // 维护 部门Id 字段，供当前前端状态或配置直接使用。
      departmentId: state.departments[0]?.id || '',
      // 维护 状态 字段，供当前前端状态或配置直接使用。
      status: 'ACTIVE'
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    })
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  function resetMappingForm() {
    // 映射表单每次提交后清空，防止把前一个员工的外部编号误绑到下一个员工。
    // 把来源数据整体回填到当前状态对象，减少逐字段重复赋值。
    Object.assign(state.mappingForm, {
      // 维护 员工Id 字段，供当前前端状态或配置直接使用。
      employeeId: null,
      // 维护 sourceSystem 字段，供当前前端状态或配置直接使用。
      sourceSystem: 'KING_OF_TIME',
      // 维护 外部系统员工Id 字段，供当前前端状态或配置直接使用。
      externalEmployeeId: '',
      // 维护 外部系统员工No 字段，供当前前端状态或配置直接使用。
      externalEmployeeNo: '',
      // 维护 状态 字段，供当前前端状态或配置直接使用。
      status: 'ACTIVE'
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    })
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  function resetShiftForm() {
    // 班次模板空表单保持第一阶段默认标准工时模板。
    // 把来源数据整体回填到当前状态对象，减少逐字段重复赋值。
    Object.assign(state.shiftForm, {
      // 维护 id 字段，供当前前端状态或配置直接使用。
      id: null,
      // 维护 模板编码 字段，供当前前端状态或配置直接使用。
      templateCode: '',
      // 维护 模板名称 字段，供当前前端状态或配置直接使用。
      templateName: '',
      // 维护 班次Type 字段，供当前前端状态或配置直接使用。
      shiftType: 'WORK',
      // 维护 startTime 字段，供当前前端状态或配置直接使用。
      startTime: '09:00:00',
      // 维护 endTime 字段，供当前前端状态或配置直接使用。
      endTime: '18:00:00',
      // 维护 crossDay 字段，供当前前端状态或配置直接使用。
      crossDay: false,
      // 维护 scheduledBreakMinutes 字段，供当前前端状态或配置直接使用。
      scheduledBreakMinutes: 60,
      // 维护 颜色 字段，供当前前端状态或配置直接使用。
      color: 'BLUE',
      // 维护 启用标记 字段，供当前前端状态或配置直接使用。
      active: true
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    })
  }

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  function pushToast(message) {
    // 轻提示统一在 2.2 秒后自动清空，避免遮挡工作台内容。
    // 更新轻提示内容，让用户及时看到当前操作反馈。
    toast.value = message
    // 启动定时器，在短暂提示后自动清理临时界面状态。
    window.setTimeout(() => {
      // 更新轻提示内容，让用户及时看到当前操作反馈。
      toast.value = ''
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    }, 2200)
  }

  // 返回当前步骤产出的业务结果，继续交给上一层消费。
  return {
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    locale,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    localeOptions,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    activeSection,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    loading,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    toast,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    state,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    t,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    navItems,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    recommendedNextLabel,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    filteredDepartments,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    currentDepartmentWorkplaceName,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    filteredEmployees,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    loadBootstrap,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    submitTenant,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    submitWorkplace,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    removeWorkplace,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    submitDepartment,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    removeDepartment,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    submitEmployee,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    removeEmployee,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    submitMapping,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    submitImport,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    handleExport,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    loadScheduleBoard,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    selectScheduleTemplate,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    applySchedule,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    removeScheduleItem,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    openBatchWizard,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    closeBatchWizard,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    nextBatchStep,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    prevBatchStep,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    confirmBatchWizard,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    copySchedulesFromLastWeek,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    copySchedulesFromLastMonth,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    runUnassignedCheck,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    handleScheduleExport,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    submitShiftTemplate,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    generateRecommended,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    removeShiftTemplate,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    editWorkplace,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    openWorkplaceDepartments,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    editDepartment,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    openDepartmentEmployees,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    openDepartmentSchedule,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    editEmployee,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    editMapping,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    editShiftTemplate,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    resetWorkplaceForm,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    resetDepartmentForm,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    clearDepartmentWorkplaceFilter,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    resetEmployeeForm,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    resetMappingForm,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    resetShiftForm
  }
}
