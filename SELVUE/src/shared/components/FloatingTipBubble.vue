<script setup>
import { computed, onBeforeUnmount, onMounted } from 'vue'

const props = defineProps({
  open: { type: Boolean, required: true },
  title: { type: String, required: true },
  body: { type: String, required: true },
  metaText: { type: String, default: '' },
  bubbleX: { type: Number, default: 24 },
  bubbleY: { type: Number, default: 24 },
  anchorX: { type: Number, default: 48 },
  anchorY: { type: Number, default: 48 },
  closeAriaLabel: { type: String, default: 'Close tip' },
  tailDragAriaLabel: { type: String, default: 'Drag tip tail' }
})

const emit = defineEmits([
  'close',
  'update:bubbleX',
  'update:bubbleY',
  'update:anchorX',
  'update:anchorY'
])

// 共享 tip 组件内部统一维护拖拽中的类型和光标偏移，页面层只保留业务状态和默认定位逻辑。
const dragState = {
  kind: '',
  offsetX: 0,
  offsetY: 0
}

// 这组尺寸控制共享 tip 的统一外形，让不同页面复用时都保持同一套圆角气泡与尾巴比例。
const bubbleWidth = 320
const bubbleHeight = 182
const tailBaseOffset = 158
const tailBaseWidth = 46

// 所有坐标都先夹到可视区内，避免拖拽或窗口变化后把气泡或尾巴甩出屏幕。
function clampCoordinate(value, min, max) {
  return Math.max(min, Math.min(max, value))
}

// 统一回写气泡左上角坐标，保证外层页面状态和共享组件视觉位置始终同步。
function updateBubblePosition(nextX, nextY) {
  emit('update:bubbleX', clampCoordinate(Math.round(nextX), 16, window.innerWidth - (bubbleWidth + 16)))
  emit('update:bubbleY', clampCoordinate(Math.round(nextY), 16, window.innerHeight - (bubbleHeight + 16)))
}

// 统一回写尾巴尖端坐标，供业务页面继续把 tip 指向真正要用户去操作的区域。
function updateAnchorPosition(nextX, nextY) {
  emit('update:anchorX', clampCoordinate(Math.round(nextX), 16, window.innerWidth - 16))
  emit('update:anchorY', clampCoordinate(Math.round(nextY), 16, window.innerHeight - 16))
}

// 指针抬起时立即清掉拖拽状态，避免页面滚动或切区块后仍残留拖拽模式。
function stopDrag() {
  dragState.kind = ''
}

// 拖动头部时只平移气泡，拖动尾巴时只调整尖端落点，保证两个交互职责清晰分离。
function handlePointerMove(event) {
  if (!props.open || !dragState.kind) {
    return
  }
  if (dragState.kind === 'bubble') {
    updateBubblePosition(event.clientX - dragState.offsetX, event.clientY - dragState.offsetY)
    return
  }
  updateAnchorPosition(event.clientX, event.clientY)
}

// 头部拖拽用于把整块提示挪到不遮挡业务内容的位置。
function startBubbleDrag(event) {
  if (!props.open) {
    return
  }
  dragState.kind = 'bubble'
  dragState.offsetX = event.clientX - props.bubbleX
  dragState.offsetY = event.clientY - props.bubbleY
  event.currentTarget?.setPointerCapture?.(event.pointerId)
  event.preventDefault()
}

// 尾巴拖拽用于微调“指向哪里”，这样页面不需要再额外摆一颗独立锚点按钮。
function startTailDrag(event) {
  if (!props.open) {
    return
  }
  dragState.kind = 'tail'
  event.currentTarget?.setPointerCapture?.(event.pointerId)
  event.preventDefault()
}

// 当窗口尺寸变化时，把当前气泡和尾巴重新夹回可见范围，避免复用到不同布局时跑出屏幕。
function syncWithinViewport() {
  if (!props.open) {
    return
  }
  updateBubblePosition(props.bubbleX, props.bubbleY)
  updateAnchorPosition(props.anchorX, props.anchorY)
}

const bubbleStyle = computed(() => ({
  left: `${props.bubbleX}px`,
  top: `${props.bubbleY}px`
}))

const tailShape = computed(() => {
  // 共享组件内部自己算尾巴几何，业务页面只需要提供气泡和尖端坐标即可复用。
  const baseLeftX = props.bubbleX + tailBaseOffset
  const baseRightX = baseLeftX + tailBaseWidth
  const baseY = props.bubbleY + bubbleHeight - 2
  const minX = Math.min(baseLeftX, baseRightX, props.anchorX) - 2
  const minY = Math.min(baseY, props.anchorY) - 2
  const width = Math.max(baseLeftX, baseRightX, props.anchorX) - minX + 2
  const height = Math.max(baseY, props.anchorY) - minY + 2
  return {
    svgStyle: {
      left: `${minX}px`,
      top: `${minY}px`,
      width: `${width}px`,
      height: `${height}px`
    },
    viewBox: `0 0 ${width} ${height}`,
    points: `${baseLeftX - minX},${baseY - minY} ${baseRightX - minX},${baseY - minY} ${props.anchorX - minX},${props.anchorY - minY}`,
    hitStyle: {
      left: `${props.anchorX - 22}px`,
      top: `${props.anchorY - 22}px`
    }
  }
})

