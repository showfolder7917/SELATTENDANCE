<script setup>
import LanguageSwitch from '../../../shared/components/LanguageSwitch.vue'
import SharedMetricCards from '../../../shared/components/SharedMetricCards.vue'
import SharedWorkbenchHeader from '../../../shared/components/SharedWorkbenchHeader.vue'
import ThemeSwitch from '../../../shared/components/ThemeSwitch.vue'
import ThreePaneWorkbenchLayout from '../../../shared/components/ThreePaneWorkbenchLayout.vue'
import HostContextSection from '../components/HostContextSection.vue'
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
  messageText,
  messageTone,
  loginPending,
  savePending,
  reloadPending,
  currentUser,
  hostContext,
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
  currentUserEntries,
  hostContextPreview,
  permissionReferenceRows,
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
  editMenu
} = useUniauthWorkbench()

// 主题切换独立于业务状态，保证切换视觉主题时不会影响当前表单和登录态。
const { themeId, themeOptions } = useUniauthTheme()
</script>

<template>
  <!-- 权限中心根壳沿用 attendance 的 page shell 节奏，并复用同一套主题变量。 -->
  <div class="seladmin-page seluniauth-page-shell">
    <!-- 顶部 hero 负责解释当前是第九阶段统一权限中心，并承接语言和主题切换。 -->
    <header class="seladmin-hero seladmin-surface seluniauth-hero">
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

    <!-- 页面级提示统一放在 hero 下方，保持和 attendance 一样的反馈层级。 -->
    <section v-if="messageText" class="seluniauth-message" :class="`is-${messageTone}`">
      {{ messageText }}
    </section>

    <!-- 未登录时只展示登录区块，不渲染后续工作台结构。 -->
    <LoginSection
      v-if="!authSession?.accessToken"
      :title="t('loginTitle')"
      :lead="t('loginHint')"
      :login-form="loginForm"
      :login-pending="loginPending"
      :t="t"
      @submit="submitLogin"
    />

    <!-- 已登录后按 attendance 的三分栏工作台装配权限中心。 -->
    <ThreePaneWorkbenchLayout
      v-else
      class="seluniauth-workbench"
      outer-storage-key="uniauth-shell-split"
      :outer-default-left-percent="18"
      :outer-min-left-percent="12"
      :outer-max-left-percent="24"
      inner-storage-key="uniauth-content-split"
      :inner-default-left-percent="70"
      :inner-min-left-percent="56"
      :inner-max-left-percent="82"
    >
      <template #left>
        <!-- 左侧导航只负责模块切换和全局动作，节奏完全对齐 attendance 侧栏。 -->
        <aside class="selattendance-sidebar seladmin-surface">
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
      </template>

      <template #main>
        <!-- 主内容区负责当前模块头部和当前区块本体，不再掺杂宿主桥接与用户快照。 -->
        <main class="seluniauth-content">
          <SharedWorkbenchHeader
            class="seluniauth-content-header seladmin-surface"
            :eyebrow="t('workspaceStatus')"
            :title="activeSectionMeta.title"
            :lead="activeSectionMeta.lead"
            split-mode="left-summary-right-metrics"
          >
            <template #metrics>
              <!-- 当前区块摘要卡直接复用 attendance 的共享指标卡。 -->
              <SharedMetricCards class="seluniauth-header-metrics" :items="metricItems" />
            </template>

            <template #actions>
              <!-- 头部只保留当前用户与租户焦点，帮助管理员确认当前操作上下文。 -->
              <div class="seluniauth-header-status">
                <span class="seluniauth-status-pill">
                  {{ currentUser?.displayName || t('noSession') }}
                </span>
                <span class="seluniauth-status-pill is-muted">
                  {{ currentUser?.tenantCode || '-' }}
                </span>
              </div>
            </template>
          </SharedWorkbenchHeader>

          <!-- 模块管理区块承接统一权限中心托管工程主数据维护。 -->
          <ModuleSection
            v-if="activeSection === 'module'"
            :module-form="moduleForm"
            :module-rows="moduleRows"
            :save-pending="savePending"
            :t="t"
            @submit="submitModule"
            @edit="editModule"
          />

          <!-- 租户管理区块承接平台租户主数据维护。 -->
          <TenantSection
            v-else-if="activeSection === 'tenant'"
            :tenant-form="tenantForm"
            :tenant-rows="tenantRows"
            :save-pending="savePending"
            :t="t"
            @submit="submitTenant"
            @edit="editTenant"
          />

          <!-- 用户管理区块承接账号资料与角色绑定维护。 -->
          <UserSection
            v-else-if="activeSection === 'user'"
            :user-form="userForm"
            :user-rows="userRows"
            :save-pending="savePending"
            :t="t"
            @submit="submitUser"
            @edit="editUser"
          />

          <!-- 角色管理区块承接角色主资料和授权表达维护。 -->
          <RoleSection
            v-else-if="activeSection === 'role'"
            :role-form="roleForm"
            :role-rows="roleRows"
            :save-pending="savePending"
            :t="t"
            @submit="submitRole"
            @edit="editRole"
          />

          <!-- 菜单管理区块承接动态菜单节点和双语标题维护。 -->
          <MenuSection
            v-else
            :menu-form="menuForm"
            :menu-rows="menuRows"
            :save-pending="savePending"
            :t="t"
            @submit="submitMenu"
            @edit="editMenu"
          />
        </main>
      </template>

      <template #side>
        <!-- 右侧辅助栏固定展示当前用户快照、宿主桥接和角色权限参考。 -->
        <aside class="seluniauth-side-stack">
          <section class="seluniauth-side-panel seladmin-surface">
            <header class="seluniauth-side-panel-header">
              <h3>{{ t('currentUser') }}</h3>
              <p class="seladmin-copy">{{ t('workspaceStatus') }}</p>
            </header>

            <dl class="seluniauth-key-value-list">
              <div v-for="entry in currentUserEntries" :key="entry.key" class="seluniauth-key-value-item">
                <dt>{{ entry.label }}</dt>
                <dd>{{ entry.value }}</dd>
              </div>
            </dl>
          </section>

          <HostContextSection
            :title="t('hostCheck')"
            :lead="hostContext ? t('hostContextReady') : t('hostContextMissing')"
            :preview="hostContextPreview"
          />

          <section v-if="activeSection === 'role'" class="seluniauth-side-panel seladmin-surface">
            <header class="seluniauth-side-panel-header">
              <h3>{{ t('permissionReference') }}</h3>
              <p class="seladmin-copy">{{ t('sectionRoleHint') }}</p>
            </header>

            <ul class="seluniauth-reference-list">
              <li v-for="item in permissionReferenceRows" :key="item.id">
                <strong>{{ item.label }}</strong>
                <small>{{ item.hint }}</small>
              </li>
            </ul>
          </section>
        </aside>
      </template>
    </ThreePaneWorkbenchLayout>
  </div>
</template>
