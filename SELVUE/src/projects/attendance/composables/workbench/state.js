// 这里集中定义 workbench 的初始状态工厂，供壳层和各 section 模块共享同一份业务模型。

// 统一生成当前月份字符串，供排班筛选和排班看板默认定位到当月。
const currentMonth = () => new Date().toISOString().slice(0, 7)

// 生成空租户对象，供首页租户面板在轻量壳返回前先有稳定结构。
export const createEmptyTenant = () => ({
  tenantCode: '',
  tenantName: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  timezone: 'Asia/Tokyo'
})

// 生成 section 级加载状态，供多接口并发场景下按区块显示局部加载状态。
export const createSectionLoaders = () => ({
  workplace: false,
  department: false,
  employee: false,
  rule: false,
  shift: false,
  schedule: false,
  punch: false,
  daily: false,
  case: false,
  monthly: false
})

// 生成 section 级错误状态，供某个区块失败时只影响本区块而不是整页。
export const createSectionErrors = () => ({
  workplace: '',
  department: '',
  employee: '',
  rule: '',
  shift: '',
  schedule: '',
  punch: '',
  daily: '',
  case: '',
  monthly: ''
})

// 生成 section 加载完成标记，供按需懒加载时避免重复首刷同一区块。
export const createSectionStates = () => ({
  workplace: false,
  department: false,
  employee: false,
  rule: false,
  shift: false,
  schedule: false,
  punch: false,
  daily: false,
  case: false,
  monthly: false
})

// 生成轻量首页壳状态，承载租户摘要、步骤计数和推荐动作。
export const createBootstrapShell = () => ({
  tenantSummary: createEmptyTenant(),
  sectionCounters: {
    workplace: 0,
    department: 0,
    employee: 0,
    rule: 0,
    shift: 0,
    schedule: 0,
    punch: 0,
    daily: 0,
    case: 0,
    monthly: 0
  },
  sectionStates: createSectionStates(),
  recommendedNextAction: 'wizard.schedule'
})

