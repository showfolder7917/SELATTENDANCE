<script setup>
import { computed } from 'vue'
import EmptyGuide from '../../../shared/components/EmptyGuide.vue'
import SharedDataTable from '../../../shared/components/SharedDataTable.vue'
import ThreePaneWorkbenchLayout from '../../../shared/components/ThreePaneWorkbenchLayout.vue'
import { attendanceMasterDataLayoutPreset } from '../constants/workbenchLayoutPresets'

const props = defineProps({
  visible: { type: Boolean, required: true },
  workplaces: { type: Array, required: true },
  departments: { type: Array, required: true },
  filteredDepartments: { type: Array, required: true },
  departmentForm: { type: Object, required: true },
  currentWorkplaceName: { type: String, required: false, default: '' },
  t: { type: Function, required: true },
  onSubmit: { type: Function, required: true },
  onReset: { type: Function, required: true },
  onEdit: { type: Function, required: true },
  onClearWorkplaceFilter: { type: Function, required: true },
  onOpenEmployees: { type: Function, required: true },
  onOpenSchedule: { type: Function, required: true },
  onDelete: { type: Function, required: true }
})

// 部门页继续保留当前列顺序和动作列，只把表格展示壳下沉到 shared。
const departmentColumns = computed(() => [
  { key: 'departmentCode', label: props.t('departmentCode') },
  { key: 'departmentName', label: props.t('departmentName'), wrap: true, minWidth: '140px' },
  { key: 'workplaceName', label: props.t('workplace'), wrap: true, minWidth: '140px' },
  { key: 'sortOrder', label: props.t('sortOrder'), minWidth: '92px' },
  { key: 'actions', label: '', minWidth: '248px' }
])
</script>

<template>
  <ThreePaneWorkbenchLayout
    v-show="visible"
    class="selattendance-master-split"
    outer-storage-key="attendance-department-split"
    :outer-default-left-percent="attendanceMasterDataLayoutPreset.outerDefaultLeftPercent"
    :outer-min-left-percent="attendanceMasterDataLayoutPreset.outerMinLeftPercent"
    :outer-max-left-percent="attendanceMasterDataLayoutPreset.outerMaxLeftPercent"
  >
    <template #left>
      <article class="seladmin-panel seladmin-surface selattendance-data-panel selattendance-master-list-panel">
        <div class="seladmin-panel-header"><h2>{{ t('departmentTitle') }}</h2></div>
        <div v-if="currentWorkplaceName" class="selattendance-context-strip">
          <span>{{ t('currentWorkplaceFilter').replace('{name}', currentWorkplaceName) }}</span>
          <button type="button" @click="onClearWorkplaceFilter()">{{ t('showAllDepartments') }}</button>
        </div>
        <EmptyGuide v-if="!departments.length" :title="t('departmentTitle')" :description="t('emptyDepartment')" />
        <SharedDataTable
          v-else
          variant="admin"
          :columns="departmentColumns"
          :rows="filteredDepartments"
          row-key="id"
          :active-row-key="departmentForm.id"
          clickable-rows
          min-table-width="860px"
          @row-click="onEdit"
        >
          <template #cell-actions="{ row }">
            <div class="seladmin-inline-actions">
              <button type="button" @click.stop="onEdit(row)">{{ t('save') }}</button>
              <button type="button" @click.stop="onOpenEmployees(row)">{{ t('jumpEmployee') }}</button>
              <button type="button" @click.stop="onOpenSchedule(row)">{{ t('jumpSchedule') }}</button>
              <button type="button" @click.stop="onDelete(row.id)">{{ t('delete') }}</button>
            </div>
          </template>
        </SharedDataTable>
      </article>
    </template>

    <template #main>
      <article class="seladmin-panel seladmin-surface selattendance-form-panel selattendance-master-detail-panel">
        <div class="seladmin-panel-header"><h2>{{ t('departmentTitle') }}</h2></div>
        <div class="seladmin-form-grid">
          <label class="seladmin-field">
            <span>{{ t('workplace') }}</span>
            <select v-model="departmentForm.workplaceId">
              <option v-for="item in workplaces" :key="item.id" :value="item.id">{{ item.workplaceName }}</option>
            </select>
          </label>
          <label class="seladmin-field"><span>{{ t('departmentCode') }}</span><input v-model="departmentForm.departmentCode" /></label>
          <label class="seladmin-field"><span>{{ t('departmentName') }}</span><input v-model="departmentForm.departmentName" /></label>
          <label class="seladmin-field"><span>{{ t('sortOrder') }}</span><input v-model="departmentForm.sortOrder" type="number" /></label>
        </div>
        <div class="seladmin-action-row">
          <button class="seladmin-button seladmin-button-primary" type="button" @click="onSubmit()">{{ t('save') }}</button>
          <button class="seladmin-button seladmin-button-secondary" type="button" @click="onReset()">{{ t('create') }}</button>
        </div>
      </article>
    </template>
  </ThreePaneWorkbenchLayout>
</template>
