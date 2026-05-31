// 用户接口单独拆分，避免账号保存和其他管理动作继续混放。
import { requestJson } from '../../../shared/services/request'

// 用户保存接口负责账号资料和角色绑定维护。
export function saveUser(payload) {
  // 用户保存属于显式写操作，因此统一使用 POST。
  return requestJson('/api/uniauth/users', {
    // 后端按 id 判断新增还是更新，前端只负责提交最终 payload。
    method: 'POST',
    // 请求体直接发送归一化后的账号表单。
    body: JSON.stringify(payload)
  })
}
