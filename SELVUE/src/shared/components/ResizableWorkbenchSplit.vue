<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { workbenchViewportBreakpoints } from '../constants/workbenchLayoutConfig'

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
// 当前是否已经进入共享单列堆叠档位单独收口，避免 JS 和 CSS 各自手写同一个断点。
const stackedViewport = ref(false)
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

// 所有工程的分栏退化时机统一从 shared 断点入口读取，不再在组件里手写 1180。
const syncStackedViewport = () => {
  stackedViewport.value = window.innerWidth <= workbenchViewportBreakpoints.splitStackedMaxWidth
}

// 只有用户按下拖拽柄时才进入拖拽态，避免正常点击表格或表单时误触。
const startDrag = (event) => {
  if (stackedViewport.value) return
  dragging.value = true
  updatePercentFromClientX(event.clientX)
  window.addEventListener('pointermove', handlePointerMove)
  window.addEventListener('pointerup', stopDrag)
}

// 首次进入模块时优先恢复上次拖拽比例，让同一用户保持自己的工作习惯。
onMounted(() => {
  syncStackedViewport()
  const cachedValue = Number(window.localStorage.getItem(props.storageKey) || '')
  if (!Number.isNaN(cachedValue) && cachedValue > 0) {
    leftPercent.value = clampPercent(cachedValue)
  }
  window.addEventListener('resize', syncStackedViewport)
})

// 组件销毁时兜底清理全局拖拽监听，避免切页后残留事件。
onBeforeUnmount(() => {
  stopDrag()
  window.removeEventListener('resize', syncStackedViewport)
})

// 分栏比例通过 CSS 变量下发给容器，模板层不用重复拼接多个 style 字段。
const splitStyle = computed(() => ({
  '--selattendance-left-pane-percent': `${leftPercent.value}%`
}))
</script>

<template>
  <section
    ref="splitRoot"
    class="selattendance-resizable-split"
    :class="{ dragging, 'is-stacked': stackedViewport }"
    :style="splitStyle"
  >
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

<style scoped>
/* 共享分栏根壳统一提供桌面横向拉伸能力，避免业务页用了组件却还要另抄一份基础布局样式。 */
.selattendance-resizable-split {
  display: flex;
  align-items: stretch;
  gap: 0;
  min-width: 0;
  min-height: 0;
  height: 100%;
}

/* 拖拽中统一切到列缩放光标，并禁止选中文字，减少表格与表单被误选中的干扰。 */
.selattendance-resizable-split.dragging {
  cursor: col-resize;
  user-select: none;
}

/* 左右面板都必须允许自身压缩和内部滚动，否则共享分栏会被子内容反向撑爆。 */
.selattendance-resizable-pane {
  min-width: 0;
  min-height: 0;
  height: 100%;
}

/* 左侧面板的真实宽度始终由共享百分比变量驱动，供不同业务页在宽屏下按比例伸缩。 */
.selattendance-resizable-pane-left {
  flex: 0 0 var(--selattendance-left-pane-percent, 60%);
  max-width: var(--selattendance-left-pane-percent, 60%);
}

/* 右侧面板吃掉剩余空间，让主表格或右栏随容器扩大继续扩展。 */
.selattendance-resizable-pane-right {
  flex: 1 1 auto;
}

/* 中间拖拽柄由 shared 层统一提供，后续业务页只需要覆写局部高度和色彩节奏。 */
.selattendance-resize-handle {
  flex: 0 0 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  border: 0;
  padding: 0;
  margin: 0 6px;
  background: transparent;
  cursor: col-resize;
}

/* 双竖条视觉固定表达“这里可拖拽”，避免不同页面再各自发明 handle 造型。 */
.selattendance-resize-handle span {
  width: 3px;
  height: min(72vh, 840px);
  border-radius: 999px;
  background: rgba(117, 135, 156, 0.34);
  transition: background 160ms ease, transform 160ms ease;
}

/* hover 和 dragging 状态统一增强可见度，让共享分栏在深色主题里更容易被发现。 */
.selattendance-resize-handle:hover span,
.selattendance-resizable-split.dragging .selattendance-resize-handle span {
  background: rgba(87, 131, 216, 0.58);
  transform: scaleY(1.04);
}

/* 进入 shared 单列堆叠档位后，分栏直接退化成一列，避免在窄屏继续强行维持拖拽布局。 */
.selattendance-resizable-split.is-stacked {
  display: grid;
  grid-template-columns: 1fr;
  gap: 18px;
}

/* 堆叠模式下取消左右 pane 的固定宽度，让每个区块都能完整吃满当前容器。 */
.selattendance-resizable-split.is-stacked .selattendance-resizable-pane-left,
.selattendance-resizable-split.is-stacked .selattendance-resizable-pane-right {
  flex: initial;
  max-width: none;
}

/* 堆叠模式下隐藏拖拽柄，避免移动端仍出现不可用的列缩放控件。 */
.selattendance-resizable-split.is-stacked .selattendance-resize-handle {
  display: none;
}
</style>
