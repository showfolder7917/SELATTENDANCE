<script setup>
import { computed, ref, watch } from 'vue'
import SharedMiniSelect from './SharedMiniSelect.vue'

const props = defineProps({
  pageSize: { type: Number, required: true },
  pageSizeOptions: { type: Array, required: true },
  currentPage: { type: Number, required: true },
  totalPages: { type: Number, required: true },
  totalCount: { type: Number, required: true },
  pageSizeLabel: { type: String, required: true },
  prevLabel: { type: String, required: true },
  nextLabel: { type: String, required: true },
  lastLabel: { type: String, required: true },
  jumpPrefix: { type: String, required: true },
  jumpSuffix: { type: String, required: true },
  jumpActionLabel: { type: String, required: true },
  totalText: { type: String, required: true },
  invalidMessage: { type: String, required: true },
  outOfRangeMessage: { type: String, required: true }
})

const emit = defineEmits(['page-change', 'page-size-change', 'invalid'])

// 跳页输入框单独维护字符串，允许用户先编辑再点击加载，避免父组件状态被半成品输入污染。
const pageJumpInput = ref('')

// 当前页变化后同步输入框，让分页条始终回显真实数据库页码。
watch(
  () => props.currentPage,
  (nextPage) => {
    pageJumpInput.value = String(nextPage)
  },
  { immediate: true }
)

// 按页数生成统一页码片段，所有列表页共用同一套首页、邻近页、尾页和省略号策略。
const pageItems = computed(() => {
  const total = Number(props.totalPages || 1)
  const current = Number(props.currentPage || 1)
  if (total <= 7) {
    return Array.from({ length: total }, (_, index) => ({ type: 'page', value: index + 1 }))
  }

  const items = [{ type: 'page', value: 1 }]
  let start = Math.max(2, current - 1)
  let end = Math.min(total - 1, current + 1)

  // 当前页靠前时优先让用户看见连续前几页，减少一开始就出现省略号。
  if (current <= 4) {
    start = 2
    end = 5
  }

  // 当前页靠近尾部时优先展示最后几页，方便快速检查收尾数据。
  if (current >= total - 3) {
    start = total - 4
    end = total - 1
  }

  if (start > 2) {
    items.push({ type: 'ellipsis', key: 'leading' })
  }

  for (let page = start; page <= end; page += 1) {
    items.push({ type: 'page', value: page })
  }

  if (end < total - 1) {
    items.push({ type: 'ellipsis', key: 'trailing' })
  }

  items.push({ type: 'page', value: total })
  return items
})

// 上一页按钮只在第 1 页禁用，避免继续发起无意义翻页。
const isPrevDisabled = computed(() => props.currentPage <= 1)
// 最后一页按钮在已经处于最后一页时禁用，避免重复请求相同页码。
const isLastDisabled = computed(() => props.currentPage >= props.totalPages)
// 下一页按钮只在尾页禁用，保持按钮语义稳定。
const isNextDisabled = computed(() => props.currentPage >= props.totalPages)

// 页大小切换时只把新的档位发给父组件，由业务页统一回到第一页并触发重查。
function handlePageSizeChange(nextPageSize) {
  emit('page-size-change', Number(nextPageSize))
}

// 直接点页码时只负责派发目标页码，不自己操作外部过滤对象。
function goToPage(page) {
  if (page === props.currentPage) return
  emit('page-change', page)
}

function goPrevPage() {
  if (isPrevDisabled.value) return
  emit('page-change', props.currentPage - 1)
}

function goLastPage() {
  if (isLastDisabled.value) return
  emit('page-change', props.totalPages)
}

function goNextPage() {
  if (isNextDisabled.value) return
  emit('page-change', props.currentPage + 1)
}

// 跳页按钮统一做数字和范围校验，错误信息通过事件抛给业务页 toast。
function submitPageJump() {
  const rawValue = String(pageJumpInput.value || '').trim()
  if (!/^\d+$/.test(rawValue)) {
    emit('invalid', props.invalidMessage.replace('{totalPages}', String(props.totalPages)))
    return
  }

  const targetPage = Number(rawValue)
  if (targetPage < 1 || targetPage > props.totalPages) {
    emit('invalid', props.outOfRangeMessage.replace('{totalPages}', String(props.totalPages)))
    return
  }

  goToPage(targetPage)
}
</script>