// 生成工作台主状态，继续沿用原页面消费的数据结构，降低视图改造范围。
export const createWorkbenchState = () => ({
  bootstrapShell: createBootstrapShell(),
  sectionLoaders: createSectionLoaders(),
  sectionErrors: createSectionErrors(),
  tenant: createEmptyTenant(),
  steps: [],
  workplaces: [],
  departments: [],
  departmentFilters: {
    workplaceId: ''
  },
  employees: [],
  ruleFilters: {
    yearMonth: '2026-05',
    keyword: '',
    activeOnly: true
  },
  ruleWorkbench: {
    rules: [],
    assignments: [],
    alerts: [],
    summary: {
      highRiskCount: 0,
      reminderCount: 0,
      boundEmployeeCount: 0
    }
  },
  shiftTemplates: [],
  punchFilters: {
    dateFrom: '2026-05-01',
    dateTo: '2026-05-31',
    employeeKeyword: '',
    sourceSystem: '',
    processStatus: '',
    punchType: '',
    page: 1,
    pageSize: 20
  },
  punchLogList: {
    items: [],
    total: 0,
    page: 1,
    pageSize: 20,
    totalPages: 1,
    summary: {
      processed: 0,
      unmatched: 0,
      error: 0,
      duplicate: 0,
      ignored: 0
    }
  },
  punchDetail: null,
  punchManualForm: {
    employeeId: '',
    punchTime: '2026-05-28T09:00',
    punchType: 'CLOCK_IN',
    sourceSystem: 'MANUAL',
    deviceName: '管理员手动补录',
    note: ''
  },
  punchImportForm: {
    fileName: 'attendance-punch-import.csv',
    csvText: ''
  },
  punchImportPreview: null,
  punchActionForm: {
    employeeId: '',
    ignoreReason: '确认后忽略'
  },
  dailyFilters: {
    startDate: '2026-05-01',
    endDate: '2026-05-31',
    workplaceId: '',
    departmentId: '',
    employeeKeyword: '',
    status: '',
    exceptionOnly: false,
    page: 1,
    pageSize: 20
  },
  dailyList: {
    items: [],
    total: 0,
    page: 1,
    pageSize: 20,
    totalPages: 1,
    summary: {
      normalCount: 0,
      lateCount: 0,
      missingClockCount: 0,
      absenceCount: 0
    }
  },
  dailyDetail: null,
  caseFilters: {
    startDate: '2026-05-01',
    endDate: '2026-05-31',
    workplaceId: '',
    departmentId: '',
    employeeKeyword: '',
    caseStatus: '',
    handlingStatus: '',
    mineOnly: false,
    page: 1,
    pageSize: 20
  },
  caseList: {
    items: [],
    total: 0,
    page: 1,
    pageSize: 20,
    totalPages: 1,
    summary: {
      pendingCount: 0,
      reviewingCount: 0,
      approvedCount: 0,
      rejectedCount: 0,
      lockedCount: 0
    }
  },
  caseDetail: null,
  caseFocusItem: null,
  caseCreateForm: {
    applicantId: 9001,
    applicantRole: 'MANAGER',
    reasonCategory: 'DEVICE_ERROR',
    reasonText: '',
    expectedResolution: ''
  },
  caseActionForm: {
    comment: '',
    finalStatus: 'NORMAL',
    finalClockIn: '',
    finalClockOut: '',
    finalBreakMinutes: '',
    finalExceptionFlag: false
  },
  monthlyFilters: {
    yearMonth: '2026-05',
    workplaceId: '',
    departmentId: '',
    employeeKeyword: '',
    closeStatus: '',
    blockedOnly: false,
    page: 1,
    pageSize: 20
  },
  monthlyList: {
    items: [],
    total: 0,
    page: 1,
    pageSize: 20,
    totalPages: 1,
    summary: {
      openCount: 0,
      closableCount: 0,
      closedCount: 0,
      reopenedCount: 0
    }
  },
  monthlyDetail: null,
  monthlyActionForm: {
    operatorId: 9001,
    comment: '',
    reopenReason: ''
  },
  scheduleFilters: {
    month: currentMonth(),
    workplaceId: '',
    departmentId: '',
    employeeKeyword: '',
    onlyUnassigned: false
  },
  scheduleBoard: {
    month: currentMonth(),
    dates: [],
    employeeRows: [],
    scheduleItems: [],
    shiftTemplates: [],
    endDate: ''
  },
  // 记录“先选模板”引导气泡状态，供排班右侧模板区定点提示当前要操作的位置。
  scheduleTemplateTip: {
    open: false,
    employeeName: '',
    workDate: '',
    bubbleX: null,
    bubbleY: null,
    anchorX: null,
    anchorY: null
  },
  scheduleForm: {
    selectedTemplateId: null,
    remark: '',
    selectedEmployeeId: null,
    selectedEmployeeName: '',
    selectedWorkDate: '',
    selectedScheduleId: null,
    selectedTemplateName: ''
  },
  batchWizard: {
    open: false,
    step: 1,
    employeeIds: [],
    startDate: '',
    endDate: '',
    shiftTemplateId: null,
    skipExisting: false,
    overwriteExisting: true,
    remark: ''
  },
  scheduleUnassignedItems: [],
  recommendedNextAction: '',
  employeeFilters: {
    keyword: '',
    departmentId: '',
    employmentType: '',
    status: ''
  },
  ruleForm: {
    id: null,
    ruleCode: '',
    ruleName: '',
    standardDailyMinutes: 480,
    standardWeeklyMinutes: 2400,
    autoBreakEnabled: true,
    autoBreakThresholdMinutes: 360,
    autoBreakDeductMinutes: 60,
    nightWorkStart: '22:00',
    nightWorkEnd: '05:00',
    roundingUnitMinutes: 15,
    roundingMode: 'ROUND_NEAREST',
    monthlyOvertimeAlertHours: 45,
    yearlyOvertimeAlertHours: 360,
    paidLeaveReminderEnabled: true,
    activeFlag: true,
    note: ''
  },
  ruleAssignmentForm: {
    employeeId: '',
    ruleId: '',
    effectiveStartDate: '2026-05-01',
    effectiveEndDate: '',
    note: ''
  },
  workplaceForm: {
    id: null,
    workplaceCode: '',
    workplaceName: '',
    address: '',
    phone: '',
    status: 'ACTIVE'
  },
  departmentForm: {
    id: null,
    workplaceId: '',
    departmentCode: '',
    departmentName: '',
    sortOrder: 0,
    status: 'ACTIVE'
  },
  employeeForm: {
    id: null,
    employeeNo: '',
    employeeName: '',
    employeeNameKana: '',
    employmentType: 'FULL_TIME',
    hireDate: '',
    workplaceId: '',
    departmentId: '',
    status: 'ACTIVE'
  },
  mappingForm: {
    employeeId: null,
    sourceSystem: 'KING_OF_TIME',
    externalEmployeeId: '',
    externalEmployeeNo: '',
    status: 'ACTIVE'
  },
  shiftForm: {
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
  },
  importCsvText: '',
  importResult: null
})
