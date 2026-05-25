// 默认基地址保持相对路径，让开发态统一走 Vite 代理到本地考勤后端。
// 声明 DEFAULTAPIBASEURL 字段，用来保存当前业务状态或依赖。
const DEFAULT_API_BASE_URL = ''
// 超时设置用于在后端离线时快速触发本地回退，而不是长时间等待。
// 声明 请求TIMEOUTMS 字段，用来保存当前业务状态或依赖。
const REQUEST_TIMEOUT_MS = 5000

/**
 * 执行标准 JSON 请求。
 *
 * @param {string} path 接口路径
 * @param {RequestInit} [options] 请求参数
 * @returns {Promise<any>} 解析后的结果
 */
// 执行当前业务步骤，推进本行对应的 frontend服务 处理。
export async function requestJson(path, options = {}) {
  // 允许通过环境变量覆盖联调地址；未覆盖时优先走相对路径代理，而不是直接跨域打 8080。
  // 声明 baseUrl 字段，用来保存当前业务状态或依赖。
  const baseUrl = import.meta.env.VITE_API_BASE_URL || DEFAULT_API_BASE_URL
  // 每次请求都创建独立 AbortController，便于超时主动终止。
  // 声明 控制器 字段，用来保存当前业务状态或依赖。
  const controller = new AbortController()
  // 开启超时计时器，防止接口无响应拖慢页面。
  // 声明 timeoutId 字段，用来保存当前业务状态或依赖。
  const timeoutId = window.setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS)

  // 进入受控处理区块，统一兜底当前业务动作的异常路径。
  try {
    // 发起正式请求，并统一附带 JSON 头和超时信号。
    // 声明 响应 字段，用来保存当前业务状态或依赖。
    const response = await fetch(`${baseUrl}${path}`, {
      // 维护 headers 字段，供当前前端状态或配置直接使用。
      headers: {
        // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
        'Content-Type': 'application/json',
        // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
        ...(options.headers || {})
      },
      // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
      ...options,
      // 维护 signal 字段，供当前前端状态或配置直接使用。
      signal: controller.signal
    // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
    })
    // 非 2xx 直接抛错，避免页面把失败响应误当成功。
    // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
    if (!response.ok) {
      // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
      throw new Error(`请求失败：${response.status} ${response.statusText}`)
    }
    // 考勤第一阶段所有接口统一按 JSON 解析。
    // 声明 payload 字段，用来保存当前业务状态或依赖。
    const payload = await response.json()
    // 兼容后端 CommonResponse 响应壳。
    // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
    if (typeof payload?.code !== 'undefined' && payload.code !== 0) {
      // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
      throw new Error(payload.message || '接口返回失败')
    }
    // 优先返回 data，保证服务层消费结构统一。
    // 返回当前步骤产出的业务结果，继续交给上一层消费。
    return typeof payload?.data !== 'undefined' ? payload.data : payload
  // 执行当前业务步骤，推进本行对应的 frontend服务 处理。
  } finally {
    // 请求结束后清理超时计时器，避免遗留无效任务。
    // 清理已创建的定时器，避免旧请求的超时回调继续生效。
    window.clearTimeout(timeoutId)
  }
}
