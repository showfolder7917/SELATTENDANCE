<script setup>
import { computed } from 'vue'

const props = defineProps({
  visible: { type: Boolean, required: true },
  steps: { type: Array, required: true },
  recommendedNextKey: { type: String, required: true },
  t: { type: Function, required: true }
})

// 详细步骤区块负责把每一步的状态、说明和当前数量整理成统一卡片，避免列表信息左右割裂。
const detailedSteps = computed(() =>
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
  <section v-show="visible" class="seladmin-panel seladmin-surface selattendance-wizard-detail">
    <div class="selattendance-wizard-detail-head">
      <div>
        <p class="seladmin-eyebrow">{{ t('wizardDetailTitle') }}</p>
        <h3>{{ t('wizardRoadmapTitle') }}</h3>
        <p class="seladmin-copy">{{ t('wizardDetailHint') }}</p>
      </div>
    </div>
    <div class="selattendance-wizard-detail-grid">
      <article
        v-for="step in detailedSteps"
        :key="step.stepCode"
        class="selattendance-wizard-detail-card"
        :class="{
          'is-completed': step.status === 'COMPLETED',
          'is-pending': step.status === 'PENDING',
          'is-locked': step.status === 'LOCKED_NEXT_PHASE',
          'is-highlighted': step.titleKey === recommendedNextKey
        }"
      >
        <div class="selattendance-wizard-detail-card-head">
          <span class="selattendance-wizard-detail-order">{{ step.order }}</span>
          <span class="seladmin-chip">{{ step.statusLabel }}</span>
        </div>
        <div class="selattendance-wizard-detail-card-body">
          <strong>{{ t(step.titleKey) }}</strong>
          <p class="seladmin-support-text">{{ t(step.description) }}</p>
        </div>
        <div class="selattendance-wizard-detail-card-foot">
          <small>{{ t('wizardCountLabel') }}</small>
          <strong>{{ step.count }}</strong>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.selattendance-wizard-detail {
  display: grid;
  gap: 18px;
}

.selattendance-wizard-detail-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.selattendance-wizard-detail-head h3 {
  margin: 0;
}

.selattendance-wizard-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.selattendance-wizard-detail-card {
  display: grid;
  gap: 14px;
  min-height: 184px;
  padding: 18px;
  border-radius: 22px;
  border: 1px solid rgba(115, 144, 202, 0.16);
  background: rgba(17, 26, 45, 0.42);
}

.selattendance-wizard-detail-card-head,
.selattendance-wizard-detail-card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.selattendance-wizard-detail-order {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.08);
  font-weight: 700;
}

.selattendance-wizard-detail-card-body {
  display: grid;
  gap: 10px;
}

.selattendance-wizard-detail-card-body strong {
  font-size: 1.06rem;
}

.selattendance-wizard-detail-card-foot small {
  color: var(--seladmin-text-muted, rgba(255, 255, 255, 0.7));
}

.selattendance-wizard-detail-card-foot strong {
  font-size: 1.5rem;
}

.selattendance-wizard-detail-card.is-highlighted {
  border-color: rgba(124, 167, 255, 0.42);
  background: linear-gradient(180deg, rgba(52, 77, 127, 0.34), rgba(16, 25, 45, 0.88));
}

.selattendance-wizard-detail-card.is-completed .selattendance-wizard-detail-order {
  background: rgba(94, 179, 126, 0.22);
}

.selattendance-wizard-detail-card.is-pending .selattendance-wizard-detail-order {
  background: rgba(255, 182, 77, 0.2);
}

.selattendance-wizard-detail-card.is-locked .selattendance-wizard-detail-order {
  background: rgba(146, 155, 180, 0.18);
}

@media (max-width: 960px) {
  .selattendance-wizard-detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
