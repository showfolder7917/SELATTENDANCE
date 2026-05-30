import { requestJson } from '../../../shared/services/request'

// 把第七阶段规则页筛选对象转成查询串，保证规则列表、适用列表和预警看板共用同一口径。
const buildRuleQuery = (params = {}) => {
  // URLSearchParams 负责把所有非空筛选项整理成后端可识别的查询参数。
  const searchParams = new URLSearchParams()
  // 只有真正有值的条件才发送给后端，避免空参数污染规则工作台查询。
  Object.entries(params).forEach(([key, value]) => {
    if (value === '' || value === null || typeof value === 'undefined') return
    searchParams.set(key, String(value))
  })
  // 返回完整查询串，让第七阶段服务函数直接拼接到接口路径。
  return searchParams.toString()
}

// 读取第七阶段规则工作台聚合结果，供规则配置、员工适用和预警看板共用。
export const fetchRuleWorkbench = (params = {}) => {
  const query = buildRuleQuery(params)
  if (!query) {
    return requestJson('/api/attendance/rules')
  }
  return requestJson(`/api/attendance/rules?${query}`)
}

// 新增正式规则配置，供管理员沉淀日本勤怠业务口径。
export const createRule = (payload) =>
  requestJson('/api/attendance/rules', { method: 'POST', body: JSON.stringify(payload) })

// 更新既有规则配置，供管理员修正式规则口径。
export const updateRule = (id, payload) =>
  requestJson(`/api/attendance/rules/${id}`, { method: 'PUT', body: JSON.stringify(payload) })

// 给指定员工绑定正式规则，供规则页员工适用表单保存。
export const assignRule = (employeeId, payload) =>
  requestJson(`/api/attendance/rules/assignments/${employeeId}`, { method: 'PUT', body: JSON.stringify(payload) })
