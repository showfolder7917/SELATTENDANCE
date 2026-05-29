<script setup>
import { computed } from 'vue'

const props = defineProps({
  columns: { type: Array, required: true },
  rows: { type: Array, required: true },
  rowKey: { type: [String, Function], default: 'id' },
  activeRowKey: { type: [String, Number], default: null },
  stickyHeader: { type: Boolean, default: false },
  clickableRows: { type: Boolean, default: false },
  variant: { type: String, default: 'admin' },
  minTableWidth: { type: [String, Number], default: '' },
  emptyText: { type: String, default: '' },
  showPagination: { type: Boolean, default: false }
})

const emit = defineEmits(['row-click'])

// 统一把列配置规整成表头和单元格都能直接消费的结构，减少业务页重复拼 class 和样式。
const normalizedColumns = computed(() =>
  props.columns.map((column) => ({
    ...column,
    align: column.align || 'left',
    wrap: Boolean(column.wrap),
    headerClass: column.headerClass || '',
    cellClass: column.cellClass || ''
  }))
)

// 支持字符串主键和函数主键两种方式，兼容基础资料和日志列表不同的数据主键来源。
function resolveRowKey(row, index) {
  if (typeof props.rowKey === 'function') {
    return props.rowKey(row, index)
  }
  return row?.[props.rowKey] ?? index
}

// 统一判断当前行是否为业务页选中行，让共享组件只负责高亮展示，不负责选中状态存储。
function isActiveRow(row, index) {
  return props.activeRowKey !== null && resolveRowKey(row, index) === props.activeRowKey
}

// 行点击只做事件桥接，具体编辑、详情加载仍由业务页自己处理。
function handleRowClick(row, index) {
  if (!props.clickableRows) return
  emit('row-click', row, index)
}

// 最小表格宽度统一转成 style，方便业务页按列数差异控制横向滚动阈值。
const tableStyle = computed(() => {
  if (!props.minTableWidth) return {}
  const widthValue =
    typeof props.minTableWidth === 'number'
      ? `${props.minTableWidth}px`
      : String(props.minTableWidth)
  return { minWidth: widthValue }
})

// 只有业务页明确要求显示分页时，底部分页区才会渲染，便于单组件按需隐藏分页栏。
const hasPagination = computed(() => props.showPagination)
</script>

<template>
  <div
    class="shared-data-table-shell"
    :class="[
      `variant-${variant}`,
      {
        'is-sticky-header': stickyHeader,
        'is-clickable': clickableRows,
        'has-pagination': hasPagination
      }
    ]"
  >
    <div class="shared-data-table-scroll">
      <table class="shared-data-table" :style="tableStyle">
        <thead>
          <tr>
            <th
              v-for="column in normalizedColumns"
              :key="column.key"
              :class="column.headerClass"
              :style="{
                textAlign: column.align,
                whiteSpace: column.wrap ? 'normal' : 'nowrap',
                width: column.width || undefined,
                minWidth: column.minWidth || undefined
              }"
            >
              {{ column.label }}
            </th>
          </tr>
        </thead>
        <tbody v-if="rows.length">
          <tr
            v-for="(row, index) in rows"
            :key="resolveRowKey(row, index)"
            :class="{ active: isActiveRow(row, index) }"
            @click="handleRowClick(row, index)"
          >
            <td
              v-for="column in normalizedColumns"
              :key="column.key"
              :class="column.cellClass"
              :style="{
                textAlign: column.align,
                whiteSpace: column.wrap ? 'normal' : 'nowrap',
                width: column.width || undefined,
                minWidth: column.minWidth || undefined
              }"
            >
              <slot
                :name="`cell-${column.key}`"
                :row="row"
                :column="column"
                :value="row?.[column.key]"
              >
                {{ row?.[column.key] ?? '-' }}
              </slot>
            </td>
          </tr>
        </tbody>
        <tbody v-else>
          <tr>
            <td :colspan="normalizedColumns.length" class="shared-data-table-empty-cell">
              <slot name="empty">{{ emptyText }}</slot>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="hasPagination" class="shared-data-table-pagination">
      <slot name="pagination" />
    </div>
  </div>
</template>

<style scoped>
.shared-data-table-shell {
  min-height: 0;
}

.shared-data-table-shell.has-pagination {
  height: 100%;
  min-height: 0;
  display: grid;
  grid-template-rows: minmax(0, 1fr) auto;
}

.shared-data-table-scroll {
  overflow: auto;
  border-radius: 22px;
  border: 1px solid rgba(112, 141, 194, 0.36);
  background: rgba(20, 28, 42, 0.96);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.03);
}

.shared-data-table-shell.has-pagination .shared-data-table-scroll {
  height: 100%;
  min-height: 0;
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
}

.shared-data-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}

.shared-data-table th,
.shared-data-table td {
  padding: 14px 12px;
  border-bottom: 1px solid rgba(115, 144, 202, 0.12);
  vertical-align: top;
}

.shared-data-table thead th {
  font-size: 0.88rem;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.shared-data-table-shell.is-sticky-header .shared-data-table thead th {
  position: sticky;
  top: 0;
  z-index: 3;
  padding: 18px 16px;
  background: linear-gradient(180deg, rgba(28, 40, 67, 0.98), rgba(19, 29, 48, 0.98));
  color: rgba(242, 246, 255, 0.94);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.06),
    inset 0 -1px 0 rgba(115, 144, 202, 0.18),
    0 8px 18px rgba(8, 12, 24, 0.24);
  backdrop-filter: blur(14px);
}

.shared-data-table-shell.is-sticky-header .shared-data-table thead th:first-child {
  border-top-left-radius: 18px;
}

