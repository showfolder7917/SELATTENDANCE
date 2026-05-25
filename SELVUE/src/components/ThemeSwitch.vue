<script setup>
// 主题切换器只负责展示当前可选主题和发出切换事件，不直接加载 CSS。
defineProps({
  modelValue: {
    type: String,
    required: true
  },
  options: {
    type: Array,
    required: true
  },
  label: {
    type: String,
    required: true
  },
  t: {
    type: Function,
    required: true
  }
})

const emit = defineEmits(['update:modelValue'])
</script>

<template>
  <div class="selattendance-theme-switch">
    <p class="selattendance-theme-label">{{ label }}</p>
    <div class="seladmin-locale-switch" role="group" :aria-label="label">
      <button
        v-for="option in options"
        :key="option.value"
        type="button"
        class="seladmin-locale-button seladmin-surface"
        :class="{ active: option.value === modelValue }"
        @click="emit('update:modelValue', option.value)"
      >
        <span class="seladmin-locale-label">{{ t(option.labelKey) }}</span>
        <span class="seladmin-locale-caption">{{ option.short }}</span>
      </button>
    </div>
  </div>
</template>
