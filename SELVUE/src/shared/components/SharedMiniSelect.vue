<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps({
  modelValue: { type: [String, Number], required: true },
  options: { type: Array, required: true },
  ariaLabel: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue', 'change'])

// 控制页大小下拉是否展开，保证分页条在所有浏览器里走统一的自绘下拉而不是原生 select。
const open = ref(false)

// 用组件根节点判断点击是否发生在控件外部，外部点击时自动收起下拉层。
const rootRef = ref(null)

// 把业务页传入的原始选项统一规范成 { value, label } 结构，避免每个页面自己做重复映射。
const normalizedOptions = computed(() => {
  return props.options.map((option) => {
    if (option && typeof option === 'object' && 'value' in option) {
      return {
        value: option.value,
        label: option.label ?? String(option.value)
      }
    }
    return {
      value: option,
      label: String(option)
    }
  })
})

// 当前选中项优先按 value 精确匹配，保证按钮上显示的就是业务页实际页大小值。
const selectedOption = computed(() => {
  return normalizedOptions.value.find((option) => String(option.value) === String(props.modelValue)) || normalizedOptions.value[0] || null
})

// 统一把页大小选项显示文本收口到共享控件里，业务页只需要给 value 和 label。
const selectedLabel = computed(() => {
  if (!selectedOption.value) return ''
  return selectedOption.value.label ?? String(selectedOption.value.value)
})

// 点击触发按钮时只切换展开状态，避免把分页条其他按钮的事件流打断。
function toggleOpen() {
  open.value = !open.value
}

// 选择页大小后立刻回传给业务页，并同步收起下拉层。
function chooseOption(option) {
  emit('update:modelValue', option.value)
  emit('change', option.value)
  open.value = false
}

// 点击控件外部时自动收起，避免分页条里残留悬浮菜单遮挡后续操作。
function handleDocumentPointerDown(event) {
  if (!rootRef.value?.contains(event.target)) {
    open.value = false
  }
}

// 组件挂载后注册全局指针监听，让自定义下拉具备接近原生控件的收起行为。
onMounted(() => {
  document.addEventListener('pointerdown', handleDocumentPointerDown)
})

// 组件卸载时释放监听，避免切频道后残留全局事件处理器。
onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', handleDocumentPointerDown)
})
</script>

<template>
  <div ref="rootRef" class="shared-mini-select" :class="{ open }">
    <button
      class="shared-mini-select-trigger"
      type="button"
      :aria-label="ariaLabel"
      :aria-expanded="open ? 'true' : 'false'"
      @click="toggleOpen"
    >
      <span>{{ selectedLabel }}</span>
    </button>

    <div v-if="open" class="shared-mini-select-menu" role="listbox">
      <button
        v-for="option in normalizedOptions"
        :key="String(option.value)"
        class="shared-mini-select-option"
        :class="{ active: String(option.value) === String(modelValue) }"
        type="button"
        @click="chooseOption(option)"
      >
        {{ option.label ?? option.value }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.shared-mini-select {
  position: relative;
  min-width: 84px;
}

.shared-mini-select-trigger {
  width: 100%;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 0 34px 0 14px;
  border: 1px solid rgba(115, 144, 202, 0.18);
  border-radius: 12px;
  background: rgba(15, 23, 38, 0.92);
  color: #f6f8ff;
  font: inherit;
  font-weight: 600;
  letter-spacing: 0.01em;
  cursor: pointer;
  position: relative;
}

.shared-mini-select-trigger::after {
  content: '';
  position: absolute;
  right: 14px;
  top: 50%;
  width: 8px;
  height: 8px;
  margin-top: -6px;
  border-right: 2px solid rgba(227, 237, 255, 0.82);
  border-bottom: 2px solid rgba(227, 237, 255, 0.82);
  transform: rotate(45deg);
  transition: transform 140ms ease, margin-top 140ms ease;
}

.shared-mini-select.open .shared-mini-select-trigger::after {
  margin-top: -2px;
  transform: rotate(225deg);
}

.shared-mini-select-menu {
  position: absolute;
  left: 0;
  right: 0;
  top: calc(100% + 6px);
  z-index: 30;
  display: grid;
  gap: 4px;
  padding: 6px;
  border: 1px solid rgba(115, 144, 202, 0.16);
  border-radius: 12px;
  background: rgba(13, 20, 33, 0.98);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.26);
}

.shared-mini-select-option {
  height: 34px;
  padding: 0 10px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: rgba(235, 241, 255, 0.9);
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.shared-mini-select-option:hover,
.shared-mini-select-option.active {
  background: rgba(91, 125, 255, 0.16);
  color: #ffffff;
}
</style>
