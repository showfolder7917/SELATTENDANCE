// 引入待测状态工厂，验证考勤工程在多区块模式下的初始壳状态是否稳定。
import {
  createBootstrapShell,
  createEmptyTenant,
  createSectionErrors,
  createSectionLoaders,
  createSectionStates,
  createWorkbenchState
} from '../../../src/projects/attendance/composables/workbench/state.js'

describe('attendance workbench state', () => {
  // 测试空租户工厂，保证首页壳在接口返回前就拥有稳定字段结构。
  it('creates an empty tenant shell', () => {
    const tenant = createEmptyTenant()

    expect(tenant).toEqual({
      tenantCode: '',
      tenantName: '',
      contactName: '',
      contactPhone: '',
      contactEmail: '',
      timezone: 'Asia/Tokyo'
    })
  })

  // 测试区块级加载和错误状态工厂，保证多接口并发场景下每个区块都能独立管理状态。
  it('creates section loader, error and state registries', () => {
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

  // 测试轻量首页壳工厂，保证推荐动作和区块计数具备宿主依赖的默认值。
  it('creates a bootstrap shell with counters and defaults', () => {
    const shell = createBootstrapShell()

    expect(shell.tenantSummary.timezone).toBe('Asia/Tokyo')
    expect(shell.recommendedNextAction).toBe('wizard.schedule')
    expect(shell.sectionCounters).toEqual({
      workplace: 0,
      department: 0,
      employee: 0,
      shift: 0,
      schedule: 0
    })
  })

  // 测试工作台主状态工厂，保证 section 壳、表单和排班板默认结构一次性就绪。
  it('creates a full workbench state for the attendance project', () => {
    const state = createWorkbenchState()

    expect(state.bootstrapShell.recommendedNextAction).toBe('wizard.schedule')
    expect(state.sectionLoaders.schedule).toBe(false)
    expect(state.sectionErrors.employee).toBe('')
    expect(state.scheduleFilters.month).toMatch(/^\d{4}-\d{2}$/)
    expect(state.scheduleBoard.scheduleItems).toEqual([])
    expect(state.workplaceForm.status).toBe('ACTIVE')
    expect(state.departmentForm.sortOrder).toBe(0)
    expect(state.employeeForm.employmentType).toBe('FULL_TIME')
    expect(state.shiftForm.templateCode).toBe('')
    expect(state.batchWizard.overwriteExisting).toBe(true)
  })
})