<template>
  <div class="selshared-pagination">
    <label class="selshared-pagination-size">
      <span>{{ pageSizeLabel }}</span>
      <SharedMiniSelect
        :model-value="String(pageSize)"
        :options="pageSizeOptions.map((size) => ({ value: String(size), label: String(size) }))"
        :aria-label="pageSizeLabel"
        @change="handlePageSizeChange"
      />
    </label>

    <div class="selshared-pagination-pages">
      <button class="seladmin-button seladmin-button-secondary selshared-pagination-tail" type="button" :disabled="isPrevDisabled" @click="goPrevPage">
        {{ prevLabel }}
      </button>
      <button
        v-for="item in pageItems"
        :key="item.type === 'page' ? `page-${item.value}` : item.key"
        class="selshared-pagination-number"
        :class="{ active: item.type === 'page' && item.value === currentPage }"
        :type="item.type === 'page' ? 'button' : undefined"
        :disabled="item.type !== 'page'"
        @click="item.type === 'page' ? goToPage(item.value) : null"
      >
        <template v-if="item.type === 'page'">{{ item.value }}</template>
        <template v-else>...</template>
      </button>
      <button class="seladmin-button seladmin-button-secondary selshared-pagination-tail" type="button" :disabled="isLastDisabled" @click="goLastPage">
        {{ lastLabel }}
      </button>
      <button class="seladmin-button seladmin-button-secondary selshared-pagination-tail" type="button" :disabled="isNextDisabled" @click="goNextPage">
        {{ nextLabel }}
      </button>
    </div>

    <div class="selshared-pagination-jump">
      <label class="selshared-pagination-jump-field">
        <span>{{ jumpPrefix }}</span>
        <input
          v-model="pageJumpInput"
          inputmode="numeric"
          @keydown.enter.prevent="submitPageJump"
        />
        <span>{{ jumpSuffix }}</span>
      </label>
      <button class="seladmin-button seladmin-button-primary selshared-pagination-load" type="button" @click="submitPageJump">
        {{ jumpActionLabel }}
      </button>
      <span class="selshared-pagination-total">{{ totalText }}</span>
    </div>
  </div>
</template>

<style scoped>
.selshared-pagination {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 16px;
  padding: 10px 14px;
}

.selshared-pagination-size {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.selshared-pagination-size > span,
.selshared-pagination-jump-field span,
.selshared-pagination-total {
  color: rgba(216, 226, 255, 0.8);
  font-size: 0.9rem;
  white-space: nowrap;
}

.selshared-pagination-size :deep(.shared-mini-select) {
  min-width: 84px;
  z-index: 2;
}

.selshared-pagination-size :deep(.shared-mini-select.open) {
  z-index: 12;
}

.selshared-pagination-size :deep(.shared-mini-select-trigger) {
  box-sizing: border-box;
  height: 36px;
}

.selshared-pagination-size :deep(.shared-mini-select-menu) {
  top: auto;
  bottom: calc(100% + 8px);
  min-width: 92px;
}

.selshared-pagination-pages {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 0;
  padding: 6px 8px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(115, 144, 202, 0.14);
}

.selshared-pagination-number,
.selshared-pagination-tail,
.selshared-pagination-load {
  min-height: 36px;
}

.selshared-pagination-number {
  min-width: 36px;
  padding: 0 12px;
  border-radius: 12px;
  border: 1px solid rgba(115, 144, 202, 0.26);
  background: rgba(255, 255, 255, 0.03);
  color: rgba(236, 242, 255, 0.9);
  font-weight: 700;
}

.selshared-pagination-number.active {
  border-color: rgba(129, 159, 255, 0.64);
  background: rgba(89, 123, 236, 0.34);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.08);
}

.selshared-pagination-number:disabled {
  cursor: default;
  opacity: 0.82;
}

.selshared-pagination-jump {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  min-width: 0;
}

.selshared-pagination-jump-field {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.selshared-pagination-jump-field input {
  width: 96px;
  height: 36px;
  padding: 0 12px;
  border-radius: 12px;
  border: 1px solid rgba(115, 144, 202, 0.2);
  background: rgba(13, 20, 35, 0.92);
  color: rgba(246, 250, 255, 0.94);
  text-align: center;
}

@media (max-width: 1440px) {
  .selshared-pagination {
    grid-template-columns: 1fr;
    justify-items: start;
  }

  .selshared-pagination-pages,
  .selshared-pagination-jump {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
