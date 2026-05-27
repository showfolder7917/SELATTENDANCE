import { requestJson } from '../../../shared/services/request'

// 读取轻量首页壳数据，只拿租户摘要、步骤状态和推荐动作。
export const fetchBootstrap = () => requestJson('/api/attendance/bootstrap')

// 独立读取当前租户资料，供租户面板和后续局部刷新复用。
export const fetchCurrentTenant = () => requestJson('/api/attendance/tenant/current')

// 保存当前租户资料，供首页租户面板回写基础主数据。
export const saveTenant = (payload) =>
  requestJson('/api/attendance/tenant/current', { method: 'PUT', body: JSON.stringify(payload) })
