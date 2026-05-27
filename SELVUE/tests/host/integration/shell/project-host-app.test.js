// 引入 Vue 挂载工具和宿主根组件，验证宿主切换器会按项目注册表切换实际工程。
import { defineComponent, nextTick } from 'vue'
import { mount } from '@vue/test-utils'
import { HostApp } from '@tests-host'

// 模拟宿主注册表，避免集成测试被真实工程内部复杂依赖放大，只专注宿主切换行为。
vi.mock('@/projects', () => {
  // 用最小虚拟工程组件代表 attendance 页面，验证宿主能渲染当前激活工程。
  const AttendanceStub = defineComponent({
    name: 'AttendanceStub',
    template: '<section data-testid="attendance-view">Attendance Stub</section>'
  })
  // 用最小虚拟工程组件代表 memory 页面，验证宿主切换后能替换实际渲染根组件。
  const MemoryStub = defineComponent({
    name: 'MemoryStub',
    template: '<section data-testid="memory-view">Memory Stub</section>'
  })
  // 返回宿主所需的注册工具，完整模拟多工程发现、查找和 URL 回写契约。
  return {
    availableProjects: [
      { id: 'attendance', label: 'Attendance', component: AttendanceStub },
      { id: 'memory', label: 'Memory', component: MemoryStub }
    ],
    findProjectById: (projectId) =>
      [
        { id: 'attendance', label: 'Attendance', component: AttendanceStub },
        { id: 'memory', label: 'Memory', component: MemoryStub }
      ].find((projectEntry) => projectEntry.id === projectId) || null,
    normalizeProjectId: (projectId) => (projectId === 'attendance' || projectId === 'memory' ? projectId : ''),
    readProjectIdFromUrl: () => '',
    resolveInitialProjectId: () => 'attendance',
    writeProjectIdToUrl: vi.fn()
  }
})

describe('host integration project shell app', () => {
  // 测试宿主切换器和工程切换，保证项目宿主不会把某个业务工程写死到根层。
  it('renders the active project and switches to another project from the selector', async () => {
    const wrapper = mount(HostApp)

    expect(wrapper.text()).toContain('Project')
    expect(wrapper.find('[data-testid="attendance-view"]').exists()).toBe(true)

    await wrapper.get('#project-host-select').setValue('memory')
    await nextTick()

    expect(wrapper.find('[data-testid="memory-view"]').exists()).toBe(true)
  })
})
