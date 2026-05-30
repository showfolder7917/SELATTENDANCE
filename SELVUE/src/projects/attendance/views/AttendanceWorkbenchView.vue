<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import ConfirmDialog from '../../../shared/components/ConfirmDialog.vue'
import FloatingTipBubble from '../../../shared/components/FloatingTipBubble.vue'
import LanguageSwitch from '../../../shared/components/LanguageSwitch.vue'
import SharedMetricCards from '../../../shared/components/SharedMetricCards.vue'
import SharedWorkbenchHeader from '../../../shared/components/SharedWorkbenchHeader.vue'
import ThreePaneWorkbenchLayout from '../../../shared/components/ThreePaneWorkbenchLayout.vue'
import ThemeSwitch from '../../../shared/components/ThemeSwitch.vue'
import AttendanceSectionNav from '../components/AttendanceSectionNav.vue'
import AttendanceSummaryPanel from '../components/AttendanceSummaryPanel.vue'
import CaseSection from '../components/CaseSection.vue'
import DailySection from '../components/DailySection.vue'
import DepartmentSection from '../components/DepartmentSection.vue'
import EmployeeSection from '../components/EmployeeSection.vue'
import MonthlySection from '../components/MonthlySection.vue'
import PunchSection from '../components/PunchSection.vue'
import ScheduleSection from '../components/ScheduleSection.vue'
import ShiftTemplateSection from '../components/ShiftTemplateSection.vue'
import TenantPanel from '../components/TenantPanel.vue'
import WizardSection from '../components/WizardSection.vue'
import WorkplaceSection from '../components/WorkplaceSection.vue'
import {
  attendanceOverviewLayoutPreset,
  attendanceShellLayoutPreset
} from '../constants/workbenchLayoutPresets'
import { useAttendanceWorkbench } from '../composables/useAttendanceWorkbench'
import { useAttendanceTheme } from '../composables/useAttendanceTheme'

// workbench 视图只负责拼装页面骨架，具体业务状态和动作下沉到 composable。
const {
  locale,
  localeOptions,
  activeSection,
  loading,
  toast,
  confirmDialog,
  state,
  t,
  navItems,
  recommendedNextLabel,
  filteredDepartments,
  currentDepartmentWorkplaceName,
  filteredEmployees,
  showToast,
  submitTenant,
  submitWorkplace,
  removeWorkplace,
  submitDepartment,
  removeDepartment,
  submitEmployee,
  removeEmployee,
  submitMapping,
  submitImport,
  handleExport,
  requestConfirm,
  cancelConfirmDialog,
  submitConfirmDialog,
  submitShiftTemplate,
  loadScheduleBoard,
  runScheduleSearch,
  generateRecommended,
  removeShiftTemplate,
  selectScheduleTemplate,
  closeScheduleTemplateTip,
  applySchedule,
  removeScheduleItem,
  openBatchWizard,
  closeBatchWizard,
  nextBatchStep,
  prevBatchStep,
  confirmBatchWizard,
  copySchedulesFromLastWeek,
  copySchedulesFromLastMonth,
  runUnassignedCheck,
  handleScheduleExport,
  loadPunchLogs,
  loadDailyResults,
  loadCases,
  runPunchSearch,
  runDailySearch,
  loadMonthlyResults,
  runCaseSearch,
  runMonthlySearch,
  openPunchDetail,
  openDailyDetail,
  openCaseDetail,
  openMonthlyDetail,
  submitManualPunch,
  runPunchImportPreview,
  submitPunchImport,
  submitPunchBind,
  submitPunchIgnore,
  submitPunchReprocess,
  submitDailyRecalculate,
  submitDailyRangeRecalculate,
  submitCaseCreate,
  submitCaseAction,
  submitCaseLock,
  submitCaseUnlock,
  submitMonthlyRecalculate,
  submitMonthlyRecalculateOne,
  submitMonthlyClose,
  submitMonthlyReopen,
  submitMonthlyExport,
  editWorkplace,
  openWorkplaceDepartments,
  editDepartment,
  openDepartmentEmployees,
  openDepartmentSchedule,
  editEmployee,
  editMapping,
  editShiftTemplate,
  resetWorkplaceForm,
  resetDepartmentForm,
  clearDepartmentWorkplaceFilter,
  resetEmployeeForm
} = useAttendanceWorkbench()

// 主题切换独立于业务工作台状态，避免保存表单时误触发主题重置。
const { themeId, themeOptions } = useAttendanceTheme()

// 导入文本更新入口显式落回工作台状态，避免模板自动解包 ref 后触发无效 prop 警告。
function updateImportCsvText(nextValue) {
  state.importCsvText = nextValue
}

// 左侧导航除了标签，还补上当前模块说明和数量，减少用户在一页里四处找入口。
const workspaceNavItems = computed(() => {
  const moduleCountMap = {
    wizard: state.steps.length,
    workplace: state.workplaces.length,
    department: state.departments.length,
    employee: state.employees.length,
    shift: state.shiftTemplates.length
    ,
    schedule: state.scheduleBoard.scheduleItems.length,
    punch: state.punchLogList.total,
    daily: state.dailyList.total,
    case: state.caseList.total,
    monthly: state.monthlyList.total
  }

  const moduleHintMap = {
    wizard: t('sectionWizardHint'),
    workplace: t('sectionWorkplaceHint'),
    department: t('sectionDepartmentHint'),
    employee: t('sectionEmployeeHint'),
    shift: t('sectionShiftHint')
    ,
    schedule: t('sectionScheduleHint'),
    punch: t('sectionPunchHint'),
    daily: t('sectionDailyHint'),
    case: t('sectionCaseHint'),
    monthly: t('sectionMonthlyHint')
  }

  return navItems.value.map((item) => ({
    ...item,
    caption: moduleHintMap[item.key],
    badge: String(moduleCountMap[item.key] ?? 0)
  }))
})

