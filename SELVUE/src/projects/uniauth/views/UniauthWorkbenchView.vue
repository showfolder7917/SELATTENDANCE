<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import AlertDialog from '../../../shared/components/AlertDialog.vue'
import LanguageSwitch from '../../../shared/components/LanguageSwitch.vue'
import ThemeSwitch from '../../../shared/components/ThemeSwitch.vue'
import ThreePaneWorkbenchLayout from '../../../shared/components/ThreePaneWorkbenchLayout.vue'
import { useWorkbenchShellStyle } from '../../../shared/composables/useWorkbenchShellStyle'
import {
  uniauthContentLayoutPreset,
  uniauthShellLayoutPreset,
  workbenchViewportBreakpoints,
  workbenchViewportMetrics
} from '../../../shared/constants/workbenchLayoutConfig'
import LoginSection from '../components/LoginSection.vue'
import MenuSection from '../components/MenuSection.vue'
import ModuleSection from '../components/ModuleSection.vue'
import RoleSection from '../components/RoleSection.vue'
import TenantSection from '../components/TenantSection.vue'
import UniauthSectionNav from '../components/UniauthSectionNav.vue'
import UserSection from '../components/UserSection.vue'
import { useUniauthTheme } from '../composables/useUniauthTheme'
import { useUniauthWorkbench } from '../composables/useUniauthWorkbench'

// view 层只负责拼装 shared header、三分栏和五个 section，不再维护任何请求副作用。
const {
  locale,
  localeOptions,
  authSession,
  activeSection,
  errorDialog,
  loginPending,
  savePending,
  reloadPending,
  loginForm,
  moduleForm,
  tenantForm,
  userForm,
  roleForm,
  menuForm,
  moduleRows,
  tenantRows,
  userRows,
  roleRows,
  menuRows,
  navItems,
  activeSectionMeta,
  metricItems,
  t,
  reloadWorkbench,
  submitLogin,
  restartLogin,
  signOut,
  submitModule,
  submitTenant,
  submitUser,
  submitRole,
  submitMenu,
  editModule,
  editTenant,
  editUser,
  editRole,
  editMenu,
  closeErrorDialog
} = useUniauthWorkbench()

// 主题切换独立于业务状态，保证切换视觉主题时不会影响当前表单和登录态。
const { themeId, themeOptions } = useUniauthTheme()
// 权限中心既有登录首屏又有正式三栏工作台，所以页面壳直接切到宽工作台 profile。
const { shellStyle } = useWorkbenchShellStyle('wideWorkbench')
// hero 区块引用用于测量顶部真实高度，再把剩余视口分配给下方工作台。
const heroSectionRef = ref(null)
// 左侧导航占位容器引用用于读取当前列的横向位置和宽度。
const sidebarPaneRef = ref(null)
// 左侧真正吸附的导航盒子引用用于保留原高度并计算吸附尺寸。
const sidebarStickRef = ref(null)
// 工作台剩余视口高度单独记录，保证权限中心也走“hero 下局部滚动”模式。
const workbenchViewportHeight = ref(null)
// 左栏吸附状态统一在这里维护，让模板只消费最终定位结果。
const sidebarFloatingState = ref({
  active: false,
  left: 0,
  width: 0,
  height: 0
})
// hero 尺寸监听器单独保存，离开页面时要主动释放。
let heroResizeObserver = null

// 根据 hero 当前底边回算剩余视口高度，让权限中心主工作台固定在首屏内滚动。
function syncWorkbenchViewportHeight() {
  if (!heroSectionRef.value) return
  // 成功和信息提示已不再占用 hero 下方布局，所以高度只需要按 hero 实际底边回算。
  const heroBottom = heroSectionRef.value.getBoundingClientRect().bottom
  // 小屏单列模式允许工作台更矮一些，避免 hero 和消息区变高时首屏再次被最小高度顶出。
  const minimumWorkbenchHeight =
    window.innerWidth <= workbenchViewportBreakpoints.splitStackedMaxWidth ? 240 : 420
  // 顶部间距和底部呼吸空间统一走 shared 指标，保证权限中心和其他工作台页看到同样的 hero 下节奏。
  workbenchViewportHeight.value = Math.max(
    minimumWorkbenchHeight,
    Math.floor(
      window.innerHeight -
        heroBottom -
        workbenchViewportMetrics.topGapPx -
        workbenchViewportMetrics.bottomBreathingPx
    )
  )
}

