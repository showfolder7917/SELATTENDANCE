<script setup>
// 权限中心侧边导航只负责区块切换，不直接承接任何保存或刷新动作。
defineProps({
  // navItems 承接四个正式管理域的标签、说明和角标。
  navItems: { type: Array, required: true },
  // activeSection 用于高亮当前激活的管理域。
  activeSection: { type: String, required: true },
  // title 用于补齐导航区域的 aria 标签。
  title: { type: String, default: '' }
})

// 侧边导航只向上抛出区块切换事件，让 workbench 统一维护当前激活模块。
const emit = defineEmits(['update:activeSection'])
</script>

<template>
  <!-- 导航列表沿用 attendance 的按钮节奏，让两个工程的模块切换手感保持一致。 -->
  <nav class="selattendance-section-menu" :aria-label="title || 'uniauth section navigation'">
    <!-- 每个按钮只代表一个正式管理域，点击后把 key 回传给 workbench。 -->
    <button
      v-for="item in navItems"
      :key="item.key"
      type="button"
      class="selattendance-section-button"
      :class="{ active: activeSection === item.key }"
      @click="emit('update:activeSection', item.key)"
    >
      <!-- 左侧文案列同时展示模块名和当前管理域的职责说明。 -->
      <span class="selattendance-section-copy">
        <strong>{{ item.label }}</strong>
        <small v-if="item.caption">{{ item.caption }}</small>
      </span>
      <!-- 右侧角标直接展示当前模块统计值，帮助管理员先看规模再切换。 -->
      <span class="selattendance-section-badge">{{ item.badge }}</span>
    </button>
  </nav>
</template>
