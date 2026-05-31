<script setup>
// 这里先提供最小 memory 宿主页面，验证工程装上或移除时宿主无需再改代码。

// 把当前工程的下一步说明整理成最小可见内容，便于后续继续扩展 memory 业务。
const checkpoints = [
  '工程已被宿主自动发现并可切换',
  '删除 memory 目录后宿主仍可正常启动',
  '后续可在该目录内独立扩展 memory 的 views、components、services'
]
</script>

<template>
  <main class="memory-shell">
    <section class="memory-hero">
      <div class="memory-hero-copy">
        <p class="memory-eyebrow">Project Module</p>
        <h1>Memory Workspace</h1>
        <p class="memory-copy">
          This is the minimal pluggable skeleton for the <code>memory</code> project. As long as
          <code>src/projects/memory</code> exists, the host can discover and load it.
        </p>
      </div>

      <div class="selattendance-hero-actions">
        <!-- memory 也提供统一的宿主 toolbar 挂载点，让 project 切换器切入任何工程时都能稳定挂到同一条 hero 操作带。 -->
        <div id="project-host-toolbar-target" class="selattendance-hero-toolbar-slot"></div>
      </div>
    </section>

    <section class="memory-panel">
      <h2>Pluggable Contract</h2>
      <ul class="memory-list">
        <li v-for="checkpoint in checkpoints" :key="checkpoint">{{ checkpoint }}</li>
      </ul>
    </section>
  </main>
</template>
