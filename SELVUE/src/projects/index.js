// 通过 Vite 的模块发现能力自动收集 src/projects 下的子工程注册文件，避免根层写死 attendance。
const discoveredProjectModules = import.meta.glob('./*/index.js', { eager: true })

// 只保留声明完整 id 和 component 的子工程，保证宿主在模块被删除后仍能安全降级。
const discoveredProjects = Object.values(discoveredProjectModules)
  .map((moduleEntry) => moduleEntry?.default)
  .filter((projectEntry) => projectEntry?.id && projectEntry?.component)
  .sort((leftProject, rightProject) => (leftProject.order ?? 999) - (rightProject.order ?? 999))

// 对外暴露可用工程清单，供宿主决定默认首页和空状态渲染。
export const availableProjects = discoveredProjects

// 统一从 URL 读取 project 参数，保证宿主和后续子工程扩展使用同一入口约定。
export function readProjectIdFromUrl() {
  return new URLSearchParams(window.location.search).get('project') || ''
}

// 将外部 project 参数收敛到当前可用工程集合，避免删除模块后仍指向失效工程。
export function normalizeProjectId(rawProjectId) {
  return availableProjects.find((projectEntry) => projectEntry.id === rawProjectId)?.id || ''
}

// 给宿主一个稳定的首选工程，优先用 URL 指定值，否则回退到当前发现到的第一个工程。
export function resolveInitialProjectId() {
  return normalizeProjectId(readProjectIdFromUrl()) || availableProjects[0]?.id || ''
}
