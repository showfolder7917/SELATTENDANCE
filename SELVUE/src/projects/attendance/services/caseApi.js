import { requestJson } from '../../../shared/services/request'

// 把第五阶段筛选对象整理成查询串，保证列表、统计和分页共用同一口径。
const buildCaseQuery = (params = {}) => {
  // URLSearchParams 负责过滤空值并组装成后端能直接识别的查询参数。
  const searchParams = new URLSearchParams()
  // 只有真正有值的条件才透传给后端，避免空参数污染第五阶段过滤。
  Object.entries(params).forEach(([key, value]) => {
    if (value === '' || value === null || typeof value === 'undefined') return
    searchParams.set(key, String(value))
  })
  // 返回完整查询串，让第五阶段服务调用可以直接拼接到接口路径。
  return searchParams.toString()
}

// 导出 查询第五阶段列表 前端服务动作，供异常处理 / 审批区块读取分页列表和统计卡。
export const listCases = (params) =>
  requestJson(`/api/attendance/cases?${buildCaseQuery(params)}`)

// 导出 查询单条处理单详情 前端服务动作，供右侧详情区读取审批单、日次详情和时间线。
export const getCaseDetail = (id) =>
  requestJson(`/api/attendance/cases/${id}`)

// 导出 创建处理单 前端服务动作，供未建单异常直接进入审批流。
export const createCase = (payload) =>
  requestJson('/api/attendance/cases', { method: 'POST', body: JSON.stringify(payload) })

// 导出 单条审批动作 前端服务动作，供通过、退回、驳回按钮提交处理结果。
export const applyCaseAction = (id, payload) =>
  requestJson(`/api/attendance/cases/${id}/actions`, { method: 'POST', body: JSON.stringify(payload) })

// 导出 批量审批动作 前端服务动作，供后续批量通过和批量退回场景复用。
export const applyCaseBatchAction = (payload) =>
  requestJson('/api/attendance/cases/batch-actions', { method: 'POST', body: JSON.stringify(payload) })
