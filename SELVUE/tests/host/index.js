// host 测试公开入口统一收口宿主壳、项目注册表和 URL 同步工具，避免每个测试重复穿透 src 目录。

// 暴露宿主根组件，供集成和 e2e 相关测试验证多工程切换行为。
export { default as HostApp } from '@/App.vue'

// 暴露项目注册表和 URL 工具，供宿主单元测试验证自动发现与地址栏同步契约。
export {
  availableProjects,
  findProjectById,
  normalizeProjectId,
  readProjectIdFromUrl,
  resolveInitialProjectId,
  writeProjectIdToUrl
} from '@/projects/index.js'
