<script setup>
import { computed, ref } from 'vue'
import LanguageSwitch from '../../../shared/components/LanguageSwitch.vue'
import ThemeSwitch from '../../../shared/components/ThemeSwitch.vue'
import AttendanceSectionNav from '../components/AttendanceSectionNav.vue'
import AttendanceSummaryPanel from '../components/AttendanceSummaryPanel.vue'
import DepartmentSection from '../components/DepartmentSection.vue'
import EmployeeSection from '../components/EmployeeSection.vue'
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
  submitShiftTemplate,
  loadScheduleBoard,
  generateRecommended,
  removeShiftTemplate,
  selectScheduleTemplate,
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
    schedule: state.scheduleBoard.scheduleItems.length
  }

  const moduleHintMap = {
    wizard: t('sectionWizardHint'),
    workplace: t('sectionWorkplaceHint'),
    department: t('sectionDepartmentHint'),
    employee: t('sectionEmployeeHint'),
    shift: t('sectionShiftHint')
    ,
    schedule: t('sectionScheduleHint')
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

// 删除确认框在页面层统一维护开关、文案和待执行动作，避免每个模块各自造一套弹窗状态。
const deleteDialog = ref({
  open: false,
  title: '',
  message: '',
  confirmLabel: '',
  onConfirm: null
})

// 统一关闭删除确认框，并清空挂在弹框里的旧动作，避免误触发上一条删除请求。
function closeDeleteDialog() {
  deleteDialog.value = {
    open: false,
    title: '',
    message: '',
    confirmLabel: '',
    onConfirm: null
  }
}

// 删除文案在页面层统一拼接，保证三套主题下看到的是同一套确认语义。
function openDeleteDialog(targetLabel, targetName, onConfirm) {
  const namedMessage = t('confirmDeleteMessageNamed')
    .replace('{target}', targetLabel)
    .replace('{name}', targetName)
  const unnamedMessage = t('confirmDeleteMessageUnnamed').replace('{target}', targetLabel)
  deleteDialog.value = {
    open: true,
    title: t('confirmDeleteTitle'),
    message: targetName ? namedMessage : unnamedMessage,
    confirmLabel: t('confirmDeleteAction'),
    onConfirm
  }
}

// 点击确认后先收起弹框，再执行真正删除动作，避免删除请求期间弹框还停留在页面上。
async function confirmDeleteDialog() {
  const pendingAction = deleteDialog.value.onConfirm
  closeDeleteDialog()
  if (pendingAction) {
    await pendingAction()
  }
}

// 事业所删除文案优先展示名称，其次回退编码，避免用户只看到无意义主键。
function requestWorkplaceDelete(id) {
  const item = state.workplaces.find((entry) => entry.id === id)
  openDeleteDialog(
    t('workplaceTitle'),
    item?.workplaceName || item?.workplaceCode || '',
    () => removeWorkplace(id)
  )
}

// 部门删除沿用同一弹框能力，只替换目标模块标签和展示名称。
function requestDepartmentDelete(id) {
  const item = state.departments.find((entry) => entry.id === id)
  openDeleteDialog(
    t('departmentTitle'),
    item?.departmentName || item?.departmentCode || '',
    () => removeDepartment(id)
  )
}

// 员工删除确认优先展示员工姓名，避免在同一编号体系下误删到其他人。
function requestEmployeeDelete(id) {
  const item = state.employees.find((entry) => entry.id === id)
  openDeleteDialog(
    t('employeeTitle'),
    item?.employeeName || item?.employeeNo || '',
    () => removeEmployee(id)
  )
}

// 班次模板删除同样在页面层先确认，再把真正删除动作透传给 composable。
function requestShiftTemplateDelete(id) {
  const item = state.shiftTemplates.find((entry) => entry.id === id)
  openDeleteDialog(
    t('shiftTitle'),
    item?.templateName || item?.templateCode || '',
    () => removeShiftTemplate(id)
  )
}

// 排班删除需要把员工和日期拼成当前格标签，帮助用户确认自己要清掉的是哪一天哪位员工。
function requestScheduleDelete(id) {
  const item = state.scheduleBoard.scheduleItems.find((entry) => entry.id === id)
  const targetName = [
    item?.employeeName || state.scheduleForm.selectedEmployeeName || '',
    item?.workDate || state.scheduleForm.selectedWorkDate || ''
  ].filter(Boolean).join(' / ')
  openDeleteDialog(t('scheduleTitle'), targetName, () => removeScheduleItem(id))
}
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
        <div class="selattendance-sidebar-pane">
          <div class="selattendance-sidebar-stick">
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
          </div>
        </main>
      </template>
    </ResizableWorkbenchSplit>

    <div v-if="toast" class="seladmin-toast seladmin-surface">{{ toast }}</div>
    <div v-if="deleteDialog.open" class="selattendance-confirm-overlay" @click.self="closeDeleteDialog">
      <section
        class="selattendance-confirm-dialog seladmin-surface"
        role="alertdialog"
        aria-modal="true"
        :aria-label="deleteDialog.title"
      >
        <p class="seladmin-eyebrow">{{ deleteDialog.title }}</p>
        <h3>{{ deleteDialog.title }}</h3>
        <p class="seladmin-copy">{{ deleteDialog.message }}</p>
        <div class="selattendance-confirm-actions">
          <button type="button" class="seladmin-button seladmin-button-secondary" @click="closeDeleteDialog">
            {{ t('cancel') }}
          </button>
          <button type="button" class="seladmin-button selattendance-confirm-danger" @click="confirmDeleteDialog">
            {{ deleteDialog.confirmLabel }}
          </button>
        </div>
      </section>
    </div>
    <div v-if="loading" class="seladmin-loading">{{ t('loading') }}</div>
  </div>
</template>
