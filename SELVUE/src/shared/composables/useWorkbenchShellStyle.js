import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { buildWorkbenchShellStyle } from '../constants/workbenchLayoutConfig'

// 共享页面壳样式 composable 统一负责感知 viewport 宽度，并把当前档位对应的壳宽 style 回给页面入口。
export function useWorkbenchShellStyle(shellProfileKey = 'defaultEntry') {
  // 视口宽度单独收在 ref 里，保证页面 resize 后能立刻回算 shared 壳宽。
  const viewportWidth = ref(typeof window === 'undefined' ? 1440 : window.innerWidth)

  // 当前浏览器窗口变化时只更新这一份宽度状态，让各工程都复用同一套回算入口。
  function syncViewportWidth() {
    // 真正参与壳宽计算的只有当前浏览器的 innerWidth，避免读取无关节点宽度引入额外误差。
    viewportWidth.value = window.innerWidth
  }

  // 页面挂载后立即同步一次窗口宽度，并开始监听 resize，让宽屏档位切换实时生效。
  onMounted(() => {
    // 首次挂载时先把当前真实视口宽度写进状态，避免热更新或前序默认值残留。
    syncViewportWidth()
    // 后续所有窗口宽度变化都统一从这里驱动 shared 壳宽重算。
    window.addEventListener('resize', syncViewportWidth)
  })

  // 页面卸载时清理全局 resize 监听，避免多个工程入口来回切换后残留重复回调。
  onBeforeUnmount(() => {
    // 只移除本 composable 注册的同名监听，不影响其他页面自己的 resize 逻辑。
    window.removeEventListener('resize', syncViewportWidth)
  })

  // 页面壳最终 style 直接从 shared 配置入口回算，业务页不再自己拼 width 和 max-width 数字。
  const shellStyle = computed(() => buildWorkbenchShellStyle(shellProfileKey, viewportWidth.value))

  // 返回值同时保留 shellStyle 和 viewportWidth，便于页面在需要时继续复用当前档位宽度。
  return {
    // 页面模板直接绑定 shellStyle 即可吃到统一 shared 壳宽配置。
    shellStyle,
    // 如需联动其他几何逻辑，页面也可以直接复用这份实时 viewport 宽度。
    viewportWidth
  }
}
