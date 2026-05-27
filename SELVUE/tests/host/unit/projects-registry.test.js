// 引入宿主工程注册表，验证自动发现、规范化和 URL 同步工具不会偏离插件宿主契约。
import {
  availableProjects,
  findProjectById,
  normalizeProjectId,
  readProjectIdFromUrl,
  resolveInitialProjectId,
  writeProjectIdToUrl
} from '../../../src/projects/index.js'

describe('host project registry', () => {
  // 每个用例前重置地址栏，避免前一个 project 查询参数污染当前宿主解析逻辑。
  beforeEach(() => {
    window.history.replaceState({}, '', 'http://localhost/')
  })

  // 测试项目发现列表，保证宿主能够看到 attendance 和 memory 两个已安装工程。
  it('discovers the installed projects through the registry', () => {
    const projectIds = availableProjects.map((projectEntry) => projectEntry.id)

    expect(projectIds).toContain('attendance')
    expect(projectIds).toContain('memory')
  })

  // 测试工程 id 规范化与查找，保证无效工程引用不会被宿主继续保留。
  it('normalizes and finds project ids safely', () => {
    expect(normalizeProjectId('attendance')).toBe('attendance')
    expect(normalizeProjectId('missing')).toBe('')
    expect(findProjectById('memory')?.label).toBe('Memory')
    expect(findProjectById('missing')).toBeNull()
  })

  // 测试 URL 读取与初始工程解析，保证浏览器地址栏仍是宿主恢复激活工程的唯一入口。
  it('reads, resolves and rewrites project ids through the url', () => {
    window.history.replaceState({}, '', 'http://localhost/?project=memory')

    expect(readProjectIdFromUrl()).toBe('memory')
    expect(resolveInitialProjectId()).toBe('memory')

    writeProjectIdToUrl('attendance')
    expect(new URL(window.location.href).searchParams.get('project')).toBe('attendance')

    writeProjectIdToUrl('')
    expect(new URL(window.location.href).searchParams.get('project')).toBeNull()
  })
})
