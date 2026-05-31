import { computed, ref, watch } from 'vue'
import { defaultUniauthTheme, uniauthThemeOptions } from '../constants/themeOptions'
import adminWorkbenchThemeCss from '../../attendance/theme-presets/admin-workbench-dark.css?inline'
import attendanceGlassThemeCss from '../../attendance/theme-presets/attendance-glass-dark.css?inline'
import liquidGlassThemeCss from '../../attendance/theme-presets/liquid-glass.css?inline'

// 主题样式节点 id 独立于 attendance，避免两个工程切换主题时相互覆盖。
const THEME_STYLESHEET_ID = 'seluniauth-theme-stylesheet'
// 本地存储键独立于 attendance，保证两个工程各自记住用户偏好。
const STORAGE_KEY = 'seluniauth-theme'
// 主题值到真实 CSS 文本的映射直接复用 attendance 的正式主题资源。
const THEME_CSS_MAP = {
  'admin-workbench-dark': adminWorkbenchThemeCss,
  'attendance-glass-dark': attendanceGlassThemeCss,
  'liquid-glass': liquidGlassThemeCss
}

// 权限中心主题 composable 复用 attendance 的主题策略，但保留自己的状态节点和本地键。
export function useUniauthTheme() {
  // URL 参数优先级最高，便于真实页面验证时直接指定主题。
  const themeId = ref(resolveInitialTheme())

  // 当前主题选项供页面按钮显示名称和短标识。
  const currentThemeOption = computed(
    () => uniauthThemeOptions.find((item) => item.value === themeId.value) || uniauthThemeOptions[0]
  )

  // 主题值一旦变化，就同步写入样式节点、本地存储和 URL 参数。
  watch(
    themeId,
    (nextThemeId) => {
      // 未知主题一律回退到默认主题，避免页面进入无样式状态。
      const normalizedThemeId = normalizeThemeId(nextThemeId)
      if (normalizedThemeId !== themeId.value) {
        themeId.value = normalizedThemeId
        return
      }
      applyTheme(normalizedThemeId)
      persistTheme(normalizedThemeId)
      syncThemeToUrl(normalizedThemeId)
    },
    { immediate: true }
  )

  // 外部只通过统一入口更新主题值，真正的样式替换由 watch 负责。
  function setTheme(nextThemeId) {
    themeId.value = normalizeThemeId(nextThemeId)
  }

  // 对外暴露当前主题状态和正式主题选项。
  return {
    themeId,
    themeOptions: uniauthThemeOptions,
    currentThemeOption,
    setTheme
  }
}

// 初始化主题时优先读取 URL，再回退本地存储，最后使用默认主题。
function resolveInitialTheme() {
  const themeFromUrl = new URLSearchParams(window.location.search).get('theme')
  if (themeFromUrl && THEME_CSS_MAP[themeFromUrl]) {
    return themeFromUrl
  }
  const themeFromStorage = window.localStorage.getItem(STORAGE_KEY)
  if (themeFromStorage && THEME_CSS_MAP[themeFromStorage]) {
    return themeFromStorage
  }
  return defaultUniauthTheme
}

// 主题值统一归一化，避免无效字符串进入样式切换流程。
function normalizeThemeId(themeId) {
  return THEME_CSS_MAP[themeId] ? themeId : defaultUniauthTheme
}

// 把当前主题写进 head 中的单一样式节点，避免开发态外链主题失效。
function applyTheme(themeId) {
  const stylesheetNode = ensureThemeStylesheet()
  stylesheetNode.textContent = THEME_CSS_MAP[themeId]
  document.documentElement.setAttribute('data-seluniauth-theme', themeId)
}

// 确保页面里只有一个权限中心主题样式节点，防止重复注入主题文本。
function ensureThemeStylesheet() {
  let stylesheetNode = document.getElementById(THEME_STYLESHEET_ID)
  if (stylesheetNode) {
    return stylesheetNode
  }
  stylesheetNode = document.createElement('style')
  stylesheetNode.id = THEME_STYLESHEET_ID
  document.head.appendChild(stylesheetNode)
  return stylesheetNode
}

// 当前主题写入本地存储，保证刷新后继续沿用用户上次选择。
function persistTheme(themeId) {
  window.localStorage.setItem(STORAGE_KEY, themeId)
}

// 主题参数同步到地址栏，方便截图、分享和自动化验证。
function syncThemeToUrl(themeId) {
  const currentUrl = new URL(window.location.href)
  if (themeId === defaultUniauthTheme) {
    currentUrl.searchParams.delete('theme')
  } else {
    currentUrl.searchParams.set('theme', themeId)
  }
  window.history.replaceState({}, '', currentUrl)
}
