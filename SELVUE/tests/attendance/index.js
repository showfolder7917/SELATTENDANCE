// 考勤测试公开入口只暴露允许测试依赖的状态工厂、共享辅助和服务边界，避免测试直接绑死 src 内部物理路径。

// 暴露工作台状态工厂，供 bootstrap、tenant、schedule 等单元测试验证默认业务结构。
export {
  createBootstrapShell,
  createEmptyTenant,
  createSectionErrors,
  createSectionLoaders,
  createSectionStates,
  createWorkbenchState
} from '@/projects/attendance/composables/workbench/state.js'

// 暴露工作台共享辅助函数，供表单默认值、筛选修复、toast 和导出测试统一复用。
export {
  downloadCsv,
  pushToast,
  syncFilterReferences,
  syncFormDefaults,
  syncShellCounters
} from '@/projects/attendance/composables/workbench/helpers.js'

// 暴露考勤模块注册信息，供宿主发现链路和插件契约测试验证。
export { default as attendanceProject } from '@/projects/attendance/index.js'

// 暴露 workbench 组合入口，供测试确认目录化入口仍保持稳定对外契约。
export { useAttendanceWorkbench } from '@/projects/attendance/composables/useAttendanceWorkbench.js'

// 暴露 bootstrap 服务边界，供首页壳和租户面板相关测试验证接口接线。
export { fetchBootstrap, fetchCurrentTenant, saveTenant } from '@/projects/attendance/services/bootstrapApi.js'

// 暴露部门服务边界，供部门模块集成测试验证请求路径和写回方式。
export {
  listDepartments,
  createDepartment,
  updateDepartment,
  deleteDepartment
} from '@/projects/attendance/services/departmentApi.js'

// 暴露员工服务边界，供员工模块集成和回归测试验证查询串与写接口契约。
export {
  listEmployees,
  createEmployee,
  updateEmployee,
  deleteEmployee,
  bindExternalMapping,
  importEmployees,
  exportEmployees
} from '@/projects/attendance/services/employeeApi.js'

// 暴露排班服务边界，供 schedule 模块集成和回归测试验证查询串与批量动作契约。
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
} from '@/projects/attendance/services/scheduleApi.js'

// 暴露班次模板服务边界，供 shift 模块测试验证模板读写与推荐生成动作。
export {
  listShiftTemplates,
  createShiftTemplate,
  updateShiftTemplate,
  deleteShiftTemplate,
  generateRecommendedShiftTemplates
} from '@/projects/attendance/services/shiftTemplateApi.js'

// 暴露场所服务边界，供 workplace 模块集成测试验证增删改查契约。
export {
  listWorkplaces,
  createWorkplace,
  updateWorkplace,
  deleteWorkplace
} from '@/projects/attendance/services/workplaceApi.js'
