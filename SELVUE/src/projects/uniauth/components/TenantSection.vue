<script setup>
// 租户区块只负责渲染租户表单和列表，不直接发起任何接口调用。
defineProps({
  // tenantForm 承接当前租户编辑状态。
  tenantForm: { type: Object, required: true },
  // tenantRows 承接当前租户列表。
  tenantRows: { type: Array, required: true },
  // savePending 用于驱动保存按钮等待态。
  savePending: { type: Boolean, default: false },
  // t 用于读取当前语言字段文案。
  t: { type: Function, required: true }
})

// 租户区块把保存和编辑动作交给 workbench，让状态和请求都留在 composable 层。
defineEmits(['submit', 'edit'])
</script>

<template>
  <!-- 租户区块沿用 attendance 的“左编辑右列表”两栏节奏。 -->
  <section class="seluniauth-section-shell">
    <!-- 左栏表单负责维护租户编码、名称、联系人和启停状态。 -->
    <form class="seluniauth-editor-card seladmin-surface" @submit.prevent="$emit('submit')">
      <header class="seluniauth-card-header">
        <h3>{{ t('tenantTitle') }}</h3>
        <p class="seladmin-copy">{{ t('tenantLead') }}</p>
      </header>

      <div class="seluniauth-form-grid">
        <label class="seladmin-field">
          <span>{{ t('tenantCode') }}</span>
          <input v-model="tenantForm.tenantCode" type="text" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('tenantName') }}</span>
          <input v-model="tenantForm.tenantName" type="text" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('tenantStatus') }}</span>
          <input v-model="tenantForm.tenantStatus" type="text" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('contactName') }}</span>
          <input v-model="tenantForm.contactName" type="text" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('contactEmail') }}</span>
          <input v-model="tenantForm.contactEmail" type="email" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('contactPhone') }}</span>
          <input v-model="tenantForm.contactPhone" type="text" />
        </label>
      </div>

      <div class="seluniauth-action-row">
        <button type="submit" class="seladmin-button seladmin-button-primary" :disabled="savePending">
          {{ savePending ? `${t('save')}...` : t('save') }}
        </button>
      </div>
    </form>

    <!-- 右栏表格负责展示已有租户，点击后回填左栏表单继续修改。 -->
    <section class="seluniauth-table-card seladmin-surface">
      <header class="seluniauth-card-header">
        <h3>{{ t('summaryTenant') }}</h3>
        <p class="seladmin-copy">{{ t('editHint') }}</p>
      </header>

      <div class="seluniauth-table-shell">
        <table class="seluniauth-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>{{ t('tableTenantCode') }}</th>
              <th>{{ t('tableTenantName') }}</th>
              <th>{{ t('tableTenantStatus') }}</th>
              <th>{{ t('contactName') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in tenantRows" :key="row.id" @click="$emit('edit', row)">
              <td>{{ row.id }}</td>
              <td>{{ row.tenantCode }}</td>
              <td>{{ row.tenantName }}</td>
              <td>{{ row.tenantStatus }}</td>
              <td>{{ row.contactName || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>
