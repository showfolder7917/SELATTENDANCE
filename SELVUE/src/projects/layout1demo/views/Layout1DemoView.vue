<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import LanguageSwitch from '../../../shared/components/LanguageSwitch.vue'
import ThemeSwitch from '../../../shared/components/ThemeSwitch.vue'
import ThreePaneWorkbenchLayout from '../../../shared/components/ThreePaneWorkbenchLayout.vue'
import { useWorkbenchShellStyle } from '../../../shared/composables/useWorkbenchShellStyle'
import {
  attendancePunchLayoutPreset,
  attendanceShellLayoutPreset,
  workbenchViewportBreakpoints,
  workbenchViewportMetrics
} from '../../../shared/constants/workbenchLayoutConfig'
import { useLayout1DemoWorkbench } from '../composables/useLayout1DemoWorkbench'

// demo 视图只负责拼装布局1号的英雄栏、左侧导航、中间表格和右侧表单，不直接散落接口细节。
const {
  locale,
  localeOptions,
  themeId,
  themeOptions,
  sourceLabel,
  loadError,
  heroOut,
  navItems,
  summaryCards,
  filterPreset,
  tableColumns,
  tableRows,
  formFields,
  actionCards,
  promptDocPath,
  t
} = useLayout1DemoWorkbench()
// 布局1号页面壳也改成 shared profile 驱动，后续其他 demo 或正式工程可以直接复用同一个入口。
const { shellStyle } = useWorkbenchShellStyle('wideWorkbench')

// 英雄栏引用用于测量当前顶部真实高度，再把剩余视口交给 workbench 主体滚动区。
const heroSectionRef = ref(null)
// 左栏占位容器引用用于读取当前横向位置和宽度，保证吸附后不跳位。
const sidebarPaneRef = ref(null)
// 左栏实际导航盒子引用用于在悬浮时保留原高度，避免主区回流抖动。
const sidebarStickRef = ref(null)
// 工作台主体高度单独记录，让布局1号和 attendance 一样走“hero 下局部滚动”而不是整页自然撑高。
const workbenchViewportHeight = ref(null)
// 左栏吸附状态统一收口在这里，模板只负责消费最终定位样式。
const sidebarFloatingState = ref({
  active: false,
  left: 0,
  width: 0,
  height: 0
})
// hero 高度变化监听器需要在离开页面时释放，避免 demo 切页后残留观察器。
let heroResizeObserver = null

// 根据 hero 当前底边回算剩余视口高度，让三栏主体和 attendance 一样被限制在固定工作区里滚动。
function syncWorkbenchViewportHeight() {
  if (!heroSectionRef.value) return
  const heroBottom = heroSectionRef.value.getBoundingClientRect().bottom
  // 顶部间距和底部呼吸空间统一改读 shared 指标，保证布局1号和正式工程保持同一首屏节奏。
  workbenchViewportHeight.value = Math.max(
    420,
    Math.floor(
      window.innerHeight -
        heroBottom -
        workbenchViewportMetrics.topGapPx -
        workbenchViewportMetrics.bottomBreathingPx
    )
  )
}

// 根据当前滚动位置和左栏几何信息决定布局1号左栏是否切到固定吸附态。
function syncSidebarFloating() {
  if (!sidebarPaneRef.value || !sidebarStickRef.value) return
  // 小屏幕直接回到自然文档流，保持堆叠模式可读性，不再维持吸附侧栏。
  if (window.innerWidth <= workbenchViewportBreakpoints.splitStackedMaxWidth) {
    sidebarFloatingState.value = { active: false, left: 0, width: 0, height: 0 }
    return
  }
  const paneRect = sidebarPaneRef.value.getBoundingClientRect()
  const stickRect = sidebarStickRef.value.getBoundingClientRect()
  const shouldFloat = paneRect.top <= 20
  sidebarFloatingState.value = {
    active: shouldFloat,
    // 吸附后继续复用当前左栏横向坐标，避免拖拽分栏后侧栏突然偏移。
    left: paneRect.left,
    // 吸附后锁定当前列宽，避免导航文本重新换行导致视觉节奏变化。
    width: paneRect.width,
    // 占位容器使用当前真实高度，避免左栏吸附时主区整体上跳。
    height: stickRect.height
  }
}

// 左栏进入固定吸附后，外层占位容器继续保留原高度，保证整体分栏骨架稳定。
const sidebarPaneStyle = computed(() => {
  if (!sidebarFloatingState.value.active) {
    return {}
  }
  return {
    minHeight: `${Math.ceil(sidebarFloatingState.value.height)}px`
  }
})

// 左栏固定吸附时直接输出最终定位信息，让模板层不再自己拼接布局细节。
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

// 页面挂载后先完成一次几何计算，再开始监听 hero 高度、滚动和窗口变化。
onMounted(async () => {
  await nextTick()
  syncWorkbenchViewportHeight()
  syncSidebarFloating()
  // 英雄栏会因为语言切换、视口变窄或按钮换行而改变高度，需要实时回算主体高度。
  heroResizeObserver = new ResizeObserver(() => {
    syncWorkbenchViewportHeight()
  })
  if (heroSectionRef.value) {
    heroResizeObserver.observe(heroSectionRef.value)
  }
  window.addEventListener('scroll', syncSidebarFloating, { passive: true })
  window.addEventListener('resize', syncWorkbenchViewportHeight)
  window.addEventListener('resize', syncSidebarFloating)
})

