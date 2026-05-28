<script setup>
import { computed } from 'vue'

const props = defineProps({
  open: { type: Boolean, required: true },
  title: { type: String, required: true },
  message: { type: String, required: true },
  confirmLabel: { type: String, required: true },
  cancelLabel: { type: String, required: true },
  confirmVariant: { type: String, default: 'primary' }
})

const emit = defineEmits(['cancel', 'confirm'])

// 通用确认组件只区分危险确认和普通确认，让删除、复制、覆盖都能复用同一套骨架。
const confirmButtonClass = computed(() =>
  props.confirmVariant === 'danger'
    ? 'seladmin-button selattendance-confirm-danger'
    : 'seladmin-button seladmin-button-primary'
)
</script>

<template>
  <div v-if="open" class="selattendance-confirm-overlay" @click.self="emit('cancel')">
    <section
      class="selattendance-confirm-dialog seladmin-surface"
      role="alertdialog"
      aria-modal="true"
      :aria-label="title"
    >
      <h3>{{ title }}</h3>
      <p class="seladmin-copy">{{ message }}</p>
      <div class="selattendance-confirm-actions">
        <button type="button" class="seladmin-button seladmin-button-secondary" @click="emit('cancel')">
          {{ cancelLabel }}
        </button>
        <button type="button" :class="confirmButtonClass" @click="emit('confirm')">
          {{ confirmLabel }}
        </button>
      </div>
    </section>
  </div>
</template>
