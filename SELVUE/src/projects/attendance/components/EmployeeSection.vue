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
  employees: { type: Array, required: true },
  filteredEmployees: { type: Array, required: true },
  employeeForm: { type: Object, required: true },
  mappingForm: { type: Object, required: true },
  employeeFilters: { type: Object, required: true },
  importCsvText: { type: String, required: true },
  importResult: { type: Object, required: false, default: null },
  t: { type: Function, required: true },
  onSubmit: { type: Function, required: true },
  onReset: { type: Function, required: true },
  onImport: { type: Function, required: true },
  onUpdateImportCsvText: { type: Function, required: true },
  onExport: { type: Function, required: true },
  onBind: { type: Function, required: true },
  onEditEmployee: { type: Function, required: true },
  onEditMapping: { type: Function, required: true },
  onDeleteEmployee: { type: Function, required: true }
})

// 员工页保留原有多列信息和警告文案，通过 slot 继续输出姓名副标题和绑定状态。
const employeeColumns = computed(() => [
  { key: 'employeeNo', label: props.t('employeeNo'), minWidth: '116px' },
  { key: 'employeeName', label: props.t('employeeName'), wrap: true, minWidth: '160px' },
  { key: 'departmentName', label: props.t('departmentName'), wrap: true, minWidth: '140px' },
  { key: 'workplaceName', label: props.t('workplace'), wrap: true, minWidth: '140px' },
  { key: 'employmentType', label: props.t('employmentType'), minWidth: '120px' },
  { key: 'externalEmployeeId', label: props.t('externalEmployeeId'), minWidth: '140px' },
  { key: 'actions', label: '', minWidth: '208px' }
])
</script>

