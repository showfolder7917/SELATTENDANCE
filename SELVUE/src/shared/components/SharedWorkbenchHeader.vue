<script setup>
import { computed, useSlots } from 'vue'

const props = defineProps({
  title: { type: String, required: true },
  lead: { type: String, default: '' },
  eyebrow: { type: String, default: '' },
  splitMode: {
    type: String,
    default: 'stacked',
    validator: (value) => ['stacked', 'left-summary-right-metrics'].includes(value)
  }
})

const slots = useSlots()

// 共享头部壳只负责判断哪些业务区块要显示，具体内容仍交给调用方插槽提供。
const hasMetrics = computed(() => Boolean(slots.metrics))
const hasFilters = computed(() => Boolean(slots.filters))
const hasActions = computed(() => Boolean(slots.actions))
</script>

<template>
  <section
    class="shared-workbench-header"
    :class="{
      'split-stacked': splitMode === 'stacked',
      'split-copy-metrics': splitMode === 'left-summary-right-metrics'
    }"
  >
    <div class="shared-workbench-header-topline">
      <div class="shared-workbench-header-copy">
        <p v-if="eyebrow" class="seladmin-eyebrow">{{ eyebrow }}</p>
        <h2>{{ title }}</h2>
        <p v-if="lead" class="seladmin-copy">{{ lead }}</p>
      </div>

      <!-- 统计卡区始终由业务页自己提供内容，shared 只承接左右布局和响应式壳层。 -->
      <div v-if="hasMetrics" class="shared-workbench-header-metrics">
        <slot name="metrics" />
      </div>
    </div>

    <!-- 筛选区与动作区保持可选，避免为了适配单个模块把所有 props 都写死在组件上。 -->
    <div v-if="hasFilters || hasActions" class="shared-workbench-header-bottom">
      <div v-if="hasFilters" class="shared-workbench-header-filters">
        <slot name="filters" />
      </div>
      <div v-if="hasActions" class="shared-workbench-header-actions">
        <slot name="actions" />
      </div>
    </div>
  </section>
</template>

<style scoped>
.shared-workbench-header {
  display: grid;
  justify-content: stretch;
  align-items: stretch;
  gap: 18px;
  padding: 22px 24px;
  border-radius: 20px;
}

.shared-workbench-header-topline {
  display: grid;
  width: 100%;
  gap: 18px;
  align-items: stretch;
}

.shared-workbench-header.split-stacked .shared-workbench-header-topline {
  grid-template-columns: 1fr;
}

.shared-workbench-header.split-copy-metrics .shared-workbench-header-topline {
  grid-template-columns:
    minmax(var(--shared-workbench-copy-min, 0), var(--shared-workbench-copy-max, 1fr))
    minmax(var(--shared-workbench-metrics-min, 0), var(--shared-workbench-metrics-max, 1fr));
}

.shared-workbench-header-copy {
  display: grid;
  align-content: center;
  gap: 8px;
}

.shared-workbench-header-copy h2 {
  margin: 6px 0 8px;
}

.shared-workbench-header-copy .seladmin-copy {
  margin: 0;
}

.shared-workbench-header-metrics,
.shared-workbench-header-filters,
.shared-workbench-header-actions {
  min-width: 0;
  width: 100%;
}

.shared-workbench-header-bottom {
  display: grid;
  gap: 14px;
}

@media (max-width: 1180px) {
  .shared-workbench-header.split-copy-metrics .shared-workbench-header-topline {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .shared-workbench-header {
    padding: 18px 16px;
  }
}
</style>
