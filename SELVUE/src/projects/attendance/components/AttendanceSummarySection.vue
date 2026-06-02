<script setup>
import { computed } from 'vue'

// 首页汇总区块只承接步骤统计展示，不拥有任何写状态。
const props = defineProps({
  steps: { type: Array, required: true },
  recommendedNextKey: { type: String, required: true },
  recommendedNextLabel: { type: String, required: true },
  t: { type: Function, required: true }
})

// 向导总览先在卡片顶部汇总完成度，帮助管理员快速判断当前处于哪个准备阶段。
const completedCount = computed(() => props.steps.filter((step) => step.status === 'COMPLETED').length)
// “待处理”只统计真正还需要当前阶段介入的步骤，不把下一阶段锁定位算进去。
const actionableCount = computed(() => props.steps.filter((step) => step.status === 'PENDING').length)
// 锁定步骤单独统计，用户能一眼区分“没做”和“尚未开放”。
const lockedCount = computed(() => props.steps.filter((step) => step.status === 'LOCKED_NEXT_PHASE').length)
// 进度条百分比统一按当前总步骤计算，避免后续步骤增减时模板里重复写公式。
const progressPercent = computed(() => {
  if (!props.steps.length) {
    return 0
  }
  return Math.round((completedCount.value / props.steps.length) * 100)
})
// 顶部焦点卡只突出当前推荐动作，避免和下方完整步骤列表重复堆叠同一批信息。
const highlightedStep = computed(
  () => props.steps.find((step) => step.titleKey === props.recommendedNextKey) || props.steps[0] || null
)
// 总览区仍保留阶段轨道，但压缩成轻量胶囊，承担“扫一眼全局”的作用。
const roadmapSteps = computed(() =>
  props.steps.map((step, index) => ({
    ...step,
    order: index + 1,
    statusLabel:
      step.status === 'COMPLETED'
        ? props.t('statusCompleted')
        : step.status === 'LOCKED_NEXT_PHASE'
          ? props.t('phaseLocked')
          : props.t('statusNeedsAction')
  }))
)
</script>

<template>
  <section class="seladmin-panel seladmin-surface seladmin-surface-accent selattendance-wizard-summary">
    <div class="selattendance-wizard-summary-hero">
      <div class="selattendance-wizard-summary-copy">
        <p class="seladmin-eyebrow">{{ t('wizardOverviewTag') }}</p>
        <h2>{{ t('wizardTitle') }}</h2>
        <p class="seladmin-copy">{{ t('sectionWizardHint') }}</p>
        <p class="seladmin-copy">{{ t('wizardHint') }}</p>
      </div>
      <div class="selattendance-wizard-summary-progress">
        <span class="selattendance-wizard-summary-progress-label">{{ t('wizardProgressLabel') }}</span>
        <strong>{{ progressPercent }}%</strong>
        <small>{{ t('wizardStepCount').replace('{done}', String(completedCount)).replace('{total}', String(steps.length)) }}</small>
      </div>
    </div>

    <div class="selattendance-wizard-summary-metrics">
      <article class="selattendance-wizard-summary-metric">
        <span>{{ t('statusCompleted') }}</span>
        <strong>{{ completedCount }}</strong>
      </article>
      <article class="selattendance-wizard-summary-metric">
        <span>{{ t('wizardActionableCount') }}</span>
        <strong>{{ actionableCount }}</strong>
      </article>
      <article class="selattendance-wizard-summary-metric">
        <span>{{ t('wizardLockedCount') }}</span>
        <strong>{{ lockedCount }}</strong>
      </article>
    </div>

    <div class="selattendance-wizard-summary-focus">
      <div>
        <p class="seladmin-eyebrow">{{ t('wizardFocusTitle') }}</p>
        <strong>{{ recommendedNextLabel }}</strong>
        <p v-if="highlightedStep" class="seladmin-copy">{{ t(highlightedStep.description) }}</p>
      </div>
      <span class="seladmin-chip">{{ t('nextAction') }}</span>
    </div>

    <div class="selattendance-wizard-summary-roadmap">
      <div class="selattendance-wizard-summary-roadmap-head">
        <div>
          <p class="seladmin-eyebrow">{{ t('wizardRoadmapTitle') }}</p>
          <p class="seladmin-copy">{{ t('wizardRoadmapHint') }}</p>
        </div>
      </div>
      <div class="selattendance-wizard-summary-roadmap-grid">
        <article
          v-for="step in roadmapSteps"
          :key="step.stepCode"
          class="selattendance-wizard-summary-roadmap-item"
          :class="{
            'is-completed': step.status === 'COMPLETED',
            'is-pending': step.status === 'PENDING',
            'is-locked': step.status === 'LOCKED_NEXT_PHASE',
            'is-highlighted': step.titleKey === recommendedNextKey
          }"
        >
          <span class="selattendance-wizard-summary-roadmap-order">{{ step.order }}</span>
          <div>
            <strong>{{ t(step.titleKey) }}</strong>
            <small>{{ step.statusLabel }}</small>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.selattendance-wizard-summary {
  display: grid;
  gap: 18px;
}

