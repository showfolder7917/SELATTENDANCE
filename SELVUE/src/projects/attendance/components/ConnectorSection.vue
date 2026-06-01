<script setup>
import { computed } from 'vue'
import EmptyGuide from '../../../shared/components/EmptyGuide.vue'
import SharedDataTable from '../../../shared/components/SharedDataTable.vue'
import ThreePaneWorkbenchLayout from '../../../shared/components/ThreePaneWorkbenchLayout.vue'
import { attendanceMasterDataLayoutPreset } from '../../../shared/constants/workbenchLayoutConfig'

const props = defineProps({
  visible: { type: Boolean, required: true },
  employees: { type: Array, required: true },
  workplaces: { type: Array, required: true },
  connectorWorkbench: { type: Object, required: true },
  connectorForm: { type: Object, required: true },
  connectorMappingForm: { type: Object, required: true },
  connectorTestResult: { type: Object, default: null },
  t: { type: Function, required: true },
  onSubmitConnector: { type: Function, required: true },
  onResetConnector: { type: Function, required: true },
  onEditConnector: { type: Function, required: true },
  onTestConnector: { type: Function, required: true },
  onSubmitMapping: { type: Function, required: true },
  onResetMapping: { type: Function, required: true },
  onEditMapping: { type: Function, required: true },
  onRetryLog: { type: Function, required: true }
})

// 左栏只保留连接器清单，避免接入页再次把筛选和统计挤回列表区。
const connectorColumns = computed(() => [
  { key: 'sourceSystem', label: props.t('connectorSourceSystem'), minWidth: '132px' },
  { key: 'connectorName', label: props.t('connectorName'), minWidth: '180px', wrap: true },
  { key: 'receiveMode', label: props.t('connectorReceiveMode'), minWidth: '110px' },
  { key: 'activeFlag', label: props.t('status'), minWidth: '96px' }
])

// 右栏映射表突出内部员工和外部编号，供管理员快速确认谁已经能被第三方识别。
const mappingColumns = computed(() => [
  { key: 'employeeNo', label: props.t('employeeNo'), minWidth: '120px' },
  { key: 'employeeName', label: props.t('employeeName'), minWidth: '140px' },
  { key: 'externalEmployeeId', label: props.t('externalEmployeeId'), minWidth: '160px' },
  { key: 'sourceSystem', label: props.t('sourceSystem'), minWidth: '120px' }
])

// 同步日志表只保留最关键的问题定位字段，避免管理员第一次进入就被大段快照压住。
const syncLogColumns = computed(() => [
  { key: 'externalRequestId', label: props.t('connectorRequestId'), minWidth: '156px' },
  { key: 'triggerType', label: props.t('connectorTriggerType'), minWidth: '96px' },
  { key: 'syncStatus', label: props.t('status'), minWidth: '100px' },
  { key: 'failedCount', label: props.t('connectorFailedCount'), minWidth: '90px' },
  { key: 'actions', label: '', minWidth: '96px' }
])
</script>

