<script setup>
import EmptyGuide from '../EmptyGuide.vue'
import ResizableWorkbenchSplit from './ResizableWorkbenchSplit.vue'

defineProps({
  visible: { type: Boolean, required: true },
  workplaces: { type: Array, required: true },
  workplaceForm: { type: Object, required: true },
  t: { type: Function, required: true },
  onSubmit: { type: Function, required: true },
  onReset: { type: Function, required: true },
  onEdit: { type: Function, required: true },
  onDelete: { type: Function, required: true }
})
</script>

<template>
  <ResizableWorkbenchSplit v-show="visible" storage-key="attendance-workplace-split" :default-left-percent="60">
    <template #left>
      <article class="seladmin-panel seladmin-surface selattendance-data-panel">
        <div class="seladmin-panel-header"><h2>{{ t('workplaceTitle') }}</h2></div>
        <EmptyGuide v-if="!workplaces.length" :title="t('workplaceTitle')" :description="t('emptyWorkplace')" />
        <div v-else class="selattendance-table-shell">
          <table class="seladmin-table">
            <thead><tr><th>{{ t('workplaceCode') }}</th><th>{{ t('workplaceName') }}</th><th>{{ t('address') }}</th><th>{{ t('phone') }}</th><th>{{ t('status') }}</th><th></th></tr></thead>
            <tbody>
              <tr
                v-for="item in workplaces"
                :key="item.id"
                :class="{ 'selattendance-table-row-active': workplaceForm.id === item.id }"
                @click="onEdit(item)"
              >
                <td>{{ item.workplaceCode }}</td>
                <td>{{ item.workplaceName }}</td>
                <td>{{ item.address }}</td>
                <td>{{ item.phone }}</td>
                <td>{{ item.status }}</td>
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
  </ResizableWorkbenchSplit>
</template>
