<script setup>
import { computed } from 'vue'
import EmptyGuide from '../../../shared/components/EmptyGuide.vue'
import ResizableWorkbenchSplit from './ResizableWorkbenchSplit.vue'
import { getJapanCalendarMeta } from '../../../utils/japanHolidayCalendar'

const props = defineProps({
  visible: { type: Boolean, required: true },
  workplaces: { type: Array, required: true },
  departments: { type: Array, required: true },
  scheduleBoard: { type: Object, required: true },
  scheduleFilters: { type: Object, required: true },
  scheduleTemplateTip: { type: Object, required: true },
  scheduleForm: { type: Object, required: true },
  batchWizard: { type: Object, required: true },
  unassignedItems: { type: Array, required: true },
  t: { type: Function, required: true },
  onRefresh: { type: Function, required: true },
  onSelectTemplate: { type: Function, required: true },
  onApplySchedule: { type: Function, required: true },
  onDeleteSchedule: { type: Function, required: true },
  onCopyLastWeek: { type: Function, required: true },
  onCopyLastMonth: { type: Function, required: true },
  onExport: { type: Function, required: true },
  onCheckUnassigned: { type: Function, required: true },
  onOpenBatchWizard: { type: Function, required: true },
  onCloseBatchWizard: { type: Function, required: true },
  onNextBatchStep: { type: Function, required: true },
  onPrevBatchStep: { type: Function, required: true },
  onConfirmBatchWizard: { type: Function, required: true }
})

// 先把排班明细按员工和日期建索引，这样日历格子就能 O(1) 找到当前班次。
const scheduleIndex = computed(() => {
  const index = new Map()
  ;(props.scheduleBoard.scheduleItems || []).forEach((item) => {
    index.set(`${item.employeeId}_${item.workDate}`, item)
  })
  return index
})

// 右侧模板面板需要知道当前已选模板对象，方便显示颜色、时间和说明。
const selectedTemplate = computed(() =>
  (props.scheduleBoard.shiftTemplates || []).find((item) => item.id === props.scheduleForm.selectedTemplateId) || null
)

// 排班表头需要把日期扩展成“月日 + 星期 + 红日子名”，统一在这里把元信息算好。
const dateHeadItems = computed(() =>
  (props.scheduleBoard.dates || []).map((dateText) => {
    // 先读取当前日期在日本日历语境下的星期与节假日状态，供表头着色和文案显示。
    const calendarMeta = getJapanCalendarMeta(dateText)
    // 星期标签仍走当前页面语言字典，保证中日切换时表头文案同步变化。
    const weekdayKeyMap = ['weekdaySun', 'weekdayMon', 'weekdayTue', 'weekdayWed', 'weekdayThu', 'weekdayFri', 'weekdaySat']
    // 节日名称在有 holidayKey 时再从字典里取，普通日期保持空字符串避免多余占位。
    const holidayLabel = calendarMeta.holidayKey ? props.t(calendarMeta.holidayKey) : ''
    // 返回表头渲染需要的完整对象，避免模板里重复计算同一批日期状态。
    return {
      dateText,
      dateLabel: dateText.slice(5),
      weekdayLabel: props.t(weekdayKeyMap[calendarMeta.weekday]),
      holidayLabel,
      isSaturday: calendarMeta.isSaturday,
      isSunday: calendarMeta.isSunday,
      isHoliday: calendarMeta.isHoliday
    }
  })
)

// 批量向导的预览说明由当前可见员工数、日期范围和模板共同决定。
const batchPreviewText = computed(() => {
  const employeeCount = props.batchWizard.employeeIds.length
  const template = (props.scheduleBoard.shiftTemplates || []).find((item) => item.id === props.batchWizard.shiftTemplateId)
  if (!employeeCount || !props.batchWizard.startDate || !props.batchWizard.endDate || !template) {
    return props.t('scheduleBatchPreviewEmpty')
  }
  return props.t('scheduleBatchPreviewReady')
    .replace('{employees}', String(employeeCount))
    .replace('{startDate}', props.batchWizard.startDate)
    .replace('{endDate}', props.batchWizard.endDate)
    .replace('{template}', template.templateName)
})

// 单个员工格子需要读取当前排班对象，没有排班时返回 null 交给空状态样式处理。
const getCellItem = (employeeId, workDate) => scheduleIndex.value.get(`${employeeId}_${workDate}`) || null

// 批量向导的步骤标签固定是 5 步，直接按文案键生成，保持页面和设计稿一致。
const wizardStepLabels = computed(() => [
  props.t('scheduleBatchStep1'),
  props.t('scheduleBatchStep2'),
  props.t('scheduleBatchStep3'),
  props.t('scheduleBatchStep4'),
  props.t('scheduleBatchStep5')
])
</script>

