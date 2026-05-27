// 引入 Vue 挂载工具和 memory 页面根组件，验证最小插件工程装上后可以真实渲染。
import { mount } from '@vue/test-utils'
import MemoryWorkbenchView from '../../../src/projects/memory/views/MemoryWorkbenchView.vue'

describe('memory workbench view', () => {
  // 测试 memory 骨架页面渲染，保证最小插件工程的说明面板和检查项都能显示。
  it('renders the pluggable memory workspace shell', () => {
    const wrapper = mount(MemoryWorkbenchView)

    expect(wrapper.text()).toContain('Memory Workspace')
    expect(wrapper.text()).toContain('工程已被宿主自动发现并可切换')
    expect(wrapper.text()).toContain('删除 memory 目录后宿主仍可正常启动')
  })
})
