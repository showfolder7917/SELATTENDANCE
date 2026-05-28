import { requestJson } from '../../../shared/services/request'

// 把第四阶段筛选对象转成查询串，保证列表、摘要和数据库分页共用同一套过滤口径。
const buildDailyQuery = (params = {}) => {
  // URLSearchParams 负责把所有非空筛选项整理成后端可识别的查询参数。
  const searchParams = new URLSearchParams()
  // 只有真正有值的条件才发送给后端，避免空参数污染第四阶段查询。
  Object.entries(params).forEach(([key, value]) => {
    if (value === '' || value === null || typeof value === 'undefined') return
    searchParams.set(key, String(value))
  })
  // 返回完整查询串，让第四阶段服务函数直接拼接到接口路径。
  return searchParams.toString()
}

// 导出 查询日次结果列表 前端服务动作，供第四阶段日次工作区读取分页列表和摘要。
export const listDailyResults = (params) =>
  requestJson(`/api/attendance/daily?${buildDailyQuery(params)}`)

// 导出 查询单条日次详情 前端服务动作，供右侧详情抽屉读取排班、打卡、异常和计算过程。
export const getDailyDetail = (id) =>
  requestJson(`/api/attendance/daily/${id}`)

// 导出 单日重算 前端服务动作，供用户对选中日次立即重新计算。
export const recalculateDaily = (payload) =>
  requestJson('/api/attendance/daily/recalculate', { method: 'POST', body: JSON.stringify(payload) })

// 导出 范围重算 前端服务动作，供用户按当前筛选区间批量刷新第四阶段结果。
export const recalculateDailyRange = (payload) =>
  requestJson('/api/attendance/daily/recalculate-range', { method: 'POST', body: JSON.stringify(payload) })
