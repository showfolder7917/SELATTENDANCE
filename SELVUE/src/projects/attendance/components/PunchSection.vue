<script setup>
import { computed, ref, watch } from 'vue'
import SharedDataTable from '../../../shared/components/SharedDataTable.vue'
import SharedMiniSelect from '../../../shared/components/SharedMiniSelect.vue'
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

// 跳页输入框单独维护本地字符串，允许用户先输入再点按钮触发校验。
const pageJumpInput = ref('')

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

  // 当前页靠前时多展示前几页，减少一开始就出现过早省略号。
  if (current <= 4) {
    start = 2
    end = 5
  }

  // 当前页接近尾部时多展示最后几页，让用户能直接看到收尾页码。
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

// 打卡列表列定义继续留在业务页，保证状态文案、字段顺序和后续 slot 扩展都由业务决定。
const punchColumns = computed(() => [
  { key: 'employeeName', label: props.t('employeeName'), minWidth: '160px' },
  { key: 'externalEmployeeId', label: props.t('externalEmployeeId'), minWidth: '136px' },
  { key: 'punchTime', label: props.t('punchTime'), minWidth: '170px' },
  { key: 'punchType', label: props.t('punchType'), minWidth: '120px' },
  { key: 'sourceSystem', label: props.t('sourceSystem'), minWidth: '120px' },
  { key: 'processStatus', label: props.t('status'), minWidth: '116px' }
])

// 当前页变化后同步回填跳页输入框，让输入框始终反映实际分页状态。
watch(
  () => currentPage.value,
  (nextPage) => {
    pageJumpInput.value = String(nextPage)
  },
  { immediate: true }
)

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

// 点击最后一页时直接跳到总页数，方便长列表快速收尾定位。
function goLastPage() {
  if (isLastDisabled.value) return
  props.punchFilters.page = totalPages.value
}

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

// 点击加载时校验输入页码是否合法，错误时用页面级 toast 直接提示用户。
function submitPageJump() {
  const rawValue = String(pageJumpInput.value || '').trim()
  if (!/^\d+$/.test(rawValue)) {
    props.onShowToast(
      props.t('punchPageInvalid').replace('{totalPages}', String(totalPages.value))
    )
    return
  }

  const targetPage = Number(rawValue)
  if (targetPage < 1 || targetPage > totalPages.value) {
    props.onShowToast(
      props.t('punchPageOutOfRange').replace('{totalPages}', String(totalPages.value))
    )
    return
  }

  goToPage(targetPage)
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
          :show-pagination="totalPages > 1"
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
            <div class="selattendance-punch-pagination">
              <label class="selattendance-punch-pagination-size">
                <span>{{ t('punchPageSize') }}</span>
                <SharedMiniSelect
                  :model-value="String(punchFilters.pageSize || 20)"
                  :options="pageSizeOptions.map((size) => ({ value: String(size), label: String(size) }))"
                  :aria-label="t('punchPageSize')"
                  @change="handlePageSizeChange"
                />
              </label>

              <div class="selattendance-punch-pagination-pages" :aria-label="t('punchPaginationNav')">
                <button class="seladmin-button seladmin-button-secondary" type="button" :disabled="isPrevDisabled" @click="goPrevPage()">{{ t('punchPrevPage') }}</button>
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
                <button class="selattendance-pagination-tail" type="button" :disabled="isLastDisabled" @click="goLastPage()">{{ t('punchLastPage') }}</button>
                <button class="seladmin-button seladmin-button-secondary" type="button" :disabled="isNextDisabled" @click="goNextPage()">{{ t('punchNextPage') }}</button>
              </div>

              <div class="selattendance-punch-pagination-jump">
                <label class="selattendance-punch-pagination-jump-field">
                  <span>{{ t('punchPageJumpPrefix') }}</span>
                  <input
                    v-model="pageJumpInput"
                    inputmode="numeric"
                    :placeholder="t('punchPageJumpPlaceholder').replace('{totalPages}', String(totalPages))"
                    @keydown.enter.prevent="submitPageJump()"
                  />
                  <span>{{ t('punchPageJumpSuffix') }}</span>
                </label>
                <button class="seladmin-button seladmin-button-primary" type="button" @click="submitPageJump()">{{ t('punchPageJumpSubmit') }}</button>
              </div>

              <div class="selattendance-punch-pagination-meta">
                <small>{{ t('punchPaginationSummary').replace('{total}', String(punchLogList.total || 0)) }}</small>
              </div>
            </div>
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