<template>
  <ThreePaneWorkbenchLayout
    v-show="visible"
    class="selattendance-master-split"
    outer-storage-key="attendance-employee-split"
    :outer-default-left-percent="attendanceMasterDataLayoutPreset.outerDefaultLeftPercent"
    :outer-min-left-percent="attendanceMasterDataLayoutPreset.outerMinLeftPercent"
    :outer-max-left-percent="attendanceMasterDataLayoutPreset.outerMaxLeftPercent"
  >
    <template #left>
      <article class="seladmin-panel seladmin-surface selattendance-data-panel selattendance-master-list-panel">
        <div class="seladmin-panel-header"><h2>{{ t('employeeTitle') }}</h2></div>
        <div class="seladmin-filter-row selattendance-filter-row">
          <input v-model="employeeFilters.keyword" :placeholder="t('employeeName')" />
          <select v-model="employeeFilters.departmentId">
            <option value="">{{ t('departmentName') }}</option>
            <option v-for="item in departments" :key="item.id" :value="item.id">{{ item.departmentName }}</option>
          </select>
          <input v-model="employeeFilters.employmentType" :placeholder="t('employmentType')" />
          <input v-model="employeeFilters.status" :placeholder="t('status')" />
        </div>
        <EmptyGuide v-if="!employees.length" :title="t('employeeTitle')" :description="t('emptyEmployee')" />
        <SharedDataTable
          v-else
          variant="admin"
          :columns="employeeColumns"
          :rows="filteredEmployees"
          row-key="id"
          :active-row-key="employeeForm.id"
          clickable-rows
          min-table-width="1080px"
          @row-click="onEditEmployee"
        >
          <template #cell-employeeName="{ row }">
            <div>{{ row.employeeName }}</div>
            <small>{{ row.employeeNameKana }}</small>
          </template>
          <template #cell-externalEmployeeId="{ row }">
            <span v-if="row.externalMappingBound">{{ row.externalEmployeeId }}</span>
            <span v-else class="seladmin-warning-text">{{ t('bindMapping') }}</span>
          </template>
          <template #cell-actions="{ row }">
            <div class="seladmin-inline-actions">
              <button type="button" @click.stop="onEditEmployee(row)">{{ t('save') }}</button>
              <button type="button" @click.stop="onEditMapping(row)">{{ t('bindMapping') }}</button>
              <button type="button" @click.stop="onDeleteEmployee(row.id)">{{ t('delete') }}</button>
            </div>
          </template>
        </SharedDataTable>
      </article>
    </template>

    <template #main>
      <article class="seladmin-panel seladmin-surface selattendance-form-panel selattendance-master-detail-panel">
        <div class="seladmin-panel-header"><h2>{{ t('employeeTitle') }}</h2></div>
        <div class="seladmin-form-grid">
          <label class="seladmin-field"><span>{{ t('employeeNo') }}</span><input v-model="employeeForm.employeeNo" /></label>
          <label class="seladmin-field"><span>{{ t('employeeName') }}</span><input v-model="employeeForm.employeeName" /></label>
          <label class="seladmin-field"><span>{{ t('employeeNameKana') }}</span><input v-model="employeeForm.employeeNameKana" /></label>
          <label class="seladmin-field"><span>{{ t('employmentType') }}</span><input v-model="employeeForm.employmentType" /></label>
          <label class="seladmin-field"><span>{{ t('hireDate') }}</span><input v-model="employeeForm.hireDate" type="date" /></label>
          <label class="seladmin-field">
            <span>{{ t('workplace') }}</span>
            <select v-model="employeeForm.workplaceId">
              <option v-for="item in workplaces" :key="item.id" :value="item.id">{{ item.workplaceName }}</option>
            </select>
          </label>
          <label class="seladmin-field">
            <span>{{ t('departmentName') }}</span>
            <select v-model="employeeForm.departmentId">
              <option v-for="item in departments" :key="item.id" :value="item.id">{{ item.departmentName }}</option>
            </select>
          </label>
          <label class="seladmin-field"><span>{{ t('status') }}</span><input v-model="employeeForm.status" /></label>
        </div>
        <div class="seladmin-action-row">
          <button class="seladmin-button seladmin-button-primary" type="button" @click="onSubmit()">{{ t('save') }}</button>
          <button class="seladmin-button seladmin-button-secondary" type="button" @click="onReset()">{{ t('create') }}</button>
        </div>

        <div class="seladmin-subsection">
          <div class="seladmin-panel-header"><h3>{{ t('importTitle') }}</h3></div>
          <textarea :value="importCsvText" :placeholder="t('importPlaceholder')" rows="6" @input="onUpdateImportCsvText($event.target.value)" />
          <div class="seladmin-action-row">
            <button class="seladmin-button seladmin-button-primary" type="button" @click="onImport()">{{ t('importCsv') }}</button>
            <button class="seladmin-button seladmin-button-secondary" type="button" @click="onExport()">{{ t('exportCsv') }}</button>
          </div>
          <p v-if="importResult" class="seladmin-success-text">
            {{ importResult.successCount }} / {{ importResult.failedCount }}
            <span v-if="importResult.errors?.length"> · {{ importResult.errors.join(' | ') }}</span>
          </p>
        </div>

        <div class="seladmin-subsection">
          <div class="seladmin-panel-header"><h3>{{ t('bindMapping') }}</h3></div>
          <div class="seladmin-form-grid">
            <label class="seladmin-field"><span>{{ t('sourceSystem') }}</span><input v-model="mappingForm.sourceSystem" /></label>
            <label class="seladmin-field"><span>{{ t('externalEmployeeId') }}</span><input v-model="mappingForm.externalEmployeeId" /></label>
            <label class="seladmin-field"><span>{{ t('externalEmployeeNo') }}</span><input v-model="mappingForm.externalEmployeeNo" /></label>
          </div>
          <button class="seladmin-button seladmin-button-primary" type="button" :disabled="!mappingForm.employeeId" @click="onBind()">{{ t('save') }}</button>
        </div>
      </article>
    </template>
  </ThreePaneWorkbenchLayout>
</template>