// 右侧内容头使用当前激活模块的标签和说明，让用户始终知道自己在操作哪个区块。
const activeSectionMeta = computed(
  () => workspaceNavItems.value.find((item) => item.key === activeSection.value) || workspaceNavItems.value[0]
)

// 月次头部的四个指标先在页面层整理成共享卡片配置，后续其他模块接入时可直接复用同一数据结构。
const monthlyHeaderMetricItems = computed(() => [
  {
    key: 'open',
    value: state.monthlyList.summary?.openCount || 0,
    label: t('monthlySummaryOpen'),
    tone: 'default'
  },
  {
    key: 'closable',
    value: state.monthlyList.summary?.closableCount || 0,
    label: t('monthlySummaryClosable'),
    tone: 'warm'
  },
  {
    key: 'closed',
    value: state.monthlyList.summary?.closedCount || 0,
    label: t('monthlySummaryClosed'),
    tone: 'default'
  },
  {
    key: 'reopened',
    value: state.monthlyList.summary?.reopenedCount || 0,
    label: t('monthlySummaryReopened'),
    tone: 'muted'
  }
])

// 打卡头部继续沿用原有四类处理结果统计，只把展示壳切到 shared 层。
const punchHeaderMetricItems = computed(() => [
  {
    key: 'processed',
    value: state.punchLogList.summary?.processed || 0,
    label: t('punchStatusProcessed'),
    tone: 'default'
  },
  {
    key: 'unmatched',
    value: state.punchLogList.summary?.unmatched || 0,
    label: t('punchStatusUnmatched'),
    tone: 'warm'
  },
  {
    key: 'error',
    value: state.punchLogList.summary?.error || 0,
    label: t('punchStatusError'),
    tone: 'danger'
  },
  {
    key: 'ignored',
    value: state.punchLogList.summary?.ignored || 0,
    label: t('punchStatusIgnored'),
    tone: 'muted'
  }
])

// 日次头部只把统计卡配置上收到页面层，第四阶段的筛选和重算动作仍走原业务状态与接口。
const dailyHeaderMetricItems = computed(() => [
  {
    key: 'normal',
    value: state.dailyList.summary?.normalCount || 0,
    label: t('dailySummaryNormal'),
    tone: 'default'
  },
  {
    key: 'late',
    value: state.dailyList.summary?.lateCount || 0,
    label: t('dailySummaryLate'),
    tone: 'warm'
  },
  {
    key: 'missing',
    value: state.dailyList.summary?.missingClockCount || 0,
    label: t('dailySummaryMissing'),
    tone: 'danger'
  },
  {
    key: 'absence',
    value: state.dailyList.summary?.absenceCount || 0,
    label: t('dailySummaryAbsence'),
    tone: 'muted'
  }
])

// 异常处理头部保留五张统计卡，供 shared 指标卡验证多卡场景而不再让业务页重复写壳。
const caseHeaderMetricItems = computed(() => [
  {
    key: 'pending',
    value: state.caseList.summary?.pendingCount || 0,
    label: t('caseSummaryPending'),
    tone: 'warm'
  },
  {
    key: 'reviewing',
    value: state.caseList.summary?.reviewingCount || 0,
    label: t('caseSummaryReviewing'),
    tone: 'default'
  },
  {
    key: 'approved',
    value: state.caseList.summary?.approvedCount || 0,
    label: t('caseSummaryApproved'),
    tone: 'default'
  },
  {
    key: 'rejected',
    value: state.caseList.summary?.rejectedCount || 0,
    label: t('caseSummaryRejected'),
    tone: 'danger'
  },
  {
    key: 'locked',
    value: state.caseList.summary?.lockedCount || 0,
    label: t('caseSummaryLocked'),
    tone: 'muted'
  }
])

// 页面层继续只负责拼删除文案，再复用工作台统一确认入口，不再维护独立删除弹窗状态。
async function requestDeleteConfirm(targetLabel, targetName, onConfirm) {
  const namedMessage = t('confirmDeleteMessageNamed')
    .replace('{target}', targetLabel)
    .replace('{name}', targetName)
  const unnamedMessage = t('confirmDeleteMessageUnnamed').replace('{target}', targetLabel)
  // 先确认用户是否继续删除，再真正调用下层删除动作，保证所有删除入口共用同一套确认组件。
  const confirmed = await requestConfirm({
    title: t('confirmDeleteTitle'),
    message: targetName ? namedMessage : unnamedMessage,
    confirmLabel: t('confirmDeleteAction'),
    confirmVariant: 'danger'
  })
  if (confirmed) {
    await onConfirm()
  }
}

// 事业所删除文案优先展示名称，其次回退编码，避免用户只看到无意义主键。
async function requestWorkplaceDelete(id) {
  const item = state.workplaces.find((entry) => entry.id === id)
  await requestDeleteConfirm(
    t('workplaceTitle'),
    item?.workplaceName || item?.workplaceCode || '',
    () => removeWorkplace(id)
  )
}

// 部门删除沿用同一弹框能力，只替换目标模块标签和展示名称。
async function requestDepartmentDelete(id) {
  const item = state.departments.find((entry) => entry.id === id)
  await requestDeleteConfirm(
    t('departmentTitle'),
    item?.departmentName || item?.departmentCode || '',
    () => removeDepartment(id)
  )
}

// 员工删除确认优先展示员工姓名，避免在同一编号体系下误删到其他人。
async function requestEmployeeDelete(id) {
  const item = state.employees.find((entry) => entry.id === id)
  await requestDeleteConfirm(
    t('employeeTitle'),
    item?.employeeName || item?.employeeNo || '',
    () => removeEmployee(id)
  )
}

