<script setup>
import { computed, nextTick, onMounted, onBeforeUnmount, ref, watch } from 'vue'
import {
  availableProjects,
  findProjectById,
  normalizeProjectId,
  readProjectIdFromUrl,
  resolveInitialProjectId,
  writeProjectIdToUrl
} from './projects'
import { readAuthSession, subscribeAuthSession } from './shared/services/authSession'
// 宿主入口统一引入 hero 顶部工具带共享样式，让 attendance 和 uniauth 共用同一套主题/语言/project 布局规则。
import './shared/styles/hero-toolbar.css'
// 宿主入口再统一引入首屏壳共享样式，让各工程 hero 和空态卡片走同一套公共主题层。
import './shared/styles/project-entry-shell.css'

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
// 当前页面是否已经提供 hero 顶部工具条挂载点，决定 project 切换器是并入同一排还是退回独立浮层。
const toolbarTargetReady = ref(false)

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
  // 工程切换开始时先把 toolbar 目标标记为未就绪，让宿主立即回退到兜底浮层，避免旧 teleport 目标刚卸载时出现短暂空窗。
  toolbarTargetReady.value = false
  writeProjectIdToUrl(nextProjectId)
})

// 登录态或菜单权限变化时重新校正当前工程，避免用户停留在已无权限的模块。
watch(visibleProjects, (nextVisibleProjects) => {
  if (nextVisibleProjects.some((projectEntry) => projectEntry.id === activeProjectId.value)) {
    return
  }
  activeProjectId.value = nextVisibleProjects[0]?.id || ''
})

// 每次工程切换或列表变化后都重新检查当前页面是否暴露了统一 toolbar 挂载点，保证 project 切换器能并入 hero 操作带。
watch([activeProjectId, visibleProjects], async () => {
  await refreshToolbarTargetState()
})

// 页面挂载后监听浏览器历史变化，保证宿主地址栏和激活工程保持双向同步。
onMounted(() => {
  unsubscribeAuthSession = subscribeAuthSession((nextSession) => {
    authSession.value = nextSession
  })
  window.addEventListener('popstate', syncProjectFromUrl)
  // 首次挂载后也要探测一次目标页面是否已经渲染出 toolbar 挂载点。
  refreshToolbarTargetState()
})

// 页面卸载时移除浏览器历史监听，避免宿主组件重复挂载后遗留事件处理器。
onBeforeUnmount(() => {
  unsubscribeAuthSession()
  window.removeEventListener('popstate', syncProjectFromUrl)
})

// 工具条目标检测统一收口在宿主层，避免每个 project 自己判断 project 切换器该怎么放。
async function refreshToolbarTargetState() {
  // 等待当前工程视图完成一次 DOM 更新，再检查统一挂载点是否已经出现。
  await nextTick()
  // 只有存在挂载点时才把 project 切换器 teleport 进 hero 操作带，否则回退到独立浮层。
  toolbarTargetReady.value = Boolean(document.querySelector('#project-host-toolbar-target'))
}
</script>

<template>
  <div class="project-host-shell">
    <!-- 当业务页面提供统一挂载点时，宿主 project 切换器直接并入页面 hero 的主题/语言同一条操作带。 -->
    <teleport v-if="showProjectSwitcher && toolbarTargetReady" to="#project-host-toolbar-target">
      <div class="project-host-switcher-group">
        <!-- Project 标签保留在同一条横向操作带里，帮助用户知道当前切换的是宿主工程而不是业务筛选项。 -->
        <label class="project-host-switcher-label" for="project-host-select">Project</label>
        <!-- 工程下拉框继续直接绑定 activeProjectId，但视觉上改成和主题切换同一套深色胶囊风格。 -->
        <select id="project-host-select" v-model="activeProjectId" class="project-host-switcher-select">
          <option v-for="projectEntry in visibleProjects" :key="projectEntry.id" :value="projectEntry.id">
            {{ projectEntry.label }}
          </option>
        </select>
      </div>
    </teleport>

    <!-- 没有挂载点的页面仍保留宿主兜底浮层，避免 memory 等简单工程失去 project 切换能力。 -->
    <header v-else-if="showProjectSwitcher" class="project-host-switcher">
      <div class="project-host-switcher-group">
        <label class="project-host-switcher-label" for="project-host-select">Project</label>
        <select id="project-host-select" v-model="activeProjectId" class="project-host-switcher-select">
          <option v-for="projectEntry in visibleProjects" :key="projectEntry.id" :value="projectEntry.id">
            {{ projectEntry.label }}
          </option>
        </select>
      </div>
    </header>

    <component :is="activeProject.component" v-if="activeProject" />

    <main v-else class="project-host-empty-state selshared-entry-stage">
      <section class="project-host-empty-card selshared-entry-card">
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
  display: flex;
  align-items: center;
  padding: 0;
}

.project-host-empty-state {
  min-height: 100vh;
  padding: 32px;
  background:
    radial-gradient(circle at top, rgba(93, 141, 217, 0.16), transparent 30%),
    linear-gradient(180deg, #0d1320 0%, #111b2c 100%);
  color: #f7fbff;
}

.project-host-empty-card {
  width: min(100%, 640px);
  gap: 12px;
  padding: 28px;
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

  .project-host-switcher-group {
    width: 100%;
    justify-content: space-between;
  }

  .project-host-switcher-select {
    min-width: 0;
    width: min(100%, 220px);
  }
}
</style>
