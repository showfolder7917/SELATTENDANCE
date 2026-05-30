<script setup>
import { computed } from 'vue'
import SharedDataTable from '../../../shared/components/SharedDataTable.vue'
import SharedPaginationBar from '../../../shared/components/SharedPaginationBar.vue'
import ThreePaneWorkbenchLayout from '../../../shared/components/ThreePaneWorkbenchLayout.vue'
import { attendancePunchLayoutPreset } from '../constants/workbenchLayoutPresets'

const props = defineProps({
  visible: { type: Boolean, required: true },
  employees: { type: Array, required: true },
  punchLogList: { type: Object, required: true },
  punchFilters: { type: Object, required: true },
  punchDetail: { type: Object, default: null },
  punchManualForm: { type: Object, required: true },
  punchImportForm: { type: Object, required: true },
  punchImportPreview: { type: Object, default: null },
  punchActionForm: { type: Object, required: true },
  t: { type: Function, required: true },
  onRefresh: { type: Function, required: true },
  onSelectLog: { type: Function, required: true },
  onSubmitManual: { type: Function, required: true },
  onPreviewImport: { type: Function, required: true },
  onSubmitImport: { type: Function, required: true },
  onBindEmployee: { type: Function, required: true },
  onIgnoreLog: { type: Function, required: true },
  onReprocessLog: { type: Function, required: true },
  onShowToast: { type: Function, required: true }
})

// 固定每页条数档位，保证前端切换值与后端数据库分页允许值完全一致。
const pageSizeOptions = [20, 50, 100, 200]

// 当前页优先使用后端回传页码，避免前端自己猜测造成分页状态漂移。
const currentPage = computed(() => Number(props.punchLogList.page || props.punchFilters.page || 1))

// 总页数优先使用后端回传值，没有时再按总数和每页条数兜底换算。
const totalPages = computed(() => {
  if (props.punchLogList.totalPages) {
    return Number(props.punchLogList.totalPages)
  }
  const pageSize = Number(props.punchFilters.pageSize || 20)
  const total = Number(props.punchLogList.total || 0)
  return Math.max(1, Math.ceil(total / pageSize))
})

// 打卡列表列定义继续留在业务页，保证状态文案、字段顺序和后续 slot 扩展都由业务决定。
const punchColumns = computed(() => [
  { key: 'employeeName', label: props.t('employeeName'), minWidth: '160px' },
  { key: 'externalEmployeeId', label: props.t('externalEmployeeId'), minWidth: '136px' },
  { key: 'punchTime', label: props.t('punchTime'), minWidth: '170px' },
  { key: 'punchType', label: props.t('punchType'), minWidth: '120px' },
  { key: 'sourceSystem', label: props.t('sourceSystem'), minWidth: '120px' },
  { key: 'processStatus', label: props.t('status'), minWidth: '116px' }
])

// 切换每页条数时同步回到第 1 页，避免旧页码在新页大小下跳空。
function handlePageSizeChange(nextPageSize) {
  props.punchFilters.pageSize = Number(nextPageSize)
  props.punchFilters.page = 1
}

// 直接点击页码时只切到目标页，避免重复请求当前页。
function goToPage(page) {
  if (page === currentPage.value) return
  props.punchFilters.page = page
}
</script>