// 根据左栏当前位置决定是否切到固定吸附态，让导航在长列表编辑时仍可稳定停留。
function syncSidebarFloating() {
  if (!sidebarPaneRef.value || !sidebarStickRef.value) return
  // 小屏和堆叠断点以下直接取消吸附，避免单列模式下出现悬浮遮挡。
  if (window.innerWidth <= workbenchViewportBreakpoints.splitStackedMaxWidth) {
    sidebarFloatingState.value = { active: false, left: 0, width: 0, height: 0 }
    return
  }
  const paneRect = sidebarPaneRef.value.getBoundingClientRect()
  const stickRect = sidebarStickRef.value.getBoundingClientRect()
  const shouldFloat = paneRect.top <= 20
  sidebarFloatingState.value = {
    active: shouldFloat,
    // 吸附后继续沿用当前列的横向坐标，避免拖拽分栏后导航突然横向跳位。
    left: paneRect.left,
    // 吸附后锁住当前列宽，避免导航文案重新换行导致视觉节奏抖动。
    width: paneRect.width,
    // 占位容器保留当前真实高度，避免吸附瞬间主内容区整体上跳。
    height: stickRect.height
  }
}

// 左栏进入吸附态后，外层占位容器继续保留原高度，保证三栏骨架稳定。
const sidebarPaneStyle = computed(() => {
  if (!sidebarFloatingState.value.active) {
    return {}
  }
  return {
    minHeight: `${Math.ceil(sidebarFloatingState.value.height)}px`
  }
})

// 左栏吸附时直接输出最终定位样式，让模板层不再自己拼接 fixed 坐标。
const sidebarStickStyle = computed(() => {
  if (!sidebarFloatingState.value.active) {
    return {}
  }
  return {
    position: 'fixed',
    top: '20px',
    left: `${Math.round(sidebarFloatingState.value.left)}px`,
    width: `${Math.round(sidebarFloatingState.value.width)}px`
  }
})

// 模块管理英雄头需要独立承接统计卡，所以在 view 层直接按当前模块列表计算一组稳定指标。
const moduleHeroMetrics = computed(() => [
  { key: 'moduleCount', value: moduleRows.value.length, label: t('summaryModule'), tone: 'default' },
  {
    key: 'enabledModuleCount',
    value: moduleRows.value.filter((row) => row.enabledFlag).length,
    label: t('enabledFlag'),
    tone: 'warm'
  },
  {
    key: 'entryProjectCount',
    value: new Set(moduleRows.value.map((row) => row.entryProject).filter(Boolean)).size,
    label: t('entryProject'),
    tone: 'muted'
  }
])

// 所有管理区块统一走同一套英雄头卡片；模块管理继续保留已经确认的指标算法，其余区块复用 metricItems。
const heroMetrics = computed(() => (activeSection.value === 'module' ? moduleHeroMetrics.value : metricItems.value))

// 顶部监听器只需要关注 hero 尺寸变化，因为页内消息布局已经被删除。
function bindHeroResizeObserver() {
  heroResizeObserver?.disconnect()
  heroResizeObserver = new ResizeObserver(() => {
    syncWorkbenchViewportHeight()
  })
  if (heroSectionRef.value) {
    heroResizeObserver.observe(heroSectionRef.value)
  }
}

// 页面挂载后先完成一次几何回算，再开始监听 hero 高度、滚动和窗口变化。
onMounted(async () => {
  await nextTick()
  syncWorkbenchViewportHeight()
  syncSidebarFloating()
  // hero 会因语言切换或顶部工具带换行而改变高度，需要实时回算工作台高度。
  bindHeroResizeObserver()
  window.addEventListener('scroll', syncSidebarFloating, { passive: true })
  window.addEventListener('resize', syncWorkbenchViewportHeight)
  window.addEventListener('resize', syncSidebarFloating)
})

// 登录成功或退出回到首屏时主动把浏览器滚回顶部，避免输入框聚焦遗留的滚动位置继续影响 hero 展示。
watch(
  () => authSession.value?.accessToken,
  async () => {
    await nextTick()
    window.scrollTo({ top: 0, left: 0, behavior: 'auto' })
    bindHeroResizeObserver()
    syncWorkbenchViewportHeight()
    syncSidebarFloating()
  }
)

// 页面卸载时清理观察器和全局监听，避免切换工程后继续读取旧节点。
onBeforeUnmount(() => {
  heroResizeObserver?.disconnect()
  window.removeEventListener('scroll', syncSidebarFloating)
  window.removeEventListener('resize', syncWorkbenchViewportHeight)
  window.removeEventListener('resize', syncSidebarFloating)
})

// 模块管理需要保留一个显式“新建模块”入口，所以这里直接复用编辑回填逻辑把表单复位到默认值。
function resetModuleForm() {
  editModule({})
}
</script>

