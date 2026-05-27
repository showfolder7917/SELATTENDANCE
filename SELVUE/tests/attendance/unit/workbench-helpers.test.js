// 引入共享辅助函数，验证考勤工程的下载、提示和壳层计数逻辑不会在拆分后退化。
import {
  downloadCsv,
  pushToast,
  syncFilterReferences,
  syncFormDefaults,
  syncShellCounters
} from '../../../src/projects/attendance/composables/workbench/helpers.js'

describe('attendance workbench helpers', () => {
  // 每个用例前都重置浏览器辅助能力的 mock，避免下载和定时器状态串用。
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  // 测试 toast 写入与自动清空，保证区块动作反馈仍然维持现有交互节奏。
  it('pushes a toast message and clears it after the timeout', () => {
    vi.useFakeTimers()

    const toast = { value: '' }

    pushToast(toast, 'saved')
    expect(toast.value).toBe('saved')

    vi.advanceTimersByTime(2200)
    expect(toast.value).toBe('')

    vi.useRealTimers()
  })

  // 测试 CSV 下载桥接，保证服务端返回的文件名和内容会被正确转成浏览器下载动作。
  it('downloads csv payloads through a temporary browser link', () => {
    const click = vi.fn()
    const createElement = vi.spyOn(document, 'createElement').mockReturnValue({
      href: '',
      download: '',
      click
    })
    const createObjectUrl = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:csv')
    const revokeObjectUrl = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => {})

    downloadCsv({ fileName: 'attendance.csv', content: 'a,b' })

    expect(createElement).toHaveBeenCalledWith('a')
    expect(createObjectUrl).toHaveBeenCalled()
    expect(click).toHaveBeenCalledTimes(1)
    expect(revokeObjectUrl).toHaveBeenCalledWith('blob:csv')
  })

  // 测试默认表单回填，保证刷新后部门和员工表单会优先绑定首个可用主数据。
  it('synchronizes form defaults from the currently loaded master data', () => {
    const state = {
      workplaces: [{ id: 11 }, { id: 12 }],
      departments: [{ id: 21 }, { id: 22 }],
      departmentForm: { workplaceId: '' },
      employeeForm: { workplaceId: '', departmentId: '' }
    }

    syncFormDefaults(state)

    expect(state.departmentForm.workplaceId).toBe(11)
    expect(state.employeeForm.workplaceId).toBe(11)
    expect(state.employeeForm.departmentId).toBe(21)
  })

  // 测试失效筛选清理，保证主数据刷新后不会继续保留已经不存在的场所和部门引用。
  it('clears orphaned filter references after data refresh', () => {
    const state = {
      workplaces: [{ id: 11 }],
      departments: [{ id: 21 }],
      departmentFilters: { workplaceId: '99' },
      employeeFilters: { departmentId: '88' },
      scheduleFilters: { workplaceId: '77', departmentId: '66' }
    }

    syncFilterReferences(state)

    expect(state.departmentFilters.workplaceId).toBe('')
    expect(state.employeeFilters.departmentId).toBe('')
    expect(state.scheduleFilters.workplaceId).toBe('')
    expect(state.scheduleFilters.departmentId).toBe('')
  })

  // 测试壳层计数同步，保证宿主导航徽标会反映当前各区块数据量。
  it('synchronizes shell counters from section data', () => {
    const state = {
      workplaces: [{ id: 1 }, { id: 2 }],
      departments: [{ id: 3 }],
      employees: [{ id: 4 }, { id: 5 }, { id: 6 }],
      shiftTemplates: [{ id: 7 }],
      scheduleBoard: { scheduleItems: [{ id: 8 }, { id: 9 }] },
      bootstrapShell: {
        sectionCounters: {
          workplace: 0,
          department: 0,
          employee: 0,
          shift: 0,
          schedule: 0
        }
      }
    }

    syncShellCounters(state)

    expect(state.bootstrapShell.sectionCounters).toEqual({
      workplace: 2,
      department: 1,
      employee: 3,
      shift: 1,
      schedule: 2
    })
  })
})
