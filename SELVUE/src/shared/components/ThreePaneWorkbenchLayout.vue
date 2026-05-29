<script setup>
import { computed, useSlots } from 'vue'
import ResizableWorkbenchSplit from './ResizableWorkbenchSplit.vue'

const props = defineProps({
  outerStorageKey: { type: String, required: true },
  outerDefaultLeftPercent: { type: Number, default: 18 },
  outerMinLeftPercent: { type: Number, default: 10 },
  outerMaxLeftPercent: { type: Number, default: 28 },
  innerStorageKey: { type: String, default: 'shared-three-pane-inner' },
  innerDefaultLeftPercent: { type: Number, default: 68 },
  innerMinLeftPercent: { type: Number, default: 48 },
  innerMaxLeftPercent: { type: Number, default: 82 },
  collapseEmptySide: { type: Boolean, default: true }
})

const slots = useSlots()

// 共享三分栏只有在业务页真正提供右栏内容时才展开内层分栏，避免空壳侧栏白占一列。
const hasSideSlot = computed(() => Boolean(slots.side))

// 当调用方允许空右栏折叠时，三分栏退化成两栏壳层，保证旧页面接线时行为不变。
const useInnerSplit = computed(() => !props.collapseEmptySide || hasSideSlot.value)
</script>

<template>
  <ResizableWorkbenchSplit
    class="selshared-three-pane-layout"
    :storage-key="outerStorageKey"
    :default-left-percent="outerDefaultLeftPercent"
    :min-left-percent="outerMinLeftPercent"
    :max-left-percent="outerMaxLeftPercent"
  >
    <template #left>
      <div class="selshared-three-pane-slot selshared-three-pane-slot-left">
        <slot name="left" />
      </div>
    </template>

    <template #right>
      <ResizableWorkbenchSplit
        v-if="useInnerSplit"
        class="selshared-three-pane-layout selshared-three-pane-layout-inner"
        :storage-key="innerStorageKey"
        :default-left-percent="innerDefaultLeftPercent"
        :min-left-percent="innerMinLeftPercent"
        :max-left-percent="innerMaxLeftPercent"
      >
        <template #left>
          <div class="selshared-three-pane-slot selshared-three-pane-slot-main">
            <slot name="main" />
          </div>
        </template>

        <template #right>
          <div class="selshared-three-pane-slot selshared-three-pane-slot-side">
            <slot name="side" />
          </div>
        </template>
      </ResizableWorkbenchSplit>

      <div v-else class="selshared-three-pane-slot selshared-three-pane-slot-main">
        <slot name="main" />
      </div>
    </template>
  </ResizableWorkbenchSplit>
</template>

<style scoped>
.selshared-three-pane-layout {
  height: 100%;
  min-height: 0;
}

.selshared-three-pane-slot {
  height: 100%;
  min-height: 0;
  min-width: 0;
}

.selshared-three-pane-layout :deep(.selattendance-resizable-pane-left),
.selshared-three-pane-layout :deep(.selattendance-resizable-pane-right) {
  overflow: hidden;
}
</style>
