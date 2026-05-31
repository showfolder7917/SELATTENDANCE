// 菜单接口单独拆分，保证动态导航节点维护和其他域彻底分开。
import { requestJson } from '../../../shared/services/request'

// 菜单保存接口负责动态菜单节点和双语标题维护。
export function saveMenu(payload) {
  // 菜单树写操作统一使用 POST。
  return requestJson('/api/uniauth/menus', {
    // 后端按 id 分流新增和更新，前端保持统一提交口径。
    method: 'POST',
    // 请求体发送当前菜单节点表单。
    body: JSON.stringify(payload)
  })
}
