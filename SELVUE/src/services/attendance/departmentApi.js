import { requestJson } from '../request'

// 导出 新增部门 前端服务动作，供页面调用后端接口。
export const createDepartment = (payload) =>
  // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
  requestJson('/api/attendance/departments', { method: 'POST', body: JSON.stringify(payload) })

// 导出 更新部门 前端服务动作，供页面调用后端接口。
export const updateDepartment = (id, payload) =>
  // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
  requestJson(`/api/attendance/departments/${id}`, { method: 'PUT', body: JSON.stringify(payload) })

// 导出 删除部门 前端服务动作，供页面调用后端接口。
export const deleteDepartment = (id) =>
  // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
  requestJson(`/api/attendance/departments/${id}`, { method: 'DELETE' })

