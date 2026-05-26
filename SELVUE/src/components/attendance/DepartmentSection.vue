<script setup>
import EmptyGuide from '../EmptyGuide.vue'
import ResizableWorkbenchSplit from './ResizableWorkbenchSplit.vue'

defineProps({
  visible: { type: Boolean, required: true },
  workplaces: { type: Array, required: true },
  departments: { type: Array, required: true },
  departmentForm: { type: Object, required: true },
  t: { type: Function, required: true },
  onSubmit: { type: Function, required: true },
  onReset: { type: Function, required: true },
  onEdit: { type: Function, required: true },
  onDelete: { type: Function, required: true }
})
</script>

<template>
  <ResizableWorkbenchSplit v-show="visible" storage-key="attendance-department-split" :default-left-percent="60">
    <template #left>
      <article class="seladmin-panel seladmin-surface selattendance-data-panel">
        <div class="seladmin-panel-header"><h2>{{ t('departmentTitle') }}</h2></div>
        <EmptyGuide v-if="!departments.length" :title="t('departmentTitle')" :description="t('emptyDepartment')" />
        <div v-else class="selattendance-table-shell">
          <table class="seladmin-table">
            <thead><tr><th>{{ t('departmentCode') }}</th><th>{{ t('departmentName') }}</th><th>{{ t('workplace') }}</th><th>{{ t('sortOrder') }}</th><th></th></tr></thead>
            <tbody>
              <tr
                v-for="item in departments"
                :key="item.id"
                :class="{ 'selattendance-table-row-active': departmentForm.id === item.id }"
                @click="onEdit(item)"
              >
                <td>{{ item.departmentCode }}</td>
                <td>{{ item.departmentName }}</td>
                <td>{{ item.workplaceName }}</td>
                <td>{{ item.sortOrder }}</td>
                <td class="seladmin-inline-actions">
                  <button type="button" @click.stop="onEdit(item)">{{ t('save') }}</button>
                  <button type="button" @click.stop="onDelete(item.id)">{{ t('delete') }}</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>
    </template>

    <template #right>
      <article class="seladmin-panel seladmin-surface selattendance-form-panel">
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
  </ResizableWorkbenchSplit>
</template>
