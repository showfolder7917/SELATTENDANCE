<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import ConfirmDialog from '../../../shared/components/ConfirmDialog.vue'
import FloatingTipBubble from '../../../shared/components/FloatingTipBubble.vue'
import LanguageSwitch from '../../../shared/components/LanguageSwitch.vue'
import ThemeSwitch from '../../../shared/components/ThemeSwitch.vue'
import AttendanceSectionNav from '../components/AttendanceSectionNav.vue'
import AttendanceSummaryPanel from '../components/AttendanceSummaryPanel.vue'
import DailySection from '../components/DailySection.vue'
import DepartmentSection from '../components/DepartmentSection.vue'
import EmployeeSection from '../components/EmployeeSection.vue'
import PunchSection from '../components/PunchSection.vue'
import ResizableWorkbenchSplit from '../components/ResizableWorkbenchSplit.vue'
import ScheduleSection from '../components/ScheduleSection.vue'
import ShiftTemplateSection from '../components/ShiftTemplateSection.vue'
import TenantPanel from '../components/TenantPanel.vue'
import WizardSection from '../components/WizardSection.vue'
import WorkplaceSection from '../components/WorkplaceSection.vue'
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
  openPunchDetail,
  openDailyDetail,
  submitManualPunch,
  runPunchImportPreview,
  submitPunchImport,
  submitPunchBind,
  submitPunchIgnore,
  submitPunchReprocess,
  submitDailyRecalculate,
  submitDailyRangeRecalculate,
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
    daily: state.dailyList.total
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
    daily: t('sectionDailyHint')
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
// 侧栏悬浮后的定位数据统一收在这里，供模板层直接绑定样式。
const sidebarFloatingState = ref({
  active: false,
  left: 0,
  width: 0,
  height: 0
})

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
  syncSidebarFloating()
  window.addEventListener('scroll', syncSidebarFloating, { passive: true })
  window.addEventListener('resize', syncSidebarFloating)
})

// 页面销毁时清理滚动与缩放监听，避免离开页面后残留无效回调。
onBeforeUnmount(() => {
  window.removeEventListener('scroll', syncSidebarFloating)
  window.removeEventListener('resize', syncSidebarFloating)
})
</script>

<template>
  <div class="seladmin-page selattendance-page-shell">
    <header class="seladmin-hero seladmin-surface">
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

    <ResizableWorkbenchSplit
      class="selattendance-workbench selattendance-workbench-shell"
      storage-key="attendance-shell-split"
      :default-left-percent="13"
      :min-left-percent="9"
      :max-left-percent="22"
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

      <template #right>
        <main class="selattendance-content">
          <section class="selattendance-content-header seladmin-surface">
            <div>
              <p class="seladmin-eyebrow">{{ t('workspaceStatus') }}</p>
              <h2>{{ activeSectionMeta.label }}</h2>
              <p class="seladmin-copy">{{ activeSectionMeta.caption }}</p>
            </div>
            <span class="seladmin-chip">{{ activeSectionMeta.badge }}</span>
          </section>

          <div class="selattendance-content-stack">
            <template v-if="activeSection === 'wizard'">
              <ResizableWorkbenchSplit
                class="selattendance-overview-split"
                storage-key="attendance-overview-split"
                :default-left-percent="64"
                :min-left-percent="42"
                :max-left-percent="76"
              >
                <template #left>
                  <AttendanceSummaryPanel :steps="state.steps" :recommended-next-label="recommendedNextLabel" :t="t" />
                </template>

                <template #right>
                  <TenantPanel :tenant="state.tenant" :t="t" :on-submit="submitTenant" />
                </template>
              </ResizableWorkbenchSplit>
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
              :workplaces="state.workplaces"
              :departments="state.departments"
              :schedule-board="state.scheduleBoard"
              :schedule-filters="state.scheduleFilters"
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
              :on-recalculate-daily="submitDailyRecalculate"
              :on-recalculate-range="submitDailyRangeRecalculate"
            />
          </div>
        </main>
      </template>
    </ResizableWorkbenchSplit>

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
