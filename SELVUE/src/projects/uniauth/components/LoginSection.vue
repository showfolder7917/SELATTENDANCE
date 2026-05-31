<script setup>
// 登录区块只负责采集账号密码并把登录动作交给 workbench，不直接持有请求副作用。
defineProps({
  // title 承接当前语言下的登录标题。
  title: { type: String, required: true },
  // lead 承接当前语言下的登录引导说明。
  lead: { type: String, required: true },
  // loginForm 承接登录名和密码输入状态。
  loginForm: { type: Object, required: true },
  // loginPending 用于驱动登录按钮等待态。
  loginPending: { type: Boolean, default: false },
  // t 用于读取当前语言字段文案。
  t: { type: Function, required: true }
})

// 登录区块只向上通知提交动作，让 workbench 统一建立统一会话和刷新宿主上下文。
defineEmits(['submit'])
</script>

<template>
  <!-- 未登录时只展示登录卡，避免管理区块在没有统一会话时提前暴露。 -->
  <section class="seluniauth-login-card seladmin-surface">
    <!-- 标题区直接解释当前要先完成统一权限中心登录。 -->
    <header class="seluniauth-login-copy">
      <p class="seladmin-eyebrow">UniAuth / Login</p>
      <h2>{{ title }}</h2>
      <p class="seladmin-copy">{{ lead }}</p>
    </header>

    <!-- 表单区只承接登录名和密码两个最小闭环字段。 -->
    <div class="seluniauth-form-grid">
      <label class="seladmin-field">
        <span>{{ t('loginName') }}</span>
        <input v-model="loginForm.loginName" type="text" autocomplete="username" />
      </label>
      <label class="seladmin-field">
        <span>{{ t('password') }}</span>
        <input v-model="loginForm.password" type="password" autocomplete="current-password" />
      </label>
    </div>

    <!-- 登录按钮点击后由 workbench 发起统一登录、会话写入和首屏工作台初始化。 -->
    <div class="seluniauth-action-row">
      <button
        type="button"
        class="seladmin-button seladmin-button-primary"
        :disabled="loginPending"
        @click="$emit('submit')"
      >
        {{ loginPending ? `${t('signIn')}...` : t('signIn') }}
      </button>
    </div>
  </section>
</template>
