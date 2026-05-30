// 定义 语言选项 处理入口，承接当前业务动作。
export const localeOptions = [
  // 执行当前业务步骤，推进本行对应的 constants 处理。
  { value: 'zh-CN', label: '中文', short: 'CN' },
  // 执行当前业务步骤，推进本行对应的 constants 处理。
  { value: 'ja-JP', label: '日本語', short: 'JP' }
// 执行当前业务步骤，推进本行对应的 constants 处理。
]

// 定义 文案集合 处理入口，承接当前业务动作。
export const messages = {
  // 执行当前业务步骤，推进本行对应的 constants 处理。
  'zh-CN': {
    // 维护 appTitle 字段，供当前前端状态或配置直接使用。
    appTitle: '考勤系统第六阶段工作台',
    // 维护 appSubtitle 字段，供当前前端状态或配置直接使用。
    appSubtitle: '基础资料、排班、打卡、日次、异常处理与第六阶段月次汇总月结在一个页面连续完成。',
    // 维护 liveTag 字段，供当前前端状态或配置直接使用。
    liveTag: '中日双语 / 一键启动',
    // 维护 主题Switch 字段，供当前前端状态或配置直接使用。
    themeSwitch: '主题切换',
    // 维护 主题AdminWorkbenchDark 字段，供当前前端状态或配置直接使用。
    themeAdminWorkbenchDark: '通用后台深色',
    // 维护 主题考勤GlassDark 字段，供当前前端状态或配置直接使用。
    themeAttendanceGlassDark: '考勤玻璃深色',
    // 维护 主题LiquidGlass 字段，供当前前端状态或配置直接使用。
    themeLiquidGlass: '液态玻璃浅色',
    // 维护 toastSaved 字段，供当前前端状态或配置直接使用。
    toastSaved: '已保存',
    // 维护 toastDeleted 字段，供当前前端状态或配置直接使用。
    toastDeleted: '已删除',
    // 维护 toastImported 字段，供当前前端状态或配置直接使用。
    toastImported: '导入已处理',
    // 维护 toastGenerated 字段，供当前前端状态或配置直接使用。
    toastGenerated: '推荐模板已生成',
    // 维护 导航Wizard 字段，供当前前端状态或配置直接使用。
    navWizard: '初始化向导',
    // 维护 导航事业所 字段，供当前前端状态或配置直接使用。
    navWorkplace: '事业所',
    // 维护 导航部门 字段，供当前前端状态或配置直接使用。
    navDepartment: '部门',
    // 维护 导航员工 字段，供当前前端状态或配置直接使用。
    navEmployee: '员工',
    // 维护 导航班次 字段，供当前前端状态或配置直接使用。
    navShift: '班次模板',
    // 维护 导航排班 字段，供当前前端状态或配置直接使用。
    navSchedule: '排班管理',
    // 维护 导航打卡 字段，供当前前端状态或配置直接使用。
    navPunch: '打卡记录',
    // 维护 导航日次 字段，供当前前端状态或配置直接使用。
    navDaily: '日次结果',
    // 维护 导航异常处理 字段，供当前前端状态或配置直接使用。
    navCase: '异常处理',
    // 维护 导航月次汇总 字段，供当前前端状态或配置直接使用。
    navMonthly: '月次汇总',
    // 维护 workspaceSidebarTitle 字段，供当前前端状态或配置直接使用。
    workspaceSidebarTitle: '功能导航',
    // 维护 workspaceSidebarHint 字段，供当前前端状态或配置直接使用。
    workspaceSidebarHint: '从左侧选择当前要处理的模块，右侧只展示对应工作区。',
    // 维护 workspace状态 字段，供当前前端状态或配置直接使用。
    workspaceStatus: '当前模块',
    // 维护 sectionWizardHint 字段，供当前前端状态或配置直接使用。
    sectionWizardHint: '查看第一阶段完成度、推荐下一步和公司基础信息。',
    // 维护 section事业所Hint 字段，供当前前端状态或配置直接使用。
    sectionWorkplaceHint: '维护事业所主数据，作为员工与部门的归属入口。',
    // 维护 section部门Hint 字段，供当前前端状态或配置直接使用。
    sectionDepartmentHint: '维护组织部门，供员工主数据和后续排班关联。',
    // 维护 section员工Hint 字段，供当前前端状态或配置直接使用。
    sectionEmployeeHint: '处理员工主数据、CSV 导入导出和打卡 ID 绑定。',
    // 维护 section班次Hint 字段，供当前前端状态或配置直接使用。
    sectionShiftHint: '维护班次模板，为下一阶段排班复用做准备。',
    // 维护 section排班Hint 字段，供当前前端状态或配置直接使用。
    sectionScheduleHint: '像填日历一样完成第二阶段排班，并立即检查未排班缺口。',
    // 维护 section打卡Hint 字段，供当前前端状态或配置直接使用。
    sectionPunchHint: '第三阶段开始接收真实打卡事实，并处理未匹配、失败和忽略记录。',
    // 维护 section日次Hint 字段，供当前前端状态或配置直接使用。
    sectionDailyHint: '第四阶段开始把排班和打卡计算成每日结果，并集中处理异常。',
    // 维护 section异常处理Hint 字段，供当前前端状态或配置直接使用。
    sectionCaseHint: '第五阶段开始把异常整理成处理单，进入审批、回写和锁定闭环。',
    // 维护 section月次Hint 字段，供当前前端状态或配置直接使用。
    sectionMonthlyHint: '第六阶段开始把日次汇总成月结果，并完成月结、反结和导出。',
    // 维护 wizardTitle 字段，供当前前端状态或配置直接使用。
    wizardTitle: '初始化向导',
    // 维护 wizardHint 字段，供当前前端状态或配置直接使用。
    wizardHint: '基础资料、排班、打卡、日次和异常审批已经接通；当前已进入第六阶段月次汇总与月结闭环。',
    // 维护 nextAction 字段，供当前前端状态或配置直接使用。
    nextAction: '推荐下一步',
    // 维护 phaseLocked 字段，供当前前端状态或配置直接使用。
    phaseLocked: '下一阶段开放',
    // 维护 状态Completed 字段，供当前前端状态或配置直接使用。
    statusCompleted: '已完成',
    // 维护 状态NeedsAction 字段，供当前前端状态或配置直接使用。
    statusNeedsAction: '需要处理',
    // 维护 事业所Title 字段，供当前前端状态或配置直接使用。
    workplaceTitle: '事业所管理',
    // 维护 部门Title 字段，供当前前端状态或配置直接使用。
    departmentTitle: '部门管理',
    // 维护 员工Title 字段，供当前前端状态或配置直接使用。
    employeeTitle: '员工管理',
    // 维护 班次Title 字段，供当前前端状态或配置直接使用。
    shiftTitle: '班次模板',
    // 维护 排班Title 字段，供当前前端状态或配置直接使用。
    scheduleTitle: '排班管理',
    // 维护 打卡Title 字段，供当前前端状态或配置直接使用。
    punchTitle: '打卡记录',
    // 维护 日次Title 字段，供当前前端状态或配置直接使用。
    dailyTitle: '日次结果',
    // 维护 月次Title 字段，供当前前端状态或配置直接使用。
    monthlyTitle: '月次汇总',
    // 维护 保存 字段，供当前前端状态或配置直接使用。
    save: '保存',
    // 维护 新增 字段，供当前前端状态或配置直接使用。
    create: '新增',
    // 维护 删除 字段，供当前前端状态或配置直接使用。
    delete: '删除',
    // 维护 进入部门 字段，供当前前端状态或配置直接使用。
    jumpDepartment: '进入部门',
    // 维护 查看员工 字段，供当前前端状态或配置直接使用。
    jumpEmployee: '查看员工',
    // 维护 查看排班 字段，供当前前端状态或配置直接使用。
    jumpSchedule: '查看排班',
    // 维护 当前事业所筛选 字段，供当前前端状态或配置直接使用。
    currentWorkplaceFilter: '当前事业所：{name}',
    // 维护 查看全部部门 字段，供当前前端状态或配置直接使用。
    showAllDepartments: '查看全部部门',
    // 维护 取消 字段，供当前前端状态或配置直接使用。
    cancel: '取消',
    // 维护 确认删除标题 字段，供当前前端状态或配置直接使用。
    confirmDeleteTitle: '确认删除',
    // 维护 确认删除动作 字段，供当前前端状态或配置直接使用。
    confirmDeleteAction: '确认删除',
    // 维护 具名删除确认文案 字段，供当前前端状态或配置直接使用。
    confirmDeleteMessageNamed: '确定删除{target}「{name}」吗？此操作不可撤销。',
    // 维护 泛化删除确认文案 字段，供当前前端状态或配置直接使用。
    confirmDeleteMessageUnnamed: '确定删除当前{target}吗？此操作不可撤销。',
    // 维护 导入Csv 字段，供当前前端状态或配置直接使用。
    importCsv: '导入 CSV',
    // 维护 导出Csv 字段，供当前前端状态或配置直接使用。
    exportCsv: '导出 CSV',
    // 维护 生成推荐 字段，供当前前端状态或配置直接使用。
    generateRecommended: '一键生成推荐模板',
    // 维护 绑定映射 字段，供当前前端状态或配置直接使用。
    bindMapping: '绑定打卡 ID',
    // 维护 租户编码 字段，供当前前端状态或配置直接使用。
    tenantCode: '公司编码',
    // 维护 租户名称 字段，供当前前端状态或配置直接使用。
    tenantName: '公司/教室名称',
    // 维护 contact名称 字段，供当前前端状态或配置直接使用。
    contactName: '联系人',
    // 维护 contact电话 字段，供当前前端状态或配置直接使用。
    contactPhone: '联系电话',
    // 维护 contact邮箱 字段，供当前前端状态或配置直接使用。
    contactEmail: '联系邮箱',
    // 维护 时区 字段，供当前前端状态或配置直接使用。
    timezone: '时区',
    // 维护 事业所编码 字段，供当前前端状态或配置直接使用。
    workplaceCode: '事业所编码',
    // 维护 事业所名称 字段，供当前前端状态或配置直接使用。
    workplaceName: '事业所名称',
    // 维护 地址 字段，供当前前端状态或配置直接使用。
    address: '地址',
    // 维护 电话 字段，供当前前端状态或配置直接使用。
    phone: '电话',
    // 维护 部门编码 字段，供当前前端状态或配置直接使用。
    departmentCode: '部门编码',
    // 维护 部门名称 字段，供当前前端状态或配置直接使用。
    departmentName: '部门名称',
    // 维护 全部部门 字段，供当前前端状态或配置直接使用。
    allDepartments: '全部部门',
    // 维护 事业所 字段，供当前前端状态或配置直接使用。
    workplace: '事业所',
    // 维护 全部事业所 字段，供当前前端状态或配置直接使用。
    allWorkplaces: '全部事业所',
    // 维护 sortOrder 字段，供当前前端状态或配置直接使用。
    sortOrder: '排序号',
    // 维护 员工No 字段，供当前前端状态或配置直接使用。
    employeeNo: '员工编号',
    // 维护 员工名称 字段，供当前前端状态或配置直接使用。
    employeeName: '员工姓名',
    // 维护 员工名称Kana 字段，供当前前端状态或配置直接使用。
    employeeNameKana: '员工假名',
    // 维护 employmentType 字段，供当前前端状态或配置直接使用。
    employmentType: '雇佣类型',
    // 维护 入社Date 字段，供当前前端状态或配置直接使用。
    hireDate: '入社日',
    // 维护 状态 字段，供当前前端状态或配置直接使用。
    status: '状态',
    // 维护 外部系统员工Id 字段，供当前前端状态或配置直接使用。
    externalEmployeeId: '外部打卡 ID',
    // 维护 外部系统员工No 字段，供当前前端状态或配置直接使用。
    externalEmployeeNo: '外部打卡编号',
    // 维护 sourceSystem 字段，供当前前端状态或配置直接使用。
    sourceSystem: '外部系统',
    // 维护 班次编码 字段，供当前前端状态或配置直接使用。
    shiftCode: '模板编码',
    // 维护 班次名称 字段，供当前前端状态或配置直接使用。
    shiftName: '模板名称',
    // 维护 班次Type 字段，供当前前端状态或配置直接使用。
    shiftType: '班次类型',
    // 维护 startTime 字段，供当前前端状态或配置直接使用。
    startTime: '开始时间',
    // 维护 endTime 字段，供当前前端状态或配置直接使用。
    endTime: '结束时间',
    // 维护 crossDay 字段，供当前前端状态或配置直接使用。
    crossDay: '跨日',
    // 维护 breakMinutes 字段，供当前前端状态或配置直接使用。
    breakMinutes: '休息分钟',
    // 维护 颜色 字段，供当前前端状态或配置直接使用。
    color: '颜色',
    // 维护 导入Title 字段，供当前前端状态或配置直接使用。
    importTitle: '员工 CSV 导入',
    // 维护 导入Placeholder 字段，供当前前端状态或配置直接使用。
    importPlaceholder: '粘贴 CSV 文本，表头需包含 employeeNo,employeeName,employeeNameKana,employmentType,workplaceCode,departmentCode,hireDate,email,phone',
    // 维护 empty事业所 字段，供当前前端状态或配置直接使用。
    emptyWorkplace: '还没有事业所，请先补事业所主数据。',
    // 维护 empty部门 字段，供当前前端状态或配置直接使用。
    emptyDepartment: '还没有部门，请先补部门主数据。',
    // 维护 empty员工 字段，供当前前端状态或配置直接使用。
    emptyEmployee: '还没有员工，请先新增员工或导入 CSV。',
    // 维护 empty班次 字段，供当前前端状态或配置直接使用。
    emptyShift: '还没有班次模板，可先新增或一键生成推荐模板。',
    // 维护 排班空标题 字段，供当前前端状态或配置直接使用。
    scheduleEmptyTitle: '这个月还没有可排班员工',
    // 维护 排班空说明 字段，供当前前端状态或配置直接使用。
    scheduleEmptyDescription: '请先准备员工和班次模板，或调整当前筛选后再进入排班。',
    // 维护 排班引导标题 字段，供当前前端状态或配置直接使用。
    scheduleLead: '先选模板，再点日期格子，就能把排班像贴便签一样贴到日历上。',
    // 维护 排班刷新 字段，供当前前端状态或配置直接使用。
    scheduleRefresh: '刷新排班',
    // 维护 排班月份 字段，供当前前端状态或配置直接使用。
    scheduleMonth: '排班月份',
    // 维护 排班关键字 字段，供当前前端状态或配置直接使用。
    scheduleKeyword: '员工检索',
    // 维护 排班关键字Hint 字段，供当前前端状态或配置直接使用。
    scheduleKeywordHint: '输入员工编号或姓名',
    // 维护 排班事业所筛选Hint 字段，供当前前端状态或配置直接使用。
    scheduleWorkplaceFilterHint: '全部事业所',
    // 维护 排班部门筛选Hint 字段，供当前前端状态或配置直接使用。
    scheduleDepartmentFilterHint: '全部部门',
    // 维护 只看未排班 字段，供当前前端状态或配置直接使用。
    scheduleOnlyUnassigned: '只看仍有缺口的员工',
    // 维护 批量排班入口 字段，供当前前端状态或配置直接使用。
    scheduleBatchOpen: '批量排班',
    // 维护 复制上周 字段，供当前前端状态或配置直接使用。
    scheduleCopyLastWeek: '复制上周',
    // 维护 复制上月 字段，供当前前端状态或配置直接使用。
    scheduleCopyLastMonth: '复制上月',
    // 维护 检查未排班 字段，供当前前端状态或配置直接使用。
    scheduleCheckUnassigned: '检查未排班',
    // 维护 导出排班 字段，供当前前端状态或配置直接使用。
    scheduleExport: '导出排班表',
    // 维护 排班员工列 字段，供当前前端状态或配置直接使用。
    scheduleEmployeeColumn: '员工 / 缺口',
    // 维护 星期日 字段，供当前前端状态或配置直接使用。
    weekdaySun: '周日',
    // 维护 星期一 字段，供当前前端状态或配置直接使用。
    weekdayMon: '周一',
    // 维护 星期二 字段，供当前前端状态或配置直接使用。
    weekdayTue: '周二',
    // 维护 星期三 字段，供当前前端状态或配置直接使用。
    weekdayWed: '周三',
    // 维护 星期四 字段，供当前前端状态或配置直接使用。
    weekdayThu: '周四',
    // 维护 星期五 字段，供当前前端状态或配置直接使用。
    weekdayFri: '周五',
    // 维护 星期六 字段，供当前前端状态或配置直接使用。
    weekdaySat: '周六',
    // 维护 元日 字段，供当前前端状态或配置直接使用。
    holidayNewYear: '元日',
    // 维护 成人之日 字段，供当前前端状态或配置直接使用。
    holidayComingOfAgeDay: '成人之日',
    // 维护 建国纪念日 字段，供当前前端状态或配置直接使用。
    holidayNationalFoundationDay: '建国纪念日',
    // 维护 天皇诞生日 字段，供当前前端状态或配置直接使用。
    holidayEmperorBirthday: '天皇诞生日',
    // 维护 春分之日 字段，供当前前端状态或配置直接使用。
    holidayVernalEquinox: '春分之日',
    // 维护 昭和之日 字段，供当前前端状态或配置直接使用。
    holidayShowaDay: '昭和之日',
    // 维护 宪法纪念日 字段，供当前前端状态或配置直接使用。
    holidayConstitutionMemorialDay: '宪法纪念日',
    // 维护 绿之日 字段，供当前前端状态或配置直接使用。
    holidayGreeneryDay: '绿之日',
    // 维护 儿童之日 字段，供当前前端状态或配置直接使用。
    holidayChildrensDay: '儿童之日',
    // 维护 海之日 字段，供当前前端状态或配置直接使用。
    holidayMarineDay: '海之日',
    // 维护 山之日 字段，供当前前端状态或配置直接使用。
    holidayMountainDay: '山之日',
    // 维护 敬老之日 字段，供当前前端状态或配置直接使用。
    holidayRespectForTheAgedDay: '敬老之日',
    // 维护 秋分之日 字段，供当前前端状态或配置直接使用。
    holidayAutumnalEquinox: '秋分之日',
    // 维护 体育之日 字段，供当前前端状态或配置直接使用。
    holidaySportsDay: '体育之日',
    // 维护 文化之日 字段，供当前前端状态或配置直接使用。
    holidayCultureDay: '文化之日',
    // 维护 勤劳感谢之日 字段，供当前前端状态或配置直接使用。
    holidayLaborThanksgivingDay: '勤劳感谢之日',
    // 维护 振替休日 字段，供当前前端状态或配置直接使用。
    holidaySubstitute: '振替休日',
    // 维护 国民之休日 字段，供当前前端状态或配置直接使用。
    holidayCitizensHoliday: '国民之休日',
    // 维护 未排班数量 字段，供当前前端状态或配置直接使用。
    scheduleUnassignedCount: '未排班 {count} 天',
    // 维护 未排班短文案 字段，供当前前端状态或配置直接使用。
    scheduleUnassignedShort: '未排班',
    // 维护 排班格提示 字段，供当前前端状态或配置直接使用。
    scheduleCellGuide: '点这里安排班次',
    // 维护 休息标签 字段，供当前前端状态或配置直接使用。
    scheduleRestLabel: '休息 / 无时间',
    // 维护 右侧标题 字段，供当前前端状态或配置直接使用。
    scheduleSideTitle: '排班助手',
    // 维护 右侧说明 字段，供当前前端状态或配置直接使用。
    scheduleSideHint: '右侧集中放模板、当前格说明、批量向导和缺口提醒，避免你在一页里来回找按钮。',
    // 维护 模板面板标题 字段，供当前前端状态或配置直接使用。
    scheduleTemplatePanelTitle: '班次模板面板',
    // 维护 已选模板提示 字段，供当前前端状态或配置直接使用。
    scheduleTemplatePicked: '当前将使用：{template}',
    // 维护 需先选模板提示 字段，供当前前端状态或配置直接使用。
    scheduleTemplateNeedPick: '请先在右侧选择一个班次模板，再点击日历格子。',
    // 维护 模板引导气泡标题 字段，供当前前端状态或配置直接使用。
    scheduleTemplateTipTitle: '先在这里选班次模板',
    // 维护 模板引导气泡正文 字段，供当前前端状态或配置直接使用。
    scheduleTemplateTipBody: '选择一个班次模板后，再返回左侧点击日历格子。',
    // 维护 模板引导气泡上下文 字段，供当前前端状态或配置直接使用。
    scheduleTemplateTipContext: '你刚刚点击了 {employee} 在 {date} 的格子。',
    // 维护 模板引导气泡关闭 字段，供当前前端状态或配置直接使用。
    scheduleTemplateTipClose: '关闭提示',
    // 维护 模板引导气泡锚点拖拽 字段，供当前前端状态或配置直接使用。
    scheduleTemplateTipAnchorDrag: '拖动箭头指向',
    // 维护 排班备注 字段，供当前前端状态或配置直接使用。
    scheduleRemark: '排班备注',
    // 维护 排班备注提示 字段，供当前前端状态或配置直接使用。
    scheduleRemarkHint: '例如：开月培训、临时顶班、半天会议',
    // 维护 当前选中标题 字段，供当前前端状态或配置直接使用。
    scheduleCurrentSelectionTitle: '当前格子',
    // 维护 当前选中文案 字段，供当前前端状态或配置直接使用。
    scheduleCurrentSelection: '你正在处理 {employee} 在 {date} 的排班。',
    // 维护 当前模板文案 字段，供当前前端状态或配置直接使用。
    scheduleCurrentTemplate: '当前班次：{template}',
    // 维护 删除当前排班 字段，供当前前端状态或配置直接使用。
    scheduleDeleteCurrent: '清空当前排班',
    // 维护 图例标题 字段，供当前前端状态或配置直接使用。
    scheduleLegendTitle: '图例说明',
    // 维护 图例空格子 字段，供当前前端状态或配置直接使用。
    scheduleLegendEmpty: '空白格子代表这天仍未安排班次',
    // 维护 图例已排班 字段，供当前前端状态或配置直接使用。
    scheduleLegendFilled: '彩色标签代表已安排班次',
    // 维护 图例当前选择 字段，供当前前端状态或配置直接使用。
    scheduleLegendSelected: '高亮边框代表你刚刚操作的格子',
    // 维护 批量向导标题 字段，供当前前端状态或配置直接使用。
    scheduleBatchTitle: '批量排班向导',
    // 维护 批量向导关闭 字段，供当前前端状态或配置直接使用。
    scheduleBatchClose: '收起向导',
    // 维护 批量向导说明 字段，供当前前端状态或配置直接使用。
    scheduleBatchLead: '批量排班分 5 步走，先选人、再选日期、再选模板，最后确认。',
    scheduleBatchStep1: '选择员工',
    scheduleBatchStep2: '选择日期',
    scheduleBatchStep3: '选择模板',
    scheduleBatchStep4: '检查范围',
    scheduleBatchStep5: '确认保存',
    scheduleBatchStep1Hint: '勾选这次要一起排班的员工。',
    scheduleBatchStep2Hint: '选择这次批量排班要覆盖的日期范围。',
    scheduleBatchStep3Hint: '选择要批量套用的班次模板。',
    scheduleBatchStep5Hint: '确认无误后再一次性保存，避免误覆盖整段排班。',
    scheduleBatchPreviewEmpty: '先把员工、日期和模板选好，这里就会告诉你这次会影响谁、影响多久。',
    scheduleBatchPreviewReady: '将给 {employees} 名员工在 {startDate} 到 {endDate} 之间套用 {template}。',
    scheduleSkipExisting: '遇到已有排班时直接跳过',
    scheduleOverwriteExisting: '允许覆盖已有排班',
    scheduleBatchConfirm: '确认批量排班',
    // 维护 替换排班确认动作 字段，供当前前端状态或配置直接使用。
    scheduleReplaceConfirmAction: '确认替换',
    scheduleBatchPrev: '上一步',
    scheduleBatchNext: '下一步',
    scheduleBatchConfirmDialog: '这次会批量写入当前所选员工和日期范围，确认继续吗？',
    scheduleDateStart: '开始日期',
    scheduleDateEnd: '结束日期',
    scheduleUnassignedPanelTitle: '仍需补排班的员工',
    scheduleOverwriteConfirm: '这一天已经排了 {current}，是否替换为 {next}？',
    scheduleDeleteConfirm: '确定清空当前排班吗？',
    scheduleCopyWeekConfirm: '将把当前月份视图按上周内容复制过来，确认继续吗？',
    scheduleCopyMonthConfirm: '将把当前月份视图按上月内容复制过来，确认继续吗？',
    scheduleToastSaved: '单日排班已保存',
    scheduleToastReplaced: '已有排班已替换',
    scheduleBatchResultToast: '批量排班完成：新增 {created}，覆盖 {updated}，跳过 {skipped}',
    scheduleCopyResultToast: '复制完成：新增 {created}，覆盖 {updated}，跳过 {skipped}',
    scheduleUnassignedChecked: '未排班检查已刷新',
    punchLead: '先把原始打卡事实接进来，再处理未匹配、失败和重复记录，后续日次计算才有依据。',
    punchRefresh: '刷新打卡',
    punchDateFrom: '开始日期',
    punchDateTo: '结束日期',
    punchEmployeeKeywordHint: '输入员工编号、姓名或外部打卡 ID',
    punchAllSources: '全部来源',
    punchAllStatuses: '全部状态',
    punchStatusProcessed: '已处理',
    punchStatusUnmatched: '未匹配',
    punchStatusError: '失败',
    punchStatusDuplicate: '重复',
    punchStatusIgnored: '已忽略',
    punchPageSize: '每页条数',
    punchPrevPage: '上一页',
    punchNextPage: '下一页',
    punchLastPage: '最后一页',
    punchPaginationNav: '打卡记录分页导航',
    punchPaginationSummary: '共 {total} 条',
    punchPaginationCurrent: '第 {page} / {totalPages} 页',
    punchPageJump: '跳到第几页',
    punchPageJumpPrefix: '跳转到 第',
    punchPageJumpSuffix: '页',
    punchPageJumpPlaceholder: '1 - {totalPages}',
    punchPageJumpSubmit: '加载',
    punchPageInvalid: '请输入正确页码数字',
    punchPageOutOfRange: '页码超出范围，请输入 1 到 {totalPages}',
    punchTime: '打卡时间',
    punchType: '打卡类型',
    punchDeviceName: '设备名称',
    punchUnmatchedLabel: '未匹配员工',
    punchManualTitle: '手动补录',
    punchManualHint: '员工忘记打卡时，可直接手动补一条原始打卡事实。',
    punchPickEmployee: '请选择员工',
    punchManualSubmit: '提交补录',
    punchImportTitle: 'CSV 导入',
    punchImportHint: '先预览，再正式导入，避免一次把错误数据整批带进来。',
    punchImportFileName: '文件名',
    punchImportPlaceholder: '表头需包含 externalEmployeeId,punchTime,punchType,sourceSystem,sourceEventId,deviceId,deviceName',
    punchPreviewImport: '预览导入',
    punchSubmitImport: '正式导入',
    punchPreviewSummary: '预览摘要',
    punchPreviewTotal: '总行数：{count}',
    punchPreviewReady: '可导入：{count}',
    punchPreviewUnmatched: '未匹配：{count}',
    punchPreviewError: '错误：{count}',
    punchDetailTitle: '当前记录详情',
    punchBindEmployee: '绑定到已有员工',
    punchBindAction: '确认绑定',
    punchIgnoreReason: '忽略原因',
    punchIgnoreAction: '忽略当前记录',
    punchReprocessAction: '重新处理',
    punchRawPayload: '查看原始 JSON',
    punchToastManualSaved: '手动补录已保存',
    punchToastPreviewReady: '导入预览已刷新',
    punchToastImported: '打卡导入已处理',
    punchToastBound: '未匹配记录已绑定到员工',
    punchToastIgnored: '当前打卡记录已忽略',
    punchToastReprocessed: '当前打卡记录已重新处理',
    dailyLead: '把排班与有效打卡组合起来，先产出每日结论，再把异常集中交给管理员处理。',
    dailyRefresh: '刷新日次',
    dailyRecalculateRange: '按当前筛选重算',
    dailyDateFrom: '开始日期',
    dailyDateTo: '结束日期',
    dailyEmployeeKeywordHint: '输入员工编号或姓名',
    dailyExceptionOnly: '只看异常',
    dailyStatusAll: '全部状态',
    dailySummaryNormal: '正常',
    dailySummaryLate: '迟到/早退',
    dailySummaryMissing: '缺卡',
    dailySummaryAbsence: '缺勤',
    dailyPageSize: '每页条数',
    dailyPrevPage: '上一页',
    dailyNextPage: '下一页',
    dailyLastPage: '最后一页',
    dailyPaginationNav: '日次结果分页导航',
    dailyPaginationSummary: '共 {total} 条',
    dailyPaginationCurrent: '第 {page} / {totalPages} 页',
    dailyPageJump: '跳到第几页',
    dailyPageJumpPrefix: '跳转到 第',
    dailyPageJumpSuffix: '页',
    dailyPageJumpPlaceholder: '1 - {totalPages}',
    dailyPageJumpSubmit: '加载',
    dailyJumpTo: '跳转到 第',
    dailyPageUnit: '页',
    dailyJumpAction: '加载',
    dailyTotalCount: '共 {total} 条',
    dailyPageInvalid: '请输入正确页码数字',
    dailyPageOutOfRange: '页码超出范围，请输入 1 到 {totalPages}',
    dailyWorkDate: '工作日',
    dailyScheduleLabel: '计划班次',
    dailyActualClockIn: '实际上班',
    dailyActualClockOut: '实际下班',
    dailyDetailTitle: '当前日次详情',
    dailyDetailHint: '当前查看 {date} 的排班、打卡、异常和计算过程。',
    dailyRecalculateOne: '重算当天',
    dailyScheduleSnapshot: '排班快照',
    dailyPunchSnapshot: '打卡快照',
    dailyExceptionList: '异常列表',
    dailyCalcSteps: '计算过程',
    dailyNoSelection: '请先从左侧列表选择一条日次结果。',
    dailyNoPunches: '当天没有有效打卡。',
    dailyNoExceptions: '当前日次没有异常。',
    dailyNoCalcSteps: '当前没有可展示的计算步骤。',
    dailyActualWorkMinutes: '实际工时分钟',
    dailyLateMinutes: '迟到分钟',
    dailyEarlyLeaveMinutes: '早退分钟',
    caseTitle: '异常处理',
    caseLead: '把异常日次整理成处理单，完成审批、回写和锁定，避免月结前结果反复漂移。',
    caseRefresh: '刷新处理单',
    caseSummaryPending: '待建单',
    caseSummaryReviewing: '审批中',
    caseSummaryApproved: '已通过',
    caseSummaryRejected: '已驳回',
    caseSummaryLocked: '已锁定',
    caseCurrentException: '当前异常',
    caseStatusLabel: '处理状态',
    caseUpdatedAt: '最近更新时间',
    caseStatusAll: '全部处理状态',
    caseStatusUnhandled: '待建单',
    caseStatusSubmitted: '已提交',
    caseStatusReturned: '已退回',
    caseStatusApproved: '已通过',
    caseStatusRejected: '已驳回',
    caseStatusLocked: '已锁定',
    caseMineOnly: '只看我负责的处理单',
    caseApplicant: '申请来源',
    caseApplicantSystem: '系统自动汇总',
    caseDetailTitle: '当前处理单详情',
    caseCreateTitle: '创建处理单',
    caseDetailLead: '确认异常原因、审批过程和最终回写结果。',
    caseCreateLead: '当前异常还没有处理单，请先补充原因和预期处理方式。',
    caseReasonCategory: '原因分类',
    caseReasonDeviceError: '设备异常',
    caseReasonManualConfirm: '人工确认',
    caseReasonOther: '其他原因',
    caseReasonText: '原因说明',
    caseExpectedResolution: '期望处理方式',
    caseCreateAction: '提交处理单',
    caseTimelineTitle: '处理时间线',
    caseApprovalTitle: '审批处理',
    dailyStatusFinal: '最终结果',
    caseKeepExceptionFlag: '仍保留异常标记',
    caseApprovalComment: '审批备注',
    caseApproveAction: '审批通过',
    caseReturnAction: '退回补充',
    caseRejectAction: '驳回处理单',
    caseLockAction: '锁定当前日次',
    caseUnlockAction: '解除锁定',
    casePageInvalid: '请输入正确页码数字',
    casePageOutOfRange: '页码超出范围，请输入 1 到 {totalPages}',
    caseToastCreated: '处理单已创建',
    caseToastApproved: '处理单已审批通过',
    caseToastReturned: '处理单已退回',
    caseToastRejected: '处理单已驳回',
    caseToastLocked: '当前日次已锁定',
    caseToastUnlocked: '当前日次已解除锁定',
    monthlyLead: '先把已锁定的日次结果按月聚合，再确认能否月结、反结和导出。',
    monthlyRefresh: '刷新月次',
    monthlyExport: '导出月次表',
    monthlyRecalculateRange: '按当前筛选重算',
    monthlySummaryOpen: '待汇总',
    monthlySummaryClosable: '可月结',
    monthlySummaryClosed: '已月结',
    monthlySummaryReopened: '已反结',
    monthlyYearMonth: '所属月份',
    monthlyEmployeeKeywordHint: '输入员工编号或姓名',
    searchAction: '搜索',
    monthlySearchAction: '搜索月次',
    monthlyCloseStatus: '月结状态',
    monthlyCloseStatusAll: '全部月结状态',
    monthlyCloseStatusOpen: '待汇总',
    monthlyCloseStatusClosable: '可月结',
    monthlyCloseStatusClosed: '已月结',
    monthlyCloseStatusReopened: '已反结',
    monthlyBlockedOnly: '只看仍有阻塞的月次',
    monthlyUpdatedAt: '最后更新时间',
    monthlyScheduledDays: '计划出勤天数',
    monthlyAttendanceDays: '实际出勤天数',
    monthlyExceptionDays: '异常天数',
    monthlyDetailTitle: '当前月次详情',
    monthlyDetailLead: '当前查看 {month} 的月次汇总、阻塞原因和操作记录。',
    monthlyBlockReasonCount: '阻塞原因数',
    monthlyMetricTitle: '月次统计快照',
    monthlyNormalDays: '正常天数',
    monthlyLateCount: '迟到次数',
    monthlyEarlyLeaveCount: '早退次数',
    monthlyMissingPunchCount: '缺卡次数',
    monthlyAbsenceCount: '缺勤次数',
    monthlyPaidLeaveDays: '有休天数',
    monthlyRestDays: '休息天数',
    monthlyBlockTitle: '月结阻塞项',
    monthlyNoBlock: '当前月次没有阻塞项，可以继续月结。',
    monthlyActionLogTitle: '月次动作留痕',
    monthlyNoActionLog: '当前月次还没有动作日志。',
    monthlyActionPanelTitle: '月次处理动作',
    monthlyCloseComment: '月结备注',
    monthlyReopenReason: '反结原因',
    monthlyRecalculateOne: '重算当月当前员工',
    monthlyCloseAction: '确认月结',
    monthlyReopenAction: '执行反结',
    monthlyPageInvalid: '请输入正确页码数字',
    monthlyPageOutOfRange: '页码超出范围，请输入 1 到 {totalPages}',
    monthlyToastRecalculated: '当前筛选范围月次已重算',
    monthlyToastOneRecalculated: '当前员工月次已重算',
    monthlyToastClosed: '当前范围月次已完成月结',
    monthlyToastReopened: '当前月次已反结',
    monthlyToastExported: '月次表已导出',
    emptyData: '当前没有可显示的数据。',
    dailyStatusNormal: '正常',
    dailyStatusLate: '迟到',
    dailyStatusEarlyLeave: '早退',
    dailyStatusMissingClockIn: '缺上班卡',
    dailyStatusMissingClockOut: '缺下班卡',
    dailyStatusAbsence: '缺勤',
    dailyStatusNoSchedule: '无排班打卡',
    dailyStatusHolidayWork: '休日出勤',
    dailyExceptionLevelWarn: '提醒',
    dailyExceptionLevelError: '错误',
    dailyToastRecalculated: '当前日次已重算',
    dailyToastRangeRecalculated: '当前筛选范围已重算',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'guide.tenant': '完成公司/教室信息后，初始化向导才能进入组织资料建设。',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'guide.workplace': '至少准备一个事业所，员工才能挂载归属地点。',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'guide.employee': '员工主数据是后续排班和打卡的基础。',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'guide.shiftTemplate': '班次模板准备完后，下一阶段排班才能快速复用。',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'guide.workRule': '每位员工都需要默认勤怠规则占位。',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'guide.schedule': '第二阶段开始处理排班表和排班向导。',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'guide.punch': '第三阶段开始导入或接收打卡记录。',
    // 维护 guide租户 字段，供当前前端状态或配置直接使用。
    guideTenant: '完成公司/教室信息后，初始化向导才能进入组织资料建设。',
    // 维护 guide事业所 字段，供当前前端状态或配置直接使用。
    guideWorkplace: '至少准备一个事业所，员工才能挂载归属地点。',
    // 维护 guide员工 字段，供当前前端状态或配置直接使用。
    guideEmployee: '员工主数据是后续排班和打卡的基础。',
    // 维护 guide班次模板 字段，供当前前端状态或配置直接使用。
    guideShiftTemplate: '班次模板准备完后，下一阶段排班才能快速复用。',
    // 维护 guide工时规则 字段，供当前前端状态或配置直接使用。
    guideWorkRule: '每位员工都需要默认勤怠规则占位。',
    // 维护 guideSchedule 字段，供当前前端状态或配置直接使用。
    guideSchedule: '第二阶段开始处理排班表和排班向导。',
    // 维护 guidePunch 字段，供当前前端状态或配置直接使用。
    guidePunch: '第三阶段开始导入或接收打卡记录。',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'wizard.tenant': '设置公司/教室信息',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'wizard.workplace': '添加事业所',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'wizard.employee': '添加员工',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'wizard.shiftTemplate': '创建班次模板',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'wizard.workRule': '确认默认勤怠规则',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'wizard.schedule': '开始排班',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'wizard.punch': '导入或接收打卡'
  },
  // 执行当前业务步骤，推进本行对应的 constants 处理。
  'ja-JP': {
    // 维护 appTitle 字段，供当前前端状态或配置直接使用。
    appTitle: '勤怠システム Phase 6',
    // 维护 appSubtitle 字段，供当前前端状态或配置直接使用。
    appSubtitle: '基本マスタ、シフト、打刻、日次、異常処理と Phase 6 の月次集計・月締めを一画面で連続して管理します。',
    // 维护 liveTag 字段，供当前前端状态或配置直接使用。
    liveTag: '中国語 / 日本語 / ワンクリック起動',
    // 维护 主题Switch 字段，供当前前端状态或配置直接使用。
    themeSwitch: 'テーマ切替',
    // 维护 主题AdminWorkbenchDark 字段，供当前前端状态或配置直接使用。
    themeAdminWorkbenchDark: '汎用管理ダーク',
    // 维护 主题考勤GlassDark 字段，供当前前端状态或配置直接使用。
    themeAttendanceGlassDark: '勤怠ガラスダーク',
    // 维护 主题LiquidGlass 字段，供当前前端状态或配置直接使用。
    themeLiquidGlass: 'リキッドガラスライト',
    // 维护 toastSaved 字段，供当前前端状态或配置直接使用。
    toastSaved: '保存しました',
    // 维护 toastDeleted 字段，供当前前端状态或配置直接使用。
    toastDeleted: '削除しました',
    // 维护 toastImported 字段，供当前前端状态或配置直接使用。
    toastImported: '取込を処理しました',
    // 维护 toastGenerated 字段，供当前前端状态或配置直接使用。
    toastGenerated: '推奨テンプレートを生成しました',
    // 维护 导航Wizard 字段，供当前前端状态或配置直接使用。
    navWizard: '初期化ウィザード',
    // 维护 导航事业所 字段，供当前前端状态或配置直接使用。
    navWorkplace: '事業所',
    // 维护 导航部门 字段，供当前前端状态或配置直接使用。
    navDepartment: '部署',
    // 维护 导航员工 字段，供当前前端状态或配置直接使用。
    navEmployee: '社員',
    // 维护 导航班次 字段，供当前前端状态或配置直接使用。
    navShift: 'シフトテンプレート',
    // 维护 导航排班 字段，供当前前端状态或配置直接使用。
    navSchedule: 'シフト作成',
    // 维护 导航打卡 字段，供当前前端状态或配置直接使用。
    navPunch: '打刻記録',
    // 维护 导航日次 字段，供当前前端状态或配置直接使用。
    navDaily: '日次結果',
    // 维护 导航异常处理 字段，供当前前端状态或配置直接使用。
    navCase: '異常処理',
    // 维护 导航月次汇总 字段，供当前前端状态或配置直接使用。
    navMonthly: '月次集計',
    // 维护 workspaceSidebarTitle 字段，供当前前端状态或配置直接使用。
    workspaceSidebarTitle: '機能ナビ',
    // 维护 workspaceSidebarHint 字段，供当前前端状态或配置直接使用。
    workspaceSidebarHint: '左側で処理対象モジュールを選択し、右側には対応ワークスペースのみを表示します。',
    // 维护 workspace状态 字段，供当前前端状态或配置直接使用。
    workspaceStatus: '現在のモジュール',
    // 维护 sectionWizardHint 字段，供当前前端状态或配置直接使用。
    sectionWizardHint: 'Phase 1 の進捗、次アクション、会社基本情報をまとめて確認します。',
    // 维护 section事业所Hint 字段，供当前前端状态或配置直接使用。
    sectionWorkplaceHint: '事業所マスタを整備し、社員と部署の所属起点を管理します。',
    // 维护 section部门Hint 字段，供当前前端状态或配置直接使用。
    sectionDepartmentHint: '組織部署を整備し、社員主データと後続シフトの紐付けに備えます。',
    // 维护 section员工Hint 字段，供当前前端状态或配置直接使用。
    sectionEmployeeHint: '社員主データ、CSV 取込 / 出力、打刻 ID 連携を処理します。',
    // 维护 section班次Hint 字段，供当前前端状态或配置直接使用。
    sectionShiftHint: 'シフトテンプレートを整備し、次フェーズのシフト作成に備えます。',
    // 维护 section排班Hint 字段，供当前前端状态或配置直接使用。
    sectionScheduleHint: 'カレンダー感覚でシフトを並べ、未配置の穴をすぐ確認できます。',
    // 维护 section打卡Hint 字段，供当前前端状态或配置直接使用。
    sectionPunchHint: 'Phase 3 で実打刻ログを受け取り、未紐付け・失敗・無視データを整理します。',
    // 维护 section日次Hint 字段，供当前前端状态或配置直接使用。
    sectionDailyHint: 'Phase 4 でシフトと打刻を日次結果へ計算し、異常をまとめて処理します。',
    // 维护 section异常处理Hint 字段，供当前前端状态或配置直接使用。
    sectionCaseHint: 'Phase 5 で異常を処理票へまとめ、承認・反映・ロックまでを閉じます。',
    // 维护 section月次Hint 字段，供当前前端状态或配置直接使用。
    sectionMonthlyHint: 'Phase 6 で日次結果を月次へ集計し、月締め・再オープン・出力までを行います。',
    // 维护 wizardTitle 字段，供当前前端状态或配置直接使用。
    wizardTitle: '初期化ウィザード',
    // 维护 wizardHint 字段，供当前前端状态或配置直接使用。
    wizardHint: '基本マスタ、シフト、打刻、日次、異常承認は接続済みで、現在は Phase 6 の月次集計と月締めクローズへ進んでいます。',
    // 维护 nextAction 字段，供当前前端状态或配置直接使用。
    nextAction: '次の推奨アクション',
    // 维护 phaseLocked 字段，供当前前端状态或配置直接使用。
    phaseLocked: '次フェーズで公開',
    // 维护 状态Completed 字段，供当前前端状态或配置直接使用。
    statusCompleted: '完了',
    // 维护 状态NeedsAction 字段，供当前前端状态或配置直接使用。
    statusNeedsAction: '要対応',
    // 维护 事业所Title 字段，供当前前端状态或配置直接使用。
    workplaceTitle: '事業所管理',
    // 维护 部门Title 字段，供当前前端状态或配置直接使用。
    departmentTitle: '部署管理',
    // 维护 员工Title 字段，供当前前端状态或配置直接使用。
    employeeTitle: '社員管理',
    // 维护 班次Title 字段，供当前前端状态或配置直接使用。
    shiftTitle: 'シフトテンプレート',
    // 维护 排班Title 字段，供当前前端状态或配置直接使用。
    scheduleTitle: 'シフト作成',
    // 维护 打卡Title 字段，供当前前端状态或配置直接使用。
    punchTitle: '打刻記録',
    // 维护 日次Title 字段，供当前前端状态或配置直接使用。
    dailyTitle: '日次結果',
    // 维护 月次Title 字段，供当前前端状态或配置直接使用。
    monthlyTitle: '月次集計',
    // 维护 异常处理Title 字段，供当前前端状态或配置直接使用。
    caseTitle: '異常処理',
    // 维护 保存 字段，供当前前端状态或配置直接使用。
    save: '保存',
    // 维护 新增 字段，供当前前端状态或配置直接使用。
    create: '追加',
    // 维护 删除 字段，供当前前端状态或配置直接使用。
    delete: '削除',
    // 维护 进入部门 字段，供当前前端状态或配置直接使用。
    jumpDepartment: '部署へ',
    // 维护 查看员工 字段，供当前前端状态或配置直接使用。
    jumpEmployee: '社員へ',
    // 维护 查看排班 字段，供当前前端状态或配置直接使用。
    jumpSchedule: 'シフトへ',
    // 维护 当前事业所筛选 字段，供当前前端状态或配置直接使用。
    currentWorkplaceFilter: '現在の事業所：{name}',
    // 维护 查看全部部门 字段，供当前前端状态或配置直接使用。
    showAllDepartments: '全部署を表示',
    // 维护 取消 字段，供当前前端状态或配置直接使用。
    cancel: 'キャンセル',
    // 维护 确认删除标题 字段，供当前前端状态或配置直接使用。
    confirmDeleteTitle: '削除確認',
    // 维护 确认删除动作 字段，供当前前端状态或配置直接使用。
    confirmDeleteAction: '削除する',
    // 维护 具名删除确认文案 字段，供当前前端状态或配置直接使用。
    confirmDeleteMessageNamed: '{target}「{name}」を削除しますか？この操作は取り消せません。',
    // 维护 泛化删除确认文案 字段，供当前前端状态或配置直接使用。
    confirmDeleteMessageUnnamed: '現在の{target}を削除しますか？この操作は取り消せません。',
    // 维护 导入Csv 字段，供当前前端状态或配置直接使用。
    importCsv: 'CSV 取込',
    // 维护 导出Csv 字段，供当前前端状态或配置直接使用。
    exportCsv: 'CSV 出力',
    // 维护 生成推荐 字段，供当前前端状态或配置直接使用。
    generateRecommended: '推奨テンプレート生成',
    // 维护 绑定映射 字段，供当前前端状态或配置直接使用。
    bindMapping: '打刻 ID 連携',
    // 维护 租户编码 字段，供当前前端状态或配置直接使用。
    tenantCode: '会社コード',
    // 维护 租户名称 字段，供当前前端状态或配置直接使用。
    tenantName: '会社 / 教室名',
    // 维护 contact名称 字段，供当前前端状态或配置直接使用。
    contactName: '担当者',
    // 维护 contact电话 字段，供当前前端状态或配置直接使用。
    contactPhone: '電話番号',
    // 维护 contact邮箱 字段，供当前前端状态或配置直接使用。
    contactEmail: 'メール',
    // 维护 时区 字段，供当前前端状态或配置直接使用。
    timezone: 'タイムゾーン',
    // 维护 事业所编码 字段，供当前前端状态或配置直接使用。
    workplaceCode: '事業所コード',
    // 维护 事业所名称 字段，供当前前端状态或配置直接使用。
    workplaceName: '事業所名',
    // 维护 地址 字段，供当前前端状态或配置直接使用。
    address: '住所',
    // 维护 电话 字段，供当前前端状态或配置直接使用。
    phone: '電話',
    // 维护 部门编码 字段，供当前前端状态或配置直接使用。
    departmentCode: '部署コード',
    // 维护 部门名称 字段，供当前前端状态或配置直接使用。
    departmentName: '部署名',
    // 维护 全部部门 字段，供当前前端状态或配置直接使用。
    allDepartments: '全部署',
    // 维护 事业所 字段，供当前前端状态或配置直接使用。
    workplace: '事業所',
    // 维护 全部事业所 字段，供当前前端状态或配置直接使用。
    allWorkplaces: '全事業所',
    // 维护 sortOrder 字段，供当前前端状态或配置直接使用。
    sortOrder: '表示順',
    // 维护 员工No 字段，供当前前端状态或配置直接使用。
    employeeNo: '社員番号',
    // 维护 员工名称 字段，供当前前端状态或配置直接使用。
    employeeName: '社員名',
    // 维护 员工名称Kana 字段，供当前前端状态或配置直接使用。
    employeeNameKana: '社員カナ',
    // 维护 employmentType 字段，供当前前端状态或配置直接使用。
    employmentType: '雇用区分',
    // 维护 入社Date 字段，供当前前端状态或配置直接使用。
    hireDate: '入社日',
    // 维护 状态 字段，供当前前端状态或配置直接使用。
    status: '状態',
    // 维护 外部系统员工Id 字段，供当前前端状态或配置直接使用。
    externalEmployeeId: '外部打刻 ID',
    // 维护 外部系统员工No 字段，供当前前端状态或配置直接使用。
    externalEmployeeNo: '外部打刻番号',
    // 维护 sourceSystem 字段，供当前前端状态或配置直接使用。
    sourceSystem: '外部システム',
    // 维护 班次编码 字段，供当前前端状态或配置直接使用。
    shiftCode: 'テンプレートコード',
    // 维护 班次名称 字段，供当前前端状态或配置直接使用。
    shiftName: 'テンプレート名',
    // 维护 班次Type 字段，供当前前端状态或配置直接使用。
    shiftType: 'シフト区分',
    // 维护 startTime 字段，供当前前端状态或配置直接使用。
    startTime: '開始時刻',
    // 维护 endTime 字段，供当前前端状态或配置直接使用。
    endTime: '終了時刻',
    // 维护 crossDay 字段，供当前前端状态或配置直接使用。
    crossDay: '跨日',
    // 维护 breakMinutes 字段，供当前前端状态或配置直接使用。
    breakMinutes: '休憩分',
    // 维护 颜色 字段，供当前前端状态或配置直接使用。
    color: 'カラー',
    // 维护 导入Title 字段，供当前前端状态或配置直接使用。
    importTitle: '社員 CSV 取込',
    // 维护 导入Placeholder 字段，供当前前端状态或配置直接使用。
    importPlaceholder: 'employeeNo,employeeName,employeeNameKana,employmentType,workplaceCode,departmentCode,hireDate,email,phone のヘッダー付き CSV を貼り付けてください。',
    // 维护 empty事业所 字段，供当前前端状态或配置直接使用。
    emptyWorkplace: '事業所が未登録です。先に事業所マスタを追加してください。',
    // 维护 empty部门 字段，供当前前端状态或配置直接使用。
    emptyDepartment: '部署が未登録です。先に部署マスタを追加してください。',
    // 维护 empty员工 字段，供当前前端状态或配置直接使用。
    emptyEmployee: '社員が未登録です。手入力または CSV 取込を実行してください。',
    // 维护 empty班次 字段，供当前前端状态或配置直接使用。
    emptyShift: 'テンプレートが未登録です。手入力または推奨テンプレート生成を実行してください。',
    scheduleEmptyTitle: '今月まだシフト対象の社員がいません',
    scheduleEmptyDescription: '社員とテンプレートを先に整えるか、現在の絞り込み条件を見直してください。',
    scheduleLead: '先にテンプレートを選び、次に日付セルを押すだけで、付箋を貼る感覚でシフトを配置できます。',
    scheduleRefresh: 'シフト再読込',
    scheduleMonth: '対象月',
    scheduleKeyword: '社員検索',
    scheduleKeywordHint: '社員番号または氏名で絞り込み',
    scheduleWorkplaceFilterHint: 'すべての事業所',
    scheduleDepartmentFilterHint: 'すべての部署',
    scheduleOnlyUnassigned: '未配置が残る社員だけを見る',
    scheduleBatchOpen: '一括シフト',
    scheduleCopyLastWeek: '前週をコピー',
    scheduleCopyLastMonth: '前月をコピー',
    scheduleCheckUnassigned: '未配置チェック',
    scheduleExport: 'シフト表を出力',
    scheduleEmployeeColumn: '社員 / 未配置',
    weekdaySun: '日',
    weekdayMon: '月',
    weekdayTue: '火',
    weekdayWed: '水',
    weekdayThu: '木',
    weekdayFri: '金',
    weekdaySat: '土',
    holidayNewYear: '元日',
    holidayComingOfAgeDay: '成人の日',
    holidayNationalFoundationDay: '建国記念の日',
    holidayEmperorBirthday: '天皇誕生日',
    holidayVernalEquinox: '春分の日',
    holidayShowaDay: '昭和の日',
    holidayConstitutionMemorialDay: '憲法記念日',
    holidayGreeneryDay: 'みどりの日',
    holidayChildrensDay: 'こどもの日',
    holidayMarineDay: '海の日',
    holidayMountainDay: '山の日',
    holidayRespectForTheAgedDay: '敬老の日',
    holidayAutumnalEquinox: '秋分の日',
    holidaySportsDay: 'スポーツの日',
    holidayCultureDay: '文化の日',
    holidayLaborThanksgivingDay: '勤労感謝の日',
    holidaySubstitute: '振替休日',
    holidayCitizensHoliday: '国民の休日',
    scheduleUnassignedCount: '未配置 {count} 日',
    scheduleUnassignedShort: '未配置',
    scheduleCellGuide: 'ここを押して配置',
    scheduleRestLabel: '休み / 時間なし',
    scheduleSideTitle: 'シフトアシスタント',
    scheduleSideHint: '右側にテンプレート、現在セル、まとめて配置する向けガイド、未配置警告を集約します。',
    scheduleTemplatePanelTitle: 'テンプレートパネル',
    scheduleTemplatePicked: '現在使うテンプレート：{template}',
    scheduleTemplateNeedPick: '先に右側でテンプレートを選び、その後でカレンダーセルを押してください。',
    scheduleTemplateTipTitle: '先にここでテンプレートを選択',
    scheduleTemplateTipBody: 'テンプレートを 1 つ選んでから、左側のカレンダーセルをもう一度押してください。',
    scheduleTemplateTipContext: '{employee} の {date} をクリックしました。',
    scheduleTemplateTipClose: 'ガイドを閉じる',
    scheduleTemplateTipAnchorDrag: '矢印の指し先を移動',
    scheduleRemark: 'シフトメモ',
    scheduleRemarkHint: '例：月初研修、臨時ヘルプ、午前会議',
    scheduleCurrentSelectionTitle: '現在のセル',
    scheduleCurrentSelection: '{employee} の {date} を編集中です。',
    scheduleCurrentTemplate: '現在のシフト：{template}',
    scheduleDeleteCurrent: '現在のシフトを消す',
    scheduleLegendTitle: '見方',
    scheduleLegendEmpty: '淡い色の空セルはまだ未配置です',
    scheduleLegendFilled: '色付きラベルは配置済みシフトです',
    scheduleLegendSelected: '強調枠は今あなたが触っているセルです',
    scheduleBatchTitle: '一括シフトウィザード',
    scheduleBatchClose: '閉じる',
    scheduleBatchLead: '一括シフトは 5 ステップで進め、誰に・いつ・何を入れるかを最後に確認します。',
    scheduleBatchStep1: '社員選択',
    scheduleBatchStep2: '日付選択',
    scheduleBatchStep3: 'テンプレート選択',
    scheduleBatchStep4: '範囲確認',
    scheduleBatchStep5: '保存確認',
    scheduleBatchStep1Hint: '今回まとめて配置する社員を選んでください。',
    scheduleBatchStep2Hint: '今回まとめて配置する日付範囲を選んでください。',
    scheduleBatchStep3Hint: '一括で適用するシフトテンプレートを選んでください。',
    scheduleBatchStep5Hint: '問題なければ最後にまとめて保存します。',
    scheduleBatchPreviewEmpty: '社員、日付、テンプレートを選ぶと、ここに今回の影響範囲が表示されます。',
    scheduleBatchPreviewReady: '{employees} 名の社員に対して {startDate} から {endDate} まで {template} を適用します。',
    scheduleSkipExisting: '既存シフトがある日はスキップする',
    scheduleOverwriteExisting: '既存シフトを上書きしてよい',
    scheduleBatchConfirm: '一括シフトを保存',
    // 维护 替换排班确认动作 字段，供当前前端状态或配置直接使用。
    scheduleReplaceConfirmAction: '置き換える',
    scheduleBatchPrev: '前へ',
    scheduleBatchNext: '次へ',
    scheduleBatchConfirmDialog: '現在の選択内容で一括シフトを保存します。続けますか？',
    scheduleDateStart: '開始日',
    scheduleDateEnd: '終了日',
    scheduleUnassignedPanelTitle: 'まだ埋まっていない社員',
    scheduleOverwriteConfirm: 'この日はすでに {current} が入っています。{next} に置き換えますか？',
    scheduleDeleteConfirm: '現在のシフトを削除してよいですか？',
    scheduleCopyWeekConfirm: '今見ている月の範囲へ前週の内容をコピーします。続けますか？',
    scheduleCopyMonthConfirm: '今見ている月の範囲へ前月の内容をコピーします。続けますか？',
    scheduleToastSaved: '単日シフトを保存しました',
    scheduleToastReplaced: '既存シフトを置き換えました',
    scheduleBatchResultToast: '一括シフト完了：新規 {created}、上書き {updated}、スキップ {skipped}',
    scheduleCopyResultToast: 'コピー完了：新規 {created}、上書き {updated}、スキップ {skipped}',
    scheduleUnassignedChecked: '未配置チェックを更新しました',
    punchLead: 'まず原始打刻事実を受け取り、未紐付け・失敗・重複を整理してから日次計算へ進みます。',
    punchRefresh: '打刻更新',
    punchDateFrom: '開始日',
    punchDateTo: '終了日',
    punchEmployeeKeywordHint: '社員番号、氏名、外部打刻 ID で検索',
    punchAllSources: '全ソース',
    punchAllStatuses: '全状態',
    punchStatusProcessed: '処理済み',
    punchStatusUnmatched: '未紐付け',
    punchStatusError: '失敗',
    punchStatusDuplicate: '重複',
    punchStatusIgnored: '無視済み',
    punchPageSize: '1ページ件数',
    punchPrevPage: '前へ',
    punchNextPage: '次へ',
    punchLastPage: '最後のページ',
    punchPaginationNav: '打刻記録のページ切替',
    punchPaginationSummary: '合計 {total} 件',
    punchPaginationCurrent: '{page} / {totalPages} ページ',
    punchPageJump: 'ページ指定',
    punchPageJumpPrefix: '第',
    punchPageJumpSuffix: 'ページへ',
    punchPageJumpPlaceholder: '1 - {totalPages}',
    punchPageJumpSubmit: '読み込む',
    punchPageInvalid: '正しいページ番号を入力してください',
    punchPageOutOfRange: 'ページ番号は 1 から {totalPages} の範囲で入力してください',
    punchTime: '打刻時刻',
    punchType: '打刻種別',
    punchDeviceName: '端末名',
    punchUnmatchedLabel: '未紐付け',
    punchManualTitle: '手動補録',
    punchManualHint: '社員が打刻を忘れた場合でも、原始打刻事実としてそのまま補録できます。',
    punchPickEmployee: '社員を選択してください',
    punchManualSubmit: '補録を保存',
    punchImportTitle: 'CSV 取込',
    punchImportHint: '先にプレビューしてから正式取込し、誤データの一括流入を防ぎます。',
    punchImportFileName: 'ファイル名',
    punchImportPlaceholder: 'ヘッダー: externalEmployeeId,punchTime,punchType,sourceSystem,sourceEventId,deviceId,deviceName',
    punchPreviewImport: '取込プレビュー',
    punchSubmitImport: '正式取込',
    punchPreviewSummary: 'プレビュー集計',
    punchPreviewTotal: '総行数：{count}',
    punchPreviewReady: '取込可能：{count}',
    punchPreviewUnmatched: '未紐付け：{count}',
    punchPreviewError: 'エラー：{count}',
    punchDetailTitle: '現在の記録詳細',
    punchBindEmployee: '既存社員へ紐付け',
    punchBindAction: '社員に紐付ける',
    punchIgnoreReason: '無視理由',
    punchIgnoreAction: 'この記録を無視',
    punchReprocessAction: '再処理',
    punchRawPayload: '原始 JSON を表示',
    punchToastManualSaved: '手動補録を保存しました',
    punchToastPreviewReady: '取込プレビューを更新しました',
    punchToastImported: '打刻取込を処理しました',
    punchToastBound: '未紐付け記録を社員へ紐付けました',
    punchToastIgnored: '現在の打刻記録を無視しました',
    punchToastReprocessed: '現在の打刻記録を再処理しました',
    dailyLead: 'シフトと有効打刻を組み合わせて、まず日次結論を作り、異常を管理者へ集約します。',
    dailyRefresh: '日次更新',
    dailyRecalculateRange: '現在の絞込で再計算',
    dailyDateFrom: '開始日',
    dailyDateTo: '終了日',
    dailyEmployeeKeywordHint: '社員番号または氏名で検索',
    dailyExceptionOnly: '異常のみ表示',
    dailyStatusAll: '全状態',
    dailySummaryNormal: '正常',
    dailySummaryLate: '遅刻 / 早退',
    dailySummaryMissing: '打刻不足',
    dailySummaryAbsence: '欠勤',
    dailyPageSize: '1ページ件数',
    dailyPrevPage: '前へ',
    dailyNextPage: '次へ',
    dailyLastPage: '最後のページ',
    dailyPaginationNav: '日次結果のページ切替',
    dailyPaginationSummary: '合計 {total} 件',
    dailyPaginationCurrent: '{page} / {totalPages} ページ',
    dailyPageJump: 'ページ指定',
    dailyPageJumpPrefix: '第',
    dailyPageJumpSuffix: 'ページへ',
    dailyPageJumpPlaceholder: '1 - {totalPages}',
    dailyPageJumpSubmit: '読み込む',
    dailyJumpTo: '第',
    dailyPageUnit: 'ページ',
    dailyJumpAction: '読み込む',
    dailyTotalCount: '合計 {total} 件',
    dailyPageInvalid: '正しいページ番号を入力してください',
    dailyPageOutOfRange: 'ページ番号は 1 から {totalPages} の範囲で入力してください',
    dailyWorkDate: '勤務日',
    dailyScheduleLabel: '予定シフト',
    dailyActualClockIn: '実上番',
    dailyActualClockOut: '実下番',
    dailyDetailTitle: '現在の日次詳細',
    dailyDetailHint: '{date} のシフト、打刻、異常、計算過程を確認します。',
    dailyRecalculateOne: '当日を再計算',
    dailyScheduleSnapshot: 'シフトスナップショット',
    dailyPunchSnapshot: '打刻スナップショット',
    dailyExceptionList: '異常一覧',
    dailyCalcSteps: '計算過程',
    dailyNoSelection: '左側の日次一覧から 1 件選択してください。',
    dailyNoPunches: '当日に有効打刻はありません。',
    dailyNoExceptions: '現在の日次に異常はありません。',
    dailyNoCalcSteps: '表示できる計算ステップはありません。',
    dailyActualWorkMinutes: '実働分',
    dailyLateMinutes: '遅刻分',
    dailyEarlyLeaveMinutes: '早退分',
    caseLead: '異常日次を処理票へまとめ、承認、反映、ロックまで進めて月次前の揺れを止めます。',
    caseRefresh: '処理票を更新',
    caseSummaryPending: '未起票',
    caseSummaryReviewing: '承認中',
    caseSummaryApproved: '承認済み',
    caseSummaryRejected: '却下済み',
    caseSummaryLocked: 'ロック済み',
    caseCurrentException: '現在の異常',
    caseStatusLabel: '処理状態',
    caseUpdatedAt: '最終更新',
    caseStatusAll: '全処理状態',
    caseStatusUnhandled: '未起票',
    caseStatusSubmitted: '申請済み',
    caseStatusReturned: '差戻し',
    caseStatusApproved: '承認済み',
    caseStatusRejected: '却下済み',
    caseStatusLocked: 'ロック済み',
    caseMineOnly: '自分担当の処理票のみ',
    caseApplicant: '起票元',
    caseApplicantSystem: 'システム自動集約',
    caseDetailTitle: '現在の処理票詳細',
    caseCreateTitle: '処理票を作成',
    caseDetailLead: '異常理由、承認経過、最終反映結果を確認します。',
    caseCreateLead: '現在の異常にはまだ処理票がありません。先に理由と処理方針を登録してください。',
    caseReasonCategory: '理由区分',
    caseReasonDeviceError: '機器異常',
    caseReasonManualConfirm: '手動確認',
    caseReasonOther: 'その他',
    caseReasonText: '理由メモ',
    caseExpectedResolution: '想定処理',
    caseCreateAction: '処理票を提出',
    caseTimelineTitle: '処理タイムライン',
    caseApprovalTitle: '承認処理',
    dailyStatusFinal: '最終結果',
    caseKeepExceptionFlag: '異常フラグを維持',
    caseApprovalComment: '承認コメント',
    caseApproveAction: '承認する',
    caseReturnAction: '差し戻す',
    caseRejectAction: '却下する',
    caseLockAction: 'この日次をロック',
    caseUnlockAction: 'ロック解除',
    casePageInvalid: '正しいページ番号を入力してください',
    casePageOutOfRange: 'ページ番号は 1 から {totalPages} の範囲で入力してください',
    caseToastCreated: '処理票を作成しました',
    caseToastApproved: '処理票を承認しました',
    caseToastReturned: '処理票を差し戻しました',
    caseToastRejected: '処理票を却下しました',
    caseToastLocked: '現在の日次をロックしました',
    caseToastUnlocked: '現在の日次のロックを解除しました',
    monthlyLead: 'ロック済みの日次結果を月単位で集計し、月締め・再オープン・出力へつなげます。',
    monthlyRefresh: '月次を更新',
    monthlyExport: '月次表を出力',
    monthlyRecalculateRange: '現在の条件で再集計',
    monthlySummaryOpen: '未集計',
    monthlySummaryClosable: '月締め可能',
    monthlySummaryClosed: '月締め済み',
    monthlySummaryReopened: '再オープン済み',
    monthlyYearMonth: '対象月',
    monthlyEmployeeKeywordHint: '社員番号または氏名を入力',
    searchAction: '検索',
    monthlySearchAction: '月次を検索',
    monthlyCloseStatus: '月締め状態',
    monthlyCloseStatusAll: 'すべての月締め状態',
    monthlyCloseStatusOpen: '未集計',
    monthlyCloseStatusClosable: '月締め可能',
    monthlyCloseStatusClosed: '月締め済み',
    monthlyCloseStatusReopened: '再オープン済み',
    monthlyBlockedOnly: '阻塞が残る月次だけ表示',
    monthlyUpdatedAt: '最終更新日時',
    monthlyScheduledDays: '予定出勤日数',
    monthlyAttendanceDays: '実出勤日数',
    monthlyExceptionDays: '異常日数',
    monthlyDetailTitle: '現在の月次詳細',
    monthlyDetailLead: '{month} の月次集計、阻塞理由、操作履歴を確認します。',
    monthlyBlockReasonCount: '阻塞件数',
    monthlyMetricTitle: '月次集計スナップショット',
    monthlyNormalDays: '正常日数',
    monthlyLateCount: '遅刻回数',
    monthlyEarlyLeaveCount: '早退回数',
    monthlyMissingPunchCount: '打刻不足回数',
    monthlyAbsenceCount: '欠勤回数',
    monthlyPaidLeaveDays: '有休日数',
    monthlyRestDays: '休日日数',
    monthlyBlockTitle: '月締め阻塞項目',
    monthlyNoBlock: 'この月次には阻塞がなく、そのまま月締めへ進めます。',
    monthlyActionLogTitle: '月次アクション履歴',
    monthlyNoActionLog: 'この月次にはまだアクション履歴がありません。',
    monthlyActionPanelTitle: '月次処理アクション',
    monthlyCloseComment: '月締めコメント',
    monthlyReopenReason: '再オープン理由',
    monthlyRecalculateOne: '現在社員の当月を再集計',
    monthlyCloseAction: '月締め確定',
    monthlyReopenAction: '再オープン',
    monthlyPageInvalid: '正しいページ番号を入力してください',
    monthlyPageOutOfRange: 'ページ番号は 1 から {totalPages} の範囲で入力してください',
    monthlyToastRecalculated: '現在の条件範囲の月次を再集計しました',
    monthlyToastOneRecalculated: '現在社員の月次を再集計しました',
    monthlyToastClosed: '現在範囲の月次を月締めしました',
    monthlyToastReopened: '現在の月次を再オープンしました',
    monthlyToastExported: '月次表を出力しました',
    emptyData: '表示できるデータがありません。',
    dailyStatusNormal: '正常',
    dailyStatusLate: '遅刻',
    dailyStatusEarlyLeave: '早退',
    dailyStatusMissingClockIn: '出勤打刻不足',
    dailyStatusMissingClockOut: '退勤打刻不足',
    dailyStatusAbsence: '欠勤',
    dailyStatusNoSchedule: '無シフト打刻',
    dailyStatusHolidayWork: '休日出勤',
    dailyExceptionLevelWarn: '注意',
    dailyExceptionLevelError: 'エラー',
    dailyToastRecalculated: '現在の日次を再計算しました',
    dailyToastRangeRecalculated: '現在の絞込範囲を再計算しました',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'guide.tenant': '会社 / 教室情報を登録すると組織マスタ準備に進めます。',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'guide.workplace': '社員を所属させるため、少なくとも 1 件の事業所が必要です。',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'guide.employee': '社員マスタは今後のシフト、打刻、勤怠計算の基礎です。',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'guide.shiftTemplate': 'テンプレートが揃うと次フェーズのシフト作成が速くなります。',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'guide.workRule': '各社員にデフォルト勤怠ルールのプレースホルダが必要です。',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'guide.schedule': 'Phase 2 でシフト表と作成ウィザードに進みます。',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'guide.punch': 'Phase 3 で打刻ログ取込と連携を開始します。',
    // 维护 guide租户 字段，供当前前端状态或配置直接使用。
    guideTenant: '会社 / 教室情報を登録すると組織マスタ準備に進めます。',
    // 维护 guide事业所 字段，供当前前端状态或配置直接使用。
    guideWorkplace: '社員を所属させるため、少なくとも 1 件の事業所が必要です。',
    // 维护 guide员工 字段，供当前前端状态或配置直接使用。
    guideEmployee: '社員マスタは今後のシフト、打刻、勤怠計算の基礎です。',
    // 维护 guide班次模板 字段，供当前前端状态或配置直接使用。
    guideShiftTemplate: 'テンプレートが揃うと次フェーズのシフト作成が速くなります。',
    // 维护 guide工时规则 字段，供当前前端状态或配置直接使用。
    guideWorkRule: '各社員にデフォルト勤怠ルールのプレースホルダが必要です。',
    // 维护 guideSchedule 字段，供当前前端状态或配置直接使用。
    guideSchedule: 'Phase 2 でシフト表と作成ウィザードに進みます。',
    // 维护 guidePunch 字段，供当前前端状态或配置直接使用。
    guidePunch: 'Phase 3 で打刻ログ取込と連携を開始します。',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'wizard.tenant': '会社 / 教室情報設定',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'wizard.workplace': '事業所追加',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'wizard.employee': '社員追加',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'wizard.shiftTemplate': 'シフトテンプレート作成',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'wizard.workRule': 'デフォルト勤怠ルール確認',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'wizard.schedule': 'シフト開始',
    // 执行当前业务步骤，推进本行对应的 constants 处理。
    'wizard.punch': '打刻取込 / 受信'
  }
}
