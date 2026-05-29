<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps({
  storageKey: { type: String, required: true },
  defaultLeftPercent: { type: Number, default: 60 },
  minLeftPercent: { type: Number, default: 42 },
  maxLeftPercent: { type: Number, default: 72 }
})

// 左右分栏比例决定业务列表与编辑表单的主次关系，默认让左侧列表占更大空间。
const leftPercent = ref(props.defaultLeftPercent)
// 拖拽仅在桌面宽度下开启，移动端仍回到上下堆叠布局。
const dragging = ref(false)
// 容器宽度用于把鼠标位置换算成百分比，而不是写死像素。
const splitRoot = ref(null)

// 百分比需要始终落在业务允许的范围内，避免把表格或表单拖到不可用宽度。
const clampPercent = (value) => Math.min(props.maxLeftPercent, Math.max(props.minLeftPercent, value))

// 保存比例时使用工作台模块自己的 key，避免不同模块互相覆盖拖拽结果。
const persistPercent = (value) => {
  window.localStorage.setItem(props.storageKey, String(value))
}

// 鼠标移动时根据容器左边界和总宽度实时更新左栏比例。
const updatePercentFromClientX = (clientX) => {
  if (!splitRoot.value) return
  const bounds = splitRoot.value.getBoundingClientRect()
  if (!bounds.width) return
  const nextPercent = clampPercent(((clientX - bounds.left) / bounds.width) * 100)
  leftPercent.value = nextPercent
}

// 拖拽过程中持续追随鼠标位置，让列表区和表单区宽度同步变化。
const handlePointerMove = (event) => {
  if (!dragging.value) return
  updatePercentFromClientX(event.clientX)
}

// 放开鼠标后收口本次拖拽，并把最终比例写回本地缓存。
const stopDrag = () => {
  if (!dragging.value) return
  dragging.value = false
  persistPercent(leftPercent.value)
  window.removeEventListener('pointermove', handlePointerMove)
  window.removeEventListener('pointerup', stopDrag)
}

// 只有用户按下拖拽柄时才进入拖拽态，避免正常点击表格或表单时误触。
const startDrag = (event) => {
  if (window.innerWidth <= 1180) return
  dragging.value = true
  updatePercentFromClientX(event.clientX)
  window.addEventListener('pointermove', handlePointerMove)
  window.addEventListener('pointerup', stopDrag)
}

// 首次进入模块时优先恢复上次拖拽比例，让同一用户保持自己的工作习惯。
onMounted(() => {
  const cachedValue = Number(window.localStorage.getItem(props.storageKey) || '')
  if (!Number.isNaN(cachedValue) && cachedValue > 0) {
    leftPercent.value = clampPercent(cachedValue)
  }
})

// 组件销毁时兜底清理全局拖拽监听，避免切页后残留事件。
onBeforeUnmount(() => {
  stopDrag()
})

// 分栏比例通过 CSS 变量下发给容器，模板层不用重复拼接多个 style 字段。
const splitStyle = computed(() => ({
  '--selattendance-left-pane-percent': `${leftPercent.value}%`
}))
</script>

<template>
  <section ref="splitRoot" class="selattendance-resizable-split" :class="{ dragging }" :style="splitStyle">
    <div class="selattendance-resizable-pane selattendance-resizable-pane-left">
      <slot name="left" />
    </div>

    <button
      type="button"
      class="selattendance-resize-handle"
      :aria-label="`resize ${storageKey}`"
      @pointerdown.prevent="startDrag"
    >
      <span />
      <span />
    </button>

    <div class="selattendance-resizable-pane selattendance-resizable-pane-right">
      <slot name="right" />
    </div>
  </section>
</template>
