// 引入 host 测试公开入口，验证宿主工程注册表和 URL 同步工具不会偏离多模块插件契约。
import {
  availableProjects,
  findProjectById,
  normalizeProjectId,
  readProjectIdFromUrl,
  resolveInitialProjectId,
  writeProjectIdToUrl
} from '@tests-host'

describe('host unit projects registry', () => {
  // 每个用例前重置地址栏，避免前一个 project 查询参数污染当前宿主解析逻辑。
  beforeEach(() => {
    // jsdom 只能在当前 origin 下改地址栏，这里统一复用当前域名验证宿主 URL 协议本身。
    window.history.replaceState({}, '', `${window.location.origin}/`)
  })

  // 校验工程发现列表，保证宿主能够看到 attendance 和 memory 两个已安装工程。
  it('discovers the installed projects through the registry', () => {
    const projectIds = availableProjects.map((projectEntry) => projectEntry.id)

    expect(projectIds).toContain('attendance')
    expect(projectIds).toContain('memory')
  })

  // 校验工程 id 规范化与查找，保证无效工程引用不会被宿主继续保留。
  it('normalizes and finds project ids safely', () => {
    expect(normalizeProjectId('attendance')).toBe('attendance')
    expect(normalizeProjectId('missing')).toBe('')
    expect(findProjectById('memory')?.label).toBe('Memory')
    expect(findProjectById('missing')).toBeNull()
  })

  // 校验 URL 读取与初始工程解析，保证浏览器地址栏仍是宿主恢复激活工程的唯一入口。
  it('reads, resolves and rewrites project ids through the url', () => {
    // 用当前 origin 构造带 project 参数的地址，避免测试夹具误触跨域安全限制。
    window.history.replaceState({}, '', `${window.location.origin}/?project=memory`)

    expect(readProjectIdFromUrl()).toBe('memory')
    expect(resolveInitialProjectId()).toBe('memory')

    writeProjectIdToUrl('attendance')
    expect(new URL(window.location.href).searchParams.get('project')).toBe('attendance')

    writeProjectIdToUrl('')
    expect(new URL(window.location.href).searchParams.get('project')).toBeNull()
  })
})
