import { requestJson } from '../request'

// 导出 读取初始化聚合 前端服务动作，供页面调用后端接口。
export const fetchBootstrap = () => requestJson('/api/attendance/bootstrap')
// 导出 保存租户 前端服务动作，供页面调用后端接口。
export const saveTenant = (payload) =>
  // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
  requestJson('/api/attendance/tenant/current', { method: 'PUT', body: JSON.stringify(payload) })

