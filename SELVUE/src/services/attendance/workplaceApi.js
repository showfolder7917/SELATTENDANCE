import { requestJson } from '../request'

// 导出 新增事业所 前端服务动作，供页面调用后端接口。
export const createWorkplace = (payload) =>
  // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
  requestJson('/api/attendance/workplaces', { method: 'POST', body: JSON.stringify(payload) })

// 导出 更新事业所 前端服务动作，供页面调用后端接口。
export const updateWorkplace = (id, payload) =>
  // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
  requestJson(`/api/attendance/workplaces/${id}`, { method: 'PUT', body: JSON.stringify(payload) })

// 导出 删除事业所 前端服务动作，供页面调用后端接口。
export const deleteWorkplace = (id) =>
  // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
  requestJson(`/api/attendance/workplaces/${id}`, { method: 'DELETE' })

