import { computed, ref, watch } from 'vue'
import { attendanceThemeOptions, defaultAttendanceTheme } from '../constants/themeOptions'
import adminWorkbenchThemeCss from '../theme-presets/admin-workbench-dark.css?inline'
import attendanceGlassThemeCss from '../theme-presets/attendance-glass-dark.css?inline'
import liquidGlassThemeCss from '../theme-presets/liquid-glass.css?inline'

// 固定样式节点 id，确保主题切换时始终只维护同一个主题容器。
// 声明 主题STYLESHEETID 状态，保存当前工作台交互过程中需要的前端数据。
const THEME_STYLESHEET_ID = 'selattendance-theme-stylesheet'
// 本地持久化键用于记住用户上次选择的主题。
// 声明 STORAGEKEY 状态，保存当前工作台交互过程中需要的前端数据。
const STORAGE_KEY = 'selattendance-theme'
// 主题值到实际 CSS 文本的映射由 Vite 在构建阶段展开，运行时只负责切换内容。
// 声明 主题CSSMAP 状态，保存当前工作台交互过程中需要的前端数据。
const THEME_CSS_MAP = {
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  'admin-workbench-dark': adminWorkbenchThemeCss,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  'attendance-glass-dark': attendanceGlassThemeCss,
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  'liquid-glass': liquidGlassThemeCss
}

// 执行当前业务步骤，推进本行对应的 composable 处理。
export function useAttendanceTheme() {
  // URL 参数优先级最高，便于真实页面验证时直接指定主题。
  // 声明 主题Id 状态，保存当前工作台交互过程中需要的前端数据。
  const themeId = ref(resolveInitialTheme())

  // 当前主题选项用于页面展示名称和切换按钮状态。
  // 声明 当前主题选项 状态，保存当前工作台交互过程中需要的前端数据。
  const currentThemeOption = computed(
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    () => attendanceThemeOptions.find((item) => item.value === themeId.value) || attendanceThemeOptions[0]
  )

  // 主题切换一旦发生，立即替换 link、写回本地偏好并同步到地址栏。
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  watch(
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    themeId,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    (nextThemeId) => {
      // 声明 normalized主题Id 状态，保存当前工作台交互过程中需要的前端数据。
      const normalizedThemeId = normalizeThemeId(nextThemeId)
      // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
      if (normalizedThemeId !== themeId.value) {
        // 执行当前业务步骤，推进本行对应的 composable 处理。
        themeId.value = normalizedThemeId
        // 执行当前业务步骤，推进本行对应的 composable 处理。
        return
      }
      // 执行当前业务步骤，推进本行对应的 composable 处理。
      applyTheme(normalizedThemeId)
      // 执行当前业务步骤，推进本行对应的 composable 处理。
      persistTheme(normalizedThemeId)
      // 执行当前业务步骤，推进本行对应的 composable 处理。
      syncThemeToUrl(normalizedThemeId)
    },
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    { immediate: true }
  )

  // 执行当前业务步骤，推进本行对应的 composable 处理。
  function setTheme(nextThemeId) {
    // 外部入口只负责更新主题值，真正的 link 替换交给 watch 统一处理。
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    themeId.value = normalizeThemeId(nextThemeId)
  }

  // 返回当前步骤产出的业务结果，继续交给上一层消费。
  return {
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    themeId,
    // 维护 主题选项 字段，供当前前端状态或配置直接使用。
    themeOptions: attendanceThemeOptions,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    currentThemeOption,
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    setTheme
  }
}

// 执行当前业务步骤，推进本行对应的 composable 处理。
function resolveInitialTheme() {
  // 真实页面验证优先读取 URL 参数，这样不用依赖浏览器已有 localStorage 状态。
  // 声明 主题FromUrl 状态，保存当前工作台交互过程中需要的前端数据。
  const themeFromUrl = new URLSearchParams(window.location.search).get('theme')
  // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
  if (themeFromUrl && THEME_CSS_MAP[themeFromUrl]) {
    // 返回当前步骤产出的业务结果，继续交给上一层消费。
    return themeFromUrl
  }
  // 正常用户进入页面时优先恢复上次选择，避免每次刷新都回到默认主题。
  // 声明 主题FromStorage 状态，保存当前工作台交互过程中需要的前端数据。
  const themeFromStorage = window.localStorage.getItem(STORAGE_KEY)
  // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
  if (themeFromStorage && THEME_CSS_MAP[themeFromStorage]) {
    // 返回当前步骤产出的业务结果，继续交给上一层消费。
    return themeFromStorage
  }
  // 返回当前步骤产出的业务结果，继续交给上一层消费。
  return defaultAttendanceTheme
}

// 执行当前业务步骤，推进本行对应的 composable 处理。
function normalizeThemeId(themeId) {
  // 任何未知主题值都回退到默认主题，避免 link 指向无效资源。
  // 返回当前步骤产出的业务结果，继续交给上一层消费。
  return THEME_CSS_MAP[themeId] ? themeId : defaultAttendanceTheme
}

// 执行当前业务步骤，推进本行对应的 composable 处理。
function applyTheme(themeId) {
  // 主题样式始终通过 head 中的单一 style 标签承接，避免 dev 环境下外部 link 链式导入失效。
  // 声明 stylesheetNode 状态，保存当前工作台交互过程中需要的前端数据。
  const stylesheetNode = ensureThemeStylesheet()
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  stylesheetNode.textContent = THEME_CSS_MAP[themeId]
  // 根节点保留当前主题标识，后续如果有少量业务差异样式可按 data 属性扩展。
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  document.documentElement.setAttribute('data-selattendance-theme', themeId)
}

// 执行当前业务步骤，推进本行对应的 composable 处理。
function ensureThemeStylesheet() {
  // 如果页面尚未创建主题样式节点，则在 head 中补一个正式 style 标签。
  // 声明 stylesheetNode 状态，保存当前工作台交互过程中需要的前端数据。
  let stylesheetNode = document.getElementById(THEME_STYLESHEET_ID)
  // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
  if (stylesheetNode) {
    // 返回当前步骤产出的业务结果，继续交给上一层消费。
    return stylesheetNode
  }
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  stylesheetNode = document.createElement('style')
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  stylesheetNode.id = THEME_STYLESHEET_ID
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  document.head.appendChild(stylesheetNode)
  // 返回当前步骤产出的业务结果，继续交给上一层消费。
  return stylesheetNode
}

// 执行当前业务步骤，推进本行对应的 composable 处理。
function persistTheme(themeId) {
  // 当前主题写入 localStorage，保证刷新后保留用户偏好。
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  window.localStorage.setItem(STORAGE_KEY, themeId)
}

// 执行当前业务步骤，推进本行对应的 composable 处理。
function syncThemeToUrl(themeId) {
  // 主题参数同步到地址栏，便于分享特定主题截图和自动化验证。
  // 声明 当前Url 状态，保存当前工作台交互过程中需要的前端数据。
  const currentUrl = new URL(window.location.href)
  // 根据当前业务条件分流处理路径，避免错误数据进入后续流程。
  if (themeId === defaultAttendanceTheme) {
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    currentUrl.searchParams.delete('theme')
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  } else {
    // 执行当前业务步骤，推进本行对应的 composable 处理。
    currentUrl.searchParams.set('theme', themeId)
  }
  // 执行当前业务步骤，推进本行对应的 composable 处理。
  window.history.replaceState({}, '', currentUrl)
}
