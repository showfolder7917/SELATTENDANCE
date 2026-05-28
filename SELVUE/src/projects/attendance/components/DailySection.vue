<script setup>
import { computed } from 'vue'
import ResizableWorkbenchSplit from './ResizableWorkbenchSplit.vue'

const props = defineProps({
  visible: { type: Boolean, required: true },
  workplaces: { type: Array, required: true },
  departments: { type: Array, required: true },
  dailyList: { type: Object, required: true },
  dailyFilters: { type: Object, required: true },
  dailyDetail: { type: Object, default: null },
  t: { type: Function, required: true },
  onRefresh: { type: Function, required: true },
  onSelectDaily: { type: Function, required: true },
  onRecalculateDaily: { type: Function, required: true },
  onRecalculateRange: { type: Function, required: true }
})

// 固定每页条数档位，保证第四阶段前端分页值与后端数据库分页口径完全一致。
const pageSizeOptions = [20, 50, 100, 200]

// 当前页优先使用后端回传页码，避免前端自行换算导致分页状态漂移。
const currentPage = computed(() => Number(props.dailyList.page || props.dailyFilters.page || 1))

// 总页数优先使用后端回传值，没有时再按总数和每页条数做兜底换算。
const totalPages = computed(() => {
  if (props.dailyList.totalPages) {
    return Number(props.dailyList.totalPages)
  }
  const pageSize = Number(props.dailyFilters.pageSize || 20)
  const total = Number(props.dailyList.total || 0)
  return Math.max(1, Math.ceil(total / pageSize))
})

// 上一页按钮在第 1 页时禁用，避免继续发无意义的数据库分页请求。
const isPrevDisabled = computed(() => currentPage.value <= 1)

// 下一页按钮在最后一页时禁用，避免请求不存在的页码。
const isNextDisabled = computed(() => currentPage.value >= totalPages.value)

// 把后端状态码翻译成当前语言的第四阶段业务状态名称。
function translateDailyStatus(status) {
  const statusMap = {
    NORMAL: 'dailyStatusNormal',
    LATE: 'dailyStatusLate',
    EARLY_LEAVE: 'dailyStatusEarlyLeave',
    MISSING_CLOCK_IN: 'dailyStatusMissingClockIn',
    MISSING_CLOCK_OUT: 'dailyStatusMissingClockOut',
    ABSENCE: 'dailyStatusAbsence',
    NO_SCHEDULE: 'dailyStatusNoSchedule',
    HOLIDAY_WORK: 'dailyStatusHolidayWork'
  }
  return props.t(statusMap[status] || 'status')
}

// 把异常等级翻译为当前语言，方便用户快速区分提醒和错误。
function translateExceptionLevel(level) {
  if (level === 'ERROR') return props.t('dailyExceptionLevelError')
  if (level === 'WARN') return props.t('dailyExceptionLevelWarn')
  return level || '-'
}

// 把日期时间统一渲染成人眼易读格式，避免详情区出现原始 ISO 串。
function formatDateTime(value) {
  return value?.replace?.('T', ' ') || value || '-'
}

// 点击上一页时只回退一个页码，让外层 watch 触发新的数据库分页请求。
function goPrevPage() {
  if (isPrevDisabled.value) return
  props.dailyFilters.page = currentPage.value - 1
}

// 点击下一页时只前进一个页码，让外层 watch 触发新的数据库分页请求。
function goNextPage() {
  if (isNextDisabled.value) return
  props.dailyFilters.page = currentPage.value + 1
}

// 切换每页条数时同步回到第 1 页，避免旧页码在新页大小下跳空。
function handlePageSizeChange(event) {
  props.dailyFilters.pageSize = Number(event.target.value)
  props.dailyFilters.page = 1
}
</script>

