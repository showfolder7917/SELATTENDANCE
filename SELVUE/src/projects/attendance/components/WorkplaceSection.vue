<script setup>
import EmptyGuide from '../../../shared/components/EmptyGuide.vue'
import SharedDataTable from '../../../shared/components/SharedDataTable.vue'
import ThreePaneWorkbenchLayout from '../../../shared/components/ThreePaneWorkbenchLayout.vue'
import { computed } from 'vue'
import { attendanceMasterDataLayoutPreset } from '../constants/workbenchLayoutPresets'

const props = defineProps({
  visible: { type: Boolean, required: true },
  workplaces: { type: Array, required: true },
  workplaceForm: { type: Object, required: true },
  t: { type: Function, required: true },
  onSubmit: { type: Function, required: true },
  onReset: { type: Function, required: true },
  onEdit: { type: Function, required: true },
  onOpenDepartments: { type: Function, required: true },
  onDelete: { type: Function, required: true }
})

// 基础资料表格的列定义保持在业务页，便于继续使用当前中日文案和动作列顺序。
const workplaceColumns = computed(() => [
  { key: 'workplaceCode', label: props.t('workplaceCode') },
  { key: 'workplaceName', label: props.t('workplaceName'), wrap: true, minWidth: '140px' },
  { key: 'address', label: props.t('address'), wrap: true, minWidth: '180px' },
  { key: 'phone', label: props.t('phone') },
  { key: 'status', label: props.t('status') },
  { key: 'actions', label: '', minWidth: '176px' }
])
</script>

<template>
  <ThreePaneWorkbenchLayout
    v-show="visible"
    class="selattendance-master-split"
    outer-storage-key="attendance-workplace-split"
    :outer-default-left-percent="attendanceMasterDataLayoutPreset.outerDefaultLeftPercent"
    :outer-min-left-percent="attendanceMasterDataLayoutPreset.outerMinLeftPercent"
    :outer-max-left-percent="attendanceMasterDataLayoutPreset.outerMaxLeftPercent"
  >
    <template #left>
      <article class="seladmin-panel seladmin-surface selattendance-data-panel selattendance-master-list-panel">
        <div class="seladmin-panel-header"><h2>{{ t('workplaceTitle') }}</h2></div>
        <EmptyGuide v-if="!workplaces.length" :title="t('workplaceTitle')" :description="t('emptyWorkplace')" />
        <SharedDataTable
          v-else
          variant="admin"
          :columns="workplaceColumns"
          :rows="workplaces"
          row-key="id"
          :active-row-key="workplaceForm.id"
          clickable-rows
          min-table-width="820px"
          @row-click="onEdit"
        >
          <template #cell-actions="{ row }">
            <div class="seladmin-inline-actions">
              <button type="button" @click.stop="onEdit(row)">{{ t('save') }}</button>
              <button type="button" @click.stop="onOpenDepartments(row)">{{ t('jumpDepartment') }}</button>
              <button type="button" @click.stop="onDelete(row.id)">{{ t('delete') }}</button>
            </div>
          </template>
        </SharedDataTable>
      </article>
    </template>

    <template #main>
      <article class="seladmin-panel seladmin-surface selattendance-form-panel selattendance-master-detail-panel">
        <div class="seladmin-panel-header"><h2>{{ t('workplaceTitle') }}</h2></div>
        <div class="seladmin-form-grid">
          <label class="seladmin-field"><span>{{ t('workplaceCode') }}</span><input v-model="workplaceForm.workplaceCode" /></label>
          <label class="seladmin-field"><span>{{ t('workplaceName') }}</span><input v-model="workplaceForm.workplaceName" /></label>
          <label class="seladmin-field"><span>{{ t('address') }}</span><input v-model="workplaceForm.address" /></label>
          <label class="seladmin-field"><span>{{ t('phone') }}</span><input v-model="workplaceForm.phone" /></label>
        </div>
        <div class="seladmin-action-row">
          <button class="seladmin-button seladmin-button-primary" type="button" @click="onSubmit()">{{ t('save') }}</button>
          <button class="seladmin-button seladmin-button-secondary" type="button" @click="onReset()">{{ t('create') }}</button>
        </div>
      </article>
    </template>
  </ThreePaneWorkbenchLayout>
</template>
