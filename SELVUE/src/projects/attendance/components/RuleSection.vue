<script setup>
import { computed, ref, watch } from 'vue'
import EmptyGuide from '../../../shared/components/EmptyGuide.vue'
import SharedDataTable from '../../../shared/components/SharedDataTable.vue'
import ThreePaneWorkbenchLayout from '../../../shared/components/ThreePaneWorkbenchLayout.vue'
import { attendanceMasterDataLayoutPreset } from '../constants/workbenchLayoutPresets'

const props = defineProps({
  visible: { type: Boolean, required: true },
  employees: { type: Array, required: true },
  ruleWorkbench: { type: Object, required: true },
  ruleForm: { type: Object, required: true },
  ruleAssignmentForm: { type: Object, required: true },
  t: { type: Function, required: true },
  onSubmitRule: { type: Function, required: true },
  onResetRule: { type: Function, required: true },
  onEditRule: { type: Function, required: true },
  onSubmitAssignment: { type: Function, required: true },
  onResetAssignment: { type: Function, required: true },
  onEditAssignment: { type: Function, required: true }
})

// 第七阶段要求高级规则默认折叠，因此这里单独维护一个只影响视图的折叠状态。
const advancedExpanded = ref(false)

// 规则主键变化代表切到另一条规则或回到新建状态，此时统一收回高级区，避免旧状态残留。
watch(
  () => props.ruleForm.id,
  () => {
    advancedExpanded.value = false
  }
)

// 编辑已有规则时持续暴露影响提示，提醒管理员这次保存只会影响后续计算口径。
const editingExistingRule = computed(() => Boolean(props.ruleForm.id))

// 规则列表保留核心字段，避免左侧清单塞入过多配置项影响浏览效率。
const ruleColumns = computed(() => [
  { key: 'ruleCode', label: props.t('ruleCode'), minWidth: '132px' },
  { key: 'ruleName', label: props.t('ruleName'), wrap: true, minWidth: '180px' },
  { key: 'standardDailyMinutes', label: props.t('ruleStandardDailyMinutes'), minWidth: '120px' },
  { key: 'monthlyOvertimeAlertHours', label: props.t('ruleMonthlyAlertHours'), minWidth: '120px' },
  { key: 'activeFlag', label: props.t('status'), minWidth: '90px' },
  { key: 'actions', label: '', minWidth: '96px' }
])

// 员工适用清单突出当前规则和风险分钟，方便管理员从同一页完成查看与调整。
const assignmentColumns = computed(() => [
  { key: 'employeeNo', label: props.t('employeeNo'), minWidth: '116px' },
  { key: 'employeeName', label: props.t('employeeName'), minWidth: '132px' },
  { key: 'ruleName', label: props.t('ruleAssignedRule'), minWidth: '160px', wrap: true },
  { key: 'monthlyOvertimeMinutes', label: props.t('ruleMonthlyOvertime'), minWidth: '126px' },
  { key: 'yearlyPaidLeaveDays', label: props.t('ruleYearlyPaidLeaveDays'), minWidth: '110px' }
])

// 分钟统一转成小时 + 分钟展示，减少管理员理解负担。
const formatMinutes = (value) => {
  const totalMinutes = Number(value || 0)
  const hours = Math.floor(totalMinutes / 60)
  const minutes = totalMinutes % 60
  return `${hours}${props.t('monthlyHourUnit')} ${minutes}${props.t('monthlyMinuteUnit')}`
}

// 规则预警类型统一转成人话标题，避免页面直接暴露英文枚举。
const formatAlertType = (alertType) => {
  if (alertType === 'MONTHLY_OVERTIME') return props.t('ruleAlertMonthly')
  if (alertType === 'YEARLY_OVERTIME') return props.t('ruleAlertYearly')
  if (alertType === 'PAID_LEAVE_REMINDER') return props.t('ruleAlertPaidLeave')
  return alertType || '-'
}

