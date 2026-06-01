<script setup>
import { computed } from 'vue'

// 单按钮提示框只负责承接错误、告警这类“读完即关闭”的消息，不引入确认分支。
const props = defineProps({
  // open 控制当前提示框是否挂载到页面，避免无消息时继续占据遮罩层。
  open: { type: Boolean, required: true },
  // title 负责明确告诉用户当前弹窗是什么级别的反馈。
  title: { type: String, required: true },
  // message 负责展示后端或前端整理后的最终可读错误文案。
  message: { type: String, required: true },
  // closeLabel 允许各工程继续使用自己的双语关闭按钮文案。
  closeLabel: { type: String, required: true },
  // tone 用于区分 danger/warn/info 三种视觉语气，先满足错误弹窗主场景。
  tone: { type: String, default: 'danger' }
})

// close 事件统一通知父层关闭弹窗，保证业务状态收口仍在 composable 内部。
const emit = defineEmits(['close'])

// tone 对应的样式类在组件内部统一映射，避免调用方自己拼接危险类名。
const dialogToneClass = computed(() => `selshared-alert-dialog--${props.tone}`)
</script>

<template>
  <!-- 遮罩层点击空白处也允许关闭，避免错误弹窗在移动端把页面彻底锁死。 -->
  <div v-if="open" class="selshared-alert-overlay" @click.self="emit('close')">
    <section
      class="selshared-alert-dialog seladmin-surface"
      :class="dialogToneClass"
      role="alertdialog"
      aria-modal="true"
      :aria-label="title"
    >
      <!-- 标题负责先告诉用户当前是错误还是普通提醒。 -->
      <h3>{{ title }}</h3>
      <!-- 正文直接展示已经本地化过的最终错误文案。 -->
      <p class="seladmin-copy">{{ message }}</p>
      <div class="selshared-alert-actions">
        <!-- 单按钮提示框只保留关闭动作，避免把简单错误提示做成确认流程。 -->
        <button type="button" class="seladmin-button seladmin-button-primary" @click="emit('close')">
          {{ closeLabel }}
        </button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.selshared-alert-overlay {
  position: fixed;
  inset: 0;
  z-index: 80;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(5, 10, 22, 0.56);
  backdrop-filter: blur(10px);
}

.selshared-alert-dialog {
  width: min(100%, 460px);
  display: grid;
  gap: 16px;
  padding: 24px;
  border-radius: 22px;
  border: 1px solid rgba(139, 170, 255, 0.2);
  box-shadow: 0 28px 80px rgba(5, 10, 22, 0.38);
}

.selshared-alert-dialog h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
}

.selshared-alert-dialog .seladmin-copy {
  margin: 0;
  line-height: 1.6;
}

.selshared-alert-actions {
  display: flex;
  justify-content: flex-end;
}

.selshared-alert-dialog--danger {
  border-color: rgba(255, 122, 146, 0.34);
  box-shadow: 0 28px 80px rgba(64, 18, 31, 0.36);
}

.selshared-alert-dialog--warn {
  border-color: rgba(255, 185, 102, 0.34);
  box-shadow: 0 28px 80px rgba(76, 46, 12, 0.34);
}

.selshared-alert-dialog--info {
  border-color: rgba(108, 140, 255, 0.28);
}
</style>
