import { requestJson } from '../../../shared/services/request'

// 读取班次模板列表，供班次区块和排班区块共享可选模板数据。
export const listShiftTemplates = () => requestJson('/api/attendance/shift-templates')

// 新增班次模板，供班次区块登记新的排班模板。
export const createShiftTemplate = (payload) =>
  requestJson('/api/attendance/shift-templates', { method: 'POST', body: JSON.stringify(payload) })

// 更新班次模板，供班次区块维护既有模板资料。
export const updateShiftTemplate = (id, payload) =>
  requestJson(`/api/attendance/shift-templates/${id}`, { method: 'PUT', body: JSON.stringify(payload) })

// 删除班次模板，供班次区块移除不再使用的模板。
export const deleteShiftTemplate = (id) =>
  requestJson(`/api/attendance/shift-templates/${id}`, { method: 'DELETE' })

// 生成推荐班次模板，供班次区块快速建立初始模板集合。
export const generateRecommendedShiftTemplates = () =>
  requestJson('/api/attendance/shift-templates/recommended', { method: 'POST' })
