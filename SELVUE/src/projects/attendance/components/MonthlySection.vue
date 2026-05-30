<script setup>
import { computed } from 'vue'
import SharedDataTable from '../../../shared/components/SharedDataTable.vue'
import SharedPaginationBar from '../../../shared/components/SharedPaginationBar.vue'
import ThreePaneWorkbenchLayout from '../../../shared/components/ThreePaneWorkbenchLayout.vue'
import { attendanceMonthlyLayoutPreset } from '../constants/workbenchLayoutPresets'

const props = defineProps({
  visible: { type: Boolean, required: true },
  workplaces: { type: Array, required: true },
  departments: { type: Array, required: true },
  monthlyList: { type: Object, required: true },
  monthlyFilters: { type: Object, required: true },
  monthlyDetail: { type: Object, default: null },
  monthlyActionForm: { type: Object, required: true },
  t: { type: Function, required: true },
  onRefresh: { type: Function, required: true },
  onSelectMonthly: { type: Function, required: true },
  onRecalculateMonthly: { type: Function, required: true },
  onRecalculateOne: { type: Function, required: true },
  onCloseMonthly: { type: Function, required: true },
  onReopenMonthly: { type: Function, required: true },
  onExportMonthly: { type: Function, required: true },
  onShowToast: { type: Function, required: true }
})

// 固定每页条数档位，保证第六阶段前端分页值与后端数据库分页口径完全一致。
const pageSizeOptions = [20, 50, 100, 200]

// 当前页优先使用后端回传页码，避免前端自己换算导致分页状态漂移。
const currentPage = computed(() => Number(props.monthlyList.page || props.monthlyFilters.page || 1))

// 总页数优先使用后端回传值，没有时再按总数和每页条数做兜底换算。
const totalPages = computed(() => {
  if (props.monthlyList.totalPages) {
    return Number(props.monthlyList.totalPages)
  }
  const pageSize = Number(props.monthlyFilters.pageSize || 20)
  const total = Number(props.monthlyList.total || 0)
  return Math.max(1, Math.ceil(total / pageSize))
})

// 第六阶段列表列顺序聚焦“这个月能不能结、异常卡在哪里、统计结果是什么”。
const monthlyColumns = computed(() => [
  { key: 'employeeName', label: props.t('employeeName'), minWidth: '170px' },
  { key: 'yearMonth', label: props.t('monthlyYearMonth'), minWidth: '108px' },
  { key: 'scheduledDays', label: props.t('monthlyScheduledDays'), minWidth: '108px', align: 'center' },
  { key: 'attendanceDays', label: props.t('monthlyAttendanceDays'), minWidth: '108px', align: 'center' },
  { key: 'exceptionDays', label: props.t('monthlyExceptionDays'), minWidth: '108px', align: 'center' },
  { key: 'closeStatus', label: props.t('monthlyCloseStatus'), minWidth: '130px' },
  { key: 'updatedAt', label: props.t('monthlyUpdatedAt'), minWidth: '168px' }
])

// 把月结状态码翻译成当前语言，方便列表和详情统一显示。
function translateCloseStatus(status) {
  const statusMap = {
    OPEN: 'monthlyCloseStatusOpen',
    CLOSABLE: 'monthlyCloseStatusClosable',
    CLOSED: 'monthlyCloseStatusClosed',
    REOPENED: 'monthlyCloseStatusReopened'
  }
  return props.t(statusMap[status] || 'status')
}

// 把日期时间统一渲染成人眼易读格式，避免详情区出现原始 ISO 串。
function formatDateTime(value) {
  return value?.replace?.('T', ' ') || value || '-'
}

// 当前选中月次是否允许月结，由 closeStatus 和阻塞原因数量共同决定。
const canClose = computed(() =>
  props.monthlyDetail?.closeStatus === 'CLOSABLE' || props.monthlyDetail?.closeStatus === 'REOPENED'
)

// 当前选中月次只有已结状态才允许反结，避免无效点击。
const canReopen = computed(() => props.monthlyDetail?.closeStatus === 'CLOSED')

function handlePageSizeChange(nextPageSize) {
  props.monthlyFilters.pageSize = Number(nextPageSize)
  props.monthlyFilters.page = 1
}

function goToPage(page) {
  if (page === currentPage.value) return
  props.monthlyFilters.page = page
}
</script>