// 页面卸载时清理观察器和事件监听，避免离开布局1号后继续读取旧节点。
onBeforeUnmount(() => {
  heroResizeObserver?.disconnect()
  window.removeEventListener('scroll', syncSidebarFloating)
  window.removeEventListener('resize', syncWorkbenchViewportHeight)
  window.removeEventListener('resize', syncSidebarFloating)
})
</script>

<template>
  <!-- 布局1号根壳固定复用 shared 首屏壳，让 demo 本身也成为标准化布局示例。 -->
  <div
    class="seladmin-page sellayout1-page-shell selshared-entry-page-shell selshared-entry-page-shell--hero-workbench selshared-entry-page-shell--wide-workbench"
    :style="shellStyle"
  >
    <!-- 顶部英雄栏固定承接布局名、用途说明、主题语言切换和宿主 project 切换入口。 -->
    <header ref="heroSectionRef" class="seladmin-hero seladmin-surface sellayout1-hero selshared-entry-hero">
      <div class="sellayout1-hero-copy">
        <!-- 英雄栏眉标直接告诉使用者当前看的就是布局1号模板。 -->
        <p class="seladmin-eyebrow">{{ heroOut.stageTag }}</p>
        <!-- 英雄栏主标题固定说明 demo 工程目的。 -->
        <h1>{{ heroOut.title }}</h1>
        <!-- 英雄栏副标题直接解释这套布局适合承接什么页面。 -->
        <p class="seladmin-copy">{{ heroOut.lead }}</p>
        <!-- 英雄栏补充区直接回显后端接口和当前数据来源，方便前后台联调时快速判断状态。 -->
        <div class="sellayout1-hero-meta">
          <span class="sellayout1-hero-pill">{{ t('backendLink') }}：{{ heroOut.backendPath }}</span>
          <span class="sellayout1-hero-pill is-muted">{{ sourceLabel }}</span>
        </div>
      </div>

      <!-- 顶部操作带继续复用 shared 主题、语言和宿主工程切换体系。 -->
      <div class="selattendance-hero-actions">
        <ThemeSwitch v-model="themeId" :options="themeOptions" :label="t('layoutGuide')" :t="(key) => key" />
        <LanguageSwitch v-model="locale" :options="localeOptions" />
        <!-- 宿主 project 切换器仍通过 teleport 挂进这里，保证 demo 也和正式工程接法一致。 -->
        <div id="project-host-toolbar-target" class="selattendance-hero-toolbar-slot"></div>
      </div>
    </header>

    <!-- 主工作台区固定采用左导航 + 中表格 + 右表单的三栏形态，作为布局1号的核心演示。 -->
    <ThreePaneWorkbenchLayout
      class="sellayout1-workbench sellayout1-workbench-shell"
      :style="workbenchViewportHeight ? { height: `${workbenchViewportHeight}px` } : {}"
      outer-storage-key="layout1demo-shell-split"
      :outer-default-left-percent="attendanceShellLayoutPreset.outerDefaultLeftPercent"
      :outer-min-left-percent="attendanceShellLayoutPreset.outerMinLeftPercent"
      :outer-max-left-percent="attendanceShellLayoutPreset.outerMaxLeftPercent"
      inner-storage-key="layout1demo-main-split"
      :inner-default-left-percent="attendancePunchLayoutPreset.outerDefaultLeftPercent"
      :inner-min-left-percent="attendancePunchLayoutPreset.outerMinLeftPercent"
      :inner-max-left-percent="attendancePunchLayoutPreset.outerMaxLeftPercent"
    >
      <template #left>
        <!-- 左栏也补齐 attendance 同口径的占位容器和吸附容器，避免只分栏不吸附导致观感仍不一样。 -->
        <div ref="sidebarPaneRef" class="sellayout1-sidebar-pane" :style="sidebarPaneStyle">
          <div ref="sidebarStickRef" class="sellayout1-sidebar-stick" :style="sidebarStickStyle">
            <aside class="sellayout1-nav seladmin-surface">
              <header class="sellayout1-panel-header">
                <h2>{{ t('navTitle') }}</h2>
                <p class="seladmin-copy">{{ t('navLead') }}</p>
              </header>

              <!-- 导航列表固定展示“标题 + 说明 + 数量”，并随左栏吸附与分栏比例一起稳定布局。 -->
              <ul class="sellayout1-nav-list">
                <li v-for="item in navItems" :key="item.id" class="sellayout1-nav-item">
                  <div class="sellayout1-nav-copy">
                    <strong>{{ locale === 'ja' ? item.titleJa : item.titleZh }}</strong>
                    <small>{{ locale === 'ja' ? item.leadJa : item.leadZh }}</small>
                  </div>
                  <span class="sellayout1-nav-badge">{{ item.count }}</span>
                </li>
              </ul>
            </aside>
          </div>
        </div>
      </template>

      <template #main>
        <!-- 中间主表格栏继续承接摘要卡、筛选栏和表格主体，但可用宽度改由 shared 内层分栏伸缩。 -->
        <main class="sellayout1-table-panel seladmin-surface">
          <header class="sellayout1-panel-header">
            <div>
              <h2>{{ t('tableTitle') }}</h2>
              <p class="seladmin-copy">{{ t('tableLead') }}</p>
            </div>

            <!-- 摘要卡仍固定挂在中栏头部右侧，但宽度空间不再被写死网格上限限制。 -->
            <div class="sellayout1-summary-grid">
              <article
                v-for="card in summaryCards"
                :key="card.id"
                class="sellayout1-summary-card"
                :class="`is-${card.tone}`"
              >
                <strong>{{ card.value }}</strong>
                <small>{{ locale === 'ja' ? card.titleJa : card.titleZh }}</small>
              </article>
            </div>
          </header>

          <!-- 筛选栏保持放在表格上方，继续示范布局1号里搜索条件应如何归位。 -->
          <section class="sellayout1-filter-bar">
            <label class="seladmin-field">
              <span>{{ t('filterDateFrom') }}</span>
              <input :value="filterPreset.dateFrom" type="text" readonly />
            </label>
            <label class="seladmin-field">
              <span>{{ t('filterDateTo') }}</span>
              <input :value="filterPreset.dateTo" type="text" readonly />
            </label>
            <label class="seladmin-field">
              <span>{{ t('filterKeyword') }}</span>
              <input :value="filterPreset.keyword" type="text" readonly />
            </label>
            <label class="seladmin-field">
              <span>{{ t('filterSourceSystem') }}</span>
              <input :value="filterPreset.sourceSystem" type="text" readonly />
            </label>
            <label class="seladmin-field">
              <span>{{ t('filterStatus') }}</span>
              <input :value="filterPreset.status" type="text" readonly />
            </label>
          </section>

          <!-- 表格主体固定占据中栏主要空间，并随 shared 宽屏工作台继续扩展。 -->
          <div class="sellayout1-table-shell">
            <table class="sellayout1-table">
              <thead>
                <tr>
                  <th v-for="column in tableColumns" :key="column.key">{{ column.label }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in tableRows" :key="`${row.externalPunchId}-${row.punchTime}`">
                  <td>{{ row.employeeName }}</td>
                  <td>{{ row.externalPunchId }}</td>
                  <td>{{ row.punchTime }}</td>
                  <td>{{ row.punchType }}</td>
                  <td>{{ row.sourceSystem }}</td>
                  <td>
                    <span class="sellayout1-status-pill" :class="`is-${row.status.toLowerCase()}`">
                      {{ row.status }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </main>
      </template>

      <template #side>
        <!-- 右侧栏固定由表单演示和其他处理项两块组成，但宽度现在交给 shared 右栏分配。 -->
        <aside class="sellayout1-side">
          <section class="sellayout1-form-panel seladmin-surface">
            <header class="sellayout1-panel-header">
              <h2>{{ t('formTitle') }}</h2>
              <p class="seladmin-copy">{{ t('formLead') }}</p>
            </header>

            <!-- 表单区固定展示当前选中记录要编辑的主字段，让 AI 后续照着重建右栏。 -->
            <div class="sellayout1-form-grid">
              <label v-for="field in formFields" :key="field.label" class="seladmin-field">
                <span>{{ field.label }}</span>
                <textarea v-if="field.label === t('formRemark')" :value="field.value" rows="4" readonly />
                <input v-else :value="field.value" type="text" readonly />
              </label>
            </div>
          </section>

          <section class="sellayout1-action-panel seladmin-surface">
            <header class="sellayout1-panel-header">
              <h2>{{ t('actionsTitle') }}</h2>
              <p class="seladmin-copy">{{ t('actionsLead') }}</p>
            </header>

            <!-- 处理项卡片固定展示导入、提示词和其他辅助动作，让右栏不只承载表单。 -->
            <div class="sellayout1-action-list">
              <article v-for="card in actionCards" :key="card.id" class="sellayout1-action-card">
                <strong>{{ locale === 'ja' ? card.titleJa : card.titleZh }}</strong>
                <p>{{ locale === 'ja' ? card.leadJa : card.leadZh }}</p>
                <code>{{ card.primaryValue }}</code>
                <small>{{ card.secondaryValue }}</small>
              </article>
            </div>

            <!-- 提示词路径固定放在右栏底部，方便以后直接按“布局1号”提示词复建。 -->
            <div class="sellayout1-prompt-box">
              <span class="sellayout1-prompt-label">{{ t('promptDoc') }}</span>
              <code>{{ promptDocPath }}</code>
              <p>{{ t('rebuildHint') }}</p>
              <p v-if="loadError" class="sellayout1-fallback-note">{{ loadError }}</p>
            </div>
          </section>
        </aside>
      </template>
    </ThreePaneWorkbenchLayout>
  </div>
</template>
