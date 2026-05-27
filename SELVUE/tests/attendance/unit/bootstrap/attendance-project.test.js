// 引入考勤模块注册信息、首页壳工厂和 workbench 组合入口，验证 bootstrap 模块仍对宿主暴露稳定契约。
import {
  attendanceProject,
  createBootstrapShell,
  createSectionErrors,
  createSectionLoaders,
  createSectionStates,
  useAttendanceWorkbench
} from '@tests-attendance'

describe('attendance unit bootstrap project contract', () => {
  // 校验考勤模块注册元数据，保证宿主仍能通过统一插件契约发现 attendance 工程。
  it('exports the expected attendance project metadata', () => {
    expect(attendanceProject.id).toBe('attendance')
    expect(attendanceProject.label).toBe('Attendance')
    expect(attendanceProject.order).toBe(10)
    expect(attendanceProject.component).toBeTruthy()
  })

  // 校验 bootstrap 壳维护的区块加载、错误和完成态字典，保证首页作为多模块入口时能独立追踪每个 section。
  it('keeps section registries available for bootstrap orchestration', () => {
    expect(createSectionLoaders()).toEqual({
      workplace: false,
      department: false,
      employee: false,
      shift: false,
      schedule: false
    })
    expect(createSectionErrors()).toEqual({
      workplace: '',
      department: '',
      employee: '',
      shift: '',
      schedule: ''
    })
    expect(createSectionStates()).toEqual({
      workplace: false,
      department: false,
      employee: false,
      shift: false,
      schedule: false
    })
  })

  // 校验首页壳默认计数和推荐动作，保证 bootstrap 首屏在后端摘要返回前就有稳定导航骨架。
  it('creates the bootstrap shell counters and next-action defaults', () => {
    const shell = createBootstrapShell()

    expect(shell.sectionCounters).toEqual({
      workplace: 0,
      department: 0,
      employee: 0,
      shift: 0,
      schedule: 0
    })
    expect(shell.sectionStates.schedule).toBe(false)
    expect(shell.recommendedNextAction).toBe('wizard.schedule')
  })

  // 校验 workbench 主 composable 入口仍然以函数形式暴露，避免目录化转发入口被误删或改成错误值。
  it('keeps the workbench composition entry available through the test adapter', () => {
    expect(typeof useAttendanceWorkbench).toBe('function')
  })
})