.shared-data-table-shell.is-sticky-header .shared-data-table thead th:last-child {
  border-top-right-radius: 18px;
}

.shared-data-table-shell.variant-admin .shared-data-table th,
.shared-data-table-shell.variant-admin .shared-data-table td {
  border-right: 1px solid rgba(112, 141, 194, 0.22);
  background: rgba(18, 26, 40, 0.98);
}

.shared-data-table-shell.variant-admin .shared-data-table thead th {
  background: rgba(34, 44, 62, 0.98);
  box-shadow: inset 0 -1px 0 rgba(112, 141, 194, 0.34);
}

.shared-data-table-shell.variant-admin .shared-data-table td:last-child,
.shared-data-table-shell.variant-admin .shared-data-table th:last-child {
  border-right: 0;
}

.shared-data-table-shell.variant-admin .shared-data-table tbody tr:nth-child(even) td {
  background: rgba(24, 32, 47, 0.98);
}

.shared-data-table-shell.variant-list .shared-data-table tbody td {
  border-right: 1px solid rgba(112, 141, 194, 0.22);
  background: rgba(11, 18, 34, 0.2);
}

.shared-data-table-shell.variant-list .shared-data-table thead th {
  border-right: 1px solid rgba(112, 141, 194, 0.22);
}

.shared-data-table-shell.variant-list .shared-data-table td:last-child,
.shared-data-table-shell.variant-list .shared-data-table th:last-child {
  border-right: 0;
}

.shared-data-table-shell.variant-list .shared-data-table tbody td small {
  display: block;
  margin-top: 4px;
  color: rgba(216, 226, 255, 0.66);
}

.shared-data-table-shell.is-clickable .shared-data-table tbody tr {
  cursor: pointer;
  transition: background 160ms ease, box-shadow 160ms ease;
}

.shared-data-table-shell.variant-admin.is-clickable .shared-data-table tbody tr:hover td {
  background: rgba(40, 58, 88, 0.96);
}

.shared-data-table-shell.variant-list.is-clickable .shared-data-table tbody tr:hover {
  background: rgba(85, 110, 180, 0.14);
}

.shared-data-table-shell.variant-admin .shared-data-table tbody tr.active td {
  background: rgba(66, 98, 148, 0.94);
  box-shadow: inset 0 1px 0 rgba(173, 201, 246, 0.12), inset 0 -1px 0 rgba(173, 201, 246, 0.12);
}

.shared-data-table-shell.variant-admin .shared-data-table tbody tr.active:hover td {
  background: rgba(74, 110, 166, 0.96);
}

.shared-data-table-shell.variant-list .shared-data-table tbody tr.active {
  background: rgba(79, 124, 255, 0.16);
}

.shared-data-table-empty-cell {
  text-align: center;
  color: rgba(216, 226, 255, 0.72);
}

.shared-data-table-pagination {
  border: 1px solid rgba(112, 141, 194, 0.36);
  border-top: 0;
  border-bottom-left-radius: 22px;
  border-bottom-right-radius: 22px;
  background: rgba(14, 21, 36, 0.96);
  overflow: visible;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.02);
}

[data-selattendance-theme='liquid-glass'] .shared-data-table-shell.variant-admin .shared-data-table {
  color: rgba(58, 70, 96, 0.96);
}

[data-selattendance-theme='liquid-glass'] .shared-data-table-shell.variant-admin .shared-data-table th,
[data-selattendance-theme='liquid-glass'] .shared-data-table-shell.variant-admin .shared-data-table td {
  border-bottom-color: rgba(166, 185, 220, 0.56);
  border-right-color: rgba(182, 199, 229, 0.46);
  background: rgba(237, 243, 251, 0.76);
  color: rgba(71, 82, 106, 0.96);
}

[data-selattendance-theme='liquid-glass'] .shared-data-table-shell.variant-admin .shared-data-table thead th {
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.86) 0%, rgba(231, 239, 249, 0.72) 100%),
    rgba(222, 232, 246, 0.84);
  color: rgba(84, 94, 118, 0.98);
  box-shadow: inset 0 -1px 0 rgba(174, 193, 225, 0.52);
}

[data-selattendance-theme='liquid-glass'] .shared-data-table-shell.variant-admin .shared-data-table tbody tr:nth-child(even) td {
  background: rgba(228, 236, 247, 0.82);
}

[data-selattendance-theme='liquid-glass'] .shared-data-table-shell.variant-admin.is-clickable .shared-data-table tbody tr:hover td {
  background: rgba(213, 226, 243, 0.9);
}

[data-selattendance-theme='liquid-glass'] .shared-data-table-shell.variant-admin .shared-data-table tbody tr.active td {
  background: rgba(199, 216, 239, 0.96);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.58), inset 0 -1px 0 rgba(168, 188, 221, 0.34);
}

[data-selattendance-theme='liquid-glass'] .shared-data-table-shell.variant-admin .shared-data-table tbody tr.active:hover td {
  background: rgba(190, 210, 236, 0.98);
}

[data-selattendance-theme='liquid-glass'] .shared-data-table-shell.variant-list .shared-data-table thead th,
[data-selattendance-theme='liquid-glass'] .shared-data-table-shell.variant-list .shared-data-table tbody td {
  border-right-color: rgba(182, 199, 229, 0.46);
}

[data-selattendance-theme='liquid-glass'] .shared-data-table-shell.variant-list .shared-data-table tbody td small,
[data-selattendance-theme='liquid-glass'] .shared-data-table-shell.variant-admin .shared-data-table td small {
  color: rgba(96, 109, 136, 0.82);
}

[data-selattendance-theme='liquid-glass'] .shared-data-table-shell .seladmin-warning-text {
  color: rgba(178, 112, 55, 0.96);
}
</style>
