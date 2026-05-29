<script setup>
import { computed } from 'vue'
import EmptyGuide from '../../../shared/components/EmptyGuide.vue'
import SharedDataTable from '../../../shared/components/SharedDataTable.vue'
import ThreePaneWorkbenchLayout from '../../../shared/components/ThreePaneWorkbenchLayout.vue'
import { attendanceMasterDataLayoutPreset } from '../constants/workbenchLayoutPresets'

const props = defineProps({
  visible: { type: Boolean, required: true },
  shiftTemplates: { type: Array, required: true },
  shiftForm: { type: Object, required: true },
  t: { type: Function, required: true },
  onSubmit: { type: Function, required: true },
  onGenerate: { type: Function, required: true },
  onEdit: { type: Function, required: true },
  onDelete: { type: Function, required: true }
})

// 班次模板页保持现有列结构，只把表格壳替换成共享组件。
const shiftColumns = computed(() => [
  { key: 'templateCode', label: props.t('shiftCode'), minWidth: '120px' },
  { key: 'templateName', label: props.t('shiftName'), wrap: true, minWidth: '148px' },
  { key: 'shiftType', label: props.t('shiftType'), minWidth: '116px' },
  { key: 'startTime', label: props.t('startTime'), minWidth: '108px' },
  { key: 'endTime', label: props.t('endTime'), minWidth: '108px' },
  { key: 'crossDay', label: props.t('crossDay'), minWidth: '88px' },
  { key: 'actions', label: '', minWidth: '132px' }
])
</script>

<template>
  <ThreePaneWorkbenchLayout
    v-show="visible"
    class="selattendance-master-split"
    outer-storage-key="attendance-shift-split"
    :outer-default-left-percent="attendanceMasterDataLayoutPreset.outerDefaultLeftPercent"
    :outer-min-left-percent="attendanceMasterDataLayoutPreset.outerMinLeftPercent"
    :outer-max-left-percent="attendanceMasterDataLayoutPreset.outerMaxLeftPercent"
  >
    <template #left>
      <article class="seladmin-panel seladmin-surface selattendance-data-panel selattendance-master-list-panel">
        <div class="seladmin-panel-header"><h2>{{ t('shiftTitle') }}</h2></div>
        <EmptyGuide v-if="!shiftTemplates.length" :title="t('shiftTitle')" :description="t('emptyShift')" />
        <SharedDataTable
          v-else
          variant="admin"
          :columns="shiftColumns"
          :rows="shiftTemplates"
          row-key="id"
          :active-row-key="shiftForm.id"
          clickable-rows
          min-table-width="900px"
          @row-click="onEdit"
        >
          <template #cell-startTime="{ row }">
            {{ row.startTime || '-' }}
          </template>
          <template #cell-endTime="{ row }">
            {{ row.endTime || '-' }}
          </template>
          <template #cell-crossDay="{ row }">
            {{ row.crossDay ? 'Y' : 'N' }}
          </template>
          <template #cell-actions="{ row }">
            <div class="seladmin-inline-actions">
              <button type="button" @click.stop="onEdit(row)">{{ t('save') }}</button>
              <button type="button" @click.stop="onDelete(row.id)">{{ t('delete') }}</button>
            </div>
          </template>
        </SharedDataTable>
      </article>
    </template>

    <template #main>
      <article class="seladmin-panel seladmin-surface selattendance-form-panel selattendance-master-detail-panel">
        <div class="seladmin-panel-header"><h2>{{ t('shiftTitle') }}</h2></div>
        <div class="seladmin-form-grid">
          <label class="seladmin-field"><span>{{ t('shiftCode') }}</span><input v-model="shiftForm.templateCode" /></label>
          <label class="seladmin-field"><span>{{ t('shiftName') }}</span><input v-model="shiftForm.templateName" /></label>
          <label class="seladmin-field"><span>{{ t('shiftType') }}</span><input v-model="shiftForm.shiftType" /></label>
          <label class="seladmin-field"><span>{{ t('color') }}</span><input v-model="shiftForm.color" /></label>
          <label class="seladmin-field"><span>{{ t('startTime') }}</span><input v-model="shiftForm.startTime" /></label>
          <label class="seladmin-field"><span>{{ t('endTime') }}</span><input v-model="shiftForm.endTime" /></label>
          <label class="seladmin-field"><span>{{ t('breakMinutes') }}</span><input v-model="shiftForm.scheduledBreakMinutes" type="number" /></label>
          <label class="seladmin-field seladmin-checkbox-field"><input v-model="shiftForm.crossDay" type="checkbox" /> <span>{{ t('crossDay') }}</span></label>
        </div>
        <div class="seladmin-action-row">
          <button class="seladmin-button seladmin-button-primary" type="button" @click="onSubmit()">{{ t('save') }}</button>
          <button class="seladmin-button seladmin-button-secondary" type="button" @click="onGenerate()">{{ t('generateRecommended') }}</button>
        </div>
      </article>
    </template>
  </ThreePaneWorkbenchLayout>
</template>
