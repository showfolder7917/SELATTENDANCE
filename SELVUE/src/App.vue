<script setup>
import { computed, onMounted, onBeforeUnmount, ref, watch } from 'vue'
import {
  availableProjects,
  findProjectById,
  normalizeProjectId,
  readProjectIdFromUrl,
  resolveInitialProjectId,
  writeProjectIdToUrl
} from './projects'
import { readAuthSession, subscribeAuthSession } from './shared/services/authSession'

// 用可写工程 id 维护当前宿主激活项目，保证切换器能直接切换工程而不是只读计算。
const activeProjectId = ref(resolveInitialProjectId())
// 宿主额外维护统一登录态，供工程显隐和后续顶部状态展示使用。
const authSession = ref(readAuthSession())
// 订阅清理函数单独缓存，保证宿主卸载时能安全解除登录态监听。
let unsubscribeAuthSession = () => {}

// 根据当前激活工程 id 解析工程元数据，供宿主渲染实际工程组件。
const activeProject = computed(() =>
  visibleProjects.value.find((projectEntry) => projectEntry.id === activeProjectId.value) || null
)

// 宿主切换器直接复用当前工程列表，保证安装或移除工程目录后列表自动变化。
const projectOptions = computed(() => availableProjects)
// 当前登录态带的菜单码决定哪些工程真正允许出现在宿主切换器里。
const grantedMenuCodes = computed(() => authSession.value?.currentUser?.menuCodes || [])
// 工程列表在宿主层按 publicEntry 和 requiredMenuCodes 双条件过滤，实现最小菜单权限消费。
const visibleProjects = computed(() =>
  projectOptions.value.filter((projectEntry) => {
    if (projectEntry.publicEntry) {
      return true
    }
    if (!projectEntry.requiredMenuCodes?.length) {
      return true
    }
    return projectEntry.requiredMenuCodes.some((menuCode) => grantedMenuCodes.value.includes(menuCode))
  })
)

// 是否显示工程切换器取决于当前是否存在多个工程，单工程时避免无意义控件干扰页面。
const showProjectSwitcher = computed(() => visibleProjects.value.length > 1)

// 把 URL 中的 project 参数重新同步回宿主激活工程，支持浏览器前进后退或手工改 URL。
const syncProjectFromUrl = () => {
  // 只接受当前仍然存在的工程 id，避免 URL 指向已移除工程目录时宿主报错。
  const nextProjectId = normalizeProjectId(readProjectIdFromUrl()) || visibleProjects.value[0]?.id || ''
  // 仅在工程 id 实际变化时更新宿主状态，减少无意义的重渲染。
  if (nextProjectId !== activeProjectId.value) {
    activeProjectId.value = nextProjectId
  }
}

// 当前激活工程变化时立即写回 URL，保证刷新页面或复制链接后仍能打开同一工程。
watch(activeProjectId, (nextProjectId) => {
  writeProjectIdToUrl(nextProjectId)
})

// 登录态或菜单权限变化时重新校正当前工程，避免用户停留在已无权限的模块。
watch(visibleProjects, (nextVisibleProjects) => {
  if (nextVisibleProjects.some((projectEntry) => projectEntry.id === activeProjectId.value)) {
    return
  }
  activeProjectId.value = nextVisibleProjects[0]?.id || ''
})

// 页面挂载后监听浏览器历史变化，保证宿主地址栏和激活工程保持双向同步。
onMounted(() => {
  unsubscribeAuthSession = subscribeAuthSession((nextSession) => {
    authSession.value = nextSession
  })
  window.addEventListener('popstate', syncProjectFromUrl)
})

// 页面卸载时移除浏览器历史监听，避免宿主组件重复挂载后遗留事件处理器。
onBeforeUnmount(() => {
  unsubscribeAuthSession()
  window.removeEventListener('popstate', syncProjectFromUrl)
})
</script>

<template>
  <div class="project-host-shell">
    <header v-if="showProjectSwitcher" class="project-host-switcher">
      <label class="project-host-switcher-label" for="project-host-select">Project</label>
      <select id="project-host-select" v-model="activeProjectId" class="project-host-switcher-select">
        <option v-for="projectEntry in visibleProjects" :key="projectEntry.id" :value="projectEntry.id">
          {{ projectEntry.label }}
        </option>
      </select>
    </header>

    <component :is="activeProject.component" v-if="activeProject" />

    <main v-else class="project-host-empty-state">
      <section class="project-host-empty-card">
        <p class="project-host-eyebrow">Project Host</p>
        <h1>No project is currently registered</h1>
        <p>
          The root host is running, but there is no available module under <code>src/projects</code>.
        </p>
      </section>
    </main>
  </div>
</template>

<style scoped>
.project-host-shell {
  min-height: 100vh;
}

.project-host-switcher {
  position: fixed;
  top: 16px;
  right: 16px;
  z-index: 40;
  display: grid;
  gap: 6px;
  padding: 12px 14px;
  border: 1px solid rgba(15, 23, 42, 0.14);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.14);
  backdrop-filter: blur(10px);
}

.project-host-switcher-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #5b6474;
}

.project-host-switcher-select {
  min-width: 164px;
  padding: 8px 10px;
  border: 1px solid #cdd5df;
  border-radius: 10px;
  background: #ffffff;
  color: #142033;
  font: inherit;
}

.project-host-empty-state {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px;
  background:
    radial-gradient(circle at top, rgba(93, 141, 217, 0.16), transparent 30%),
    linear-gradient(180deg, #0d1320 0%, #111b2c 100%);
  color: #f7fbff;
}

.project-host-empty-card {
  width: min(100%, 640px);
  display: grid;
  gap: 12px;
  padding: 28px;
  border: 1px solid rgba(196, 214, 242, 0.18);
  border-radius: 24px;
  background: rgba(14, 21, 33, 0.78);
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.24);
}

.project-host-eyebrow {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(217, 231, 255, 0.66);
}

.project-host-empty-card h1,
.project-host-empty-card p {
  margin: 0;
}

@media (max-width: 720px) {
  .project-host-switcher {
    left: 16px;
    right: 16px;
  }

  .project-host-switcher-select {
    min-width: 0;
    width: 100%;
  }
}
</style>
