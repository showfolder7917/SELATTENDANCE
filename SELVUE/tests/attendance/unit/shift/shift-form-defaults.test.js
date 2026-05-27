// 引入工作台状态工厂，验证班次模板表单默认值和推荐动作依赖不会在模块拆分后走样。
import { createWorkbenchState } from '@tests-attendance'

describe('attendance unit shift defaults', () => {
  // 校验班次模板表单默认班型、时间和启用态，保证模板新增入口始终可直接编辑。
  it('keeps the shift template form defaults stable', () => {
    expect(createWorkbenchState().shiftForm).toEqual({
      id: null,
      templateCode: '',
      templateName: '',
      shiftType: 'WORK',
      startTime: '09:00:00',
      endTime: '18:00:00',
      crossDay: false,
      scheduledBreakMinutes: 60,
      color: 'BLUE',
      active: true
    })
  })
})