<template>
  <ThreePaneWorkbenchLayout
    v-show="visible"
    class="selattendance-punch-split"
    outer-storage-key="attendance-punch-split"
    :outer-default-left-percent="attendancePunchLayoutPreset.outerDefaultLeftPercent"
    :outer-min-left-percent="attendancePunchLayoutPreset.outerMinLeftPercent"
    :outer-max-left-percent="attendancePunchLayoutPreset.outerMaxLeftPercent"
  >
    <template #left>
      <article class="seladmin-panel seladmin-surface selattendance-data-panel selattendance-punch-list-panel">
        <div class="seladmin-panel-header">
          <div>
            <h2>{{ t('punchTitle') }}</h2>
            <p class="seladmin-copy">{{ t('punchLead') }}</p>
          </div>
          <button class="seladmin-button seladmin-button-secondary" type="button" @click="onRefresh()">{{ t('punchRefresh') }}</button>
        </div>

        <div class="selattendance-punch-summary">
          <div class="selattendance-punch-summary-card">
            <strong>{{ punchLogList.summary?.processed || 0 }}</strong>
            <small>{{ t('punchStatusProcessed') }}</small>
          </div>
          <div class="selattendance-punch-summary-card warm">
            <strong>{{ punchLogList.summary?.unmatched || 0 }}</strong>
            <small>{{ t('punchStatusUnmatched') }}</small>
          </div>
          <div class="selattendance-punch-summary-card danger">
            <strong>{{ punchLogList.summary?.error || 0 }}</strong>
            <small>{{ t('punchStatusError') }}</small>
          </div>
          <div class="selattendance-punch-summary-card muted">
            <strong>{{ punchLogList.summary?.ignored || 0 }}</strong>
            <small>{{ t('punchStatusIgnored') }}</small>
          </div>
        </div>

        <div class="selattendance-schedule-toolbar">
          <label class="seladmin-field">
            <span>{{ t('punchDateFrom') }}</span>
            <input v-model="punchFilters.dateFrom" type="date" />
          </label>
          <label class="seladmin-field">
            <span>{{ t('punchDateTo') }}</span>
            <input v-model="punchFilters.dateTo" type="date" />
          </label>
          <label class="seladmin-field">
            <span>{{ t('employeeName') }}</span>
            <input v-model="punchFilters.employeeKeyword" :placeholder="t('punchEmployeeKeywordHint')" />
          </label>
          <label class="seladmin-field">
            <span>{{ t('sourceSystem') }}</span>
            <select v-model="punchFilters.sourceSystem">
              <option value="">{{ t('punchAllSources') }}</option>
              <option value="MANUAL">MANUAL</option>
              <option value="CSV_IMPORT">CSV_IMPORT</option>
              <option value="WEBHOOK">WEBHOOK</option>
            </select>
          </label>
          <label class="seladmin-field">
            <span>{{ t('status') }}</span>
            <select v-model="punchFilters.processStatus">
              <option value="">{{ t('punchAllStatuses') }}</option>
              <option value="PROCESSED">{{ t('punchStatusProcessed') }}</option>
              <option value="UNMATCHED">{{ t('punchStatusUnmatched') }}</option>
              <option value="ERROR">{{ t('punchStatusError') }}</option>
              <option value="DUPLICATE">{{ t('punchStatusDuplicate') }}</option>
              <option value="IGNORED">{{ t('punchStatusIgnored') }}</option>
            </select>
          </label>
        </div>

        <SharedDataTable
          class="selattendance-punch-list-shell"
          variant="list"
          sticky-header
          clickable-rows
          :show-pagination="(punchLogList.total || 0) > 0"
          :columns="punchColumns"
          :rows="punchLogList.items || []"
          row-key="id"
          :active-row-key="punchDetail?.id || null"
          min-table-width="920px"
          :empty-text="t('emptyData')"
          @row-click="onSelectLog"
        >
          <template #cell-employeeName="{ row }">
            <strong>{{ row.employeeName || t('punchUnmatchedLabel') }}</strong>
            <small>{{ row.employeeNo || row.deviceName || '-' }}</small>
          </template>
          <template #cell-externalEmployeeId="{ row }">
            {{ row.externalEmployeeId || '-' }}
          </template>
          <template #cell-punchTime="{ row }">
            {{ row.punchTime?.replace?.('T', ' ') || row.punchTime }}
          </template>
          <template #cell-processStatus="{ row }">
            <span class="selattendance-punch-status" :class="`status-${(row.processStatus || '').toLowerCase()}`">{{ row.processStatus }}</span>
          </template>
          <template #pagination>
            <SharedPaginationBar
              :page-size="Number(punchFilters.pageSize || 20)"
              :page-size-options="pageSizeOptions"
              :current-page="currentPage"
              :total-pages="totalPages"
              :total-count="Number(punchLogList.total || 0)"
              :page-size-label="t('punchPageSize')"
              :prev-label="t('punchPrevPage')"
              :next-label="t('punchNextPage')"
              :last-label="t('punchLastPage')"
              :jump-prefix="t('punchPageJumpPrefix')"
              :jump-suffix="t('punchPageJumpSuffix')"
              :jump-action-label="t('punchPageJumpSubmit')"
              :total-text="t('punchPaginationSummary').replace('{total}', String(punchLogList.total || 0))"
              :invalid-message="t('punchPageInvalid')"
              :out-of-range-message="t('punchPageOutOfRange')"
              @page-change="goToPage"
              @page-size-change="handlePageSizeChange"
              @invalid="onShowToast"
            />
          </template>
        </SharedDataTable>
      </article>
    </template>

    <template #main>
      <article class="seladmin-panel seladmin-surface selattendance-form-panel selattendance-punch-side selattendance-punch-detail-panel">
        <section class="selattendance-schedule-side-block">
          <div class="seladmin-panel-header">
            <div>
              <h3>{{ t('punchManualTitle') }}</h3>
              <p class="seladmin-copy">{{ t('punchManualHint') }}</p>
            </div>
          </div>
          <label class="seladmin-field">
            <span>{{ t('employeeName') }}</span>
            <select v-model="punchManualForm.employeeId">
              <option value="">{{ t('punchPickEmployee') }}</option>
              <option v-for="item in employees" :key="item.id" :value="item.id">{{ item.employeeNo }} / {{ item.employeeName }}</option>
            </select>
          </label>
          <label class="seladmin-field">
            <span>{{ t('punchTime') }}</span>
            <input v-model="punchManualForm.punchTime" type="datetime-local" />
          </label>
          <label class="seladmin-field">
            <span>{{ t('punchType') }}</span>
            <select v-model="punchManualForm.punchType">
              <option value="CLOCK_IN">CLOCK_IN</option>
              <option value="CLOCK_OUT">CLOCK_OUT</option>
              <option value="BREAK_START">BREAK_START</option>
              <option value="BREAK_END">BREAK_END</option>
            </select>
          </label>
          <label class="seladmin-field">
            <span>{{ t('punchDeviceName') }}</span>
            <input v-model="punchManualForm.deviceName" />
          </label>
          <label class="seladmin-field">
            <span>{{ t('scheduleRemark') }}</span>
            <textarea v-model="punchManualForm.note" rows="2" />
          </label>
          <button class="seladmin-button seladmin-button-primary" type="button" @click="onSubmitManual()">{{ t('punchManualSubmit') }}</button>
        </section>

        <section class="selattendance-schedule-side-block">
          <div class="seladmin-panel-header">
            <div>
              <h3>{{ t('punchImportTitle') }}</h3>
              <p class="seladmin-copy">{{ t('punchImportHint') }}</p>
            </div>
          </div>
          <label class="seladmin-field">
            <span>{{ t('punchImportFileName') }}</span>
            <input v-model="punchImportForm.fileName" />
          </label>
          <label class="seladmin-field">
            <span>CSV</span>
            <textarea v-model="punchImportForm.csvText" rows="7" :placeholder="t('punchImportPlaceholder')" />
          </label>
          <div class="selattendance-punch-actions">
            <button class="seladmin-button seladmin-button-secondary" type="button" @click="onPreviewImport()">{{ t('punchPreviewImport') }}</button>
            <button class="seladmin-button seladmin-button-primary" type="button" @click="onSubmitImport()">{{ t('punchSubmitImport') }}</button>
          </div>
          <div v-if="punchImportPreview?.summary" class="selattendance-punch-preview">
            <strong>{{ t('punchPreviewSummary') }}</strong>
            <small>{{ t('punchPreviewTotal').replace('{count}', String(punchImportPreview.summary.totalCount || 0)) }}</small>
            <small>{{ t('punchPreviewReady').replace('{count}', String(punchImportPreview.summary.readyCount || 0)) }}</small>
            <small>{{ t('punchPreviewUnmatched').replace('{count}', String(punchImportPreview.summary.unmatchedCount || 0)) }}</small>
            <small>{{ t('punchPreviewError').replace('{count}', String(punchImportPreview.summary.errorCount || 0)) }}</small>
          </div>
        </section>

        <section class="selattendance-schedule-side-block" v-if="punchDetail">
          <div class="seladmin-panel-header">
            <div>
              <h3>{{ t('punchDetailTitle') }}</h3>
              <p class="seladmin-copy">{{ punchDetail.employeeName || t('punchUnmatchedLabel') }}</p>
            </div>
          </div>
          <div class="selattendance-punch-detail-grid">
            <small>{{ t('punchTime') }}: {{ punchDetail.punchTime?.replace?.('T', ' ') || punchDetail.punchTime }}</small>
            <small>{{ t('punchType') }}: {{ punchDetail.punchType }}</small>
            <small>{{ t('sourceSystem') }}: {{ punchDetail.sourceSystem }}</small>
            <small>{{ t('status') }}: {{ punchDetail.processStatus }}</small>
            <small>{{ t('externalEmployeeId') }}: {{ punchDetail.externalEmployeeId || '-' }}</small>
            <small>{{ t('punchDeviceName') }}: {{ punchDetail.deviceName || '-' }}</small>
          </div>
          <label v-if="punchDetail.processStatus === 'UNMATCHED'" class="seladmin-field">
            <span>{{ t('punchBindEmployee') }}</span>
            <select v-model="punchActionForm.employeeId">
              <option value="">{{ t('punchPickEmployee') }}</option>
              <option v-for="item in employees" :key="item.id" :value="item.id">{{ item.employeeNo }} / {{ item.employeeName }}</option>
            </select>
          </label>
          <label class="seladmin-field">
            <span>{{ t('punchIgnoreReason') }}</span>
            <input v-model="punchActionForm.ignoreReason" />
          </label>
          <div class="selattendance-punch-actions">
            <button v-if="punchDetail.processStatus === 'UNMATCHED'" class="seladmin-button seladmin-button-primary" type="button" @click="onBindEmployee()">{{ t('punchBindAction') }}</button>
            <button class="seladmin-button seladmin-button-secondary" type="button" @click="onReprocessLog()">{{ t('punchReprocessAction') }}</button>
            <button class="seladmin-button seladmin-button-secondary" type="button" @click="onIgnoreLog()">{{ t('punchIgnoreAction') }}</button>
          </div>
          <details class="selattendance-punch-raw">
            <summary>{{ t('punchRawPayload') }}</summary>
            <pre>{{ punchDetail.rawPayload }}</pre>
          </details>
        </section>
      </article>
    </template>
  </ThreePaneWorkbenchLayout>
</template>