<template>
  <ThreePaneWorkbenchLayout
    v-show="visible"
    class="selattendance-monthly-split"
    outer-storage-key="attendance-monthly-split"
    :outer-default-left-percent="attendanceMonthlyLayoutPreset.outerDefaultLeftPercent"
    :outer-min-left-percent="attendanceMonthlyLayoutPreset.outerMinLeftPercent"
    :outer-max-left-percent="attendanceMonthlyLayoutPreset.outerMaxLeftPercent"
  >
    <template #left>
      <article class="seladmin-panel seladmin-surface selattendance-data-panel selattendance-monthly-list-panel">
        <div class="seladmin-panel-header">
          <div>
            <h2>{{ t('monthlyTitle') }}</h2>
            <p class="seladmin-copy">{{ t('monthlyLead') }}</p>
          </div>
          <div class="selattendance-punch-actions">
            <button class="seladmin-button seladmin-button-secondary" type="button" @click="onRefresh()">{{ t('monthlyRefresh') }}</button>
            <button class="seladmin-button seladmin-button-secondary" type="button" @click="onExportMonthly()">{{ t('monthlyExport') }}</button>
            <button class="seladmin-button seladmin-button-primary" type="button" @click="onRecalculateMonthly()">{{ t('monthlyRecalculateRange') }}</button>
          </div>
        </div>

        <div class="selattendance-punch-summary">
          <div class="selattendance-punch-summary-card">
            <strong>{{ monthlyList.summary?.openCount || 0 }}</strong>
            <small>{{ t('monthlySummaryOpen') }}</small>
          </div>
          <div class="selattendance-punch-summary-card warm">
            <strong>{{ monthlyList.summary?.closableCount || 0 }}</strong>
            <small>{{ t('monthlySummaryClosable') }}</small>
          </div>
          <div class="selattendance-punch-summary-card">
            <strong>{{ monthlyList.summary?.closedCount || 0 }}</strong>
            <small>{{ t('monthlySummaryClosed') }}</small>
          </div>
          <div class="selattendance-punch-summary-card muted">
            <strong>{{ monthlyList.summary?.reopenedCount || 0 }}</strong>
            <small>{{ t('monthlySummaryReopened') }}</small>
          </div>
        </div>

        <div class="selattendance-schedule-toolbar">
          <label class="seladmin-field">
            <span>{{ t('monthlyYearMonth') }}</span>
            <input v-model="monthlyFilters.yearMonth" type="month" />
          </label>
          <label class="seladmin-field">
            <span>{{ t('workplace') }}</span>
            <select v-model="monthlyFilters.workplaceId">
              <option value="">{{ t('allWorkplaces') }}</option>
              <option v-for="item in workplaces" :key="item.id" :value="item.id">{{ item.workplaceName }}</option>
            </select>
          </label>
          <label class="seladmin-field">
            <span>{{ t('departmentName') }}</span>
            <select v-model="monthlyFilters.departmentId">
              <option value="">{{ t('allDepartments') }}</option>
              <option v-for="item in departments" :key="item.id" :value="item.id">{{ item.departmentName }}</option>
            </select>
          </label>
          <label class="seladmin-field">
            <span>{{ t('employeeName') }}</span>
            <input v-model="monthlyFilters.employeeKeyword" :placeholder="t('monthlyEmployeeKeywordHint')" />
          </label>
          <label class="seladmin-field">
            <span>{{ t('monthlyCloseStatus') }}</span>
            <select v-model="monthlyFilters.closeStatus">
              <option value="">{{ t('monthlyCloseStatusAll') }}</option>
              <option value="OPEN">{{ t('monthlyCloseStatusOpen') }}</option>
              <option value="CLOSABLE">{{ t('monthlyCloseStatusClosable') }}</option>
              <option value="CLOSED">{{ t('monthlyCloseStatusClosed') }}</option>
              <option value="REOPENED">{{ t('monthlyCloseStatusReopened') }}</option>
            </select>
          </label>
          <label class="selattendance-daily-checkbox">
            <input v-model="monthlyFilters.blockedOnly" type="checkbox" />
            <span>{{ t('monthlyBlockedOnly') }}</span>
          </label>
        </div>

        <SharedDataTable
          class="selattendance-punch-list-shell"
          variant="list"
          sticky-header
          clickable-rows
          :show-pagination="(monthlyList.total || 0) > 0"
          :columns="monthlyColumns"
          :rows="monthlyList.items || []"
          row-key="monthlyId"
          :active-row-key="monthlyDetail?.monthlyId || null"
          min-table-width="940px"
          :empty-text="t('emptyData')"
          @row-click="onSelectMonthly"
        >
          <template #cell-employeeName="{ row }">
            <strong>{{ row.employeeName }}</strong>
            <small>{{ row.employeeCode }} / {{ row.departmentName }}</small>
          </template>
          <template #cell-closeStatus="{ row }">
            <span class="selattendance-punch-status" :class="`status-${(row.closeStatus || '').toLowerCase()}`">
              {{ translateCloseStatus(row.closeStatus) }}
            </span>
          </template>
          <template #cell-updatedAt="{ row }">
            {{ formatDateTime(row.updatedAt) }}
          </template>
          <template #pagination>
            <SharedPaginationBar
              :page-size="Number(monthlyFilters.pageSize || 20)"
              :page-size-options="pageSizeOptions"
              :current-page="currentPage"
              :total-pages="totalPages"
              :total-count="Number(monthlyList.total || 0)"
              :page-size-label="t('dailyPageSize')"
              :prev-label="t('dailyPrevPage')"
              :next-label="t('dailyNextPage')"
              :last-label="t('dailyLastPage')"
              :jump-prefix="t('dailyJumpTo')"
              :jump-suffix="t('dailyPageUnit')"
              :jump-action-label="t('dailyJumpAction')"
              :total-text="t('dailyTotalCount').replace('{total}', String(monthlyList.total || 0))"
              :invalid-message="t('monthlyPageInvalid')"
              :out-of-range-message="t('monthlyPageOutOfRange')"
              @page-change="goToPage"
              @page-size-change="handlePageSizeChange"
              @invalid="onShowToast"
            />
          </template>
        </SharedDataTable>
      </article>
    </template>

    <template #main>
      <article class="seladmin-panel seladmin-surface selattendance-punch-side selattendance-monthly-detail-panel">
        <template v-if="monthlyDetail">
          <div class="seladmin-panel-header">
            <div>
              <h2>{{ t('monthlyDetailTitle') }}</h2>
              <p class="seladmin-copy">{{ t('monthlyDetailLead').replace('{month}', String(monthlyDetail.yearMonth || '-')) }}</p>
            </div>
            <div class="selattendance-punch-actions">
              <button class="seladmin-button seladmin-button-secondary" type="button" @click="onRecalculateOne()">{{ t('monthlyRecalculateOne') }}</button>
              <button v-if="canClose" class="seladmin-button seladmin-button-primary" type="button" @click="onCloseMonthly()">{{ t('monthlyCloseAction') }}</button>
              <button v-if="canReopen" class="seladmin-button seladmin-button-secondary" type="button" @click="onReopenMonthly()">{{ t('monthlyReopenAction') }}</button>
            </div>
          </div>

          <section class="selattendance-punch-preview">
            <strong>{{ monthlyDetail.employeeName }} / {{ monthlyDetail.employeeCode }}</strong>
            <small>{{ monthlyDetail.workplaceName }} / {{ monthlyDetail.departmentName }}</small>
            <small>{{ t('monthlyCloseStatus') }}：{{ translateCloseStatus(monthlyDetail.closeStatus) }}</small>
            <small>{{ t('monthlyBlockReasonCount') }}：{{ monthlyDetail.blockReasonCount || 0 }}</small>
          </section>

          <section class="selattendance-schedule-side-block">
            <h3>{{ t('monthlyMetricTitle') }}</h3>
            <div class="selattendance-punch-detail-grid">
              <small>{{ t('monthlyScheduledDays') }}：{{ monthlyDetail.scheduledDays || 0 }}</small>
              <small>{{ t('monthlyAttendanceDays') }}：{{ monthlyDetail.attendanceDays || 0 }}</small>
              <small>{{ t('monthlyNormalDays') }}：{{ monthlyDetail.normalDays || 0 }}</small>
              <small>{{ t('monthlyLateCount') }}：{{ monthlyDetail.lateCount || 0 }}</small>
              <small>{{ t('monthlyEarlyLeaveCount') }}：{{ monthlyDetail.earlyLeaveCount || 0 }}</small>
              <small>{{ t('monthlyMissingPunchCount') }}：{{ monthlyDetail.missingPunchCount || 0 }}</small>
              <small>{{ t('monthlyAbsenceCount') }}：{{ monthlyDetail.absenceCount || 0 }}</small>
              <small>{{ t('monthlyExceptionDays') }}：{{ monthlyDetail.exceptionDays || 0 }}</small>
              <small>{{ t('monthlyPaidLeaveDays') }}：{{ monthlyDetail.paidLeaveDays || 0 }}</small>
              <small>{{ t('monthlyRestDays') }}：{{ monthlyDetail.restDays || 0 }}</small>
            </div>
          </section>

          <section class="selattendance-schedule-side-block">
            <h3>{{ t('monthlyBlockTitle') }}</h3>
            <div v-if="monthlyDetail.blockReasons?.length" class="selattendance-punch-detail-grid">
              <small v-for="(item, index) in monthlyDetail.blockReasons" :key="`${item.blockCode}-${item.workDate}-${index}`">
                {{ item.workDate || '-' }} / {{ item.blockMessage }}
              </small>
            </div>
            <p v-else class="seladmin-copy">{{ t('monthlyNoBlock') }}</p>
          </section>

          <section class="selattendance-schedule-side-block">
            <h3>{{ t('monthlyActionLogTitle') }}</h3>
            <div v-if="monthlyDetail.actionLogs?.length" class="selattendance-punch-detail-grid">
              <small v-for="(item, index) in monthlyDetail.actionLogs" :key="`${item.actionType}-${index}`">
                {{ formatDateTime(item.createdAt) }} / {{ item.actionType }} / {{ item.actionComment || '-' }}
              </small>
            </div>
            <p v-else class="seladmin-copy">{{ t('monthlyNoActionLog') }}</p>
          </section>

          <section class="selattendance-schedule-side-block">
            <h3>{{ t('monthlyActionPanelTitle') }}</h3>
            <label class="seladmin-field">
              <span>{{ t('monthlyCloseComment') }}</span>
              <textarea v-model="monthlyActionForm.comment" rows="3" />
            </label>
            <label class="seladmin-field">
              <span>{{ t('monthlyReopenReason') }}</span>
              <textarea v-model="monthlyActionForm.reopenReason" rows="3" />
            </label>
          </section>
        </template>
      </article>
    </template>
  </ThreePaneWorkbenchLayout>
</template>