.selattendance-wizard-summary-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(220px, 0.7fr);
  gap: 16px;
  align-items: stretch;
}

.selattendance-wizard-summary-copy {
  display: grid;
  gap: 10px;
}

.selattendance-wizard-summary-copy h2 {
  margin: 0;
}

.selattendance-wizard-summary-progress {
  display: grid;
  gap: 8px;
  align-content: center;
  padding: 18px 20px;
  border-radius: 22px;
  border: 1px solid rgba(115, 144, 202, 0.24);
  background: linear-gradient(180deg, rgba(40, 57, 94, 0.46), rgba(18, 28, 49, 0.78));
}

.selattendance-wizard-summary-progress-label {
  font-size: 0.82rem;
  color: var(--seladmin-text-muted, rgba(255, 255, 255, 0.7));
}

.selattendance-wizard-summary-progress strong {
  font-size: clamp(2rem, 3vw, 2.8rem);
  line-height: 1;
}

.selattendance-wizard-summary-progress small {
  color: var(--seladmin-text-muted, rgba(255, 255, 255, 0.72));
}

.selattendance-wizard-summary-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.selattendance-wizard-summary-metric,
.selattendance-wizard-summary-focus,
.selattendance-wizard-summary-roadmap-item {
  padding: 16px 18px;
  border-radius: 20px;
  border: 1px solid rgba(115, 144, 202, 0.18);
  background: rgba(18, 28, 49, 0.54);
}

.selattendance-wizard-summary-metric {
  display: grid;
  gap: 8px;
}

.selattendance-wizard-summary-metric span {
  color: var(--seladmin-text-muted, rgba(255, 255, 255, 0.68));
}

.selattendance-wizard-summary-metric strong {
  font-size: 1.6rem;
}

.selattendance-wizard-summary-focus {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.selattendance-wizard-summary-focus > div {
  display: grid;
  gap: 6px;
}

.selattendance-wizard-summary-roadmap {
  display: grid;
  gap: 12px;
}

.selattendance-wizard-summary-roadmap-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.selattendance-wizard-summary-roadmap-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.selattendance-wizard-summary-roadmap-item {
  display: flex;
  align-items: center;
  gap: 14px;
}

.selattendance-wizard-summary-roadmap-order {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  font-weight: 700;
}

.selattendance-wizard-summary-roadmap-item strong,
.selattendance-wizard-summary-roadmap-item small {
  display: block;
}

.selattendance-wizard-summary-roadmap-item small {
  margin-top: 4px;
  color: var(--seladmin-text-muted, rgba(255, 255, 255, 0.7));
}

.selattendance-wizard-summary-roadmap-item.is-highlighted {
  border-color: rgba(124, 167, 255, 0.42);
  background: linear-gradient(180deg, rgba(52, 77, 127, 0.42), rgba(22, 33, 56, 0.86));
}

.selattendance-wizard-summary-roadmap-item.is-completed .selattendance-wizard-summary-roadmap-order {
  background: rgba(94, 179, 126, 0.22);
}

.selattendance-wizard-summary-roadmap-item.is-pending .selattendance-wizard-summary-roadmap-order {
  background: rgba(255, 182, 77, 0.2);
}

.selattendance-wizard-summary-roadmap-item.is-locked .selattendance-wizard-summary-roadmap-order {
  background: rgba(146, 155, 180, 0.18);
}

@media (max-width: 960px) {
  .selattendance-wizard-summary-hero,
  .selattendance-wizard-summary-metrics,
  .selattendance-wizard-summary-roadmap-grid {
    grid-template-columns: 1fr;
  }

  .selattendance-wizard-summary-focus {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
