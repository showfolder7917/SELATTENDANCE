<script setup>
// 菜单区块只负责渲染动态菜单节点表单和列表，不直接知道宿主请求细节。
defineProps({
  // pane 直接决定当前组件输出右栏菜单表单还是中栏菜单列表，旧内部双栏结构已删除。
  pane: {
    type: String,
    required: true,
    validator: (value) => ['form', 'table'].includes(value)
  },
  // menuForm 承接当前菜单节点编辑状态。
  menuForm: { type: Object, required: true },
  // menuRows 承接当前菜单节点列表。
  menuRows: { type: Array, required: true },
  // savePending 用于驱动保存按钮等待态。
  savePending: { type: Boolean, default: false },
  // t 用于读取当前语言字段文案。
  t: { type: Function, required: true }
})

// 菜单区块把保存和编辑动作上抛给 workbench，让动态导航节点维护逻辑统一收口。
defineEmits(['submit', 'edit'])
</script>

<template>
  <!-- 右栏表单模式只承接菜单节点编辑，把动态路由和双语标题统一收口到右栏。 -->
  <form
    v-if="pane === 'form'"
    class="seladmin-panel seladmin-surface selattendance-form-panel seluniauth-editor-card seluniauth-module-form-card"
    @submit.prevent="$emit('submit')"
  >
    <header class="seladmin-panel-header seluniauth-card-header">
      <p class="seladmin-eyebrow">{{ t('moduleFormEyebrow') }}</p>
      <h3>{{ t('menuTitle') }}</h3>
      <p class="seladmin-copy">{{ t('menuLead') }}</p>
    </header>

    <div class="seluniauth-form-grid">
      <label class="seladmin-field">
        <span>{{ t('moduleCode') }}</span>
        <input v-model="menuForm.moduleCode" type="text" />
      </label>
      <label class="seladmin-field">
        <span>{{ t('menuCode') }}</span>
        <input v-model="menuForm.menuCode" type="text" />
      </label>
      <label class="seladmin-field">
        <span>{{ t('parentId') }}</span>
        <input v-model="menuForm.parentId" type="number" />
      </label>
      <label class="seladmin-field">
        <span>{{ t('menuType') }}</span>
        <input v-model="menuForm.menuType" type="text" />
      </label>
      <label class="seladmin-field">
        <span>{{ t('routePath') }}</span>
        <input v-model="menuForm.routePath" type="text" />
      </label>
      <label class="seladmin-field">
        <span>{{ t('componentName') }}</span>
        <input v-model="menuForm.componentName" type="text" />
      </label>
      <label class="seladmin-field">
        <span>{{ t('iconName') }}</span>
        <input v-model="menuForm.iconName" type="text" />
      </label>
      <label class="seladmin-field">
        <span>{{ t('sortOrder') }}</span>
        <input v-model="menuForm.sortOrder" type="number" />
      </label>
      <label class="seladmin-field">
        <span>{{ t('titleZh') }}</span>
        <input v-model="menuForm.titleZh" type="text" />
      </label>
      <label class="seladmin-field">
        <span>{{ t('titleJa') }}</span>
        <input v-model="menuForm.titleJa" type="text" />
      </label>
      <label class="seluniauth-inline-check">
        <input v-model="menuForm.enabledFlag" type="checkbox" />
        <span>{{ t('enabledFlag') }}</span>
      </label>
    </div>

    <div class="seluniauth-action-row">
      <button type="submit" class="seladmin-button seladmin-button-primary" :disabled="savePending">
        {{ savePending ? `${t('save')}...` : t('save') }}
      </button>
    </div>
  </form>

  <!-- 中栏表格模式只负责展示菜单节点列表，点击后把节点回填到右栏。 -->
  <section v-else class="seladmin-panel seladmin-surface selattendance-data-panel seluniauth-table-card seluniauth-module-list-panel">
    <div class="seluniauth-table-shell">
      <table class="seluniauth-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>{{ t('tableMenuCode') }}</th>
            <th>{{ t('moduleCode') }}</th>
            <th>{{ t('tableRoutePath') }}</th>
            <th>{{ t('tableTitleZh') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in menuRows" :key="row.id" @click="$emit('edit', row)">
            <td>{{ row.id }}</td>
            <td>{{ row.menuCode }}</td>
            <td>{{ row.moduleCode }}</td>
            <td>{{ row.routePath || '-' }}</td>
            <td>{{ row.titleZh || '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
