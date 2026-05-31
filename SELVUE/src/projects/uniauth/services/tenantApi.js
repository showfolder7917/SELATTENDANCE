// 租户接口单独拆分，保证租户管理区块只依赖自己的服务文件。
import { requestJson } from '../../../shared/services/request'

// 租户保存接口按是否带 id 由后端分流新增和更新。
export function saveTenant(payload) {
  // 租户资料修改属于提交动作，因此统一使用 POST。
  return requestJson('/api/uniauth/tenants', {
    // 当前最小闭环不区分 create/update 路由，统一交给后端识别。
    method: 'POST',
    // 请求体直接发送当前租户表单。
    body: JSON.stringify(payload)
  })
}
