<script setup>
// 侧边导航只负责当前区块切换，不直接承接任何业务动作。
defineProps({
  navItems: { type: Array, required: true },
  activeSection: { type: String, required: true },
  title: { type: String, default: '' }
})

const emit = defineEmits(['update:activeSection'])
</script>

<template>
  <nav class="selattendance-section-menu" :aria-label="title || 'section navigation'">
    <button
      v-for="item in navItems"
      :key="item.key"
      type="button"
      class="selattendance-section-button"
      :class="{ active: activeSection === item.key }"
      @click="emit('update:activeSection', item.key)"
    >
      <span class="selattendance-section-copy">
        <strong>{{ item.label }}</strong>
        <small v-if="item.caption">{{ item.caption }}</small>
      </span>
      <span class="selattendance-section-badge">{{ item.badge }}</span>
    </button>
  </nav>
</template>
