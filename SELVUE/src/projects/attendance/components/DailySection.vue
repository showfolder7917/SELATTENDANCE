<script setup>
import { computed, ref, watch } from 'vue'
import SharedDataTable from '../../../shared/components/SharedDataTable.vue'
import SharedMiniSelect from '../../../shared/components/SharedMiniSelect.vue'
import ThreePaneWorkbenchLayout from '../../../shared/components/ThreePaneWorkbenchLayout.vue'
import { attendanceDailyLayoutPreset } from '../constants/workbenchLayoutPresets'

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
  onRecalculateRange: { type: Function, required: true },
  onShowToast: { type: Function, required: true }
})

// 固定每页条数档位，保证第四阶段前端分页值与后端数据库分页口径完全一致。
const pageSizeOptions = [20, 50, 100, 200]

// 跳页输入框单独保留本地值，允许用户先编辑再点击加载。
const pageJumpInput = ref('')

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

// 最后一页按钮在已经位于最后一页时禁用，避免重复请求同一页。
const isLastDisabled = computed(() => currentPage.value >= totalPages.value)

// 生成底部分页条要显示的页码片段，兼顾首页、当前页附近和最后一页。
const pageItems = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  if (total <= 7) {
    return Array.from({ length: total }, (_, index) => ({ type: 'page', value: index + 1 }))
  }

  const items = [{ type: 'page', value: 1 }]
  let start = Math.max(2, current - 1)
  let end = Math.min(total - 1, current + 1)

  // 当前页靠前时多显示前几页，减少过早出现的省略号。
  if (current <= 4) {
    start = 2
    end = 5
  }

  // 当前页接近末尾时多显示最后几页，让尾部翻页更直观。
  if (current >= total - 3) {
    start = total - 4
    end = total - 1
  }

  if (start > 2) {
    items.push({ type: 'ellipsis', key: 'leading' })
  }

  for (let page = start; page <= end; page += 1) {
    items.push({ type: 'page', value: page })
  }

  if (end < total - 1) {
    items.push({ type: 'ellipsis', key: 'trailing' })
  }

  items.push({ type: 'page', value: total })
  return items
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

// 当前页变化后同步输入框，让用户看到的跳页值始终跟当前实际页一致。
watch(
  () => currentPage.value,
  (nextPage) => {
    pageJumpInput.value = String(nextPage)
  },
  { immediate: true }
)

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

// 点击最后一页时直接跳到总页数，方便长列表快速收尾定位。
function goLastPage() {
  if (isLastDisabled.value) return
  props.dailyFilters.page = totalPages.value
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

// 点击加载时校验输入页码是否合法，错误时用页面级 toast 直接提示用户。
function submitPageJump() {
  const rawValue = String(pageJumpInput.value || '').trim()
  if (!/^\d+$/.test(rawValue)) {
    props.onShowToast(
      props.t('dailyPageInvalid').replace('{totalPages}', String(totalPages.value))
    )
    return
  }

  const targetPage = Number(rawValue)
  if (targetPage < 1 || targetPage > totalPages.value) {
    props.onShowToast(
      props.t('dailyPageOutOfRange').replace('{totalPages}', String(totalPages.value))
    )
    return
  }

  goToPage(targetPage)
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

        <SharedDataTable
          class="selattendance-punch-list-shell"
          variant="list"
          sticky-header
          clickable-rows
          :show-pagination="totalPages > 1"
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
            <div class="selattendance-punch-pagination">
              <label class="selattendance-punch-pagination-size">
                <span>{{ t('dailyPageSize') }}</span>
                <SharedMiniSelect
                  :model-value="String(dailyFilters.pageSize || 20)"
                  :options="pageSizeOptions.map((size) => ({ value: String(size), label: String(size) }))"
                  :aria-label="t('dailyPageSize')"
                  @change="handlePageSizeChange"
                />
              </label>
              <div class="selattendance-punch-pagination-pages" :aria-label="t('dailyPaginationNav')">
                <button class="seladmin-button seladmin-button-secondary" type="button" :disabled="isPrevDisabled" @click="goPrevPage()">{{ t('dailyPrevPage') }}</button>
                <template v-for="item in pageItems" :key="item.type === 'page' ? `page-${item.value}` : item.key">
                  <button
                    v-if="item.type === 'page'"
                    class="selattendance-pagination-number"
                    :class="{ active: item.value === currentPage }"
                    type="button"
                    @click="goToPage(item.value)"
                  >
                    {{ item.value }}
                  </button>
                  <span v-else class="selattendance-pagination-ellipsis">...</span>
                </template>
                <button class="selattendance-pagination-tail" type="button" :disabled="isLastDisabled" @click="goLastPage()">{{ t('dailyLastPage') }}</button>
                <button class="seladmin-button seladmin-button-secondary" type="button" :disabled="isNextDisabled" @click="goNextPage()">{{ t('dailyNextPage') }}</button>
              </div>
              <div class="selattendance-punch-pagination-jump">
                <label class="selattendance-punch-pagination-jump-field">
                  <span>{{ t('dailyPageJumpPrefix') }}</span>
                  <input
                    v-model="pageJumpInput"
                    inputmode="numeric"
                    :placeholder="t('dailyPageJumpPlaceholder').replace('{totalPages}', String(totalPages))"
                    @keydown.enter.prevent="submitPageJump()"
                  />
                  <span>{{ t('dailyPageJumpSuffix') }}</span>
                </label>
                <button class="seladmin-button seladmin-button-primary" type="button" @click="submitPageJump()">{{ t('dailyPageJumpSubmit') }}</button>
              </div>
              <div class="selattendance-punch-pagination-meta">
                <small>{{ t('dailyPaginationSummary').replace('{total}', String(dailyList.total || 0)) }}</small>
              </div>
            </div>
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
  </ThreePaneWorkbenchLayout>
</template>
