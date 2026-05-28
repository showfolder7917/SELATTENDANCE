<script setup>
import { computed } from 'vue'
import ResizableWorkbenchSplit from './ResizableWorkbenchSplit.vue'

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
  onReprocessLog: { type: Function, required: true }
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

// 上一页按钮在第 1 页时禁用，避免无效数据库分页请求。
const isPrevDisabled = computed(() => currentPage.value <= 1)

// 下一页按钮在最后一页禁用，避免继续请求不存在的页码。
const isNextDisabled = computed(() => currentPage.value >= totalPages.value)

// 点击上一页时只回退一个页码，让外层 watch 触发新的数据库分页请求。
function goPrevPage() {
  if (isPrevDisabled.value) return
  props.punchFilters.page = currentPage.value - 1
}

// 点击下一页时只前进一个页码，让外层 watch 触发新的数据库分页请求。
function goNextPage() {
  if (isNextDisabled.value) return
  props.punchFilters.page = currentPage.value + 1
}

// 切换每页条数时同步回到第 1 页，避免旧页码在新页大小下跳空。
function handlePageSizeChange(event) {
  props.punchFilters.pageSize = Number(event.target.value)
  props.punchFilters.page = 1
}
</script>

<template>
  <ResizableWorkbenchSplit v-show="visible" storage-key="attendance-punch-split" :default-left-percent="65" :min-left-percent="48" :max-left-percent="78">
    <template #left>
      <article class="seladmin-panel seladmin-surface selattendance-data-panel">
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

        <div class="selattendance-punch-list-shell">
          <table class="selattendance-punch-table">
            <thead>
              <tr>
                <th>{{ t('employeeName') }}</th>
                <th>{{ t('externalEmployeeId') }}</th>
                <th>{{ t('punchTime') }}</th>
                <th>{{ t('punchType') }}</th>
                <th>{{ t('sourceSystem') }}</th>
                <th>{{ t('status') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in punchLogList.items"
                :key="item.id"
                class="selattendance-punch-row"
                :class="{ active: punchDetail?.id === item.id }"
                @click="onSelectLog(item)"
              >
                <td>
                  <strong>{{ item.employeeName || t('punchUnmatchedLabel') }}</strong>
                  <small>{{ item.employeeNo || item.deviceName || '-' }}</small>
                </td>
                <td>{{ item.externalEmployeeId || '-' }}</td>
                <td>{{ item.punchTime?.replace?.('T', ' ') || item.punchTime }}</td>
                <td>{{ item.punchType }}</td>
                <td>{{ item.sourceSystem }}</td>
                <td><span class="selattendance-punch-status" :class="`status-${(item.processStatus || '').toLowerCase()}`">{{ item.processStatus }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="selattendance-punch-pagination">
          <label class="seladmin-field selattendance-punch-pagination-size">
            <span>{{ t('punchPageSize') }}</span>
            <select :value="String(punchFilters.pageSize || 20)" @change="handlePageSizeChange">
              <option v-for="size in pageSizeOptions" :key="size" :value="String(size)">{{ size }}</option>
            </select>
          </label>
          <div class="selattendance-punch-pagination-meta">
            <small>{{ t('punchPaginationSummary').replace('{total}', String(punchLogList.total || 0)) }}</small>
            <small>{{ t('punchPaginationCurrent').replace('{page}', String(currentPage)).replace('{totalPages}', String(totalPages)) }}</small>
          </div>
          <div class="selattendance-punch-pagination-actions">
            <button class="seladmin-button seladmin-button-secondary" type="button" :disabled="isPrevDisabled" @click="goPrevPage()">{{ t('punchPrevPage') }}</button>
            <button class="seladmin-button seladmin-button-secondary" type="button" :disabled="isNextDisabled" @click="goNextPage()">{{ t('punchNextPage') }}</button>
          </div>
        </div>
      </article>
    </template>

    <template #right>
      <article class="seladmin-panel seladmin-surface selattendance-form-panel selattendance-punch-side">
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
  </ResizableWorkbenchSplit>
</template>
