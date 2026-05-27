<script setup>
import { computed } from 'vue'
import { availableProjects, normalizeProjectId, resolveInitialProjectId } from './projects'

// 宿主只保存当前激活工程标识，避免根层直接依赖 attendance 的具体页面实现。
const activeProjectId = computed(() => normalizeProjectId(resolveInitialProjectId()))

// 根壳优先渲染当前激活工程；当工程目录被移除时，宿主回退到空状态而不是构建时报错。
const activeProject = computed(
  () => availableProjects.find((projectEntry) => projectEntry.id === activeProjectId.value) || null
)
</script>

<template>
  <component :is="activeProject.component" v-if="activeProject" />
  <main v-else class="project-host-empty-state">
    <section class="project-host-empty-card">
      <p class="project-host-eyebrow">Project Host</p>
      <h1>No project is currently registered</h1>
      <p>
        The root host is running, but there is no available module under <code>src/projects</code>.
      </p>
    </section>
  </main>
</template>

<style scoped>
.project-host-empty-state {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px;
  background:
    radial-gradient(circle at top, rgba(93, 141, 217, 0.16), transparent 30%),
    linear-gradient(180deg, #0d1320 0%, #111b2c 100%);
  color: #f7fbff;
}

.project-host-empty-card {
  width: min(100%, 640px);
  display: grid;
  gap: 12px;
  padding: 28px;
  border: 1px solid rgba(196, 214, 242, 0.18);
  border-radius: 24px;
  background: rgba(14, 21, 33, 0.78);
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.24);
}

.project-host-eyebrow {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(217, 231, 255, 0.66);
}

.project-host-empty-card h1,
.project-host-empty-card p {
  margin: 0;
}
</style>
