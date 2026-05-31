// 认证接口单独拆出来，避免登录链路和管理工作台接口继续混在一个服务文件里。
import { requestJson } from '../../../shared/services/request'

// 登录接口把账号密码换成 token、当前用户和首屏工作台。
export function loginUniauth(payload) {
  // 登录属于显式提交动作，因此统一使用 POST。
  return requestJson('/api/uniauth/auth/login', {
    // 请求方法固定成 POST，避免账号密码出现在 URL 中。
    method: 'POST',
    // 请求体直接发送登录表单，保持前后端字段口径一致。
    body: JSON.stringify(payload)
  })
}

// 当前用户快照接口供刷新页面后恢复登录态时复用。
export function fetchCurrentUser() {
  // 快照读取只依赖 Bearer token，因此直接走 GET。
  return requestJson('/api/uniauth/auth/me')
}
