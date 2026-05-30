<script setup>
const props = defineProps({
  items: { type: Array, required: true }
})

// 共享统计卡只负责按统一视觉壳渲染指标，不参与任何业务计算或接口调用。
function resolveToneClass(tone) {
  const toneMap = {
    default: '',
    warm: 'is-warm',
    danger: 'is-danger',
    muted: 'is-muted'
  }
  return toneMap[tone] || ''
}
</script>

<template>
  <div class="shared-metric-cards" role="list" aria-label="summary metrics">
    <article
      v-for="(item, index) in items"
      :key="item.key || item.label || index"
      class="shared-metric-card"
      :class="resolveToneClass(item.tone)"
      role="listitem"
    >
      <!-- 指标值保留最醒目的视觉层级，让业务页不需要重复维护卡片字号和层次。 -->
      <strong>{{ item.value ?? 0 }}</strong>
      <small>{{ item.label }}</small>
    </article>
  </div>
</template>

<style scoped>
.shared-metric-cards {
  display: grid;
  width: 100%;
  grid-template-columns: repeat(var(--shared-metric-card-columns, 4), minmax(0, 1fr));
  gap: var(--shared-metric-card-gap, 12px);
}

.shared-metric-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(37, 55, 87, 0.48);
  border: 1px solid rgba(115, 144, 202, 0.22);
}

.shared-metric-card strong {
  font-size: 1.25rem;
}

.shared-metric-card small {
  color: rgba(218, 228, 255, 0.72);
}

.shared-metric-card.is-warm {
  background: rgba(109, 73, 34, 0.38);
}

.shared-metric-card.is-danger {
  background: rgba(115, 42, 42, 0.38);
}

.shared-metric-card.is-muted {
  background: rgba(74, 80, 97, 0.34);
}

@media (max-width: 900px) {
  .shared-metric-cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .shared-metric-cards {
    grid-template-columns: 1fr;
  }
}
</style>