// 班次模板删除同样在页面层先确认，再把真正删除动作透传给 composable。
async function requestShiftTemplateDelete(id) {
  const item = state.shiftTemplates.find((entry) => entry.id === id)
  await requestDeleteConfirm(
    t('shiftTitle'),
    item?.templateName || item?.templateCode || '',
    () => removeShiftTemplate(id)
  )
}

// 排班删除需要把员工和日期拼成当前格标签，帮助用户确认自己要清掉的是哪一天哪位员工。
async function requestScheduleDelete(id) {
  const item = state.scheduleBoard.scheduleItems.find((entry) => entry.id === id)
  const targetName = [
    item?.employeeName || state.scheduleForm.selectedEmployeeName || '',
    item?.workDate || state.scheduleForm.selectedWorkDate || ''
  ].filter(Boolean).join(' / ')
  await requestDeleteConfirm(t('scheduleTitle'), targetName, () => removeScheduleItem(id))
}

// 这组尺寸继续留在 attendance 页面，专门服务于“首次打开时默认摆到模板区哪里”。
const scheduleTipBubbleWidth = 320
const scheduleTipBubbleHeight = 182

function clampTipCoordinate(value, min, max) {
  return Math.max(min, Math.min(max, value))
}

// 全局 tip 打开时按模板区当前可见位置初始化浮层和箭头锚点，避免首次出现漂到错误区域。
async function ensureScheduleTemplateTipLayout() {
  if (activeSection.value !== 'schedule' || !state.scheduleTemplateTip.open) {
    return
  }
  await nextTick()
  const target = document.querySelector('[data-schedule-template-target]')
  if (!target) {
    return
  }
  const rect = target.getBoundingClientRect()
  if (state.scheduleTemplateTip.anchorX == null || state.scheduleTemplateTip.anchorY == null) {
    // 默认把尾巴尖端落在右侧模板区左上角附近，首次弹出时就能明确指向“先选模板”的区域。
    state.scheduleTemplateTip.anchorX = Math.round(rect.left + 56)
    state.scheduleTemplateTip.anchorY = Math.round(rect.top + 28)
  }
  if (state.scheduleTemplateTip.bubbleX == null || state.scheduleTemplateTip.bubbleY == null) {
    // 提示框默认悬在模板区左上方，既贴近目标，又尽量不挡住模板卡片本身。
    state.scheduleTemplateTip.bubbleX = clampTipCoordinate(
      Math.round(rect.left - 26),
      16,
      window.innerWidth - (scheduleTipBubbleWidth + 16)
    )
    state.scheduleTemplateTip.bubbleY = clampTipCoordinate(
      Math.round(rect.top - 196),
      16,
      window.innerHeight - (scheduleTipBubbleHeight + 16)
    )
  }
}

// 页面层只负责把共享 tip 的坐标写回到 attendance 状态，供下次打开或切换时继续沿用。
function updateScheduleTemplateTipField(field, value) {
  state.scheduleTemplateTip[field] = value
}

watch(
  () => [activeSection.value, state.scheduleTemplateTip.open],
  async ([nextSection, tipOpen]) => {
    if (nextSection === 'schedule' && tipOpen) {
      await ensureScheduleTemplateTipLayout()
    }
  }
)

// 共享气泡的正文和上下文文案继续由 attendance 业务层拼好，组件本身不感知员工和日期语义。
const scheduleTemplateTipMetaText = computed(() =>
  t('scheduleTemplateTipContext')
    .replace('{employee}', state.scheduleTemplateTip.employeeName || '-')
    .replace('{date}', state.scheduleTemplateTip.workDate || '-')
)

// 左侧导航容器引用用于计算何时切换成悬浮停留状态。
const sidebarPaneRef = ref(null)
// 左侧导航实际盒子引用用于读取当前高度并在悬浮时保留占位。
const sidebarStickRef = ref(null)
// hero 头部引用用于测量当前头部实际高度，再把剩余视口高度分配给 workbench 内部滚动区。
const heroSectionRef = ref(null)
// workbench 视口高度单独记录，避免长内容继续把浏览器外层页面撑高。
const workbenchViewportHeight = ref(null)
// 侧栏悬浮后的定位数据统一收在这里，供模板层直接绑定样式。
const sidebarFloatingState = ref({
  active: false,
  left: 0,
  width: 0,
  height: 0
})
// hero 高度变化监听器单独保留，离开 attendance 页面时需要主动释放。
let heroResizeObserver = null

// 用 hero 当前底边和视口高度计算 workbench 可用空间，让下面这整块改成局部滚动容器。
function syncWorkbenchViewportHeight() {
  if (!heroSectionRef.value) return
  const heroBottom = heroSectionRef.value.getBoundingClientRect().bottom
  // 预留 workbench 顶部间距和底部呼吸空间，避免内部滚动条紧贴浏览器边缘。
  workbenchViewportHeight.value = Math.max(420, Math.floor(window.innerHeight - heroBottom - 16))
}

// 根据当前滚动位置和左栏几何信息决定侧栏是否需要固定停留在视口中。
function syncSidebarFloating() {
  if (!sidebarPaneRef.value || !sidebarStickRef.value) return
  // 窄屏仍走原本自然文档流，避免固定侧栏挤压移动端内容。
  if (window.innerWidth <= 1180) {
    sidebarFloatingState.value = { active: false, left: 0, width: 0, height: 0 }
    return
  }
  const paneRect = sidebarPaneRef.value.getBoundingClientRect()
  const stickRect = sidebarStickRef.value.getBoundingClientRect()
  const shouldFloat = paneRect.top <= 20
  sidebarFloatingState.value = {
    active: shouldFloat,
    // 悬浮时保持与左栏当前横向位置一致，避免分栏拖拽后侧栏跳位。
    left: paneRect.left,
    // 悬浮时保持与左栏当前宽度一致，避免内容重新换行。
    width: paneRect.width,
    // 左栏进入悬浮后用当前真实高度给占位容器补空间，防止布局抖动。
    height: stickRect.height
  }
}

