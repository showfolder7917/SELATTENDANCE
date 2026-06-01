<script setup>
import { computed } from 'vue'
import SharedDataTable from '../../../shared/components/SharedDataTable.vue'
import SharedPaginationBar from '../../../shared/components/SharedPaginationBar.vue'
import ThreePaneWorkbenchLayout from '../../../shared/components/ThreePaneWorkbenchLayout.vue'
import { attendanceCaseLayoutPreset } from '../../../shared/constants/workbenchLayoutConfig'

const props = defineProps({
  visible: { type: Boolean, required: true },
  workplaces: { type: Array, required: true },
  departments: { type: Array, required: true },
  caseList: { type: Object, required: true },
  caseFilters: { type: Object, required: true },
  caseDetail: { type: Object, default: null },
  caseFocusItem: { type: Object, default: null },
  caseCreateForm: { type: Object, required: true },
  caseActionForm: { type: Object, required: true },
  t: { type: Function, required: true },
  onRefresh: { type: Function, required: true },
  onSelectCase: { type: Function, required: true },
  onSubmitCreate: { type: Function, required: true },
  onSubmitAction: { type: Function, required: true },
  onLock: { type: Function, required: true },
  onUnlock: { type: Function, required: true },
  onShowToast: { type: Function, required: true }
})

// 固定每页条数档位，保证第五阶段前端分页值与后端数据库分页口径完全一致。
const pageSizeOptions = [20, 50, 100, 200]

// 当前页优先使用后端回传页码，避免前端自己换算导致分页状态漂移。
const currentPage = computed(() => Number(props.caseList.page || props.caseFilters.page || 1))

// 总页数优先使用后端回传值，没有时再按总数和每页条数做兜底换算。
const totalPages = computed(() => {
  if (props.caseList.totalPages) {
    return Number(props.caseList.totalPages)
  }
  const pageSize = Number(props.caseFilters.pageSize || 20)
  const total = Number(props.caseList.total || 0)
  return Math.max(1, Math.ceil(total / pageSize))
})

// 第五阶段列表列顺序聚焦“这条异常卡在哪一步、谁在处理、最后改到哪里”。
const caseColumns = computed(() => [
  { key: 'employeeName', label: props.t('employeeName'), minWidth: '160px' },
  { key: 'workDate', label: props.t('dailyWorkDate'), minWidth: '118px' },
  { key: 'currentException', label: props.t('caseCurrentException'), minWidth: '150px' },
  { key: 'caseStatus', label: props.t('caseStatusLabel'), minWidth: '138px' },
  { key: 'applicantRole', label: props.t('caseApplicant'), minWidth: '110px' },
  { key: 'updatedAt', label: props.t('caseUpdatedAt'), minWidth: '168px' }
])

// 把处理单状态码翻译成当前语言，方便列表和详情统一显示。
function translateCaseStatus(status) {
  const statusMap = {
    UNHANDLED: 'caseStatusUnhandled',
    SUBMITTED: 'caseStatusSubmitted',
    RETURNED: 'caseStatusReturned',
    APPROVED: 'caseStatusApproved',
    REJECTED: 'caseStatusRejected',
    LOCKED: 'caseStatusLocked'
  }
  return props.t(statusMap[status] || 'status')
}

// 把日期时间统一渲染成人眼易读格式，避免详情区出现原始 ISO 串。
function formatDateTime(value) {
  return value?.replace?.('T', ' ') || value || '-'
}

// 当前选中项如果还没建单，就回退到列表里的伪行数据供右侧建单区展示。
const currentCaseSubject = computed(() => props.caseDetail || props.caseFocusItem)

// 当前已建单详情是否允许继续审批动作，由后端回传的可用动作决定。
const canApprove = computed(() => props.caseDetail?.availableActions?.includes('APPROVE'))
const canReturn = computed(() => props.caseDetail?.availableActions?.includes('RETURN'))
const canReject = computed(() => props.caseDetail?.availableActions?.includes('REJECT'))

// 锁定按钮只有处理单已通过且当前还未锁定时才展示。
const canLock = computed(() =>
  props.caseDetail?.caseStatus === 'APPROVED' && !props.caseDetail?.lockedFlag
)

// 解锁按钮只有已经锁定的日次才展示，避免无效点击。
const canUnlock = computed(() => Boolean(props.caseDetail?.lockedFlag))

function handlePageSizeChange(nextPageSize) {
  props.caseFilters.pageSize = Number(nextPageSize)
  props.caseFilters.page = 1
}

