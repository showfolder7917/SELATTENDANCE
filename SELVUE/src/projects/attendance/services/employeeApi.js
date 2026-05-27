import { requestJson } from '../../../shared/services/request'

// 导出 新增员工 前端服务动作，供页面调用后端接口。
export const createEmployee = (payload) =>
  // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
  requestJson('/api/attendance/employees', { method: 'POST', body: JSON.stringify(payload) })

// 导出 更新员工 前端服务动作，供页面调用后端接口。
export const updateEmployee = (id, payload) =>
  // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
  requestJson(`/api/attendance/employees/${id}`, { method: 'PUT', body: JSON.stringify(payload) })

// 导出 删除员工 前端服务动作，供页面调用后端接口。
export const deleteEmployee = (id) =>
  // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
  requestJson(`/api/attendance/employees/${id}`, { method: 'DELETE' })

// 导出 绑定外部系统映射 前端服务动作，供页面调用后端接口。
export const bindExternalMapping = (id, payload) =>
  // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
  requestJson(`/api/attendance/employees/${id}/external-mapping`, { method: 'PUT', body: JSON.stringify(payload) })

// 导出 导入Employees 前端服务动作，供页面调用后端接口。
export const importEmployees = (payload) =>
  // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
  requestJson('/api/attendance/employees/import', { method: 'POST', body: JSON.stringify(payload) })

// 导出 导出Employees 前端服务动作，供页面调用后端接口。
export const exportEmployees = () => requestJson('/api/attendance/employees/export')
