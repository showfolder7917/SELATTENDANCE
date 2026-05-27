import { requestJson } from '../../../shared/services/request'

// 读取全部场所列表，供场所区块和依赖场所的其他区块初始化复用。
export const listWorkplaces = () => requestJson('/api/attendance/workplaces')

// 新增场所主数据，供场所区块写入新的工作地点。
export const createWorkplace = (payload) =>
  requestJson('/api/attendance/workplaces', { method: 'POST', body: JSON.stringify(payload) })

// 更新场所主数据，供场所区块维护既有工作地点资料。
export const updateWorkplace = (id, payload) =>
  requestJson(`/api/attendance/workplaces/${id}`, { method: 'PUT', body: JSON.stringify(payload) })

// 删除场所主数据，供场所区块移除不再使用的工作地点。
export const deleteWorkplace = (id) =>
  requestJson(`/api/attendance/workplaces/${id}`, { method: 'DELETE' })
