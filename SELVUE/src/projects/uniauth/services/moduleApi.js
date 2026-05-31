// 模块接口单独拆分，保证工程模块主数据维护和菜单、角色等域彻底分开。
import { requestJson } from '../../../shared/services/request'

// 模块保存接口负责统一权限中心的模块主数据维护。
export function saveModule(payload) {
  // 模块写操作统一使用 POST，后端根据 id 判断新增还是更新。
  return requestJson('/api/uniauth/modules', {
    // 当前接口只有一个保存入口，前端保持统一提交流程。
    method: 'POST',
    // 请求体发送当前模块主数据表单。
    body: JSON.stringify(payload)
  })
}
