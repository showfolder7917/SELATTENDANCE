// 这里统一发现和管理宿主下的工程模块，保证新增或移除 projects/* 目录都不需要改宿主硬编码。

// 让 Vite 在构建期自动发现 src/projects 下每个工程目录的 index.js，形成可插拔工程注册表。
const discoveredProjectModules = import.meta.glob('./*/index.js', { eager: true })

// 把每个工程模块规范化成宿主需要的工程元数据，并过滤掉缺失 id 或 component 的无效注册。
const discoveredProjects = Object.values(discoveredProjectModules)
  .map((moduleEntry) => moduleEntry?.default)
  .filter((projectEntry) => projectEntry?.id && projectEntry?.component)
  .sort((leftProject, rightProject) => (leftProject.order ?? 999) - (rightProject.order ?? 999))

// 暴露当前宿主已经发现的全部工程列表，供宿主切换器和激活逻辑统一复用。
export const availableProjects = discoveredProjects

// 从 URL 读取当前希望激活的工程 id，便于宿主在刷新后保持当前工程不丢失。
export function readProjectIdFromUrl() {
  return new URLSearchParams(window.location.search).get('project') || ''
}

// 校验外部传入的工程 id 是否真实存在，避免 URL 或本地缓存指向已移除的工程目录。
export function normalizeProjectId(rawProjectId) {
  return availableProjects.find((projectEntry) => projectEntry.id === rawProjectId)?.id || ''
}

// 解析当前应该激活的工程 id，优先使用 URL 指定值，失效时回退到第一个可用工程。
export function resolveInitialProjectId() {
  return normalizeProjectId(readProjectIdFromUrl()) || availableProjects[0]?.id || ''
}

// 按工程 id 查询工程元数据，供宿主激活工程和渲染切换器时按需读取。
export function findProjectById(projectId) {
  return availableProjects.find((projectEntry) => projectEntry.id === projectId) || null
}

// 把当前工程写回 URL 查询参数，保证宿主切换后刷新页面仍然回到同一个工程。
export function writeProjectIdToUrl(projectId) {
  // 先基于当前地址构造可变 URL，避免丢掉其他现有查询参数。
  const nextUrl = new URL(window.location.href)
  // 当前工程有效时写入 project 参数，支持直接分享指定工程入口。
  if (projectId) {
    nextUrl.searchParams.set('project', projectId)
  } else {
    // 没有有效工程时移除 project 参数，避免保留过期引用。
    nextUrl.searchParams.delete('project')
  }
  // 用 replaceState 更新地址栏，避免切换工程时不断堆积历史记录。
  window.history.replaceState({}, '', `${nextUrl.pathname}${nextUrl.search}${nextUrl.hash}`)
}
