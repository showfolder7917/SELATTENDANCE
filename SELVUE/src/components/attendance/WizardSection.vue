<script setup>
defineProps({
  visible: { type: Boolean, required: true },
  steps: { type: Array, required: true },
  t: { type: Function, required: true }
})
</script>

<template>
  <section v-show="visible" class="seladmin-panel seladmin-surface seladmin-list">
    <div class="seladmin-panel-header"><h2>{{ t('wizardTitle') }}</h2></div>
    <article v-for="step in steps" :key="step.stepCode" class="seladmin-list-row">
      <div>
        <p class="seladmin-list-title">{{ t(step.titleKey) }}</p>
        <p class="seladmin-support-text">{{ t(step.description) }}</p>
      </div>
      <div class="seladmin-list-meta">
        <strong>{{ step.count }}</strong>
        <span class="seladmin-chip">{{ step.status === 'COMPLETED' ? t('statusCompleted') : step.status === 'LOCKED_NEXT_PHASE' ? t('phaseLocked') : t('statusNeedsAction') }}</span>
      </div>
    </article>
  </section>
</template>

