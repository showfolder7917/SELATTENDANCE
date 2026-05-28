import { requestJson } from '../../../shared/services/request'

// 把打卡筛选对象转成查询串，保证列表、摘要和分页共用同一套过滤口径。
const buildPunchQuery = (params = {}) => {
  // URLSearchParams 负责把非空筛选项拼成后端可识别的查询参数。
  const searchParams = new URLSearchParams()
  // 只把真正有值的筛选项带给后端，避免空字段污染第三阶段查询条件。
  Object.entries(params).forEach(([key, value]) => {
    if (value === '' || value === null || typeof value === 'undefined') return
    searchParams.set(key, String(value))
  })
  // 返回完整查询串，让打卡服务函数直接拼接到接口路径上。
  return searchParams.toString()
}

// 导出 查询打卡记录列表 前端服务动作，供第三阶段打卡区块读取原始事实列表和摘要。
export const listPunchLogs = (params) =>
  requestJson(`/api/attendance/punch/logs?${buildPunchQuery(params)}`)

// 导出 查询单条打卡详情 前端服务动作，供右侧详情面板读取原始 payload 和可执行动作。
export const getPunchLogDetail = (id) =>
  requestJson(`/api/attendance/punch/logs/${id}`)

// 导出 手动补录打卡 前端服务动作，供管理员处理漏打卡场景。
export const createManualPunch = (payload) =>
  requestJson('/api/attendance/punch/manual', { method: 'POST', body: JSON.stringify(payload) })

// 导出 预览打卡CSV 前端服务动作，供正式导入前先看前几行的匹配结果。
export const previewPunchImport = (payload) =>
  requestJson('/api/attendance/punch/import-csv/preview', { method: 'POST', body: JSON.stringify(payload) })

// 导出 正式导入打卡CSV 前端服务动作，供第三阶段批量落地原始打卡事实。
export const importPunchCsv = (payload) =>
  requestJson('/api/attendance/punch/import-csv', { method: 'POST', body: JSON.stringify(payload) })

// 导出 绑定打卡员工 前端服务动作，供未匹配记录手动归属到系统员工。
export const bindPunchEmployee = (id, payload) =>
  requestJson(`/api/attendance/punch/logs/${id}/bind-employee`, { method: 'POST', body: JSON.stringify(payload) })

// 导出 忽略打卡记录 前端服务动作，供误导入记录流转到已忽略状态。
export const ignorePunchLog = (id, payload) =>
  requestJson(`/api/attendance/punch/logs/${id}/ignore`, { method: 'POST', body: JSON.stringify(payload || {}) })

// 导出 重处理打卡记录 前端服务动作，供映射修复后重新跑匹配逻辑。
export const reprocessPunchLog = (id) =>
  requestJson(`/api/attendance/punch/logs/${id}/reprocess`, { method: 'POST' })
