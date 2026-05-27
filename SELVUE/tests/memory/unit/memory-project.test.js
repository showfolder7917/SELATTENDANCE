// 引入 memory 工程注册入口，验证插件式模块元数据在宿主发现链路里保持稳定。
import memoryProject from '../../../src/projects/memory/index.js'

describe('memory project module', () => {
  // 测试 memory 工程元数据，保证装上模块后宿主能按统一契约识别它。
  it('exports the expected pluggable project metadata', () => {
    expect(memoryProject.id).toBe('memory')
    expect(memoryProject.label).toBe('Memory')
    expect(memoryProject.order).toBe(20)
    expect(memoryProject.component).toBeTruthy()
  })
})