onMounted(() => {
  // 共享层统一接管 pointer 事件，后续任何页面复用 tip 都不用再手写这一套拖拽监听。
  window.addEventListener('pointermove', handlePointerMove)
  window.addEventListener('pointerup', stopDrag)
  window.addEventListener('resize', syncWithinViewport)
})

onBeforeUnmount(() => {
  window.removeEventListener('pointermove', handlePointerMove)
  window.removeEventListener('pointerup', stopDrag)
  window.removeEventListener('resize', syncWithinViewport)
})
</script>

<template>
  <div v-if="open" class="selshared-floating-tip-layer">
    <svg
      class="selshared-floating-tip-tail"
      :style="tailShape.svgStyle"
      :viewBox="tailShape.viewBox"
      preserveAspectRatio="none"
      aria-hidden="true"
    >
      <polygon class="selshared-floating-tip-tail-fill" :points="tailShape.points" />
    </svg>
    <button
      type="button"
      class="selshared-floating-tip-tail-handle"
      :style="tailShape.hitStyle"
      :aria-label="tailDragAriaLabel"
      @pointerdown="startTailDrag"
    />
    <section class="selshared-floating-tip-bubble seladmin-surface" :style="bubbleStyle" role="status" aria-live="polite">
      <div class="selshared-floating-tip-head" @pointerdown="startBubbleDrag">
        <strong>{{ title }}</strong>
        <button
          type="button"
          class="selshared-floating-tip-close"
          :aria-label="closeAriaLabel"
          @pointerdown.stop
          @click.stop="emit('close')"
        >
          ×
        </button>
      </div>
      <p>{{ body }}</p>
      <small v-if="metaText">{{ metaText }}</small>
    </section>
  </div>
</template>

<style scoped>
.selshared-floating-tip-layer {
  position: fixed;
  inset: 0;
  z-index: 70;
  pointer-events: none;
}

.selshared-floating-tip-tail {
  position: fixed;
  z-index: 71;
  overflow: visible;
  pointer-events: none;
}

.selshared-floating-tip-tail-fill {
  fill: rgba(49, 29, 35, 0.96);
  stroke: rgba(255, 186, 144, 0.42);
  stroke-width: 1.6;
  stroke-linejoin: round;
}

.selshared-floating-tip-tail-handle {
  position: fixed;
  z-index: 72;
  width: 44px;
  height: 44px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  cursor: grab;
  pointer-events: auto;
  touch-action: none;
}

.selshared-floating-tip-tail-handle::after {
  content: '';
  position: absolute;
  inset: 9px;
  border-radius: 999px;
  border: 1px dashed rgba(255, 186, 144, 0);
  transition: border-color 140ms ease, background 140ms ease;
}

.selshared-floating-tip-tail-handle:hover::after {
  border-color: rgba(255, 186, 144, 0.32);
  background: rgba(255, 186, 144, 0.08);
}

.selshared-floating-tip-tail-handle:active {
  cursor: grabbing;
}

.selshared-floating-tip-bubble {
  position: fixed;
  z-index: 73;
  width: 320px;
  min-height: 182px;
  padding: 16px 18px 20px;
  border: 1px solid rgba(255, 186, 144, 0.42);
  border-radius: 26px;
  background:
    linear-gradient(180deg, rgba(61, 31, 36, 0.96), rgba(39, 22, 33, 0.94)),
    rgba(39, 22, 33, 0.94);
  color: rgba(255, 241, 236, 0.98);
  box-shadow: 0 18px 40px rgba(13, 10, 20, 0.28);
  pointer-events: auto;
}

.selshared-floating-tip-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
  cursor: grab;
  touch-action: none;
}

.selshared-floating-tip-head:active {
  cursor: grabbing;
}

.selshared-floating-tip-head strong {
  font-size: 16px;
}

.selshared-floating-tip-bubble p,
.selshared-floating-tip-bubble small {
  display: block;
  line-height: 1.5;
}

.selshared-floating-tip-bubble p {
  margin: 0;
}

.selshared-floating-tip-bubble small {
  margin-top: 12px;
  color: rgba(255, 212, 203, 0.82);
}

.selshared-floating-tip-close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: inherit;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
}

.selshared-floating-tip-close:hover {
  background: rgba(255, 255, 255, 0.14);
}
</style>
