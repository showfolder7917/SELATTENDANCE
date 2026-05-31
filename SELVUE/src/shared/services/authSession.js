// 本地存储键统一固定，保证权限中心和宿主都读写同一份登录态快照。
const AUTHSESSIONSTORAGEKEY = 'selattendance.uniauth.session'
// 事件名统一固定，保证保存和清空登录态后所有页面都能同步刷新。
const AUTHSESSIONCHANGEEVENT = 'selattendance-uniauth-session-change'

// 读取当前浏览器里保存的权限中心登录态，供宿主启动和页面刷新时恢复会话。
export function readAuthSession() {
  // 浏览器禁用 localStorage 时直接回空，避免宿主初始化报错。
  if (!window.localStorage) {
    return null
  }
  // 先把原始文本读出来，再决定是否需要 JSON 解析。
  const rawSessionText = window.localStorage.getItem(AUTHSESSIONSTORAGEKEY)
  // 当前还没有任何登录态时直接回空，表示宿主处于未登录状态。
  if (!rawSessionText) {
    return null
  }
  // 用受控解析把本地文本还原成对象，避免坏数据拖垮整个页面。
  try {
    // 正常情况下把文本解析成统一会话对象供宿主和权限中心复用。
    return JSON.parse(rawSessionText)
  } catch (error) {
    // 解析失败说明历史缓存结构已损坏，需要先清掉无效登录态。
    window.localStorage.removeItem(AUTHSESSIONSTORAGEKEY)
    // 清理坏缓存后回空，让页面退回重新登录流程。
    return null
  }
}

// 保存最新登录态时统一写 localStorage，并通知所有工程立即同步状态。
export function writeAuthSession(nextSession) {
  // 调用方显式传空时直接清理登录态，避免宿主保留失效 token。
  if (!nextSession) {
    clearAuthSession()
    return
  }
  // 先把对象序列化后落盘，保证刷新页面后仍能恢复同一登录态。
  window.localStorage.setItem(AUTHSESSIONSTORAGEKEY, JSON.stringify(nextSession))
  // 写盘后立刻广播会话变化，保证宿主切换器和页面头部同步刷新。
  window.dispatchEvent(new CustomEvent(AUTHSESSIONCHANGEEVENT, { detail: nextSession }))
}

// 清空当前浏览器会话，供退出登录或 token 失效后统一收口。
export function clearAuthSession() {
  // 从本地存储里移除旧会话，避免后续请求继续携带过期 token。
  window.localStorage.removeItem(AUTHSESSIONSTORAGEKEY)
  // 清理后广播空会话，让所有工程同步回未登录状态。
  window.dispatchEvent(new CustomEvent(AUTHSESSIONCHANGEEVENT, { detail: null }))
}

// 单独暴露 token 读取能力，供请求层自动附带 Authorization 头。
export function readAccessToken() {
  // 当前 token 来自统一会话对象，避免请求层自己拼装或猜字段。
  return readAuthSession()?.accessToken || ''
}

// 对外提供订阅接口，让宿主和业务工程都能监听登录态变化。
export function subscribeAuthSession(listener) {
  // 统一把浏览器事件转换成回调调用，减少外层重复写事件适配逻辑。
  const eventHandler = (event) => listener(event.detail ?? null)
  // 注册监听器后，后续登录、退出和刷新都能驱动页面更新。
  window.addEventListener(AUTHSESSIONCHANGEEVENT, eventHandler)
  // 返回解除订阅函数，供组件卸载时安全清理监听器。
  return () => window.removeEventListener(AUTHSESSIONCHANGEEVENT, eventHandler)
}