function goToPage(page) {
  if (page === currentPage.value) return
  props.caseFilters.page = page
}
</script>

<template>
  <ThreePaneWorkbenchLayout
    v-show="visible"
    class="selattendance-case-split"
    outer-storage-key="attendance-case-split"
    :outer-default-left-percent="attendanceCaseLayoutPreset.outerDefaultLeftPercent"
    :outer-min-left-percent="attendanceCaseLayoutPreset.outerMinLeftPercent"
    :outer-max-left-percent="attendanceCaseLayoutPreset.outerMaxLeftPercent"
  >
    <template #left>
      <article class="seladmin-panel seladmin-surface selattendance-data-panel selattendance-case-list-panel">
        <!-- 异常处理参照月次汇总，把刷新动作放回列表区，保持按钮和表格在同一业务区块。 -->
        <div class="seladmin-panel-header">
          <div class="selattendance-punch-actions">
            <button class="seladmin-button seladmin-button-secondary" type="button" @click="onRefresh()">{{ t('caseRefresh') }}</button>
          </div>
        </div>

        <SharedDataTable
          class="selattendance-punch-list-shell"
          variant="list"
          sticky-header
          clickable-rows
          :show-pagination="(caseList.total || 0) > 0"
          :columns="caseColumns"
          :rows="caseList.items || []"
          row-key="attendanceDailyId"
          :active-row-key="currentCaseSubject?.attendanceDailyId || null"
          min-table-width="900px"
          :empty-text="t('emptyData')"
          @row-click="onSelectCase"
        >
          <template #cell-employeeName="{ row }">
            <strong>{{ row.employeeName }}</strong>
            <small>{{ row.employeeNo }} / {{ row.departmentName }}</small>
          </template>
          <template #cell-currentException="{ row }">
            {{ row.currentException || '-' }}
          </template>
          <template #cell-caseStatus="{ row }">
            <span class="selattendance-punch-status" :class="`status-${(row.caseStatus || '').toLowerCase()}`">
              {{ translateCaseStatus(row.caseStatus) }}
            </span>
          </template>
          <template #cell-applicantRole="{ row }">
            {{ row.applicantRole || t('caseApplicantSystem') }}
          </template>
          <template #cell-updatedAt="{ row }">
            {{ formatDateTime(row.updatedAt) }}
          </template>
          <template #pagination>
            <SharedPaginationBar
              :page-size="Number(caseFilters.pageSize || 20)"
              :page-size-options="pageSizeOptions"
              :current-page="currentPage"
              :total-pages="totalPages"
              :total-count="Number(caseList.total || 0)"
              :page-size-label="t('dailyPageSize')"
              :prev-label="t('dailyPrevPage')"
              :next-label="t('dailyNextPage')"
              :last-label="t('dailyLastPage')"
              :jump-prefix="t('dailyJumpTo')"
              :jump-suffix="t('dailyPageUnit')"
              :jump-action-label="t('dailyJumpAction')"
              :total-text="t('dailyTotalCount').replace('{total}', String(caseList.total || 0))"
              :invalid-message="t('casePageInvalid')"
              :out-of-range-message="t('casePageOutOfRange')"
              @page-change="goToPage"
              @page-size-change="handlePageSizeChange"
              @invalid="onShowToast"
            />
          </template>
        </SharedDataTable>
      </article>
    </template>

    <template #main>
      <article class="seladmin-panel seladmin-surface selattendance-punch-side selattendance-case-detail-panel">
        <template v-if="currentCaseSubject">
          <div class="seladmin-panel-header">
            <div>
              <h2>{{ caseDetail ? t('caseDetailTitle') : t('caseCreateTitle') }}</h2>
              <p class="seladmin-copy">
                {{ caseDetail ? t('caseDetailLead') : t('caseCreateLead') }}
              </p>
            </div>
          </div>

          <section class="selattendance-punch-preview">
            <strong>{{ currentCaseSubject.employeeName }} / {{ currentCaseSubject.employeeNo }}</strong>
            <small>{{ currentCaseSubject.workDate }}</small>
            <small>{{ t('caseCurrentException') }}：{{ currentCaseSubject.currentException || '-' }}</small>
            <small>{{ t('caseStatusLabel') }}：{{ translateCaseStatus(currentCaseSubject.caseStatus || 'UNHANDLED') }}</small>
          </section>

          <template v-if="!caseDetail">
            <label class="seladmin-field">
              <span>{{ t('caseReasonCategory') }}</span>
              <select v-model="caseCreateForm.reasonCategory">
                <option value="DEVICE_ERROR">{{ t('caseReasonDeviceError') }}</option>
                <option value="MANUAL_CONFIRM">{{ t('caseReasonManualConfirm') }}</option>
                <option value="OTHER">{{ t('caseReasonOther') }}</option>
              </select>
            </label>
            <label class="seladmin-field">
              <span>{{ t('caseReasonText') }}</span>
              <textarea v-model="caseCreateForm.reasonText" rows="4" />
            </label>
            <label class="seladmin-field">
              <span>{{ t('caseExpectedResolution') }}</span>
              <textarea v-model="caseCreateForm.expectedResolution" rows="4" />
            </label>
            <button class="seladmin-button seladmin-button-primary" type="button" @click="onSubmitCreate()">
              {{ t('caseCreateAction') }}
            </button>
          </template>

          <template v-else>
            <section class="selattendance-punch-preview">
              <strong>{{ t('caseTimelineTitle') }}</strong>
              <div class="selattendance-daily-list">
                <div v-for="item in caseDetail.actionLogs || []" :key="item.id" class="selattendance-daily-list-item">
                  <strong>{{ item.actionType }}</strong>
                  <small>{{ formatDateTime(item.createdAt) }}</small>
                  <small>{{ item.actionComment || '-' }}</small>
                </div>
              </div>
            </section>

            <section class="selattendance-punch-preview">
              <strong>{{ t('caseApprovalTitle') }}</strong>
              <label class="seladmin-field">
                <span>{{ t('dailyStatusFinal') }}</span>
                <select v-model="caseActionForm.finalStatus">
                  <option value="NORMAL">{{ t('dailyStatusNormal') }}</option>
                  <option value="LATE">{{ t('dailyStatusLate') }}</option>
                  <option value="EARLY_LEAVE">{{ t('dailyStatusEarlyLeave') }}</option>
                  <option value="MISSING_CLOCK_IN">{{ t('dailyStatusMissingClockIn') }}</option>
                  <option value="MISSING_CLOCK_OUT">{{ t('dailyStatusMissingClockOut') }}</option>
                  <option value="ABSENCE">{{ t('dailyStatusAbsence') }}</option>
                </select>
              </label>
              <label class="seladmin-field">
                <span>{{ t('dailyActualClockIn') }}</span>
                <input v-model="caseActionForm.finalClockIn" type="datetime-local" />
              </label>
              <label class="seladmin-field">
                <span>{{ t('dailyActualClockOut') }}</span>
                <input v-model="caseActionForm.finalClockOut" type="datetime-local" />
              </label>
              <label class="seladmin-field">
                <span>{{ t('breakMinutes') }}</span>
                <input v-model="caseActionForm.finalBreakMinutes" inputmode="numeric" />
              </label>
              <label class="selattendance-daily-checkbox">
                <input v-model="caseActionForm.finalExceptionFlag" type="checkbox" />
                <span>{{ t('caseKeepExceptionFlag') }}</span>
              </label>
              <label class="seladmin-field">
                <span>{{ t('caseApprovalComment') }}</span>
                <textarea v-model="caseActionForm.comment" rows="4" />
              </label>
              <div class="selattendance-punch-actions">
                <button v-if="canApprove" class="seladmin-button seladmin-button-primary" type="button" @click="onSubmitAction('APPROVE')">
                  {{ t('caseApproveAction') }}
                </button>
                <button v-if="canReturn" class="seladmin-button seladmin-button-secondary" type="button" @click="onSubmitAction('RETURN')">
                  {{ t('caseReturnAction') }}
                </button>
                <button v-if="canReject" class="seladmin-button seladmin-button-danger" type="button" @click="onSubmitAction('REJECT')">
                  {{ t('caseRejectAction') }}
                </button>
                <button v-if="canLock" class="seladmin-button seladmin-button-secondary" type="button" @click="onLock()">
                  {{ t('caseLockAction') }}
                </button>
                <button v-if="canUnlock" class="seladmin-button seladmin-button-secondary" type="button" @click="onUnlock()">
                  {{ t('caseUnlockAction') }}
                </button>
              </div>
            </section>
          </template>
        </template>
        <p v-else class="selattendance-daily-empty">{{ t('emptyData') }}</p>
      </article>
    </template>
  </ThreePaneWorkbenchLayout>
</template>
