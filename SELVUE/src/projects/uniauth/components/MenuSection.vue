<script setup>
// 菜单区块只负责渲染动态菜单节点表单和列表，不直接知道宿主请求细节。
defineProps({
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
  <!-- 菜单区块沿用统一双栏布局，左栏改入口节点，右栏看现有菜单树明细。 -->
  <section class="seluniauth-section-shell">
    <!-- 左栏表单负责模块编码、菜单编码、路由和双语标题维护。 -->
    <form class="seluniauth-editor-card seladmin-surface" @submit.prevent="$emit('submit')">
      <header class="seluniauth-card-header">
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

    <!-- 右栏表格负责列出已有菜单节点，点击后把当前节点回填到左栏继续修改。 -->
    <section class="seluniauth-table-card seladmin-surface">
      <header class="seluniauth-card-header">
        <h3>{{ t('summaryMenu') }}</h3>
        <p class="seladmin-copy">{{ t('editHint') }}</p>
      </header>

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
  </section>
</template>