<template>
  <ResizableWorkbenchSplit v-show="visible" storage-key="attendance-schedule-split" :default-left-percent="72" :min-left-percent="56" :max-left-percent="80">
    <template #left>
      <article class="seladmin-panel seladmin-surface selattendance-data-panel">
        <div class="seladmin-panel-header">
          <div>
            <h2>{{ t('scheduleTitle') }}</h2>
            <p class="seladmin-copy">{{ t('scheduleLead') }}</p>
          </div>
          <button class="seladmin-button seladmin-button-secondary" type="button" @click="onRefresh()">{{ t('scheduleRefresh') }}</button>
        </div>

        <div class="selattendance-schedule-toolbar">
          <label class="seladmin-field">
            <span>{{ t('scheduleMonth') }}</span>
            <input v-model="scheduleFilters.month" type="month" />
          </label>
          <label class="seladmin-field">
            <span>{{ t('workplace') }}</span>
            <select v-model="scheduleFilters.workplaceId">
              <option value="">{{ t('scheduleWorkplaceFilterHint') }}</option>
              <option v-for="item in workplaces" :key="item.id" :value="item.id">{{ item.workplaceName }}</option>
            </select>
          </label>
          <label class="seladmin-field">
            <span>{{ t('departmentName') }}</span>
            <select v-model="scheduleFilters.departmentId">
              <option value="">{{ t('scheduleDepartmentFilterHint') }}</option>
              <option v-for="item in departments" :key="item.id" :value="item.id">{{ item.departmentName }}</option>
            </select>
          </label>
          <label class="seladmin-field">
            <span>{{ t('scheduleKeyword') }}</span>
            <input v-model="scheduleFilters.employeeKeyword" :placeholder="t('scheduleKeywordHint')" />
          </label>
          <label class="selattendance-inline-check selattendance-inline-check-wide">
            <input v-model="scheduleFilters.onlyUnassigned" type="checkbox" />
            <span>{{ t('scheduleOnlyUnassigned') }}</span>
          </label>
        </div>

        <div class="selattendance-schedule-actionbar">
          <button class="seladmin-button seladmin-button-primary" type="button" @click="onOpenBatchWizard()">{{ t('scheduleBatchOpen') }}</button>
          <button class="seladmin-button seladmin-button-secondary" type="button" @click="onCopyLastWeek()">{{ t('scheduleCopyLastWeek') }}</button>
          <button class="seladmin-button seladmin-button-secondary" type="button" @click="onCopyLastMonth()">{{ t('scheduleCopyLastMonth') }}</button>
          <button class="seladmin-button seladmin-button-secondary" type="button" @click="onCheckUnassigned()">{{ t('scheduleCheckUnassigned') }}</button>
          <button class="seladmin-button seladmin-button-secondary" type="button" @click="onExport()">{{ t('scheduleExport') }}</button>
        </div>

        <EmptyGuide
          v-if="!scheduleBoard.employeeRows?.length"
          :title="t('scheduleEmptyTitle')"
          :description="t('scheduleEmptyDescription')"
        />

        <div v-else class="selattendance-schedule-grid-shell">
          <table class="selattendance-schedule-grid">
            <thead>
              <tr>
                <th class="selattendance-sticky-column selattendance-employee-head">{{ t('scheduleEmployeeColumn') }}</th>
                <th
                  v-for="item in dateHeadItems"
                  :key="item.dateText"
                  class="selattendance-date-head"
                  :class="{
                    saturday: item.isSaturday && !item.isHoliday,
                    sunday: item.isSunday && !item.isHoliday,
                    holiday: item.isHoliday
                  }"
                >
                  <span class="selattendance-date-chip">{{ item.dateLabel }}</span>
                  <small class="selattendance-date-weekday">{{ item.weekdayLabel }}</small>
                  <small v-if="item.holidayLabel" class="selattendance-date-holiday">{{ item.holidayLabel }}</small>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in scheduleBoard.employeeRows" :key="row.employeeId">
                <td class="selattendance-sticky-column selattendance-employee-cell">
                  <div class="selattendance-employee-primary">
                    <strong>{{ row.employeeName }}</strong>
                    <span>{{ row.employeeNo }}</span>
                  </div>
                  <div class="selattendance-employee-meta">
                    <small>{{ row.departmentName }}</small>
                    <small>{{ row.workplaceName }}</small>
                  </div>
                  <div class="selattendance-unassigned-chip" :class="{ warm: row.unassignedCount > 0 }">
                    {{ t('scheduleUnassignedCount').replace('{count}', String(row.unassignedCount)) }}
                  </div>
                </td>
                <td
                  v-for="date in scheduleBoard.dates"
                  :key="`${row.employeeId}_${date}`"
                  class="selattendance-schedule-cell"
                  :class="{
                    empty: !getCellItem(row.employeeId, date),
                    filled: !!getCellItem(row.employeeId, date),
                    selected: scheduleForm.selectedEmployeeId === row.employeeId && scheduleForm.selectedWorkDate === date
                  }"
                  @click="onApplySchedule(row, date, getCellItem(row.employeeId, date))"
                >
                  <template v-if="getCellItem(row.employeeId, date)">
                    <span class="selattendance-schedule-tag" :style="{ '--selattendance-tag-color': getCellItem(row.employeeId, date).color || 'var(--seladmin-accent, #4f7cff)' }">
                      {{ getCellItem(row.employeeId, date).templateName }}
                    </span>
                    <div class="selattendance-schedule-timeblock">
                      <small>{{ getCellItem(row.employeeId, date).startTime || t('scheduleRestLabel') }}</small>
                      <small v-if="getCellItem(row.employeeId, date).endTime" class="selattendance-schedule-time-separator">-</small>
                      <small v-if="getCellItem(row.employeeId, date).endTime">{{ getCellItem(row.employeeId, date).endTime }}</small>
                    </div>
                  </template>
                  <template v-else>
                    <strong>{{ t('scheduleUnassignedShort') }}</strong>
                    <small>{{ t('scheduleCellGuide') }}</small>
                  </template>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>
    </template>

    <template #right>
      <article class="seladmin-panel seladmin-surface selattendance-form-panel selattendance-schedule-side">
        <div class="seladmin-panel-header">
          <div>
            <h2>{{ t('scheduleSideTitle') }}</h2>
            <p class="seladmin-copy">{{ t('scheduleSideHint') }}</p>
          </div>
        </div>

        <section class="selattendance-schedule-side-block selattendance-schedule-template-block">
          <div class="seladmin-panel-header"><h3>{{ t('scheduleTemplatePanelTitle') }}</h3></div>
          <div class="selattendance-template-palette-shell" data-schedule-template-target>
            <div class="selattendance-template-palette" :class="{ 'selattendance-template-palette-target': scheduleTemplateTip.open }">
            <button
              v-for="template in scheduleBoard.shiftTemplates"
              :key="template.id"
              type="button"
              class="selattendance-template-card"
              :class="{ active: selectedTemplate?.id === template.id }"
              :style="{ '--selattendance-template-color': template.color || 'var(--seladmin-accent, #4f7cff)' }"
              @click="onSelectTemplate(template)"
            >
              <strong>{{ template.templateName }}</strong>
              <small>{{ template.startTime || t('scheduleRestLabel') }}<template v-if="template.endTime"> - {{ template.endTime }}</template></small>
            </button>
          </div>
          </div>
          <p class="seladmin-copy">
            {{ selectedTemplate ? t('scheduleTemplatePicked').replace('{template}', selectedTemplate.templateName) : t('scheduleTemplateNeedPick') }}
          </p>
          <label class="seladmin-field">
            <span>{{ t('scheduleRemark') }}</span>
            <textarea v-model="scheduleForm.remark" rows="3" :placeholder="t('scheduleRemarkHint')" />
          </label>
        </section>

        <section class="selattendance-schedule-side-block" v-if="scheduleForm.selectedWorkDate">
          <div class="seladmin-panel-header"><h3>{{ t('scheduleCurrentSelectionTitle') }}</h3></div>
          <p class="seladmin-copy">
            {{ t('scheduleCurrentSelection').replace('{employee}', scheduleForm.selectedEmployeeName || '-').replace('{date}', scheduleForm.selectedWorkDate || '-') }}
          </p>
          <p class="seladmin-copy" v-if="scheduleForm.selectedTemplateName">
            {{ t('scheduleCurrentTemplate').replace('{template}', scheduleForm.selectedTemplateName) }}
          </p>
          <button
            v-if="scheduleForm.selectedScheduleId"
            class="seladmin-button seladmin-button-secondary"
            type="button"
            @click="onDeleteSchedule(scheduleForm.selectedScheduleId)"
          >
            {{ t('scheduleDeleteCurrent') }}
          </button>
        </section>

        <section class="selattendance-schedule-side-block">
          <div class="seladmin-panel-header"><h3>{{ t('scheduleLegendTitle') }}</h3></div>
          <div class="selattendance-legend-list">
            <div class="selattendance-legend-item"><span class="dot warm" />{{ t('scheduleLegendEmpty') }}</div>
            <div class="selattendance-legend-item"><span class="dot filled" />{{ t('scheduleLegendFilled') }}</div>
            <div class="selattendance-legend-item"><span class="dot active" />{{ t('scheduleLegendSelected') }}</div>
          </div>
        </section>

        <section class="selattendance-schedule-side-block">
          <div class="seladmin-panel-header">
            <h3>{{ t('scheduleBatchTitle') }}</h3>
            <button v-if="batchWizard.open" type="button" class="seladmin-button seladmin-button-secondary" @click="onCloseBatchWizard()">{{ t('scheduleBatchClose') }}</button>
          </div>
          <p class="seladmin-copy">{{ t('scheduleBatchLead') }}</p>
          <div class="selattendance-batch-steps">
            <span
              v-for="(label, index) in wizardStepLabels"
              :key="label"
              class="selattendance-batch-step"
              :class="{ active: batchWizard.step === index + 1, done: batchWizard.step > index + 1 }"
            >
              {{ index + 1 }}. {{ label }}
            </span>
          </div>
          <div v-if="batchWizard.open" class="selattendance-batch-body">
            <div v-if="batchWizard.step === 1" class="selattendance-batch-panel">
              <p class="seladmin-copy">{{ t('scheduleBatchStep1Hint') }}</p>
              <label v-for="row in scheduleBoard.employeeRows" :key="row.employeeId" class="selattendance-batch-check">
                <input v-model="batchWizard.employeeIds" :value="row.employeeId" type="checkbox" />
                <span>{{ row.employeeName }} / {{ row.employeeNo }}</span>
              </label>
            </div>
            <div v-else-if="batchWizard.step === 2" class="selattendance-batch-panel">
              <p class="seladmin-copy">{{ t('scheduleBatchStep2Hint') }}</p>
              <div class="seladmin-form-grid">
                <label class="seladmin-field"><span>{{ t('scheduleDateStart') }}</span><input v-model="batchWizard.startDate" type="date" /></label>
                <label class="seladmin-field"><span>{{ t('scheduleDateEnd') }}</span><input v-model="batchWizard.endDate" type="date" /></label>
              </div>
            </div>
            <div v-else-if="batchWizard.step === 3" class="selattendance-batch-panel">
              <p class="seladmin-copy">{{ t('scheduleBatchStep3Hint') }}</p>
              <div class="selattendance-template-palette compact">
                <button
                  v-for="template in scheduleBoard.shiftTemplates"
                  :key="template.id"
                  type="button"
                  class="selattendance-template-card"
                  :class="{ active: batchWizard.shiftTemplateId === template.id }"
                  :style="{ '--selattendance-template-color': template.color || 'var(--seladmin-accent, #4f7cff)' }"
                  @click="batchWizard.shiftTemplateId = template.id"
                >
                  <strong>{{ template.templateName }}</strong>
                  <small>{{ template.startTime || t('scheduleRestLabel') }}</small>
                </button>
              </div>
            </div>
            <div v-else-if="batchWizard.step === 4" class="selattendance-batch-panel">
              <p class="seladmin-copy">{{ batchPreviewText }}</p>
              <label class="selattendance-inline-check"><input v-model="batchWizard.skipExisting" type="checkbox" /><span>{{ t('scheduleSkipExisting') }}</span></label>
              <label class="selattendance-inline-check"><input v-model="batchWizard.overwriteExisting" type="checkbox" /><span>{{ t('scheduleOverwriteExisting') }}</span></label>
              <label class="seladmin-field"><span>{{ t('scheduleRemark') }}</span><textarea v-model="batchWizard.remark" rows="3" :placeholder="t('scheduleRemarkHint')" /></label>
            </div>
            <div v-else class="selattendance-batch-panel">
              <p class="seladmin-copy">{{ t('scheduleBatchStep5Hint') }}</p>
              <p class="seladmin-copy">{{ batchPreviewText }}</p>
              <button class="seladmin-button seladmin-button-primary" type="button" @click="onConfirmBatchWizard()">{{ t('scheduleBatchConfirm') }}</button>
            </div>
          </div>
          <div class="seladmin-action-row" v-if="batchWizard.open">
            <button class="seladmin-button seladmin-button-secondary" type="button" :disabled="batchWizard.step === 1" @click="onPrevBatchStep()">{{ t('scheduleBatchPrev') }}</button>
            <button class="seladmin-button seladmin-button-primary" type="button" :disabled="batchWizard.step === 5" @click="onNextBatchStep()">{{ t('scheduleBatchNext') }}</button>
          </div>
        </section>

        <section class="selattendance-schedule-side-block" v-if="unassignedItems.length">
          <div class="seladmin-panel-header"><h3>{{ t('scheduleUnassignedPanelTitle') }}</h3></div>
          <article v-for="item in unassignedItems" :key="item.employeeId" class="selattendance-unassigned-card">
            <strong>{{ item.employeeName }} / {{ item.employeeNo }}</strong>
            <small>{{ t('scheduleUnassignedCount').replace('{count}', String(item.unassignedCount)) }}</small>
            <p>{{ item.missingDates.join(' / ') }}</p>
          </article>
        </section>
      </article>
    </template>
  </ResizableWorkbenchSplit>
</template>
