<script setup>
import { computed } from 'vue'
import LanguageSwitch from '../../components/LanguageSwitch.vue'
import ThemeSwitch from '../../components/ThemeSwitch.vue'
import AttendanceSectionNav from '../../components/attendance/AttendanceSectionNav.vue'
import AttendanceSummaryPanel from '../../components/attendance/AttendanceSummaryPanel.vue'
import DepartmentSection from '../../components/attendance/DepartmentSection.vue'
import EmployeeSection from '../../components/attendance/EmployeeSection.vue'
import ShiftTemplateSection from '../../components/attendance/ShiftTemplateSection.vue'
import TenantPanel from '../../components/attendance/TenantPanel.vue'
import WizardSection from '../../components/attendance/WizardSection.vue'
import WorkplaceSection from '../../components/attendance/WorkplaceSection.vue'
import { useAttendanceWorkbench } from '../../composables/attendance/useAttendanceWorkbench'
import { useAttendanceTheme } from '../../composables/theme/useAttendanceTheme'

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
  generateRecommended,
  removeShiftTemplate,
  editWorkplace,
  editDepartment,
  editEmployee,
  editMapping,
  editShiftTemplate,
  resetWorkplaceForm,
  resetDepartmentForm,
  resetEmployeeForm
} = useAttendanceWorkbench()

// 主题切换独立于业务工作台状态，避免保存表单时误触发主题重置。
const { themeId, themeOptions } = useAttendanceTheme()

// 导入文本通过可写 computed 包成 ref 形态，便于子组件直接用 v-model 操作。
const importCsvTextRef = computed({
  get() {
    return state.importCsvText
  },
  set(nextValue) {
    state.importCsvText = nextValue
  }
})

// 左侧导航除了标签，还补上当前模块说明和数量，减少用户在一页里四处找入口。
const workspaceNavItems = computed(() => {
  const moduleCountMap = {
    wizard: state.steps.length,
    workplace: state.workplaces.length,
    department: state.departments.length,
    employee: state.employees.length,
    shift: state.shiftTemplates.length
  }

  const moduleHintMap = {
    wizard: t('sectionWizardHint'),
    workplace: t('sectionWorkplaceHint'),
    department: t('sectionDepartmentHint'),
    employee: t('sectionEmployeeHint'),
    shift: t('sectionShiftHint')
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

    <div class="selattendance-workbench">
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
            <div class="selattendance-overview-grid">
              <AttendanceSummaryPanel :steps="state.steps" :recommended-next-label="recommendedNextLabel" :t="t" />
              <TenantPanel :tenant="state.tenant" :t="t" :on-submit="submitTenant" />
            </div>
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
            :on-delete="removeWorkplace"
          />

          <DepartmentSection
            :visible="activeSection === 'department'"
            :workplaces="state.workplaces"
            :departments="state.departments"
            :department-form="state.departmentForm"
            :t="t"
            :on-submit="submitDepartment"
            :on-reset="resetDepartmentForm"
            :on-edit="editDepartment"
            :on-delete="removeDepartment"
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
            :import-csv-text-ref="importCsvTextRef"
            :import-result="state.importResult"
            :t="t"
            :on-submit="submitEmployee"
            :on-reset="resetEmployeeForm"
            :on-import="submitImport"
            :on-export="handleExport"
            :on-bind="submitMapping"
            :on-edit-employee="editEmployee"
            :on-edit-mapping="editMapping"
            :on-delete-employee="removeEmployee"
          />

          <ShiftTemplateSection
            :visible="activeSection === 'shift'"
            :shift-templates="state.shiftTemplates"
            :shift-form="state.shiftForm"
            :t="t"
            :on-submit="submitShiftTemplate"
            :on-generate="generateRecommended"
            :on-edit="editShiftTemplate"
            :on-delete="removeShiftTemplate"
          />
        </div>
      </main>
    </div>

    <div v-if="toast" class="seladmin-toast seladmin-surface">{{ toast }}</div>
    <div v-if="loading" class="seladmin-loading">{{ t('loading') }}</div>
  </div>
</template>
