<script setup>
// 宿主桥接区块只负责展示 attendance 宿主已消费到的统一上下文，不主动发请求。
defineProps({
  // title 承接当前语言下的宿主桥接标题。
  title: { type: String, required: true },
  // lead 承接当前语言下的宿主桥接说明。
  lead: { type: String, required: true },
  // preview 承接格式化后的宿主上下文文本。
  preview: { type: String, required: true }
})
</script>

<template>
  <!-- 宿主桥接卡用于证明 attendance 和 uniauth 已经真正共用同一份 JWT 上下文。 -->
  <section class="seluniauth-side-panel seladmin-surface">
    <!-- 卡片头部先解释当前展示的是宿主上下文而不是权限中心本地状态。 -->
    <header class="seluniauth-side-panel-header">
      <h3>{{ title }}</h3>
      <p class="seladmin-copy">{{ lead }}</p>
    </header>

    <!-- 多行文本直接展示 JSON 快照，方便管理员和自动化截图核对 menuCodes、dataScopes。 -->
    <pre class="seluniauth-code-block">{{ preview }}</pre>
  </section>
</template>
