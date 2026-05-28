// 引入工作台状态工厂、toast、下载和壳层计数工具，覆盖排班模块最容易在重构后退化的本地行为。
import { createWorkbenchState, downloadCsv, pushToast, syncShellCounters } from '@tests-attendance'

describe('attendance unit schedule state', () => {
  // 校验排班筛选、看板、表单和批量向导的默认值，保证页面初次进入时就能围绕当月排班工作。
  it('creates schedule filters, board, form and batch wizard defaults', () => {
    const state = createWorkbenchState()

    expect(state.scheduleFilters).toMatchObject({
      workplaceId: '',
      departmentId: '',
      employeeKeyword: '',
      onlyUnassigned: false
    })
    expect(state.scheduleFilters.month).toMatch(/^\d{4}-\d{2}$/)
    expect(state.scheduleBoard).toMatchObject({
      dates: [],
      employeeRows: [],
      scheduleItems: [],
      endDate: ''
    })
    expect(state.scheduleForm).toMatchObject({
      selectedTemplateId: null,
      selectedEmployeeId: null,
      selectedWorkDate: '',
      selectedScheduleId: null
    })
    expect(state.batchWizard).toMatchObject({
      open: false,
      step: 1,
      employeeIds: [],
      shiftTemplateId: null,
      skipExisting: false,
      overwriteExisting: true
    })
    expect(state.sectionLoaders.schedule).toBe(false)
  })

  // 校验 shell 计数会随着排班和主数据量同步更新，保证侧边导航徽标始终反映当前工作量。
  it('synchronizes shell counters from current section data', () => {
    const state = createWorkbenchState()

    state.workplaces = [{ id: 1 }, { id: 2 }]
    state.departments = [{ id: 3 }]
    state.employees = [{ id: 4 }, { id: 5 }]
    state.shiftTemplates = [{ id: 6 }]
    state.scheduleBoard.scheduleItems = [{ id: 7 }, { id: 8 }, { id: 9 }]

    syncShellCounters(state)

    expect(state.bootstrapShell.sectionCounters).toEqual({
      workplace: 2,
      department: 1,
      employee: 2,
      shift: 1,
      schedule: 3
    })
  })

  // 校验 toast 自动清空和 CSV 下载桥接，长期盯住排班导出与保存反馈这两条高频交互不回退。
  it('keeps toast timing and csv download behavior stable', () => {
    vi.useFakeTimers()

    const toast = { value: '' }
    const click = vi.fn()
    const createElement = vi.spyOn(document, 'createElement').mockReturnValue({
      href: '',
      download: '',
      click
    })

    URL.createObjectURL ??= () => ''
    URL.revokeObjectURL ??= () => {}

    const createObjectUrl = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:csv')
    const revokeObjectUrl = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => {})

    pushToast(toast, 'saved')
    expect(toast.value).toBe('saved')

    vi.advanceTimersByTime(2200)
    expect(toast.value).toBe('')

    downloadCsv({ fileName: 'schedule.csv', content: 'a,b' })

    expect(createElement).toHaveBeenCalledWith('a')
    expect(createObjectUrl).toHaveBeenCalled()
    expect(click).toHaveBeenCalledTimes(1)
    expect(revokeObjectUrl).toHaveBeenCalledWith('blob:csv')

    vi.useRealTimers()
  })
})
