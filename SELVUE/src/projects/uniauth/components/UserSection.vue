<script setup>
// 用户区块只负责渲染账号表单和列表，不直接发起请求或维护全局会话。
defineProps({
  // userForm 承接当前账号编辑状态。
  userForm: { type: Object, required: true },
  // userRows 承接当前账号列表。
  userRows: { type: Array, required: true },
  // savePending 用于驱动保存按钮等待态。
  savePending: { type: Boolean, default: false },
  // t 用于读取当前语言字段文案。
  t: { type: Function, required: true }
})

// 用户区块只把保存和编辑动作抛给 workbench，让账号写入逻辑统一收口。
defineEmits(['submit', 'edit'])
</script>

<template>
  <!-- 用户区块继续沿用 attendance 的“表单 + 表格”双栏布局。 -->
  <section class="seluniauth-section-shell">
    <!-- 左栏负责维护账号主资料、角色绑定和语言偏好。 -->
    <form class="seluniauth-editor-card seladmin-surface" @submit.prevent="$emit('submit')">
      <header class="seluniauth-card-header">
        <h3>{{ t('userTitle') }}</h3>
        <p class="seladmin-copy">{{ t('userLead') }}</p>
      </header>

      <div class="seluniauth-form-grid">
        <label class="seladmin-field">
          <span>{{ t('tenantId') }}</span>
          <input v-model="userForm.tenantId" type="number" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('loginName') }}</span>
          <input v-model="userForm.loginName" type="text" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('password') }}</span>
          <input v-model="userForm.password" type="password" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('loginDisplayName') }}</span>
          <input v-model="userForm.displayName" type="text" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('displayNameKana') }}</span>
          <input v-model="userForm.displayNameKana" type="text" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('locale') }}</span>
          <select v-model="userForm.locale">
            <option value="zh-CN">zh-CN</option>
            <option value="ja-JP">ja-JP</option>
          </select>
        </label>
        <label class="seladmin-field">
          <span>{{ t('email') }}</span>
          <input v-model="userForm.email" type="email" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('phone') }}</span>
          <input v-model="userForm.phone" type="text" />
        </label>
        <label class="seladmin-field">
          <span>{{ t('userStatus') }}</span>
          <input v-model="userForm.userStatus" type="text" />
        </label>
        <label class="seladmin-field seluniauth-field-span-2">
          <span>{{ t('roleIds') }}</span>
          <input v-model="userForm.roleIdsText" type="text" placeholder="1,2" />
        </label>
      </div>

      <div class="seluniauth-action-row">
        <button type="submit" class="seladmin-button seladmin-button-primary" :disabled="savePending">
          {{ savePending ? `${t('save')}...` : t('save') }}
        </button>
      </div>
    </form>

    <!-- 右栏负责展示账号列表，点击后把当前账号回填到左栏继续修改。 -->
    <section class="seluniauth-table-card seladmin-surface">
      <header class="seluniauth-card-header">
        <h3>{{ t('summaryUser') }}</h3>
        <p class="seladmin-copy">{{ t('editHint') }}</p>
      </header>

      <div class="seluniauth-table-shell">
        <table class="seluniauth-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>{{ t('tableLoginName') }}</th>
              <th>{{ t('tableDisplayName') }}</th>
              <th>{{ t('tableLocale') }}</th>
              <th>{{ t('tenantCode') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in userRows" :key="row.id" @click="$emit('edit', row)">
              <td>{{ row.id }}</td>
              <td>{{ row.loginName }}</td>
              <td>{{ row.displayName }}</td>
              <td>{{ row.locale }}</td>
              <td>{{ row.tenantCode || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>
