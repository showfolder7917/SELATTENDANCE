<script setup>
defineProps({
  modelValue: {
    type: String,
    required: true
  },
  options: {
    type: Array,
    required: true
  }
})

const emit = defineEmits(['update:modelValue'])
</script>

<template>
  <div class="seladmin-locale-switch" role="group" aria-label="language switch">
    <button
      v-for="option in options"
      :key="option.value"
      type="button"
      class="seladmin-locale-button seladmin-surface"
      :class="{ active: option.value === modelValue }"
      @click="emit('update:modelValue', option.value)"
    >
      <span class="seladmin-locale-label">{{ option.label }}</span>
      <span class="seladmin-locale-caption">{{ option.short }}</span>
    </button>
  </div>
</template>