// 规则预警值根据分钟型和天数型分别格式化，保证一张列表同时兼容三种提醒。
const formatAlertValue = (alert) => {
  if (alert.alertType === 'PAID_LEAVE_REMINDER') {
    return `${Number(alert.currentValueDays || 0)} / ${Number(alert.thresholdDays || 0)}`
  }
  return `${formatMinutes(alert.currentValueMinutes)} / ${formatMinutes(alert.thresholdMinutes)}`
}

// 重置规则时同步恢复“高级规则折叠”的默认页面状态。
const handleResetRule = () => {
  advancedExpanded.value = false
  props.onResetRule()
}

// 切换到某条规则编辑时先收回高级区，让管理员先核对基础口径和影响提示。
const handleEditRule = (rule) => {
  advancedExpanded.value = false
  props.onEditRule(rule)
}
</script>

<template>
  <ThreePaneWorkbenchLayout
    v-show="visible"
    class="selattendance-master-split"
    outer-storage-key="attendance-rule-split"
    :outer-default-left-percent="attendanceMasterDataLayoutPreset.outerDefaultLeftPercent"
    :outer-min-left-percent="attendanceMasterDataLayoutPreset.outerMinLeftPercent"
    :outer-max-left-percent="attendanceMasterDataLayoutPreset.outerMaxLeftPercent"
  >
    <template #left>
      <!-- 左栏只保留规则清单，让标题、筛选和指标卡全部回到通用 workbench header。 -->
      <article class="seladmin-panel seladmin-surface selattendance-data-panel selattendance-master-list-panel">
        <div class="seladmin-panel-header"><h2>{{ t('ruleListTitle') }}</h2></div>
        <EmptyGuide
          v-if="!ruleWorkbench.rules?.length"
          :title="t('ruleListTitle')"
          :description="t('ruleEmpty')"
        />
        <SharedDataTable
          v-else
          variant="admin"
          :columns="ruleColumns"
          :rows="ruleWorkbench.rules"
          row-key="id"
          :active-row-key="ruleForm.id"
          clickable-rows
          min-table-width="860px"
          @row-click="handleEditRule"
        >
          <template #cell-standardDailyMinutes="{ row }">
            <span>{{ formatMinutes(row.standardDailyMinutes) }}</span>
          </template>
          <template #cell-activeFlag="{ row }">
            <span>{{ row.activeFlag ? t('activeStatus') : t('inactiveStatus') }}</span>
          </template>
          <template #cell-actions="{ row }">
            <button type="button" @click.stop="handleEditRule(row)">{{ t('save') }}</button>
          </template>
        </SharedDataTable>
      </article>
    </template>

    <template #main>
      <!-- 中栏专注规则编辑，管理员从左侧选中规则后就在这里核对影响提示和正式口径。 -->
      <article class="seladmin-panel seladmin-surface selattendance-form-panel selattendance-master-detail-panel">
        <div class="seladmin-panel-header"><h2>{{ t('ruleFormTitle') }}</h2></div>
        <div v-if="editingExistingRule" class="selattendance-context-strip">
          <span>{{ t('ruleImpactInlineMessage').replace('{name}', ruleForm.ruleName || ruleForm.ruleCode || t('ruleFormTitle')) }}</span>
        </div>
        <div class="seladmin-form-grid">
          <label class="seladmin-field"><span>{{ t('ruleCode') }}</span><input v-model="ruleForm.ruleCode" /></label>
          <label class="seladmin-field"><span>{{ t('ruleName') }}</span><input v-model="ruleForm.ruleName" /></label>
          <label class="seladmin-field"><span>{{ t('ruleStandardDailyMinutes') }}</span><input v-model="ruleForm.standardDailyMinutes" type="number" min="1" /></label>
          <label class="seladmin-field"><span>{{ t('ruleNightStart') }}</span><input v-model="ruleForm.nightWorkStart" type="time" /></label>
          <label class="seladmin-field"><span>{{ t('ruleNightEnd') }}</span><input v-model="ruleForm.nightWorkEnd" type="time" /></label>
          <label class="seladmin-field"><span>{{ t('ruleMonthlyAlertHours') }}</span><input v-model="ruleForm.monthlyOvertimeAlertHours" type="number" min="1" /></label>
          <label class="seladmin-field"><span>{{ t('ruleYearlyAlertHours') }}</span><input v-model="ruleForm.yearlyOvertimeAlertHours" type="number" min="1" /></label>
          <label class="seladmin-field selattendance-switch-field">
            <span>{{ t('ruleActiveFlag') }}</span>
            <input v-model="ruleForm.activeFlag" type="checkbox" />
          </label>
        </div>
        <div class="selattendance-rule-advanced-toggle">
          <div class="selattendance-rule-advanced-copy">
            <strong>{{ t('ruleAdvancedTitle') }}</strong>
            <p>{{ t('ruleAdvancedHint') }}</p>
          </div>
          <!-- 高级规则默认折叠，避免第一次进入第七阶段就被大量次要字段淹没。 -->
          <button class="seladmin-button seladmin-button-secondary" type="button" @click="advancedExpanded = !advancedExpanded">
            {{ advancedExpanded ? t('ruleAdvancedCollapse') : t('ruleAdvancedExpand') }}
          </button>
        </div>
        <div v-if="advancedExpanded" class="selattendance-rule-advanced-panel">
          <div class="seladmin-form-grid">
            <label class="seladmin-field"><span>{{ t('ruleStandardWeeklyMinutes') }}</span><input v-model="ruleForm.standardWeeklyMinutes" type="number" min="1" /></label>
            <label class="seladmin-field"><span>{{ t('ruleRoundingUnitMinutes') }}</span><input v-model="ruleForm.roundingUnitMinutes" type="number" min="1" /></label>
            <label class="seladmin-field"><span>{{ t('ruleRoundingMode') }}</span><input v-model="ruleForm.roundingMode" /></label>
            <label class="seladmin-field"><span>{{ t('ruleAutoBreakThreshold') }}</span><input v-model="ruleForm.autoBreakThresholdMinutes" type="number" min="0" /></label>
            <label class="seladmin-field"><span>{{ t('ruleAutoBreakDeduct') }}</span><input v-model="ruleForm.autoBreakDeductMinutes" type="number" min="0" /></label>
            <label class="seladmin-field selattendance-switch-field">
              <span>{{ t('ruleAutoBreakEnabled') }}</span>
              <input v-model="ruleForm.autoBreakEnabled" type="checkbox" />
            </label>
            <label class="seladmin-field selattendance-switch-field">
              <span>{{ t('rulePaidLeaveReminderEnabled') }}</span>
              <input v-model="ruleForm.paidLeaveReminderEnabled" type="checkbox" />
            </label>
          </div>
          <label class="seladmin-field">
            <span>{{ t('remark') }}</span>
            <textarea v-model="ruleForm.note" rows="4" />
          </label>
        </div>
        <div class="seladmin-action-row">
          <button class="seladmin-button seladmin-button-primary" type="button" @click="onSubmitRule()">{{ t('save') }}</button>
          <button class="seladmin-button seladmin-button-secondary" type="button" @click="handleResetRule()">{{ t('create') }}</button>
        </div>
      </article>
    </template>

    <template #right>
      <!-- 右栏把员工适用和预警放在一起，方便管理员改完规则后立即查看谁会受影响。 -->
      <article class="seladmin-panel seladmin-surface selattendance-form-panel selattendance-master-detail-panel">
        <div class="seladmin-panel-header"><h2>{{ t('ruleAssignmentTitle') }}</h2></div>
        <div class="seladmin-form-grid">
          <label class="seladmin-field">
            <span>{{ t('employeeName') }}</span>
            <select v-model="ruleAssignmentForm.employeeId">
              <option value="">{{ t('ruleSelectEmployee') }}</option>
              <option v-for="item in employees" :key="item.id" :value="item.id">
                {{ item.employeeNo }} / {{ item.employeeName }}
              </option>
            </select>
          </label>
          <label class="seladmin-field">
            <span>{{ t('ruleAssignedRule') }}</span>
            <select v-model="ruleAssignmentForm.ruleId">
              <option value="">{{ t('ruleSelectRule') }}</option>
              <option v-for="item in ruleWorkbench.rules" :key="item.id" :value="item.id">
                {{ item.ruleName }}
              </option>
            </select>
          </label>
          <label class="seladmin-field"><span>{{ t('effectiveStartDate') }}</span><input v-model="ruleAssignmentForm.effectiveStartDate" type="date" /></label>
          <label class="seladmin-field"><span>{{ t('effectiveEndDate') }}</span><input v-model="ruleAssignmentForm.effectiveEndDate" type="date" /></label>
        </div>
        <label class="seladmin-field">
          <span>{{ t('remark') }}</span>
          <textarea v-model="ruleAssignmentForm.note" rows="3" />
        </label>
        <div class="seladmin-action-row">
          <button class="seladmin-button seladmin-button-primary" type="button" @click="onSubmitAssignment()">{{ t('save') }}</button>
          <button class="seladmin-button seladmin-button-secondary" type="button" @click="onResetAssignment()">{{ t('create') }}</button>
        </div>

        <div class="seladmin-subsection">
          <div class="seladmin-panel-header"><h3>{{ t('ruleAssignmentListTitle') }}</h3></div>
          <EmptyGuide
            v-if="!ruleWorkbench.assignments?.length"
            :title="t('ruleAssignmentListTitle')"
            :description="t('ruleAssignmentEmpty')"
          />
          <SharedDataTable
            v-else
            variant="admin"
            :columns="assignmentColumns"
            :rows="ruleWorkbench.assignments"
            row-key="employeeId"
            :active-row-key="ruleAssignmentForm.employeeId"
            clickable-rows
            min-table-width="920px"
            @row-click="onEditAssignment"
          >
            <template #cell-ruleName="{ row }">
              <span>{{ row.ruleName || t('ruleUnassigned') }}</span>
            </template>
            <template #cell-monthlyOvertimeMinutes="{ row }">
              <span>{{ formatMinutes(row.monthlyOvertimeMinutes) }}</span>
            </template>
            <template #cell-yearlyPaidLeaveDays="{ row }">
              <span>{{ Number(row.yearlyPaidLeaveDays || 0) }}</span>
            </template>
          </SharedDataTable>
        </div>

        <div class="seladmin-subsection">
          <div class="seladmin-panel-header"><h3>{{ t('ruleAlertBoardTitle') }}</h3></div>
          <EmptyGuide
            v-if="!ruleWorkbench.alerts?.length"
            :title="t('ruleAlertBoardTitle')"
            :description="t('ruleAlertEmpty')"
          />
          <ul v-else class="selattendance-rule-alert-list">
            <li
              v-for="alert in ruleWorkbench.alerts"
              :key="`${alert.alertType}-${alert.employeeId}-${alert.ruleId || 'none'}`"
              class="selattendance-rule-alert-item"
              :class="{ reminder: alert.alertLevel === 'REMINDER' }"
            >
              <!-- 每条预警同时展示员工、提醒类型、规则名和当前值，减少管理员来回切换查看。 -->
              <strong>{{ alert.employeeName }} / {{ formatAlertType(alert.alertType) }}</strong>
              <small>{{ alert.ruleName || t('ruleUnassigned') }}</small>
              <small>{{ formatAlertValue(alert) }}</small>
            </li>
          </ul>
        </div>
      </article>
    </template>
  </ThreePaneWorkbenchLayout>
</template>
