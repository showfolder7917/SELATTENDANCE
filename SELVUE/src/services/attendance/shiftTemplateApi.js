import { requestJson } from '../request'

// 导出 新增班次模板 前端服务动作，供页面调用后端接口。
export const createShiftTemplate = (payload) =>
  // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
  requestJson('/api/attendance/shift-templates', { method: 'POST', body: JSON.stringify(payload) })

// 导出 更新班次模板 前端服务动作，供页面调用后端接口。
export const updateShiftTemplate = (id, payload) =>
  // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
  requestJson(`/api/attendance/shift-templates/${id}`, { method: 'PUT', body: JSON.stringify(payload) })

// 导出 删除班次模板 前端服务动作，供页面调用后端接口。
export const deleteShiftTemplate = (id) =>
  // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
  requestJson(`/api/attendance/shift-templates/${id}`, { method: 'DELETE' })

// 导出 生成推荐班次Templates 前端服务动作，供页面调用后端接口。
export const generateRecommendedShiftTemplates = () =>
  // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
  requestJson('/api/attendance/shift-templates/recommended', { method: 'POST' })

