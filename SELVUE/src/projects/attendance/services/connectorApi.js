import { requestJson } from '../../../shared/services/request'

// 把第八阶段接入页筛选条件统一转成查询串，保证配置、映射和同步日志共用同一口径。
const buildConnectorQuery = (params = {}) => {
  // URLSearchParams 统一负责筛选项序列化，避免手写问号和连接符出错。
  const searchParams = new URLSearchParams()
  // 只发送真正有值的条件，避免空参数污染第八阶段工作台查询。
  Object.entries(params).forEach(([key, value]) => {
    if (value === '' || value === null || typeof value === 'undefined') return
    searchParams.set(key, String(value))
  })
  // 返回编码后的查询串，供工作台接口直接拼 URL。
  return searchParams.toString()
}

// 读取第八阶段接入工作台聚合结果，供接入配置、映射和同步日志同时展示。
export const fetchConnectorWorkbench = (params = {}) => {
  const query = buildConnectorQuery(params)
  if (!query) {
    return requestJson('/api/attendance/connectors')
  }
  return requestJson(`/api/attendance/connectors?${query}`)
}

// 新增正式接入配置，供管理员沉淀新的第三方打卡接入定义。
export const createConnector = (payload) =>
  requestJson('/api/attendance/connectors', { method: 'POST', body: JSON.stringify(payload) })

// 更新既有接入配置，供管理员修正密钥、方式和备注。
export const updateConnector = (id, payload) =>
  requestJson(`/api/attendance/connectors/${id}`, { method: 'PUT', body: JSON.stringify(payload) })

// 测试当前接入配置是否达到最小可用条件，供页面先确认配置完整性。
export const testConnector = (id) =>
  requestJson(`/api/attendance/connectors/${id}/test`, { method: 'POST' })

// 重试一条失败同步日志，供修完映射或配置后重新进入第三阶段原始打卡链路。
export const retryConnectorSyncLog = (id) =>
  requestJson(`/api/attendance/connectors/sync-logs/${id}/retry`, { method: 'POST' })