<template>
  <ThreePaneWorkbenchLayout
    v-show="visible"
    class="selattendance-master-split"
    outer-storage-key="attendance-connector-split"
    :outer-default-left-percent="attendanceMasterDataLayoutPreset.outerDefaultLeftPercent"
    :outer-min-left-percent="attendanceMasterDataLayoutPreset.outerMinLeftPercent"
    :outer-max-left-percent="attendanceMasterDataLayoutPreset.outerMaxLeftPercent"
  >
    <template #left>
      <article class="seladmin-panel seladmin-surface selattendance-data-panel selattendance-master-list-panel">
        <div class="seladmin-panel-header"><h2>{{ t('connectorListTitle') }}</h2></div>
        <EmptyGuide
          v-if="!connectorWorkbench.connectors?.length"
          :title="t('connectorListTitle')"
          :description="t('connectorEmpty')"
        />
        <SharedDataTable
          v-else
          variant="admin"
          :columns="connectorColumns"
          :rows="connectorWorkbench.connectors"
          row-key="id"
          :active-row-key="connectorForm.id"
          clickable-rows
          min-table-width="860px"
          @row-click="onEditConnector"
        >
          <template #cell-activeFlag="{ row }">
            <span>{{ row.activeFlag ? t('activeStatus') : t('inactiveStatus') }}</span>
          </template>
        </SharedDataTable>
      </article>
    </template>

    <template #main>
      <article class="seladmin-panel seladmin-surface selattendance-form-panel selattendance-master-detail-panel">
        <div class="seladmin-panel-header"><h2>{{ t('connectorFormTitle') }}</h2></div>
        <div class="seladmin-form-grid">
          <label class="seladmin-field"><span>{{ t('connectorSourceSystem') }}</span><input v-model="connectorForm.sourceSystem" /></label>
          <label class="seladmin-field"><span>{{ t('connectorName') }}</span><input v-model="connectorForm.connectorName" /></label>
          <label class="seladmin-field"><span>{{ t('connectorProviderType') }}</span><input v-model="connectorForm.providerType" /></label>
          <label class="seladmin-field">
            <span>{{ t('connectorReceiveMode') }}</span>
            <select v-model="connectorForm.receiveMode">
              <option value="WEBHOOK">WEBHOOK</option>
              <option value="PULL">PULL</option>
              <option value="CSV">CSV</option>
            </select>
          </label>
          <label class="seladmin-field"><span>{{ t('connectorApiBaseUrl') }}</span><input v-model="connectorForm.apiBaseUrl" /></label>
          <label class="seladmin-field"><span>{{ t('connectorApiKey') }}</span><input v-model="connectorForm.apiKey" /></label>
          <label class="seladmin-field"><span>{{ t('connectorApiSecret') }}</span><input v-model="connectorForm.apiSecret" /></label>
          <label class="seladmin-field"><span>{{ t('connectorWebhookSecret') }}</span><input v-model="connectorForm.webhookSecret" /></label>
          <label class="seladmin-field"><span>{{ t('connectorSyncCron') }}</span><input v-model="connectorForm.syncCron" /></label>
          <label class="seladmin-field">
            <span>{{ t('workplace') }}</span>
            <select v-model="connectorForm.workplaceId">
              <option value="">{{ t('allWorkplaces') }}</option>
              <option v-for="item in workplaces" :key="item.id" :value="item.id">{{ item.workplaceName }}</option>
            </select>
          </label>
          <label class="seladmin-field selattendance-switch-field">
            <span>{{ t('ruleActiveFlag') }}</span>
            <input v-model="connectorForm.activeFlag" type="checkbox" />
          </label>
        </div>
        <label class="seladmin-field">
          <span>{{ t('remark') }}</span>
          <textarea v-model="connectorForm.note" rows="3" />
        </label>
        <div v-if="connectorTestResult?.webhookUrl" class="selattendance-context-strip">
          <span>{{ t('connectorWebhookUrl') }}: {{ connectorTestResult.webhookUrl }}</span>
        </div>
        <div class="seladmin-action-row">
          <button class="seladmin-button seladmin-button-primary" type="button" @click="onSubmitConnector()">{{ t('save') }}</button>
          <button class="seladmin-button seladmin-button-secondary" type="button" @click="onTestConnector()">{{ t('connectorTestAction') }}</button>
          <button class="seladmin-button seladmin-button-secondary" type="button" @click="onResetConnector()">{{ t('create') }}</button>
        </div>
      </article>
    </template>

    <template #right>
      <article class="seladmin-panel seladmin-surface selattendance-form-panel selattendance-master-detail-panel">
        <div class="seladmin-panel-header"><h2>{{ t('connectorMappingTitle') }}</h2></div>
        <div class="seladmin-form-grid">
          <label class="seladmin-field">
            <span>{{ t('employeeName') }}</span>
            <select v-model="connectorMappingForm.employeeId">
              <option value="">{{ t('connectorSelectEmployee') }}</option>
              <option v-for="item in employees" :key="item.id" :value="item.id">{{ item.employeeNo }} / {{ item.employeeName }}</option>
            </select>
          </label>
          <label class="seladmin-field"><span>{{ t('connectorSourceSystem') }}</span><input v-model="connectorMappingForm.sourceSystem" /></label>
          <label class="seladmin-field"><span>{{ t('externalEmployeeId') }}</span><input v-model="connectorMappingForm.externalEmployeeId" /></label>
          <label class="seladmin-field"><span>{{ t('externalEmployeeNo') }}</span><input v-model="connectorMappingForm.externalEmployeeNo" /></label>
        </div>
        <div class="seladmin-action-row">
          <button class="seladmin-button seladmin-button-primary" type="button" @click="onSubmitMapping()">{{ t('save') }}</button>
          <button class="seladmin-button seladmin-button-secondary" type="button" @click="onResetMapping()">{{ t('create') }}</button>
        </div>

        <div class="seladmin-subsection">
          <div class="seladmin-panel-header"><h3>{{ t('connectorMappingListTitle') }}</h3></div>
          <EmptyGuide
            v-if="!connectorWorkbench.mappings?.length"
            :title="t('connectorMappingListTitle')"
            :description="t('connectorMappingEmpty')"
          />
          <SharedDataTable
            v-else
            variant="admin"
            :columns="mappingColumns"
            :rows="connectorWorkbench.mappings"
            row-key="employeeId"
            :active-row-key="connectorMappingForm.employeeId"
            clickable-rows
            min-table-width="920px"
            @row-click="onEditMapping"
          />
        </div>

        <div class="seladmin-subsection">
          <div class="seladmin-panel-header"><h3>{{ t('connectorSyncLogTitle') }}</h3></div>
          <EmptyGuide
            v-if="!connectorWorkbench.syncLogs?.length"
            :title="t('connectorSyncLogTitle')"
            :description="t('connectorSyncEmpty')"
          />
          <SharedDataTable
            v-else
            variant="admin"
            :columns="syncLogColumns"
            :rows="connectorWorkbench.syncLogs"
            row-key="id"
            min-table-width="920px"
          >
            <template #cell-actions="{ row }">
              <button type="button" @click.stop="onRetryLog(row)">{{ t('connectorRetryAction') }}</button>
            </template>
          </SharedDataTable>
        </div>
      </article>
    </template>
  </ThreePaneWorkbenchLayout>
</template>