<template>
  <!-- 权限中心根壳沿用 attendance 的 page shell 节奏，并复用同一套主题变量。 -->
  <div
    class="seladmin-page seluniauth-page-shell selshared-entry-page-shell selshared-entry-page-shell--hero-workbench selshared-entry-page-shell--wide-workbench"
    :style="shellStyle"
  >
    <!-- 顶部 hero 负责解释当前是第九阶段统一权限中心，并承接语言和主题切换。 -->
    <header ref="heroSectionRef" class="seladmin-hero seladmin-surface seluniauth-hero selshared-entry-hero">
      <div>
        <p class="seladmin-eyebrow">{{ t('liveTag') }}</p>
        <h1>{{ t('appTitle') }}</h1>
        <p class="seladmin-copy">{{ t('appSubtitle') }}</p>
      </div>
      <div class="selattendance-hero-actions">
        <ThemeSwitch v-model="themeId" :options="themeOptions" :label="t('themeSwitch')" :t="t" />
        <LanguageSwitch v-model="locale" :options="localeOptions" />
        <!-- 宿主 project 切换器通过 teleport 挂进这里，并固定排在语言切换器后面。 -->
        <div id="project-host-toolbar-target" class="selattendance-hero-toolbar-slot"></div>
        <!-- 已登录时额外提供重新登录快捷入口，让令牌失效或切换账号时不用先去侧栏找退出。 -->
        <button
          v-if="authSession?.accessToken"
          type="button"
          class="selattendance-hero-action-button"
          @click="restartLogin"
        >
          {{ t('relogin') }}
        </button>
      </div>
    </header>

    <!-- 未登录时把登录区块放进独立舞台，让 hero 下方剩余视口真正用于首屏居中展示。 -->
    <section v-if="!authSession?.accessToken" class="seluniauth-login-stage selshared-entry-stage">
      <LoginSection
        :title="t('loginTitle')"
        :lead="t('loginHint')"
        :login-form="loginForm"
        :login-pending="loginPending"
        :t="t"
        @submit="submitLogin"
      />
    </section>

    <!-- 已登录后按 attendance 的三分栏工作台装配权限中心。 -->
    <ThreePaneWorkbenchLayout
      v-else
      class="seluniauth-workbench seluniauth-workbench-shell"
      :style="workbenchViewportHeight ? { height: `${workbenchViewportHeight}px` } : {}"
      outer-storage-key="uniauth-shell-split"
      :outer-default-left-percent="uniauthShellLayoutPreset.outerDefaultLeftPercent"
      :outer-min-left-percent="uniauthShellLayoutPreset.outerMinLeftPercent"
      :outer-max-left-percent="uniauthShellLayoutPreset.outerMaxLeftPercent"
      inner-storage-key="uniauth-content-split"
      :inner-default-left-percent="uniauthContentLayoutPreset.outerDefaultLeftPercent"
      :inner-min-left-percent="uniauthContentLayoutPreset.outerMinLeftPercent"
      :inner-max-left-percent="uniauthContentLayoutPreset.outerMaxLeftPercent"
    >
      <template #left>
        <!-- 左栏补齐布局1号同口径的占位层和吸附层，让导航在长工作台中始终稳定可见。 -->
        <div ref="sidebarPaneRef" class="seluniauth-sidebar-pane" :style="sidebarPaneStyle">
          <div ref="sidebarStickRef" class="seluniauth-sidebar-stick" :style="sidebarStickStyle">
            <aside class="selattendance-sidebar seladmin-surface seluniauth-sidebar-card">
              <div class="selattendance-sidebar-header">
                <p class="seladmin-eyebrow">{{ t('workspaceSidebarTitle') }}</p>
                <p class="seladmin-copy">{{ t('workspaceSidebarHint') }}</p>
              </div>

              <UniauthSectionNav
                v-model:active-section="activeSection"
                :nav-items="navItems"
                :active-section="activeSection"
                :title="t('workspaceSidebarTitle')"
              />

              <div class="seluniauth-sidebar-actions">
                <button
                  type="button"
                  class="seladmin-button seladmin-button-secondary"
                  :disabled="reloadPending"
                  @click="reloadWorkbench()"
                >
                  {{ reloadPending ? `${t('reload')}...` : t('reload') }}
                </button>
                <button type="button" class="seladmin-button seladmin-button-secondary" @click="signOut">
                  {{ t('signOut') }}
                </button>
              </div>
            </aside>
          </div>
        </div>
      </template>

      <template #main>
        <!-- 中栏正式收口成布局1号的主工作板：先头部，再当前模块的双栏编辑区。 -->
        <main class="seluniauth-content">
          <section class="seluniauth-main-board seladmin-surface">
            <section class="seladmin-hero seladmin-surface seluniauth-module-hero">
              <div class="seluniauth-module-hero-copy">
                <p class="seladmin-eyebrow">{{ t('workspaceStatus') }}</p>
                <h2>{{ activeSectionMeta.title }}</h2>
                <p class="seladmin-copy">{{ activeSectionMeta.lead }}</p>
              </div>
              <div class="seluniauth-module-summary-grid seluniauth-module-hero-metrics">
                <article
                  v-for="card in heroMetrics"
                  :key="card.key"
                  class="seluniauth-module-summary-card"
                  :class="`is-${card.tone}`"
                >
                  <strong>{{ card.value }}</strong>
                  <small>{{ card.label }}</small>
                </article>
              </div>
            </section>

            <div class="seluniauth-main-shell">
              <!-- 模块管理区块在中栏只保留主列表，让布局1号的主工作板职责回到表格浏览。 -->
              <ModuleSection
                v-if="activeSection === 'module'"
                pane="table"
                :module-form="moduleForm"
                :module-rows="moduleRows"
                :save-pending="savePending"
                :t="t"
                @submit="submitModule"
                @edit="editModule"
                @reset="resetModuleForm"
              />

              <!-- 租户管理区块在中栏只保留租户列表，当前记录编辑改由右栏承接。 -->
              <TenantSection
                v-else-if="activeSection === 'tenant'"
                pane="table"
                :tenant-form="tenantForm"
                :tenant-rows="tenantRows"
                :save-pending="savePending"
                :t="t"
                @submit="submitTenant"
                @edit="editTenant"
              />

              <!-- 用户管理区块在中栏只保留账号主列表，避免再次在中栏拆出内部双列。 -->
              <UserSection
                v-else-if="activeSection === 'user'"
                pane="table"
                :user-form="userForm"
                :user-rows="userRows"
                :save-pending="savePending"
                :t="t"
                @submit="submitUser"
                @edit="editUser"
              />

              <!-- 角色管理区块在中栏只保留角色列表，让授权编辑彻底移动到右栏。 -->
              <RoleSection
                v-else-if="activeSection === 'role'"
                pane="table"
                :role-form="roleForm"
                :role-rows="roleRows"
                :save-pending="savePending"
                :t="t"
                @submit="submitRole"
                @edit="editRole"
              />

              <!-- 菜单管理区块在中栏只保留菜单节点列表，右栏承接节点编辑。 -->
              <MenuSection
                v-else
                pane="table"
                :menu-form="menuForm"
                :menu-rows="menuRows"
                :save-pending="savePending"
                :t="t"
                @submit="submitMenu"
                @edit="editMenu"
              />
            </div>
          </section>
        </main>
      </template>

      <template #side>
        <!-- 右栏参照布局1号固定承接当前记录表单和其他处理项，旧的辅助信息顺序整体后移。 -->
        <aside class="seluniauth-side-stack">
          <!-- 模块管理区块在右栏承接当前模块编辑表单。 -->
          <ModuleSection
            v-if="activeSection === 'module'"
            pane="form"
            :module-form="moduleForm"
            :module-rows="moduleRows"
            :save-pending="savePending"
            :t="t"
            @submit="submitModule"
            @edit="editModule"
            @reset="resetModuleForm"
          />

          <!-- 租户管理区块在右栏承接当前租户编辑表单。 -->
          <TenantSection
            v-else-if="activeSection === 'tenant'"
            pane="form"
            :tenant-form="tenantForm"
            :tenant-rows="tenantRows"
            :save-pending="savePending"
            :t="t"
            @submit="submitTenant"
            @edit="editTenant"
          />

          <!-- 用户管理区块在右栏承接当前账号编辑表单。 -->
          <UserSection
            v-else-if="activeSection === 'user'"
            pane="form"
            :user-form="userForm"
            :user-rows="userRows"
            :save-pending="savePending"
            :t="t"
            @submit="submitUser"
            @edit="editUser"
          />

          <!-- 角色管理区块在右栏承接授权表单，权限参考继续放在后续处理项中。 -->
          <RoleSection
            v-else-if="activeSection === 'role'"
            pane="form"
            :role-form="roleForm"
            :role-rows="roleRows"
            :save-pending="savePending"
            :t="t"
            @submit="submitRole"
            @edit="editRole"
          />

          <!-- 菜单管理区块在右栏承接节点编辑表单。 -->
          <MenuSection
            v-else
            pane="form"
            :menu-form="menuForm"
            :menu-rows="menuRows"
            :save-pending="savePending"
            :t="t"
            @submit="submitMenu"
            @edit="editMenu"
          />

        </aside>
      </template>
    </ThreePaneWorkbenchLayout>

    <!-- 错误反馈改成共享单按钮弹出框，保证后端返回的完整错误文案不会再被页内布局压缩。 -->
    <AlertDialog
      :open="errorDialog.open"
      :title="errorDialog.title"
      :message="errorDialog.message"
      :close-label="t('close')"
      tone="danger"
      @close="closeErrorDialog"
    />
  </div>
</template>
