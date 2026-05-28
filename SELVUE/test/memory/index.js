// memory 测试公开入口只暴露最小插件工程契约与根视图，保持 memory 测试侧也能独立插拔。

// 暴露 memory 插件注册信息，供宿主发现链路验证工程元数据。
export { default as memoryProject } from '@/projects/memory/index.js'

// 暴露 memory 根视图，供最小插件页面集成测试验证渲染骨架。
export { default as MemoryWorkbenchView } from '@/projects/memory/views/MemoryWorkbenchView.vue'