// 供模板直接复用的左栏占位样式，悬浮后仍保留原高度避免右侧主区回流。
const sidebarPaneStyle = computed(() => {
  if (!sidebarFloatingState.value.active) {
    return {}
  }
  return {
    minHeight: `${Math.ceil(sidebarFloatingState.value.height)}px`
  }
})

// 供模板直接复用的左栏悬浮样式，让导航固定停留在当前视口顶部附近。
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

// 页面挂载后立即计算一次左栏状态，并在滚动和窗口变化时持续同步。
onMounted(async () => {
  await nextTick()
  syncWorkbenchViewportHeight()
  syncSidebarFloating()
  // hero 文案在语言切换、窗口收缩时都可能换行，需要实时回算下面 workbench 的可用高度。
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

// 页面销毁时清理滚动与缩放监听，避免离开页面后残留无效回调。
onBeforeUnmount(() => {
  heroResizeObserver?.disconnect()
  window.removeEventListener('scroll', syncSidebarFloating)
  window.removeEventListener('resize', syncWorkbenchViewportHeight)
  window.removeEventListener('resize', syncSidebarFloating)
})
</script>

<template>
  <div class="seladmin-page selattendance-page-shell">
    <header ref="heroSectionRef" class="seladmin-hero seladmin-surface">
      <div>
        <p class="seladmin-eyebrow">{{ t('liveTag') }}</p>
        <h1>{{ t('appTitle') }}</h1>
        <p class="seladmin-copy">{{ t('appSubtitle') }}</p>
      </div>
      <div class="selattendance-hero-actions">
        <ThemeSwitch v-model="themeId" :options="themeOptions" :label="t('themeSwitch')" :t="t" />
        <LanguageSwitch v-model="locale" :options="localeOptions" />
      </div>
    </header>

    <ThreePaneWorkbenchLayout
      class="selattendance-workbench selattendance-workbench-shell"
      :style="workbenchViewportHeight ? { height: `${workbenchViewportHeight}px` } : {}"
      outer-storage-key="attendance-shell-split"
      :outer-default-left-percent="attendanceShellLayoutPreset.outerDefaultLeftPercent"
      :outer-min-left-percent="attendanceShellLayoutPreset.outerMinLeftPercent"
      :outer-max-left-percent="attendanceShellLayoutPreset.outerMaxLeftPercent"
    >
      <template #left>
        <div ref="sidebarPaneRef" class="selattendance-sidebar-pane" :style="sidebarPaneStyle">
          <div ref="sidebarStickRef" class="selattendance-sidebar-stick" :style="sidebarStickStyle">
            <aside class="selattendance-sidebar seladmin-surface">
              <div class="selattendance-sidebar-header">
                <p class="seladmin-eyebrow">{{ t('workspaceSidebarTitle') }}</p>
                <p class="seladmin-copy">{{ t('workspaceSidebarHint') }}</p>
              </div>

              <AttendanceSectionNav
                v-model:active-section="activeSection"
                :nav-items="workspaceNavItems"
                :active-section="activeSection"
                :title="t('workspaceSidebarTitle')"
              />

              <div class="selattendance-sidebar-foot">
                <span class="selattendance-sidebar-foot-label">{{ t('nextAction') }}</span>
                <strong>{{ recommendedNextLabel }}</strong>
              </div>
            </aside>
          </div>
        </div>
      </template>

      <template #main>
        <main class="selattendance-content">
          <SharedWorkbenchHeader
            v-if="activeSection === 'schedule'"
            class="selattendance-content-header seladmin-surface"
            :title="t('scheduleTitle')"
            :lead="t('scheduleLead')"
          >
            <template #filters>
              <!-- 排班头部只承接筛选条件，真正刷新看板的查询动作由放大镜按钮显式触发。 -->
              <div class="selattendance-workbench-header-toolbar selattendance-workbench-header-toolbar--schedule">
                <label class="seladmin-field">
                  <span>{{ t('scheduleMonth') }}</span>
                  <input v-model="state.scheduleFilters.month" type="month" />
                </label>
                <label class="seladmin-field">
                  <span>{{ t('workplace') }}</span>
                  <select v-model="state.scheduleFilters.workplaceId">
                    <option value="">{{ t('scheduleWorkplaceFilterHint') }}</option>
                    <option v-for="item in state.workplaces" :key="item.id" :value="item.id">{{ item.workplaceName }}</option>
                  </select>
                </label>
                <label class="seladmin-field">
                  <span>{{ t('departmentName') }}</span>
                  <select v-model="state.scheduleFilters.departmentId">
                    <option value="">{{ t('scheduleDepartmentFilterHint') }}</option>
                    <option v-for="item in state.departments" :key="item.id" :value="item.id">{{ item.departmentName }}</option>
                  </select>
                </label>
                <label class="seladmin-field">
                  <span>{{ t('scheduleKeyword') }}</span>
                  <div class="selattendance-header-search-field">
                    <input v-model="state.scheduleFilters.employeeKeyword" :placeholder="t('scheduleKeywordHint')" />
                    <!-- 排班筛选也改成统一的手动搜索入口，避免用户编辑关键字时频繁重刷整张排班看板。 -->
                    <button
                      class="seladmin-button seladmin-button-secondary selattendance-header-search-button"
                      type="button"
                      :aria-label="t('searchAction')"
                      :title="t('searchAction')"
                      @click="runScheduleSearch()"
                    >
                      <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="11" cy="11" r="5.5" />
                        <path d="M16 16l4 4" />
                      </svg>
                    </button>
                  </div>
                </label>
                <label class="selattendance-header-inline-checkbox">
                  <input v-model="state.scheduleFilters.onlyUnassigned" type="checkbox" />
                  <span>{{ t('scheduleOnlyUnassigned') }}</span>
                </label>
              </div>
            </template>
          </SharedWorkbenchHeader>

          <SharedWorkbenchHeader
            v-if="activeSection === 'monthly'"
            class="selattendance-content-header seladmin-surface selattendance-monthly-workbench-header"
            :title="t('monthlyTitle')"
            :lead="t('monthlyLead')"
            split-mode="left-summary-right-metrics"
          >
            <template #metrics>
              <!-- 月次先作为 shared 指标卡的样板模块，后续 punch/daily/case 可以直接复用同样入口。 -->
              <SharedMetricCards
                class="selattendance-monthly-header-summary"
                :items="monthlyHeaderMetricItems"
              />
            </template>

            <template #filters>
              <!-- 月次搜索条件继续放在头部，但改成显式点击放大镜后才走后台月次查询。 -->
              <div class="selattendance-workbench-header-toolbar selattendance-workbench-header-toolbar--monthly">
                <label class="seladmin-field">
                  <span>{{ t('monthlyYearMonth') }}</span>
                  <input v-model="state.monthlyFilters.yearMonth" type="month" />
                </label>
                <label class="seladmin-field">
                  <span>{{ t('workplace') }}</span>
                  <select v-model="state.monthlyFilters.workplaceId">
                    <option value="">{{ t('allWorkplaces') }}</option>
                    <option v-for="item in state.workplaces" :key="item.id" :value="item.id">{{ item.workplaceName }}</option>
                  </select>
                </label>
                <label class="seladmin-field">
                  <span>{{ t('departmentName') }}</span>
                  <select v-model="state.monthlyFilters.departmentId">
                    <option value="">{{ t('allDepartments') }}</option>
                    <option v-for="item in state.departments" :key="item.id" :value="item.id">{{ item.departmentName }}</option>
                  </select>
                </label>
                <label class="seladmin-field">
                  <span>{{ t('employeeName') }}</span>
                  <div class="selattendance-header-search-field">
                    <input v-model="state.monthlyFilters.employeeKeyword" :placeholder="t('monthlyEmployeeKeywordHint')" />
                    <!-- 搜索按钮与员工关键字输入框同组展示，提醒用户需要点击后才真正向后台检索。 -->
                    <button
                      class="seladmin-button seladmin-button-secondary selattendance-header-search-button"
                      type="button"
                      :aria-label="t('searchAction')"
                      :title="t('searchAction')"
                      @click="runMonthlySearch()"
                    >
                      <!-- 改用 SVG 放大镜图标，保证在同样按钮尺寸下比字符图标更清晰醒目。 -->
                      <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="11" cy="11" r="5.5" />
                        <path d="M16 16l4 4" />
                      </svg>
                    </button>
                  </div>
                </label>
                <label class="seladmin-field">
                  <span>{{ t('monthlyCloseStatus') }}</span>
                  <select v-model="state.monthlyFilters.closeStatus">
                    <option value="">{{ t('monthlyCloseStatusAll') }}</option>
                    <option value="OPEN">{{ t('monthlyCloseStatusOpen') }}</option>
                    <option value="CLOSABLE">{{ t('monthlyCloseStatusClosable') }}</option>
                    <option value="CLOSED">{{ t('monthlyCloseStatusClosed') }}</option>
                    <option value="REOPENED">{{ t('monthlyCloseStatusReopened') }}</option>
                  </select>
                </label>
                <label class="selattendance-daily-checkbox selattendance-header-inline-checkbox">
                  <input v-model="state.monthlyFilters.blockedOnly" type="checkbox" />
                  <span>{{ t('monthlyBlockedOnly') }}</span>
                </label>
              </div>
            </template>
          </SharedWorkbenchHeader>

          <SharedWorkbenchHeader
            v-else-if="activeSection === 'punch'"
            class="selattendance-content-header seladmin-surface selattendance-generic-workbench-header"
            :title="t('punchTitle')"
            :lead="t('punchLead')"
            split-mode="left-summary-right-metrics"
          >
            <template #metrics>
              <SharedMetricCards :items="punchHeaderMetricItems" />
            </template>

            <template #filters>
              <div class="selattendance-workbench-header-toolbar selattendance-workbench-header-toolbar--punch">
                <label class="seladmin-field">
                  <span>{{ t('punchDateFrom') }}</span>
                  <input v-model="state.punchFilters.dateFrom" type="date" />
                </label>
                <label class="seladmin-field">
                  <span>{{ t('punchDateTo') }}</span>
                  <input v-model="state.punchFilters.dateTo" type="date" />
                </label>
                <label class="seladmin-field">
                  <span>{{ t('employeeName') }}</span>
                  <div class="selattendance-header-search-field">
                    <input v-model="state.punchFilters.employeeKeyword" :placeholder="t('punchEmployeeKeywordHint')" />
                    <!-- 打卡记录改成显式搜索后，只有点击放大镜才真正查询后台打卡列表。 -->
                    <button
                      class="seladmin-button seladmin-button-secondary selattendance-header-search-button"
                      type="button"
                      :aria-label="t('searchAction')"
                      :title="t('searchAction')"
                      @click="runPunchSearch()"
                    >
                      <!-- 继续复用头部统一 SVG 放大镜，避免 monthly/case/punch 出现三种搜索按钮。 -->
                      <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="11" cy="11" r="5.5" />
                        <path d="M16 16l4 4" />
                      </svg>
                    </button>
                  </div>
                </label>
                <label class="seladmin-field">
                  <span>{{ t('sourceSystem') }}</span>
                  <select v-model="state.punchFilters.sourceSystem">
                    <option value="">{{ t('punchAllSources') }}</option>
                    <option value="MANUAL">MANUAL</option>
                    <option value="CSV_IMPORT">CSV_IMPORT</option>
                    <option value="WEBHOOK">WEBHOOK</option>
                  </select>
                </label>
                <label class="seladmin-field">
                  <span>{{ t('status') }}</span>
                  <select v-model="state.punchFilters.processStatus">
                    <option value="">{{ t('punchAllStatuses') }}</option>
                    <option value="PROCESSED">{{ t('punchStatusProcessed') }}</option>
                    <option value="UNMATCHED">{{ t('punchStatusUnmatched') }}</option>
                    <option value="ERROR">{{ t('punchStatusError') }}</option>
                    <option value="DUPLICATE">{{ t('punchStatusDuplicate') }}</option>
                    <option value="IGNORED">{{ t('punchStatusIgnored') }}</option>
                  </select>
                </label>
              </div>
            </template>

          </SharedWorkbenchHeader>

          <SharedWorkbenchHeader
            v-else-if="activeSection === 'daily'"
            class="selattendance-content-header seladmin-surface selattendance-generic-workbench-header"
            :title="t('dailyTitle')"
            :lead="t('dailyLead')"
            split-mode="left-summary-right-metrics"
          >
            <template #metrics>
              <SharedMetricCards :items="dailyHeaderMetricItems" />
            </template>

            <template #filters>
              <div class="selattendance-workbench-header-toolbar selattendance-workbench-header-toolbar--daily">
                <label class="seladmin-field">
                  <span>{{ t('dailyDateFrom') }}</span>
                  <input v-model="state.dailyFilters.startDate" type="date" />
                </label>
                <label class="seladmin-field">
                  <span>{{ t('dailyDateTo') }}</span>
                  <input v-model="state.dailyFilters.endDate" type="date" />
                </label>
                <label class="seladmin-field">
                  <span>{{ t('workplace') }}</span>
                  <select v-model="state.dailyFilters.workplaceId">
                    <option value="">{{ t('allWorkplaces') }}</option>
                    <option v-for="item in state.workplaces" :key="item.id" :value="item.id">{{ item.workplaceName }}</option>
                  </select>
                </label>
                <label class="seladmin-field">
                  <span>{{ t('departmentName') }}</span>
                  <select v-model="state.dailyFilters.departmentId">
                    <option value="">{{ t('allDepartments') }}</option>
                    <option v-for="item in state.departments" :key="item.id" :value="item.id">{{ item.departmentName }}</option>
                  </select>
                </label>
                <label class="seladmin-field">
                  <span>{{ t('employeeName') }}</span>
                  <div class="selattendance-header-search-field">
                    <input v-model="state.dailyFilters.employeeKeyword" :placeholder="t('dailyEmployeeKeywordHint')" />
                    <!-- 日次结果改成和月次一致的显式搜索模式，输入筛选后点击按钮才查询后台。 -->
                    <button
                      class="seladmin-button seladmin-button-secondary selattendance-header-search-button"
                      type="button"
                      :aria-label="t('searchAction')"
                      :title="t('searchAction')"
                      @click="runDailySearch()"
                    >
                      <!-- 共用头部搜索按钮图标样式，保证四个模块的按钮尺寸和视觉统一。 -->
                      <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="11" cy="11" r="5.5" />
                        <path d="M16 16l4 4" />
                      </svg>
                    </button>
                  </div>
                </label>
                <label class="seladmin-field">
                  <span>{{ t('status') }}</span>
                  <select v-model="state.dailyFilters.status">
                    <option value="">{{ t('dailyStatusAll') }}</option>
                    <option value="NORMAL">{{ t('dailyStatusNormal') }}</option>
                    <option value="LATE">{{ t('dailyStatusLate') }}</option>
                    <option value="EARLY_LEAVE">{{ t('dailyStatusEarlyLeave') }}</option>
                    <option value="MISSING_CLOCK_IN">{{ t('dailyStatusMissingClockIn') }}</option>
                    <option value="MISSING_CLOCK_OUT">{{ t('dailyStatusMissingClockOut') }}</option>
                    <option value="ABSENCE">{{ t('dailyStatusAbsence') }}</option>
                    <option value="NO_SCHEDULE">{{ t('dailyStatusNoSchedule') }}</option>
                    <option value="HOLIDAY_WORK">{{ t('dailyStatusHolidayWork') }}</option>
                  </select>
                </label>
                <label class="selattendance-daily-checkbox selattendance-header-inline-checkbox">
                  <input v-model="state.dailyFilters.exceptionOnly" type="checkbox" />
                  <span>{{ t('dailyExceptionOnly') }}</span>
                </label>
              </div>
            </template>

          </SharedWorkbenchHeader>

          <SharedWorkbenchHeader
            v-else-if="activeSection === 'case'"
            class="selattendance-content-header seladmin-surface selattendance-generic-workbench-header selattendance-case-workbench-header"
            :title="t('caseTitle')"
            :lead="t('caseLead')"
            split-mode="left-summary-right-metrics"
          >
            <template #metrics>
              <SharedMetricCards class="selattendance-case-header-summary" :items="caseHeaderMetricItems" />
            </template>

            <template #filters>
              <div class="selattendance-workbench-header-toolbar selattendance-workbench-header-toolbar--case">
                <label class="seladmin-field">
                  <span>{{ t('dailyDateFrom') }}</span>
                  <input v-model="state.caseFilters.startDate" type="date" />
                </label>
                <label class="seladmin-field">
                  <span>{{ t('dailyDateTo') }}</span>
                  <input v-model="state.caseFilters.endDate" type="date" />
                </label>
                <label class="seladmin-field">
                  <span>{{ t('workplace') }}</span>
                  <select v-model="state.caseFilters.workplaceId">
                    <option value="">{{ t('allWorkplaces') }}</option>
                    <option v-for="item in state.workplaces" :key="item.id" :value="item.id">{{ item.workplaceName }}</option>
                  </select>
                </label>
                <label class="seladmin-field">
                  <span>{{ t('departmentName') }}</span>
                  <select v-model="state.caseFilters.departmentId">
                    <option value="">{{ t('allDepartments') }}</option>
                    <option v-for="item in state.departments" :key="item.id" :value="item.id">{{ item.departmentName }}</option>
                  </select>
                </label>
                <label class="seladmin-field">
                  <span>{{ t('employeeName') }}</span>
                  <div class="selattendance-header-search-field">
                    <input v-model="state.caseFilters.employeeKeyword" :placeholder="t('dailyEmployeeKeywordHint')" />
                    <!-- 异常处理改成和月次一致的显式搜索按钮，输入关键字后点击才真正触发后台查询。 -->
                    <button
                      class="seladmin-button seladmin-button-secondary selattendance-header-search-button"
                      type="button"
                      :aria-label="t('searchAction')"
                      :title="t('searchAction')"
                      @click="runCaseSearch()"
                    >
                      <!-- 和月次共用同一套 SVG 放大镜样式，避免不同模块出现两种搜索按钮尺寸。 -->
                      <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="11" cy="11" r="5.5" />
                        <path d="M16 16l4 4" />
                      </svg>
                    </button>
                  </div>
                </label>
                <label class="seladmin-field">
                  <span>{{ t('caseStatusLabel') }}</span>
                  <select v-model="state.caseFilters.caseStatus">
                    <option value="">{{ t('caseStatusAll') }}</option>
                    <option value="UNHANDLED">{{ t('caseStatusUnhandled') }}</option>
                    <option value="SUBMITTED">{{ t('caseStatusSubmitted') }}</option>
                    <option value="RETURNED">{{ t('caseStatusReturned') }}</option>
                    <option value="APPROVED">{{ t('caseStatusApproved') }}</option>
                    <option value="REJECTED">{{ t('caseStatusRejected') }}</option>
                    <option value="LOCKED">{{ t('caseStatusLocked') }}</option>
                  </select>
                </label>
                <label class="selattendance-daily-checkbox selattendance-header-inline-checkbox">
                  <input v-model="state.caseFilters.mineOnly" type="checkbox" />
                  <span>{{ t('caseMineOnly') }}</span>
                </label>
              </div>
            </template>
          </SharedWorkbenchHeader>

          <section v-else class="selattendance-content-header seladmin-surface">
            <div>
              <p class="seladmin-eyebrow">{{ t('workspaceStatus') }}</p>
              <h2>{{ activeSectionMeta.label }}</h2>
              <p class="seladmin-copy">{{ activeSectionMeta.caption }}</p>
            </div>
            <span class="seladmin-chip">{{ activeSectionMeta.badge }}</span>
          </section>

          <div
            class="selattendance-content-stack"
            :class="{ 'selattendance-content-stack-schedule': activeSection === 'schedule' }"
          >
            <template v-if="activeSection === 'wizard'">
              <ThreePaneWorkbenchLayout
                class="selattendance-overview-split"
                outer-storage-key="attendance-overview-split"
                :outer-default-left-percent="attendanceOverviewLayoutPreset.outerDefaultLeftPercent"
                :outer-min-left-percent="attendanceOverviewLayoutPreset.outerMinLeftPercent"
                :outer-max-left-percent="attendanceOverviewLayoutPreset.outerMaxLeftPercent"
              >
                <template #left>
                  <AttendanceSummaryPanel :steps="state.steps" :recommended-next-label="recommendedNextLabel" :t="t" />
                </template>

                <template #main>
                  <TenantPanel :tenant="state.tenant" :t="t" :on-submit="submitTenant" />
                </template>
              </ThreePaneWorkbenchLayout>
              <WizardSection :visible="true" :steps="state.steps" :t="t" />
            </template>

            <WorkplaceSection
              :visible="activeSection === 'workplace'"
              :workplaces="state.workplaces"
              :workplace-form="state.workplaceForm"
              :t="t"
              :on-submit="submitWorkplace"
              :on-reset="resetWorkplaceForm"
              :on-edit="editWorkplace"
              :on-open-departments="openWorkplaceDepartments"
              :on-delete="requestWorkplaceDelete"
            />

            <DepartmentSection
              :visible="activeSection === 'department'"
              :workplaces="state.workplaces"
              :departments="state.departments"
              :filtered-departments="filteredDepartments"
              :department-form="state.departmentForm"
              :current-workplace-name="currentDepartmentWorkplaceName"
              :t="t"
              :on-submit="submitDepartment"
              :on-reset="resetDepartmentForm"
              :on-edit="editDepartment"
              :on-clear-workplace-filter="clearDepartmentWorkplaceFilter"
              :on-open-employees="openDepartmentEmployees"
              :on-open-schedule="openDepartmentSchedule"
              :on-delete="requestDepartmentDelete"
            />

            <EmployeeSection
              :visible="activeSection === 'employee'"
              :workplaces="state.workplaces"
              :departments="state.departments"
              :employees="state.employees"
              :filtered-employees="filteredEmployees"
              :employee-form="state.employeeForm"
              :mapping-form="state.mappingForm"
              :employee-filters="state.employeeFilters"
              :import-csv-text="state.importCsvText"
              :import-result="state.importResult"
              :t="t"
              :on-submit="submitEmployee"
              :on-reset="resetEmployeeForm"
              :on-import="submitImport"
              :on-update-import-csv-text="updateImportCsvText"
              :on-export="handleExport"
              :on-bind="submitMapping"
              :on-edit-employee="editEmployee"
              :on-edit-mapping="editMapping"
              :on-delete-employee="requestEmployeeDelete"
            />

            <ShiftTemplateSection
              :visible="activeSection === 'shift'"
              :shift-templates="state.shiftTemplates"
              :shift-form="state.shiftForm"
              :t="t"
              :on-submit="submitShiftTemplate"
              :on-generate="generateRecommended"
              :on-edit="editShiftTemplate"
              :on-delete="requestShiftTemplateDelete"
            />

            <ScheduleSection
              :visible="activeSection === 'schedule'"
              :schedule-board="state.scheduleBoard"
              :schedule-template-tip="state.scheduleTemplateTip"
              :schedule-form="state.scheduleForm"
              :batch-wizard="state.batchWizard"
              :unassigned-items="state.scheduleUnassignedItems"
              :t="t"
              :on-refresh="loadScheduleBoard"
              :on-select-template="selectScheduleTemplate"
              :on-apply-schedule="applySchedule"
              :on-delete-schedule="requestScheduleDelete"
              :on-copy-last-week="copySchedulesFromLastWeek"
              :on-copy-last-month="copySchedulesFromLastMonth"
              :on-export="handleScheduleExport"
              :on-check-unassigned="runUnassignedCheck"
              :on-open-batch-wizard="openBatchWizard"
              :on-close-batch-wizard="closeBatchWizard"
              :on-next-batch-step="nextBatchStep"
              :on-prev-batch-step="prevBatchStep"
              :on-confirm-batch-wizard="confirmBatchWizard"
            />

            <PunchSection
              :visible="activeSection === 'punch'"
              :employees="state.employees"
              :punch-log-list="state.punchLogList"
              :punch-filters="state.punchFilters"
              :punch-detail="state.punchDetail"
              :punch-manual-form="state.punchManualForm"
              :punch-import-form="state.punchImportForm"
              :punch-import-preview="state.punchImportPreview"
              :punch-action-form="state.punchActionForm"
              :t="t"
              :on-refresh="loadPunchLogs"
              :on-select-log="openPunchDetail"
              :on-submit-manual="submitManualPunch"
              :on-preview-import="runPunchImportPreview"
              :on-submit-import="submitPunchImport"
              :on-bind-employee="submitPunchBind"
              :on-ignore-log="submitPunchIgnore"
              :on-reprocess-log="submitPunchReprocess"
              :on-show-toast="showToast"
            />

            <DailySection
              :visible="activeSection === 'daily'"
              :workplaces="state.workplaces"
              :departments="state.departments"
              :daily-list="state.dailyList"
              :daily-filters="state.dailyFilters"
              :daily-detail="state.dailyDetail"
              :t="t"
              :on-refresh="loadDailyResults"
              :on-select-daily="openDailyDetail"
              :on-recalculate-range="submitDailyRangeRecalculate"
              :on-recalculate-daily="submitDailyRecalculate"
              :on-show-toast="showToast"
            />

            <CaseSection
              :visible="activeSection === 'case'"
              :workplaces="state.workplaces"
              :departments="state.departments"
              :case-list="state.caseList"
              :case-filters="state.caseFilters"
              :case-detail="state.caseDetail"
              :case-focus-item="state.caseFocusItem"
              :case-create-form="state.caseCreateForm"
              :case-action-form="state.caseActionForm"
              :t="t"
              :on-refresh="loadCases"
              :on-select-case="openCaseDetail"
              :on-submit-create="submitCaseCreate"
              :on-submit-action="submitCaseAction"
              :on-lock="submitCaseLock"
              :on-unlock="submitCaseUnlock"
              :on-show-toast="showToast"
            />

            <MonthlySection
              :visible="activeSection === 'monthly'"
              :workplaces="state.workplaces"
              :departments="state.departments"
              :monthly-list="state.monthlyList"
              :monthly-filters="state.monthlyFilters"
              :monthly-detail="state.monthlyDetail"
              :monthly-action-form="state.monthlyActionForm"
              :t="t"
              :on-refresh="loadMonthlyResults"
              :on-select-monthly="openMonthlyDetail"
              :on-recalculate-monthly="submitMonthlyRecalculate"
              :on-recalculate-one="submitMonthlyRecalculateOne"
              :on-close-monthly="submitMonthlyClose"
              :on-reopen-monthly="submitMonthlyReopen"
              :on-export-monthly="submitMonthlyExport"
              :on-show-toast="showToast"
            />
          </div>
        </main>
      </template>
    </ThreePaneWorkbenchLayout>

    <div v-if="toast" class="seladmin-toast seladmin-surface" role="status" aria-live="polite">
      {{ toast }}
    </div>
    <FloatingTipBubble
      :open="activeSection === 'schedule' && state.scheduleTemplateTip.open"
      :title="t('scheduleTemplateTipTitle')"
      :body="t('scheduleTemplateTipBody')"
      :meta-text="scheduleTemplateTipMetaText"
      :bubble-x="state.scheduleTemplateTip.bubbleX || 24"
      :bubble-y="state.scheduleTemplateTip.bubbleY || 24"
      :anchor-x="state.scheduleTemplateTip.anchorX || 48"
      :anchor-y="state.scheduleTemplateTip.anchorY || 48"
      :close-aria-label="t('scheduleTemplateTipClose')"
      :tail-drag-aria-label="t('scheduleTemplateTipAnchorDrag')"
      @close="closeScheduleTemplateTip()"
      @update:bubble-x="updateScheduleTemplateTipField('bubbleX', $event)"
      @update:bubble-y="updateScheduleTemplateTipField('bubbleY', $event)"
      @update:anchor-x="updateScheduleTemplateTipField('anchorX', $event)"
      @update:anchor-y="updateScheduleTemplateTipField('anchorY', $event)"
    />
    <ConfirmDialog
      :open="confirmDialog.open"
      :title="confirmDialog.title"
      :message="confirmDialog.message"
      :confirm-label="confirmDialog.confirmLabel"
      :cancel-label="t('cancel')"
      :confirm-variant="confirmDialog.confirmVariant"
      @cancel="cancelConfirmDialog"
      @confirm="submitConfirmDialog"
    />
    <div v-if="loading" class="seladmin-loading">{{ t('loading') }}</div>
  </div>
</template>
