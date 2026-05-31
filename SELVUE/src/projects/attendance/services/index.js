// 暴露轻量首页壳和租户独立接口，供工作台壳层初始化复用。
export { fetchBootstrap, fetchCurrentTenant, saveTenant } from './bootstrapApi'

// 暴露场所读写接口，供场所区块和依赖场所的其他区块复用。
export { listWorkplaces, createWorkplace, updateWorkplace, deleteWorkplace } from './workplaceApi'

// 暴露部门读写接口，供部门区块和员工/排班筛选复用。
export { listDepartments, createDepartment, updateDepartment, deleteDepartment } from './departmentApi'

// 暴露员工读写接口，供员工区块和映射/导入导出功能复用。
export {
  listEmployees,
  createEmployee,
  updateEmployee,
  deleteEmployee,
  bindExternalMapping,
  importEmployees,
  exportEmployees
} from './employeeApi'

// 暴露第七阶段规则接口，供规则配置、员工适用和预警看板复用。
export {
  fetchRuleWorkbench,
  createRule,
  updateRule,
  assignRule
} from './ruleApi'

// 暴露第八阶段外部接入接口，供连接器工作台、测试连接和同步重试复用。
export {
  fetchConnectorWorkbench,
  createConnector,
  updateConnector,
  testConnector,
  retryConnectorSyncLog
} from './connectorApi'

// 暴露班次模板读写接口，供班次区块和排班模板选择复用。
export {
  listShiftTemplates,
  createShiftTemplate,
  updateShiftTemplate,
  deleteShiftTemplate,
  generateRecommendedShiftTemplates
} from './shiftTemplateApi'

// 暴露排班接口，供排班区块独立加载和批量操作复用。
export {
  fetchScheduleBoard,
  createSchedule,
  updateSchedule,
  deleteSchedule,
  batchAssignSchedules,
  copyLastWeekSchedules,
  copyLastMonthSchedules,
  checkUnassignedSchedules,
  exportSchedules
} from './scheduleApi'

// 暴露第三阶段打卡接口，供打卡记录区块独立加载和处理原始打卡事实。
export {
  listPunchLogs,
  getPunchLogDetail,
  createManualPunch,
  previewPunchImport,
  importPunchCsv,
  bindPunchEmployee,
  ignorePunchLog,
  reprocessPunchLog
} from './punchApi'

// 暴露第四阶段日次接口，供日次结果区块独立加载、详情查看和重算复用。
export {
  listDailyResults,
  getDailyDetail,
  recalculateDaily,
  recalculateDailyRange
} from './dailyApi'

// 暴露第五阶段异常处理与审批接口，供异常处理区块独立加载、建单和审批复用。
export {
  listCases,
  getCaseDetail,
  createCase,
  applyCaseAction,
  applyCaseBatchAction
} from './caseApi'
export {
  listMonthlyResults,
  getMonthlyDetail,
  recalculateMonthly,
  closeMonthly,
  reopenMonthly,
  exportMonthly
} from './monthlyApi'
