<script setup>
import { computed } from 'vue'
import SharedDataTable from '../../../shared/components/SharedDataTable.vue'
import SharedPaginationBar from '../../../shared/components/SharedPaginationBar.vue'
import ThreePaneWorkbenchLayout from '../../../shared/components/ThreePaneWorkbenchLayout.vue'
import { attendanceDailyLayoutPreset } from '../../../shared/constants/workbenchLayoutConfig'

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
  onRecalculateRange: { type: Function, required: true },
  onRecalculateDaily: { type: Function, required: true },
  onShowToast: { type: Function, required: true }
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

// 日次结果页继续保留当前列顺序和状态翻译，只把表格外壳收口到共享组件。
const dailyColumns = computed(() => [
  { key: 'employeeName', label: props.t('employeeName'), minWidth: '160px' },
  { key: 'workDate', label: props.t('dailyWorkDate'), minWidth: '118px' },
  { key: 'scheduleLabel', label: props.t('dailyScheduleLabel'), wrap: true, minWidth: '220px' },
  { key: 'actualClockIn', label: props.t('dailyActualClockIn'), minWidth: '148px' },
  { key: 'actualClockOut', label: props.t('dailyActualClockOut'), minWidth: '148px' },
  { key: 'status', label: props.t('status'), minWidth: '120px' }
])

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

// 日次增强字段继续按分钟展示，避免前端再推导业务时长导致口径漂移。
function formatMinutes(value) {
  return Number.isFinite(Number(value)) ? Number(value) : 0
}

// 休日类型转成人话，供第七阶段管理员直接区分法定休日和所定休日。
function translateHolidayType(holidayType) {
  if (holidayType === 'LEGAL_HOLIDAY') return props.t('dailyHolidayTypeLegal')
  if (holidayType === 'SCHEDULED_HOLIDAY') return props.t('dailyHolidayTypeScheduled')
  return props.t('dailyHolidayTypeWorkday')
}

// 切换每页条数时同步回到第 1 页，避免旧页码在新页大小下跳空。
function handlePageSizeChange(nextPageSize) {
  props.dailyFilters.pageSize = Number(nextPageSize)
  props.dailyFilters.page = 1
}

// 直接点击页码时只切到目标页，避免重复请求当前页。
function goToPage(page) {
  if (page === currentPage.value) return
  props.dailyFilters.page = page
}
</script>

<template>
  <ThreePaneWorkbenchLayout
    v-show="visible"
    class="selattendance-daily-split"
    outer-storage-key="attendance-daily-split"
    :outer-default-left-percent="attendanceDailyLayoutPreset.outerDefaultLeftPercent"
    :outer-min-left-percent="attendanceDailyLayoutPreset.outerMinLeftPercent"
    :outer-max-left-percent="attendanceDailyLayoutPreset.outerMaxLeftPercent"
  >
    <template #left>
      <article class="seladmin-panel seladmin-surface selattendance-data-panel selattendance-daily-list-panel">
        <div class="seladmin-panel-header">
          <div class="selattendance-punch-actions">
            <!-- 日次列表区的刷新动作属于表格业务动作，和异常处理一样留在列表区顶部，避免污染头部概览层。 -->
            <button class="seladmin-button seladmin-button-secondary" type="button" @click="onRefresh()">{{ t('dailyRefresh') }}</button>
            <!-- 当前筛选重算依赖表格筛选结果，应和列表放在一起，便于用户先看结果再触发批量处理。 -->
            <button class="seladmin-button seladmin-button-primary" type="button" @click="onRecalculateRange()">{{ t('dailyRecalculateRange') }}</button>
          </div>
        </div>
        <SharedDataTable
          class="selattendance-punch-list-shell"
          variant="list"
          sticky-header
          clickable-rows
          :show-pagination="(dailyList.total || 0) > 0"
          :columns="dailyColumns"
          :rows="dailyList.items || []"
          row-key="id"
          :active-row-key="dailyDetail?.id || null"
          min-table-width="980px"
          :empty-text="t('emptyData')"
          @row-click="onSelectDaily"
        >
          <template #cell-employeeName="{ row }">
            <strong>{{ row.employeeName }}</strong>
            <small>{{ row.employeeNo }} / {{ row.departmentName }}</small>
          </template>
          <template #cell-scheduleLabel="{ row }">
            {{ row.scheduleLabel || '-' }}
          </template>
          <template #cell-actualClockIn="{ row }">
            {{ formatDateTime(row.actualClockIn) }}
          </template>
          <template #cell-actualClockOut="{ row }">
            {{ formatDateTime(row.actualClockOut) }}
          </template>
          <template #cell-status="{ row }">
            <span class="selattendance-punch-status" :class="`status-${(row.status || '').toLowerCase()}`">
              {{ translateDailyStatus(row.status) }}
            </span>
          </template>
          <template #pagination>
            <SharedPaginationBar
              :page-size="Number(dailyFilters.pageSize || 20)"
              :page-size-options="pageSizeOptions"
              :current-page="currentPage"
              :total-pages="totalPages"
              :total-count="Number(dailyList.total || 0)"
              :page-size-label="t('dailyPageSize')"
              :prev-label="t('dailyPrevPage')"
              :next-label="t('dailyNextPage')"
              :last-label="t('dailyLastPage')"
              :jump-prefix="t('dailyPageJumpPrefix')"
              :jump-suffix="t('dailyPageJumpSuffix')"
              :jump-action-label="t('dailyPageJumpSubmit')"
              :total-text="t('dailyPaginationSummary').replace('{total}', String(dailyList.total || 0))"
              :invalid-message="t('dailyPageInvalid')"
              :out-of-range-message="t('dailyPageOutOfRange')"
              @page-change="goToPage"
              @page-size-change="handlePageSizeChange"
              @invalid="onShowToast"
            />
          </template>
        </SharedDataTable>
      </article>
    </template>

    <template #main>
      <article class="seladmin-panel seladmin-surface selattendance-form-panel selattendance-punch-side selattendance-daily-detail-panel">
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
            <small>{{ t('dailyAppliedRule') }}：{{ dailyDetail.appliedRuleName || '-' }}</small>
            <small>{{ t('dailyHolidayType') }}：{{ translateHolidayType(dailyDetail.holidayType) }}</small>
            <small>{{ t('dailyActualWorkMinutes') }}：{{ formatMinutes(dailyDetail.actualWorkMinutes) }}</small>
            <small>{{ t('dailyNormalWorkMinutes') }}：{{ formatMinutes(dailyDetail.normalWorkMinutes) }}</small>
            <small>{{ t('dailyOvertimeMinutes') }}：{{ formatMinutes(dailyDetail.overtimeMinutes) }}</small>
            <small>{{ t('dailyLegalOvertimeMinutes') }}：{{ formatMinutes(dailyDetail.legalOvertimeMinutes) }}</small>
            <small>{{ t('dailyNightWorkMinutes') }}：{{ formatMinutes(dailyDetail.nightWorkMinutes) }}</small>
            <small>{{ t('dailyHolidayWorkMinutes') }}：{{ formatMinutes(dailyDetail.holidayWorkMinutes) }}</small>
            <small>{{ t('dailyLateMinutes') }}：{{ formatMinutes(dailyDetail.lateMinutes) }}</small>
            <small>{{ t('dailyEarlyLeaveMinutes') }}：{{ formatMinutes(dailyDetail.earlyLeaveMinutes) }}</small>
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
  </ThreePaneWorkbenchLayout>
</template>
