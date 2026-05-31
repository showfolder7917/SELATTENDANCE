// 角色接口单独拆分，便于第九阶段后续继续扩展角色授权能力。
import { requestJson } from '../../../shared/services/request'

// 角色保存接口负责角色主资料、权限码、菜单码和数据范围。
export function saveRole(payload) {
  // 角色写操作统一使用 POST，和 attendance 管理类接口保持一致。
  return requestJson('/api/uniauth/roles', {
    // 后端按 id 分流新增和更新，前端只负责提交最终授权表达。
    method: 'POST',
    // 请求体直接发送当前角色表单和解析后的编码数组。
    body: JSON.stringify(payload)
  })
}
