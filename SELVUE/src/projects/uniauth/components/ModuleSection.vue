<script setup>
// 模块区块只负责渲染工程模块主数据表单和列表，不直接维护任何请求副作用。
const props = defineProps({
  // pane 直接决定当前组件输出右栏表单还是中栏表格，旧的内部双栏模型已经删除。
  pane: {
    type: String,
    required: true,
    validator: (value) => ['form', 'table'].includes(value)
  },
  // moduleForm 承接当前模块编辑状态。
  moduleForm: { type: Object, required: true },
  // moduleRows 承接当前模块主数据列表。
  moduleRows: { type: Array, required: true },
  // savePending 用于驱动保存按钮等待态。
  savePending: { type: Boolean, default: false },
  // t 用于读取当前语言字段文案。
  t: { type: Function, required: true }
})

// 模块区块把保存、新增和编辑动作交给 workbench，避免组件层知道模块接口细节。
const emit = defineEmits(['submit', 'edit', 'reset'])

</script>

<template>
  <!-- 右栏表单模式只保留模块维护表单本体，删除后半段当前模块详情块。 -->
  <article
    v-if="pane === 'form'"
    class="seladmin-panel seladmin-surface selattendance-form-panel seluniauth-editor-card seluniauth-module-form-card seluniauth-module-side"
  >
    <section class="seluniauth-module-side-block">
      <header class="seladmin-panel-header seluniauth-card-header">
        <div>
          <p class="seladmin-eyebrow">{{ t('moduleFormEyebrow') }}</p>
          <h3>{{ t('moduleTitle') }}</h3>
          <p class="seladmin-copy">{{ t('moduleLead') }}</p>
        </div>
      </header>

      <form class="seluniauth-module-form" @submit.prevent="emit('submit')">
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

        <div class="seluniauth-action-row seluniauth-module-side-actions">
          <button type="submit" class="seladmin-button seladmin-button-primary" :disabled="savePending">
            {{ savePending ? `${t('save')}...` : t('save') }}
          </button>
          <button type="button" class="seladmin-button seladmin-button-secondary" @click="emit('reset')">
            {{ t('moduleCreateNew') }}
          </button>
        </div>
      </form>
    </section>
  </article>

  <!-- 中栏表格模式重构成打卡记录同口径的大列表板，主职责只保留“浏览列表并选中右侧编辑”。 -->
  <article v-else class="seladmin-panel seladmin-surface selattendance-data-panel seluniauth-table-card seluniauth-module-list-panel">
    <div class="seluniauth-table-shell">
      <table class="seluniauth-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>{{ t('moduleCode') }}</th>
            <th>{{ t('moduleName') }}</th>
            <th>{{ t('ownerSystem') }}</th>
            <th>{{ t('entryProject') }}</th>
            <th>{{ t('routeKey') }}</th>
            <th>{{ t('enabledFlag') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in moduleRows" :key="row.id" @click="emit('edit', row)">
            <td>{{ row.id }}</td>
            <td>
              <strong>{{ row.moduleCode }}</strong>
              <small>{{ row.moduleType || '-' }}</small>
            </td>
            <td>
              <strong>{{ row.moduleName }}</strong>
              <small>{{ row.moduleDesc || '-' }}</small>
            </td>
            <td>{{ row.ownerSystem || '-' }}</td>
            <td>{{ row.entryProject || '-' }}</td>
            <td>{{ row.routeKey || '-' }}</td>
            <td>
              <span class="seluniauth-module-status" :class="row.enabledFlag ? 'is-enabled' : 'is-disabled'">
                {{ row.enabledFlag ? t('enabledYes') : t('enabledNo') }}
              </span>
            </td>
          </tr>
          <tr v-if="moduleRows.length === 0">
            <td colspan="7" class="seluniauth-table-empty-cell">{{ t('moduleTableEmpty') }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </article>
</template>
