// 工作台初始化接口独立拆分，便于后续继续扩展摘要卡和多区块聚合。
import { requestJson } from '../../../shared/services/request'

// 权限中心工作台接口一次性返回租户、用户、角色、菜单和权限定义。
export function fetchUniauthWorkbench() {
  // workbench 聚合读取只读当前 token，因此使用 GET 即可。
  return requestJson('/api/uniauth/bootstrap')
}
