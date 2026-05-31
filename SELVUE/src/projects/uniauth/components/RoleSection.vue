<script setup>
// 角色区块只负责渲染角色主资料和授权表达输入，不直接维护任何请求副作用。
defineProps({
  // roleForm 承接当前角色编辑状态。
  roleForm: { type: Object, required: true },
  // roleRows 承接当前角色列表。
  roleRows: { type: Array, required: true },
  // savePending 用于驱动保存按钮等待态。
  savePending: { type: Boolean, default: false },
  // t 用于读取当前语言字段文案。
  t: { type: Function, required: true }
})

// 角色区块把保存和编辑动作交给 workbench，避免组件层知道角色接口细节。
defineEmits(['submit', 'edit'])
</script>

<template>
  <!-- 角色区块继续采用左编辑右列表布局，让授权编辑和角色浏览保持同屏。 -->
  <section class="seluniauth-section-shell">
    <!-- 左栏表单负责角色主资料、权限码、菜单码和数据范围维护。 -->
    <form class="seluniauth-editor-card seladmin-surface" @submit.prevent="$emit('submit')">
      <header class="seluniauth-card-header">
        <h3>{{ t('roleTitle') }}</h3>
        <p class="seladmin-copy">{{ t('roleLead') }}</p>
      </header>

      <div class="seluniauth-form-grid">
        <label class="seladmin-field">
          <span>{{ t('tenantId') }}</span>
          <input v-model="roleForm.tenantId" type="number" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('roleCode') }}</span>
          <input v-model="roleForm.roleCode" type="text" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('roleName') }}</span>
          <input v-model="roleForm.roleName" type="text" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('roleDesc') }}</span>
          <input v-model="roleForm.roleDesc" type="text" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('roleStatus') }}</span>
          <input v-model="roleForm.roleStatus" type="text" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('dataScopeType') }}</span>
          <input v-model="roleForm.dataScopeType" type="text" />
        </label>
        <label class="seladmin-field seluniauth-field-span-2">
          <span>{{ t('permissionCodes') }}</span>
          <textarea v-model="roleForm.permissionCodesText" rows="4" />
        </label>
        <label class="seladmin-field seluniauth-field-span-2">
          <span>{{ t('menuCodes') }}</span>
          <textarea v-model="roleForm.menuCodesText" rows="4" />
        </label>
        <label class="seladmin-field seluniauth-field-span-2">
          <span>{{ t('dataScopeValue') }}</span>
          <input v-model="roleForm.dataScopeValue" type="text" />
        </label>
      </div>

      <div class="seluniauth-action-row">
        <button type="submit" class="seladmin-button seladmin-button-primary" :disabled="savePending">
          {{ savePending ? `${t('save')}...` : t('save') }}
        </button>
      </div>
    </form>

    <!-- 右栏表格负责列出当前角色，点击后继续回填授权表达。 -->
    <section class="seluniauth-table-card seladmin-surface">
      <header class="seluniauth-card-header">
        <h3>{{ t('summaryRole') }}</h3>
        <p class="seladmin-copy">{{ t('editHint') }}</p>
      </header>

      <div class="seluniauth-table-shell">
        <table class="seluniauth-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>{{ t('tableRoleCode') }}</th>
              <th>{{ t('tableRoleName') }}</th>
              <th>{{ t('tableRoleDesc') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in roleRows" :key="row.id" @click="$emit('edit', row)">
              <td>{{ row.id }}</td>
              <td>{{ row.roleCode }}</td>
              <td>{{ row.roleName }}</td>
              <td>{{ row.roleDesc || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>
