import { requestJson } from '../../../shared/services/request'

// 把第六阶段筛选对象转成查询串，保证月次列表、摘要和数据库分页共用同一套过滤口径。
const buildMonthlyQuery = (params = {}) => {
  // URLSearchParams 负责把所有非空筛选项整理成后端可识别的查询参数。
  const searchParams = new URLSearchParams()
  // 只有真正有值的条件才发送给后端，避免空参数污染第六阶段查询。
  Object.entries(params).forEach(([key, value]) => {
    if (value === '' || value === null || typeof value === 'undefined') return
    searchParams.set(key, String(value))
  })
  // 返回完整查询串，让第六阶段服务函数直接拼接到接口路径。
  return searchParams.toString()
}

// 导出 查询月次列表 前端服务动作，供第六阶段月次工作区读取分页列表和摘要。
export const listMonthlyResults = (params) =>
  requestJson(`/api/attendance/monthly?${buildMonthlyQuery(params)}`)

// 导出 查询单条月次详情 前端服务动作，供右侧详情面板读取统计项、阻塞和动作日志。
export const getMonthlyDetail = (id) =>
  requestJson(`/api/attendance/monthly/${id}`)

// 导出 月次重算 前端服务动作，供用户按当前筛选或当前员工重建月汇总。
export const recalculateMonthly = (payload) =>
  requestJson('/api/attendance/monthly/recalculate', { method: 'POST', body: JSON.stringify(payload) })

// 导出 月结确认 前端服务动作，供管理员把当前范围正式置为已结。
export const closeMonthly = (payload) =>
  requestJson('/api/attendance/monthly/close', { method: 'POST', body: JSON.stringify(payload) })

// 导出 反结 前端服务动作，供管理员重新开放已结月份。
export const reopenMonthly = (payload) =>
  requestJson('/api/attendance/monthly/reopen', { method: 'POST', body: JSON.stringify(payload) })

// 导出 月次 CSV 前端服务动作，供页面直接复用统一下载逻辑。
export const exportMonthly = (payload) =>
  requestJson('/api/attendance/monthly/export', { method: 'POST', body: JSON.stringify(payload) })
