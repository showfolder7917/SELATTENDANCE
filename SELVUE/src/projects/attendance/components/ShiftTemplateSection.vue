<script setup>
import EmptyGuide from '../../../shared/components/EmptyGuide.vue'
import ThreePaneWorkbenchLayout from '../../../shared/components/ThreePaneWorkbenchLayout.vue'
import { attendanceMasterDataLayoutPreset } from '../constants/workbenchLayoutPresets'

defineProps({
  visible: { type: Boolean, required: true },
  shiftTemplates: { type: Array, required: true },
  shiftForm: { type: Object, required: true },
  t: { type: Function, required: true },
  onSubmit: { type: Function, required: true },
  onGenerate: { type: Function, required: true },
  onEdit: { type: Function, required: true },
  onDelete: { type: Function, required: true }
})
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
        <div v-else class="selattendance-table-shell">
          <table class="seladmin-table selattendance-wide-table">
            <thead><tr><th>{{ t('shiftCode') }}</th><th>{{ t('shiftName') }}</th><th>{{ t('shiftType') }}</th><th>{{ t('startTime') }}</th><th>{{ t('endTime') }}</th><th>{{ t('crossDay') }}</th><th></th></tr></thead>
            <tbody>
              <tr
                v-for="item in shiftTemplates"
                :key="item.id"
                :class="{ 'selattendance-table-row-active': shiftForm.id === item.id }"
                @click="onEdit(item)"
              >
                <td>{{ item.templateCode }}</td>
                <td>{{ item.templateName }}</td>
                <td>{{ item.shiftType }}</td>
                <td>{{ item.startTime || '-' }}</td>
                <td>{{ item.endTime || '-' }}</td>
                <td>{{ item.crossDay ? 'Y' : 'N' }}</td>
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
