<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    required: true
  }
})
</script>

<template>
  <div class="seladmin-empty">
    <div class="seladmin-empty-icon">+</div>
    <div>
      <p class="seladmin-empty-title">{{ title }}</p>
      <p class="seladmin-empty-description">{{ description }}</p>
    </div>
  </div>
</template>
