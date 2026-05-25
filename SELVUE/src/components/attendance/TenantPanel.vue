<script setup>
// 租户面板只负责展示与编辑公司基础信息，保存动作仍由父视图统一编排。
defineProps({
  tenant: { type: Object, required: true },
  t: { type: Function, required: true },
  onSubmit: { type: Function, required: true }
})
</script>

<template>
  <section class="seladmin-panel seladmin-surface">
    <div class="seladmin-panel-header">
      <div>
        <p class="seladmin-eyebrow">{{ t('wizard.tenant') }}</p>
        <h2>{{ tenant.tenantName || t('tenantName') }}</h2>
      </div>
    </div>
    <div class="seladmin-form-grid">
      <label class="seladmin-field"><span>{{ t('tenantCode') }}</span><input v-model="tenant.tenantCode" /></label>
      <label class="seladmin-field"><span>{{ t('tenantName') }}</span><input v-model="tenant.tenantName" /></label>
      <label class="seladmin-field"><span>{{ t('contactName') }}</span><input v-model="tenant.contactName" /></label>
      <label class="seladmin-field"><span>{{ t('contactPhone') }}</span><input v-model="tenant.contactPhone" /></label>
      <label class="seladmin-field"><span>{{ t('contactEmail') }}</span><input v-model="tenant.contactEmail" /></label>
      <label class="seladmin-field"><span>{{ t('timezone') }}</span><input v-model="tenant.timezone" /></label>
    </div>
    <button class="seladmin-button seladmin-button-primary" type="button" @click="onSubmit()">{{ t('save') }}</button>
  </section>
</template>