<template>
  <ResizableWorkbenchSplit
    v-show="visible"
    storage-key="attendance-daily-split"
    :default-left-percent="67"
    :min-left-percent="48"
    :max-left-percent="78"
  >
    <template #left>
      <article class="seladmin-panel seladmin-surface selattendance-data-panel">
        <div class="seladmin-panel-header">
          <div>
            <h2>{{ t('dailyTitle') }}</h2>
            <p class="seladmin-copy">{{ t('dailyLead') }}</p>
          </div>
          <div class="selattendance-punch-actions">
            <button class="seladmin-button seladmin-button-secondary" type="button" @click="onRefresh()">{{ t('dailyRefresh') }}</button>
            <button class="seladmin-button seladmin-button-primary" type="button" @click="onRecalculateRange()">{{ t('dailyRecalculateRange') }}</button>
          </div>
        </div>

        <div class="selattendance-punch-summary">
          <div class="selattendance-punch-summary-card">
            <strong>{{ dailyList.summary?.normalCount || 0 }}</strong>
            <small>{{ t('dailySummaryNormal') }}</small>
          </div>
          <div class="selattendance-punch-summary-card warm">
            <strong>{{ dailyList.summary?.lateCount || 0 }}</strong>
            <small>{{ t('dailySummaryLate') }}</small>
          </div>
          <div class="selattendance-punch-summary-card danger">
            <strong>{{ dailyList.summary?.missingClockCount || 0 }}</strong>
            <small>{{ t('dailySummaryMissing') }}</small>
          </div>
          <div class="selattendance-punch-summary-card muted">
            <strong>{{ dailyList.summary?.absenceCount || 0 }}</strong>
            <small>{{ t('dailySummaryAbsence') }}</small>
          </div>
        </div>

        <div class="selattendance-schedule-toolbar">
          <label class="seladmin-field">
            <span>{{ t('dailyDateFrom') }}</span>
            <input v-model="dailyFilters.startDate" type="date" />
          </label>
          <label class="seladmin-field">
            <span>{{ t('dailyDateTo') }}</span>
            <input v-model="dailyFilters.endDate" type="date" />
          </label>
          <label class="seladmin-field">
            <span>{{ t('workplace') }}</span>
            <select v-model="dailyFilters.workplaceId">
              <option value="">{{ t('allWorkplaces') }}</option>
              <option v-for="item in workplaces" :key="item.id" :value="item.id">{{ item.workplaceName }}</option>
            </select>
          </label>
          <label class="seladmin-field">
            <span>{{ t('departmentName') }}</span>
            <select v-model="dailyFilters.departmentId">
              <option value="">{{ t('allDepartments') }}</option>
              <option v-for="item in departments" :key="item.id" :value="item.id">{{ item.departmentName }}</option>
            </select>
          </label>
          <label class="seladmin-field">
            <span>{{ t('employeeName') }}</span>
            <input v-model="dailyFilters.employeeKeyword" :placeholder="t('dailyEmployeeKeywordHint')" />
          </label>
          <label class="seladmin-field">
            <span>{{ t('status') }}</span>
            <select v-model="dailyFilters.status">
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
          <label class="selattendance-daily-checkbox">
            <input v-model="dailyFilters.exceptionOnly" type="checkbox" />
            <span>{{ t('dailyExceptionOnly') }}</span>
          </label>
        </div>

        <div class="selattendance-punch-list-shell">
          <table class="selattendance-punch-table">
            <thead>
              <tr>
                <th>{{ t('employeeName') }}</th>
                <th>{{ t('dailyWorkDate') }}</th>
                <th>{{ t('dailyScheduleLabel') }}</th>
                <th>{{ t('dailyActualClockIn') }}</th>
                <th>{{ t('dailyActualClockOut') }}</th>
                <th>{{ t('status') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in dailyList.items"
                :key="item.id"
                class="selattendance-punch-row"
                :class="{ active: dailyDetail?.id === item.id }"
                @click="onSelectDaily(item)"
              >
                <td>
                  <strong>{{ item.employeeName }}</strong>
                  <small>{{ item.employeeNo }} / {{ item.departmentName }}</small>
                </td>
                <td>{{ item.workDate }}</td>
                <td>{{ item.scheduleLabel || '-' }}</td>
                <td>{{ formatDateTime(item.actualClockIn) }}</td>
                <td>{{ formatDateTime(item.actualClockOut) }}</td>
                <td>
                  <span class="selattendance-punch-status" :class="`status-${(item.status || '').toLowerCase()}`">
                    {{ translateDailyStatus(item.status) }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="selattendance-punch-pagination">
          <label class="seladmin-field selattendance-punch-pagination-size">
            <span>{{ t('dailyPageSize') }}</span>
            <select :value="String(dailyFilters.pageSize || 20)" @change="handlePageSizeChange">
              <option v-for="size in pageSizeOptions" :key="size" :value="String(size)">{{ size }}</option>
            </select>
          </label>
          <div class="selattendance-punch-pagination-meta">
            <small>{{ t('dailyPaginationSummary').replace('{total}', String(dailyList.total || 0)) }}</small>
            <small>{{ t('dailyPaginationCurrent').replace('{page}', String(currentPage)).replace('{totalPages}', String(totalPages)) }}</small>
          </div>
          <div class="selattendance-punch-pagination-actions">
            <button class="seladmin-button seladmin-button-secondary" type="button" :disabled="isPrevDisabled" @click="goPrevPage()">{{ t('dailyPrevPage') }}</button>
            <button class="seladmin-button seladmin-button-secondary" type="button" :disabled="isNextDisabled" @click="goNextPage()">{{ t('dailyNextPage') }}</button>
          </div>
        </div>
      </article>
    </template>

    <template #right>
      <article class="seladmin-panel seladmin-surface selattendance-form-panel selattendance-punch-side">
        <section class="selattendance-schedule-side-block" v-if="dailyDetail">
          <div class="seladmin-panel-header">
            <div>
              <h3>{{ t('dailyDetailTitle') }}</h3>
              <p class="seladmin-copy">{{ t('dailyDetailHint').replace('{date}', String(dailyDetail.workDate || '-')) }}</p>
            </div>
            <button class="seladmin-button seladmin-button-primary" type="button" @click="onRecalculateDaily()">{{ t('dailyRecalculateOne') }}</button>
          </div>

          <div class="selattendance-punch-detail-grid">
            <strong>{{ dailyDetail.employeeName }} / {{ dailyDetail.employeeNo }}</strong>
            <small>{{ dailyDetail.workplaceName }} / {{ dailyDetail.departmentName }}</small>
            <small>{{ t('dailyScheduleLabel') }}：{{ dailyDetail.scheduleLabel || '-' }}</small>
            <small>{{ t('status') }}：{{ translateDailyStatus(dailyDetail.status) }}</small>
            <small>{{ t('dailyActualWorkMinutes') }}：{{ dailyDetail.actualWorkMinutes || 0 }}</small>
            <small>{{ t('dailyLateMinutes') }}：{{ dailyDetail.lateMinutes || 0 }}</small>
            <small>{{ t('dailyEarlyLeaveMinutes') }}：{{ dailyDetail.earlyLeaveMinutes || 0 }}</small>
          </div>
        </section>

        <section class="selattendance-schedule-side-block" v-if="dailyDetail">
          <h3>{{ t('dailyScheduleSnapshot') }}</h3>
          <div class="selattendance-punch-detail-grid">
            <small>{{ t('dailyScheduleLabel') }}：{{ dailyDetail.schedule?.label || '-' }}</small>
            <small>{{ t('dailyActualClockIn') }}：{{ formatDateTime(dailyDetail.schedule?.startTime) }}</small>
            <small>{{ t('dailyActualClockOut') }}：{{ formatDateTime(dailyDetail.schedule?.endTime) }}</small>
            <small>{{ t('breakMinutes') }}：{{ dailyDetail.schedule?.breakMinutes ?? '-' }}</small>
          </div>
        </section>

        <section class="selattendance-schedule-side-block" v-if="dailyDetail">
          <h3>{{ t('dailyPunchSnapshot') }}</h3>
          <div v-if="dailyDetail.punches?.length" class="selattendance-daily-list">
            <div v-for="item in dailyDetail.punches" :key="item.id" class="selattendance-daily-list-item">
              <strong>{{ item.punchType }}</strong>
              <small>{{ formatDateTime(item.punchTime) }} / {{ item.sourceSystem }} / {{ item.deviceName || '-' }}</small>
            </div>
          </div>
          <p v-else class="selattendance-daily-empty">{{ t('dailyNoPunches') }}</p>
        </section>

        <section class="selattendance-schedule-side-block" v-if="dailyDetail">
          <h3>{{ t('dailyExceptionList') }}</h3>
          <div v-if="dailyDetail.exceptions?.length" class="selattendance-daily-list">
            <div v-for="(item, index) in dailyDetail.exceptions" :key="`${item.exceptionType}-${index}`" class="selattendance-daily-list-item">
              <strong>{{ item.exceptionType }} / {{ translateExceptionLevel(item.exceptionLevel) }}</strong>
              <small>{{ item.message }}</small>
              <small>{{ item.suggestedAction }}</small>
            </div>
          </div>
          <p v-else class="selattendance-daily-empty">{{ t('dailyNoExceptions') }}</p>
        </section>

        <section class="selattendance-schedule-side-block" v-if="dailyDetail">
          <h3>{{ t('dailyCalcSteps') }}</h3>
          <div v-if="dailyDetail.calcSteps?.length" class="selattendance-daily-list">
            <div v-for="(item, index) in dailyDetail.calcSteps" :key="`${item.stepName}-${index}`" class="selattendance-daily-list-item">
              <strong>{{ item.stepName }}</strong>
              <small>{{ item.stepMessage }}</small>
            </div>
          </div>
          <p v-else class="selattendance-daily-empty">{{ t('dailyNoCalcSteps') }}</p>
        </section>

        <section v-if="!dailyDetail" class="selattendance-schedule-side-block">
          <h3>{{ t('dailyDetailTitle') }}</h3>
          <p class="seladmin-copy">{{ t('dailyNoSelection') }}</p>
        </section>
      </article>
    </template>
  </ResizableWorkbenchSplit>
</template>
