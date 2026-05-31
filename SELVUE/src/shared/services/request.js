// 权限中心统一会话服务负责为所有工程提供同口径 access token。
import { readAccessToken } from './authSession'

// 默认基地址保持相对路径，让开发态统一走 Vite 代理到本地考勤后端。
// 声明 DEFAULTAPIBASEURL 字段，用来保存当前业务状态或依赖。
const DEFAULT_API_BASE_URL = ''
// 超时设置用于在后端离线时快速触发本地回退，而不是长时间等待。
// 声明 请求TIMEOUTMS 字段，用来保存当前业务状态或依赖。
const REQUEST_TIMEOUT_MS = 5000
// 已知鉴权与权限异常消息统一映射成稳定错误码，避免前端直接把后端单一语言文案暴露给多语言页面。
const KNOWN_ERROR_KEY_RESOLVERS = [
  // 未登录或登录态失效时统一归到会话已失效，方便页面按当前语言提示重新登录。
  { matcher: (message) => message === '当前请求未登录或登录态已失效', key: 'errorSessionExpired' },
  // 令牌过期需要单独提示，帮助用户区分“没登录”和“登录过期”。
  { matcher: (message) => message === '登录令牌已过期', key: 'errorTokenExpired' },
  // 令牌格式不正确通常表示本地缓存损坏或请求头拼装异常。
  { matcher: (message) => message === '登录令牌格式不正确', key: 'errorTokenMalformed' },
  // 签名无效通常意味着 token 被篡改或服务端签名密钥发生变化。
  { matcher: (message) => message === '登录令牌签名无效', key: 'errorTokenSignatureInvalid' },
  // 权限不足属于稳定业务语义，统一映射后可按当前语言给出一致提示。
  { matcher: (message) => message.startsWith('当前账号缺少权限'), key: 'errorPermissionDenied' }
]

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
  // 每次请求前先读取当前最新 access token，保证登录后无需刷新页面也能立刻鉴权。
  // 声明 accessToken 字段，用来保存当前业务状态或依赖。
  const accessToken = readAccessToken()
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
        // access token 存在时统一转成 Bearer 头，供 uniauth 和 attendance 宿主共享鉴权。
        ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
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
      // 失败响应也先按统一响应壳解析，优先把后端真实 message 暴露给页面而不是只显示 HTTP 文案。
      // 声明 failedPayload 字段，用来保存当前业务状态或依赖。
      const failedPayload = await readResponsePayload(response)
      // 后端若按 CommonResponse.failure 返回业务提示，这里优先直接给用户看真实失败原因。
      // 声明 failedMessage 字段，用来保存当前业务状态或依赖。
      const failedMessage = extractFailedMessage(failedPayload)
      // 只有后端确实没有返回可读 message 时，才回退到 HTTP 状态文案。
      // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
      throw createRequestError(failedMessage, `请求失败：${response.status} ${response.statusText}`)
    }
    // 考勤第一阶段所有接口统一按 JSON 解析。
    // 声明 payload 字段，用来保存当前业务状态或依赖。
    const payload = await readResponsePayload(response)
    // 兼容后端 CommonResponse 响应壳。
    // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
    if (typeof payload?.code !== 'undefined' && payload.code !== 0) {
      // 在关键校验失败时主动抛出异常，阻止错误数据继续流转。
      throw createRequestError(payload.message || '接口返回失败', '接口返回失败')
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

// 响应解析统一兼容 JSON 与纯文本，避免失败分支因为 response.json() 抛错而丢掉真实后端提示。
async function readResponsePayload(response) {
  // 先读取响应头里的内容类型，决定当前更适合按 JSON 还是按文本解析。
  // 声明 contentType 字段，用来保存当前业务状态或依赖。
  const contentType = response.headers.get('content-type') || ''
  // 明确声明 JSON 时优先按对象解析，保持和 CommonResponse 响应壳一致。
  // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
  if (contentType.includes('application/json')) {
    // 返回结构化 JSON，供成功与失败分支统一取 code、message、data。
    return response.json()
  }
  // 非 JSON 响应统一先读文本，兼容网关错误页或代理层纯文本提示。
  // 声明 textPayload 字段，用来保存当前业务状态或依赖。
  const textPayload = await response.text()
  // 纯文本时直接回可读文本，避免前端丢掉后端或代理层的原始失败提示。
  return textPayload
}

// 失败消息提取器统一从 CommonResponse、纯文本或兜底结构里提炼可直接展示的诊断文案。
function extractFailedMessage(failedPayload) {
  // 失败载荷为空时直接回空，让上层按 HTTP 状态兜底。
  if (!failedPayload) {
    return ''
  }
  // CommonResponse.failure 会把真实业务错误放在 message，这里优先提取该字段。
  if (typeof failedPayload === 'object' && typeof failedPayload.message === 'string' && failedPayload.message.trim()) {
    return failedPayload.message.trim()
  }
  // 代理层或服务层若只返回纯文本，这里直接把文本透传给页面提示条。
  if (typeof failedPayload === 'string' && failedPayload.trim()) {
    return failedPayload.trim()
  }
  // 其他未知结构暂不强行猜字段，交回上层走 HTTP 状态兜底。
  return ''
}

// 请求异常统一补上 messageKey，让多语言页面可以先按稳定错误码翻译，再回退到原始 message。
function createRequestError(message, fallbackMessage) {
  // 最终展示文案优先使用后端真实消息，只有后端没给出内容时才走前端兜底。
  const resolvedMessage = message || fallbackMessage || ''
  // 通过稳定错误码把后端中文异常和页面双语提示解耦。
  const messageKey = resolveKnownErrorMessageKey(resolvedMessage)
  // 继续复用标准 Error 对象，避免 service/composable 侧需要额外识别新的异常类型。
  const requestError = new Error(resolvedMessage)
  // 把稳定错误码挂到异常对象，供页面按当前语言翻译。
  requestError.messageKey = messageKey
  // 原始消息仍然保留，便于开发态诊断和未知异常直出。
  requestError.rawMessage = resolvedMessage
  return requestError
}

// 已知后端异常优先映射成稳定错误码，未知异常则保持空值交回页面直出。
function resolveKnownErrorMessageKey(message) {
  // 空消息无法做稳定映射，直接回空让上层继续走原始或兜底文案。
  if (!message) {
    return ''
  }
  // 逐个匹配当前已知的鉴权和权限异常，保证页面翻译不依赖后端语言环境。
  const matchedResolver = KNOWN_ERROR_KEY_RESOLVERS.find((resolver) => resolver.matcher(message))
  // 命中已知规则时返回稳定错误码，供页面多语言转换。
  return matchedResolver?.key || ''
}

// 页面层读取异常时优先按稳定错误码翻译，只有未知错误才回退到原始 message。
export function resolveLocalizedRequestError(error, translate) {
  // 没有异常对象时直接回通用失败提示，避免页面出现空提示条。
  if (!error) {
    return translate?.('errorRequestFailed') || '请求失败'
  }
  // 已知错误码先走当前语言字典，保证“令牌过期”等提示和页面语言一致。
  if (error.messageKey && typeof translate === 'function') {
    const translatedMessage = translate(error.messageKey)
    // 只有当前语言字典里确实存在对应 key 时才使用翻译结果，避免把 key 名直接显示给用户。
    if (translatedMessage && translatedMessage !== error.messageKey) {
      return translatedMessage
    }
  }
  // 未知异常继续保留后端原始消息，确保诊断能力不会因为国际化而倒退。
  if (typeof error.message === 'string' && error.message.trim()) {
    return error.message.trim()
  }
  // 所有细节都缺失时才回退到统一通用提示。
  return translate?.('errorRequestFailed') || '请求失败'
}
