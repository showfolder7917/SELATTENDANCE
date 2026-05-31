<script setup>
// 模块区块只负责渲染工程模块主数据表单和列表，不直接维护任何请求副作用。
defineProps({
  // moduleForm 承接当前模块编辑状态。
  moduleForm: { type: Object, required: true },
  // moduleRows 承接当前模块主数据列表。
  moduleRows: { type: Array, required: true },
  // savePending 用于驱动保存按钮等待态。
  savePending: { type: Boolean, default: false },
  // t 用于读取当前语言字段文案。
  t: { type: Function, required: true }
})

// 模块区块把保存和编辑动作交给 workbench，避免组件层知道模块接口细节。
defineEmits(['submit', 'edit'])
</script>

<template>
  <!-- 模块区块继续沿用统一双栏布局，让模块主数据编辑和当前模块清单保持同屏。 -->
  <section class="seluniauth-section-shell">
    <!-- 左栏表单负责模块编码、入口工程、归属系统和路由键维护。 -->
    <form class="seluniauth-editor-card seladmin-surface" @submit.prevent="$emit('submit')">
      <header class="seluniauth-card-header">
        <h3>{{ t('moduleTitle') }}</h3>
        <p class="seladmin-copy">{{ t('moduleLead') }}</p>
      </header>

      <div class="seluniauth-form-grid">
        <label class="seladmin-field">
          <span>{{ t('moduleCode') }}</span>
          <input v-model="moduleForm.moduleCode" type="text" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('moduleName') }}</span>
          <input v-model="moduleForm.moduleName" type="text" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('moduleType') }}</span>
          <input v-model="moduleForm.moduleType" type="text" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('entryProject') }}</span>
          <input v-model="moduleForm.entryProject" type="text" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('ownerSystem') }}</span>
          <input v-model="moduleForm.ownerSystem" type="text" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('routeKey') }}</span>
          <input v-model="moduleForm.routeKey" type="text" />
        </label>
        <label class="seladmin-field seluniauth-field-span-2">
          <span>{{ t('moduleDesc') }}</span>
          <textarea v-model="moduleForm.moduleDesc" rows="4" />
        </label>
        <label class="seluniauth-inline-check">
          <input v-model="moduleForm.enabledFlag" type="checkbox" />
          <span>{{ t('enabledFlag') }}</span>
        </label>
      </div>

      <div class="seluniauth-action-row">
        <button type="submit" class="seladmin-button seladmin-button-primary" :disabled="savePending">
          {{ savePending ? `${t('save')}...` : t('save') }}
        </button>
      </div>
    </form>

    <!-- 右栏表格负责列出已托管模块，点击后回填当前主数据继续修改。 -->
    <section class="seluniauth-table-card seladmin-surface">
      <header class="seluniauth-card-header">
        <h3>{{ t('summaryModule') }}</h3>
        <p class="seladmin-copy">{{ t('editHint') }}</p>
      </header>

      <div class="seluniauth-table-shell">
        <table class="seluniauth-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>{{ t('moduleCode') }}</th>
              <th>{{ t('moduleName') }}</th>
              <th>{{ t('entryProject') }}</th>
              <th>{{ t('routeKey') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in moduleRows" :key="row.id" @click="$emit('edit', row)">
              <td>{{ row.id }}</td>
              <td>{{ row.moduleCode }}</td>
              <td>{{ row.moduleName }}</td>
              <td>{{ row.entryProject || '-' }}</td>
              <td>{{ row.routeKey || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>
