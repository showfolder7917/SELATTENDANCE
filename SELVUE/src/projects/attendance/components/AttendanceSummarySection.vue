<script setup>
// 首页汇总区块只承接步骤统计展示，不拥有任何写状态。
defineProps({
  steps: { type: Array, required: true },
  recommendedNextLabel: { type: String, required: true },
  t: { type: Function, required: true }
})
</script>

<template>
  <section class="seladmin-panel seladmin-surface seladmin-surface-accent">
    <div class="seladmin-panel-header">
      <div>
        <p class="seladmin-eyebrow">{{ t('nextAction') }}</p>
        <h2>{{ t('wizardTitle') }}</h2>
      </div>
      <span class="seladmin-chip">{{ recommendedNextLabel }}</span>
    </div>
    <p class="seladmin-copy">{{ t('sectionWizardHint') }}</p>
    <p class="seladmin-copy">{{ t('wizardHint') }}</p>
    <div class="seladmin-kpi-grid">
      <article v-for="step in steps" :key="step.stepCode" class="seladmin-kpi-card seladmin-surface">
        <div class="seladmin-kpi-row">
          <span class="seladmin-kpi-title">{{ t(step.titleKey) }}</span>
          <span class="seladmin-chip">
            {{ step.status === 'COMPLETED' ? t('statusCompleted') : step.status === 'LOCKED_NEXT_PHASE' ? t('phaseLocked') : t('statusNeedsAction') }}
          </span>
        </div>
        <p class="seladmin-copy">{{ t(step.description) }}</p>
        <strong class="seladmin-kpi-value">{{ step.count }}</strong>
      </article>
    </div>
  </section>
</template>
