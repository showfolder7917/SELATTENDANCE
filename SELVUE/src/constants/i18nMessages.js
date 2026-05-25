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
    appTitle: '考勤系统第二阶段工作台',
    // 维护 appSubtitle 字段，供当前前端状态或配置直接使用。
    appSubtitle: '基础资料、班次模板与第二阶段排班在一个页面连续完成。',
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
    // 维护 wizardTitle 字段，供当前前端状态或配置直接使用。
    wizardTitle: '初始化向导',
    // 维护 wizardHint 字段，供当前前端状态或配置直接使用。
    wizardHint: '基础资料已经用于第二阶段排班；当前仍不进入打卡导入、日次月次计算和审批闭环。',
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
    // 维护 保存 字段，供当前前端状态或配置直接使用。
    save: '保存',
    // 维护 新增 字段，供当前前端状态或配置直接使用。
    create: '新增',
    // 维护 删除 字段，供当前前端状态或配置直接使用。
    delete: '删除',
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
    // 维护 事业所 字段，供当前前端状态或配置直接使用。
    workplace: '事业所',
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
    appTitle: '勤怠システム Phase 2',
    // 维护 appSubtitle 字段，供当前前端状态或配置直接使用。
    appSubtitle: '基本マスタ、シフトテンプレート、Phase 2 のシフト作成を一画面で連続して管理します。',
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
    // 维护 wizardTitle 字段，供当前前端状态或配置直接使用。
    wizardTitle: '初期化ウィザード',
    // 维护 wizardHint 字段，供当前前端状态或配置直接使用。
    wizardHint: '基本マスタは Phase 2 のシフト作成で利用し、打刻取込・日次月次計算・申請承認はまだ対象外です。',
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
    // 维护 保存 字段，供当前前端状态或配置直接使用。
    save: '保存',
    // 维护 新增 字段，供当前前端状态或配置直接使用。
    create: '追加',
    // 维护 删除 字段，供当前前端状态或配置直接使用。
    delete: '削除',
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
    // 维护 事业所 字段，供当前前端状态或配置直接使用。
    workplace: '事業所',
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
    scheduleUnassignedCount: '未配置 {count} 日',
    scheduleUnassignedShort: '未配置',
    scheduleCellGuide: 'ここを押して配置',
    scheduleRestLabel: '休み / 時間なし',
    scheduleSideTitle: 'シフトアシスタント',
    scheduleSideHint: '右側にテンプレート、現在セル、まとめて配置する向けガイド、未配置警告を集約します。',
    scheduleTemplatePanelTitle: 'テンプレートパネル',
    scheduleTemplatePicked: '現在使うテンプレート：{template}',
    scheduleTemplateNeedPick: '先に右側でテンプレートを選び、その後でカレンダーセルを押してください。',
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
